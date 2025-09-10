package ai

import (
	"context"
	"errors"
	"time"
)

// Provider identifies the backing AI image generator implementation.
type Provider string

const (
	ProviderGemini Provider = "gemini"
	ProviderFlux   Provider = "flux"
	ProviderGPT    Provider = "gpt"
)

// Options configures an image edit request.
type Options struct {
	CandidateCount  int
	MimeType        string
	MaxOutputTokens *int
	Temperature     *float64
	Timeout         time.Duration
}

// ImageGenerator edits/manipulates an input image according to a prompt and returns images.
type ImageGenerator interface {
	Edit(ctx context.Context, image []byte, prompt string, opts Options) ([][]byte, error)
}

// Create returns an ImageGenerator implementation for the provider.
// Only Gemini is implemented; Flux and GPT return a stub error.
func Create(provider Provider) (ImageGenerator, error) {
	switch provider {
	case ProviderGemini:
		return NewGeminiGeneratorFromEnv(), nil
	case ProviderFlux:
		return nil, errors.New("Flux image generator is not implemented yet")
	case ProviderGPT:
		return nil, errors.New("GPT image generator is not implemented yet")
	default:
		return nil, errors.New("unknown AI image provider: " + string(provider))
	}
}
