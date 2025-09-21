package postgres

import (
	"time"

	"voenix/backend/internal/article"
	"voenix/backend/internal/prompt"
)

// Database representations used by the Postgres repository. These structs keep
// GORM configuration isolated from the rest of the prompt package.

type PromptCategoryRow struct {
	ID        int    `gorm:"primaryKey"`
	Name      string `gorm:"size:255;uniqueIndex;not null"`
	CreatedAt time.Time
	UpdatedAt time.Time
}

func (PromptCategoryRow) TableName() string {
	return "prompt_categories"
}

func (r PromptCategoryRow) toDomain() prompt.PromptCategory {
	return prompt.PromptCategory{
		ID:        r.ID,
		Name:      r.Name,
		CreatedAt: r.CreatedAt,
		UpdatedAt: r.UpdatedAt,
	}
}

func promptCategoryRowFromDomain(v *prompt.PromptCategory) PromptCategoryRow {
	return PromptCategoryRow{
		ID:        v.ID,
		Name:      v.Name,
		CreatedAt: v.CreatedAt,
		UpdatedAt: v.UpdatedAt,
	}
}

type PromptSubCategoryRow struct {
	ID               int                `gorm:"primaryKey"`
	PromptCategoryID int                `gorm:"not null"`
	PromptCategory   *PromptCategoryRow `gorm:"foreignKey:PromptCategoryID"`
	Name             string             `gorm:"size:255;not null"`
	Description      *string            `gorm:"type:text"`
	CreatedAt        time.Time
	UpdatedAt        time.Time
}

func (PromptSubCategoryRow) TableName() string {
	return "prompt_subcategories"
}

func (r PromptSubCategoryRow) toDomain() prompt.PromptSubCategory {
	var category *prompt.PromptCategory
	if r.PromptCategory != nil {
		cat := r.PromptCategory.toDomain()
		category = &cat
	}
	return prompt.PromptSubCategory{
		ID:               r.ID,
		PromptCategoryID: r.PromptCategoryID,
		PromptCategory:   category,
		Name:             r.Name,
		Description:      r.Description,
		CreatedAt:        r.CreatedAt,
		UpdatedAt:        r.UpdatedAt,
	}
}

func promptSubCategoryRowFromDomain(v *prompt.PromptSubCategory) PromptSubCategoryRow {
	var category *PromptCategoryRow
	if v.PromptCategory != nil {
		cat := promptCategoryRowFromDomain(v.PromptCategory)
		category = &cat
	}
	return PromptSubCategoryRow{
		ID:               v.ID,
		PromptCategoryID: v.PromptCategoryID,
		PromptCategory:   category,
		Name:             v.Name,
		Description:      v.Description,
		CreatedAt:        v.CreatedAt,
		UpdatedAt:        v.UpdatedAt,
	}
}

type PromptSlotTypeRow struct {
	ID        int    `gorm:"primaryKey"`
	Name      string `gorm:"size:255;uniqueIndex;not null"`
	Position  int    `gorm:"uniqueIndex;not null"`
	CreatedAt time.Time
	UpdatedAt time.Time
}

func (PromptSlotTypeRow) TableName() string {
	return "prompt_slot_types"
}

func (r PromptSlotTypeRow) toDomain() prompt.PromptSlotType {
	return prompt.PromptSlotType{
		ID:        r.ID,
		Name:      r.Name,
		Position:  r.Position,
		CreatedAt: r.CreatedAt,
		UpdatedAt: r.UpdatedAt,
	}
}

func promptSlotTypeRowFromDomain(v *prompt.PromptSlotType) PromptSlotTypeRow {
	return PromptSlotTypeRow{
		ID:        v.ID,
		Name:      v.Name,
		Position:  v.Position,
		CreatedAt: v.CreatedAt,
		UpdatedAt: v.UpdatedAt,
	}
}

type PromptSlotVariantRow struct {
	ID                   int                `gorm:"primaryKey"`
	PromptSlotTypeID     int                `gorm:"column:slot_type_id;not null"`
	PromptSlotType       *PromptSlotTypeRow `gorm:"foreignKey:PromptSlotTypeID;references:ID"`
	Name                 string             `gorm:"size:255;uniqueIndex;not null"`
	Prompt               *string            `gorm:"type:text"`
	Description          *string            `gorm:"type:text"`
	ExampleImageFilename *string            `gorm:"size:500"`
	LLM                  string             `gorm:"size:255"`
	CreatedAt            time.Time
	UpdatedAt            time.Time
}

func (PromptSlotVariantRow) TableName() string {
	return "prompt_slot_variants"
}

func (r PromptSlotVariantRow) toDomain() prompt.PromptSlotVariant {
	var slotType *prompt.PromptSlotType
	if r.PromptSlotType != nil {
		st := r.PromptSlotType.toDomain()
		slotType = &st
	}
	return prompt.PromptSlotVariant{
		ID:                   r.ID,
		PromptSlotTypeID:     r.PromptSlotTypeID,
		PromptSlotType:       slotType,
		Name:                 r.Name,
		Prompt:               r.Prompt,
		Description:          r.Description,
		ExampleImageFilename: r.ExampleImageFilename,
		LLM:                  r.LLM,
		CreatedAt:            r.CreatedAt,
		UpdatedAt:            r.UpdatedAt,
	}
}

func promptSlotVariantRowFromDomain(v *prompt.PromptSlotVariant) PromptSlotVariantRow {
	var slotType *PromptSlotTypeRow
	if v.PromptSlotType != nil {
		st := promptSlotTypeRowFromDomain(v.PromptSlotType)
		slotType = &st
	}
	return PromptSlotVariantRow{
		ID:                   v.ID,
		PromptSlotTypeID:     v.PromptSlotTypeID,
		PromptSlotType:       slotType,
		Name:                 v.Name,
		Prompt:               v.Prompt,
		Description:          v.Description,
		ExampleImageFilename: v.ExampleImageFilename,
		LLM:                  v.LLM,
		CreatedAt:            v.CreatedAt,
		UpdatedAt:            v.UpdatedAt,
	}
}

type PromptSlotVariantMappingRow struct {
	PromptID          int                   `gorm:"primaryKey;column:prompt_id"`
	SlotID            int                   `gorm:"primaryKey;column:slot_id"`
	Prompt            *PromptRow            `gorm:"foreignKey:PromptID;references:ID"`
	PromptSlotVariant *PromptSlotVariantRow `gorm:"foreignKey:SlotID;references:ID"`
	CreatedAt         time.Time
}

func (PromptSlotVariantMappingRow) TableName() string {
	return "prompt_slot_variant_mappings"
}

func (r PromptSlotVariantMappingRow) toDomain() prompt.PromptSlotVariantMapping {
	var p *prompt.Prompt
	if r.Prompt != nil {
		pDomain := r.Prompt.toDomain(false)
		p = &pDomain
	}
	var v *prompt.PromptSlotVariant
	if r.PromptSlotVariant != nil {
		variant := r.PromptSlotVariant.toDomain()
		v = &variant
	}
	return prompt.PromptSlotVariantMapping{
		PromptID:          r.PromptID,
		SlotID:            r.SlotID,
		Prompt:            p,
		PromptSlotVariant: v,
		CreatedAt:         r.CreatedAt,
	}
}

func promptSlotVariantMappingRowsFromDomain(v []prompt.PromptSlotVariantMapping) []PromptSlotVariantMappingRow {
	rows := make([]PromptSlotVariantMappingRow, 0, len(v))
	for i := range v {
		rows = append(rows, PromptSlotVariantMappingRow{
			PromptID:  v[i].PromptID,
			SlotID:    v[i].SlotID,
			CreatedAt: v[i].CreatedAt,
		})
	}
	return rows
}

type PromptRow struct {
	ID                        int                           `gorm:"primaryKey"`
	Title                     string                        `gorm:"size:500;not null"`
	PromptText                *string                       `gorm:"type:text"`
	CategoryID                *int                          `gorm:"column:category_id"`
	Category                  *PromptCategoryRow            `gorm:"foreignKey:CategoryID;references:ID"`
	SubcategoryID             *int                          `gorm:"column:subcategory_id"`
	Subcategory               *PromptSubCategoryRow         `gorm:"foreignKey:SubcategoryID;references:ID"`
	PriceID                   *int                          `gorm:"column:price_id"`
	Price                     *article.Price                `gorm:"foreignKey:PriceID;references:ID"`
	Active                    bool                          `gorm:"not null;default:true"`
	ExampleImageFilename      *string                       `gorm:"size:500"`
	LLM                       *string                       `gorm:"size:255"`
	PromptSlotVariantMappings []PromptSlotVariantMappingRow `gorm:"foreignKey:PromptID;references:ID"`
	CreatedAt                 time.Time
	UpdatedAt                 time.Time
}

func (PromptRow) TableName() string {
	return "prompts"
}

func (r PromptRow) toDomain(includeMappings bool) prompt.Prompt {
	var category *prompt.PromptCategory
	if r.Category != nil {
		cat := r.Category.toDomain()
		category = &cat
	}
	var subcategory *prompt.PromptSubCategory
	if r.Subcategory != nil {
		sub := r.Subcategory.toDomain()
		subcategory = &sub
	}
	mappings := make([]prompt.PromptSlotVariantMapping, 0, len(r.PromptSlotVariantMappings))
	if includeMappings {
		for i := range r.PromptSlotVariantMappings {
			mappings = append(mappings, r.PromptSlotVariantMappings[i].toDomain())
		}
	}
	return prompt.Prompt{
		ID:                        r.ID,
		Title:                     r.Title,
		PromptText:                r.PromptText,
		CategoryID:                r.CategoryID,
		Category:                  category,
		SubcategoryID:             r.SubcategoryID,
		Subcategory:               subcategory,
		PriceID:                   r.PriceID,
		Price:                     r.Price,
		Active:                    r.Active,
		ExampleImageFilename:      r.ExampleImageFilename,
		LLM:                       r.LLM,
		PromptSlotVariantMappings: mappings,
		CreatedAt:                 r.CreatedAt,
		UpdatedAt:                 r.UpdatedAt,
	}
}

func promptRowFromDomain(v *prompt.Prompt) PromptRow {
	var category *PromptCategoryRow
	if v.Category != nil {
		cat := promptCategoryRowFromDomain(v.Category)
		category = &cat
	}
	var subcategory *PromptSubCategoryRow
	if v.Subcategory != nil {
		sub := promptSubCategoryRowFromDomain(v.Subcategory)
		subcategory = &sub
	}
	return PromptRow{
		ID:                        v.ID,
		Title:                     v.Title,
		PromptText:                v.PromptText,
		CategoryID:                v.CategoryID,
		Category:                  category,
		SubcategoryID:             v.SubcategoryID,
		Subcategory:               subcategory,
		PriceID:                   v.PriceID,
		Price:                     v.Price,
		Active:                    v.Active,
		ExampleImageFilename:      v.ExampleImageFilename,
		LLM:                       v.LLM,
		PromptSlotVariantMappings: promptSlotVariantMappingRowsFromDomain(v.PromptSlotVariantMappings),
		CreatedAt:                 v.CreatedAt,
		UpdatedAt:                 v.UpdatedAt,
	}
}
