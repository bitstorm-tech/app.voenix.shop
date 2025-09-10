package prompt

import (
	"time"
)

// GORM models mirroring Kotlin/Python prompt entities.

type PromptCategory struct {
	ID        int       `gorm:"primaryKey" json:"id"`
	Name      string    `gorm:"size:255;uniqueIndex;not null" json:"name"`
	CreatedAt time.Time `json:"createdAt"`
	UpdatedAt time.Time `json:"updatedAt"`
}

func (PromptCategory) TableName() string { return "prompt_categories" }

type PromptSubCategory struct {
	ID               int             `gorm:"primaryKey" json:"id"`
	PromptCategoryID int             `gorm:"not null" json:"promptCategoryId"`
	PromptCategory   *PromptCategory `gorm:"foreignKey:PromptCategoryID" json:"promptCategory,omitempty"`
	Name             string          `gorm:"size:255;not null" json:"name"`
	Description      *string         `gorm:"type:text" json:"description"`
	CreatedAt        time.Time       `json:"createdAt"`
	UpdatedAt        time.Time       `json:"updatedAt"`
}

func (PromptSubCategory) TableName() string { return "prompt_subcategories" }

type PromptSlotType struct {
	ID        int       `gorm:"primaryKey" json:"id"`
	Name      string    `gorm:"size:255;uniqueIndex;not null" json:"name"`
	Position  int       `gorm:"uniqueIndex;not null" json:"position"`
	CreatedAt time.Time `json:"createdAt"`
	UpdatedAt time.Time `json:"updatedAt"`
}

func (PromptSlotType) TableName() string { return "prompt_slot_types" }

type PromptSlotVariant struct {
	ID                   int             `gorm:"primaryKey" json:"id"`
	PromptSlotTypeID     int             `gorm:"column:slot_type_id;not null" json:"promptSlotTypeId"`
	PromptSlotType       *PromptSlotType `gorm:"foreignKey:PromptSlotTypeID;references:ID" json:"promptSlotType,omitempty"`
	Name                 string          `gorm:"size:255;uniqueIndex;not null" json:"name"`
	Prompt               *string         `gorm:"type:text" json:"prompt"`
	Description          *string         `gorm:"type:text" json:"description"`
	ExampleImageFilename *string         `gorm:"size:500" json:"exampleImageFilename"`
	CreatedAt            time.Time       `json:"createdAt"`
	UpdatedAt            time.Time       `json:"updatedAt"`
}

func (PromptSlotVariant) TableName() string { return "prompt_slot_variants" }

// Association table with composite PK: (prompt_id, slot_id)
type PromptSlotVariantMapping struct {
	PromptID          int                `gorm:"primaryKey;column:prompt_id" json:"promptId"`
	SlotID            int                `gorm:"primaryKey;column:slot_id" json:"slotId"`
	Prompt            *Prompt            `gorm:"foreignKey:PromptID;references:ID" json:"prompt,omitempty"`
	PromptSlotVariant *PromptSlotVariant `gorm:"foreignKey:SlotID;references:ID" json:"promptSlotVariant,omitempty"`
	CreatedAt         time.Time          `json:"createdAt"`
}

func (PromptSlotVariantMapping) TableName() string { return "prompt_slot_variant_mappings" }

type Prompt struct {
	ID                   int                `gorm:"primaryKey" json:"id"`
	Title                string             `gorm:"size:500;not null" json:"title"`
	PromptText           *string            `gorm:"type:text" json:"promptText"`
	CategoryID           *int               `gorm:"column:category_id" json:"categoryId"`
	Category             *PromptCategory    `gorm:"foreignKey:CategoryID;references:ID" json:"category,omitempty"`
	SubcategoryID        *int               `gorm:"column:subcategory_id" json:"subcategoryId"`
	Subcategory          *PromptSubCategory `gorm:"foreignKey:SubcategoryID;references:ID" json:"subcategory,omitempty"`
	Active               bool               `gorm:"not null;default:true" json:"active"`
	ExampleImageFilename *string            `gorm:"size:500" json:"exampleImageFilename"`
	// mappings relation (manually loaded)
	PromptSlotVariantMappings []PromptSlotVariantMapping `gorm:"foreignKey:PromptID;references:ID" json:"promptSlotVariantMappings,omitempty"`
	CreatedAt                 time.Time                  `json:"createdAt"`
	UpdatedAt                 time.Time                  `json:"updatedAt"`
}

func (Prompt) TableName() string { return "prompts" }

// -----------------------------
// Response DTOs (admin + public)
// -----------------------------

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
}

type PromptSummaryRead struct {
	ID    int    `json:"id"`
	Title string `json:"title"`
}
