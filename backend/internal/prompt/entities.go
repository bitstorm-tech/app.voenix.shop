package prompt

import (
	"time"
	"voenix/backend/internal/article"
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
	ID                   int                      `gorm:"primaryKey" json:"id"`
	Title                string                   `gorm:"size:500;not null" json:"title"`
	PromptText           *string                  `gorm:"type:text" json:"promptText"`
	CategoryID           *int                     `gorm:"column:category_id" json:"categoryId"`
	Category             *PromptCategory          `gorm:"foreignKey:CategoryID;references:ID" json:"category,omitempty"`
	SubcategoryID        *int                     `gorm:"column:subcategory_id" json:"subcategoryId"`
	Subcategory          *PromptSubCategory       `gorm:"foreignKey:SubcategoryID;references:ID" json:"subcategory,omitempty"`
	PriceID              *int                     `gorm:"column:price_id" json:"priceId"`
	Price                *article.CostCalculation `gorm:"foreignKey:PriceID;references:ID" json:"-"`
	Active               bool                     `gorm:"not null;default:true" json:"active"`
	ExampleImageFilename *string                  `gorm:"size:500" json:"exampleImageFilename"`
	// mappings relation (manually loaded)
	PromptSlotVariantMappings []PromptSlotVariantMapping `gorm:"foreignKey:PromptID;references:ID" json:"promptSlotVariantMappings,omitempty"`
	CreatedAt                 time.Time                  `json:"createdAt"`
	UpdatedAt                 time.Time                  `json:"updatedAt"`
}

func (Prompt) TableName() string { return "prompts" }
