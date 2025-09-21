package prompt

import (
	"os"
	"path/filepath"
	"strconv"
	"strings"
	"time"

	"voenix/backend/internal/article"
	img "voenix/backend/internal/image"
)

func toSlotTypeRead(t *PromptSlotType) PromptSlotTypeRead {
	return PromptSlotTypeRead{
		ID:        t.ID,
		Name:      t.Name,
		Position:  t.Position,
		CreatedAt: timePtr(t.CreatedAt),
		UpdatedAt: timePtr(t.UpdatedAt),
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
		LLM:              v.LLM,
		CreatedAt:        timePtr(v.CreatedAt),
		UpdatedAt:        timePtr(v.UpdatedAt),
	}
}

func toPromptRead(p *Prompt) PromptRead {
	var cat *PromptCategoryRead
	if p.Category != nil {
		pc := p.Category
		cat = &PromptCategoryRead{
			ID:                 pc.ID,
			Name:               pc.Name,
			PromptsCount:       0,
			SubcategoriesCount: 0,
			CreatedAt:          timePtr(pc.CreatedAt),
			UpdatedAt:          timePtr(pc.UpdatedAt),
		}
	}
	var subcat *PromptSubCategoryRead
	if p.Subcategory != nil {
		sc := p.Subcategory
		subcat = &PromptSubCategoryRead{
			ID:               sc.ID,
			PromptCategoryID: sc.PromptCategoryID,
			Name:             sc.Name,
			Description:      sc.Description,
			PromptsCount:     0,
			CreatedAt:        timePtr(sc.CreatedAt),
			UpdatedAt:        timePtr(sc.UpdatedAt),
		}
	}
	slots := make([]PromptSlotVariantRead, 0, len(p.PromptSlotVariantMappings))
	for i := range p.PromptSlotVariantMappings {
		m := p.PromptSlotVariantMappings[i]
		if m.PromptSlotVariant != nil {
			slots = append(slots, toSlotVariantRead(m.PromptSlotVariant))
		}
	}
	var price *costCalculationRequest
	if p.Price != nil {
		price = priceToCostCalculation(p.Price)
	}
	return PromptRead{
		ID:              p.ID,
		Title:           p.Title,
		PromptText:      p.PromptText,
		LLM:             p.LLM,
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

func toSubCategoryRead(sc *PromptSubCategory, promptsCount int) PromptSubCategoryRead {
	return PromptSubCategoryRead{
		ID:               sc.ID,
		PromptCategoryID: sc.PromptCategoryID,
		Name:             sc.Name,
		Description:      sc.Description,
		PromptsCount:     promptsCount,
		CreatedAt:        timePtr(sc.CreatedAt),
		UpdatedAt:        timePtr(sc.UpdatedAt),
	}
}

func priceToCostCalculation(pr *article.Price) *costCalculationRequest {
	if pr == nil {
		return nil
	}
	return &costCalculationRequest{
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
		PurchasePriceCorresponds: stringToNetGrossChoice(pr.PurchasePriceCorresponds),
		SalesPriceCorresponds:    stringToNetGrossChoice(pr.SalesPriceCorresponds),
		PurchaseActiveRow:        pr.PurchaseActiveRow,
		SalesActiveRow:           pr.SalesActiveRow,
	}
}

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
			trimmed := strings.TrimSpace(p)
			if trimmed != "" {
				vals = append(vals, trimmed)
			}
		}
	}
	uniq := map[int]struct{}{}
	out := []int{}
	for _, v := range vals {
		if n, err := strconv.Atoi(v); err == nil {
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
