package article

import (
	"net/http"
	"strings"

	"github.com/gin-gonic/gin"
	"gorm.io/gorm"

	"voenix/backend/internal/auth"
)

type shirtVariantCreate struct {
	Color                string  `json:"color"`
	Size                 string  `json:"size"`
	ExampleImageFilename *string `json:"exampleImageFilename"`
}

func registerAdminShirtVariantRoutes(r *gin.Engine, db *gorm.DB) {
	grp := r.Group("/api/admin/articles/shirts")
	grp.Use(auth.RequireAdmin(db))

	grp.POST("/:articleId/variants", func(c *gin.Context) {
		var payload shirtVariantCreate
		if err := c.ShouldBindJSON(&payload); err != nil || strings.TrimSpace(payload.Color) == "" || strings.TrimSpace(payload.Size) == "" {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Invalid payload"})
			return
		}
		var a Article
		if err := db.First(&a, "id = ?", c.Param("articleId")).Error; err != nil {
			if errorsIsNotFound(err) {
				c.JSON(http.StatusNotFound, gin.H{"detail": "Article not found"})
				return
			}
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to fetch article"})
			return
		}
		v := ShirtVariant{ArticleID: a.ID, Color: payload.Color, Size: payload.Size, ExampleImageFilename: payload.ExampleImageFilename}
		if err := db.Create(&v).Error; err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to create variant"})
			return
		}
		c.JSON(http.StatusCreated, toArticleShirtVariantResponse(&v))
	})

	grp.PUT("/variants/:variantId", func(c *gin.Context) {
		var existing ShirtVariant
		if err := db.First(&existing, "id = ?", c.Param("variantId")).Error; err != nil {
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
		if err := db.Save(&existing).Error; err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to update variant"})
			return
		}
		c.JSON(http.StatusOK, toArticleShirtVariantResponse(&existing))
	})

	grp.DELETE("/variants/:variantId", func(c *gin.Context) {
		if err := db.Delete(&ShirtVariant{}, "id = ?", c.Param("variantId")).Error; err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to delete variant"})
			return
		}
		c.Status(http.StatusNoContent)
	})
}
