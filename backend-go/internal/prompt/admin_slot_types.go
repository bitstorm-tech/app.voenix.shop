package prompt

import (
	"net/http"
	"strings"

	"github.com/gin-gonic/gin"
	"gorm.io/gorm"

	"voenix/backend-go/internal/auth"
)

type slotTypeCreate struct {
	Name     string `json:"name"`
	Position int    `json:"position"`
}

type slotTypeUpdate struct {
	Name     *string `json:"name"`
	Position *int    `json:"position"`
}

func registerAdminSlotTypeRoutes(r *gin.Engine, db *gorm.DB) {
	grp := r.Group("/api/admin/prompts/prompt-slot-types")
	grp.Use(auth.RequireAdmin(db))

	grp.GET("/", func(c *gin.Context) {
		var rows []PromptSlotType
		if err := db.Order("id desc").Find(&rows).Error; err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to fetch slot types"})
			return
		}
		out := make([]PromptSlotTypeRead, 0, len(rows))
		for i := range rows {
			out = append(out, toSlotTypeRead(&rows[i]))
		}
		c.JSON(http.StatusOK, out)
	})

	grp.GET("/:id", func(c *gin.Context) {
		var row PromptSlotType
		if err := db.First(&row, "id = ?", c.Param("id")).Error; err != nil {
			if errorsIsNotFound(err) {
				c.JSON(http.StatusNotFound, gin.H{"detail": "PromptSlotType not found"})
				return
			}
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to fetch slot type"})
			return
		}
		c.JSON(http.StatusOK, toSlotTypeRead(&row))
	})

	grp.POST("/", func(c *gin.Context) {
		var payload slotTypeCreate
		if err := c.ShouldBindJSON(&payload); err != nil || strings.TrimSpace(payload.Name) == "" {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Invalid payload"})
			return
		}
		// Uniqueness checks
		var cnt int64
		db.Model(&PromptSlotType{}).Where("name = ?", payload.Name).Count(&cnt)
		if cnt > 0 {
			c.JSON(http.StatusConflict, gin.H{"detail": "PromptSlotType name already exists"})
			return
		}
		db.Model(&PromptSlotType{}).Where("position = ?", payload.Position).Count(&cnt)
		if cnt > 0 {
			c.JSON(http.StatusConflict, gin.H{"detail": "PromptSlotType position already exists"})
			return
		}
		row := PromptSlotType{Name: payload.Name, Position: payload.Position}
		if err := db.Create(&row).Error; err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to create slot type"})
			return
		}
		c.JSON(http.StatusCreated, toSlotTypeRead(&row))
	})

	grp.PUT("/:id", func(c *gin.Context) {
		id := c.Param("id")
		var existing PromptSlotType
		if err := db.First(&existing, "id = ?", id).Error; err != nil {
			if errorsIsNotFound(err) {
				c.JSON(http.StatusNotFound, gin.H{"detail": "PromptSlotType not found"})
				return
			}
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to fetch slot type"})
			return
		}
		var payload slotTypeUpdate
		if err := c.ShouldBindJSON(&payload); err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Invalid payload"})
			return
		}
		if payload.Name != nil && *payload.Name != existing.Name {
			var cnt int64
			db.Model(&PromptSlotType{}).Where("name = ? AND id <> ?", *payload.Name, existing.ID).Count(&cnt)
			if cnt > 0 {
				c.JSON(http.StatusConflict, gin.H{"detail": "PromptSlotType name already exists"})
				return
			}
			existing.Name = *payload.Name
		}
		if payload.Position != nil && *payload.Position != existing.Position {
			var cnt int64
			db.Model(&PromptSlotType{}).Where("position = ? AND id <> ?", *payload.Position, existing.ID).Count(&cnt)
			if cnt > 0 {
				c.JSON(http.StatusConflict, gin.H{"detail": "PromptSlotType position already exists"})
				return
			}
			existing.Position = *payload.Position
		}
		if err := db.Save(&existing).Error; err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to update slot type"})
			return
		}
		c.JSON(http.StatusOK, toSlotTypeRead(&existing))
	})

	grp.DELETE("/:id", func(c *gin.Context) {
		if err := db.Delete(&PromptSlotType{}, "id = ?", c.Param("id")).Error; err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to delete slot type"})
			return
		}
		c.Status(http.StatusNoContent)
	})
}
