package prompt

import "time"

// Response DTOs (admin + public)

type PromptSlotTypeRead struct {
	ID        int        `json:"id"`
	Name      string     `json:"name"`
	Position  int        `json:"position"`
	CreatedAt *time.Time `json:"createdAt"`
	UpdatedAt *time.Time `json:"updatedAt"`
}

type PromptSlotVariantRead struct {
	ID               int                 `json:"id"`
	PromptSlotTypeID int                 `json:"promptSlotTypeId"`
	PromptSlotType   *PromptSlotTypeRead `json:"promptSlotType,omitempty"`
	Name             string              `json:"name"`
	Prompt           *string             `json:"prompt"`
	Description      *string             `json:"description"`
	ExampleImageURL  *string             `json:"exampleImageUrl"`
	LLM              string              `json:"llm"`
	CreatedAt        *time.Time          `json:"createdAt"`
	UpdatedAt        *time.Time          `json:"updatedAt"`
}

type PromptCategoryRead struct {
	ID                 int        `json:"id"`
	Name               string     `json:"name"`
	PromptsCount       int        `json:"promptsCount"`
	SubcategoriesCount int        `json:"subcategoriesCount"`
	CreatedAt          *time.Time `json:"createdAt"`
	UpdatedAt          *time.Time `json:"updatedAt"`
}

type PromptSubCategoryRead struct {
	ID               int        `json:"id"`
	PromptCategoryID int        `json:"promptCategoryId"`
	Name             string     `json:"name"`
	Description      *string    `json:"description"`
	PromptsCount     int        `json:"promptsCount"`
	CreatedAt        *time.Time `json:"createdAt"`
	UpdatedAt        *time.Time `json:"updatedAt"`
}

type PromptRead struct {
	ID              int                     `json:"id"`
	Title           string                  `json:"title"`
	PromptText      *string                 `json:"promptText"`
	CategoryID      *int                    `json:"categoryId"`
	Category        *PromptCategoryRead     `json:"category"`
	SubcategoryID   *int                    `json:"subcategoryId"`
	Subcategory     *PromptSubCategoryRead  `json:"subcategory"`
	PriceID         *int                    `json:"priceId"`
	CostCalculation *costCalculationRequest `json:"costCalculation"`
	Active          bool                    `json:"active"`
	Slots           []PromptSlotVariantRead `json:"slots"`
	ExampleImageURL *string                 `json:"exampleImageUrl"`
	CreatedAt       *time.Time              `json:"createdAt"`
	UpdatedAt       *time.Time              `json:"updatedAt"`
}

// Public DTOs
type PublicPromptCategoryRead struct {
	ID   int    `json:"id"`
	Name string `json:"name"`
}

type PublicPromptSubCategoryRead struct {
	ID          int     `json:"id"`
	Name        string  `json:"name"`
	Description *string `json:"description"`
}

type PublicPromptSlotTypeRead struct {
	ID       int    `json:"id"`
	Name     string `json:"name"`
	Position int    `json:"position"`
}

type PublicPromptSlotRead struct {
	ID              int                       `json:"id"`
	Name            string                    `json:"name"`
	Description     *string                   `json:"description"`
	ExampleImageURL *string                   `json:"exampleImageUrl"`
	SlotType        *PublicPromptSlotTypeRead `json:"slotType"`
}

type PublicPromptRead struct {
	ID              int                          `json:"id"`
	Title           string                       `json:"title"`
	ExampleImageURL *string                      `json:"exampleImageUrl"`
	Category        *PublicPromptCategoryRead    `json:"category"`
	Subcategory     *PublicPromptSubCategoryRead `json:"subcategory"`
	Slots           []PublicPromptSlotRead       `json:"slots"`
	Price           *int                         `json:"price,omitempty"`
}

type PromptSummaryRead struct {
	ID    int    `json:"id"`
	Title string `json:"title"`
}
