package ai

import (
	"context"
)

// MockGenerator is a dummy ImageGenerator that returns the input image bytes
// repeated N times. Useful for tests and offline development.
type MockGenerator struct {
	DefaultCandidates int
}

// Edit implements ImageGenerator by returning copies of the input image.
func (m *MockGenerator) Edit(_ context.Context, image []byte, _ string, n int) ([][]byte, error) {
	if n <= 0 {
		n = m.DefaultCandidates
		if n <= 0 {
			n = 1
		}
	}
	out := make([][]byte, 0, n)
	for i := 0; i < n; i++ {
		// copy to avoid sharing the same backing array
		cp := make([]byte, len(image))
		copy(cp, image)
		out = append(out, cp)
	}
	return out, nil
}
