package article

import (
	"net/http"
	"strings"
	"time"

	"github.com/gin-gonic/gin"
	"gorm.io/gorm"

	"voenix/backend-go/internal/auth"
)

type articleCategoryCreate struct {
	Name        string  `json:"name"`
	Description *string `json:"description"`
}

type articleCategoryUpdate struct {
	Name        *string `json:"name"`
	Description *string `json:"description"`
}

func registerAdminCategoryRoutes(r *gin.Engine, db *gorm.DB) {
	grp := r.Group("/api/admin/articles/categories")
	grp.Use(auth.RequireAdmin(db))

	grp.GET("", func(c *gin.Context) {
		var rows []ArticleCategory
		if err := db.Order("id desc").Find(&rows).Error; err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to fetch categories"})
			return
		}
		// Include articles_count in payload as used by UI
		type Row struct {
			ID            int        `json:"id"`
			Name          string     `json:"name"`
			Description   *string    `json:"description"`
			CreatedAt     *time.Time `json:"createdAt"`
			UpdatedAt     *time.Time `json:"updatedAt"`
			ArticlesCount int        `json:"articles_count"`
		}
		out := make([]Row, 0, len(rows))
		for i := range rows {
			cat := rows[i]
			out = append(out, Row{
				ID:            cat.ID,
				Name:          cat.Name,
				Description:   cat.Description,
				CreatedAt:     timePtr(cat.CreatedAt),
				UpdatedAt:     timePtr(cat.UpdatedAt),
				ArticlesCount: countArticlesByCategory(db, cat.ID),
			})
		}
		c.JSON(http.StatusOK, out)
	})

	grp.GET("/:id", func(c *gin.Context) {
		var row ArticleCategory
		if err := db.First(&row, "id = ?", c.Param("id")).Error; err != nil {
			if errorsIsNotFound(err) {
				c.JSON(http.StatusNotFound, gin.H{"detail": "ArticleCategory not found"})
				return
			}
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to fetch category"})
			return
		}
		c.JSON(http.StatusOK, gin.H{
			"id":          row.ID,
			"name":        row.Name,
			"description": row.Description,
			"createdAt":   timePtr(row.CreatedAt),
			"updatedAt":   timePtr(row.UpdatedAt),
		})
	})

	grp.POST("", func(c *gin.Context) {
		var payload articleCategoryCreate
		if err := c.ShouldBindJSON(&payload); err != nil || strings.TrimSpace(payload.Name) == "" {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Invalid payload"})
			return
		}
		row := ArticleCategory{Name: payload.Name, Description: payload.Description}
		if err := db.Create(&row).Error; err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to create category"})
			return
		}
		c.JSON(http.StatusCreated, gin.H{
			"id":          row.ID,
			"name":        row.Name,
			"description": row.Description,
			"createdAt":   timePtr(row.CreatedAt),
			"updatedAt":   timePtr(row.UpdatedAt),
		})
	})

	grp.PUT("/:id", func(c *gin.Context) {
		var existing ArticleCategory
		if err := db.First(&existing, "id = ?", c.Param("id")).Error; err != nil {
			if errorsIsNotFound(err) {
				c.JSON(http.StatusNotFound, gin.H{"detail": "ArticleCategory not found"})
				return
			}
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to fetch category"})
			return
		}
		var payload articleCategoryUpdate
		if err := c.ShouldBindJSON(&payload); err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Invalid payload"})
			return
		}
		if payload.Name != nil {
			existing.Name = *payload.Name
		}
		if payload.Description != nil {
			existing.Description = payload.Description
		}
		if err := db.Save(&existing).Error; err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to update category"})
			return
		}
		c.JSON(http.StatusOK, gin.H{
			"id":          existing.ID,
			"name":        existing.Name,
			"description": existing.Description,
			"createdAt":   timePtr(existing.CreatedAt),
			"updatedAt":   timePtr(existing.UpdatedAt),
		})
	})

	grp.DELETE("/:id", func(c *gin.Context) {
		var existing ArticleCategory
		if err := db.First(&existing, "id = ?", c.Param("id")).Error; err != nil {
			if errorsIsNotFound(err) {
				c.Status(http.StatusNoContent)
				return
			}
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to fetch category"})
			return
		}
		if err := db.Delete(&ArticleCategory{}, existing.ID).Error; err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to delete category"})
			return
		}
		c.Status(http.StatusNoContent)
	})
}
