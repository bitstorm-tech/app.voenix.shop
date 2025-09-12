package prompt

import (
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
	Title                string          `json:"title"`
	PromptText           *string         `json:"promptText"`
	CategoryID           *int            `json:"categoryId"`
	SubcategoryID        *int            `json:"subcategoryId"`
	ExampleImageFilename *string         `json:"exampleImageFilename"`
	Slots                []promptSlotRef `json:"slots"`
}

type promptUpdate struct {
	Title                *string          `json:"title"`
	PromptText           *string          `json:"promptText"`
	CategoryID           *int             `json:"categoryId"`
	SubcategoryID        *int             `json:"subcategoryId"`
	Active               *bool            `json:"active"`
	ExampleImageFilename *string          `json:"exampleImageFilename"`
	Slots                *[]promptSlotRef `json:"slots"`
}

func registerAdminPromptRoutes(r *gin.Engine, db *gorm.DB) {
	grp := r.Group("/api/admin/prompts")
	grp.Use(auth.RequireAdmin(db))

	grp.GET("", func(c *gin.Context) {
		rows, err := allPromptsWithRelations(db)
		if err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to fetch prompts"})
			return
		}
		out := make([]PromptRead, 0, len(rows))
		for i := range rows {
			out = append(out, toPromptRead(db, &rows[i]))
		}
		c.JSON(http.StatusOK, out)
	})

	grp.GET("/:id", func(c *gin.Context) {
		id, _ := strconv.Atoi(c.Param("id"))
		row, err := loadPromptWithRelations(db, id)
		if err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to fetch prompt"})
			return
		}
		if row == nil {
			c.JSON(http.StatusNotFound, gin.H{"detail": "Prompt not found"})
			return
		}
		c.JSON(http.StatusOK, toPromptRead(db, row))
	})

	grp.POST("", func(c *gin.Context) {
		var payload promptCreate
		if err := c.ShouldBindJSON(&payload); err != nil || strings.TrimSpace(payload.Title) == "" {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Invalid payload"})
			return
		}
		// Validate relationships
		if payload.CategoryID != nil && !existsByID[PromptCategory](db, *payload.CategoryID) {
			c.JSON(http.StatusNotFound, gin.H{"detail": "PromptCategory not found"})
			return
		}
		if payload.SubcategoryID != nil {
			var sc PromptSubCategory
			if err := db.First(&sc, "id = ?", *payload.SubcategoryID).Error; err != nil {
				if errorsIsNotFound(err) {
					c.JSON(http.StatusNotFound, gin.H{"detail": "PromptSubCategory not found"})
					return
				}
				c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to fetch subcategory"})
				return
			}
			if payload.CategoryID != nil && sc.PromptCategoryID != *payload.CategoryID {
				c.JSON(http.StatusBadRequest, gin.H{"detail": "Subcategory does not belong to the specified category"})
				return
			}
		}
		// Validate slots (unique + existence)
		slotIDs := uniqueSlotIDs(payload.Slots)
		if len(slotIDs) > 0 {
			var cnt int64
			if err := db.Model(&PromptSlotVariant{}).Where("id IN ?", slotIDs).Count(&cnt).Error; err != nil {
				c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to validate slots"})
				return
			}
			if cnt != int64(len(slotIDs)) {
				c.JSON(http.StatusNotFound, gin.H{"detail": "Some PromptSlotVariant ids not found"})
				return
			}
		}
		row := Prompt{
			Title:                payload.Title,
			PromptText:           payload.PromptText,
			CategoryID:           payload.CategoryID,
			SubcategoryID:        payload.SubcategoryID,
			Active:               true,
			ExampleImageFilename: payload.ExampleImageFilename,
		}
		if err := db.Create(&row).Error; err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to create prompt"})
			return
		}
		// Insert mappings
		for _, sid := range slotIDs {
			_ = db.Create(&PromptSlotVariantMapping{PromptID: row.ID, SlotID: sid}).Error
		}
		// Reload with relations
		reloaded, _ := loadPromptWithRelations(db, row.ID)
		c.JSON(http.StatusCreated, toPromptRead(db, reloaded))
	})

	grp.PUT("/:id", func(c *gin.Context) {
		id, _ := strconv.Atoi(c.Param("id"))
		var existing Prompt
		if err := db.First(&existing, "id = ?", id).Error; err != nil {
			if errorsIsNotFound(err) {
				c.JSON(http.StatusNotFound, gin.H{"detail": "Prompt not found"})
				return
			}
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to fetch prompt"})
			return
		}
		var payload promptUpdate
		if err := c.ShouldBindJSON(&payload); err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Invalid payload"})
			return
		}
		// Validate category/subcategory when provided
		if payload.CategoryID != nil && !existsByID[PromptCategory](db, *payload.CategoryID) {
			c.JSON(http.StatusNotFound, gin.H{"detail": "PromptCategory not found"})
			return
		}
		if payload.SubcategoryID != nil {
			var sc PromptSubCategory
			if err := db.First(&sc, "id = ?", *payload.SubcategoryID).Error; err != nil {
				if errorsIsNotFound(err) {
					c.JSON(http.StatusNotFound, gin.H{"detail": "PromptSubCategory not found"})
					return
				}
				c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to fetch subcategory"})
				return
			}
			if payload.CategoryID != nil && sc.PromptCategoryID != *payload.CategoryID {
				c.JSON(http.StatusBadRequest, gin.H{"detail": "Subcategory does not belong to the specified category"})
				return
			}
		}
		// Apply fields
		if payload.Title != nil {
			existing.Title = *payload.Title
		}
		if payload.PromptText != nil {
			existing.PromptText = payload.PromptText
		}
		if payload.CategoryID != nil {
			existing.CategoryID = payload.CategoryID
		}
		if payload.SubcategoryID != nil {
			existing.SubcategoryID = payload.SubcategoryID
		}
		if payload.Active != nil {
			existing.Active = *payload.Active
		}
		if payload.ExampleImageFilename != nil {
			old := existing.ExampleImageFilename
			if old != nil && (payload.ExampleImageFilename == nil || *old != *payload.ExampleImageFilename) {
				safeDeletePublicImage(*old, "prompt")
			}
			existing.ExampleImageFilename = payload.ExampleImageFilename
		}
		if err := db.Save(&existing).Error; err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to update prompt"})
			return
		}
		// Update mappings if provided
		if payload.Slots != nil {
			newIDs := uniqueSlotIDs(*payload.Slots)
			if len(newIDs) > 0 {
				var cnt int64
				if err := db.Model(&PromptSlotVariant{}).Where("id IN ?", newIDs).Count(&cnt).Error; err != nil {
					c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to validate slots"})
					return
				}
				if cnt != int64(len(newIDs)) {
					c.JSON(http.StatusNotFound, gin.H{"detail": "Some PromptSlotVariant ids not found"})
					return
				}
			}
			// Clear and insert fresh
			if err := db.Where("prompt_id = ?", existing.ID).Delete(&PromptSlotVariantMapping{}).Error; err != nil {
				c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to update slots"})
				return
			}
			for _, sid := range newIDs {
				_ = db.Create(&PromptSlotVariantMapping{PromptID: existing.ID, SlotID: sid}).Error
			}
		}
		// Return full prompt
		reloaded, _ := loadPromptWithRelations(db, existing.ID)
		c.JSON(http.StatusOK, toPromptRead(db, reloaded))
	})

	grp.DELETE("/:id", func(c *gin.Context) {
		id, _ := strconv.Atoi(c.Param("id"))
		var existing Prompt
		if err := db.First(&existing, "id = ?", id).Error; err != nil {
			if errorsIsNotFound(err) {
				c.Status(http.StatusNoContent)
				return
			}
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to fetch prompt"})
			return
		}
		if existing.ExampleImageFilename != nil {
			safeDeletePublicImage(*existing.ExampleImageFilename, "prompt")
		}
		// Delete mappings first, then prompt
		_ = db.Where("prompt_id = ?", existing.ID).Delete(&PromptSlotVariantMapping{}).Error
		if err := db.Delete(&Prompt{}, existing.ID).Error; err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to delete prompt"})
			return
		}
		c.Status(http.StatusNoContent)
	})
}
