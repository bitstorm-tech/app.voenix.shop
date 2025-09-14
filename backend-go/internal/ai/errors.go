package ai

// SafetyBlockedError indicates the provider refused to generate content due to safety policies.
// Handlers may map this to HTTP 422 to give user-actionable feedback.
type SafetyBlockedError struct {
	Provider Provider
	Reason   string
}

func (e *SafetyBlockedError) Error() string {
	p := string(e.Provider)
	if p == "" {
		p = "ai"
	}
	if e.Reason == "" {
		return p + ": request blocked by safety policy"
	}
	return p + ": request blocked by safety policy: " + e.Reason
}
