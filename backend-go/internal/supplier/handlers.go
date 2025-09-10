package supplier

import (
	"errors"
	"net/http"

	"github.com/gin-gonic/gin"
	"gorm.io/gorm"

	"voenix/backend-go/internal/auth"
)

// RegisterRoutes mounts Supplier admin routes under /api/admin/suppliers.
func RegisterRoutes(r *gin.Engine, db *gorm.DB) {
	grp := r.Group("/api/admin/suppliers")
	grp.Use(auth.RequireAdmin(db))

	// GET /api/admin/suppliers -> list all
	grp.GET("/", func(c *gin.Context) {
		var rows []Supplier
		if err := db.Preload("Country").Find(&rows).Error; err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to fetch suppliers"})
			return
		}
		c.JSON(http.StatusOK, rows)
	})

	// GET /api/admin/suppliers/:id -> single by id
	grp.GET("/:id", func(c *gin.Context) {
		var row Supplier
		if err := db.Preload("Country").First(&row, "id = ?", c.Param("id")).Error; err != nil {
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
	grp.POST("/", func(c *gin.Context) {
		var payload SupplierCreate
		if err := c.ShouldBindJSON(&payload); err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Invalid payload"})
			return
		}
		s := Supplier{
			Name:         payload.Name,
			Title:        payload.Title,
			FirstName:    payload.FirstName,
			LastName:     payload.LastName,
			Street:       payload.Street,
			HouseNumber:  payload.HouseNumber,
			City:         payload.City,
			PostalCode:   payload.PostalCode,
			CountryID:    payload.CountryID,
			PhoneNumber1: payload.PhoneNumber1,
			PhoneNumber2: payload.PhoneNumber2,
			PhoneNumber3: payload.PhoneNumber3,
			Email:        payload.Email,
			Website:      payload.Website,
		}
		if err := db.Create(&s).Error; err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to create supplier"})
			return
		}
		// attach country if present (ignore preload errors intentionally)
		_ = db.Preload("Country").First(&s, s.ID).Error
		c.JSON(http.StatusCreated, s)
	})

	// PUT /api/admin/suppliers/:id -> update
	grp.PUT("/:id", func(c *gin.Context) {
		id := c.Param("id")
		var payload SupplierUpdate
		if err := c.ShouldBindJSON(&payload); err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Invalid payload"})
			return
		}
		var existing Supplier
		if err := db.First(&existing, "id = ?", id).Error; err != nil {
			if errors.Is(err, gorm.ErrRecordNotFound) {
				c.JSON(http.StatusNotFound, gin.H{"detail": "Supplier not found"})
				return
			}
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to fetch supplier"})
			return
		}

		// Overwrite fields with incoming values (frontend sends all)
		existing.Name = payload.Name
		existing.Title = payload.Title
		existing.FirstName = payload.FirstName
		existing.LastName = payload.LastName
		existing.Street = payload.Street
		existing.HouseNumber = payload.HouseNumber
		existing.City = payload.City
		existing.PostalCode = payload.PostalCode
		existing.CountryID = payload.CountryID
		existing.PhoneNumber1 = payload.PhoneNumber1
		existing.PhoneNumber2 = payload.PhoneNumber2
		existing.PhoneNumber3 = payload.PhoneNumber3
		existing.Email = payload.Email
		existing.Website = payload.Website

		if err := db.Save(&existing).Error; err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to update supplier"})
			return
		}
		// refresh with preloaded country (ignore preload errors)
		_ = db.Preload("Country").First(&existing, existing.ID).Error
		c.JSON(http.StatusOK, existing)
	})

	// DELETE /api/admin/suppliers/:id -> delete
	grp.DELETE("/:id", func(c *gin.Context) {
		id := c.Param("id")
		if err := db.Delete(&Supplier{}, "id = ?", id).Error; err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to delete supplier"})
			return
		}
		c.Status(http.StatusNoContent)
	})
}
