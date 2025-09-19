package article

import (
	"net/http"
	"strconv"
	"strings"
	"time"

	"github.com/gin-gonic/gin"
)

type articleCategoryCreate struct {
	Name        string  `json:"name"`
	Description *string `json:"description"`
}

type articleCategoryUpdate struct {
	Name        *string `json:"name"`
	Description *string `json:"description"`
}

func registerAdminCategoryRoutes(r *gin.Engine, adminMiddleware gin.HandlerFunc, svc *Service) {
	grp := r.Group("/api/admin/articles/categories")
	grp.Use(adminMiddleware)

	grp.GET("", func(c *gin.Context) {
		summaries, err := svc.ListCategories(c.Request.Context())
		if err != nil {
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
		out := make([]Row, 0, len(summaries))
		for _, summary := range summaries {
			cat := summary.Category
			out = append(out, Row{
				ID:            cat.ID,
				Name:          cat.Name,
				Description:   cat.Description,
				CreatedAt:     timePtr(cat.CreatedAt),
				UpdatedAt:     timePtr(cat.UpdatedAt),
				ArticlesCount: summary.ArticlesCount,
			})
		}
		c.JSON(http.StatusOK, out)
	})

	grp.GET("/:id", func(c *gin.Context) {
		id, err := strconv.Atoi(c.Param("id"))
		if err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Invalid category id"})
			return
		}
		row, err := svc.GetCategory(c.Request.Context(), id)
		if err != nil {
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
		row, err := svc.CreateCategory(c.Request.Context(), strings.TrimSpace(payload.Name), payload.Description)
		if err != nil {
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
		id, err := strconv.Atoi(c.Param("id"))
		if err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Invalid category id"})
			return
		}
		var payload articleCategoryUpdate
		if err := c.ShouldBindJSON(&payload); err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Invalid payload"})
			return
		}
		var namePtr *string
		if payload.Name != nil {
			t := strings.TrimSpace(*payload.Name)
			namePtr = &t
		}
		row, err := svc.UpdateCategory(c.Request.Context(), id, namePtr, payload.Description)
		if err != nil {
			if errorsIsNotFound(err) {
				c.JSON(http.StatusNotFound, gin.H{"detail": "ArticleCategory not found"})
				return
			}
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to update category"})
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

	grp.DELETE("/:id", func(c *gin.Context) {
		id, err := strconv.Atoi(c.Param("id"))
		if err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Invalid category id"})
			return
		}
		if _, err := svc.GetCategory(c.Request.Context(), id); err != nil {
			if errorsIsNotFound(err) {
				c.Status(http.StatusNoContent)
				return
			}
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to fetch category"})
			return
		}
		if err := svc.DeleteCategory(c.Request.Context(), id); err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to delete category"})
			return
		}
		c.Status(http.StatusNoContent)
	})
}
