package ai

// ProviderLLM describes a supported LLM along with its provider metadata.
type ProviderLLM struct {
	Provider     string `json:"provider"`
	LLM          string `json:"llm"`
	FriendlyName string `json:"friendlyName"`
}

var providerLLMs = []ProviderLLM{
	{
		Provider:     "Google",
		LLM:          "gemini-2.5-flash-image-preview",
		FriendlyName: "Nano Banana (Google)",
	},
	{
		Provider:     "OpenAI",
		LLM:          "gpt-image-1",
		FriendlyName: "GPT-Image-1 (OpenAI)",
	},
	{
		Provider:     "Black Forest Labs",
		LLM:          "flux",
		FriendlyName: "Flux (Black Forest Labs)",
	},
}

// ProviderLLMs returns all configured provider + LLM combinations.
func ProviderLLMs() []ProviderLLM {
	out := make([]ProviderLLM, len(providerLLMs))
	copy(out, providerLLMs)
	return out
}

// ProviderLLMIDs exposes just the LLM identifiers for validation in other packages.
func ProviderLLMIDs() []string {
	ids := make([]string, 0, len(providerLLMs))
	for i := range providerLLMs {
		ids = append(ids, providerLLMs[i].LLM)
	}
	return ids
}
