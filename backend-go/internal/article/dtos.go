package article

import (
	"cmp"
	"path/filepath"
	"strings"
	"time"

	img "voenix/backend-go/internal/image"
)

// Admin/public DTOs aligned with frontend types.

type ArticleMugVariantRead struct {
	ID                   int        `json:"id"`
	ArticleID            int        `json:"articleId"`
	InsideColorCode      string     `json:"insideColorCode"`
	OutsideColorCode     string     `json:"outsideColorCode"`
	Name                 string     `json:"name"`
	ExampleImageURL      *string    `json:"exampleImageUrl"`
	ArticleVariantNumber *string    `json:"articleVariantNumber"`
	IsDefault            bool       `json:"isDefault"`
	Active               bool       `json:"active"`
	ExampleImageFilename *string    `json:"exampleImageFilename"`
	CreatedAt            *time.Time `json:"createdAt"`
	UpdatedAt            *time.Time `json:"updatedAt"`
}

type ArticleShirtVariantRead struct {
	ID              int        `json:"id"`
	ArticleID       int        `json:"articleId"`
	Color           string     `json:"color"`
	Size            string     `json:"size"`
	ExampleImageURL *string    `json:"exampleImageUrl"`
	CreatedAt       *time.Time `json:"createdAt"`
	UpdatedAt       *time.Time `json:"updatedAt"`
}

type ArticleMugDetailsRead struct {
	ArticleID                    int        `json:"articleId"`
	HeightMm                     int        `json:"heightMm"`
	DiameterMm                   int        `json:"diameterMm"`
	PrintTemplateWidthMm         int        `json:"printTemplateWidthMm"`
	PrintTemplateHeightMm        int        `json:"printTemplateHeightMm"`
	DocumentFormatWidthMm        *int       `json:"documentFormatWidthMm"`
	DocumentFormatHeightMm       *int       `json:"documentFormatHeightMm"`
	DocumentFormatMarginBottomMm *int       `json:"documentFormatMarginBottomMm"`
	FillingQuantity              *string    `json:"fillingQuantity"`
	DishwasherSafe               bool       `json:"dishwasherSafe"`
	CreatedAt                    *time.Time `json:"createdAt"`
	UpdatedAt                    *time.Time `json:"updatedAt"`
}

type ArticleShirtDetailsRead struct {
	ArticleID        int        `json:"articleId"`
	Material         string     `json:"material"`
	CareInstructions *string    `json:"careInstructions"`
	FitType          string     `json:"fitType"`
	AvailableSizes   []string   `json:"availableSizes"`
	CreatedAt        *time.Time `json:"createdAt"`
	UpdatedAt        *time.Time `json:"updatedAt"`
}

type CostCalculationRead struct {
	ID                       int        `json:"id"`
	ArticleID                int        `json:"articleId"`
	PurchasePriceNet         int        `json:"purchasePriceNet"`
	PurchasePriceTax         int        `json:"purchasePriceTax"`
	PurchasePriceGross       int        `json:"purchasePriceGross"`
	PurchaseCostNet          int        `json:"purchaseCostNet"`
	PurchaseCostTax          int        `json:"purchaseCostTax"`
	PurchaseCostGross        int        `json:"purchaseCostGross"`
	PurchaseCostPercent      float64    `json:"purchaseCostPercent"`
	PurchaseTotalNet         int        `json:"purchaseTotalNet"`
	PurchaseTotalTax         int        `json:"purchaseTotalTax"`
	PurchaseTotalGross       int        `json:"purchaseTotalGross"`
	PurchasePriceUnit        string     `json:"purchasePriceUnit"`
	PurchaseVatRateID        *int       `json:"purchaseVatRateId"`
	PurchaseVatRatePercent   float64    `json:"purchaseVatRatePercent"`
	PurchaseCalculationMode  string     `json:"purchaseCalculationMode"`
	SalesVatRateID           *int       `json:"salesVatRateId"`
	SalesVatRatePercent      float64    `json:"salesVatRatePercent"`
	SalesMarginNet           int        `json:"salesMarginNet"`
	SalesMarginTax           int        `json:"salesMarginTax"`
	SalesMarginGross         int        `json:"salesMarginGross"`
	SalesMarginPercent       float64    `json:"salesMarginPercent"`
	SalesTotalNet            int        `json:"salesTotalNet"`
	SalesTotalTax            int        `json:"salesTotalTax"`
	SalesTotalGross          int        `json:"salesTotalGross"`
	SalesPriceUnit           string     `json:"salesPriceUnit"`
	SalesCalculationMode     string     `json:"salesCalculationMode"`
	PurchasePriceCorresponds string     `json:"purchasePriceCorresponds"`
	SalesPriceCorresponds    string     `json:"salesPriceCorresponds"`
	PurchaseActiveRow        string     `json:"purchaseActiveRow"`
	SalesActiveRow           string     `json:"salesActiveRow"`
	CreatedAt                *time.Time `json:"createdAt"`
	UpdatedAt                *time.Time `json:"updatedAt"`
}

type ArticleRead struct {
	ID                    int                       `json:"id"`
	Name                  string                    `json:"name"`
	DescriptionShort      string                    `json:"descriptionShort"`
	DescriptionLong       string                    `json:"descriptionLong"`
	Active                bool                      `json:"active"`
	ArticleType           string                    `json:"articleType"`
	CategoryID            int                       `json:"categoryId"`
	CategoryName          string                    `json:"categoryName"`
	SubcategoryID         *int                      `json:"subcategoryId"`
	SubcategoryName       *string                   `json:"subcategoryName"`
	SupplierID            *int                      `json:"supplierId"`
	SupplierName          *string                   `json:"supplierName"`
	SupplierArticleName   *string                   `json:"supplierArticleName"`
	SupplierArticleNumber *string                   `json:"supplierArticleNumber"`
	MugVariants           []ArticleMugVariantRead   `json:"mugVariants"`
	ShirtVariants         []ArticleShirtVariantRead `json:"shirtVariants"`
	MugDetails            *ArticleMugDetailsRead    `json:"mugDetails"`
	ShirtDetails          *ArticleShirtDetailsRead  `json:"shirtDetails"`
	CostCalculation       *CostCalculationRead      `json:"costCalculation"`
	CreatedAt             *time.Time                `json:"createdAt"`
	UpdatedAt             *time.Time                `json:"updatedAt"`
}

type PaginatedResponse[T any] struct {
	Content       []T   `json:"content"`
	CurrentPage   int   `json:"currentPage"`
	TotalPages    int   `json:"totalPages"`
	TotalElements int64 `json:"totalElements"`
	Size          int   `json:"size"`
}

// Public mug DTOs
type PublicMugVariantRead struct {
	ID                   int        `json:"id"`
	MugID                int        `json:"mugId"`
	ColorCode            string     `json:"colorCode"`
	Name                 string     `json:"name"`
	ExampleImageURL      *string    `json:"exampleImageUrl"`
	ArticleVariantNumber *string    `json:"articleVariantNumber"`
	IsDefault            bool       `json:"isDefault"`
	Active               bool       `json:"active"`
	ExampleImageFilename *string    `json:"exampleImageFilename"`
	CreatedAt            *time.Time `json:"createdAt"`
	UpdatedAt            *time.Time `json:"updatedAt"`
}

type PublicMugRead struct {
	ID                    int                    `json:"id"`
	Name                  string                 `json:"name"`
	Price                 float64                `json:"price"`
	Image                 *string                `json:"image"`
	FillingQuantity       *string                `json:"fillingQuantity"`
	DescriptionShort      *string                `json:"descriptionShort"`
	DescriptionLong       *string                `json:"descriptionLong"`
	HeightMm              int                    `json:"heightMm"`
	DiameterMm            int                    `json:"diameterMm"`
	PrintTemplateWidthMm  int                    `json:"printTemplateWidthMm"`
	PrintTemplateHeightMm int                    `json:"printTemplateHeightMm"`
	DishwasherSafe        bool                   `json:"dishwasherSafe"`
	Variants              []PublicMugVariantRead `json:"variants"`
}

// Admin auxiliary DTOs
type MugWithVariantsSummary struct {
	ID                  int                 `json:"id"`
	Name                string              `json:"name"`
	SupplierArticleName *string             `json:"supplierArticleName"`
	Variants            []MugVariantSummary `json:"variants"`
}

type MugVariantSummary struct {
	ID                   int     `json:"id"`
	Name                 string  `json:"name"`
	InsideColorCode      string  `json:"insideColorCode"`
	OutsideColorCode     string  `json:"outsideColorCode"`
	ArticleVariantNumber *string `json:"articleVariantNumber"`
	ExampleImageURL      *string `json:"exampleImageUrl"`
	Active               bool    `json:"active"`
}

// Assemblers
func toMugVariantRead(v *MugVariant) ArticleMugVariantRead {
	return ArticleMugVariantRead{
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

func toShirtVariantRead(v *ShirtVariant) ArticleShirtVariantRead {
	return ArticleShirtVariantRead{
		ID:              v.ID,
		ArticleID:       v.ArticleID,
		Color:           v.Color,
		Size:            v.Size,
		ExampleImageURL: strPtrOrNil(publicShirtVariantExampleURL(v.ExampleImageFilename)),
		CreatedAt:       timePtr(v.CreatedAt),
		UpdatedAt:       timePtr(v.UpdatedAt),
	}
}

func toMugDetailsRead(d *MugDetails) *ArticleMugDetailsRead {
	if d == nil {
		return nil
	}
	return &ArticleMugDetailsRead{
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

func toShirtDetailsRead(d *ShirtDetails) *ArticleShirtDetailsRead {
	if d == nil {
		return nil
	}
	sizes := []string{}
	if strings.TrimSpace(d.AvailableSizes) != "" {
		for _, s := range strings.Split(d.AvailableSizes, ",") {
			s2 := strings.TrimSpace(s)
			if s2 != "" {
				sizes = append(sizes, s2)
			}
		}
	}
	return &ArticleShirtDetailsRead{
		ArticleID:        d.ArticleID,
		Material:         d.Material,
		CareInstructions: d.CareInstructions,
		FitType:          d.FitType,
		AvailableSizes:   sizes,
		CreatedAt:        timePtr(d.CreatedAt),
		UpdatedAt:        timePtr(d.UpdatedAt),
	}
}

func toCostRead(c *CostCalculation) *CostCalculationRead {
	if c == nil {
		return nil
	}
	return &CostCalculationRead{
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

func toArticleRead(a *Article, catName string, subcatName *string, supplierName *string, mugDetails *MugDetails, shirtDetails *ShirtDetails) ArticleRead {
	out := ArticleRead{
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
		MugDetails:            toMugDetailsRead(mugDetails),
		ShirtDetails:          toShirtDetailsRead(shirtDetails),
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
		// Build URL relative to storage root's public folder
		dir := loc.MugVariantExample()
		if rel, rerr := filepath.Rel(loc.Root, dir); rerr == nil {
			// Ensure URL uses forward slashes
			relURL := filepath.ToSlash(rel)
			return "/" + relURL + "/" + filepath.Base(*filename)
		}
	}
	// Fallback to default public prefix
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
