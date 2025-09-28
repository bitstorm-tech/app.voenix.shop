package ai

import (
	"bytes"
	"context"
	"encoding/base64"
	"encoding/json"
	"errors"
	"fmt"
	"log"
	"net/http"
	"os"
	"strings"
	"sync"
	"time"

	img "voenix/backend/internal/image"
)

const (
	defaultGeminiModel = "gemini-2.5-flash-image-preview"
	geminiBaseURL      = "https://generativelanguage.googleapis.com/v1beta/models"
)

// GeminiGenerator implements ImageGenerator using Google Gemini Image (REST).
type GeminiGenerator struct {
	APIKey             string
	Model              string
	DefaultCandidates  int
	DefaultMaxTokens   *int
	DefaultTemperature *float64
	DefaultTimeout     time.Duration
	HTTPClient         *http.Client
	TargetAspectWidth  int
	TargetAspectHeight int
}

// NewGeminiGeneratorFromEnv constructs a GeminiGenerator using environment variables.
// - GOOGLE_API_KEY
// - GEMINI_IMAGE_MODEL (optional)
func NewGeminiGeneratorFromEnv() *GeminiGenerator {
	key := strings.TrimSpace(os.Getenv("GOOGLE_API_KEY"))
	model := strings.TrimSpace(os.Getenv("GEMINI_IMAGE_MODEL"))
	if model == "" {
		model = defaultGeminiModel
	}
	return &GeminiGenerator{
		APIKey:            key,
		Model:             model,
		DefaultCandidates: 1,
		DefaultTimeout:    60 * time.Second,
		HTTPClient:        &http.Client{Timeout: 60 * time.Second},
	}
}

// Edit sends an image + prompt to Gemini and returns generated images as bytes.
func (g *GeminiGenerator) Edit(ctx context.Context, image []byte, prompt string, n int) ([][]byte, error) {
	if strings.TrimSpace(g.APIKey) == "" {
		return nil, errors.New("GOOGLE_API_KEY is not configured")
	}
	model := strings.TrimSpace(g.Model)
	if model == "" {
		return nil, errors.New("model is not configured")
	}

	aspectWidth := g.TargetAspectWidth
	aspectHeight := g.TargetAspectHeight
	if aspectWidth <= 0 || aspectHeight <= 0 {
		aspectWidth = 16
		aspectHeight = 9
	}

	scaledImage, err := img.ScaleImageBytesToAspect(image, aspectWidth, aspectHeight)
	if err != nil {
		return nil, fmt.Errorf("failed to scale image to %d:%d aspect ratio: %w", aspectWidth, aspectHeight, err)
	}
	image = scaledImage

	mimeType := "image/png"
	if detectedMimeType := http.DetectContentType(image); strings.HasPrefix(detectedMimeType, "image/") {
		mimeType = detectedMimeType
	}

	requestedImageCount := n
	if requestedImageCount <= 0 {
		requestedImageCount = g.DefaultCandidates
		if requestedImageCount <= 0 {
			requestedImageCount = 1
		}
	}

	encodedImage := base64.StdEncoding.EncodeToString(image)

	contextWithCancel, cancel := context.WithCancel(ctx)
	defer cancel()

	type generationResult struct {
		images [][]byte
		err    error
	}

	resultsChannel := make(chan generationResult, requestedImageCount)
	var waitGroup sync.WaitGroup

	for requestIndex := 0; requestIndex < requestedImageCount; requestIndex++ {
		waitGroup.Add(1)
		go func() {
			defer waitGroup.Done()
			imagesPerRequest, requestErr := g.generateGeminiImages(contextWithCancel, model, mimeType, encodedImage, prompt)
			if requestErr != nil {
				resultsChannel <- generationResult{err: requestErr}
				return
			}
			resultsChannel <- generationResult{images: imagesPerRequest}
		}()
	}

	go func() {
		waitGroup.Wait()
		close(resultsChannel)
	}()

	var allImages [][]byte
	var firstError error
	for result := range resultsChannel {
		if result.err != nil {
			if firstError == nil {
				firstError = result.err
				cancel()
			}
			continue
		}
		if firstError != nil {
			continue
		}
		allImages = append(allImages, result.images...)
	}

	if firstError != nil {
		return nil, firstError
	}

	return allImages, nil
}

func (g *GeminiGenerator) generateGeminiImages(ctx context.Context, model string, mimeType string, encodedImage string, prompt string) ([][]byte, error) {
	body := createBody(mimeType, encodedImage, prompt, 1)

	generationConfig, ok := body["generationConfig"].(map[string]any)
	if !ok || generationConfig == nil {
		generationConfig = map[string]any{}
		body["generationConfig"] = generationConfig
	}
	if g.DefaultMaxTokens != nil {
		generationConfig["maxOutputTokens"] = *g.DefaultMaxTokens
	}
	if g.DefaultTemperature != nil {
		generationConfig["temperature"] = *g.DefaultTemperature
	}

	requestURL := fmt.Sprintf("%s/%s:generateContent?key=%s", geminiBaseURL, model, g.APIKey)
	payloadBytes, err := json.Marshal(body)
	if err != nil {
		return nil, fmt.Errorf("failed to marshal request body: %w", err)
	}

	client := g.HTTPClient
	if client == nil {
		client = &http.Client{}
	}

	requestContext := ctx
	var cancel context.CancelFunc
	if _, hasDeadline := requestContext.Deadline(); !hasDeadline && g.DefaultTimeout > 0 {
		requestContext, cancel = context.WithTimeout(requestContext, g.DefaultTimeout)
	}
	if cancel != nil {
		defer cancel()
	}

	request, err := http.NewRequestWithContext(requestContext, http.MethodPost, requestURL, bytes.NewReader(payloadBytes))
	if err != nil {
		return nil, err
	}
	request.Header.Set("Content-Type", "application/json")

	response, err := client.Do(request)
	if err != nil {
		return nil, err
	}
	defer func() { _ = response.Body.Close() }()

	var responseJSON map[string]any
	if err := json.NewDecoder(response.Body).Decode(&responseJSON); err != nil {
		return nil, err
	}
	if response.StatusCode < 200 || response.StatusCode >= 300 {
		if errorPayload, ok := responseJSON["error"].(map[string]any); ok {
			log.Printf("Gemini API error: http=%s code=%v status=%v message=%v", response.Status, errorPayload["code"], errorPayload["status"], errorPayload["message"])
			return nil, fmt.Errorf("gemini API error: code=%v status=%v message=%v", errorPayload["code"], errorPayload["status"], errorPayload["message"])
		}
		responseBytes, _ := json.Marshal(responseJSON)
		if len(responseBytes) > 1024 {
			responseBytes = responseBytes[:1024]
		}
		log.Printf("Gemini HTTP error: %s body=%s", response.Status, string(responseBytes))
		return nil, fmt.Errorf("gemini HTTP error: %s", response.Status)
	}

	if errorField, ok := responseJSON["error"]; ok && errorField != nil {
		if errorDetails, ok := errorField.(map[string]any); ok {
			return nil, fmt.Errorf("gemini API error: code=%v status=%v message=%v", errorDetails["code"], errorDetails["status"], errorDetails["message"])
		}
		return nil, fmt.Errorf("gemini API error: %v", errorField)
	}

	images := make([][]byte, 0, 1)
	var finishReasons []string
	if candidateList, ok := responseJSON["candidates"].([]any); ok {
		for _, candidate := range candidateList {
			candidateMap, _ := candidate.(map[string]any)
			if finishReason, _ := candidateMap["finishReason"].(string); finishReason != "" {
				finishReasons = append(finishReasons, finishReason)
			}
			contentMap, _ := candidateMap["content"].(map[string]any)
			partList, _ := contentMap["parts"].([]any)
			for _, part := range partList {
				partMap, _ := part.(map[string]any)
				inlineData, _ := partMap["inlineData"].(map[string]any)
				if inlineData == nil {
					inlineData, _ = partMap["inline_data"].(map[string]any)
				}
				if inlineData == nil {
					continue
				}
				mimeValue := inlineData["mimeType"]
				if mimeValue == nil {
					mimeValue = inlineData["mime_type"]
				}
				mimeValueString, _ := mimeValue.(string)
				if !strings.HasPrefix(mimeValueString, "image/") {
					continue
				}
				encodedData, _ := inlineData["data"].(string)
				if encodedData == "" {
					continue
				}
				decodedImage, decodeErr := base64.StdEncoding.DecodeString(encodedData)
				if decodeErr != nil {
					return nil, fmt.Errorf("failed to decode image data: %w", decodeErr)
				}
				images = append(images, decodedImage)
			}
		}
	}

	if len(images) == 0 {
		for _, finishReason := range finishReasons {
			if strings.EqualFold(finishReason, "PROHIBITED_CONTENT") || strings.EqualFold(finishReason, "SAFETY") {
				return nil, &SafetyBlockedError{Provider: ProviderGemini, Reason: finishReason}
			}
		}
		responseBytes, _ := json.Marshal(responseJSON)
		if len(responseBytes) > 2048 {
			responseBytes = responseBytes[:2048]
		}
		log.Printf("Gemini response had no image inlineData; model=%s candidateCount=%d respSnippet=%s", model, 1, string(responseBytes))
		return nil, errors.New("gemini response contained no image inlineData")
	}

	return images, nil
}

func createBody(mimeType string, encoded string, prompt string, candidateCount int) map[string]any {
	body := map[string]any{
		"contents": []any{
			map[string]any{
				"parts": []any{
					// Use snake_case for inline_data per REST examples
					map[string]any{"inline_data": map[string]any{"mime_type": mimeType, "data": encoded}},
					map[string]any{"text": prompt},
				},
			},
		},
		"generationConfig": map[string]any{
			// Only set canonical fields to avoid oneof conflicts
			"candidateCount": candidateCount,
		},
	}
	return body
}
