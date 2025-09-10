package ai

import (
	"bytes"
	"context"
	"encoding/base64"
	"encoding/json"
	"errors"
	"fmt"
	"net/http"
	"os"
	"strings"
	"time"
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
func (g *GeminiGenerator) Edit(ctx context.Context, image []byte, prompt string, opts Options) ([][]byte, error) {
	if strings.TrimSpace(g.APIKey) == "" {
		return nil, errors.New("GOOGLE_API_KEY is not configured")
	}
	model := strings.TrimSpace(g.Model)
	if model == "" {
		return nil, errors.New("model is not configured")
	}

	// Prepare payload
	mime := opts.MimeType
	if strings.TrimSpace(mime) == "" {
		mime = "image/png"
	}
	cand := opts.CandidateCount
	if cand <= 0 {
		cand = g.DefaultCandidates
		if cand <= 0 {
			cand = 1
		}
	}

	// Inline image as base64 per Gemini API schema
	encoded := base64.StdEncoding.EncodeToString(image)

	body := map[string]any{
		"contents": []any{
			map[string]any{
				"parts": []any{
					// Use snake_case for inline_data per REST examples
					map[string]any{"inline_data": map[string]any{"mime_type": mime, "data": encoded}},
					map[string]any{"text": prompt},
				},
			},
		},
		"generationConfig": map[string]any{
			// Only set canonical fields to avoid oneof conflicts
			"candidateCount": cand,
		},
	}
	// Safely access generationConfig map without unchecked type assertions
	cfg, ok := body["generationConfig"].(map[string]any)
	if !ok || cfg == nil {
		cfg = map[string]any{}
		body["generationConfig"] = cfg
	}
	if opts.MaxOutputTokens != nil {
		cfg["maxOutputTokens"] = *opts.MaxOutputTokens
	} else if g.DefaultMaxTokens != nil {
		cfg["maxOutputTokens"] = *g.DefaultMaxTokens
	}
	if opts.Temperature != nil {
		cfg["temperature"] = *opts.Temperature
	} else if g.DefaultTemperature != nil {
		cfg["temperature"] = *g.DefaultTemperature
	}

	// Request
	url := fmt.Sprintf("%s/%s:generateContent?key=%s", geminiBaseURL, model, g.APIKey)
	b, err := json.Marshal(body)
	if err != nil {
		return nil, fmt.Errorf("failed to marshal request body: %w", err)
	}

	// Use provided context; honor per-request timeout if specified
	client := g.HTTPClient
	if client == nil {
		client = &http.Client{}
	}
	timeout := g.DefaultTimeout
	if opts.Timeout > 0 {
		timeout = opts.Timeout
	}
	if timeout > 0 {
		var cancel context.CancelFunc
		ctx, cancel = context.WithTimeout(ctx, timeout)
		defer cancel()
	}
	req, err := http.NewRequestWithContext(ctx, http.MethodPost, url, bytes.NewReader(b))
	if err != nil {
		return nil, err
	}
	req.Header.Set("Content-Type", "application/json")

	resp, err := client.Do(req)
	if err != nil {
		return nil, err
	}
	defer func() { _ = resp.Body.Close() }()

	var respJSON map[string]any
	if err := json.NewDecoder(resp.Body).Decode(&respJSON); err != nil {
		return nil, err
	}
	if resp.StatusCode < 200 || resp.StatusCode >= 300 {
		// Surface HTTP error with any error payload
		if e, ok := respJSON["error"].(map[string]any); ok {
			return nil, fmt.Errorf("gemini API error: code=%v status=%v message=%v", e["code"], e["status"], e["message"])
		}
		return nil, fmt.Errorf("gemini HTTP error: %s", resp.Status)
	}

	// Extract images from candidates[].content.parts[].inlineData
	images := make([][]byte, 0, cand)
	if e, ok := respJSON["error"]; ok && e != nil {
		if em, ok := e.(map[string]any); ok {
			return nil, fmt.Errorf("gemini API error: code=%v status=%v message=%v", em["code"], em["status"], em["message"])
		}
		return nil, fmt.Errorf("gemini API error: %v", e)
	}
	if cands, ok := respJSON["candidates"].([]any); ok {
		for _, c := range cands {
			cm, _ := c.(map[string]any)
			content, _ := cm["content"].(map[string]any)
			parts, _ := content["parts"].([]any)
			for _, p := range parts {
				pm, _ := p.(map[string]any)
				inline, _ := pm["inlineData"].(map[string]any)
				if inline == nil {
					inline, _ = pm["inline_data"].(map[string]any)
				}
				if inline == nil {
					continue
				}
				mimeAny := inline["mimeType"]
				if mimeAny == nil {
					mimeAny = inline["mime_type"]
				}
				mimeStr, _ := mimeAny.(string)
				if !strings.HasPrefix(mimeStr, "image/") {
					continue
				}
				data64, _ := inline["data"].(string)
				if data64 == "" {
					continue
				}
				b, err := base64.StdEncoding.DecodeString(data64)
				if err != nil {
					return nil, fmt.Errorf("failed to decode image data: %w", err)
				}
				images = append(images, b)
			}
		}
	}
	if len(images) == 0 {
		return nil, errors.New("gemini response contained no image inlineData")
	}
	return images, nil
}
