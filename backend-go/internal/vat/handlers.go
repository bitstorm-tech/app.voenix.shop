package vat

import (
	"errors"
	"net/http"
	"strings"

	"github.com/gin-gonic/gin"
	"gorm.io/gorm"

	"voenix/backend-go/internal/auth"
)

// RegisterRoutes mounts VAT admin routes under /api/admin/vat, guarded by RequireAdmin.
func RegisterRoutes(r *gin.Engine, db *gorm.DB) {
	grp := r.Group("/api/admin/vat")
	grp.Use(auth.RequireAdmin(db))

	// GET /api/admin/vat/ -> list all
	grp.GET("", func(c *gin.Context) {
		var rows []ValueAddedTax
		if err := db.Find(&rows).Error; err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to fetch VATs"})
			return
		}
		c.JSON(http.StatusOK, rows)
	})

	// GET /api/admin/vat/:id -> single by id
	grp.GET("/:id", func(c *gin.Context) {
		var row ValueAddedTax
		if err := db.First(&row, "id = ?", c.Param("id")).Error; err != nil {
			if errors.Is(err, gorm.ErrRecordNotFound) {
				c.JSON(http.StatusNotFound, gin.H{"detail": "VAT not found"})
				return
			}
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to fetch VAT"})
			return
		}
		c.JSON(http.StatusOK, row)
	})

	// POST /api/admin/vat/ -> create
	grp.POST("", func(c *gin.Context) {
		var payload ValueAddedTaxCreate
		if err := c.ShouldBindJSON(&payload); err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Invalid payload"})
			return
		}

		var created ValueAddedTax
		if err := db.Transaction(func(tx *gorm.DB) error {
			if payload.IsDefault {
				if err := tx.Model(&ValueAddedTax{}).Where("is_default = ?", true).Update("is_default", false).Error; err != nil {
					return err
				}
			}
			created = ValueAddedTax{
				Name:        payload.Name,
				Percent:     payload.Percent,
				Description: payload.Description,
				IsDefault:   payload.IsDefault,
			}
			if err := tx.Create(&created).Error; err != nil {
				return err
			}
			return nil
		}); err != nil {
			// Map unique constraint violations to 409
			if isUniqueViolation(err) {
				c.JSON(http.StatusConflict, gin.H{"detail": "A VAT with this name already exists."})
				return
			}
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to create VAT"})
			return
		}
		c.JSON(http.StatusCreated, created)
	})

	// PUT /api/admin/vat/:id -> update
	grp.PUT("/:id", func(c *gin.Context) {
		id := c.Param("id")
		var payload ValueAddedTaxUpdate
		if err := c.ShouldBindJSON(&payload); err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Invalid payload"})
			return
		}

		// Ensure exists
		var existing ValueAddedTax
		if err := db.First(&existing, "id = ?", id).Error; err != nil {
			if errors.Is(err, gorm.ErrRecordNotFound) {
				c.JSON(http.StatusNotFound, gin.H{"detail": "VAT not found"})
				return
			}
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to fetch VAT"})
			return
		}

		if err := db.Transaction(func(tx *gorm.DB) error {
			if payload.IsDefault {
				if err := tx.Model(&ValueAddedTax{}).Where("is_default = ?", true).Update("is_default", false).Error; err != nil {
					return err
				}
			}
			existing.Name = payload.Name
			existing.Percent = payload.Percent
			existing.Description = payload.Description
			existing.IsDefault = payload.IsDefault
			if err := tx.Save(&existing).Error; err != nil {
				return err
			}
			return nil
		}); err != nil {
			if isUniqueViolation(err) {
				c.JSON(http.StatusConflict, gin.H{"detail": "A VAT with this name already exists."})
				return
			}
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to update VAT"})
			return
		}
		c.JSON(http.StatusOK, existing)
	})

	// DELETE /api/admin/vat/:id -> delete
	grp.DELETE("/:id", func(c *gin.Context) {
		id := c.Param("id")
		// Use Unscoped() only if we want hard delete; default is hard delete anyway when no DeletedAt
		if err := db.Delete(&ValueAddedTax{}, "id = ?", id).Error; err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to delete VAT"})
			return
		}
		c.Status(http.StatusNoContent)
	})
}

func isUniqueViolation(err error) bool {
	if err == nil {
		return false
	}
	// SQLite and Postgres error messages contain these substrings for unique violations
	s := strings.ToLower(err.Error())
	return strings.Contains(s, "unique constraint") || strings.Contains(s, "duplicate key value") || strings.Contains(s, "unique failed")
}
