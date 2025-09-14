package ai

import (
    "bytes"
    "context"
    "encoding/base64"
    "encoding/json"
    "errors"
    "fmt"
    "io"
    "mime/multipart"
    "net/http"
    "net/textproto"
    "os"
    "strconv"
	"strings"
	"time"
)

const (
	defaultGPTImageModel = "gpt-image-1"
	openAIBaseURL        = "https://api.openai.com/v1"
)

// GPTImageGenerator implements ImageGenerator using OpenAI Images (edits) API.
type GPTImageGenerator struct {
	APIKey            string
	Model             string
	BaseURL           string
	DefaultCandidates int
	DefaultTimeout    time.Duration
	HTTPClient        *http.Client
}

// NewGPTImageGeneratorFromEnv constructs a GPTImageGenerator using env vars:
// - OPENAI_API_KEY (required)
// - OPENAI_IMAGES_MODEL or OPENAI_IMAGE_MODEL (optional; defaults to gpt-image-1)
// - OPENAI_BASE_URL (optional; defaults to https://api.openai.com/v1)
func NewGPTImageGeneratorFromEnv() *GPTImageGenerator {
	key := strings.TrimSpace(os.Getenv("OPENAI_API_KEY"))
	model := strings.TrimSpace(os.Getenv("OPENAI_IMAGES_MODEL"))
	if model == "" {
		model = strings.TrimSpace(os.Getenv("OPENAI_IMAGE_MODEL"))
	}
	if model == "" {
		model = defaultGPTImageModel
	}
	base := strings.TrimSpace(os.Getenv("OPENAI_BASE_URL"))
	if base == "" {
		base = openAIBaseURL
	}
	return &GPTImageGenerator{
		APIKey:            key,
		Model:             model,
		BaseURL:           base,
		DefaultCandidates: 1,
		DefaultTimeout:    60 * time.Second,
		HTTPClient:        &http.Client{Timeout: 60 * time.Second},
	}
}

// Edit sends an image + prompt to OpenAI Images Edits and returns generated images as bytes.
func (g *GPTImageGenerator) Edit(ctx context.Context, image []byte, prompt string, opts Options) ([][]byte, error) {
	if strings.TrimSpace(g.APIKey) == "" {
		return nil, errors.New("OPENAI_API_KEY is not configured")
	}
	model := strings.TrimSpace(g.Model)
	if model == "" {
		model = defaultGPTImageModel
	}
	base := strings.TrimSpace(g.BaseURL)
	if base == "" {
		base = openAIBaseURL
	}

	// Candidate count (n)
	n := opts.CandidateCount
	if n <= 0 {
		n = g.DefaultCandidates
		if n <= 0 {
			n = 1
		}
	}
	if n > 10 { // reasonable cap
		n = 10
	}

	// MIME type used for file part; OpenAI accepts PNG/JPG
	mimeType := strings.TrimSpace(opts.MimeType)
	if mimeType == "" {
		mimeType = "image/png"
	}

    // Build multipart/form-data body per OpenAI Images Edits API
    var buf bytes.Buffer
    mw := multipart.NewWriter(&buf)

	// model
	_ = mw.WriteField("model", model)
	// prompt
	_ = mw.WriteField("prompt", prompt)
	// n (string)
	_ = mw.WriteField("n", strconv.Itoa(n))
    // Do NOT send response_format: some gpt-image-1 deployments reject it.
    // We'll handle either b64_json or url in the response.

	// image file part
	fh := make(textproto.MIMEHeader)
	fh.Set("Content-Disposition", `form-data; name="image"; filename="image.png"`)
	fh.Set("Content-Type", mimeType)
	fp, err := mw.CreatePart(fh)
	if err != nil {
		_ = mw.Close()
		return nil, err
	}
	if _, err := fp.Write(image); err != nil {
		_ = mw.Close()
		return nil, err
	}
	_ = mw.Close()

	// Prepare request
	url := strings.TrimRight(base, "/") + "/images/edits"
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

	req, err := http.NewRequestWithContext(ctx, http.MethodPost, url, &buf)
	if err != nil {
		return nil, err
	}
	req.Header.Set("Content-Type", mw.FormDataContentType())
	req.Header.Set("Authorization", "Bearer "+g.APIKey)

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
		// Surface error details if present
		if e, ok := respJSON["error"].(map[string]any); ok {
			return nil, fmt.Errorf("openai error: type=%v message=%v", e["type"], e["message"])
		}
		return nil, fmt.Errorf("openai HTTP error: %s", resp.Status)
	}

	// Parse data[].b64_json
    var out [][]byte
    if arr, ok := respJSON["data"].([]any); ok {
        for _, item := range arr {
            m, _ := item.(map[string]any)
            if b64, _ := m["b64_json"].(string); b64 != "" {
                b, err := base64.StdEncoding.DecodeString(b64)
                if err != nil {
                    return nil, fmt.Errorf("failed to decode image data: %w", err)
                }
                out = append(out, b)
                continue
            }
            if u, _ := m["url"].(string); u != "" {
                // Download the image bytes from the ephemeral URL
                imgReq, err := http.NewRequestWithContext(ctx, http.MethodGet, u, nil)
                if err != nil {
                    return nil, err
                }
                // use same client
                imgResp, err := client.Do(imgReq)
                if err != nil {
                    return nil, err
                }
                func() { defer func() { _ = imgResp.Body.Close() }() }()
                if imgResp.StatusCode < 200 || imgResp.StatusCode >= 300 {
                    return nil, fmt.Errorf("openai image download failed: %s", imgResp.Status)
                }
                b, err := io.ReadAll(imgResp.Body)
                if err != nil {
                    return nil, err
                }
                out = append(out, b)
                continue
            }
        }
    }
	if len(out) == 0 {
		return nil, errors.New("openai response contained no image data")
	}
	return out, nil
}
