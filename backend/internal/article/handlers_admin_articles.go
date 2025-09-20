package article

import (
	"errors"
	"net/http"
	"strconv"
	"strings"
	"time"

	"github.com/gin-gonic/gin"
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

func registerAdminArticleRoutes(r *gin.Engine, adminMiddleware gin.HandlerFunc, svc *Service) {
	grp := r.Group("/api/admin/articles")
	grp.Use(adminMiddleware)

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
		opts := ArticleListOptions{Page: page, Size: size}
		if t := strings.TrimSpace(c.Query("type")); t != "" {
			opts.ArticleType = t
		}
		if cat := strings.TrimSpace(c.Query("categoryId")); cat != "" {
			if id, err := strconv.Atoi(cat); err == nil {
				opts.CategoryID = &id
			}
		}
		if sub := strings.TrimSpace(c.Query("subcategoryId")); sub != "" {
			if id, err := strconv.Atoi(sub); err == nil {
				opts.SubcategoryID = &id
			}
		}
		if act := strings.TrimSpace(c.Query("active")); act != "" {
			if act == "true" || act == "1" {
				val := true
				opts.Active = &val
			} else if act == "false" || act == "0" {
				val := false
				opts.Active = &val
			}
		}
		opts.Search = strings.TrimSpace(c.Query("search"))
		items, total, err := svc.ListArticles(c.Request.Context(), opts)
		if err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to fetch articles"})
			return
		}
		out := make([]ArticleResponse, 0, len(items))
		for i := range items {
			item := items[i]
			resp := toArticleResponse(&item.Article, item.CategoryName, item.SubcategoryName, item.SupplierName, nil, nil)
			for j := range item.MugVariants {
				mv := item.MugVariants[j]
				resp.MugVariants = append(resp.MugVariants, toArticleMugVariantResponse(&mv))
			}
			for j := range item.ShirtVariants {
				sv := item.ShirtVariants[j]
				resp.ShirtVariants = append(resp.ShirtVariants, toArticleShirtVariantResponse(&sv))
			}
			out = append(out, resp)
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
		id, err := strconv.Atoi(c.Param("id"))
		if err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Invalid article id"})
			return
		}
		detail, err := svc.GetArticleDetail(c.Request.Context(), id)
		if err != nil {
			if errorsIsNotFound(err) {
				c.JSON(http.StatusNotFound, gin.H{"detail": "Article not found"})
				return
			}
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to fetch article"})
			return
		}
		resp := toArticleResponse(&detail.Article, detail.CategoryName, detail.SubcategoryName, detail.SupplierName, detail.MugDetails, detail.ShirtDetails)
		for i := range detail.MugVariants {
			mv := detail.MugVariants[i]
			resp.MugVariants = append(resp.MugVariants, toArticleMugVariantResponse(&mv))
		}
		for i := range detail.ShirtVariants {
			sv := detail.ShirtVariants[i]
			resp.ShirtVariants = append(resp.ShirtVariants, toArticleShirtVariantResponse(&sv))
		}
		resp.CostCalculation = toCostCalculationResponse(detail.CostCalculation)
		c.JSON(http.StatusOK, resp)
	})

	// Create
	grp.POST("", func(c *gin.Context) {
		var payload createArticleRequest
		if err := c.ShouldBindJSON(&payload); err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Invalid payload"})
			return
		}
		if payload.CategoryID <= 0 {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Category ID is required"})
			return
		}
		exists, err := svc.CategoryExists(c.Request.Context(), payload.CategoryID)
		if err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to validate category"})
			return
		}
		if !exists {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Category not found"})
			return
		}
		if payload.SubcategoryID != nil {
			exists, err = svc.SubcategoryExists(c.Request.Context(), *payload.SubcategoryID)
			if err != nil {
				c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to validate subcategory"})
				return
			}
			if !exists {
				c.JSON(http.StatusBadRequest, gin.H{"detail": "Subcategory not found"})
				return
			}
		}
		if payload.SupplierID != nil {
			exists, err = svc.SupplierExists(c.Request.Context(), *payload.SupplierID)
			if err != nil {
				c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to validate supplier"})
				return
			}
			if !exists {
				c.JSON(http.StatusBadRequest, gin.H{"detail": "Supplier not found"})
				return
			}
		}
		if payload.ArticleType == ArticleTypeMug && payload.MugDetails == nil {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Mug details are required for MUG"})
			return
		}
		if payload.ArticleType == ArticleTypeShirt && payload.ShirtDetails == nil {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Shirt details are required for SHIRT"})
			return
		}
		articleDomain := Article{
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
		mugDetails := mapCreateMugDetails(payload.MugDetails)
		shirtDetails := mapCreateShirtDetails(payload.ShirtDetails)
		cost := mapCostCalculation(payload.CostCalculation)
		mugVariants := mapCreateMugVariants(payload.MugVariants)
		shirtVariants := mapCreateShirtVariants(payload.ShirtVariants)
		detail, err := svc.CreateArticle(c.Request.Context(), &articleDomain, mugDetails, shirtDetails, cost, mugVariants, shirtVariants)
		if err != nil {
			switch {
			case errors.Is(err, ErrCategoryNotFound), errors.Is(err, ErrSubcategoryNotFound), errors.Is(err, ErrSupplierNotFound), errors.Is(err, ErrVatNotFound):
				c.JSON(http.StatusBadRequest, gin.H{"detail": err.Error()})
			default:
				c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to create article"})
			}
			return
		}
		c.Redirect(http.StatusSeeOther, "/api/admin/articles/"+strconv.Itoa(detail.Article.ID))
	})

	// Update
	grp.PUT("/:id", func(c *gin.Context) {
		id, err := strconv.Atoi(c.Param("id"))
		if err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Invalid article id"})
			return
		}
		existing, err := svc.GetArticle(c.Request.Context(), id)
		if err != nil {
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
		exists, err := svc.CategoryExists(c.Request.Context(), payload.CategoryID)
		if err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to validate category"})
			return
		}
		if !exists {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Category not found"})
			return
		}
		if payload.SubcategoryID != nil {
			exists, err = svc.SubcategoryExists(c.Request.Context(), *payload.SubcategoryID)
			if err != nil {
				c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to validate subcategory"})
				return
			}
			if !exists {
				c.JSON(http.StatusBadRequest, gin.H{"detail": "Subcategory not found"})
				return
			}
		}
		if payload.SupplierID != nil {
			exists, err = svc.SupplierExists(c.Request.Context(), *payload.SupplierID)
			if err != nil {
				c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to validate supplier"})
				return
			}
			if !exists {
				c.JSON(http.StatusBadRequest, gin.H{"detail": "Supplier not found"})
				return
			}
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
		mugDetails := mapCreateMugDetails(payload.MugDetails)
		shirtDetails := mapCreateShirtDetails(payload.ShirtDetails)
		cost := mapCostCalculation(payload.CostCalculation)
		detail, err := svc.UpdateArticle(c.Request.Context(), &existing, mugDetails, shirtDetails, cost)
		if err != nil {
			switch {
			case errors.Is(err, ErrCategoryNotFound), errors.Is(err, ErrSubcategoryNotFound), errors.Is(err, ErrSupplierNotFound), errors.Is(err, ErrVatNotFound):
				c.JSON(http.StatusBadRequest, gin.H{"detail": err.Error()})
			default:
				c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to update article"})
			}
			return
		}
		c.Redirect(http.StatusSeeOther, "/api/admin/articles/"+strconv.Itoa(detail.Article.ID))
	})

	grp.DELETE("/:id", func(c *gin.Context) {
		id, err := strconv.Atoi(c.Param("id"))
		if err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Invalid article id"})
			return
		}
		if _, err := svc.GetArticle(c.Request.Context(), id); err != nil {
			if errorsIsNotFound(err) {
				c.Status(http.StatusNoContent)
				return
			}
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to fetch article"})
			return
		}
		if err := svc.DeleteArticle(c.Request.Context(), id); err != nil {
			switch {
			case errors.Is(err, ErrArticleHasOrders):
				c.JSON(http.StatusBadRequest, gin.H{"detail": err.Error()})
			default:
				c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to delete article"})
			}
			return
		}
		c.Status(http.StatusNoContent)
	})
}

func mapCreateMugVariants(reqs []createMugVariantRequest) []MugVariant {
	variants := make([]MugVariant, 0, len(reqs))
	for i := range reqs {
		req := reqs[i]
		variant := MugVariant{
			InsideColorCode:      req.InsideColorCode,
			OutsideColorCode:     req.OutsideColorCode,
			Name:                 req.Name,
			ArticleVariantNumber: req.ArticleVariantNumber,
		}
		if req.IsDefault != nil {
			variant.IsDefault = *req.IsDefault
		}
		if req.Active != nil {
			variant.Active = *req.Active
		} else {
			variant.Active = true
		}
		variants = append(variants, variant)
	}
	return variants
}

func mapCreateShirtVariants(reqs []createShirtVariantRequest) []ShirtVariant {
	variants := make([]ShirtVariant, 0, len(reqs))
	for i := range reqs {
		req := reqs[i]
		variants = append(variants, ShirtVariant{
			Color:                req.Color,
			Size:                 req.Size,
			ExampleImageFilename: req.ExampleImageFilename,
		})
	}
	return variants
}

func mapCreateMugDetails(req *createMugDetailsRequest) *MugDetails {
	if req == nil {
		return nil
	}
	return &MugDetails{
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
}

func mapCreateShirtDetails(req *createShirtDetailsRequest) *ShirtDetails {
	if req == nil {
		return nil
	}
	return &ShirtDetails{
		Material:         req.Material,
		CareInstructions: req.CareInstructions,
		FitType:          req.FitType,
		AvailableSizes:   strings.Join(req.AvailableSizes, ","),
	}
}

func mapCostCalculation(req *costCalculationRequest) *Price {
	if req == nil {
		return nil
	}
	return &Price{
		PurchasePriceNet:         req.PurchasePriceNet,
		PurchasePriceTax:         req.PurchasePriceTax,
		PurchasePriceGross:       req.PurchasePriceGross,
		PurchaseCostNet:          req.PurchaseCostNet,
		PurchaseCostTax:          req.PurchaseCostTax,
		PurchaseCostGross:        req.PurchaseCostGross,
		PurchaseCostPercent:      req.PurchaseCostPercent,
		PurchaseTotalNet:         req.PurchaseTotalNet,
		PurchaseTotalTax:         req.PurchaseTotalTax,
		PurchaseTotalGross:       req.PurchaseTotalGross,
		PurchasePriceUnit:        req.PurchasePriceUnit,
		PurchaseVatRateID:        req.PurchaseVatRateId,
		PurchaseVatRatePercent:   req.PurchaseVatRatePercent,
		PurchaseCalculationMode:  req.PurchaseCalculationMode,
		SalesVatRateID:           req.SalesVatRateId,
		SalesVatRatePercent:      req.SalesVatRatePercent,
		SalesMarginNet:           req.SalesMarginNet,
		SalesMarginTax:           req.SalesMarginTax,
		SalesMarginGross:         req.SalesMarginGross,
		SalesMarginPercent:       req.SalesMarginPercent,
		SalesTotalNet:            req.SalesTotalNet,
		SalesTotalTax:            req.SalesTotalTax,
		SalesTotalGross:          req.SalesTotalGross,
		SalesPriceUnit:           req.SalesPriceUnit,
		SalesCalculationMode:     req.SalesCalculationMode,
		PurchasePriceCorresponds: req.PurchasePriceCorresponds,
		SalesPriceCorresponds:    req.SalesPriceCorresponds,
		PurchaseActiveRow:        req.PurchaseActiveRow,
		SalesActiveRow:           req.SalesActiveRow,
	}
}
