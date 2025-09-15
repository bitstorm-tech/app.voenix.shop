package prompt

import (
	"errors"
	"os"
	"path/filepath"
	"strconv"
	"strings"
	"time"

	"gorm.io/gorm"

	"voenix/backend/internal/article"
	img "voenix/backend/internal/image"
)

// Small wrapper to reduce repetition.
func errorsIsNotFound(err error) bool { return errors.Is(err, gorm.ErrRecordNotFound) }

// Assemblers
func toSlotTypeRead(t *PromptSlotType) PromptSlotTypeRead {
	return PromptSlotTypeRead{
		ID: t.ID, Name: t.Name, Position: t.Position,
		CreatedAt: timePtr(t.CreatedAt), UpdatedAt: timePtr(t.UpdatedAt),
	}
}

func toSlotVariantRead(v *PromptSlotVariant) PromptSlotVariantRead {
	var st *PromptSlotTypeRead
	if v.PromptSlotType != nil {
		tmp := toSlotTypeRead(v.PromptSlotType)
		st = &tmp
	}
	return PromptSlotVariantRead{
		ID:               v.ID,
		PromptSlotTypeID: v.PromptSlotTypeID,
		PromptSlotType:   st,
		Name:             v.Name,
		Prompt:           v.Prompt,
		Description:      v.Description,
		ExampleImageURL:  strPtrOrNil(publicSlotVariantExampleURL(v.ExampleImageFilename)),
		CreatedAt:        timePtr(v.CreatedAt),
		UpdatedAt:        timePtr(v.UpdatedAt),
	}
}

func toPromptRead(db *gorm.DB, p *Prompt) PromptRead {
	var cat *PromptCategoryRead
	if p.Category != nil {
		pc := p.Category
		cat = &PromptCategoryRead{
			ID: pc.ID, Name: pc.Name,
			PromptsCount: 0, SubcategoriesCount: 0,
			CreatedAt: timePtr(pc.CreatedAt), UpdatedAt: timePtr(pc.UpdatedAt),
		}
	}
	var subcat *PromptSubCategoryRead
	if p.Subcategory != nil {
		sc := p.Subcategory
		subcat = &PromptSubCategoryRead{
			ID: sc.ID, PromptCategoryID: sc.PromptCategoryID,
			Name: sc.Name, Description: sc.Description,
			PromptsCount: 0,
			CreatedAt:    timePtr(sc.CreatedAt), UpdatedAt: timePtr(sc.UpdatedAt),
		}
	}

	slots := make([]PromptSlotVariantRead, 0, len(p.PromptSlotVariantMappings))
	for i := range p.PromptSlotVariantMappings {
		m := p.PromptSlotVariantMappings[i]
		if m.PromptSlotVariant != nil {
			slots = append(slots, toSlotVariantRead(m.PromptSlotVariant))
		}
	}
	// Load price if linked
	var price *costCalculationRequest
	if p.Price != nil {
		pr := p.Price
		price = &costCalculationRequest{
			PurchasePriceNet:         pr.PurchasePriceNet,
			PurchasePriceTax:         pr.PurchasePriceTax,
			PurchasePriceGross:       pr.PurchasePriceGross,
			PurchaseCostNet:          pr.PurchaseCostNet,
			PurchaseCostTax:          pr.PurchaseCostTax,
			PurchaseCostGross:        pr.PurchaseCostGross,
			PurchaseCostPercent:      pr.PurchaseCostPercent,
			PurchaseTotalNet:         pr.PurchaseTotalNet,
			PurchaseTotalTax:         pr.PurchaseTotalTax,
			PurchaseTotalGross:       pr.PurchaseTotalGross,
			PurchasePriceUnit:        pr.PurchasePriceUnit,
			PurchaseVatRateId:        pr.PurchaseVatRateID,
			PurchaseVatRatePercent:   pr.PurchaseVatRatePercent,
			PurchaseCalculationMode:  pr.PurchaseCalculationMode,
			SalesVatRateId:           pr.SalesVatRateID,
			SalesVatRatePercent:      pr.SalesVatRatePercent,
			SalesMarginNet:           pr.SalesMarginNet,
			SalesMarginTax:           pr.SalesMarginTax,
			SalesMarginGross:         pr.SalesMarginGross,
			SalesMarginPercent:       pr.SalesMarginPercent,
			SalesTotalNet:            pr.SalesTotalNet,
			SalesTotalTax:            pr.SalesTotalTax,
			SalesTotalGross:          pr.SalesTotalGross,
			SalesPriceUnit:           pr.SalesPriceUnit,
			SalesCalculationMode:     pr.SalesCalculationMode,
			PurchasePriceCorresponds: strToBoolPtr(pr.PurchasePriceCorresponds),
			SalesPriceCorresponds:    strToBoolPtr(pr.SalesPriceCorresponds),
			PurchaseActiveRow:        pr.PurchaseActiveRow,
			SalesActiveRow:           pr.SalesActiveRow,
		}
	} else if p.PriceID != nil {
		var pr article.CostCalculation
		if err := db.First(&pr, "id = ?", *p.PriceID).Error; err == nil {
			price = &costCalculationRequest{
				PurchasePriceNet:         pr.PurchasePriceNet,
				PurchasePriceTax:         pr.PurchasePriceTax,
				PurchasePriceGross:       pr.PurchasePriceGross,
				PurchaseCostNet:          pr.PurchaseCostNet,
				PurchaseCostTax:          pr.PurchaseCostTax,
				PurchaseCostGross:        pr.PurchaseCostGross,
				PurchaseCostPercent:      pr.PurchaseCostPercent,
				PurchaseTotalNet:         pr.PurchaseTotalNet,
				PurchaseTotalTax:         pr.PurchaseTotalTax,
				PurchaseTotalGross:       pr.PurchaseTotalGross,
				PurchasePriceUnit:        pr.PurchasePriceUnit,
				PurchaseVatRateId:        pr.PurchaseVatRateID,
				PurchaseVatRatePercent:   pr.PurchaseVatRatePercent,
				PurchaseCalculationMode:  pr.PurchaseCalculationMode,
				SalesVatRateId:           pr.SalesVatRateID,
				SalesVatRatePercent:      pr.SalesVatRatePercent,
				SalesMarginNet:           pr.SalesMarginNet,
				SalesMarginTax:           pr.SalesMarginTax,
				SalesMarginGross:         pr.SalesMarginGross,
				SalesMarginPercent:       pr.SalesMarginPercent,
				SalesTotalNet:            pr.SalesTotalNet,
				SalesTotalTax:            pr.SalesTotalTax,
				SalesTotalGross:          pr.SalesTotalGross,
				SalesPriceUnit:           pr.SalesPriceUnit,
				SalesCalculationMode:     pr.SalesCalculationMode,
				PurchasePriceCorresponds: strToBoolPtr(pr.PurchasePriceCorresponds),
				SalesPriceCorresponds:    strToBoolPtr(pr.SalesPriceCorresponds),
				PurchaseActiveRow:        pr.PurchaseActiveRow,
				SalesActiveRow:           pr.SalesActiveRow,
			}
		}
	}

	return PromptRead{
		ID:              p.ID,
		Title:           p.Title,
		PromptText:      p.PromptText,
		CategoryID:      p.CategoryID,
		Category:        cat,
		SubcategoryID:   p.SubcategoryID,
		Subcategory:     subcat,
		PriceID:         p.PriceID,
		CostCalculation: price,
		Active:          p.Active,
		Slots:           slots,
		ExampleImageURL: strPtrOrNil(publicPromptExampleURL(p.ExampleImageFilename)),
		CreatedAt:       timePtr(p.CreatedAt),
		UpdatedAt:       timePtr(p.UpdatedAt),
	}
}

func toPublicPromptRead(p *Prompt) PublicPromptRead {
	var cat *PublicPromptCategoryRead
	if p.Category != nil {
		cat = &PublicPromptCategoryRead{ID: p.Category.ID, Name: p.Category.Name}
	}
	var subcat *PublicPromptSubCategoryRead
	if p.Subcategory != nil {
		subcat = &PublicPromptSubCategoryRead{ID: p.Subcategory.ID, Name: p.Subcategory.Name, Description: p.Subcategory.Description}
	}
	slots := make([]PublicPromptSlotRead, 0, len(p.PromptSlotVariantMappings))
	for i := range p.PromptSlotVariantMappings {
		m := p.PromptSlotVariantMappings[i]
		v := m.PromptSlotVariant
		if v == nil {
			continue
		}
		var st *PublicPromptSlotTypeRead
		if v.PromptSlotType != nil {
			st = &PublicPromptSlotTypeRead{ID: v.PromptSlotType.ID, Name: v.PromptSlotType.Name, Position: v.PromptSlotType.Position}
		}
		slots = append(slots, PublicPromptSlotRead{
			ID:              v.ID,
			Name:            v.Name,
			Description:     v.Description,
			ExampleImageURL: strPtrOrNil(publicSlotVariantExampleURL(v.ExampleImageFilename)),
			SlotType:        st,
		})
	}
	var pricePtr *int
	if p.Price != nil {
		v := p.Price.SalesTotalGross
		pricePtr = &v
	}
	return PublicPromptRead{
		ID:              p.ID,
		Title:           p.Title,
		ExampleImageURL: strPtrOrNil(publicPromptExampleURL(p.ExampleImageFilename)),
		Category:        cat,
		Subcategory:     subcat,
		Slots:           slots,
		Price:           pricePtr,
	}
}

func loadPromptWithRelations(db *gorm.DB, id int) (*Prompt, error) {
	var row Prompt
	err := db.Where("id = ?", id).
		Preload("Category").
		Preload("Subcategory").
		Preload("Price").
		Preload("PromptSlotVariantMappings").
		Preload("PromptSlotVariantMappings.PromptSlotVariant").
		Preload("PromptSlotVariantMappings.PromptSlotVariant.PromptSlotType").
		First(&row).Error
	if err != nil {
		if errorsIsNotFound(err) {
			return nil, nil
		}
		return nil, err
	}
	return &row, nil
}

func allPromptsWithRelations(db *gorm.DB) ([]Prompt, error) {
	var rows []Prompt
	err := db.
		Preload("Category").
		Preload("Subcategory").
		Preload("Price").
		Preload("PromptSlotVariantMappings").
		Preload("PromptSlotVariantMappings.PromptSlotVariant").
		Preload("PromptSlotVariantMappings.PromptSlotVariant.PromptSlotType").
		Order("id desc").
		Find(&rows).Error
	return rows, err
}

// Category count helpers moved into Service for centralized DB access.

func countPromptsBySubcategory(db *gorm.DB, subcategoryID int) int {
	var cnt int64
	db.Model(&Prompt{}).Where("subcategory_id = ?", subcategoryID).Count(&cnt)
	return int(cnt)
}

func toSubCategoryRead(db *gorm.DB, sc *PromptSubCategory) PromptSubCategoryRead {
	return PromptSubCategoryRead{
		ID: sc.ID, PromptCategoryID: sc.PromptCategoryID,
		Name: sc.Name, Description: sc.Description,
		PromptsCount: countPromptsBySubcategory(db, sc.ID),
		CreatedAt:    timePtr(sc.CreatedAt), UpdatedAt: timePtr(sc.UpdatedAt),
	}
}

// Utility: parse ids=1,2 or repeated ids params
func parseIDs(repeated []string, commaSep string) []int {
	vals := []string{}
	for _, v := range repeated {
		if v != "" {
			vals = append(vals, v)
		}
	}
	if commaSep != "" {
		parts := strings.Split(commaSep, ",")
		for _, p := range parts {
			if strings.TrimSpace(p) != "" {
				vals = append(vals, strings.TrimSpace(p))
			}
		}
	}
	uniq := map[int]struct{}{}
	out := []int{}
	for _, v := range vals {
		if n, err := strconvAtoi(v); err == nil {
			if _, ok := uniq[n]; !ok {
				uniq[n] = struct{}{}
				out = append(out, n)
			}
		}
	}
	return out
}

func uniqueSlotIDs(slots []promptSlotRef) []int {
	seen := map[int]struct{}{}
	out := make([]int, 0, len(slots))
	for _, s := range slots {
		if s.SlotID <= 0 {
			continue
		}
		if _, ok := seen[s.SlotID]; !ok {
			seen[s.SlotID] = struct{}{}
			out = append(out, s.SlotID)
		}
	}
	return out
}

// Generic exists by id helper
func existsByID[T any](db *gorm.DB, id int) bool {
	var cnt int64
	db.Model(new(T)).Where("id = ?", id).Count(&cnt)
	return cnt > 0
}

func publicPromptExampleURL(filename *string) string {
	if filename == nil || *filename == "" {
		return ""
	}
	return "/public/images/prompt-example-images/" + filepath.Base(*filename)
}

func publicSlotVariantExampleURL(filename *string) string {
	if filename == nil || *filename == "" {
		return ""
	}
	return "/public/images/prompt-slot-variant-example-images/" + filepath.Base(*filename)
}

// Delete a public image (best-effort). kind: "prompt" | "slot-variant"
func safeDeletePublicImage(filename, kind string) {
	loc, err := img.NewStorageLocations()
	if err != nil || strings.TrimSpace(filename) == "" {
		return
	}
	var dir string
	switch kind {
	case "slot-variant":
		dir = loc.PromptSlotVariantExample()
	default:
		dir = loc.PromptExample()
	}
	path := filepath.Join(dir, filepath.Base(filename))
	_ = os.Remove(path)
}

func timePtr(t time.Time) *time.Time { return &t }
func strPtrOrNil(s string) *string {
	if s == "" {
		return nil
	}
	return &s
}

// tiny wrappers to avoid importing strconv in many files
func strconvAtoi(s string) (int, error) { return strconv.Atoi(s) }

// util: DB stores "NET"/"GROSS" but UI sends booleans
func strToBoolPtr(s string) *bool {
	var b bool
	switch strings.ToUpper(strings.TrimSpace(s)) {
	case "NET":
		b = true
	case "GROSS":
		b = false
	default:
		return nil
	}
	return &b
}
