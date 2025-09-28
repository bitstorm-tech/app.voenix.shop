package article

import (
	"io"
	"net/http"
	"os"
	"path/filepath"
	"strconv"
	"strings"

	"github.com/gin-gonic/gin"

	img "voenix/backend/internal/image"
)

type mugVariantCreate struct {
	InsideColorCode      string  `json:"insideColorCode"`
	OutsideColorCode     string  `json:"outsideColorCode"`
	Name                 string  `json:"name"`
	ArticleVariantNumber *string `json:"articleVariantNumber"`
	IsDefault            *bool   `json:"isDefault"`
	Active               *bool   `json:"active"`
}

type mugVariantCopyRequest struct {
	VariantIDs []int `json:"variantIds"`
}

// Responses
type mugWithVariantsSummaryResponse struct {
	ID                  int                         `json:"id"`
	Name                string                      `json:"name"`
	SupplierArticleName *string                     `json:"supplierArticleName"`
	Variants            []mugVariantSummaryResponse `json:"variants"`
}

type mugVariantSummaryResponse struct {
	ID                   int     `json:"id"`
	Name                 string  `json:"name"`
	InsideColorCode      string  `json:"insideColorCode"`
	OutsideColorCode     string  `json:"outsideColorCode"`
	ArticleVariantNumber *string `json:"articleVariantNumber"`
	ExampleImageURL      *string `json:"exampleImageUrl"`
	Active               bool    `json:"active"`
}

func registerAdminMugVariantRoutes(r *gin.Engine, adminMiddleware gin.HandlerFunc, svc *Service) {
	grp := r.Group("/api/admin/articles/mugs")
	grp.Use(adminMiddleware)

	grp.POST("/:articleId/variants", func(c *gin.Context) {
		aid, err := strconv.Atoi(c.Param("articleId"))
		if err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Invalid article id"})
			return
		}
		articleDomain, err := svc.GetArticle(c.Request.Context(), aid)
		if err != nil {
			if errorsIsNotFound(err) {
				c.JSON(http.StatusNotFound, gin.H{"detail": "Article not found"})
				return
			}
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to fetch article"})
			return
		}
		if articleDomain.ArticleType != ArticleTypeMug {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Variants allowed only for MUG articles"})
			return
		}
		var payload mugVariantCreate
		if err := c.ShouldBindJSON(&payload); err != nil || strings.TrimSpace(payload.Name) == "" {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Invalid payload"})
			return
		}
		variant := MugVariant{
			ArticleID:        articleDomain.ID,
			InsideColorCode:  payload.InsideColorCode,
			OutsideColorCode: payload.OutsideColorCode,
			Name:             payload.Name,
		}
		variant.ArticleVariantNumber = payload.ArticleVariantNumber
		if payload.IsDefault != nil {
			variant.IsDefault = *payload.IsDefault
		}
		if payload.Active != nil {
			variant.Active = *payload.Active
		} else {
			variant.Active = true
		}
		created, err := svc.CreateMugVariant(c.Request.Context(), &variant)
		if err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to create variant"})
			return
		}
		c.JSON(http.StatusCreated, toArticleMugVariantResponse(&created))
	})

	grp.PUT("/variants/:variantId", func(c *gin.Context) {
		id, err := strconv.Atoi(c.Param("variantId"))
		if err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Invalid variant id"})
			return
		}
		existing, err := svc.GetMugVariant(c.Request.Context(), id)
		if err != nil {
			if errorsIsNotFound(err) {
				c.JSON(http.StatusNotFound, gin.H{"detail": "Variant not found"})
				return
			}
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to fetch variant"})
			return
		}
		var payload mugVariantCreate
		if err := c.ShouldBindJSON(&payload); err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Invalid payload"})
			return
		}
		existing.InsideColorCode = payload.InsideColorCode
		existing.OutsideColorCode = payload.OutsideColorCode
		existing.Name = payload.Name
		existing.ArticleVariantNumber = payload.ArticleVariantNumber
		if payload.IsDefault != nil {
			existing.IsDefault = *payload.IsDefault
		}
		if payload.Active != nil {
			existing.Active = *payload.Active
		}
		updated, err := svc.UpdateMugVariant(c.Request.Context(), &existing)
		if err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to update variant"})
			return
		}
		c.JSON(http.StatusOK, toArticleMugVariantResponse(&updated))
	})

	grp.DELETE("/variants/:variantId", func(c *gin.Context) {
		id, err := strconv.Atoi(c.Param("variantId"))
		if err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Invalid variant id"})
			return
		}
		if err := svc.DeleteMugVariant(c.Request.Context(), id); err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to delete variant"})
			return
		}
		c.Status(http.StatusNoContent)
	})

	// Upload variant image: expects form field "image" and optional crop fields
	grp.POST("/variants/:variantId/image", func(c *gin.Context) {
		id, err := strconv.Atoi(c.Param("variantId"))
		if err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Invalid variant id"})
			return
		}
		existing, err := svc.GetMugVariant(c.Request.Context(), id)
		if err != nil {
			if errorsIsNotFound(err) {
				c.JSON(http.StatusNotFound, gin.H{"detail": "Variant not found"})
				return
			}
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to fetch variant"})
			return
		}
		fileHeader, err := c.FormFile("image")
		if err != nil || fileHeader == nil {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Missing image"})
			return
		}
		cropX := c.PostForm("cropX")
		cropY := c.PostForm("cropY")
		cropW := c.PostForm("cropWidth")
		cropH := c.PostForm("cropHeight")
		// parse crop, best-effort
		var crop *img.CropArea
		if cropX != "" && cropY != "" && cropW != "" && cropH != "" {
			var fx, fy, fw, fh float64
			if x, err := strconv.ParseFloat(cropX, 64); err == nil {
				fx = x
			}
			if y, err := strconv.ParseFloat(cropY, 64); err == nil {
				fy = y
			}
			if w, err := strconv.ParseFloat(cropW, 64); err == nil {
				fw = w
			}
			if h, err := strconv.ParseFloat(cropH, 64); err == nil {
				fh = h
			}
			crop = &img.CropArea{X: fx, Y: fy, Width: fw, Height: fh}
		}
		f, err := fileHeader.Open()
		if err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Failed to read upload"})
			return
		}
		defer func() { _ = f.Close() }()
		data, _ := io.ReadAll(f)
		if crop != nil {
			data = img.CropImageBytes(data, crop.X, crop.Y, crop.Width, crop.Height)
		}
		imageBytes, err := img.ConvertImageToWebPBytes(data)
		if err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Failed to process image"})
			return
		}
		loc, err := img.NewStorageLocations()
		if err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": err.Error()})
			return
		}
		dir := loc.MugVariantExample()
		path, err := img.StoreImageBytes(imageBytes, dir, "", "webp", false)
		if err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to store image"})
			return
		}
		filename := filepath.Base(path)
		existing.ExampleImageFilename = &filename
		updated, err := svc.UpdateMugVariant(c.Request.Context(), &existing)
		if err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to update variant"})
			return
		}
		c.JSON(http.StatusOK, toArticleMugVariantResponse(&updated))
	})

	grp.DELETE("/variants/:variantId/image", func(c *gin.Context) {
		id, err := strconv.Atoi(c.Param("variantId"))
		if err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Invalid variant id"})
			return
		}
		existing, err := svc.GetMugVariant(c.Request.Context(), id)
		if err != nil {
			if errorsIsNotFound(err) {
				c.JSON(http.StatusNotFound, gin.H{"detail": "Variant not found"})
				return
			}
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to fetch variant"})
			return
		}
		// remove file best-effort
		if existing.ExampleImageFilename != nil && *existing.ExampleImageFilename != "" {
			if loc, err := img.NewStorageLocations(); err == nil {
				_ = os.Remove(filepath.Join(loc.MugVariantExample(), filepath.Base(*existing.ExampleImageFilename)))
			}
		}
		existing.ExampleImageFilename = nil
		updated, err := svc.UpdateMugVariant(c.Request.Context(), &existing)
		if err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to update variant"})
			return
		}
		c.JSON(http.StatusOK, toArticleMugVariantResponse(&updated))
	})

	// Variants catalog (summary of all mugs with their variants)
	grp.GET("/variants-catalog", func(c *gin.Context) {
		exclude := c.Query("excludeMugId")
		var excludeID *int
		if strings.TrimSpace(exclude) != "" {
			if n, err := strconv.Atoi(exclude); err == nil {
				excludeID = &n
			}
		}
		mugs, err := svc.ListMugArticles(c.Request.Context(), false, excludeID)
		if err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to fetch mugs"})
			return
		}
		out := make([]mugWithVariantsSummaryResponse, 0, len(mugs))
		for i := range mugs {
			m := mugs[i]
			variants, err := svc.ListMugVariants(c.Request.Context(), m.ID, false)
			if err != nil {
				c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to fetch variants"})
				return
			}
			items := make([]mugVariantSummaryResponse, 0, len(variants))
			for j := range variants {
				v := variants[j]
				items = append(items, mugVariantSummaryResponse{
					ID:                   v.ID,
					Name:                 v.Name,
					InsideColorCode:      v.InsideColorCode,
					OutsideColorCode:     v.OutsideColorCode,
					ArticleVariantNumber: v.ArticleVariantNumber,
					ExampleImageURL:      strPtrOrNil(publicMugVariantExampleURL(v.ExampleImageFilename)),
					Active:               v.Active,
				})
			}
			out = append(out, mugWithVariantsSummaryResponse{ID: m.ID, Name: m.Name, SupplierArticleName: m.SupplierArticleName, Variants: items})
		}
		c.JSON(http.StatusOK, out)
	})

	// Copy variants to target mug
	grp.POST("/:articleId/copy-variants", func(c *gin.Context) {
		mid, err := strconv.Atoi(c.Param("articleId"))
		if err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Invalid article id"})
			return
		}
		target, err := svc.GetArticle(c.Request.Context(), mid)
		if err != nil {
			if errorsIsNotFound(err) {
				c.JSON(http.StatusNotFound, gin.H{"detail": "Target mug not found"})
				return
			}
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to fetch mug"})
			return
		}
		if target.ArticleType != ArticleTypeMug {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Target must be a mug"})
			return
		}
		var req mugVariantCopyRequest
		if err := c.ShouldBindJSON(&req); err != nil || len(req.VariantIDs) == 0 {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Invalid payload"})
			return
		}
		created := make([]articleMugVariantResponse, 0, len(req.VariantIDs))
		for _, id := range req.VariantIDs {
			variant, err := svc.GetMugVariant(c.Request.Context(), id)
			if err != nil {
				if errorsIsNotFound(err) {
					continue
				}
				c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to fetch source variant"})
				return
			}
			copyVariant := MugVariant{
				ArticleID:            target.ID,
				InsideColorCode:      variant.InsideColorCode,
				OutsideColorCode:     variant.OutsideColorCode,
				Name:                 variant.Name,
				ArticleVariantNumber: variant.ArticleVariantNumber,
				ExampleImageFilename: variant.ExampleImageFilename,
				IsDefault:            variant.IsDefault,
				Active:               variant.Active,
			}
			copyCreated, err := svc.CreateMugVariant(c.Request.Context(), &copyVariant)
			if err == nil {
				created = append(created, toArticleMugVariantResponse(&copyCreated))
			}
		}
		c.JSON(http.StatusCreated, created)
	})
}
