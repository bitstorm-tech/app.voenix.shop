package ai

import (
	"context"
	"errors"
	"os"
	"strconv"
)

// Provider identifies the backing AI image generator implementation.
type Provider string

const (
	ProviderGemini Provider = "gemini"
	ProviderFlux   Provider = "flux"
	ProviderGPT    Provider = "gpt"
	ProviderMock   Provider = "mock"
)

// ImageGenerator edits/manipulates an input image according to a prompt and returns images.
type ImageGenerator interface {
	// Edit generates up to n images for the prompt using the input image as source.
	// Implementations should honor the request context for timeouts/cancellation and
	// use their own defaults for provider-specific tuning.
	Edit(ctx context.Context, image []byte, prompt string, n int) ([][]byte, error)
}

// Create returns an ImageGenerator implementation for the provider.
// Only Gemini is implemented; Flux and GPT return a stub error.
func Create(provider Provider) (ImageGenerator, error) {
	// In test mode, always force the mock generator regardless of requested provider.
	if IsTestMode() {
		return &MockGenerator{DefaultCandidates: 1}, nil
	}
	switch provider {
	case ProviderGemini:
		return NewGeminiGeneratorFromEnv(), nil
	case ProviderFlux:
		return nil, errors.New("flux image generator is not implemented yet")
	case ProviderGPT:
		return NewGPTImageGeneratorFromEnv(), nil
	case ProviderMock:
		return &MockGenerator{DefaultCandidates: 1}, nil
	default:
		return nil, errors.New("unknown AI image provider: " + string(provider))
	}
}

// IsTestMode reports whether TEST_MODE env var is truthy.
// Accepts standard Go boolean values (e.g., 1/0, true/false).
func IsTestMode() bool {
	v := os.Getenv("TEST_MODE")
	b, err := strconv.ParseBool(v)
	return err == nil && b
}
