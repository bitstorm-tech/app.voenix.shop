package prompt

import (
	"context"

	"voenix/backend/internal/article"
)

// Repository defines the data access abstractions for the prompt domain.
// Implementations encapsulate all persistence logic (queries, preloads,
// transactions) so the service layer can focus on orchestration and validation.
type Repository interface {
	// Slot types
	ListSlotTypes(ctx context.Context) ([]PromptSlotType, error)
	SlotTypeByID(ctx context.Context, id int) (*PromptSlotType, error)
	SlotTypeNameExists(ctx context.Context, name string, excludeID *int) (bool, error)
	SlotTypePositionExists(ctx context.Context, position int, excludeID *int) (bool, error)
	CreateSlotType(ctx context.Context, slotType *PromptSlotType) error
	SaveSlotType(ctx context.Context, slotType *PromptSlotType) error
	DeleteSlotType(ctx context.Context, id int) error

	// Slot variants
	ListSlotVariants(ctx context.Context) ([]PromptSlotVariant, error)
	SlotVariantByID(ctx context.Context, id int) (*PromptSlotVariant, error)
	SlotVariantNameExists(ctx context.Context, name string, excludeID *int) (bool, error)
	CreateSlotVariant(ctx context.Context, variant *PromptSlotVariant) error
	SaveSlotVariant(ctx context.Context, variant *PromptSlotVariant) error
	DeleteSlotVariant(ctx context.Context, id int) error
	SlotTypeExists(ctx context.Context, id int) (bool, error)
	SlotVariantsExist(ctx context.Context, ids []int) (bool, error)

	// Categories
	ListCategories(ctx context.Context) ([]PromptCategory, error)
	CreateCategory(ctx context.Context, category *PromptCategory) error
	CategoryByID(ctx context.Context, id int) (*PromptCategory, error)
	SaveCategory(ctx context.Context, category *PromptCategory) error
	DeleteCategory(ctx context.Context, id int) error
	CountPromptsByCategory(ctx context.Context, categoryID int) (int, error)
	CountSubCategoriesByCategory(ctx context.Context, categoryID int) (int, error)

	// Subcategories
	ListSubCategories(ctx context.Context) ([]PromptSubCategory, error)
	ListSubCategoriesByCategory(ctx context.Context, categoryID int) ([]PromptSubCategory, error)
	CreateSubCategory(ctx context.Context, subcategory *PromptSubCategory) error
	SubCategoryByID(ctx context.Context, id int) (*PromptSubCategory, error)
	SaveSubCategory(ctx context.Context, subcategory *PromptSubCategory) error
	DeleteSubCategory(ctx context.Context, id int) error
	CountPromptsBySubCategory(ctx context.Context, subCategoryID int) (int, error)
	CategoryExists(ctx context.Context, id int) (bool, error)

	// Prompts
	ListPrompts(ctx context.Context) ([]Prompt, error)
	ListPublicPrompts(ctx context.Context) ([]Prompt, error)
	PromptByID(ctx context.Context, id int) (*Prompt, error)
	PromptsByIDs(ctx context.Context, ids []int) ([]Prompt, error)
	CreatePrompt(ctx context.Context, prompt *Prompt) error
	SavePrompt(ctx context.Context, prompt *Prompt) error
	DeletePrompt(ctx context.Context, id int) error
	ReplacePromptSlotVariantMappings(ctx context.Context, promptID int, slotIDs []int) error

	// Price and VAT helpers
	CreatePrice(ctx context.Context, price *article.Price) error
	PriceByID(ctx context.Context, id int) (*article.Price, error)
	SavePrice(ctx context.Context, price *article.Price) error
	VatExists(ctx context.Context, id int) (bool, error)
}
