package article

import (
	"errors"
	"strconv"
	"strings"

	"gorm.io/gorm"

	"voenix/backend-go/internal/supplier"
	"voenix/backend-go/internal/vat"
)

// articleNames fetches related display names used in responses.
func articleNames(db *gorm.DB, a *Article) (catName string, subName *string, suppName *string) {
	// category name
	var cat ArticleCategory
	if err := db.First(&cat, "id = ?", a.CategoryID).Error; err == nil {
		catName = cat.Name
	}
	// subcategory name
	if a.SubcategoryID != nil {
		var sc ArticleSubCategory
		if err := db.First(&sc, "id = ?", *a.SubcategoryID).Error; err == nil {
			subName = &sc.Name
		}
	}
	// supplier name
	if a.SupplierID != nil {
		var s supplier.Supplier
		if err := db.First(&s, "id = ?", *a.SupplierID).Error; err == nil && s.Name != nil {
			suppName = s.Name
		}
	}
	return
}

// listArticleVariantsRead returns typed variant DTOs for the given article.
func listArticleVariantsRead(db *gorm.DB, a *Article) ([]ArticleMugVariantRead, []ArticleShirtVariantRead) {
	if a.ArticleType == ArticleTypeMug {
		var mvs []MugVariant
		_ = db.Where("article_id = ?", a.ID).Order("id asc").Find(&mvs).Error
		out := make([]ArticleMugVariantRead, 0, len(mvs))
		for i := range mvs {
			out = append(out, toMugVariantRead(&mvs[i]))
		}
		return out, nil
	}
	if a.ArticleType == ArticleTypeShirt {
		var svs []ShirtVariant
		_ = db.Where("article_id = ?", a.ID).Order("id asc").Find(&svs).Error
		out := make([]ArticleShirtVariantRead, 0, len(svs))
		for i := range svs {
			out = append(out, toShirtVariantRead(&svs[i]))
		}
		return nil, out
	}
	return nil, nil
}

// defaultMugVariant returns the default variant if present, else the first.
func defaultMugVariant(vs []MugVariant) *MugVariant {
	if len(vs) == 0 {
		return nil
	}
	for i := range vs {
		if vs[i].IsDefault {
			return &vs[i]
		}
	}
	return &vs[0]
}

// upsertMugDetails creates or updates mug details for articleID using req.
func upsertMugDetails(db *gorm.DB, articleID int, req *createMugDetailsRequest) error {
	if req == nil {
		return nil
	}
	var row MugDetails
	err := db.First(&row, "article_id = ?", articleID).Error
	if err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			row = MugDetails{
				ArticleID:                    articleID,
				HeightMm:                     req.HeightMm,
				DiameterMm:                   req.DiameterMm,
				PrintTemplateWidthMm:         req.PrintTemplateWidthMm,
				PrintTemplateHeightMm:        req.PrintTemplateHeightMm,
				DocumentFormatWidthMm:        req.DocumentFormatWidthMm,
				DocumentFormatHeightMm:       req.DocumentFormatHeightMm,
				DocumentFormatMarginBottomMm: req.DocumentFormatMarginBottomMm,
				FillingQuantity:              req.FillingQuantity,
				DishwasherSafe:               req.DishwasherSafe,
			}
			return db.Create(&row).Error
		}
		return err
	}
	row.HeightMm = req.HeightMm
	row.DiameterMm = req.DiameterMm
	row.PrintTemplateWidthMm = req.PrintTemplateWidthMm
	row.PrintTemplateHeightMm = req.PrintTemplateHeightMm
	row.DocumentFormatWidthMm = req.DocumentFormatWidthMm
	row.DocumentFormatHeightMm = req.DocumentFormatHeightMm
	row.DocumentFormatMarginBottomMm = req.DocumentFormatMarginBottomMm
	row.FillingQuantity = req.FillingQuantity
	row.DishwasherSafe = req.DishwasherSafe
	return db.Save(&row).Error
}

// upsertShirtDetails creates or updates shirt details for articleID using req.
func upsertShirtDetails(db *gorm.DB, articleID int, req *createShirtDetailsRequest) error {
	if req == nil {
		return nil
	}
	var row ShirtDetails
	err := db.First(&row, "article_id = ?", articleID).Error
	csvSizes := strings.Join(req.AvailableSizes, ",")
	if err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			row = ShirtDetails{ArticleID: articleID, Material: req.Material, CareInstructions: req.CareInstructions, FitType: req.FitType, AvailableSizes: csvSizes}
			return db.Create(&row).Error
		}
		return err
	}
	row.Material = req.Material
	row.CareInstructions = req.CareInstructions
	row.FitType = req.FitType
	row.AvailableSizes = csvSizes
	return db.Save(&row).Error
}

// upsertCostCalculation creates or updates cost calculation with basic VAT validation.
func upsertCostCalculation(db *gorm.DB, articleID int, req *costCalculationRequest) error {
	if req == nil {
		return nil
	}
	if req.PurchaseVatRateId != nil && !existsByID[vat.ValueAddedTax](db, *req.PurchaseVatRateId) {
		return errors.New("purchase VAT not found: " + strconv.Itoa(*req.PurchaseVatRateId))
	}
	if req.SalesVatRateId != nil && !existsByID[vat.ValueAddedTax](db, *req.SalesVatRateId) {
		return errors.New("sales VAT not found: " + strconv.Itoa(*req.SalesVatRateId))
	}
	var row CostCalculation
	if err := db.First(&row, "article_id = ?", articleID).Error; err != nil {
		if !errors.Is(err, gorm.ErrRecordNotFound) {
			return err
		}
		row = CostCalculation{ArticleID: &articleID}
	}
	// map fields
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
	row.PurchasePriceCorresponds = req.PurchasePriceCorresponds
	row.SalesPriceCorresponds = req.SalesPriceCorresponds
	row.PurchaseActiveRow = req.PurchaseActiveRow
	row.SalesActiveRow = req.SalesActiveRow

	if row.ID == 0 {
		return db.Create(&row).Error
	}
	return db.Save(&row).Error
}

// no extra helpers required
