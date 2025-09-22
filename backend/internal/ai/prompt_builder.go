package ai

import (
	"sort"
	"strings"

	"voenix/backend/internal/prompt"
)

// CombinePrompt builds a single prompt string from the base prompt and any slot variant prompts.
// It trims whitespace, preserves a stable ordering based on slot type position, slot name, and ID,
// and separates each section with a blank line for readability.
func CombinePrompt(basePrompt string, slotVariants []prompt.PromptSlotVariantRead) string {
	trimmedBasePrompt := strings.TrimSpace(basePrompt)
	promptSections := make([]string, 0, 1+len(slotVariants))
	if trimmedBasePrompt != "" {
		promptSections = append(promptSections, trimmedBasePrompt)
	}

	if len(slotVariants) == 0 {
		return strings.Join(promptSections, "\n\n")
	}

	filteredSlotVariants := make([]prompt.PromptSlotVariantRead, 0, len(slotVariants))
	for _, slotVariant := range slotVariants {
		if slotVariant.Prompt == nil {
			continue
		}
		trimmedVariantPrompt := strings.TrimSpace(*slotVariant.Prompt)
		if trimmedVariantPrompt == "" {
			continue
		}
		filteredSlotVariants = append(filteredSlotVariants, slotVariant)
	}

	sort.SliceStable(filteredSlotVariants, func(i, j int) bool {
		iSlot := filteredSlotVariants[i]
		jSlot := filteredSlotVariants[j]
		iPosition := 0
		if iSlot.PromptSlotType != nil {
			iPosition = iSlot.PromptSlotType.Position
		}
		jPosition := 0
		if jSlot.PromptSlotType != nil {
			jPosition = jSlot.PromptSlotType.Position
		}
		if iPosition != jPosition {
			return iPosition < jPosition
		}
		if iSlot.Name != jSlot.Name {
			return iSlot.Name < jSlot.Name
		}
		return iSlot.ID < jSlot.ID
	})

	for _, slotVariant := range filteredSlotVariants {
		if slotVariant.Prompt == nil {
			continue
		}
		trimmedVariantPrompt := strings.TrimSpace(*slotVariant.Prompt)
		if trimmedVariantPrompt == "" {
			continue
		}
		promptSections = append(promptSections, trimmedVariantPrompt)
	}

	return strings.Join(promptSections, "\n\n")
}
