package postgres

import (
	"time"
)

type articleCategoryRow struct {
	ID          int     `gorm:"primaryKey"`
	Name        string  `gorm:"size:255;not null"`
	Description *string `gorm:"type:text"`
	CreatedAt   time.Time
	UpdatedAt   time.Time
}

func (articleCategoryRow) TableName() string { return "article_categories" }

type articleSubCategoryRow struct {
	ID                int `gorm:"primaryKey"`
	ArticleCategoryID int `gorm:"column:article_category_id;not null"`
	ArticleCategory   *articleCategoryRow
	Name              string  `gorm:"size:255;not null"`
	Description       *string `gorm:"type:text"`
	CreatedAt         time.Time
	UpdatedAt         time.Time
}

func (articleSubCategoryRow) TableName() string { return "article_sub_categories" }

type articleRow struct {
	ID                    int                    `gorm:"primaryKey"`
	Name                  string                 `gorm:"size:255;not null"`
	DescriptionShort      string                 `gorm:"type:text;not null"`
	DescriptionLong       string                 `gorm:"type:text;not null"`
	Active                bool                   `gorm:"not null;default:true"`
	ArticleType           string                 `gorm:"size:50;not null"`
	CategoryID            int                    `gorm:"column:category_id;not null"`
	Category              *articleCategoryRow    `gorm:"foreignKey:CategoryID;references:ID"`
	SubcategoryID         *int                   `gorm:"column:subcategory_id"`
	Subcategory           *articleSubCategoryRow `gorm:"foreignKey:SubcategoryID;references:ID"`
	SupplierID            *int                   `gorm:"column:supplier_id"`
	SupplierArticleName   *string                `gorm:"column:supplier_article_name;size:255"`
	SupplierArticleNumber *string                `gorm:"column:supplier_article_number;size:100"`
	MugVariants           []mugVariantRow        `gorm:"foreignKey:ArticleID;references:ID"`
	ShirtVariants         []shirtVariantRow      `gorm:"foreignKey:ArticleID;references:ID"`
	CostCalculation       *priceRow              `gorm:"foreignKey:ArticleID;references:ID"`
	CreatedAt             time.Time
	UpdatedAt             time.Time
}

func (articleRow) TableName() string { return "articles" }

type mugVariantRow struct {
	ID                   int `gorm:"primaryKey"`
	ArticleID            int `gorm:"column:article_id;not null"`
	Article              *articleRow
	InsideColorCode      string  `gorm:"size:7;not null"`
	OutsideColorCode     string  `gorm:"size:7;not null"`
	Name                 string  `gorm:"size:255;not null"`
	ExampleImageFilename *string `gorm:"size:500;column:example_image_filename"`
	ArticleVariantNumber *string `gorm:"size:100;column:article_variant_number"`
	IsDefault            bool    `gorm:"column:is_default;not null;default:false"`
	Active               bool    `gorm:"not null;default:true"`
	CreatedAt            time.Time
	UpdatedAt            time.Time
}

func (mugVariantRow) TableName() string { return "article_mug_variants" }

type shirtVariantRow struct {
	ID                   int `gorm:"primaryKey"`
	ArticleID            int `gorm:"column:article_id;not null"`
	Article              *articleRow
	Color                string  `gorm:"size:100;not null"`
	Size                 string  `gorm:"size:50;not null"`
	ExampleImageFilename *string `gorm:"size:500;column:example_image_filename"`
	CreatedAt            time.Time
	UpdatedAt            time.Time
}

func (shirtVariantRow) TableName() string { return "article_shirt_variants" }

type mugDetailsRow struct {
	ArticleID                    int     `gorm:"primaryKey;column:article_id"`
	HeightMm                     int     `gorm:"not null;column:height_mm"`
	DiameterMm                   int     `gorm:"not null;column:diameter_mm"`
	PrintTemplateWidthMm         int     `gorm:"not null;column:print_template_width_mm"`
	PrintTemplateHeightMm        int     `gorm:"not null;column:print_template_height_mm"`
	DocumentFormatWidthMm        *int    `gorm:"column:document_format_width_mm"`
	DocumentFormatHeightMm       *int    `gorm:"column:document_format_height_mm"`
	DocumentFormatMarginBottomMm *int    `gorm:"column:document_format_margin_bottom_mm"`
	FillingQuantity              *string `gorm:"column:filling_quantity;size:50"`
	DishwasherSafe               bool    `gorm:"not null;default:true;column:dishwasher_safe"`
	CreatedAt                    time.Time
	UpdatedAt                    time.Time
}

func (mugDetailsRow) TableName() string { return "article_mug_details" }

type shirtDetailsRow struct {
	ArticleID        int     `gorm:"primaryKey;column:article_id"`
	Material         string  `gorm:"not null"`
	CareInstructions *string `gorm:"type:text;column:care_instructions"`
	FitType          string  `gorm:"size:50;not null;column:fit_type"`
	// stored as comma-separated string for simplicity across sqlite/postgres
	AvailableSizes string `gorm:"type:text;not null;column:available_sizes"`
	CreatedAt      time.Time
	UpdatedAt      time.Time
}

func (shirtDetailsRow) TableName() string { return "article_shirt_details" }

type priceRow struct {
	ID        int  `gorm:"primaryKey"`
	ArticleID *int `gorm:"uniqueIndex;column:article_id"`
	Article   *articleRow
	// Purchase section
	PurchasePriceNet        int     `gorm:"not null;column:purchase_price_net"`
	PurchasePriceTax        int     `gorm:"not null;column:purchase_price_tax"`
	PurchasePriceGross      int     `gorm:"not null;column:purchase_price_gross"`
	PurchaseCostNet         int     `gorm:"not null;column:purchase_cost_net"`
	PurchaseCostTax         int     `gorm:"not null;column:purchase_cost_tax"`
	PurchaseCostGross       int     `gorm:"not null;column:purchase_cost_gross"`
	PurchaseCostPercent     float64 `gorm:"not null;column:purchase_cost_percent"`
	PurchaseTotalNet        int     `gorm:"not null;column:purchase_total_net"`
	PurchaseTotalTax        int     `gorm:"not null;column:purchase_total_tax"`
	PurchaseTotalGross      int     `gorm:"not null;column:purchase_total_gross"`
	PurchasePriceUnit       string  `gorm:"size:50;not null;column:purchase_price_unit"`
	PurchaseVatRateID       *int    `gorm:"column:purchase_vat_rate_id"`
	PurchaseVatRatePercent  float64 `gorm:"not null;column:purchase_vat_rate_percent"`
	PurchaseCalculationMode string  `gorm:"size:10;not null;column:purchase_calculation_mode"`
	// Sales section
	SalesVatRateID       *int    `gorm:"column:sales_vat_rate_id"`
	SalesVatRatePercent  float64 `gorm:"not null;column:sales_vat_rate_percent"`
	SalesMarginNet       int     `gorm:"not null;column:sales_margin_net"`
	SalesMarginTax       int     `gorm:"not null;column:sales_margin_tax"`
	SalesMarginGross     int     `gorm:"not null;column:sales_margin_gross"`
	SalesMarginPercent   float64 `gorm:"not null;column:sales_margin_percent"`
	SalesTotalNet        int     `gorm:"not null;column:sales_total_net"`
	SalesTotalTax        int     `gorm:"not null;column:sales_total_tax"`
	SalesTotalGross      int     `gorm:"not null;column:sales_total_gross"`
	SalesPriceUnit       string  `gorm:"size:50;not null;column:sales_price_unit"`
	SalesCalculationMode string  `gorm:"size:10;not null;column:sales_calculation_mode"`
	// UI state
	PurchasePriceCorresponds string `gorm:"size:10;not null;column:purchase_price_corresponds"`
	SalesPriceCorresponds    string `gorm:"size:10;not null;column:sales_price_corresponds"`
	PurchaseActiveRow        string `gorm:"size:20;not null;column:purchase_active_row"`
	SalesActiveRow           string `gorm:"size:20;not null;column:sales_active_row"`
	CreatedAt                time.Time
	UpdatedAt                time.Time
}

func (priceRow) TableName() string { return "prices" }
