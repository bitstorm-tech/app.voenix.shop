package prompt

import (
	"errors"
	"net/http"
	"strings"

	"github.com/gin-gonic/gin"
	"gorm.io/gorm"

	"voenix/backend/internal/auth"
)

type slotVariantCreate struct {
	PromptSlotTypeID     int     `json:"promptSlotTypeId"`
	Name                 string  `json:"name"`
	Prompt               *string `json:"prompt"`
	Description          *string `json:"description"`
	ExampleImageFilename *string `json:"exampleImageFilename"`
	LLM                  string  `json:"llm"`
}

type slotVariantUpdate struct {
	PromptSlotTypeID     *int    `json:"promptSlotTypeId"`
	Name                 *string `json:"name"`
	Prompt               *string `json:"prompt"`
	Description          *string `json:"description"`
	ExampleImageFilename *string `json:"exampleImageFilename"`
	LLM                  *string `json:"llm"`
}

func registerAdminSlotVariantRoutes(r *gin.Engine, db *gorm.DB, svc *service) {
	grp := r.Group("/api/admin/prompts/slot-variants")
	grp.Use(auth.RequireAdmin(db))

	grp.GET("", func(c *gin.Context) {
		rows, err := svc.listSlotVariants(c.Request.Context())
		if err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to fetch slot variants"})
			return
		}
		c.JSON(http.StatusOK, rows)
	})

	grp.GET("/:id", func(c *gin.Context) {
		id, _ := strconvAtoi(c.Param("id"))
		row, err := svc.getSlotVariant(c.Request.Context(), id)
		if err != nil {
			if errors.Is(err, gorm.ErrRecordNotFound) || row == nil {
				c.JSON(http.StatusNotFound, gin.H{"detail": "PromptSlotVariant not found"})
				return
			}
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to fetch slot variant"})
			return
		}
		c.JSON(http.StatusOK, row)
	})

	grp.POST("", func(c *gin.Context) {
		var payload slotVariantCreate
		if err := c.ShouldBindJSON(&payload); err != nil || strings.TrimSpace(payload.Name) == "" || payload.PromptSlotTypeID <= 0 || strings.TrimSpace(payload.LLM) == "" {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Invalid payload"})
			return
		}
		created, err := svc.createSlotVariant(c.Request.Context(), payload)
		if err != nil {
			if errors.Is(err, gorm.ErrRecordNotFound) {
				c.JSON(http.StatusNotFound, gin.H{"detail": "PromptSlotType not found"})
				return
			}
			var ce conflictError
			if errors.As(err, &ce) {
				c.JSON(http.StatusConflict, gin.H{"detail": ce.Detail})
				return
			}
			if errors.Is(err, errInvalidLLM) {
				c.JSON(http.StatusBadRequest, gin.H{"detail": "Invalid llm selection"})
				return
			}
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to create slot variant"})
			return
		}
		c.JSON(http.StatusCreated, created)
	})

	grp.PUT("/:id", func(c *gin.Context) {
		id, _ := strconvAtoi(c.Param("id"))
		var payload slotVariantUpdate
		if err := c.ShouldBindJSON(&payload); err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Invalid payload"})
			return
		}
		updated, err := svc.updateSlotVariant(c.Request.Context(), id, payload)
		if err != nil {
			if errors.Is(err, gorm.ErrRecordNotFound) {
				c.JSON(http.StatusNotFound, gin.H{"detail": "PromptSlotType or PromptSlotVariant not found"})
				return
			}
			var ce conflictError
			if errors.As(err, &ce) {
				c.JSON(http.StatusConflict, gin.H{"detail": ce.Detail})
				return
			}
			if errors.Is(err, errInvalidLLM) {
				c.JSON(http.StatusBadRequest, gin.H{"detail": "Invalid llm selection"})
				return
			}
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to update slot variant"})
			return
		}
		c.JSON(http.StatusOK, updated)
	})

	grp.DELETE("/:id", func(c *gin.Context) {
		id, _ := strconvAtoi(c.Param("id"))
		if err := svc.deleteSlotVariant(c.Request.Context(), id); err != nil {
			if errors.Is(err, gorm.ErrRecordNotFound) {
				c.Status(http.StatusNoContent)
				return
			}
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to delete slot variant"})
			return
		}
		c.Status(http.StatusNoContent)
	})
}

func registerAdminLLMRoutes(r *gin.Engine, db *gorm.DB, svc *service) {
	grp := r.Group("/api/admin/prompts/llms")
	grp.Use(auth.RequireAdmin(db))

	grp.GET("", func(c *gin.Context) {
		c.JSON(http.StatusOK, gin.H{"llms": svc.listLLMOptions()})
	})
}
