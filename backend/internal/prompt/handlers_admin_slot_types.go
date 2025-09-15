package prompt

import (
	"errors"
	"net/http"
	"strings"

	"github.com/gin-gonic/gin"
	"gorm.io/gorm"

	"voenix/backend/internal/auth"
)

type slotTypeCreate struct {
	Name     string `json:"name"`
	Position int    `json:"position"`
}

type slotTypeUpdate struct {
	Name     *string `json:"name"`
	Position *int    `json:"position"`
}

func registerAdminSlotTypeRoutes(r *gin.Engine, db *gorm.DB, svc *service) {
	grp := r.Group("/api/admin/prompts/prompt-slot-types")
	grp.Use(auth.RequireAdmin(db))

	grp.GET("", func(c *gin.Context) {
		rows, err := svc.listSlotTypes(c.Request.Context())
		if err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to fetch slot types"})
			return
		}
		c.JSON(http.StatusOK, rows)
	})

	grp.GET("/:id", func(c *gin.Context) {
		id, _ := strconvAtoi(c.Param("id"))
		row, err := svc.getSlotType(c.Request.Context(), id)
		if err != nil {
			if errors.Is(err, gorm.ErrRecordNotFound) || row == nil {
				c.JSON(http.StatusNotFound, gin.H{"detail": "PromptSlotType not found"})
				return
			}
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to fetch slot type"})
			return
		}
		c.JSON(http.StatusOK, row)
	})

	grp.POST("", func(c *gin.Context) {
		var payload slotTypeCreate
		if err := c.ShouldBindJSON(&payload); err != nil || strings.TrimSpace(payload.Name) == "" {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Invalid payload"})
			return
		}
		created, err := svc.createSlotType(c.Request.Context(), payload.Name, payload.Position)
		if err != nil {
			var ce conflictError
			if errors.As(err, &ce) {
				c.JSON(http.StatusConflict, gin.H{"detail": ce.Detail})
				return
			}
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to create slot type"})
			return
		}
		c.JSON(http.StatusCreated, created)
	})

	grp.PUT("/:id", func(c *gin.Context) {
		id, _ := strconvAtoi(c.Param("id"))
		var payload slotTypeUpdate
		if err := c.ShouldBindJSON(&payload); err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Invalid payload"})
			return
		}
		updated, err := svc.updateSlotType(c.Request.Context(), id, payload.Name, payload.Position)
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
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to update slot type"})
			return
		}
		c.JSON(http.StatusOK, updated)
	})

	grp.DELETE("/:id", func(c *gin.Context) {
		id, _ := strconvAtoi(c.Param("id"))
		if err := svc.deleteSlotType(c.Request.Context(), id); err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to delete slot type"})
			return
		}
		c.Status(http.StatusNoContent)
	})
}
