package prompt

import (
	"net/http"
	"strings"

	"github.com/gin-gonic/gin"
	"gorm.io/gorm"

	"voenix/backend-go/internal/auth"
)

type categoryCreate struct {
	Name string `json:"name"`
}

type categoryUpdate struct {
	Name *string `json:"name"`
}

func registerAdminCategoryRoutes(r *gin.Engine, db *gorm.DB) {
	grp := r.Group("/api/admin/prompts/categories")
	grp.Use(auth.RequireAdmin(db))

	grp.GET("/", func(c *gin.Context) {
		var rows []PromptCategory
		if err := db.Order("id desc").Find(&rows).Error; err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to fetch categories"})
			return
		}
		out := make([]PromptCategoryRead, 0, len(rows))
		for i := range rows {
			pc := rows[i]
			promptsCount := countPromptsByCategory(db, pc.ID)
			subcatsCount := countSubCategoriesByCategory(db, pc.ID)
			out = append(out, PromptCategoryRead{
				ID: pc.ID, Name: pc.Name,
				PromptsCount: promptsCount, SubcategoriesCount: subcatsCount,
				CreatedAt: timePtr(pc.CreatedAt), UpdatedAt: timePtr(pc.UpdatedAt),
			})
		}
		c.JSON(http.StatusOK, out)
	})

	grp.POST("/", func(c *gin.Context) {
		var payload categoryCreate
		if err := c.ShouldBindJSON(&payload); err != nil || strings.TrimSpace(payload.Name) == "" {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Invalid payload"})
			return
		}
		row := PromptCategory{Name: payload.Name}
		if err := db.Create(&row).Error; err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to create category"})
			return
		}
		c.JSON(http.StatusCreated, PromptCategoryRead{
			ID: row.ID, Name: row.Name,
			PromptsCount: 0, SubcategoriesCount: 0,
			CreatedAt: timePtr(row.CreatedAt), UpdatedAt: timePtr(row.UpdatedAt),
		})
	})

	grp.PUT("/:id", func(c *gin.Context) {
		var existing PromptCategory
		if err := db.First(&existing, "id = ?", c.Param("id")).Error; err != nil {
			if errorsIsNotFound(err) {
				c.JSON(http.StatusNotFound, gin.H{"detail": "PromptCategory not found"})
				return
			}
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to fetch category"})
			return
		}
		var payload categoryUpdate
		if err := c.ShouldBindJSON(&payload); err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Invalid payload"})
			return
		}
		if payload.Name != nil {
			existing.Name = *payload.Name
		}
		if err := db.Save(&existing).Error; err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to update category"})
			return
		}
		c.JSON(http.StatusOK, PromptCategoryRead{
			ID: existing.ID, Name: existing.Name,
			PromptsCount:       countPromptsByCategory(db, existing.ID),
			SubcategoriesCount: countSubCategoriesByCategory(db, existing.ID),
			CreatedAt:          timePtr(existing.CreatedAt), UpdatedAt: timePtr(existing.UpdatedAt),
		})
	})

	grp.DELETE("/:id", func(c *gin.Context) {
		// Ensure exists for consistent semantics
		var existing PromptCategory
		if err := db.First(&existing, "id = ?", c.Param("id")).Error; err != nil {
			if errorsIsNotFound(err) {
				c.Status(http.StatusNoContent)
				return
			}
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to fetch category"})
			return
		}
		if err := db.Delete(&PromptCategory{}, existing.ID).Error; err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to delete category"})
			return
		}
		c.Status(http.StatusNoContent)
	})
}
