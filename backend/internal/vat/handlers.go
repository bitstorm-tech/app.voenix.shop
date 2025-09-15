package vat

import (
	"errors"
	"net/http"
	"strconv"

	"github.com/gin-gonic/gin"
	"gorm.io/gorm"

	"voenix/backend/internal/auth"
)

// RegisterRoutes mounts VAT admin routes under /api/admin/vat, guarded by RequireAdmin.
func RegisterRoutes(r *gin.Engine, db *gorm.DB) {
	group := r.Group("/api/admin/vat")
	group.Use(auth.RequireAdmin(db))
	vatService := NewVATService(db)

	// GET /api/admin/vat/ -> list all
	group.GET("", func(c *gin.Context) {
		rows, err := vatService.List(c.Request.Context())
		if err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to fetch VATs"})
			return
		}
		c.JSON(http.StatusOK, rows)
	})

	// GET /api/admin/vat/:id -> single by id
	group.GET("/:id", func(c *gin.Context) {
		id, err := strconv.Atoi(c.Param("id"))
		if err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Invalid id"})
			return
		}
		row, err := vatService.Get(c.Request.Context(), id)
		if err != nil {
			if errors.Is(err, ErrNotFound) {
				c.JSON(http.StatusNotFound, gin.H{"detail": "VAT not found"})
				return
			}
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to fetch VAT"})
			return
		}
		c.JSON(http.StatusOK, row)
	})

	// POST /api/admin/vat/ -> create
	group.POST("", func(c *gin.Context) {
		var payload ValueAddedTaxCreate
		if err := c.ShouldBindJSON(&payload); err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Invalid payload"})
			return
		}
		created, err := vatService.Create(c.Request.Context(), payload)
		if err != nil {
			if errors.Is(err, ErrConflict) {
				c.JSON(http.StatusConflict, gin.H{"detail": "A VAT with this name already exists."})
				return
			}
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to create VAT"})
			return
		}
		c.JSON(http.StatusCreated, created)
	})

	// PUT /api/admin/vat/:id -> update
	group.PUT("/:id", func(c *gin.Context) {
		id, err := strconv.Atoi(c.Param("id"))
		if err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Invalid id"})
			return
		}
		var payload ValueAddedTaxUpdate
		if err := c.ShouldBindJSON(&payload); err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Invalid payload"})
			return
		}
		updated, err := vatService.Update(c.Request.Context(), id, payload)
		if err != nil {
			if errors.Is(err, ErrNotFound) {
				c.JSON(http.StatusNotFound, gin.H{"detail": "VAT not found"})
				return
			}
			if errors.Is(err, ErrConflict) {
				c.JSON(http.StatusConflict, gin.H{"detail": "A VAT with this name already exists."})
				return
			}
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to update VAT"})
			return
		}
		c.JSON(http.StatusOK, updated)
	})

	// DELETE /api/admin/vat/:id -> delete
	group.DELETE("/:id", func(c *gin.Context) {
		id, err := strconv.Atoi(c.Param("id"))
		if err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Invalid id"})
			return
		}
		if err := vatService.Delete(c.Request.Context(), id); err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to delete VAT"})
			return
		}
		c.Status(http.StatusNoContent)
	})
}
