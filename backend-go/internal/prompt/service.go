package prompt

import (
	"context"
	"errors"

	"gorm.io/gorm"
)

// service centralizes all DB access and business logic for prompts.
type service struct {
	db *gorm.DB
}

func newService(db *gorm.DB) *service { return &service{db: db} }

type conflictError struct{ Detail string }

func (e conflictError) Error() string { return e.Detail }

// ListSlotTypes returns all slot types.
func (s *service) listSlotTypes(ctx context.Context) ([]PromptSlotTypeRead, error) {
	var rows []PromptSlotType
	if err := s.db.WithContext(ctx).Order("id desc").Find(&rows).Error; err != nil {
		return nil, err
	}
	out := make([]PromptSlotTypeRead, 0, len(rows))
	for i := range rows {
		out = append(out, toSlotTypeRead(&rows[i]))
	}
	return out, nil
}

// GetSlotType returns a slot type by id. Returns (nil, nil) if not found.
func (s *service) getSlotType(ctx context.Context, id int) (*PromptSlotTypeRead, error) {
	var row PromptSlotType
	if err := s.db.WithContext(ctx).First(&row, "id = ?", id).Error; err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, nil
		}
		return nil, err
	}
	v := toSlotTypeRead(&row)
	return &v, nil
}

func (s *service) createSlotType(ctx context.Context, name string, position int) (*PromptSlotTypeRead, error) {
	// unique checks
	var cnt int64
	s.db.WithContext(ctx).Model(&PromptSlotType{}).Where("name = ?", name).Count(&cnt)
	if cnt > 0 {
		return nil, conflictError{Detail: "PromptSlotType name already exists"}
	}
	s.db.WithContext(ctx).Model(&PromptSlotType{}).Where("position = ?", position).Count(&cnt)
	if cnt > 0 {
		return nil, conflictError{Detail: "PromptSlotType position already exists"}
	}
	row := PromptSlotType{Name: name, Position: position}
	if err := s.db.WithContext(ctx).Create(&row).Error; err != nil {
		return nil, err
	}
	v := toSlotTypeRead(&row)
	return &v, nil
}

func (s *service) updateSlotType(ctx context.Context, id int, name *string, position *int) (*PromptSlotTypeRead, error) {
	var existing PromptSlotType
	if err := s.db.WithContext(ctx).First(&existing, "id = ?", id).Error; err != nil {
		return nil, err
	}
	if name != nil && *name != existing.Name {
		var cnt int64
		s.db.WithContext(ctx).Model(&PromptSlotType{}).Where("name = ? AND id <> ?", *name, existing.ID).Count(&cnt)
		if cnt > 0 {
			return nil, conflictError{Detail: "PromptSlotType name already exists"}
		}
		existing.Name = *name
	}
	if position != nil && *position != existing.Position {
		var cnt int64
		s.db.WithContext(ctx).Model(&PromptSlotType{}).Where("position = ? AND id <> ?", *position, existing.ID).Count(&cnt)
		if cnt > 0 {
			return nil, conflictError{Detail: "PromptSlotType position already exists"}
		}
		existing.Position = *position
	}
	if err := s.db.WithContext(ctx).Save(&existing).Error; err != nil {
		return nil, err
	}
	v := toSlotTypeRead(&existing)
	return &v, nil
}

func (s *service) deleteSlotType(ctx context.Context, id int) error {
	return s.db.WithContext(ctx).Delete(&PromptSlotType{}, "id = ?", id).Error
}

// ---------------------
// Slot Variants (Admin)
// ---------------------

func (s *service) listSlotVariants(ctx context.Context) ([]PromptSlotVariantRead, error) {
	var rows []PromptSlotVariant
	if err := s.db.WithContext(ctx).Preload("PromptSlotType").Order("id desc").Find(&rows).Error; err != nil {
		return nil, err
	}
	out := make([]PromptSlotVariantRead, 0, len(rows))
	for i := range rows {
		out = append(out, toSlotVariantRead(&rows[i]))
	}
	return out, nil
}

func (s *service) getSlotVariant(ctx context.Context, id int) (*PromptSlotVariantRead, error) {
	var row PromptSlotVariant
	if err := s.db.WithContext(ctx).Preload("PromptSlotType").First(&row, "id = ?", id).Error; err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, nil
		}
		return nil, err
	}
	v := toSlotVariantRead(&row)
	return &v, nil
}

func (s *service) createSlotVariant(ctx context.Context, payload slotVariantCreate) (*PromptSlotVariantRead, error) {
	if !existsByID[PromptSlotType](s.db.WithContext(ctx), payload.PromptSlotTypeID) {
		return nil, gorm.ErrRecordNotFound
	}
	var cnt int64
	s.db.WithContext(ctx).Model(&PromptSlotVariant{}).Where("name = ?", payload.Name).Count(&cnt)
	if cnt > 0 {
		return nil, conflictError{Detail: "PromptSlotVariant name already exists"}
	}
	row := PromptSlotVariant{
		PromptSlotTypeID:     payload.PromptSlotTypeID,
		Name:                 payload.Name,
		Prompt:               payload.Prompt,
		Description:          payload.Description,
		ExampleImageFilename: payload.ExampleImageFilename,
	}
	if err := s.db.WithContext(ctx).Create(&row).Error; err != nil {
		return nil, err
	}
	_ = s.db.WithContext(ctx).Preload("PromptSlotType").First(&row, row.ID).Error
	v := toSlotVariantRead(&row)
	return &v, nil
}

func (s *service) updateSlotVariant(ctx context.Context, id int, payload slotVariantUpdate) (*PromptSlotVariantRead, error) {
	var existing PromptSlotVariant
	if err := s.db.WithContext(ctx).First(&existing, "id = ?", id).Error; err != nil {
		return nil, err
	}
	if payload.PromptSlotTypeID != nil && *payload.PromptSlotTypeID != existing.PromptSlotTypeID {
		if !existsByID[PromptSlotType](s.db.WithContext(ctx), *payload.PromptSlotTypeID) {
			return nil, gorm.ErrRecordNotFound
		}
		existing.PromptSlotTypeID = *payload.PromptSlotTypeID
	}
	if payload.Name != nil && *payload.Name != existing.Name {
		var cnt int64
		s.db.WithContext(ctx).Model(&PromptSlotVariant{}).Where("name = ? AND id <> ?", *payload.Name, existing.ID).Count(&cnt)
		if cnt > 0 {
			return nil, conflictError{Detail: "PromptSlotVariant name already exists"}
		}
		existing.Name = *payload.Name
	}
	if payload.Prompt != nil {
		existing.Prompt = payload.Prompt
	}
	if payload.Description != nil {
		existing.Description = payload.Description
	}
	if payload.ExampleImageFilename != nil {
		old := existing.ExampleImageFilename
		if old != nil && (payload.ExampleImageFilename == nil || *old != *payload.ExampleImageFilename) {
			safeDeletePublicImage(*old, "slot-variant")
		}
		existing.ExampleImageFilename = payload.ExampleImageFilename
	}
	if err := s.db.WithContext(ctx).Save(&existing).Error; err != nil {
		return nil, err
	}
	_ = s.db.WithContext(ctx).Preload("PromptSlotType").First(&existing, existing.ID).Error
	v := toSlotVariantRead(&existing)
	return &v, nil
}

func (s *service) deleteSlotVariant(ctx context.Context, id int) error {
	var existing PromptSlotVariant
	if err := s.db.WithContext(ctx).First(&existing, "id = ?", id).Error; err != nil {
		return err
	}
	if existing.ExampleImageFilename != nil {
		safeDeletePublicImage(*existing.ExampleImageFilename, "slot-variant")
	}
	return s.db.WithContext(ctx).Delete(&PromptSlotVariant{}, existing.ID).Error
}

// ---------------
// Categories Admin
// ---------------

func (s *service) listCategories(ctx context.Context) ([]PromptCategoryRead, error) {
	var rows []PromptCategory
	if err := s.db.WithContext(ctx).Order("id desc").Find(&rows).Error; err != nil {
		return nil, err
	}
	out := make([]PromptCategoryRead, 0, len(rows))
	for i := range rows {
		pc := rows[i]
		out = append(out, PromptCategoryRead{
			ID:                 pc.ID,
			Name:               pc.Name,
			PromptsCount:       s.countPromptsByCategory(ctx, pc.ID),
			SubcategoriesCount: s.countSubCategoriesByCategory(ctx, pc.ID),
			CreatedAt:          timePtr(pc.CreatedAt),
			UpdatedAt:          timePtr(pc.UpdatedAt),
		})
	}
	return out, nil
}

func (s *service) createCategory(ctx context.Context, name string) (*PromptCategoryRead, error) {
	row := PromptCategory{Name: name}
	if err := s.db.WithContext(ctx).Create(&row).Error; err != nil {
		return nil, err
	}
	v := PromptCategoryRead{
		ID:                 row.ID,
		Name:               row.Name,
		PromptsCount:       0,
		SubcategoriesCount: 0,
		CreatedAt:          timePtr(row.CreatedAt),
		UpdatedAt:          timePtr(row.UpdatedAt),
	}
	return &v, nil
}

func (s *service) updateCategory(ctx context.Context, id int, name *string) (*PromptCategoryRead, error) {
	var existing PromptCategory
	if err := s.db.WithContext(ctx).First(&existing, "id = ?", id).Error; err != nil {
		return nil, err
	}
	if name != nil {
		existing.Name = *name
	}
	if err := s.db.WithContext(ctx).Save(&existing).Error; err != nil {
		return nil, err
	}
	v := PromptCategoryRead{
		ID:                 existing.ID,
		Name:               existing.Name,
		PromptsCount:       s.countPromptsByCategory(ctx, existing.ID),
		SubcategoriesCount: s.countSubCategoriesByCategory(ctx, existing.ID),
		CreatedAt:          timePtr(existing.CreatedAt),
		UpdatedAt:          timePtr(existing.UpdatedAt),
	}
	return &v, nil
}

func (s *service) deleteCategory(ctx context.Context, id int) error {
	var existing PromptCategory
	if err := s.db.WithContext(ctx).First(&existing, "id = ?", id).Error; err != nil {
		return err
	}
	return s.db.WithContext(ctx).Delete(&PromptCategory{}, existing.ID).Error
}

// ------------------
// Subcategories Admin
// ------------------

func (s *service) listSubCategories(ctx context.Context) ([]PromptSubCategoryRead, error) {
	var rows []PromptSubCategory
	if err := s.db.WithContext(ctx).Order("id desc").Find(&rows).Error; err != nil {
		return nil, err
	}
	out := make([]PromptSubCategoryRead, 0, len(rows))
	for i := range rows {
		out = append(out, toSubCategoryRead(s.db, &rows[i]))
	}
	return out, nil
}

func (s *service) listSubCategoriesByCategory(ctx context.Context, categoryID int) ([]PromptSubCategoryRead, error) {
	var rows []PromptSubCategory
	if err := s.db.WithContext(ctx).Where("prompt_category_id = ?", categoryID).Order("id desc").Find(&rows).Error; err != nil {
		return nil, err
	}
	out := make([]PromptSubCategoryRead, 0, len(rows))
	for i := range rows {
		out = append(out, toSubCategoryRead(s.db, &rows[i]))
	}
	return out, nil
}

func (s *service) createSubCategory(ctx context.Context, payload subcatCreate) (*PromptSubCategoryRead, error) {
	if !existsByID[PromptCategory](s.db.WithContext(ctx), payload.PromptCategoryID) {
		return nil, gorm.ErrRecordNotFound
	}
	row := PromptSubCategory{PromptCategoryID: payload.PromptCategoryID, Name: payload.Name, Description: payload.Description}
	if err := s.db.WithContext(ctx).Create(&row).Error; err != nil {
		return nil, err
	}
	v := toSubCategoryRead(s.db, &row)
	return &v, nil
}

func (s *service) updateSubCategory(ctx context.Context, id int, payload subcatUpdate) (*PromptSubCategoryRead, error) {
	var existing PromptSubCategory
	if err := s.db.WithContext(ctx).First(&existing, "id = ?", id).Error; err != nil {
		return nil, err
	}
	if payload.PromptCategoryID != nil {
		if !existsByID[PromptCategory](s.db.WithContext(ctx), *payload.PromptCategoryID) {
			return nil, gorm.ErrRecordNotFound
		}
		existing.PromptCategoryID = *payload.PromptCategoryID
	}
	if payload.Name != nil {
		existing.Name = *payload.Name
	}
	if payload.Description != nil {
		existing.Description = payload.Description
	}
	if err := s.db.WithContext(ctx).Save(&existing).Error; err != nil {
		return nil, err
	}
	v := toSubCategoryRead(s.db, &existing)
	return &v, nil
}

func (s *service) deleteSubCategory(ctx context.Context, id int) error {
	return s.db.WithContext(ctx).Delete(&PromptSubCategory{}, "id = ?", id).Error
}

// ---------
// Prompts
// ---------

func (s *service) listPrompts(ctx context.Context) ([]PromptRead, error) {
	rows, err := allPromptsWithRelations(s.db.WithContext(ctx))
	if err != nil {
		return nil, err
	}
	out := make([]PromptRead, 0, len(rows))
	for i := range rows {
		out = append(out, toPromptRead(s.db, &rows[i]))
	}
	return out, nil
}

func (s *service) getPrompt(ctx context.Context, id int) (*PromptRead, error) {
	row, err := loadPromptWithRelations(s.db.WithContext(ctx), id)
	if err != nil {
		return nil, err
	}
	if row == nil {
		return nil, nil
	}
	v := toPromptRead(s.db, row)
	return &v, nil
}

func (s *service) createPrompt(ctx context.Context, payload promptCreate) (*PromptRead, error) {
	// Validate relationships
	if payload.CategoryID != nil && !existsByID[PromptCategory](s.db.WithContext(ctx), *payload.CategoryID) {
		return nil, gorm.ErrRecordNotFound
	}
	if payload.SubcategoryID != nil {
		var sc PromptSubCategory
		if err := s.db.WithContext(ctx).First(&sc, "id = ?", *payload.SubcategoryID).Error; err != nil {
			return nil, err
		}
		if payload.CategoryID != nil && sc.PromptCategoryID != *payload.CategoryID {
			return nil, errors.New("subcategory does not belong to the specified category")
		}
	}
	// Validate slots
	slotIDs := uniqueSlotIDs(payload.Slots)
	if len(slotIDs) > 0 {
		var cnt int64
		if err := s.db.WithContext(ctx).Model(&PromptSlotVariant{}).Where("id IN ?", slotIDs).Count(&cnt).Error; err != nil {
			return nil, err
		}
		if cnt != int64(len(slotIDs)) {
			return nil, gorm.ErrRecordNotFound
		}
	}
	row := Prompt{
		Title:                payload.Title,
		PromptText:           payload.PromptText,
		CategoryID:           payload.CategoryID,
		SubcategoryID:        payload.SubcategoryID,
		Active:               true,
		ExampleImageFilename: payload.ExampleImageFilename,
	}
	if err := s.db.WithContext(ctx).Create(&row).Error; err != nil {
		return nil, err
	}
	for _, sid := range slotIDs {
		_ = s.db.WithContext(ctx).Create(&PromptSlotVariantMapping{PromptID: row.ID, SlotID: sid}).Error
	}
	reloaded, _ := loadPromptWithRelations(s.db.WithContext(ctx), row.ID)
	v := toPromptRead(s.db, reloaded)
	return &v, nil
}

func (s *service) updatePrompt(ctx context.Context, id int, payload promptUpdate) (*PromptRead, error) {
	var existing Prompt
	if err := s.db.WithContext(ctx).First(&existing, "id = ?", id).Error; err != nil {
		return nil, err
	}
	// Validate category/subcategory
	if payload.CategoryID != nil && !existsByID[PromptCategory](s.db.WithContext(ctx), *payload.CategoryID) {
		return nil, gorm.ErrRecordNotFound
	}
	if payload.SubcategoryID != nil {
		var sc PromptSubCategory
		if err := s.db.WithContext(ctx).First(&sc, "id = ?", *payload.SubcategoryID).Error; err != nil {
			return nil, err
		}
		if payload.CategoryID != nil && sc.PromptCategoryID != *payload.CategoryID {
			return nil, errors.New("subcategory does not belong to the specified category")
		}
	}
	// Apply changes
	if payload.Title != nil {
		existing.Title = *payload.Title
	}
	if payload.PromptText != nil {
		existing.PromptText = payload.PromptText
	}
	if payload.CategoryID != nil {
		existing.CategoryID = payload.CategoryID
	}
	if payload.SubcategoryID != nil {
		existing.SubcategoryID = payload.SubcategoryID
	}
	if payload.Active != nil {
		existing.Active = *payload.Active
	}
	if payload.ExampleImageFilename != nil {
		old := existing.ExampleImageFilename
		if old != nil && (payload.ExampleImageFilename == nil || *old != *payload.ExampleImageFilename) {
			safeDeletePublicImage(*old, "prompt")
		}
		existing.ExampleImageFilename = payload.ExampleImageFilename
	}
	if err := s.db.WithContext(ctx).Save(&existing).Error; err != nil {
		return nil, err
	}
	// Update mappings if provided
	if payload.Slots != nil {
		newIDs := uniqueSlotIDs(*payload.Slots)
		if len(newIDs) > 0 {
			var cnt int64
			if err := s.db.WithContext(ctx).Model(&PromptSlotVariant{}).Where("id IN ?", newIDs).Count(&cnt).Error; err != nil {
				return nil, err
			}
			if cnt != int64(len(newIDs)) {
				return nil, gorm.ErrRecordNotFound
			}
		}
		if err := s.db.WithContext(ctx).Where("prompt_id = ?", existing.ID).Delete(&PromptSlotVariantMapping{}).Error; err != nil {
			return nil, err
		}
		for _, sid := range newIDs {
			_ = s.db.WithContext(ctx).Create(&PromptSlotVariantMapping{PromptID: existing.ID, SlotID: sid}).Error
		}
	}
	reloaded, _ := loadPromptWithRelations(s.db.WithContext(ctx), existing.ID)
	v := toPromptRead(s.db, reloaded)
	return &v, nil
}

func (s *service) deletePrompt(ctx context.Context, id int) error {
	var existing Prompt
	if err := s.db.WithContext(ctx).First(&existing, "id = ?", id).Error; err != nil {
		return err
	}
	if existing.ExampleImageFilename != nil {
		safeDeletePublicImage(*existing.ExampleImageFilename, "prompt")
	}
	_ = s.db.WithContext(ctx).Where("prompt_id = ?", existing.ID).Delete(&PromptSlotVariantMapping{}).Error
	return s.db.WithContext(ctx).Delete(&Prompt{}, existing.ID).Error
}

// --------------
// Public queries
// --------------

func (s *service) listPublicPrompts(ctx context.Context) ([]PublicPromptRead, error) {
	var rows []Prompt
	if err := s.db.WithContext(ctx).Where("active = ?", true).
		Preload("Category").
		Preload("Subcategory").
		Preload("PromptSlotVariantMappings").
		Preload("PromptSlotVariantMappings.PromptSlotVariant").
		Preload("PromptSlotVariantMappings.PromptSlotVariant.PromptSlotType").
		Order("id desc").
		Find(&rows).Error; err != nil {
		return nil, err
	}
	out := make([]PublicPromptRead, 0, len(rows))
	for i := range rows {
		out = append(out, toPublicPromptRead(&rows[i]))
	}
	return out, nil
}

func (s *service) batchPromptSummaries(ctx context.Context, ids []int) ([]PromptSummaryRead, error) {
	if len(ids) == 0 {
		return []PromptSummaryRead{}, nil
	}
	var rows []Prompt
	if err := s.db.WithContext(ctx).Where("id IN ?", ids).Find(&rows).Error; err != nil {
		return nil, err
	}
	out := make([]PromptSummaryRead, 0, len(rows))
	for i := range rows {
		out = append(out, PromptSummaryRead{ID: rows[i].ID, Title: rows[i].Title})
	}
	return out, nil
}

// ---------
// Internals
// ---------

func (s *service) countPromptsByCategory(ctx context.Context, categoryID int) int {
	var cnt int64
	s.db.WithContext(ctx).Model(&Prompt{}).Where("category_id = ?", categoryID).Count(&cnt)
	return int(cnt)
}

func (s *service) countSubCategoriesByCategory(ctx context.Context, categoryID int) int {
	var cnt int64
	s.db.WithContext(ctx).Model(&PromptSubCategory{}).Where("prompt_category_id = ?", categoryID).Count(&cnt)
	return int(cnt)
}
