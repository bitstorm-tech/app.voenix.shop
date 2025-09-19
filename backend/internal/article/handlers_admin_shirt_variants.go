package article

import (
	"net/http"
	"strconv"
	"strings"

	"github.com/gin-gonic/gin"
)

type shirtVariantCreate struct {
	Color                string  `json:"color"`
	Size                 string  `json:"size"`
	ExampleImageFilename *string `json:"exampleImageFilename"`
}

func registerAdminShirtVariantRoutes(r *gin.Engine, adminMiddleware gin.HandlerFunc, svc *Service) {
	grp := r.Group("/api/admin/articles/shirts")
	grp.Use(adminMiddleware)

	grp.POST("/:articleId/variants", func(c *gin.Context) {
		aid, err := strconv.Atoi(c.Param("articleId"))
		if err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Invalid article id"})
			return
		}
		var payload shirtVariantCreate
		if err := c.ShouldBindJSON(&payload); err != nil || strings.TrimSpace(payload.Color) == "" || strings.TrimSpace(payload.Size) == "" {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Invalid payload"})
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
		if articleDomain.ArticleType != ArticleTypeShirt {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Variants allowed only for SHIRT articles"})
			return
		}
		variant := ShirtVariant{ArticleID: articleDomain.ID, Color: payload.Color, Size: payload.Size, ExampleImageFilename: payload.ExampleImageFilename}
		created, err := svc.CreateShirtVariant(c.Request.Context(), &variant)
		if err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to create variant"})
			return
		}
		c.JSON(http.StatusCreated, toArticleShirtVariantResponse(&created))
	})

	grp.PUT("/variants/:variantId", func(c *gin.Context) {
		id, err := strconv.Atoi(c.Param("variantId"))
		if err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Invalid variant id"})
			return
		}
		existing, err := svc.GetShirtVariant(c.Request.Context(), id)
		if err != nil {
			if errorsIsNotFound(err) {
				c.JSON(http.StatusNotFound, gin.H{"detail": "Variant not found"})
				return
			}
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to fetch variant"})
			return
		}
		var payload shirtVariantCreate
		if err := c.ShouldBindJSON(&payload); err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Invalid payload"})
			return
		}
		existing.Color = payload.Color
		existing.Size = payload.Size
		existing.ExampleImageFilename = payload.ExampleImageFilename
		updated, err := svc.UpdateShirtVariant(c.Request.Context(), &existing)
		if err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to update variant"})
			return
		}
		c.JSON(http.StatusOK, toArticleShirtVariantResponse(&updated))
	})

	grp.DELETE("/variants/:variantId", func(c *gin.Context) {
		id, err := strconv.Atoi(c.Param("variantId"))
		if err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Invalid variant id"})
			return
		}
		if err := svc.DeleteShirtVariant(c.Request.Context(), id); err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to delete variant"})
			return
		}
		c.Status(http.StatusNoContent)
	})
}
