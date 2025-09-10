package article

import (
	"net/http"
	"strconv"
	"strings"

	"github.com/gin-gonic/gin"
	"gorm.io/gorm"

	"voenix/backend-go/internal/auth"
	"voenix/backend-go/internal/supplier"
	"voenix/backend-go/internal/vat"
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
	grp.GET("/", func(c *gin.Context) {
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
		out := make([]ArticleRead, 0, len(rows))
		for i := range rows {
			a := rows[i]
			var cat ArticleCategory
			var catName string
			if err := db.First(&cat, "id = ?", a.CategoryID).Error; err == nil {
				catName = cat.Name
			}
			var subName *string
			if a.SubcategoryID != nil {
				var sc ArticleSubCategory
				if err := db.First(&sc, "id = ?", *a.SubcategoryID).Error; err == nil {
					subName = &sc.Name
				}
			}
			var suppName *string
			if a.SupplierID != nil {
				var s supplier.Supplier
				if err := db.First(&s, "id = ?", *a.SupplierID).Error; err == nil && s.Name != nil {
					suppName = s.Name
				}
			}
			ar := toArticleRead(&a, catName, subName, suppName, nil, nil)
			// Populate variant arrays for the list response to match admin expectations
			if a.ArticleType == ArticleTypeMug {
				var mvs []MugVariant
				_ = db.Where("article_id = ?", a.ID).Order("id asc").Find(&mvs).Error
				mvReads := make([]ArticleMugVariantRead, 0, len(mvs))
				for j := range mvs {
					mvReads = append(mvReads, toMugVariantRead(&mvs[j]))
				}
				ar.MugVariants = mvReads
				// Leave ShirtVariants as nil to serialize as null
			} else if a.ArticleType == ArticleTypeShirt {
				var svs []ShirtVariant
				_ = db.Where("article_id = ?", a.ID).Order("id asc").Find(&svs).Error
				svReads := make([]ArticleShirtVariantRead, 0, len(svs))
				for j := range svs {
					svReads = append(svReads, toShirtVariantRead(&svs[j]))
				}
				ar.ShirtVariants = svReads
				// Leave MugVariants as nil to serialize as null
			}
			out = append(out, ar)
		}
		resp := PaginatedResponse[ArticleRead]{
			Content:       out,
			CurrentPage:   page,
			TotalPages:    (int((total + int64(size) - 1) / int64(size))),
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
		// preload category/subcategory names
		var cat ArticleCategory
		var catName string
		if err := db.First(&cat, "id = ?", art.CategoryID).Error; err == nil {
			catName = cat.Name
		}
		var subName *string
		if art.SubcategoryID != nil {
			var sc ArticleSubCategory
			if err := db.First(&sc, "id = ?", *art.SubcategoryID).Error; err == nil {
				subName = &sc.Name
			}
		}
		var suppName *string
		if art.SupplierID != nil {
			var s supplier.Supplier
			if err := db.First(&s, "id = ?", *art.SupplierID).Error; err == nil && s.Name != nil {
				suppName = s.Name
			}
		}

		// Load variants/details depending on type
		mugVariants := []ArticleMugVariantRead{}
		shirtVariants := []ArticleShirtVariantRead{}
		var mugDetails *MugDetails
		var shirtDetails *ShirtDetails
		var cc *CostCalculation
		db.Where("article_id = ?", art.ID).First(&cc)
		if art.ArticleType == ArticleTypeMug {
			var mds MugDetails
			if err := db.First(&mds, "article_id = ?", art.ID).Error; err == nil {
				mugDetails = &mds
			}
			var vs []MugVariant
			_ = db.Where("article_id = ?", art.ID).Order("id asc").Find(&vs).Error
			for i := range vs {
				mugVariants = append(mugVariants, toMugVariantRead(&vs[i]))
			}
		} else if art.ArticleType == ArticleTypeShirt {
			var sds ShirtDetails
			if err := db.First(&sds, "article_id = ?", art.ID).Error; err == nil {
				shirtDetails = &sds
			}
			var vs []ShirtVariant
			_ = db.Where("article_id = ?", art.ID).Order("id asc").Find(&vs).Error
			for i := range vs {
				shirtVariants = append(shirtVariants, toShirtVariantRead(&vs[i]))
			}
		}

		ar := toArticleRead(&art, catName, subName, suppName, mugDetails, shirtDetails)
		ar.MugVariants = mugVariants
		ar.ShirtVariants = shirtVariants
		ar.CostCalculation = toCostRead(cc)
		c.JSON(http.StatusOK, ar)
	})

	// Create
	grp.POST("/", func(c *gin.Context) {
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
		// Type-specific details
		if a.ArticleType == ArticleTypeMug && payload.MugDetails != nil {
			md := payload.MugDetails
			row := MugDetails{
				ArticleID:                    a.ID,
				HeightMm:                     md.HeightMm,
				DiameterMm:                   md.DiameterMm,
				PrintTemplateWidthMm:         md.PrintTemplateWidthMm,
				PrintTemplateHeightMm:        md.PrintTemplateHeightMm,
				DocumentFormatWidthMm:        md.DocumentFormatWidthMm,
				DocumentFormatHeightMm:       md.DocumentFormatHeightMm,
				DocumentFormatMarginBottomMm: md.DocumentFormatMarginBottomMm,
				FillingQuantity:              md.FillingQuantity,
				DishwasherSafe:               md.DishwasherSafe,
			}
			if err := db.Create(&row).Error; err != nil {
				c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to create mug details"})
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
		if a.ArticleType == ArticleTypeShirt && payload.ShirtDetails != nil {
			sd := payload.ShirtDetails
			sizes := strings.Join(sd.AvailableSizes, ",")
			row := ShirtDetails{ArticleID: a.ID, Material: sd.Material, CareInstructions: sd.CareInstructions, FitType: sd.FitType, AvailableSizes: sizes}
			if err := db.Create(&row).Error; err != nil {
				c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to create shirt details"})
				return
			}
			for _, v := range payload.ShirtVariants {
				sv := ShirtVariant{ArticleID: a.ID, Color: v.Color, Size: v.Size, ExampleImageFilename: v.ExampleImageFilename}
				_ = db.Create(&sv).Error
			}
		}
		// Cost calculation
		if payload.CostCalculation != nil {
			cc := payload.CostCalculation
			if cc.PurchaseVatRateId != nil && !existsByID[vat.ValueAddedTax](db, *cc.PurchaseVatRateId) {
				c.JSON(http.StatusBadRequest, gin.H{"detail": "Purchase VAT not found"})
				return
			}
			if cc.SalesVatRateId != nil && !existsByID[vat.ValueAddedTax](db, *cc.SalesVatRateId) {
				c.JSON(http.StatusBadRequest, gin.H{"detail": "Sales VAT not found"})
				return
			}
			row := CostCalculation{
				ArticleID:                a.ID,
				PurchasePriceNet:         cc.PurchasePriceNet,
				PurchasePriceTax:         cc.PurchasePriceTax,
				PurchasePriceGross:       cc.PurchasePriceGross,
				PurchaseCostNet:          cc.PurchaseCostNet,
				PurchaseCostTax:          cc.PurchaseCostTax,
				PurchaseCostGross:        cc.PurchaseCostGross,
				PurchaseCostPercent:      cc.PurchaseCostPercent,
				PurchaseTotalNet:         cc.PurchaseTotalNet,
				PurchaseTotalTax:         cc.PurchaseTotalTax,
				PurchaseTotalGross:       cc.PurchaseTotalGross,
				PurchasePriceUnit:        cc.PurchasePriceUnit,
				PurchaseVatRateID:        cc.PurchaseVatRateId,
				PurchaseVatRatePercent:   cc.PurchaseVatRatePercent,
				PurchaseCalculationMode:  cc.PurchaseCalculationMode,
				SalesVatRateID:           cc.SalesVatRateId,
				SalesVatRatePercent:      cc.SalesVatRatePercent,
				SalesMarginNet:           cc.SalesMarginNet,
				SalesMarginTax:           cc.SalesMarginTax,
				SalesMarginGross:         cc.SalesMarginGross,
				SalesMarginPercent:       cc.SalesMarginPercent,
				SalesTotalNet:            cc.SalesTotalNet,
				SalesTotalTax:            cc.SalesTotalTax,
				SalesTotalGross:          cc.SalesTotalGross,
				SalesPriceUnit:           cc.SalesPriceUnit,
				SalesCalculationMode:     cc.SalesCalculationMode,
				PurchasePriceCorresponds: cc.PurchasePriceCorresponds,
				SalesPriceCorresponds:    cc.SalesPriceCorresponds,
				PurchaseActiveRow:        cc.PurchaseActiveRow,
				SalesActiveRow:           cc.SalesActiveRow,
			}
			_ = db.Create(&row).Error
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
		// Details upsert
		if existing.ArticleType == ArticleTypeMug && payload.MugDetails != nil {
			var row MugDetails
			if err := db.First(&row, "article_id = ?", existing.ID).Error; err != nil {
				// create
				md := payload.MugDetails
				row = MugDetails{ArticleID: existing.ID, HeightMm: md.HeightMm, DiameterMm: md.DiameterMm,
					PrintTemplateWidthMm: md.PrintTemplateWidthMm, PrintTemplateHeightMm: md.PrintTemplateHeightMm,
					DocumentFormatWidthMm: md.DocumentFormatWidthMm, DocumentFormatHeightMm: md.DocumentFormatHeightMm,
					DocumentFormatMarginBottomMm: md.DocumentFormatMarginBottomMm, FillingQuantity: md.FillingQuantity,
					DishwasherSafe: md.DishwasherSafe,
				}
				_ = db.Create(&row).Error
			} else {
				md := payload.MugDetails
				row.HeightMm = md.HeightMm
				row.DiameterMm = md.DiameterMm
				row.PrintTemplateWidthMm = md.PrintTemplateWidthMm
				row.PrintTemplateHeightMm = md.PrintTemplateHeightMm
				row.DocumentFormatWidthMm = md.DocumentFormatWidthMm
				row.DocumentFormatHeightMm = md.DocumentFormatHeightMm
				row.DocumentFormatMarginBottomMm = md.DocumentFormatMarginBottomMm
				row.FillingQuantity = md.FillingQuantity
				row.DishwasherSafe = md.DishwasherSafe
				_ = db.Save(&row).Error
			}
		}
		if existing.ArticleType == ArticleTypeShirt && payload.ShirtDetails != nil {
			var row ShirtDetails
			if err := db.First(&row, "article_id = ?", existing.ID).Error; err != nil {
				sd := payload.ShirtDetails
				row = ShirtDetails{ArticleID: existing.ID, Material: sd.Material, CareInstructions: sd.CareInstructions, FitType: sd.FitType, AvailableSizes: strings.Join(sd.AvailableSizes, ",")}
				_ = db.Create(&row).Error
			} else {
				sd := payload.ShirtDetails
				row.Material = sd.Material
				row.CareInstructions = sd.CareInstructions
				row.FitType = sd.FitType
				row.AvailableSizes = strings.Join(sd.AvailableSizes, ",")
				_ = db.Save(&row).Error
			}
		}
		// Cost calc upsert
		if payload.CostCalculation != nil {
			ccReq := payload.CostCalculation
			if ccReq.PurchaseVatRateId != nil && !existsByID[vat.ValueAddedTax](db, *ccReq.PurchaseVatRateId) {
				c.JSON(http.StatusBadRequest, gin.H{"detail": "Purchase VAT not found"})
				return
			}
			if ccReq.SalesVatRateId != nil && !existsByID[vat.ValueAddedTax](db, *ccReq.SalesVatRateId) {
				c.JSON(http.StatusBadRequest, gin.H{"detail": "Sales VAT not found"})
				return
			}
			var cc CostCalculation
			if err := db.First(&cc, "article_id = ?", existing.ID).Error; err != nil {
				cc = CostCalculation{ArticleID: existing.ID}
			}
			cc.PurchasePriceNet = ccReq.PurchasePriceNet
			cc.PurchasePriceTax = ccReq.PurchasePriceTax
			cc.PurchasePriceGross = ccReq.PurchasePriceGross
			cc.PurchaseCostNet = ccReq.PurchaseCostNet
			cc.PurchaseCostTax = ccReq.PurchaseCostTax
			cc.PurchaseCostGross = ccReq.PurchaseCostGross
			cc.PurchaseCostPercent = ccReq.PurchaseCostPercent
			cc.PurchaseTotalNet = ccReq.PurchaseTotalNet
			cc.PurchaseTotalTax = ccReq.PurchaseTotalTax
			cc.PurchaseTotalGross = ccReq.PurchaseTotalGross
			cc.PurchasePriceUnit = ccReq.PurchasePriceUnit
			cc.PurchaseVatRateID = ccReq.PurchaseVatRateId
			cc.PurchaseVatRatePercent = ccReq.PurchaseVatRatePercent
			cc.PurchaseCalculationMode = ccReq.PurchaseCalculationMode
			cc.SalesVatRateID = ccReq.SalesVatRateId
			cc.SalesVatRatePercent = ccReq.SalesVatRatePercent
			cc.SalesMarginNet = ccReq.SalesMarginNet
			cc.SalesMarginTax = ccReq.SalesMarginTax
			cc.SalesMarginGross = ccReq.SalesMarginGross
			cc.SalesMarginPercent = ccReq.SalesMarginPercent
			cc.SalesTotalNet = ccReq.SalesTotalNet
			cc.SalesTotalTax = ccReq.SalesTotalTax
			cc.SalesTotalGross = ccReq.SalesTotalGross
			cc.SalesPriceUnit = ccReq.SalesPriceUnit
			cc.SalesCalculationMode = ccReq.SalesCalculationMode
			cc.PurchasePriceCorresponds = ccReq.PurchasePriceCorresponds
			cc.SalesPriceCorresponds = ccReq.SalesPriceCorresponds
			cc.PurchaseActiveRow = ccReq.PurchaseActiveRow
			cc.SalesActiveRow = ccReq.SalesActiveRow
			if cc.ID == 0 {
				_ = db.Create(&cc).Error
			} else {
				_ = db.Save(&cc).Error
			}
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
