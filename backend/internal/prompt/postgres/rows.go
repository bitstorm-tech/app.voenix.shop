package postgres

import (
	"time"

	"voenix/backend/internal/article"
	"voenix/backend/internal/prompt"
)

// Database representations used by the Postgres repository. These structs keep
// GORM configuration isolated from the rest of the prompt package.

type promptCategoryRow struct {
	ID        int    `gorm:"primaryKey"`
	Name      string `gorm:"size:255;uniqueIndex;not null"`
	CreatedAt time.Time
	UpdatedAt time.Time
}

func (promptCategoryRow) TableName() string { return "prompt_categories" }

func (r promptCategoryRow) toDomain() prompt.PromptCategory {
	return prompt.PromptCategory{
		ID:        r.ID,
		Name:      r.Name,
		CreatedAt: r.CreatedAt,
		UpdatedAt: r.UpdatedAt,
	}
}

func promptCategoryRowFromDomain(v *prompt.PromptCategory) promptCategoryRow {
	return promptCategoryRow{
		ID:        v.ID,
		Name:      v.Name,
		CreatedAt: v.CreatedAt,
		UpdatedAt: v.UpdatedAt,
	}
}

type promptSubCategoryRow struct {
	ID               int                `gorm:"primaryKey"`
	PromptCategoryID int                `gorm:"not null"`
	PromptCategory   *promptCategoryRow `gorm:"foreignKey:PromptCategoryID"`
	Name             string             `gorm:"size:255;not null"`
	Description      *string            `gorm:"type:text"`
	CreatedAt        time.Time
	UpdatedAt        time.Time
}

func (promptSubCategoryRow) TableName() string { return "prompt_subcategories" }

func (r promptSubCategoryRow) toDomain() prompt.PromptSubCategory {
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

func promptSubCategoryRowFromDomain(v *prompt.PromptSubCategory) promptSubCategoryRow {
	var category *promptCategoryRow
	if v.PromptCategory != nil {
		cat := promptCategoryRowFromDomain(v.PromptCategory)
		category = &cat
	}
	return promptSubCategoryRow{
		ID:               v.ID,
		PromptCategoryID: v.PromptCategoryID,
		PromptCategory:   category,
		Name:             v.Name,
		Description:      v.Description,
		CreatedAt:        v.CreatedAt,
		UpdatedAt:        v.UpdatedAt,
	}
}

type promptSlotTypeRow struct {
	ID        int    `gorm:"primaryKey"`
	Name      string `gorm:"size:255;uniqueIndex;not null"`
	Position  int    `gorm:"uniqueIndex;not null"`
	CreatedAt time.Time
	UpdatedAt time.Time
}

func (promptSlotTypeRow) TableName() string { return "prompt_slot_types" }

func (r promptSlotTypeRow) toDomain() prompt.PromptSlotType {
	return prompt.PromptSlotType{
		ID:        r.ID,
		Name:      r.Name,
		Position:  r.Position,
		CreatedAt: r.CreatedAt,
		UpdatedAt: r.UpdatedAt,
	}
}

func promptSlotTypeRowFromDomain(v *prompt.PromptSlotType) promptSlotTypeRow {
	return promptSlotTypeRow{
		ID:        v.ID,
		Name:      v.Name,
		Position:  v.Position,
		CreatedAt: v.CreatedAt,
		UpdatedAt: v.UpdatedAt,
	}
}

type promptSlotVariantRow struct {
	ID                   int                `gorm:"primaryKey"`
	PromptSlotTypeID     int                `gorm:"column:slot_type_id;not null"`
	PromptSlotType       *promptSlotTypeRow `gorm:"foreignKey:PromptSlotTypeID;references:ID"`
	Name                 string             `gorm:"size:255;uniqueIndex;not null"`
	Prompt               *string            `gorm:"type:text"`
	Description          *string            `gorm:"type:text"`
	ExampleImageFilename *string            `gorm:"size:500"`
	LLM                  string             `gorm:"size:255"`
	CreatedAt            time.Time
	UpdatedAt            time.Time
}

func (promptSlotVariantRow) TableName() string { return "prompt_slot_variants" }

func (r promptSlotVariantRow) toDomain() prompt.PromptSlotVariant {
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

func promptSlotVariantRowFromDomain(v *prompt.PromptSlotVariant) promptSlotVariantRow {
	var slotType *promptSlotTypeRow
	if v.PromptSlotType != nil {
		st := promptSlotTypeRowFromDomain(v.PromptSlotType)
		slotType = &st
	}
	return promptSlotVariantRow{
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

type promptSlotVariantMappingRow struct {
	PromptID          int                   `gorm:"primaryKey;column:prompt_id"`
	SlotID            int                   `gorm:"primaryKey;column:slot_id"`
	Prompt            *promptRow            `gorm:"foreignKey:PromptID;references:ID"`
	PromptSlotVariant *promptSlotVariantRow `gorm:"foreignKey:SlotID;references:ID"`
	CreatedAt         time.Time
}

func (promptSlotVariantMappingRow) TableName() string { return "prompt_slot_variant_mappings" }

func (r promptSlotVariantMappingRow) toDomain() prompt.PromptSlotVariantMapping {
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

func promptSlotVariantMappingRowsFromDomain(v []prompt.PromptSlotVariantMapping) []promptSlotVariantMappingRow {
	rows := make([]promptSlotVariantMappingRow, 0, len(v))
	for i := range v {
		rows = append(rows, promptSlotVariantMappingRow{
			PromptID:  v[i].PromptID,
			SlotID:    v[i].SlotID,
			CreatedAt: v[i].CreatedAt,
		})
	}
	return rows
}

type promptRow struct {
	ID                        int                           `gorm:"primaryKey"`
	Title                     string                        `gorm:"size:500;not null"`
	PromptText                *string                       `gorm:"type:text"`
	CategoryID                *int                          `gorm:"column:category_id"`
	Category                  *promptCategoryRow            `gorm:"foreignKey:CategoryID;references:ID"`
	SubcategoryID             *int                          `gorm:"column:subcategory_id"`
	Subcategory               *promptSubCategoryRow         `gorm:"foreignKey:SubcategoryID;references:ID"`
	PriceID                   *int                          `gorm:"column:price_id"`
	Price                     *article.Price                `gorm:"foreignKey:PriceID;references:ID"`
	Active                    bool                          `gorm:"not null;default:true"`
	ExampleImageFilename      *string                       `gorm:"size:500"`
	PromptSlotVariantMappings []promptSlotVariantMappingRow `gorm:"foreignKey:PromptID;references:ID"`
	CreatedAt                 time.Time
	UpdatedAt                 time.Time
}

func (promptRow) TableName() string { return "prompts" }

func (r promptRow) toDomain(includeMappings bool) prompt.Prompt {
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
		PromptSlotVariantMappings: mappings,
		CreatedAt:                 r.CreatedAt,
		UpdatedAt:                 r.UpdatedAt,
	}
}

func promptRowFromDomain(v *prompt.Prompt) promptRow {
	var category *promptCategoryRow
	if v.Category != nil {
		cat := promptCategoryRowFromDomain(v.Category)
		category = &cat
	}
	var subcategory *promptSubCategoryRow
	if v.Subcategory != nil {
		sub := promptSubCategoryRowFromDomain(v.Subcategory)
		subcategory = &sub
	}
	return promptRow{
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
		PromptSlotVariantMappings: promptSlotVariantMappingRowsFromDomain(v.PromptSlotVariantMappings),
		CreatedAt:                 v.CreatedAt,
		UpdatedAt:                 v.UpdatedAt,
	}
}

// Models returns the list of GORM models required for migrations.
func Models() []any {
	return []any{
		&promptCategoryRow{},
		&promptSubCategoryRow{},
		&promptSlotTypeRow{},
		&promptSlotVariantRow{},
		&promptSlotVariantMappingRow{},
		&promptRow{},
	}
}
