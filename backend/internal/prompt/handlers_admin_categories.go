package prompt

import (
	"errors"
	"net/http"
	"strconv"
	"strings"

	"github.com/gin-gonic/gin"
	"gorm.io/gorm"

	"voenix/backend/internal/auth"
)

type categoryCreate struct {
	Name string `json:"name"`
}

type categoryUpdate struct {
	Name *string `json:"name"`
}

func registerAdminCategoryRoutes(r *gin.Engine, db *gorm.DB, svc *Service) {
	grp := r.Group("/api/admin/prompts/categories")
	grp.Use(auth.RequireAdmin(db))

	grp.GET("/", func(c *gin.Context) {
		rows, err := svc.ListCategories(c.Request.Context())
		if err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to fetch categories"})
			return
		}
		c.JSON(http.StatusOK, rows)
	})

	grp.POST("/", func(c *gin.Context) {
		var payload categoryCreate
		if err := c.ShouldBindJSON(&payload); err != nil || strings.TrimSpace(payload.Name) == "" {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Invalid payload"})
			return
		}
		created, err := svc.CreateCategory(c.Request.Context(), payload.Name)
		if err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to create category"})
			return
		}
		c.JSON(http.StatusCreated, created)
	})

	grp.PUT("/:id", func(c *gin.Context) {
		id, _ := strconv.Atoi(c.Param("id"))
		var payload categoryUpdate
		if err := c.ShouldBindJSON(&payload); err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Invalid payload"})
			return
		}
		updated, err := svc.UpdateCategory(c.Request.Context(), id, payload.Name)
		if err != nil {
			if errors.Is(err, gorm.ErrRecordNotFound) {
				c.JSON(http.StatusNotFound, gin.H{"detail": "PromptCategory not found"})
				return
			}
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to update category"})
			return
		}
		c.JSON(http.StatusOK, updated)
	})

	grp.DELETE("/:id", func(c *gin.Context) {
		id, _ := strconv.Atoi(c.Param("id"))
		if err := svc.DeleteCategory(c.Request.Context(), id); err != nil {
			if errors.Is(err, gorm.ErrRecordNotFound) {
				c.Status(http.StatusNoContent)
				return
			}
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to delete category"})
			return
		}
		c.Status(http.StatusNoContent)
	})
}
