package article

import (
	"net/http"
	"strconv"
	"strings"
	"time"

	"github.com/gin-gonic/gin"
	"gorm.io/gorm"

	"voenix/backend/internal/auth"
	"voenix/backend/internal/supplier"
)

// Requests
type createMugVariantRequest struct {
	InsideColorCode      string  `json:"insideColorCode"`
	OutsideColorCode     string  `json:"outsideColorCode"`
	Name                 string  `json:"name"`
	ArticleVariantNumber *string `json:"articleVariantNumber"`
	IsDefault            *bool   `json:"isDefault"`
	Active               *bool   `json:"active"`
}

type createShirtVariantRequest struct {
	Color                string  `json:"color"`
	Size                 string  `json:"size"`
	ExampleImageFilename *string `json:"exampleImageFilename"`
}

type createMugDetailsRequest struct {
	HeightMm                     int     `json:"heightMm"`
	DiameterMm                   int     `json:"diameterMm"`
	PrintTemplateWidthMm         int     `json:"printTemplateWidthMm"`
	PrintTemplateHeightMm        int     `json:"printTemplateHeightMm"`
	DocumentFormatWidthMm        *int    `json:"documentFormatWidthMm"`
	DocumentFormatHeightMm       *int    `json:"documentFormatHeightMm"`
	DocumentFormatMarginBottomMm *int    `json:"documentFormatMarginBottomMm"`
	FillingQuantity              *string `json:"fillingQuantity"`
	DishwasherSafe               bool    `json:"dishwasherSafe"`
}

type createShirtDetailsRequest struct {
	Material         string   `json:"material"`
	CareInstructions *string  `json:"careInstructions"`
	FitType          string   `json:"fitType"`
	AvailableSizes   []string `json:"availableSizes"`
}

type costCalculationRequest struct {
	PurchasePriceNet         int     `json:"purchasePriceNet"`
	PurchasePriceTax         int     `json:"purchasePriceTax"`
	PurchasePriceGross       int     `json:"purchasePriceGross"`
	PurchaseCostNet          int     `json:"purchaseCostNet"`
	PurchaseCostTax          int     `json:"purchaseCostTax"`
	PurchaseCostGross        int     `json:"purchaseCostGross"`
	PurchaseCostPercent      float64 `json:"purchaseCostPercent"`
	PurchaseTotalNet         int     `json:"purchaseTotalNet"`
	PurchaseTotalTax         int     `json:"purchaseTotalTax"`
	PurchaseTotalGross       int     `json:"purchaseTotalGross"`
	PurchasePriceUnit        string  `json:"purchasePriceUnit"`
	PurchaseVatRateId        *int    `json:"purchaseVatRateId"`
	PurchaseVatRatePercent   float64 `json:"purchaseVatRatePercent"`
	PurchaseCalculationMode  string  `json:"purchaseCalculationMode"`
	SalesVatRateId           *int    `json:"salesVatRateId"`
	SalesVatRatePercent      float64 `json:"salesVatRatePercent"`
	SalesMarginNet           int     `json:"salesMarginNet"`
	SalesMarginTax           int     `json:"salesMarginTax"`
	SalesMarginGross         int     `json:"salesMarginGross"`
	SalesMarginPercent       float64 `json:"salesMarginPercent"`
	SalesTotalNet            int     `json:"salesTotalNet"`
	SalesTotalTax            int     `json:"salesTotalTax"`
	SalesTotalGross          int     `json:"salesTotalGross"`
	SalesPriceUnit           string  `json:"salesPriceUnit"`
	SalesCalculationMode     string  `json:"salesCalculationMode"`
	PurchasePriceCorresponds string  `json:"purchasePriceCorresponds"`
	SalesPriceCorresponds    string  `json:"salesPriceCorresponds"`
	PurchaseActiveRow        string  `json:"purchaseActiveRow"`
	SalesActiveRow           string  `json:"salesActiveRow"`
}

// Responses
type articleMugVariantResponse struct {
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

type articleShirtVariantResponse struct {
	ID              int        `json:"id"`
	ArticleID       int        `json:"articleId"`
	Color           string     `json:"color"`
	Size            string     `json:"size"`
	ExampleImageURL *string    `json:"exampleImageUrl"`
	CreatedAt       *time.Time `json:"createdAt"`
	UpdatedAt       *time.Time `json:"updatedAt"`
}

type articleMugDetailsResponse struct {
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

type articleShirtDetailsResponse struct {
	ArticleID        int        `json:"articleId"`
	Material         string     `json:"material"`
	CareInstructions *string    `json:"careInstructions"`
	FitType          string     `json:"fitType"`
	AvailableSizes   []string   `json:"availableSizes"`
	CreatedAt        *time.Time `json:"createdAt"`
	UpdatedAt        *time.Time `json:"updatedAt"`
}

type costCalculationResponse struct {
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

type ArticleResponse struct {
	ID                    int                           `json:"id"`
	Name                  string                        `json:"name"`
	DescriptionShort      string                        `json:"descriptionShort"`
	DescriptionLong       string                        `json:"descriptionLong"`
	Active                bool                          `json:"active"`
	ArticleType           string                        `json:"articleType"`
	CategoryID            int                           `json:"categoryId"`
	CategoryName          string                        `json:"categoryName"`
	SubcategoryID         *int                          `json:"subcategoryId"`
	SubcategoryName       *string                       `json:"subcategoryName"`
	SupplierID            *int                          `json:"supplierId"`
	SupplierName          *string                       `json:"supplierName"`
	SupplierArticleName   *string                       `json:"supplierArticleName"`
	SupplierArticleNumber *string                       `json:"supplierArticleNumber"`
	MugVariants           []articleMugVariantResponse   `json:"mugVariants"`
	ShirtVariants         []articleShirtVariantResponse `json:"shirtVariants"`
	MugDetails            *articleMugDetailsResponse    `json:"mugDetails"`
	ShirtDetails          *articleShirtDetailsResponse  `json:"shirtDetails"`
	CostCalculation       *costCalculationResponse      `json:"costCalculation"`
	CreatedAt             *time.Time                    `json:"createdAt"`
	UpdatedAt             *time.Time                    `json:"updatedAt"`
}

type paginatedResponse[T any] struct {
	Content       []T   `json:"content"`
	CurrentPage   int   `json:"currentPage"`
	TotalPages    int   `json:"totalPages"`
	TotalElements int64 `json:"totalElements"`
	Size          int   `json:"size"`
}

type createArticleRequest struct {
	Name                  string                      `json:"name"`
	DescriptionShort      string                      `json:"descriptionShort"`
	DescriptionLong       string                      `json:"descriptionLong"`
	Active                bool                        `json:"active"`
	ArticleType           string                      `json:"articleType"`
	CategoryID            int                         `json:"categoryId"`
	SubcategoryID         *int                        `json:"subcategoryId"`
	SupplierID            *int                        `json:"supplierId"`
	SupplierArticleName   *string                     `json:"supplierArticleName"`
	SupplierArticleNumber *string                     `json:"supplierArticleNumber"`
	MugVariants           []createMugVariantRequest   `json:"mugVariants"`
	ShirtVariants         []createShirtVariantRequest `json:"shirtVariants"`
	MugDetails            *createMugDetailsRequest    `json:"mugDetails"`
	ShirtDetails          *createShirtDetailsRequest  `json:"shirtDetails"`
	CostCalculation       *costCalculationRequest     `json:"costCalculation"`
}

type updateArticleRequest struct {
	Name                  string                     `json:"name"`
	DescriptionShort      string                     `json:"descriptionShort"`
	DescriptionLong       string                     `json:"descriptionLong"`
	Active                bool                       `json:"active"`
	CategoryID            int                        `json:"categoryId"`
	SubcategoryID         *int                       `json:"subcategoryId"`
	SupplierID            *int                       `json:"supplierId"`
	SupplierArticleName   *string                    `json:"supplierArticleName"`
	SupplierArticleNumber *string                    `json:"supplierArticleNumber"`
	MugDetails            *createMugDetailsRequest   `json:"mugDetails"`
	ShirtDetails          *createShirtDetailsRequest `json:"shirtDetails"`
	CostCalculation       *costCalculationRequest    `json:"costCalculation"`
}

func registerAdminArticleRoutes(r *gin.Engine, db *gorm.DB) {
	grp := r.Group("/api/admin/articles")
	grp.Use(auth.RequireAdmin(db))

	// List with pagination and optional filters
	grp.GET("", func(c *gin.Context) {
		page, _ := strconv.Atoi(c.DefaultQuery("page", "0"))
		size, _ := strconv.Atoi(c.DefaultQuery("size", "50"))
		if size <= 0 {
			size = 50
		}
		if page < 0 {
			page = 0
		}
		q := db.Model(&Article{})
		if t := strings.TrimSpace(c.Query("type")); t != "" {
			q = q.Where("article_type = ?", t)
		}
		if cat := strings.TrimSpace(c.Query("categoryId")); cat != "" {
			q = q.Where("category_id = ?", cat)
		}
		if sub := strings.TrimSpace(c.Query("subcategoryId")); sub != "" {
			q = q.Where("subcategory_id = ?", sub)
		}
		if act := strings.TrimSpace(c.Query("active")); act != "" {
			if act == "true" || act == "1" {
				q = q.Where("active = ?", true)
			} else if act == "false" || act == "0" {
				q = q.Where("active = ?", false)
			}
		}
		if search := strings.TrimSpace(c.Query("search")); search != "" {
			like := "%" + strings.ToLower(search) + "%"
			q = q.Where("LOWER(name) LIKE ? OR LOWER(description_short) LIKE ?", like, like)
		}
		var total int64
		if err := q.Count(&total).Error; err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to count"})
			return
		}
		var rows []Article
		if err := q.Order("id desc").Limit(size).Offset(page * size).Find(&rows).Error; err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to fetch articles"})
			return
		}
		// load categories, subcategories, supplier names and variant summaries
		out := make([]ArticleResponse, 0, len(rows))
		for i := range rows {
			a := rows[i]
			catName, subName, suppName := articleNames(db, &a)
			ar := toArticleResponse(&a, catName, subName, suppName, nil, nil)
			mv, sv := listArticleVariantsRead(db, &a)
			ar.MugVariants = mv
			ar.ShirtVariants = sv
			out = append(out, ar)
		}
		resp := paginatedResponse[ArticleResponse]{
			Content:       out,
			CurrentPage:   page,
			TotalPages:    int((total + int64(size) - 1) / int64(size)),
			TotalElements: total,
			Size:          size,
		}
		c.JSON(http.StatusOK, resp)
	})

	// Get by ID with details
	grp.GET("/:id", func(c *gin.Context) {
		id := c.Param("id")
		var art Article
		if err := db.First(&art, "id = ?", id).Error; err != nil {
			if errorsIsNotFound(err) {
				c.JSON(http.StatusNotFound, gin.H{"detail": "Article not found"})
				return
			}
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to fetch article"})
			return
		}
		// preload names
		catName, subName, suppName := articleNames(db, &art)

		// Load variants/details depending on type
		mv, sv := listArticleVariantsRead(db, &art)
		var mugDetails *MugDetails
		var shirtDetails *ShirtDetails
		var cc *CostCalculation
		db.Where("article_id = ?", art.ID).First(&cc)
		if art.ArticleType == ArticleTypeMug {
			var mds MugDetails
			if err := db.First(&mds, "article_id = ?", art.ID).Error; err == nil {
				mugDetails = &mds
			}
		} else if art.ArticleType == ArticleTypeShirt {
			var sds ShirtDetails
			if err := db.First(&sds, "article_id = ?", art.ID).Error; err == nil {
				shirtDetails = &sds
			}
		}

		ar := toArticleResponse(&art, catName, subName, suppName, mugDetails, shirtDetails)
		ar.MugVariants = mv
		ar.ShirtVariants = sv
		ar.CostCalculation = toCostCalculationResponse(cc)
		c.JSON(http.StatusOK, ar)
	})

	// Create
	grp.POST("", func(c *gin.Context) {
		var payload createArticleRequest
		if err := c.ShouldBindJSON(&payload); err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Invalid payload"})
			return
		}
		// Validate category exists
		if payload.CategoryID <= 0 {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Category ID is required"})
			return
		}
		if !existsByID[ArticleCategory](db, payload.CategoryID) {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Category not found"})
			return
		}
		// Validate subcategory if provided
		if payload.SubcategoryID != nil && !existsByID[ArticleSubCategory](db, *payload.SubcategoryID) {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Subcategory not found"})
			return
		}
		// Validate supplier if provided
		if payload.SupplierID != nil && !existsByID[supplier.Supplier](db, *payload.SupplierID) {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Supplier not found"})
			return
		}
		// Validate type-specific details
		if payload.ArticleType == ArticleTypeMug && payload.MugDetails == nil {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Mug details are required for MUG"})
			return
		}
		if payload.ArticleType == ArticleTypeShirt && payload.ShirtDetails == nil {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Shirt details are required for SHIRT"})
			return
		}
		a := Article{
			Name:                  payload.Name,
			DescriptionShort:      payload.DescriptionShort,
			DescriptionLong:       payload.DescriptionLong,
			Active:                payload.Active,
			ArticleType:           payload.ArticleType,
			CategoryID:            payload.CategoryID,
			SubcategoryID:         payload.SubcategoryID,
			SupplierID:            payload.SupplierID,
			SupplierArticleName:   payload.SupplierArticleName,
			SupplierArticleNumber: payload.SupplierArticleNumber,
		}
		if err := db.Create(&a).Error; err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to create article"})
			return
		}
		// Type-specific details and optional cost calculation
		if a.ArticleType == ArticleTypeMug {
			if err := upsertMugDetails(db, a.ID, payload.MugDetails); err != nil {
				c.JSON(http.StatusBadRequest, gin.H{"detail": err.Error()})
				return
			}
			// initial mug variants
			for _, v := range payload.MugVariants {
				mv := MugVariant{ArticleID: a.ID, InsideColorCode: v.InsideColorCode, OutsideColorCode: v.OutsideColorCode, Name: v.Name}
				if v.ArticleVariantNumber != nil {
					mv.ArticleVariantNumber = v.ArticleVariantNumber
				}
				if v.IsDefault != nil {
					mv.IsDefault = *v.IsDefault
				}
				if v.Active != nil {
					mv.Active = *v.Active
				}
				_ = db.Create(&mv).Error
			}
		}
		if a.ArticleType == ArticleTypeShirt {
			if err := upsertShirtDetails(db, a.ID, payload.ShirtDetails); err != nil {
				c.JSON(http.StatusBadRequest, gin.H{"detail": err.Error()})
				return
			}
			for _, v := range payload.ShirtVariants {
				sv := ShirtVariant{ArticleID: a.ID, Color: v.Color, Size: v.Size, ExampleImageFilename: v.ExampleImageFilename}
				_ = db.Create(&sv).Error
			}
		}
		if err := upsertCostCalculation(db, a.ID, payload.CostCalculation); err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"detail": err.Error()})
			return
		}
		// Return full resource
		c.Redirect(http.StatusSeeOther, "/api/admin/articles/"+strconv.Itoa(a.ID))
	})

	// Update
	grp.PUT("/:id", func(c *gin.Context) {
		var existing Article
		if err := db.First(&existing, "id = ?", c.Param("id")).Error; err != nil {
			if errorsIsNotFound(err) {
				c.JSON(http.StatusNotFound, gin.H{"detail": "Article not found"})
				return
			}
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to fetch article"})
			return
		}
		var payload updateArticleRequest
		if err := c.ShouldBindJSON(&payload); err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Invalid payload"})
			return
		}
		if !existsByID[ArticleCategory](db, payload.CategoryID) {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Category not found"})
			return
		}
		if payload.SubcategoryID != nil && !existsByID[ArticleSubCategory](db, *payload.SubcategoryID) {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Subcategory not found"})
			return
		}
		if payload.SupplierID != nil && !existsByID[supplier.Supplier](db, *payload.SupplierID) {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Supplier not found"})
			return
		}
		existing.Name = payload.Name
		existing.DescriptionShort = payload.DescriptionShort
		existing.DescriptionLong = payload.DescriptionLong
		existing.Active = payload.Active
		existing.CategoryID = payload.CategoryID
		existing.SubcategoryID = payload.SubcategoryID
		existing.SupplierID = payload.SupplierID
		existing.SupplierArticleName = payload.SupplierArticleName
		existing.SupplierArticleNumber = payload.SupplierArticleNumber
		if err := db.Save(&existing).Error; err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to update article"})
			return
		}
		// Details + cost calc upsert
		if existing.ArticleType == ArticleTypeMug {
			if err := upsertMugDetails(db, existing.ID, payload.MugDetails); err != nil {
				c.JSON(http.StatusBadRequest, gin.H{"detail": err.Error()})
				return
			}
		}
		if existing.ArticleType == ArticleTypeShirt {
			if err := upsertShirtDetails(db, existing.ID, payload.ShirtDetails); err != nil {
				c.JSON(http.StatusBadRequest, gin.H{"detail": err.Error()})
				return
			}
		}
		if err := upsertCostCalculation(db, existing.ID, payload.CostCalculation); err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"detail": err.Error()})
			return
		}
		// Return full updated resource
		c.Redirect(http.StatusSeeOther, "/api/admin/articles/"+c.Param("id"))
	})

	grp.DELETE("/:id", func(c *gin.Context) {
		var existing Article
		if err := db.First(&existing, "id = ?", c.Param("id")).Error; err != nil {
			if errorsIsNotFound(err) {
				c.Status(http.StatusNoContent)
				return
			}
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to fetch article"})
			return
		}
		// delete dependents best-effort
		_ = db.Delete(&MugVariant{}, "article_id = ?", existing.ID).Error
		_ = db.Delete(&ShirtVariant{}, "article_id = ?", existing.ID).Error
		_ = db.Delete(&MugDetails{}, "article_id = ?", existing.ID).Error
		_ = db.Delete(&ShirtDetails{}, "article_id = ?", existing.ID).Error
		_ = db.Delete(&CostCalculation{}, "article_id = ?", existing.ID).Error
		if err := db.Delete(&Article{}, existing.ID).Error; err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to delete article"})
			return
		}
		c.Status(http.StatusNoContent)
	})
}
