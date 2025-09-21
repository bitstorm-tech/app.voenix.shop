package prompt

import (
	"time"

	"voenix/backend/internal/article"
)

// Domain entities for the prompt package. These types intentionally exclude
// persistence-layer or transport-specific struct tags so they can be reused by
// services, repositories, and DTO assemblers without cross-cutting concerns.

type PromptCategory struct {
	ID        int
	Name      string
	CreatedAt time.Time
	UpdatedAt time.Time
}

type PromptSubCategory struct {
	ID               int
	PromptCategoryID int
	PromptCategory   *PromptCategory
	Name             string
	Description      *string
	CreatedAt        time.Time
	UpdatedAt        time.Time
}

type PromptSlotType struct {
	ID        int
	Name      string
	Position  int
	CreatedAt time.Time
	UpdatedAt time.Time
}

type PromptSlotVariant struct {
	ID                   int
	PromptSlotTypeID     int
	PromptSlotType       *PromptSlotType
	Name                 string
	Prompt               *string
	Description          *string
	ExampleImageFilename *string
	LLM                  string
	CreatedAt            time.Time
	UpdatedAt            time.Time
}

// Association between prompts and slot variants.
type PromptSlotVariantMapping struct {
	PromptID          int
	SlotID            int
	Prompt            *Prompt
	PromptSlotVariant *PromptSlotVariant
	CreatedAt         time.Time
}

type Prompt struct {
	ID                        int
	Title                     string
	PromptText                *string
	CategoryID                *int
	Category                  *PromptCategory
	SubcategoryID             *int
	Subcategory               *PromptSubCategory
	PriceID                   *int
	Price                     *article.Price
	Active                    bool
	ExampleImageFilename      *string
	LLM                       *string
	PromptSlotVariantMappings []PromptSlotVariantMapping
	CreatedAt                 time.Time
	UpdatedAt                 time.Time
}
