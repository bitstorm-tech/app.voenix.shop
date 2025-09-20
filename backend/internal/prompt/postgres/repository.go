package postgres

import (
	"context"
	"errors"

	"gorm.io/gorm"

	"voenix/backend/internal/article"
	"voenix/backend/internal/prompt"
	"voenix/backend/internal/vat"
)

// Repository provides a Postgres-backed implementation of prompt.Repository.
type Repository struct {
	db *gorm.DB
}

func NewRepository(db *gorm.DB) *Repository {
	return &Repository{db: db}
}

var _ prompt.Repository = (*Repository)(nil)

// helper type to wrap not-found handling consistently.
func wrapNotFound(err error) error {
	if err == nil {
		return nil
	}
	if errors.Is(err, gorm.ErrRecordNotFound) {
		return gorm.ErrRecordNotFound
	}
	return err
}

func (r *Repository) with(ctx context.Context) *gorm.DB {
	return r.db.WithContext(ctx)
}

func (r *Repository) promptQuery(ctx context.Context) *gorm.DB {
	return r.with(ctx).
		Preload("Category").
		Preload("Subcategory").
		Preload("Price", func(tx *gorm.DB) *gorm.DB { return tx.Table("prices") }).
		Preload("PromptSlotVariantMappings").
		Preload("PromptSlotVariantMappings.PromptSlotVariant").
		Preload("PromptSlotVariantMappings.PromptSlotVariant.PromptSlotType")
}

// Slot types

func (r *Repository) ListSlotTypes(ctx context.Context) ([]prompt.PromptSlotType, error) {
	var rows []promptSlotTypeRow
	if err := r.with(ctx).Order("id desc").Find(&rows).Error; err != nil {
		return nil, err
	}
	out := make([]prompt.PromptSlotType, 0, len(rows))
	for i := range rows {
		out = append(out, rows[i].toDomain())
	}
	return out, nil
}

func (r *Repository) SlotTypeByID(ctx context.Context, id int) (*prompt.PromptSlotType, error) {
	var row promptSlotTypeRow
	if err := r.with(ctx).First(&row, "id = ?", id).Error; err != nil {
		return nil, wrapNotFound(err)
	}
	domain := row.toDomain()
	return &domain, nil
}

func (r *Repository) SlotTypeNameExists(ctx context.Context, name string, excludeID *int) (bool, error) {
	query := r.with(ctx).Model(&promptSlotTypeRow{}).Where("name = ?", name)
	if excludeID != nil {
		query = query.Where("id <> ?", *excludeID)
	}
	var cnt int64
	if err := query.Count(&cnt).Error; err != nil {
		return false, err
	}
	return cnt > 0, nil
}

func (r *Repository) SlotTypePositionExists(ctx context.Context, position int, excludeID *int) (bool, error) {
	query := r.with(ctx).Model(&promptSlotTypeRow{}).Where("position = ?", position)
	if excludeID != nil {
		query = query.Where("id <> ?", *excludeID)
	}
	var cnt int64
	if err := query.Count(&cnt).Error; err != nil {
		return false, err
	}
	return cnt > 0, nil
}

func (r *Repository) CreateSlotType(ctx context.Context, slotType *prompt.PromptSlotType) error {
	row := promptSlotTypeRowFromDomain(slotType)
	if err := r.with(ctx).Create(&row).Error; err != nil {
		return err
	}
	*slotType = row.toDomain()
	return nil
}

func (r *Repository) SaveSlotType(ctx context.Context, slotType *prompt.PromptSlotType) error {
	row := promptSlotTypeRowFromDomain(slotType)
	if err := r.with(ctx).Save(&row).Error; err != nil {
		return err
	}
	*slotType = row.toDomain()
	return nil
}

func (r *Repository) DeleteSlotType(ctx context.Context, id int) error {
	res := r.with(ctx).Delete(&promptSlotTypeRow{}, "id = ?", id)
	if res.Error != nil {
		return wrapNotFound(res.Error)
	}
	if res.RowsAffected == 0 {
		return gorm.ErrRecordNotFound
	}
	return nil
}

// Slot variants

func (r *Repository) ListSlotVariants(ctx context.Context) ([]prompt.PromptSlotVariant, error) {
	var rows []promptSlotVariantRow
	if err := r.with(ctx).Preload("PromptSlotType").Order("id desc").Find(&rows).Error; err != nil {
		return nil, err
	}
	out := make([]prompt.PromptSlotVariant, 0, len(rows))
	for i := range rows {
		out = append(out, rows[i].toDomain())
	}
	return out, nil
}

func (r *Repository) SlotVariantByID(ctx context.Context, id int) (*prompt.PromptSlotVariant, error) {
	var row promptSlotVariantRow
	if err := r.with(ctx).Preload("PromptSlotType").First(&row, "id = ?", id).Error; err != nil {
		return nil, wrapNotFound(err)
	}
	domain := row.toDomain()
	return &domain, nil
}

func (r *Repository) SlotVariantNameExists(ctx context.Context, name string, excludeID *int) (bool, error) {
	query := r.with(ctx).Model(&promptSlotVariantRow{}).Where("name = ?", name)
	if excludeID != nil {
		query = query.Where("id <> ?", *excludeID)
	}
	var cnt int64
	if err := query.Count(&cnt).Error; err != nil {
		return false, err
	}
	return cnt > 0, nil
}

func (r *Repository) CreateSlotVariant(ctx context.Context, variant *prompt.PromptSlotVariant) error {
	row := promptSlotVariantRowFromDomain(variant)
	if err := r.with(ctx).Create(&row).Error; err != nil {
		return err
	}
	*variant = row.toDomain()
	return nil
}

func (r *Repository) SaveSlotVariant(ctx context.Context, variant *prompt.PromptSlotVariant) error {
	row := promptSlotVariantRowFromDomain(variant)
	if err := r.with(ctx).Save(&row).Error; err != nil {
		return err
	}
	*variant = row.toDomain()
	return nil
}

func (r *Repository) DeleteSlotVariant(ctx context.Context, id int) error {
	res := r.with(ctx).Delete(&promptSlotVariantRow{}, "id = ?", id)
	if res.Error != nil {
		return wrapNotFound(res.Error)
	}
	if res.RowsAffected == 0 {
		return gorm.ErrRecordNotFound
	}
	return nil
}

func (r *Repository) SlotTypeExists(ctx context.Context, id int) (bool, error) {
	var cnt int64
	if err := r.with(ctx).Model(&promptSlotTypeRow{}).Where("id = ?", id).Count(&cnt).Error; err != nil {
		return false, err
	}
	return cnt > 0, nil
}

func (r *Repository) SlotVariantsExist(ctx context.Context, ids []int) (bool, error) {
	if len(ids) == 0 {
		return true, nil
	}
	var cnt int64
	if err := r.with(ctx).Model(&promptSlotVariantRow{}).Where("id IN ?", ids).Count(&cnt).Error; err != nil {
		return false, err
	}
	return cnt == int64(len(ids)), nil
}

// Categories

func (r *Repository) ListCategories(ctx context.Context) ([]prompt.PromptCategory, error) {
	var rows []promptCategoryRow
	if err := r.with(ctx).Order("id desc").Find(&rows).Error; err != nil {
		return nil, err
	}
	out := make([]prompt.PromptCategory, 0, len(rows))
	for i := range rows {
		out = append(out, rows[i].toDomain())
	}
	return out, nil
}

func (r *Repository) CreateCategory(ctx context.Context, category *prompt.PromptCategory) error {
	row := promptCategoryRowFromDomain(category)
	if err := r.with(ctx).Create(&row).Error; err != nil {
		return err
	}
	*category = row.toDomain()
	return nil
}

func (r *Repository) CategoryByID(ctx context.Context, id int) (*prompt.PromptCategory, error) {
	var row promptCategoryRow
	if err := r.with(ctx).First(&row, "id = ?", id).Error; err != nil {
		return nil, wrapNotFound(err)
	}
	domain := row.toDomain()
	return &domain, nil
}

func (r *Repository) SaveCategory(ctx context.Context, category *prompt.PromptCategory) error {
	row := promptCategoryRowFromDomain(category)
	if err := r.with(ctx).Save(&row).Error; err != nil {
		return err
	}
	*category = row.toDomain()
	return nil
}

func (r *Repository) DeleteCategory(ctx context.Context, id int) error {
	res := r.with(ctx).Delete(&promptCategoryRow{}, "id = ?", id)
	if res.Error != nil {
		return wrapNotFound(res.Error)
	}
	if res.RowsAffected == 0 {
		return gorm.ErrRecordNotFound
	}
	return nil
}

func (r *Repository) CountPromptsByCategory(ctx context.Context, categoryID int) (int, error) {
	var cnt int64
	if err := r.with(ctx).Model(&promptRow{}).Where("category_id = ?", categoryID).Count(&cnt).Error; err != nil {
		return 0, err
	}
	return int(cnt), nil
}

func (r *Repository) CountSubCategoriesByCategory(ctx context.Context, categoryID int) (int, error) {
	var cnt int64
	if err := r.with(ctx).Model(&promptSubCategoryRow{}).Where("prompt_category_id = ?", categoryID).Count(&cnt).Error; err != nil {
		return 0, err
	}
	return int(cnt), nil
}

// Subcategories

func (r *Repository) ListSubCategories(ctx context.Context) ([]prompt.PromptSubCategory, error) {
	var rows []promptSubCategoryRow
	if err := r.with(ctx).Order("id desc").Find(&rows).Error; err != nil {
		return nil, err
	}
	out := make([]prompt.PromptSubCategory, 0, len(rows))
	for i := range rows {
		out = append(out, rows[i].toDomain())
	}
	return out, nil
}

func (r *Repository) ListSubCategoriesByCategory(ctx context.Context, categoryID int) ([]prompt.PromptSubCategory, error) {
	var rows []promptSubCategoryRow
	if err := r.with(ctx).Where("prompt_category_id = ?", categoryID).Order("id desc").Find(&rows).Error; err != nil {
		return nil, err
	}
	out := make([]prompt.PromptSubCategory, 0, len(rows))
	for i := range rows {
		out = append(out, rows[i].toDomain())
	}
	return out, nil
}

func (r *Repository) CreateSubCategory(ctx context.Context, subcategory *prompt.PromptSubCategory) error {
	row := promptSubCategoryRowFromDomain(subcategory)
	if err := r.with(ctx).Create(&row).Error; err != nil {
		return err
	}
	*subcategory = row.toDomain()
	return nil
}

func (r *Repository) SubCategoryByID(ctx context.Context, id int) (*prompt.PromptSubCategory, error) {
	var row promptSubCategoryRow
	if err := r.with(ctx).Preload("PromptCategory").First(&row, "id = ?", id).Error; err != nil {
		return nil, wrapNotFound(err)
	}
	domain := row.toDomain()
	return &domain, nil
}

func (r *Repository) SaveSubCategory(ctx context.Context, subcategory *prompt.PromptSubCategory) error {
	row := promptSubCategoryRowFromDomain(subcategory)
	if err := r.with(ctx).Save(&row).Error; err != nil {
		return err
	}
	*subcategory = row.toDomain()
	return nil
}

func (r *Repository) DeleteSubCategory(ctx context.Context, id int) error {
	res := r.with(ctx).Delete(&promptSubCategoryRow{}, "id = ?", id)
	if res.Error != nil {
		return wrapNotFound(res.Error)
	}
	if res.RowsAffected == 0 {
		return gorm.ErrRecordNotFound
	}
	return nil
}

func (r *Repository) CountPromptsBySubCategory(ctx context.Context, subCategoryID int) (int, error) {
	var cnt int64
	if err := r.with(ctx).Model(&promptRow{}).Where("subcategory_id = ?", subCategoryID).Count(&cnt).Error; err != nil {
		return 0, err
	}
	return int(cnt), nil
}

func (r *Repository) CategoryExists(ctx context.Context, id int) (bool, error) {
	var cnt int64
	if err := r.with(ctx).Model(&promptCategoryRow{}).Where("id = ?", id).Count(&cnt).Error; err != nil {
		return false, err
	}
	return cnt > 0, nil
}

// Prompts

func (r *Repository) ListPrompts(ctx context.Context) ([]prompt.Prompt, error) {
	var rows []promptRow
	if err := r.promptQuery(ctx).Order("id desc").Find(&rows).Error; err != nil {
		return nil, err
	}
	out := make([]prompt.Prompt, 0, len(rows))
	for i := range rows {
		out = append(out, rows[i].toDomain(true))
	}
	return out, nil
}

func (r *Repository) ListPublicPrompts(ctx context.Context) ([]prompt.Prompt, error) {
	var rows []promptRow
	if err := r.promptQuery(ctx).Where("active = ?", true).Order("id desc").Find(&rows).Error; err != nil {
		return nil, err
	}
	out := make([]prompt.Prompt, 0, len(rows))
	for i := range rows {
		out = append(out, rows[i].toDomain(true))
	}
	return out, nil
}

func (r *Repository) PromptByID(ctx context.Context, id int) (*prompt.Prompt, error) {
	var row promptRow
	if err := r.promptQuery(ctx).First(&row, "id = ?", id).Error; err != nil {
		return nil, wrapNotFound(err)
	}
	domain := row.toDomain(true)
	return &domain, nil
}

func (r *Repository) PromptsByIDs(ctx context.Context, ids []int) ([]prompt.Prompt, error) {
	if len(ids) == 0 {
		return []prompt.Prompt{}, nil
	}
	var rows []promptRow
	if err := r.promptQuery(ctx).Where("id IN ?", ids).Find(&rows).Error; err != nil {
		return nil, err
	}
	out := make([]prompt.Prompt, 0, len(rows))
	for i := range rows {
		out = append(out, rows[i].toDomain(true))
	}
	return out, nil
}

func (r *Repository) CreatePrompt(ctx context.Context, p *prompt.Prompt) error {
	row := promptRowFromDomain(p)
	if err := r.with(ctx).Create(&row).Error; err != nil {
		return err
	}
	*p = row.toDomain(false)
	return nil
}

func (r *Repository) SavePrompt(ctx context.Context, p *prompt.Prompt) error {
	row := promptRowFromDomain(p)
	if err := r.with(ctx).Save(&row).Error; err != nil {
		return err
	}
	*p = row.toDomain(false)
	return nil
}

func (r *Repository) DeletePrompt(ctx context.Context, id int) error {
	res := r.with(ctx).Delete(&promptRow{}, "id = ?", id)
	if res.Error != nil {
		return wrapNotFound(res.Error)
	}
	if res.RowsAffected == 0 {
		return gorm.ErrRecordNotFound
	}
	return nil
}

func (r *Repository) ReplacePromptSlotVariantMappings(ctx context.Context, promptID int, slotIDs []int) error {
	tx := r.with(ctx)
	if err := tx.Where("prompt_id = ?", promptID).Delete(&promptSlotVariantMappingRow{}).Error; err != nil {
		return err
	}
	if len(slotIDs) == 0 {
		return nil
	}
	rows := make([]promptSlotVariantMappingRow, 0, len(slotIDs))
	for _, id := range slotIDs {
		rows = append(rows, promptSlotVariantMappingRow{PromptID: promptID, SlotID: id})
	}
	return tx.Create(&rows).Error
}

// Prices and VAT

func (r *Repository) CreatePrice(ctx context.Context, price *article.Price) error {
	return r.with(ctx).Table("prices").Create(price).Error
}

func (r *Repository) PriceByID(ctx context.Context, id int) (*article.Price, error) {
	var pr article.Price
	if err := r.with(ctx).Table("prices").First(&pr, "id = ?", id).Error; err != nil {
		return nil, wrapNotFound(err)
	}
	return &pr, nil
}

func (r *Repository) SavePrice(ctx context.Context, price *article.Price) error {
	return r.with(ctx).Table("prices").Save(price).Error
}

func (r *Repository) VatExists(ctx context.Context, id int) (bool, error) {
	var cnt int64
	if err := r.with(ctx).Model(&vat.ValueAddedTax{}).Where("id = ?", id).Count(&cnt).Error; err != nil {
		return false, err
	}
	return cnt > 0, nil
}
