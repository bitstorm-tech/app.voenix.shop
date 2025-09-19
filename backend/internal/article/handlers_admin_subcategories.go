package article

import (
	"net/http"
	"strconv"
	"strings"
	"time"

	"github.com/gin-gonic/gin"
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

func registerAdminSubCategoryRoutes(r *gin.Engine, adminMiddleware gin.HandlerFunc, svc *Service) {
	grp := r.Group("/api/admin/articles/subcategories")
	grp.Use(adminMiddleware)

	grp.GET("", func(c *gin.Context) {
		summaries, err := svc.ListSubcategories(c.Request.Context())
		if err != nil {
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
		out := make([]Row, 0, len(summaries))
		for _, summary := range summaries {
			sc := summary.Subcategory
			out = append(out, Row{
				ID:                sc.ID,
				ArticleCategoryID: sc.ArticleCategoryID,
				Name:              sc.Name,
				Description:       sc.Description,
				ArticlesCount:     summary.ArticlesCount,
				CreatedAt:         timePtr(sc.CreatedAt),
				UpdatedAt:         timePtr(sc.UpdatedAt),
			})
		}
		c.JSON(http.StatusOK, out)
	})

	grp.GET("/:id", func(c *gin.Context) {
		id, err := strconv.Atoi(c.Param("id"))
		if err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Invalid subcategory id"})
			return
		}
		row, err := svc.GetSubcategory(c.Request.Context(), id)
		if err != nil {
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
		categoryID, err := strconv.Atoi(c.Param("categoryId"))
		if err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Invalid category id"})
			return
		}
		summaries, err := svc.ListSubcategoriesByCategory(c.Request.Context(), categoryID)
		if err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to fetch subcategories"})
			return
		}
		out := make([]gin.H, 0, len(summaries))
		for _, summary := range summaries {
			sc := summary.Subcategory
			out = append(out, gin.H{
				"id":                sc.ID,
				"articleCategoryId": sc.ArticleCategoryID,
				"name":              sc.Name,
				"description":       sc.Description,
				"articlesCount":     summary.ArticlesCount,
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
		exists, err := svc.CategoryExists(c.Request.Context(), payload.ArticleCategoryID)
		if err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to validate category"})
			return
		}
		if !exists {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Category not found"})
			return
		}
		name := strings.TrimSpace(payload.Name)
		row, err := svc.CreateSubcategory(c.Request.Context(), payload.ArticleCategoryID, name, payload.Description)
		if err != nil {
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
		id, err := strconv.Atoi(c.Param("id"))
		if err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Invalid subcategory id"})
			return
		}
		var payload articleSubCategoryUpdate
		if err := c.ShouldBindJSON(&payload); err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Invalid payload"})
			return
		}
		if payload.ArticleCategoryID != nil {
			exists, err := svc.CategoryExists(c.Request.Context(), *payload.ArticleCategoryID)
			if err != nil {
				c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to validate category"})
				return
			}
			if !exists {
				c.JSON(http.StatusBadRequest, gin.H{"detail": "Category not found"})
				return
			}
		}
		var namePtr *string
		if payload.Name != nil {
			t := strings.TrimSpace(*payload.Name)
			namePtr = &t
		}
		row, err := svc.UpdateSubcategory(c.Request.Context(), id, payload.ArticleCategoryID, namePtr, payload.Description)
		if err != nil {
			if errorsIsNotFound(err) {
				c.JSON(http.StatusNotFound, gin.H{"detail": "ArticleSubCategory not found"})
				return
			}
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to update subcategory"})
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

	grp.DELETE("/:id", func(c *gin.Context) {
		id, err := strconv.Atoi(c.Param("id"))
		if err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Invalid subcategory id"})
			return
		}
		if _, err := svc.GetSubcategory(c.Request.Context(), id); err != nil {
			if errorsIsNotFound(err) {
				c.Status(http.StatusNoContent)
				return
			}
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to fetch subcategory"})
			return
		}
		if err := svc.DeleteSubcategory(c.Request.Context(), id); err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to delete subcategory"})
			return
		}
		c.Status(http.StatusNoContent)
	})
}
