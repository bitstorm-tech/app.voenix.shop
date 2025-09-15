package supplier

import (
	"errors"
	"net/http"
	"strconv"

	"github.com/gin-gonic/gin"
	"gorm.io/gorm"

	"voenix/backend/internal/auth"
)

// RegisterRoutes mounts Supplier admin routes under /api/admin/suppliers.
func RegisterRoutes(r *gin.Engine, db *gorm.DB) {
	svc := NewService(db)
	grp := r.Group("/api/admin/suppliers")
	grp.Use(auth.RequireAdmin(db))

	// GET /api/admin/suppliers -> list all
	grp.GET("", func(c *gin.Context) {
		rows, err := svc.ListSuppliers(c.Request.Context())
		if err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to fetch suppliers"})
			return
		}
		c.JSON(http.StatusOK, rows)
	})

	// GET /api/admin/suppliers/:id -> single by id
	grp.GET("/:id", func(c *gin.Context) {
		id, err := strconv.Atoi(c.Param("id"))
		if err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Invalid id"})
			return
		}
		row, err := svc.GetSupplierByID(c.Request.Context(), id)
		if err != nil {
			if errors.Is(err, gorm.ErrRecordNotFound) {
				c.JSON(http.StatusNotFound, gin.H{"detail": "Supplier not found"})
				return
			}
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to fetch supplier"})
			return
		}
		c.JSON(http.StatusOK, row)
	})

	// POST /api/admin/suppliers -> create
	grp.POST("", func(c *gin.Context) {
		var payload SupplierCreate
		if err := c.ShouldBindJSON(&payload); err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Invalid payload"})
			return
		}
		created, err := svc.CreateSupplier(c.Request.Context(), payload)
		if err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to create supplier"})
			return
		}
		c.JSON(http.StatusCreated, created)
	})

	// PUT /api/admin/suppliers/:id -> update
	grp.PUT("/:id", func(c *gin.Context) {
		id, err := strconv.Atoi(c.Param("id"))
		if err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Invalid id"})
			return
		}
		var payload SupplierUpdate
		if err := c.ShouldBindJSON(&payload); err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Invalid payload"})
			return
		}
		updated, err := svc.UpdateSupplier(c.Request.Context(), id, payload)
		if err != nil {
			if errors.Is(err, gorm.ErrRecordNotFound) {
				c.JSON(http.StatusNotFound, gin.H{"detail": "Supplier not found"})
				return
			}
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to update supplier"})
			return
		}
		c.JSON(http.StatusOK, updated)
	})

	// DELETE /api/admin/suppliers/:id -> delete
	grp.DELETE("/:id", func(c *gin.Context) {
		id, err := strconv.Atoi(c.Param("id"))
		if err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Invalid id"})
			return
		}
		if err := svc.DeleteSupplier(c.Request.Context(), id); err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to delete supplier"})
			return
		}
		c.Status(http.StatusNoContent)
	})
}
