package supplier

import (
	"errors"
	"net/http"
	"strconv"

	"github.com/gin-gonic/gin"
	"gorm.io/gorm"

	"voenix/backend/internal/auth"
)

type SupplierCreateRequest struct {
	Name         *string `json:"name"`
	Title        *string `json:"title"`
	FirstName    *string `json:"firstName"`
	LastName     *string `json:"lastName"`
	Street       *string `json:"street"`
	HouseNumber  *string `json:"houseNumber"`
	City         *string `json:"city"`
	PostalCode   *int    `json:"postalCode"`
	CountryID    *int    `json:"countryId"`
	PhoneNumber1 *string `json:"phoneNumber1"`
	PhoneNumber2 *string `json:"phoneNumber2"`
	PhoneNumber3 *string `json:"phoneNumber3"`
	Email        *string `json:"email"`
	Website      *string `json:"website"`
}

func (payload SupplierCreateRequest) ToDomain() Supplier {
	domainSupplier := Supplier{
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
	return domainSupplier
}

type SupplierUpdateRequest struct {
	Name         *string `json:"name"`
	Title        *string `json:"title"`
	FirstName    *string `json:"firstName"`
	LastName     *string `json:"lastName"`
	Street       *string `json:"street"`
	HouseNumber  *string `json:"houseNumber"`
	City         *string `json:"city"`
	PostalCode   *int    `json:"postalCode"`
	CountryID    *int    `json:"countryId"`
	PhoneNumber1 *string `json:"phoneNumber1"`
	PhoneNumber2 *string `json:"phoneNumber2"`
	PhoneNumber3 *string `json:"phoneNumber3"`
	Email        *string `json:"email"`
	Website      *string `json:"website"`
}

func (payload SupplierUpdateRequest) ToDomain() Supplier {
	domainSupplier := Supplier{
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
	return domainSupplier
}

// RegisterRoutes mounts Supplier admin routes under /api/admin/suppliers.
func RegisterRoutes(router *gin.Engine, database *gorm.DB, service *Service) {
	supplierGroup := router.Group("/api/admin/suppliers")
	supplierGroup.Use(auth.RequireAdmin(database))

	supplierGroup.GET("", func(ginContext *gin.Context) {
		supplierList, errorValue := service.ListSuppliers(ginContext.Request.Context())
		if errorValue != nil {
			ginContext.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to fetch suppliers"})
			return
		}
		ginContext.JSON(http.StatusOK, supplierList)
	})

	supplierGroup.GET("/:id", func(ginContext *gin.Context) {
		supplierIdentifier, errorValue := strconv.Atoi(ginContext.Param("id"))
		if errorValue != nil {
			ginContext.JSON(http.StatusBadRequest, gin.H{"detail": "Invalid id"})
			return
		}
		supplierValue, errorValue := service.GetSupplierByID(ginContext.Request.Context(), supplierIdentifier)
		if errorValue != nil {
			if errors.Is(errorValue, gorm.ErrRecordNotFound) {
				ginContext.JSON(http.StatusNotFound, gin.H{"detail": "Supplier not found"})
				return
			}
			ginContext.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to fetch supplier"})
			return
		}
		ginContext.JSON(http.StatusOK, supplierValue)
	})

	supplierGroup.POST("", func(ginContext *gin.Context) {
		var payload SupplierCreateRequest
		if errorValue := ginContext.ShouldBindJSON(&payload); errorValue != nil {
			ginContext.JSON(http.StatusBadRequest, gin.H{"detail": "Invalid payload"})
			return
		}

		supplierInput := payload.ToDomain()
		createdSupplier, errorValue := service.CreateSupplier(ginContext.Request.Context(), supplierInput)
		if errorValue != nil {
			ginContext.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to create supplier"})
			return
		}
		ginContext.JSON(http.StatusCreated, createdSupplier)
	})

	supplierGroup.PUT("/:id", func(ginContext *gin.Context) {
		supplierIdentifier, errorValue := strconv.Atoi(ginContext.Param("id"))
		if errorValue != nil {
			ginContext.JSON(http.StatusBadRequest, gin.H{"detail": "Invalid id"})
			return
		}
		var payload SupplierUpdateRequest
		if errorValue := ginContext.ShouldBindJSON(&payload); errorValue != nil {
			ginContext.JSON(http.StatusBadRequest, gin.H{"detail": "Invalid payload"})
			return
		}

		updatesSupplier := payload.ToDomain()
		updatedSupplier, errorValue := service.UpdateSupplier(ginContext.Request.Context(), supplierIdentifier, updatesSupplier)
		if errorValue != nil {
			if errors.Is(errorValue, gorm.ErrRecordNotFound) {
				ginContext.JSON(http.StatusNotFound, gin.H{"detail": "Supplier not found"})
				return
			}
			ginContext.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to update supplier"})
			return
		}
		ginContext.JSON(http.StatusOK, updatedSupplier)
	})

	supplierGroup.DELETE("/:id", func(ginContext *gin.Context) {
		supplierIdentifier, errorValue := strconv.Atoi(ginContext.Param("id"))
		if errorValue != nil {
			ginContext.JSON(http.StatusBadRequest, gin.H{"detail": "Invalid id"})
			return
		}
		if errorValue := service.DeleteSupplier(ginContext.Request.Context(), supplierIdentifier); errorValue != nil {
			ginContext.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to delete supplier"})
			return
		}
		ginContext.Status(http.StatusNoContent)
	})
}
