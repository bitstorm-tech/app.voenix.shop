package prompt

import (
	"net/http"
	"strings"

	"github.com/gin-gonic/gin"
	"gorm.io/gorm"

	"voenix/backend-go/internal/auth"
)

type slotVariantCreate struct {
	PromptSlotTypeID     int     `json:"promptSlotTypeId"`
	Name                 string  `json:"name"`
	Prompt               *string `json:"prompt"`
	Description          *string `json:"description"`
	ExampleImageFilename *string `json:"exampleImageFilename"`
}

type slotVariantUpdate struct {
	PromptSlotTypeID     *int    `json:"promptSlotTypeId"`
	Name                 *string `json:"name"`
	Prompt               *string `json:"prompt"`
	Description          *string `json:"description"`
	ExampleImageFilename *string `json:"exampleImageFilename"`
}

func registerAdminSlotVariantRoutes(r *gin.Engine, db *gorm.DB) {
	grp := r.Group("/api/admin/prompts/slot-variants")
	grp.Use(auth.RequireAdmin(db))

	grp.GET("/", func(c *gin.Context) {
		var rows []PromptSlotVariant
		if err := db.Preload("PromptSlotType").Order("id desc").Find(&rows).Error; err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to fetch slot variants"})
			return
		}
		out := make([]PromptSlotVariantRead, 0, len(rows))
		for i := range rows {
			out = append(out, toSlotVariantRead(&rows[i]))
		}
		c.JSON(http.StatusOK, out)
	})

	grp.GET("/:id", func(c *gin.Context) {
		var row PromptSlotVariant
		if err := db.Preload("PromptSlotType").First(&row, "id = ?", c.Param("id")).Error; err != nil {
			if errorsIsNotFound(err) {
				c.JSON(http.StatusNotFound, gin.H{"detail": "PromptSlotVariant not found"})
				return
			}
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to fetch slot variant"})
			return
		}
		c.JSON(http.StatusOK, toSlotVariantRead(&row))
	})

	grp.POST("/", func(c *gin.Context) {
		var payload slotVariantCreate
		if err := c.ShouldBindJSON(&payload); err != nil || strings.TrimSpace(payload.Name) == "" || payload.PromptSlotTypeID <= 0 {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Invalid payload"})
			return
		}
		// Validate slot type exists
		if !existsByID[PromptSlotType](db, payload.PromptSlotTypeID) {
			c.JSON(http.StatusNotFound, gin.H{"detail": "PromptSlotType not found"})
			return
		}
		// Unique name
		var cnt int64
		db.Model(&PromptSlotVariant{}).Where("name = ?", payload.Name).Count(&cnt)
		if cnt > 0 {
			c.JSON(http.StatusConflict, gin.H{"detail": "PromptSlotVariant name already exists"})
			return
		}
		row := PromptSlotVariant{
			PromptSlotTypeID:     payload.PromptSlotTypeID,
			Name:                 payload.Name,
			Prompt:               payload.Prompt,
			Description:          payload.Description,
			ExampleImageFilename: payload.ExampleImageFilename,
		}
		if err := db.Create(&row).Error; err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to create slot variant"})
			return
		}
		_ = db.Preload("PromptSlotType").First(&row, row.ID).Error
		c.JSON(http.StatusCreated, toSlotVariantRead(&row))
	})

	grp.PUT("/:id", func(c *gin.Context) {
		var existing PromptSlotVariant
		if err := db.First(&existing, "id = ?", c.Param("id")).Error; err != nil {
			if errorsIsNotFound(err) {
				c.JSON(http.StatusNotFound, gin.H{"detail": "PromptSlotVariant not found"})
				return
			}
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to fetch slot variant"})
			return
		}
		var payload slotVariantUpdate
		if err := c.ShouldBindJSON(&payload); err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Invalid payload"})
			return
		}
		if payload.PromptSlotTypeID != nil && *payload.PromptSlotTypeID != existing.PromptSlotTypeID {
			if !existsByID[PromptSlotType](db, *payload.PromptSlotTypeID) {
				c.JSON(http.StatusNotFound, gin.H{"detail": "PromptSlotType not found"})
				return
			}
			existing.PromptSlotTypeID = *payload.PromptSlotTypeID
		}
		if payload.Name != nil && *payload.Name != existing.Name {
			var cnt int64
			db.Model(&PromptSlotVariant{}).Where("name = ? AND id <> ?", *payload.Name, existing.ID).Count(&cnt)
			if cnt > 0 {
				c.JSON(http.StatusConflict, gin.H{"detail": "PromptSlotVariant name already exists"})
				return
			}
			existing.Name = *payload.Name
		}
		if payload.Prompt != nil {
			existing.Prompt = payload.Prompt
		}
		if payload.Description != nil {
			existing.Description = payload.Description
		}
		if payload.ExampleImageFilename != nil {
			// delete old image if changed
			old := existing.ExampleImageFilename
			if old != nil && (payload.ExampleImageFilename == nil || *old != *payload.ExampleImageFilename) {
				safeDeletePublicImage(*old, "slot-variant")
			}
			existing.ExampleImageFilename = payload.ExampleImageFilename
		}
		if err := db.Save(&existing).Error; err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to update slot variant"})
			return
		}
		_ = db.Preload("PromptSlotType").First(&existing, existing.ID).Error
		c.JSON(http.StatusOK, toSlotVariantRead(&existing))
	})

	grp.DELETE("/:id", func(c *gin.Context) {
		var existing PromptSlotVariant
		if err := db.First(&existing, "id = ?", c.Param("id")).Error; err != nil {
			if errorsIsNotFound(err) {
				c.Status(http.StatusNoContent)
				return
			}
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to fetch slot variant"})
			return
		}
		if existing.ExampleImageFilename != nil {
			safeDeletePublicImage(*existing.ExampleImageFilename, "slot-variant")
		}
		if err := db.Delete(&PromptSlotVariant{}, existing.ID).Error; err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to delete slot variant"})
			return
		}
		c.Status(http.StatusNoContent)
	})
}
