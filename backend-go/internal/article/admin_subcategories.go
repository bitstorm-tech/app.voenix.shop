package article

import (
	"net/http"
	"strings"
	"time"

	"github.com/gin-gonic/gin"
	"gorm.io/gorm"

	"voenix/backend-go/internal/auth"
)

type articleSubCategoryCreate struct {
	ArticleCategoryID int     `json:"articleCategoryId"`
	Name              string  `json:"name"`
	Description       *string `json:"description"`
}

type articleSubCategoryUpdate struct {
	ArticleCategoryID *int    `json:"articleCategoryId"`
	Name              *string `json:"name"`
	Description       *string `json:"description"`
}

func registerAdminSubCategoryRoutes(r *gin.Engine, db *gorm.DB) {
	grp := r.Group("/api/admin/articles/subcategories")
	grp.Use(auth.RequireAdmin(db))

	grp.GET("", func(c *gin.Context) {
		var rows []ArticleSubCategory
		if err := db.Order("id desc").Find(&rows).Error; err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to fetch subcategories"})
			return
		}
		type Row struct {
			ID                int        `json:"id"`
			ArticleCategoryID int        `json:"articleCategoryId"`
			Name              string     `json:"name"`
			Description       *string    `json:"description"`
			ArticlesCount     int        `json:"articlesCount"`
			CreatedAt         *time.Time `json:"createdAt"`
			UpdatedAt         *time.Time `json:"updatedAt"`
		}
		out := make([]Row, 0, len(rows))
		for i := range rows {
			sc := rows[i]
			out = append(out, Row{
				ID:                sc.ID,
				ArticleCategoryID: sc.ArticleCategoryID,
				Name:              sc.Name,
				Description:       sc.Description,
				ArticlesCount:     countArticlesBySubcategory(db, sc.ID),
				CreatedAt:         timePtr(sc.CreatedAt),
				UpdatedAt:         timePtr(sc.UpdatedAt),
			})
		}
		c.JSON(http.StatusOK, out)
	})

	grp.GET("/:id", func(c *gin.Context) {
		var row ArticleSubCategory
		if err := db.First(&row, "id = ?", c.Param("id")).Error; err != nil {
			if errorsIsNotFound(err) {
				c.JSON(http.StatusNotFound, gin.H{"detail": "ArticleSubCategory not found"})
				return
			}
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to fetch subcategory"})
			return
		}
		c.JSON(http.StatusOK, gin.H{
			"id":                row.ID,
			"articleCategoryId": row.ArticleCategoryID,
			"name":              row.Name,
			"description":       row.Description,
			"createdAt":         timePtr(row.CreatedAt),
			"updatedAt":         timePtr(row.UpdatedAt),
		})
	})

	grp.GET("/category/:categoryId", func(c *gin.Context) {
		var rows []ArticleSubCategory
		if err := db.Where("article_category_id = ?", c.Param("categoryId")).Order("id desc").Find(&rows).Error; err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to fetch subcategories"})
			return
		}
		out := make([]gin.H, 0, len(rows))
		for i := range rows {
			sc := rows[i]
			out = append(out, gin.H{
				"id":                sc.ID,
				"articleCategoryId": sc.ArticleCategoryID,
				"name":              sc.Name,
				"description":       sc.Description,
				"articlesCount":     countArticlesBySubcategory(db, sc.ID),
				"createdAt":         timePtr(sc.CreatedAt),
				"updatedAt":         timePtr(sc.UpdatedAt),
			})
		}
		c.JSON(http.StatusOK, out)
	})

	grp.POST("", func(c *gin.Context) {
		var payload articleSubCategoryCreate
		if err := c.ShouldBindJSON(&payload); err != nil || payload.ArticleCategoryID <= 0 || strings.TrimSpace(payload.Name) == "" {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Invalid payload"})
			return
		}
		// Ensure category exists
		var cat ArticleCategory
		if err := db.First(&cat, "id = ?", payload.ArticleCategoryID).Error; err != nil {
			if errorsIsNotFound(err) {
				c.JSON(http.StatusBadRequest, gin.H{"detail": "Category not found"})
				return
			}
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to validate category"})
			return
		}
		row := ArticleSubCategory{ArticleCategoryID: payload.ArticleCategoryID, Name: payload.Name, Description: payload.Description}
		if err := db.Create(&row).Error; err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to create subcategory"})
			return
		}
		c.JSON(http.StatusCreated, gin.H{
			"id":                row.ID,
			"articleCategoryId": row.ArticleCategoryID,
			"name":              row.Name,
			"description":       row.Description,
			"createdAt":         timePtr(row.CreatedAt),
			"updatedAt":         timePtr(row.UpdatedAt),
		})
	})

	grp.PUT("/:id", func(c *gin.Context) {
		var existing ArticleSubCategory
		if err := db.First(&existing, "id = ?", c.Param("id")).Error; err != nil {
			if errorsIsNotFound(err) {
				c.JSON(http.StatusNotFound, gin.H{"detail": "ArticleSubCategory not found"})
				return
			}
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to fetch subcategory"})
			return
		}
		var payload articleSubCategoryUpdate
		if err := c.ShouldBindJSON(&payload); err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Invalid payload"})
			return
		}
		if payload.ArticleCategoryID != nil {
			existing.ArticleCategoryID = *payload.ArticleCategoryID
		}
		if payload.Name != nil {
			existing.Name = strings.TrimSpace(*payload.Name)
		}
		if payload.Description != nil {
			existing.Description = payload.Description
		}
		if err := db.Save(&existing).Error; err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to update subcategory"})
			return
		}
		c.JSON(http.StatusOK, gin.H{
			"id":                existing.ID,
			"articleCategoryId": existing.ArticleCategoryID,
			"name":              existing.Name,
			"description":       existing.Description,
			"createdAt":         timePtr(existing.CreatedAt),
			"updatedAt":         timePtr(existing.UpdatedAt),
		})
	})

	grp.DELETE("/:id", func(c *gin.Context) {
		var existing ArticleSubCategory
		if err := db.First(&existing, "id = ?", c.Param("id")).Error; err != nil {
			if errorsIsNotFound(err) {
				c.Status(http.StatusNoContent)
				return
			}
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to fetch subcategory"})
			return
		}
		if err := db.Delete(&ArticleSubCategory{}, existing.ID).Error; err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to delete subcategory"})
			return
		}
		c.Status(http.StatusNoContent)
	})
}
