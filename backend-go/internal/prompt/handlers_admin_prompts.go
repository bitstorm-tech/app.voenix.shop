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

// Payload type for prompt slot references in create/update payloads.
type promptSlotRef struct {
	SlotID int `json:"slotId"`
}

type promptCreate struct {
	Title                string                  `json:"title"`
	PromptText           *string                 `json:"promptText"`
	CategoryID           *int                    `json:"categoryId"`
	SubcategoryID        *int                    `json:"subcategoryId"`
	PriceID              *int                    `json:"priceId"`
	ExampleImageFilename *string                 `json:"exampleImageFilename"`
	Slots                []promptSlotRef         `json:"slots"`
	CostCalculation      *costCalculationRequest `json:"costCalculation"`
}

type promptUpdate struct {
	Title                *string                 `json:"title"`
	PromptText           *string                 `json:"promptText"`
	CategoryID           *int                    `json:"categoryId"`
	SubcategoryID        *int                    `json:"subcategoryId"`
	Active               *bool                   `json:"active"`
	PriceID              *int                    `json:"priceId"`
	ExampleImageFilename *string                 `json:"exampleImageFilename"`
	Slots                *[]promptSlotRef        `json:"slots"`
	CostCalculation      *costCalculationRequest `json:"costCalculation"`
}

func registerAdminPromptRoutes(r *gin.Engine, db *gorm.DB, svc *service) {
	grp := r.Group("/api/admin/prompts")
	grp.Use(auth.RequireAdmin(db))

	grp.GET("", func(c *gin.Context) {
		rows, err := svc.listPrompts(c.Request.Context())
		if err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to fetch prompts"})
			return
		}
		c.JSON(http.StatusOK, rows)
	})

	grp.GET("/:id", func(c *gin.Context) {
		id, _ := strconv.Atoi(c.Param("id"))
		row, err := svc.getPrompt(c.Request.Context(), id)
		if err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to fetch prompt"})
			return
		}
		if row == nil {
			c.JSON(http.StatusNotFound, gin.H{"detail": "Prompt not found"})
			return
		}
		c.JSON(http.StatusOK, row)
	})

	grp.POST("", func(c *gin.Context) {
		var payload promptCreate
		if err := c.ShouldBindJSON(&payload); err != nil || strings.TrimSpace(payload.Title) == "" {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Invalid payload"})
			return
		}
		created, err := svc.createPrompt(c.Request.Context(), payload)
		if err != nil {
			if errors.Is(err, gorm.ErrRecordNotFound) {
				c.JSON(http.StatusNotFound, gin.H{"detail": "PromptCategory/Subcategory or SlotVariant not found"})
				return
			}
			if err.Error() == "subcategory does not belong to the specified category" {
				c.JSON(http.StatusBadRequest, gin.H{"detail": "Subcategory does not belong to the specified category"})
				return
			}
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to create prompt"})
			return
		}
		c.JSON(http.StatusCreated, created)
	})

	grp.PUT("/:id", func(c *gin.Context) {
		id, _ := strconv.Atoi(c.Param("id"))
		var payload promptUpdate
		if err := c.ShouldBindJSON(&payload); err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Invalid payload"})
			return
		}
		updated, err := svc.updatePrompt(c.Request.Context(), id, payload)
		if err != nil {
			if errors.Is(err, gorm.ErrRecordNotFound) {
				c.JSON(http.StatusNotFound, gin.H{"detail": "Prompt/Category/Subcategory or SlotVariant not found"})
				return
			}
			if err.Error() == "subcategory does not belong to the specified category" {
				c.JSON(http.StatusBadRequest, gin.H{"detail": "Subcategory does not belong to the specified category"})
				return
			}
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to update prompt"})
			return
		}
		c.JSON(http.StatusOK, updated)
	})

	grp.DELETE("/:id", func(c *gin.Context) {
		id, _ := strconv.Atoi(c.Param("id"))
		if err := svc.deletePrompt(c.Request.Context(), id); err != nil {
			if errors.Is(err, gorm.ErrRecordNotFound) {
				c.Status(http.StatusNoContent)
				return
			}
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to delete prompt"})
			return
		}
		c.Status(http.StatusNoContent)
	})
}
