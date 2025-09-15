package article

import (
	"time"
)

// ArticleType constants
const (
	ArticleTypeMug   = "MUG"
	ArticleTypeShirt = "SHIRT"
)

type ArticleCategory struct {
	ID          int       `gorm:"primaryKey" json:"id"`
	Name        string    `gorm:"size:255;not null" json:"name"`
	Description *string   `gorm:"type:text" json:"description,omitempty"`
	CreatedAt   time.Time `json:"createdAt"`
	UpdatedAt   time.Time `json:"updatedAt"`
}

func (ArticleCategory) TableName() string { return "article_categories" }

type ArticleSubCategory struct {
	ID                int              `gorm:"primaryKey" json:"id"`
	ArticleCategoryID int              `gorm:"column:article_category_id;not null" json:"articleCategoryId"`
	ArticleCategory   *ArticleCategory `gorm:"foreignKey:ArticleCategoryID;references:ID" json:"category,omitempty"`
	Name              string           `gorm:"size:255;not null" json:"name"`
	Description       *string          `gorm:"type:text" json:"description,omitempty"`
	CreatedAt         time.Time        `json:"createdAt"`
	UpdatedAt         time.Time        `json:"updatedAt"`
}

func (ArticleSubCategory) TableName() string { return "article_sub_categories" }

type Article struct {
	ID                    int                 `gorm:"primaryKey" json:"id"`
	Name                  string              `gorm:"size:255;not null" json:"name"`
	DescriptionShort      string              `gorm:"type:text;not null" json:"descriptionShort"`
	DescriptionLong       string              `gorm:"type:text;not null" json:"descriptionLong"`
	Active                bool                `gorm:"not null;default:true" json:"active"`
	ArticleType           string              `gorm:"size:50;not null" json:"articleType"`
	CategoryID            int                 `gorm:"column:category_id;not null" json:"categoryId"`
	Category              *ArticleCategory    `gorm:"foreignKey:CategoryID;references:ID" json:"category,omitempty"`
	SubcategoryID         *int                `gorm:"column:subcategory_id" json:"subcategoryId"`
	Subcategory           *ArticleSubCategory `gorm:"foreignKey:SubcategoryID;references:ID" json:"subcategory,omitempty"`
	SupplierID            *int                `gorm:"column:supplier_id" json:"supplierId"`
	SupplierArticleName   *string             `gorm:"column:supplier_article_name;size:255" json:"supplierArticleName"`
	SupplierArticleNumber *string             `gorm:"column:supplier_article_number;size:100" json:"supplierArticleNumber"`
	MugVariants           []MugVariant        `gorm:"foreignKey:ArticleID;references:ID" json:"-"`
	ShirtVariants         []ShirtVariant      `gorm:"foreignKey:ArticleID;references:ID" json:"-"`
	CostCalculation       *CostCalculation    `gorm:"foreignKey:ArticleID;references:ID" json:"-"`
	CreatedAt             time.Time           `json:"createdAt"`
	UpdatedAt             time.Time           `json:"updatedAt"`
}

func (Article) TableName() string { return "articles" }

type MugVariant struct {
	ID                   int       `gorm:"primaryKey" json:"id"`
	ArticleID            int       `gorm:"column:article_id;not null" json:"articleId"`
	Article              *Article  `gorm:"foreignKey:ArticleID;references:ID" json:"-"`
	InsideColorCode      string    `gorm:"size:7;not null" json:"insideColorCode"`
	OutsideColorCode     string    `gorm:"size:7;not null" json:"outsideColorCode"`
	Name                 string    `gorm:"size:255;not null" json:"name"`
	ExampleImageFilename *string   `gorm:"size:500;column:example_image_filename" json:"exampleImageFilename"`
	ArticleVariantNumber *string   `gorm:"size:100;column:article_variant_number" json:"articleVariantNumber"`
	IsDefault            bool      `gorm:"column:is_default;not null;default:false" json:"isDefault"`
	Active               bool      `gorm:"not null;default:true" json:"active"`
	CreatedAt            time.Time `json:"createdAt"`
	UpdatedAt            time.Time `json:"updatedAt"`
}

func (MugVariant) TableName() string { return "article_mug_variants" }

type ShirtVariant struct {
	ID                   int       `gorm:"primaryKey" json:"id"`
	ArticleID            int       `gorm:"column:article_id;not null" json:"articleId"`
	Article              *Article  `gorm:"foreignKey:ArticleID;references:ID" json:"-"`
	Color                string    `gorm:"size:100;not null" json:"color"`
	Size                 string    `gorm:"size:50;not null" json:"size"`
	ExampleImageFilename *string   `gorm:"size:500;column:example_image_filename" json:"exampleImageFilename"`
	CreatedAt            time.Time `json:"createdAt"`
	UpdatedAt            time.Time `json:"updatedAt"`
}

func (ShirtVariant) TableName() string { return "article_shirt_variants" }

type MugDetails struct {
	ArticleID                    int       `gorm:"primaryKey;column:article_id" json:"articleId"`
	HeightMm                     int       `gorm:"not null;column:height_mm" json:"heightMm"`
	DiameterMm                   int       `gorm:"not null;column:diameter_mm" json:"diameterMm"`
	PrintTemplateWidthMm         int       `gorm:"not null;column:print_template_width_mm" json:"printTemplateWidthMm"`
	PrintTemplateHeightMm        int       `gorm:"not null;column:print_template_height_mm" json:"printTemplateHeightMm"`
	DocumentFormatWidthMm        *int      `gorm:"column:document_format_width_mm" json:"documentFormatWidthMm"`
	DocumentFormatHeightMm       *int      `gorm:"column:document_format_height_mm" json:"documentFormatHeightMm"`
	DocumentFormatMarginBottomMm *int      `gorm:"column:document_format_margin_bottom_mm" json:"documentFormatMarginBottomMm"`
	FillingQuantity              *string   `gorm:"column:filling_quantity;size:50" json:"fillingQuantity"`
	DishwasherSafe               bool      `gorm:"not null;default:true;column:dishwasher_safe" json:"dishwasherSafe"`
	CreatedAt                    time.Time `json:"createdAt"`
	UpdatedAt                    time.Time `json:"updatedAt"`
}

func (MugDetails) TableName() string { return "article_mug_details" }

// FitType constants
const (
	FitTypeRegular = "REGULAR"
	FitTypeSlim    = "SLIM"
	FitTypeLoose   = "LOOSE"
)

type ShirtDetails struct {
	ArticleID        int     `gorm:"primaryKey;column:article_id" json:"articleId"`
	Material         string  `gorm:"not null" json:"material"`
	CareInstructions *string `gorm:"type:text;column:care_instructions" json:"careInstructions"`
	FitType          string  `gorm:"size:50;not null;column:fit_type" json:"fitType"`
	// stored as comma-separated string for simplicity across sqlite/postgres
	AvailableSizes string    `gorm:"type:text;not null;column:available_sizes" json:"-"`
	CreatedAt      time.Time `json:"createdAt"`
	UpdatedAt      time.Time `json:"updatedAt"`
}

func (ShirtDetails) TableName() string { return "article_shirt_details" }

// CalculationMode and active row constants
const (
	CalcModeNet   = "NET"
	CalcModeGross = "GROSS"

	PurchaseRowCost        = "cost"
	PurchaseRowCostPercent = "costPercent"

	SalesRowMargin        = "margin"
	SalesRowMarginPercent = "marginPercent"
	SalesRowTotal         = "total"
)

type CostCalculation struct {
	ID        int      `gorm:"primaryKey" json:"id"`
	ArticleID *int     `gorm:"uniqueIndex;column:article_id" json:"articleId"`
	Article   *Article `gorm:"foreignKey:ArticleID;references:ID" json:"-"`
	// Purchase section
	PurchasePriceNet        int     `gorm:"not null;column:purchase_price_net" json:"purchasePriceNet"`
	PurchasePriceTax        int     `gorm:"not null;column:purchase_price_tax" json:"purchasePriceTax"`
	PurchasePriceGross      int     `gorm:"not null;column:purchase_price_gross" json:"purchasePriceGross"`
	PurchaseCostNet         int     `gorm:"not null;column:purchase_cost_net" json:"purchaseCostNet"`
	PurchaseCostTax         int     `gorm:"not null;column:purchase_cost_tax" json:"purchaseCostTax"`
	PurchaseCostGross       int     `gorm:"not null;column:purchase_cost_gross" json:"purchaseCostGross"`
	PurchaseCostPercent     float64 `gorm:"not null;column:purchase_cost_percent" json:"purchaseCostPercent"`
	PurchaseTotalNet        int     `gorm:"not null;column:purchase_total_net" json:"purchaseTotalNet"`
	PurchaseTotalTax        int     `gorm:"not null;column:purchase_total_tax" json:"purchaseTotalTax"`
	PurchaseTotalGross      int     `gorm:"not null;column:purchase_total_gross" json:"purchaseTotalGross"`
	PurchasePriceUnit       string  `gorm:"size:50;not null;column:purchase_price_unit" json:"purchasePriceUnit"`
	PurchaseVatRateID       *int    `gorm:"column:purchase_vat_rate_id" json:"purchaseVatRateId"`
	PurchaseVatRatePercent  float64 `gorm:"not null;column:purchase_vat_rate_percent" json:"purchaseVatRatePercent"`
	PurchaseCalculationMode string  `gorm:"size:10;not null;column:purchase_calculation_mode" json:"purchaseCalculationMode"`
	// Sales section
	SalesVatRateID       *int    `gorm:"column:sales_vat_rate_id" json:"salesVatRateId"`
	SalesVatRatePercent  float64 `gorm:"not null;column:sales_vat_rate_percent" json:"salesVatRatePercent"`
	SalesMarginNet       int     `gorm:"not null;column:sales_margin_net" json:"salesMarginNet"`
	SalesMarginTax       int     `gorm:"not null;column:sales_margin_tax" json:"salesMarginTax"`
	SalesMarginGross     int     `gorm:"not null;column:sales_margin_gross" json:"salesMarginGross"`
	SalesMarginPercent   float64 `gorm:"not null;column:sales_margin_percent" json:"salesMarginPercent"`
	SalesTotalNet        int     `gorm:"not null;column:sales_total_net" json:"salesTotalNet"`
	SalesTotalTax        int     `gorm:"not null;column:sales_total_tax" json:"salesTotalTax"`
	SalesTotalGross      int     `gorm:"not null;column:sales_total_gross" json:"salesTotalGross"`
	SalesPriceUnit       string  `gorm:"size:50;not null;column:sales_price_unit" json:"salesPriceUnit"`
	SalesCalculationMode string  `gorm:"size:10;not null;column:sales_calculation_mode" json:"salesCalculationMode"`
	// UI state
	PurchasePriceCorresponds string    `gorm:"size:10;not null;column:purchase_price_corresponds" json:"purchasePriceCorresponds"`
	SalesPriceCorresponds    string    `gorm:"size:10;not null;column:sales_price_corresponds" json:"salesPriceCorresponds"`
	PurchaseActiveRow        string    `gorm:"size:20;not null;column:purchase_active_row" json:"purchaseActiveRow"`
	SalesActiveRow           string    `gorm:"size:20;not null;column:sales_active_row" json:"salesActiveRow"`
	CreatedAt                time.Time `json:"createdAt"`
	UpdatedAt                time.Time `json:"updatedAt"`
}

// TableName overrides the default to match the renamed DB table.
// The underlying domain type remains CostCalculation for API compatibility.
func (CostCalculation) TableName() string { return "prices" }

// Preload helpers
// no-op placeholder removed: relations are loaded ad-hoc per handler
