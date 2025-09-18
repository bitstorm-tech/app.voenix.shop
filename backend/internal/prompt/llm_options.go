package prompt

import (
	"os"
	"strings"
)

type LLMOption struct {
	ID    string `json:"id"`
	Label string `json:"label"`
}

const llmOptionsEnv = "PROMPT_SLOT_VARIANT_LLMS"

var defaultLLMOptions = []LLMOption{
	{ID: "gpt-4o", Label: "GPT-4o"},
	{ID: "gpt-4o-mini", Label: "GPT-4o Mini"},
	{ID: "gemini-1.5-pro", Label: "Gemini 1.5 Pro"},
}

func cloneLLMOptions(src []LLMOption) []LLMOption {
	out := make([]LLMOption, len(src))
	copy(out, src)
	return out
}

func loadLLMOptionsFromEnv() []LLMOption {
	raw := strings.TrimSpace(os.Getenv(llmOptionsEnv))
	if raw == "" {
		return cloneLLMOptions(defaultLLMOptions)
	}
	parts := strings.Split(raw, ",")
	options := make([]LLMOption, 0, len(parts))
	seen := make(map[string]struct{})
	for _, part := range parts {
		piece := strings.TrimSpace(part)
		if piece == "" {
			continue
		}
		id := piece
		label := piece
		if idx := strings.Index(piece, ":"); idx >= 0 {
			left := strings.TrimSpace(piece[:idx])
			right := strings.TrimSpace(piece[idx+1:])
			if left != "" {
				id = left
			}
			if right != "" {
				label = right
			} else {
				label = id
			}
		}
		if id == "" {
			continue
		}
		if _, exists := seen[id]; exists {
			continue
		}
		seen[id] = struct{}{}
		options = append(options, LLMOption{ID: id, Label: label})
	}
	if len(options) == 0 {
		return cloneLLMOptions(defaultLLMOptions)
	}
	return options
}
