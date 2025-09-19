package article

import "time"

// ArticleType constants
const (
	ArticleTypeMug   = "MUG"
	ArticleTypeShirt = "SHIRT"
)

type ArticleCategory struct {
	ID          int
	Name        string
	Description *string
	CreatedAt   time.Time
	UpdatedAt   time.Time
}

type ArticleSubCategory struct {
	ID                int
	ArticleCategoryID int
	ArticleCategory   *ArticleCategory
	Name              string
	Description       *string
	CreatedAt         time.Time
	UpdatedAt         time.Time
}

type Article struct {
	ID                    int
	Name                  string
	DescriptionShort      string
	DescriptionLong       string
	Active                bool
	ArticleType           string
	CategoryID            int
	Category              *ArticleCategory
	SubcategoryID         *int
	Subcategory           *ArticleSubCategory
	SupplierID            *int
	SupplierArticleName   *string
	SupplierArticleNumber *string
	MugVariants           []MugVariant
	ShirtVariants         []ShirtVariant
	CostCalculation       *Price
	CreatedAt             time.Time
	UpdatedAt             time.Time
}

type MugVariant struct {
	ID                   int
	ArticleID            int
	Article              *Article
	InsideColorCode      string
	OutsideColorCode     string
	Name                 string
	ExampleImageFilename *string
	ArticleVariantNumber *string
	IsDefault            bool
	Active               bool
	CreatedAt            time.Time
	UpdatedAt            time.Time
}

type ShirtVariant struct {
	ID                   int
	ArticleID            int
	Article              *Article
	Color                string
	Size                 string
	ExampleImageFilename *string
	CreatedAt            time.Time
	UpdatedAt            time.Time
}

type MugDetails struct {
	ArticleID                    int
	HeightMm                     int
	DiameterMm                   int
	PrintTemplateWidthMm         int
	PrintTemplateHeightMm        int
	DocumentFormatWidthMm        *int
	DocumentFormatHeightMm       *int
	DocumentFormatMarginBottomMm *int
	FillingQuantity              *string
	DishwasherSafe               bool
	CreatedAt                    time.Time
	UpdatedAt                    time.Time
}

type ShirtDetails struct {
	ArticleID        int
	Material         string
	CareInstructions *string
	FitType          string
	// stored as comma-separated string for simplicity across sqlite/postgres
	AvailableSizes string
	CreatedAt      time.Time
	UpdatedAt      time.Time
}

type Price struct {
	ID        int
	ArticleID *int
	Article   *Article
	// Purchase section
	PurchasePriceNet        int
	PurchasePriceTax        int
	PurchasePriceGross      int
	PurchaseCostNet         int
	PurchaseCostTax         int
	PurchaseCostGross       int
	PurchaseCostPercent     float64
	PurchaseTotalNet        int
	PurchaseTotalTax        int
	PurchaseTotalGross      int
	PurchasePriceUnit       string
	PurchaseVatRateID       *int
	PurchaseVatRatePercent  float64
	PurchaseCalculationMode string
	// Sales section
	SalesVatRateID       *int
	SalesVatRatePercent  float64
	SalesMarginNet       int
	SalesMarginTax       int
	SalesMarginGross     int
	SalesMarginPercent   float64
	SalesTotalNet        int
	SalesTotalTax        int
	SalesTotalGross      int
	SalesPriceUnit       string
	SalesCalculationMode string
	// UI state
	PurchasePriceCorresponds string
	SalesPriceCorresponds    string
	PurchaseActiveRow        string
	SalesActiveRow           string
	CreatedAt                time.Time
	UpdatedAt                time.Time
}
