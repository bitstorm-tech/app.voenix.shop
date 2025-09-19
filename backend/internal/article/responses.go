package article

import (
	"cmp"
	"path/filepath"
	"strings"
	"time"

	img "voenix/backend/internal/image"
)

// Assemblers build response payload structures used by handlers.
func toArticleMugVariantResponse(v *MugVariant) articleMugVariantResponse {
	return articleMugVariantResponse{
		ID:                   v.ID,
		ArticleID:            v.ArticleID,
		InsideColorCode:      v.InsideColorCode,
		OutsideColorCode:     v.OutsideColorCode,
		Name:                 v.Name,
		ExampleImageURL:      strPtrOrNil(publicMugVariantExampleURL(v.ExampleImageFilename)),
		ArticleVariantNumber: v.ArticleVariantNumber,
		IsDefault:            v.IsDefault,
		Active:               v.Active,
		ExampleImageFilename: v.ExampleImageFilename,
		CreatedAt:            timePtr(v.CreatedAt),
		UpdatedAt:            timePtr(v.UpdatedAt),
	}
}

func toArticleShirtVariantResponse(v *ShirtVariant) articleShirtVariantResponse {
	return articleShirtVariantResponse{
		ID:              v.ID,
		ArticleID:       v.ArticleID,
		Color:           v.Color,
		Size:            v.Size,
		ExampleImageURL: strPtrOrNil(publicShirtVariantExampleURL(v.ExampleImageFilename)),
		CreatedAt:       timePtr(v.CreatedAt),
		UpdatedAt:       timePtr(v.UpdatedAt),
	}
}

func toArticleMugDetailsResponse(d *MugDetails) *articleMugDetailsResponse {
	if d == nil {
		return nil
	}
	return &articleMugDetailsResponse{
		ArticleID:                    d.ArticleID,
		HeightMm:                     d.HeightMm,
		DiameterMm:                   d.DiameterMm,
		PrintTemplateWidthMm:         d.PrintTemplateWidthMm,
		PrintTemplateHeightMm:        d.PrintTemplateHeightMm,
		DocumentFormatWidthMm:        d.DocumentFormatWidthMm,
		DocumentFormatHeightMm:       d.DocumentFormatHeightMm,
		DocumentFormatMarginBottomMm: d.DocumentFormatMarginBottomMm,
		FillingQuantity:              d.FillingQuantity,
		DishwasherSafe:               d.DishwasherSafe,
		CreatedAt:                    timePtr(d.CreatedAt),
		UpdatedAt:                    timePtr(d.UpdatedAt),
	}
}

func toArticleShirtDetailsResponse(d *ShirtDetails) *articleShirtDetailsResponse {
	if d == nil {
		return nil
	}
	sizes := []string{}
	if strings.TrimSpace(d.AvailableSizes) != "" {
		for _, s := range strings.Split(d.AvailableSizes, ",") {
			trimmed := strings.TrimSpace(s)
			if trimmed != "" {
				sizes = append(sizes, trimmed)
			}
		}
	}
	return &articleShirtDetailsResponse{
		ArticleID:        d.ArticleID,
		Material:         d.Material,
		CareInstructions: d.CareInstructions,
		FitType:          d.FitType,
		AvailableSizes:   sizes,
		CreatedAt:        timePtr(d.CreatedAt),
		UpdatedAt:        timePtr(d.UpdatedAt),
	}
}

func toCostCalculationResponse(c *Price) *costCalculationResponse {
	if c == nil {
		return nil
	}
	return &costCalculationResponse{
		ID:                       c.ID,
		ArticleID:                cmp.Or(*c.ArticleID, 0),
		PurchasePriceNet:         c.PurchasePriceNet,
		PurchasePriceTax:         c.PurchasePriceTax,
		PurchasePriceGross:       c.PurchasePriceGross,
		PurchaseCostNet:          c.PurchaseCostNet,
		PurchaseCostTax:          c.PurchaseCostTax,
		PurchaseCostGross:        c.PurchaseCostGross,
		PurchaseCostPercent:      c.PurchaseCostPercent,
		PurchaseTotalNet:         c.PurchaseTotalNet,
		PurchaseTotalTax:         c.PurchaseTotalTax,
		PurchaseTotalGross:       c.PurchaseTotalGross,
		PurchasePriceUnit:        c.PurchasePriceUnit,
		PurchaseVatRateID:        c.PurchaseVatRateID,
		PurchaseVatRatePercent:   c.PurchaseVatRatePercent,
		PurchaseCalculationMode:  c.PurchaseCalculationMode,
		SalesVatRateID:           c.SalesVatRateID,
		SalesVatRatePercent:      c.SalesVatRatePercent,
		SalesMarginNet:           c.SalesMarginNet,
		SalesMarginTax:           c.SalesMarginTax,
		SalesMarginGross:         c.SalesMarginGross,
		SalesMarginPercent:       c.SalesMarginPercent,
		SalesTotalNet:            c.SalesTotalNet,
		SalesTotalTax:            c.SalesTotalTax,
		SalesTotalGross:          c.SalesTotalGross,
		SalesPriceUnit:           c.SalesPriceUnit,
		SalesCalculationMode:     c.SalesCalculationMode,
		PurchasePriceCorresponds: c.PurchasePriceCorresponds,
		SalesPriceCorresponds:    c.SalesPriceCorresponds,
		PurchaseActiveRow:        c.PurchaseActiveRow,
		SalesActiveRow:           c.SalesActiveRow,
		CreatedAt:                timePtr(c.CreatedAt),
		UpdatedAt:                timePtr(c.UpdatedAt),
	}
}

func toArticleResponse(a *Article, catName string, subcatName *string, supplierName *string, mugDetails *MugDetails, shirtDetails *ShirtDetails) ArticleResponse {
	out := ArticleResponse{
		ID:                    a.ID,
		Name:                  a.Name,
		DescriptionShort:      a.DescriptionShort,
		DescriptionLong:       a.DescriptionLong,
		Active:                a.Active,
		ArticleType:           a.ArticleType,
		CategoryID:            a.CategoryID,
		CategoryName:          catName,
		SubcategoryID:         a.SubcategoryID,
		SubcategoryName:       subcatName,
		SupplierID:            a.SupplierID,
		SupplierName:          supplierName,
		SupplierArticleName:   a.SupplierArticleName,
		SupplierArticleNumber: a.SupplierArticleNumber,
		MugDetails:            toArticleMugDetailsResponse(mugDetails),
		ShirtDetails:          toArticleShirtDetailsResponse(shirtDetails),
		CreatedAt:             timePtr(a.CreatedAt),
		UpdatedAt:             timePtr(a.UpdatedAt),
	}
	return out
}

// URL helpers
func publicMugVariantExampleURL(filename *string) string {
	if filename == nil || *filename == "" {
		return ""
	}
	if loc, err := img.NewStorageLocations(); err == nil {
		dir := loc.MugVariantExample()
		if rel, rerr := filepath.Rel(loc.Root, dir); rerr == nil {
			relURL := filepath.ToSlash(rel)
			return "/" + relURL + "/" + filepath.Base(*filename)
		}
	}
	return "/public/images/articles/mugs/variant-example-images/" + filepath.Base(*filename)
}

func publicShirtVariantExampleURL(filename *string) string {
	if filename == nil || *filename == "" {
		return ""
	}
	if loc, err := img.NewStorageLocations(); err == nil {
		dir := loc.ShirtVariantExample()
		if rel, rerr := filepath.Rel(loc.Root, dir); rerr == nil {
			relURL := filepath.ToSlash(rel)
			return "/" + relURL + "/" + filepath.Base(*filename)
		}
	}
	return "/public/images/articles/shirts/variant-example-images/" + filepath.Base(*filename)
}

func timePtr(t time.Time) *time.Time { return &t }

func strPtrOrNil(s string) *string {
	if s == "" {
		return nil
	}
	return &s
}
