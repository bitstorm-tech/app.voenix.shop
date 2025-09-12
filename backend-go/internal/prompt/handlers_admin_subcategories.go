package prompt

import (
	"errors"
	"net/http"
	"strconv"
	"strings"

	"github.com/gin-gonic/gin"
	"gorm.io/gorm"

	"voenix/backend-go/internal/auth"
)

type subcatCreate struct {
	PromptCategoryID int     `json:"promptCategoryId"`
	Name             string  `json:"name"`
	Description      *string `json:"description"`
}

type subcatUpdate struct {
	PromptCategoryID *int    `json:"promptCategoryId"`
	Name             *string `json:"name"`
	Description      *string `json:"description"`
}

func registerAdminSubCategoryRoutes(r *gin.Engine, db *gorm.DB, svc *service) {
	grp := r.Group("/api/admin/prompts/subcategories")
	grp.Use(auth.RequireAdmin(db))

	grp.GET("", func(c *gin.Context) {
		rows, err := svc.listSubCategories(c.Request.Context())
		if err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to fetch subcategories"})
			return
		}
		c.JSON(http.StatusOK, rows)
	})

	grp.GET("/category/:categoryId", func(c *gin.Context) {
		cid, _ := strconv.Atoi(c.Param("categoryId"))
		rows, err := svc.listSubCategoriesByCategory(c.Request.Context(), cid)
		if err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to fetch subcategories"})
			return
		}
		c.JSON(http.StatusOK, rows)
	})

	grp.POST("", func(c *gin.Context) {
		var payload subcatCreate
		if err := c.ShouldBindJSON(&payload); err != nil || payload.PromptCategoryID <= 0 || strings.TrimSpace(payload.Name) == "" {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Invalid payload"})
			return
		}
		created, err := svc.createSubCategory(c.Request.Context(), payload)
		if err != nil {
			if errors.Is(err, gorm.ErrRecordNotFound) {
				c.JSON(http.StatusNotFound, gin.H{"detail": "PromptCategory not found"})
				return
			}
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to create subcategory"})
			return
		}
		c.JSON(http.StatusCreated, created)
	})

	grp.PUT("/:id", func(c *gin.Context) {
		id, _ := strconv.Atoi(c.Param("id"))
		var payload subcatUpdate
		if err := c.ShouldBindJSON(&payload); err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Invalid payload"})
			return
		}
		updated, err := svc.updateSubCategory(c.Request.Context(), id, payload)
		if err != nil {
			if errors.Is(err, gorm.ErrRecordNotFound) {
				c.JSON(http.StatusNotFound, gin.H{"detail": "PromptCategory or PromptSubCategory not found"})
				return
			}
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to update subcategory"})
			return
		}
		c.JSON(http.StatusOK, updated)
	})

	grp.DELETE("/:id", func(c *gin.Context) {
		id, _ := strconv.Atoi(c.Param("id"))
		if err := svc.deleteSubCategory(c.Request.Context(), id); err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to delete subcategory"})
			return
		}
		c.Status(http.StatusNoContent)
	})
}
