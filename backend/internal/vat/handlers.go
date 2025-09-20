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
func RegisterRoutes(engine *gin.Engine, database *gorm.DB, vatService Service) {
	group := engine.Group("/api/admin/vat")
	group.Use(auth.RequireAdmin(database))

	group.GET("", func(context *gin.Context) {
		values, err := vatService.List(context.Request.Context())
		if err != nil {
			context.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to fetch VATs"})
			return
		}
		context.JSON(http.StatusOK, makeValueAddedTaxListResponse(values))
	})

	group.GET("/:id", func(context *gin.Context) {
		identifier, err := strconv.Atoi(context.Param("id"))
		if err != nil {
			context.JSON(http.StatusBadRequest, gin.H{"detail": "Invalid id"})
			return
		}
		value, err := vatService.Get(context.Request.Context(), identifier)
		if err != nil {
			if errors.Is(err, ErrNotFound) {
				context.JSON(http.StatusNotFound, gin.H{"detail": "VAT not found"})
				return
			}
			context.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to fetch VAT"})
			return
		}
		context.JSON(http.StatusOK, createValueAddedTaxResponse(value))
	})

	group.POST("", func(context *gin.Context) {
		var request updateValueAddedTaxRequest
		if err := context.ShouldBindJSON(&request); err != nil {
			context.JSON(http.StatusBadRequest, gin.H{"detail": "Invalid payload"})
			return
		}
		created, err := vatService.Create(context.Request.Context(), request.toCreate())
		if err != nil {
			if errors.Is(err, ErrConflict) {
				context.JSON(http.StatusConflict, gin.H{"detail": "A VAT with this name already exists."})
				return
			}
			context.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to create VAT"})
			return
		}
		context.JSON(http.StatusCreated, createValueAddedTaxResponse(created))
	})

	group.PUT("/:id", func(context *gin.Context) {
		identifier, err := strconv.Atoi(context.Param("id"))
		if err != nil {
			context.JSON(http.StatusBadRequest, gin.H{"detail": "Invalid id"})
			return
		}
		var request updateValueAddedTaxRequest
		if err := context.ShouldBindJSON(&request); err != nil {
			context.JSON(http.StatusBadRequest, gin.H{"detail": "Invalid payload"})
			return
		}
		updated, err := vatService.Update(context.Request.Context(), identifier, request.toCreate())
		if err != nil {
			if errors.Is(err, ErrNotFound) {
				context.JSON(http.StatusNotFound, gin.H{"detail": "VAT not found"})
				return
			}
			if errors.Is(err, ErrConflict) {
				context.JSON(http.StatusConflict, gin.H{"detail": "A VAT with this name already exists."})
				return
			}
			context.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to update VAT"})
			return
		}
		context.JSON(http.StatusOK, createValueAddedTaxResponse(updated))
	})

	group.DELETE("/:id", func(context *gin.Context) {
		identifier, err := strconv.Atoi(context.Param("id"))
		if err != nil {
			context.JSON(http.StatusBadRequest, gin.H{"detail": "Invalid id"})
			return
		}
		if err := vatService.Delete(context.Request.Context(), identifier); err != nil {
			context.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to delete VAT"})
			return
		}
		context.Status(http.StatusNoContent)
	})
}

type updateValueAddedTaxRequest struct {
	Name        string  `json:"name" binding:"required"`
	Percent     int     `json:"percent" binding:"required"`
	Description *string `json:"description"`
	IsDefault   bool    `json:"isDefault"`
}

func (request updateValueAddedTaxRequest) toCreate() ValueAddedTaxCreate {
	return ValueAddedTaxCreate(request)
}

func makeValueAddedTaxListResponse(values []ValueAddedTax) []gin.H {
	responses := make([]gin.H, 0, len(values))
	for _, value := range values {
		responses = append(responses, createValueAddedTaxResponse(value))
	}
	return responses
}

func createValueAddedTaxResponse(value ValueAddedTax) gin.H {
	response := gin.H{
		"id":        value.ID,
		"name":      value.Name,
		"percent":   value.Percent,
		"isDefault": value.IsDefault,
		"createdAt": value.CreatedAt,
		"updatedAt": value.UpdatedAt,
	}
	if value.Description != nil {
		response["description"] = value.Description
	}
	return response
}
