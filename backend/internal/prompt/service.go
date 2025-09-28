package prompt

import (
	"context"
	"errors"
	"strings"

	"gorm.io/gorm"

	"voenix/backend/internal/article"
)

type Service struct {
	repo        Repository
	allowedLLMs map[string]struct{}
}

func NewService(repo Repository, allowedLLMs []string) *Service {
	llmSet := make(map[string]struct{}, len(allowedLLMs))
	for _, llm := range allowedLLMs {
		trimmed := strings.TrimSpace(llm)
		if trimmed == "" {
			continue
		}
		llmSet[trimmed] = struct{}{}
	}
	return &Service{repo: repo, allowedLLMs: llmSet}
}

type conflictError struct{ Detail string }

func (e conflictError) Error() string { return e.Detail }

var errInvalidLLM = errors.New("invalid llm")

func (s *Service) isValidLLM(llm string) bool {
	_, ok := s.allowedLLMs[llm]
	return ok
}

func (s *Service) ListSlotTypes(ctx context.Context) ([]PromptSlotTypeRead, error) {
	rows, err := s.repo.ListSlotTypes(ctx)
	if err != nil {
		return nil, err
	}
	out := make([]PromptSlotTypeRead, 0, len(rows))
	for i := range rows {
		out = append(out, toSlotTypeRead(&rows[i]))
	}
	return out, nil
}

func (s *Service) GetSlotType(ctx context.Context, id int) (*PromptSlotTypeRead, error) {
	row, err := s.repo.SlotTypeByID(ctx, id)
	if err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, nil
		}
		return nil, err
	}
	v := toSlotTypeRead(row)
	return &v, nil
}

func (s *Service) CreateSlotType(ctx context.Context, name string, position int) (*PromptSlotTypeRead, error) {
	exists, err := s.repo.SlotTypeNameExists(ctx, name, nil)
	if err != nil {
		return nil, err
	}
	if exists {
		return nil, conflictError{Detail: "PromptSlotType name already exists"}
	}
	exists, err = s.repo.SlotTypePositionExists(ctx, position, nil)
	if err != nil {
		return nil, err
	}
	if exists {
		return nil, conflictError{Detail: "PromptSlotType position already exists"}
	}
	row := PromptSlotType{Name: name, Position: position}
	if err := s.repo.CreateSlotType(ctx, &row); err != nil {
		return nil, err
	}
	created, err := s.repo.SlotTypeByID(ctx, row.ID)
	if err != nil {
		return nil, err
	}
	v := toSlotTypeRead(created)
	return &v, nil
}

func (s *Service) UpdateSlotType(ctx context.Context, id int, name *string, position *int) (*PromptSlotTypeRead, error) {
	existing, err := s.repo.SlotTypeByID(ctx, id)
	if err != nil {
		return nil, err
	}
	if name != nil && *name != existing.Name {
		exists, err := s.repo.SlotTypeNameExists(ctx, *name, &existing.ID)
		if err != nil {
			return nil, err
		}
		if exists {
			return nil, conflictError{Detail: "PromptSlotType name already exists"}
		}
		existing.Name = *name
	}
	if position != nil && *position != existing.Position {
		exists, err := s.repo.SlotTypePositionExists(ctx, *position, &existing.ID)
		if err != nil {
			return nil, err
		}
		if exists {
			return nil, conflictError{Detail: "PromptSlotType position already exists"}
		}
		existing.Position = *position
	}
	if err := s.repo.SaveSlotType(ctx, existing); err != nil {
		return nil, err
	}
	updated, err := s.repo.SlotTypeByID(ctx, existing.ID)
	if err != nil {
		return nil, err
	}
	v := toSlotTypeRead(updated)
	return &v, nil
}

func (s *Service) DeleteSlotType(ctx context.Context, id int) error {
	if _, err := s.repo.SlotTypeByID(ctx, id); err != nil {
		return err
	}
	return s.repo.DeleteSlotType(ctx, id)
}

func (s *Service) ListSlotVariants(ctx context.Context) ([]PromptSlotVariantRead, error) {
	rows, err := s.repo.ListSlotVariants(ctx)
	if err != nil {
		return nil, err
	}
	out := make([]PromptSlotVariantRead, 0, len(rows))
	for i := range rows {
		out = append(out, toSlotVariantRead(&rows[i]))
	}
	return out, nil
}

func (s *Service) GetSlotVariant(ctx context.Context, id int) (*PromptSlotVariantRead, error) {
	row, err := s.repo.SlotVariantByID(ctx, id)
	if err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, nil
		}
		return nil, err
	}
	v := toSlotVariantRead(row)
	return &v, nil
}

func (s *Service) CreateSlotVariant(ctx context.Context, payload slotVariantCreate) (*PromptSlotVariantRead, error) {
	exists, err := s.repo.SlotTypeExists(ctx, payload.PromptSlotTypeID)
	if err != nil {
		return nil, err
	}
	if !exists {
		return nil, gorm.ErrRecordNotFound
	}
	llm := strings.TrimSpace(payload.LLM)
	if llm == "" || !s.isValidLLM(llm) {
		return nil, errInvalidLLM
	}
	exists, err = s.repo.SlotVariantNameExists(ctx, payload.Name, nil)
	if err != nil {
		return nil, err
	}
	if exists {
		return nil, conflictError{Detail: "PromptSlotVariant name already exists"}
	}
	row := PromptSlotVariant{
		PromptSlotTypeID: payload.PromptSlotTypeID,
		Name:             payload.Name,
		Prompt:           payload.Prompt,
		Description:      payload.Description,
		LLM:              llm,
	}
	if err := s.repo.CreateSlotVariant(ctx, &row); err != nil {
		return nil, err
	}
	created, err := s.repo.SlotVariantByID(ctx, row.ID)
	if err != nil {
		return nil, err
	}
	v := toSlotVariantRead(created)
	return &v, nil
}

func (s *Service) UpdateSlotVariant(ctx context.Context, id int, payload slotVariantUpdate) (*PromptSlotVariantRead, error) {
	existing, err := s.repo.SlotVariantByID(ctx, id)
	if err != nil {
		return nil, err
	}
	if payload.PromptSlotTypeID != nil && *payload.PromptSlotTypeID != existing.PromptSlotTypeID {
		ok, err := s.repo.SlotTypeExists(ctx, *payload.PromptSlotTypeID)
		if err != nil {
			return nil, err
		}
		if !ok {
			return nil, gorm.ErrRecordNotFound
		}
		existing.PromptSlotTypeID = *payload.PromptSlotTypeID
		existing.PromptSlotType = nil
	}
	if payload.LLM != nil {
		llm := strings.TrimSpace(*payload.LLM)
		if llm == "" || !s.isValidLLM(llm) {
			return nil, errInvalidLLM
		}
		existing.LLM = llm
	}
	if payload.Name != nil && *payload.Name != existing.Name {
		ok, err := s.repo.SlotVariantNameExists(ctx, *payload.Name, &existing.ID)
		if err != nil {
			return nil, err
		}
		if ok {
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
	if err := s.repo.SaveSlotVariant(ctx, existing); err != nil {
		return nil, err
	}
	updated, err := s.repo.SlotVariantByID(ctx, id)
	if err != nil {
		return nil, err
	}
	v := toSlotVariantRead(updated)
	return &v, nil
}

func (s *Service) DeleteSlotVariant(ctx context.Context, id int) error {
	if _, err := s.repo.SlotVariantByID(ctx, id); err != nil {
		return err
	}
	return s.repo.DeleteSlotVariant(ctx, id)
}

func (s *Service) ListCategories(ctx context.Context) ([]PromptCategoryRead, error) {
	rows, err := s.repo.ListCategories(ctx)
	if err != nil {
		return nil, err
	}
	out := make([]PromptCategoryRead, 0, len(rows))
	for i := range rows {
		cat := &rows[i]
		promptsCount, err := s.repo.CountPromptsByCategory(ctx, cat.ID)
		if err != nil {
			return nil, err
		}
		subcatCount, err := s.repo.CountSubCategoriesByCategory(ctx, cat.ID)
		if err != nil {
			return nil, err
		}
		out = append(out, PromptCategoryRead{
			ID:                 cat.ID,
			Name:               cat.Name,
			PromptsCount:       promptsCount,
			SubcategoriesCount: subcatCount,
			CreatedAt:          timePtr(cat.CreatedAt),
			UpdatedAt:          timePtr(cat.UpdatedAt),
		})
	}
	return out, nil
}

func (s *Service) CreateCategory(ctx context.Context, name string) (*PromptCategoryRead, error) {
	row := PromptCategory{Name: name}
	if err := s.repo.CreateCategory(ctx, &row); err != nil {
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

func (s *Service) UpdateCategory(ctx context.Context, id int, name *string) (*PromptCategoryRead, error) {
	existing, err := s.repo.CategoryByID(ctx, id)
	if err != nil {
		return nil, err
	}
	if name != nil {
		existing.Name = *name
	}
	if err := s.repo.SaveCategory(ctx, existing); err != nil {
		return nil, err
	}
	promptsCount, err := s.repo.CountPromptsByCategory(ctx, existing.ID)
	if err != nil {
		return nil, err
	}
	subcatCount, err := s.repo.CountSubCategoriesByCategory(ctx, existing.ID)
	if err != nil {
		return nil, err
	}
	v := PromptCategoryRead{
		ID:                 existing.ID,
		Name:               existing.Name,
		PromptsCount:       promptsCount,
		SubcategoriesCount: subcatCount,
		CreatedAt:          timePtr(existing.CreatedAt),
		UpdatedAt:          timePtr(existing.UpdatedAt),
	}
	return &v, nil
}

func (s *Service) DeleteCategory(ctx context.Context, id int) error {
	if _, err := s.repo.CategoryByID(ctx, id); err != nil {
		return err
	}
	return s.repo.DeleteCategory(ctx, id)
}

func (s *Service) ListSubCategories(ctx context.Context) ([]PromptSubCategoryRead, error) {
	rows, err := s.repo.ListSubCategories(ctx)
	if err != nil {
		return nil, err
	}
	out := make([]PromptSubCategoryRead, 0, len(rows))
	for i := range rows {
		count, err := s.repo.CountPromptsBySubCategory(ctx, rows[i].ID)
		if err != nil {
			return nil, err
		}
		out = append(out, toSubCategoryRead(&rows[i], count))
	}
	return out, nil
}

func (s *Service) ListSubCategoriesByCategory(ctx context.Context, categoryID int) ([]PromptSubCategoryRead, error) {
	rows, err := s.repo.ListSubCategoriesByCategory(ctx, categoryID)
	if err != nil {
		return nil, err
	}
	out := make([]PromptSubCategoryRead, 0, len(rows))
	for i := range rows {
		count, err := s.repo.CountPromptsBySubCategory(ctx, rows[i].ID)
		if err != nil {
			return nil, err
		}
		out = append(out, toSubCategoryRead(&rows[i], count))
	}
	return out, nil
}

func (s *Service) CreateSubCategory(ctx context.Context, payload subcatCreate) (*PromptSubCategoryRead, error) {
	exists, err := s.repo.CategoryExists(ctx, payload.PromptCategoryID)
	if err != nil {
		return nil, err
	}
	if !exists {
		return nil, gorm.ErrRecordNotFound
	}
	row := PromptSubCategory{
		PromptCategoryID: payload.PromptCategoryID,
		Name:             payload.Name,
		Description:      payload.Description,
	}
	if err := s.repo.CreateSubCategory(ctx, &row); err != nil {
		return nil, err
	}
	count, err := s.repo.CountPromptsBySubCategory(ctx, row.ID)
	if err != nil {
		return nil, err
	}
	v := toSubCategoryRead(&row, count)
	return &v, nil
}

func (s *Service) UpdateSubCategory(ctx context.Context, id int, payload subcatUpdate) (*PromptSubCategoryRead, error) {
	existing, err := s.repo.SubCategoryByID(ctx, id)
	if err != nil {
		return nil, err
	}
	if payload.PromptCategoryID != nil && *payload.PromptCategoryID != existing.PromptCategoryID {
		exists, err := s.repo.CategoryExists(ctx, *payload.PromptCategoryID)
		if err != nil {
			return nil, err
		}
		if !exists {
			return nil, gorm.ErrRecordNotFound
		}
		existing.PromptCategoryID = *payload.PromptCategoryID
		existing.PromptCategory = nil
	}
	if payload.Name != nil {
		existing.Name = *payload.Name
	}
	if payload.Description != nil {
		existing.Description = payload.Description
	}
	if err := s.repo.SaveSubCategory(ctx, existing); err != nil {
		return nil, err
	}
	count, err := s.repo.CountPromptsBySubCategory(ctx, existing.ID)
	if err != nil {
		return nil, err
	}
	v := toSubCategoryRead(existing, count)
	return &v, nil
}

func (s *Service) DeleteSubCategory(ctx context.Context, id int) error {
	return s.repo.DeleteSubCategory(ctx, id)
}

func (s *Service) ListPrompts(ctx context.Context) ([]PromptRead, error) {
	rows, err := s.repo.ListPrompts(ctx)
	if err != nil {
		return nil, err
	}
	out := make([]PromptRead, 0, len(rows))
	for i := range rows {
		out = append(out, toPromptRead(&rows[i]))
	}
	return out, nil
}

func (s *Service) GetPrompt(ctx context.Context, id int) (*PromptRead, error) {
	row, err := s.repo.PromptByID(ctx, id)
	if err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, nil
		}
		return nil, err
	}
	v := toPromptRead(row)
	return &v, nil
}

func (s *Service) CreatePrompt(ctx context.Context, payload promptCreate) (*PromptRead, error) {
	if payload.CategoryID != nil {
		exists, err := s.repo.CategoryExists(ctx, *payload.CategoryID)
		if err != nil {
			return nil, err
		}
		if !exists {
			return nil, gorm.ErrRecordNotFound
		}
	}
	if payload.SubcategoryID != nil {
		sc, err := s.repo.SubCategoryByID(ctx, *payload.SubcategoryID)
		if err != nil {
			return nil, err
		}
		if payload.CategoryID != nil && sc.PromptCategoryID != *payload.CategoryID {
			return nil, errors.New("subcategory does not belong to the specified category")
		}
	}
	llm := strings.TrimSpace(payload.LLM)
	if llm == "" || !s.isValidLLM(llm) {
		return nil, errInvalidLLM
	}
	slotIDs := uniqueSlotIDs(payload.Slots)
	if len(slotIDs) > 0 {
		exists, err := s.repo.SlotVariantsExist(ctx, slotIDs)
		if err != nil {
			return nil, err
		}
		if !exists {
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
	llmValue := llm
	row.LLM = &llmValue
	if payload.CostCalculation != nil {
		priceID, err := s.createOrUpdatePrice(ctx, nil, payload.CostCalculation)
		if err != nil {
			return nil, err
		}
		row.PriceID = &priceID
	} else if payload.PriceID != nil && *payload.PriceID > 0 {
		price, err := s.repo.PriceByID(ctx, *payload.PriceID)
		if err != nil {
			return nil, err
		}
		if price.ArticleID != nil {
			return nil, conflictError{Detail: "price is already linked to an article"}
		}
		row.PriceID = payload.PriceID
	}
	if err := s.repo.CreatePrompt(ctx, &row); err != nil {
		return nil, err
	}
	if len(slotIDs) > 0 {
		if err := s.repo.ReplacePromptSlotVariantMappings(ctx, row.ID, slotIDs); err != nil {
			return nil, err
		}
	}
	created, err := s.repo.PromptByID(ctx, row.ID)
	if err != nil {
		return nil, err
	}
	v := toPromptRead(created)
	return &v, nil
}

func (s *Service) UpdatePrompt(ctx context.Context, id int, payload promptUpdate) (*PromptRead, error) {
	existing, err := s.repo.PromptByID(ctx, id)
	if err != nil {
		return nil, err
	}
	if payload.CategoryID != nil {
		existsCat, err := s.repo.CategoryExists(ctx, *payload.CategoryID)
		if err != nil {
			return nil, err
		}
		if !existsCat {
			return nil, gorm.ErrRecordNotFound
		}
	}
	if payload.SubcategoryID != nil {
		sc, err := s.repo.SubCategoryByID(ctx, *payload.SubcategoryID)
		if err != nil {
			return nil, err
		}
		if payload.CategoryID != nil && sc.PromptCategoryID != *payload.CategoryID {
			return nil, errors.New("subcategory does not belong to the specified category")
		}
	}
	if payload.LLM != nil {
		llm := strings.TrimSpace(*payload.LLM)
		if llm == "" || !s.isValidLLM(llm) {
			return nil, errInvalidLLM
		}
		llmValue := llm
		existing.LLM = &llmValue
	}
	if payload.Title != nil {
		existing.Title = *payload.Title
	}
	if payload.PromptText != nil {
		existing.PromptText = payload.PromptText
	}
	if payload.CategoryID != nil {
		existing.CategoryID = payload.CategoryID
		existing.Category = nil
	}
	if payload.SubcategoryID != nil {
		existing.SubcategoryID = payload.SubcategoryID
		existing.Subcategory = nil
	}
	if payload.Active != nil {
		existing.Active = *payload.Active
	}
	if payload.CostCalculation != nil {
		var target *int
		if payload.PriceID != nil && *payload.PriceID > 0 {
			target = payload.PriceID
		} else if existing.PriceID != nil && *existing.PriceID > 0 {
			target = existing.PriceID
		}
		priceID, err := s.createOrUpdatePrice(ctx, target, payload.CostCalculation)
		if err != nil {
			return nil, err
		}
		existing.PriceID = &priceID
	} else if payload.PriceID != nil && *payload.PriceID > 0 {
		price, err := s.repo.PriceByID(ctx, *payload.PriceID)
		if err != nil {
			return nil, err
		}
		if price.ArticleID != nil {
			return nil, conflictError{Detail: "price is already linked to an article"}
		}
		existing.PriceID = payload.PriceID
	}
	if payload.ExampleImageFilename != nil {
		old := existing.ExampleImageFilename
		if old != nil && (payload.ExampleImageFilename == nil || *old != *payload.ExampleImageFilename) {
			safeDeletePublicImage(*old, "prompt")
		}
		existing.ExampleImageFilename = payload.ExampleImageFilename
	}
	if err := s.repo.SavePrompt(ctx, existing); err != nil {
		return nil, err
	}
	if payload.Slots != nil {
		newIDs := uniqueSlotIDs(*payload.Slots)
		if len(newIDs) > 0 {
			existsSlots, err := s.repo.SlotVariantsExist(ctx, newIDs)
			if err != nil {
				return nil, err
			}
			if !existsSlots {
				return nil, gorm.ErrRecordNotFound
			}
		}
		if err := s.repo.ReplacePromptSlotVariantMappings(ctx, existing.ID, newIDs); err != nil {
			return nil, err
		}
	}
	updated, err := s.repo.PromptByID(ctx, existing.ID)
	if err != nil {
		return nil, err
	}
	v := toPromptRead(updated)
	return &v, nil
}

func (s *Service) DeletePrompt(ctx context.Context, id int) error {
	existing, err := s.repo.PromptByID(ctx, id)
	if err != nil {
		return err
	}
	if existing.ExampleImageFilename != nil {
		safeDeletePublicImage(*existing.ExampleImageFilename, "prompt")
	}
	if err := s.repo.ReplacePromptSlotVariantMappings(ctx, existing.ID, nil); err != nil {
		return err
	}
	return s.repo.DeletePrompt(ctx, existing.ID)
}

func (s *Service) ListPublicPrompts(ctx context.Context) ([]PublicPromptRead, error) {
	rows, err := s.repo.ListPublicPrompts(ctx)
	if err != nil {
		return nil, err
	}
	out := make([]PublicPromptRead, 0, len(rows))
	for i := range rows {
		out = append(out, toPublicPromptRead(&rows[i]))
	}
	return out, nil
}

func (s *Service) BatchPromptSummaries(ctx context.Context, ids []int) ([]PromptSummaryRead, error) {
	if len(ids) == 0 {
		return []PromptSummaryRead{}, nil
	}
	rows, err := s.repo.PromptsByIDs(ctx, ids)
	if err != nil {
		return nil, err
	}
	out := make([]PromptSummaryRead, 0, len(rows))
	for i := range rows {
		out = append(out, PromptSummaryRead{ID: rows[i].ID, Title: rows[i].Title})
	}
	return out, nil
}

func (s *Service) createOrUpdatePrice(ctx context.Context, priceID *int, req *costCalculationRequest) (int, error) {
	if req == nil {
		return 0, errors.New("missing costCalculation")
	}
	if req.PurchaseVatRateId != nil {
		exists, err := s.repo.VatExists(ctx, *req.PurchaseVatRateId)
		if err != nil {
			return 0, err
		}
		if !exists {
			return 0, gorm.ErrRecordNotFound
		}
	}
	if req.SalesVatRateId != nil {
		exists, err := s.repo.VatExists(ctx, *req.SalesVatRateId)
		if err != nil {
			return 0, err
		}
		if !exists {
			return 0, gorm.ErrRecordNotFound
		}
	}
	if priceID == nil {
		row := article.Price{}
		s.applyCostCalculation(&row, req)
		if err := s.repo.CreatePrice(ctx, &row); err != nil {
			return 0, err
		}
		return row.ID, nil
	}
	row, err := s.repo.PriceByID(ctx, *priceID)
	if err != nil {
		return 0, err
	}
	if row.ArticleID != nil {
		return 0, conflictError{Detail: "price is already linked to an article"}
	}
	s.applyCostCalculation(row, req)
	if err := s.repo.SavePrice(ctx, row); err != nil {
		return 0, err
	}
	return row.ID, nil
}

func (s *Service) applyCostCalculation(row *article.Price, req *costCalculationRequest) {
	row.PurchasePriceNet = req.PurchasePriceNet
	row.PurchasePriceTax = req.PurchasePriceTax
	row.PurchasePriceGross = req.PurchasePriceGross
	row.PurchaseCostNet = req.PurchaseCostNet
	row.PurchaseCostTax = req.PurchaseCostTax
	row.PurchaseCostGross = req.PurchaseCostGross
	row.PurchaseCostPercent = req.PurchaseCostPercent
	row.PurchaseTotalNet = req.PurchaseTotalNet
	row.PurchaseTotalTax = req.PurchaseTotalTax
	row.PurchaseTotalGross = req.PurchaseTotalGross
	row.PurchasePriceUnit = req.PurchasePriceUnit
	row.PurchaseVatRateID = req.PurchaseVatRateId
	row.PurchaseVatRatePercent = req.PurchaseVatRatePercent
	row.PurchaseCalculationMode = req.PurchaseCalculationMode
	row.SalesVatRateID = req.SalesVatRateId
	row.SalesVatRatePercent = req.SalesVatRatePercent
	row.SalesMarginNet = req.SalesMarginNet
	row.SalesMarginTax = req.SalesMarginTax
	row.SalesMarginGross = req.SalesMarginGross
	row.SalesMarginPercent = req.SalesMarginPercent
	row.SalesTotalNet = req.SalesTotalNet
	row.SalesTotalTax = req.SalesTotalTax
	row.SalesTotalGross = req.SalesTotalGross
	row.SalesPriceUnit = req.SalesPriceUnit
	row.SalesCalculationMode = req.SalesCalculationMode
	if req.PurchasePriceCorresponds != nil {
		if *req.PurchasePriceCorresponds {
			row.PurchasePriceCorresponds = "NET"
		} else {
			row.PurchasePriceCorresponds = "GROSS"
		}
	} else if row.PurchasePriceCorresponds == "" {
		row.PurchasePriceCorresponds = "NET"
	}
	if req.SalesPriceCorresponds != nil {
		if *req.SalesPriceCorresponds {
			row.SalesPriceCorresponds = "NET"
		} else {
			row.SalesPriceCorresponds = "GROSS"
		}
	} else if row.SalesPriceCorresponds == "" {
		row.SalesPriceCorresponds = "NET"
	}
	row.PurchaseActiveRow = req.PurchaseActiveRow
	row.SalesActiveRow = req.SalesActiveRow
}
