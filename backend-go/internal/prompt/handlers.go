package prompt

import (
	"errors"
	"net/http"
	"os"
	"path/filepath"
	"strconv"
	"strings"
	"time"

	"github.com/gin-gonic/gin"
	"gorm.io/gorm"

	"voenix/backend-go/internal/auth"
	img "voenix/backend-go/internal/image"
)

// RegisterRoutes mounts prompt admin and public routes.
func RegisterRoutes(r *gin.Engine, db *gorm.DB) {
	// ----- Admin: Slot Types
	adminSlotTypes := r.Group("/api/admin/prompts/prompt-slot-types")
	adminSlotTypes.Use(auth.RequireAdmin(db))

	adminSlotTypes.GET("/", func(c *gin.Context) {
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

	adminSlotTypes.GET("/:id", func(c *gin.Context) {
		var row PromptSlotType
		if err := db.First(&row, "id = ?", c.Param("id")).Error; err != nil {
			if errors.Is(err, gorm.ErrRecordNotFound) {
				c.JSON(http.StatusNotFound, gin.H{"detail": "PromptSlotType not found"})
				return
			}
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to fetch slot type"})
			return
		}
		c.JSON(http.StatusOK, toSlotTypeRead(&row))
	})

	type slotTypeCreate struct {
		Name     string `json:"name"`
		Position int    `json:"position"`
	}
	type slotTypeUpdate struct {
		Name     *string `json:"name"`
		Position *int    `json:"position"`
	}

	adminSlotTypes.POST("/", func(c *gin.Context) {
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

	adminSlotTypes.PUT("/:id", func(c *gin.Context) {
		id := c.Param("id")
		var existing PromptSlotType
		if err := db.First(&existing, "id = ?", id).Error; err != nil {
			if errors.Is(err, gorm.ErrRecordNotFound) {
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

	adminSlotTypes.DELETE("/:id", func(c *gin.Context) {
		if err := db.Delete(&PromptSlotType{}, "id = ?", c.Param("id")).Error; err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to delete slot type"})
			return
		}
		c.Status(http.StatusNoContent)
	})

	// ----- Admin: Slot Variants
	adminSlotVariants := r.Group("/api/admin/prompts/slot-variants")
	adminSlotVariants.Use(auth.RequireAdmin(db))

	adminSlotVariants.GET("/", func(c *gin.Context) {
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

	adminSlotVariants.GET("/:id", func(c *gin.Context) {
		var row PromptSlotVariant
		if err := db.Preload("PromptSlotType").First(&row, "id = ?", c.Param("id")).Error; err != nil {
			if errors.Is(err, gorm.ErrRecordNotFound) {
				c.JSON(http.StatusNotFound, gin.H{"detail": "PromptSlotVariant not found"})
				return
			}
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to fetch slot variant"})
			return
		}
		c.JSON(http.StatusOK, toSlotVariantRead(&row))
	})

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

	adminSlotVariants.POST("/", func(c *gin.Context) {
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

	adminSlotVariants.PUT("/:id", func(c *gin.Context) {
		var existing PromptSlotVariant
		if err := db.First(&existing, "id = ?", c.Param("id")).Error; err != nil {
			if errors.Is(err, gorm.ErrRecordNotFound) {
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

	adminSlotVariants.DELETE("/:id", func(c *gin.Context) {
		var existing PromptSlotVariant
		if err := db.First(&existing, "id = ?", c.Param("id")).Error; err != nil {
			if errors.Is(err, gorm.ErrRecordNotFound) {
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

	// ----- Admin: Categories
	adminCategories := r.Group("/api/admin/prompts/categories")
	adminCategories.Use(auth.RequireAdmin(db))

	adminCategories.GET("/", func(c *gin.Context) {
		var rows []PromptCategory
		if err := db.Order("id desc").Find(&rows).Error; err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to fetch categories"})
			return
		}
		out := make([]PromptCategoryRead, 0, len(rows))
		for i := range rows {
			pc := rows[i]
			promptsCount := countPromptsByCategory(db, pc.ID)
			subcatsCount := countSubCategoriesByCategory(db, pc.ID)
			out = append(out, PromptCategoryRead{
				ID: pc.ID, Name: pc.Name,
				PromptsCount: promptsCount, SubcategoriesCount: subcatsCount,
				CreatedAt: timePtr(pc.CreatedAt), UpdatedAt: timePtr(pc.UpdatedAt),
			})
		}
		c.JSON(http.StatusOK, out)
	})

	type categoryCreate struct {
		Name string `json:"name"`
	}
	type categoryUpdate struct {
		Name *string `json:"name"`
	}

	adminCategories.POST("/", func(c *gin.Context) {
		var payload categoryCreate
		if err := c.ShouldBindJSON(&payload); err != nil || strings.TrimSpace(payload.Name) == "" {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Invalid payload"})
			return
		}
		row := PromptCategory{Name: payload.Name}
		if err := db.Create(&row).Error; err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to create category"})
			return
		}
		c.JSON(http.StatusCreated, PromptCategoryRead{
			ID: row.ID, Name: row.Name,
			PromptsCount: 0, SubcategoriesCount: 0,
			CreatedAt: timePtr(row.CreatedAt), UpdatedAt: timePtr(row.UpdatedAt),
		})
	})

	adminCategories.PUT("/:id", func(c *gin.Context) {
		var existing PromptCategory
		if err := db.First(&existing, "id = ?", c.Param("id")).Error; err != nil {
			if errors.Is(err, gorm.ErrRecordNotFound) {
				c.JSON(http.StatusNotFound, gin.H{"detail": "PromptCategory not found"})
				return
			}
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to fetch category"})
			return
		}
		var payload categoryUpdate
		if err := c.ShouldBindJSON(&payload); err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Invalid payload"})
			return
		}
		if payload.Name != nil {
			existing.Name = *payload.Name
		}
		if err := db.Save(&existing).Error; err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to update category"})
			return
		}
		c.JSON(http.StatusOK, PromptCategoryRead{
			ID: existing.ID, Name: existing.Name,
			PromptsCount:       countPromptsByCategory(db, existing.ID),
			SubcategoriesCount: countSubCategoriesByCategory(db, existing.ID),
			CreatedAt:          timePtr(existing.CreatedAt), UpdatedAt: timePtr(existing.UpdatedAt),
		})
	})

	adminCategories.DELETE("/:id", func(c *gin.Context) {
		// Ensure exists for consistent semantics
		var existing PromptCategory
		if err := db.First(&existing, "id = ?", c.Param("id")).Error; err != nil {
			if errors.Is(err, gorm.ErrRecordNotFound) {
				c.Status(http.StatusNoContent)
				return
			}
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to fetch category"})
			return
		}
		if err := db.Delete(&PromptCategory{}, existing.ID).Error; err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to delete category"})
			return
		}
		c.Status(http.StatusNoContent)
	})

	// ----- Admin: SubCategories
	adminSubcats := r.Group("/api/admin/prompts/subcategories")
	adminSubcats.Use(auth.RequireAdmin(db))

	adminSubcats.GET("/", func(c *gin.Context) {
		var rows []PromptSubCategory
		if err := db.Order("id desc").Find(&rows).Error; err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to fetch subcategories"})
			return
		}
		out := make([]PromptSubCategoryRead, 0, len(rows))
		for i := range rows {
			sc := rows[i]
			out = append(out, toSubCategoryRead(db, &sc))
		}
		c.JSON(http.StatusOK, out)
	})

	adminSubcats.GET("/category/:categoryId", func(c *gin.Context) {
		cid, _ := strconv.Atoi(c.Param("categoryId"))
		var rows []PromptSubCategory
		if err := db.Where("prompt_category_id = ?", cid).Order("id desc").Find(&rows).Error; err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to fetch subcategories"})
			return
		}
		out := make([]PromptSubCategoryRead, 0, len(rows))
		for i := range rows {
			sc := rows[i]
			out = append(out, toSubCategoryRead(db, &sc))
		}
		c.JSON(http.StatusOK, out)
	})

	type subcatCreate struct {
		PromptCategoryID int     `json:"promptCategoryId"`
		Name             string  `json:"name"`
		Description      *string `json:"description"`
	}
	type subcatUpdate struct {
		PromptCategoryID *int    `json:"promptCategoryId"`
		Name             *string `json:"name"`
		Description      *string `json:"description"`
	}

	adminSubcats.POST("/", func(c *gin.Context) {
		var payload subcatCreate
		if err := c.ShouldBindJSON(&payload); err != nil || payload.PromptCategoryID <= 0 || strings.TrimSpace(payload.Name) == "" {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Invalid payload"})
			return
		}
		if !existsByID[PromptCategory](db, payload.PromptCategoryID) {
			c.JSON(http.StatusNotFound, gin.H{"detail": "PromptCategory not found"})
			return
		}
		row := PromptSubCategory{PromptCategoryID: payload.PromptCategoryID, Name: payload.Name, Description: payload.Description}
		if err := db.Create(&row).Error; err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to create subcategory"})
			return
		}
		c.JSON(http.StatusCreated, toSubCategoryRead(db, &row))
	})

	adminSubcats.PUT("/:id", func(c *gin.Context) {
		var existing PromptSubCategory
		if err := db.First(&existing, "id = ?", c.Param("id")).Error; err != nil {
			if errors.Is(err, gorm.ErrRecordNotFound) {
				c.JSON(http.StatusNotFound, gin.H{"detail": "PromptSubCategory not found"})
				return
			}
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to fetch subcategory"})
			return
		}
		var payload subcatUpdate
		if err := c.ShouldBindJSON(&payload); err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Invalid payload"})
			return
		}
		if payload.PromptCategoryID != nil {
			if !existsByID[PromptCategory](db, *payload.PromptCategoryID) {
				c.JSON(http.StatusNotFound, gin.H{"detail": "PromptCategory not found"})
				return
			}
			existing.PromptCategoryID = *payload.PromptCategoryID
		}
		if payload.Name != nil {
			existing.Name = *payload.Name
		}
		if payload.Description != nil {
			existing.Description = payload.Description
		}
		if err := db.Save(&existing).Error; err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to update subcategory"})
			return
		}
		c.JSON(http.StatusOK, toSubCategoryRead(db, &existing))
	})

	adminSubcats.DELETE("/:id", func(c *gin.Context) {
		if err := db.Delete(&PromptSubCategory{}, "id = ?", c.Param("id")).Error; err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to delete subcategory"})
			return
		}
		c.Status(http.StatusNoContent)
	})

	// ----- Admin: Prompts
	adminPrompts := r.Group("/api/admin/prompts")
	adminPrompts.Use(auth.RequireAdmin(db))

	adminPrompts.GET("/", func(c *gin.Context) {
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

	adminPrompts.GET("/:id", func(c *gin.Context) {
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

	adminPrompts.POST("/", func(c *gin.Context) {
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
				if errors.Is(err, gorm.ErrRecordNotFound) {
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

	adminPrompts.PUT("/:id", func(c *gin.Context) {
		id, _ := strconv.Atoi(c.Param("id"))
		var existing Prompt
		if err := db.First(&existing, "id = ?", id).Error; err != nil {
			if errors.Is(err, gorm.ErrRecordNotFound) {
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
				if errors.Is(err, gorm.ErrRecordNotFound) {
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

	adminPrompts.DELETE("/:id", func(c *gin.Context) {
		id, _ := strconv.Atoi(c.Param("id"))
		var existing Prompt
		if err := db.First(&existing, "id = ?", id).Error; err != nil {
			if errors.Is(err, gorm.ErrRecordNotFound) {
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

	// ----- Public
	public := r.Group("/api/prompts")

	public.GET("/", func(c *gin.Context) {
		// Active prompts only
		var rows []Prompt
		if err := db.Where("active = ?", true).
			Preload("Category").
			Preload("Subcategory").
			Preload("PromptSlotVariantMappings").
			Preload("PromptSlotVariantMappings.PromptSlotVariant").
			Preload("PromptSlotVariantMappings.PromptSlotVariant.PromptSlotType").
			Order("id desc").
			Find(&rows).Error; err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to fetch prompts"})
			return
		}
		out := make([]PublicPromptRead, 0, len(rows))
		for i := range rows {
			out = append(out, toPublicPromptRead(&rows[i]))
		}
		c.JSON(http.StatusOK, out)
	})

	public.GET("/batch", func(c *gin.Context) {
		// Support both ids=1,2 and repeated ids parameters
		ids := parseIDs(c.QueryArray("ids"), c.Query("ids"))
		if len(ids) == 0 {
			c.JSON(http.StatusOK, []PromptSummaryRead{})
			return
		}
		var rows []Prompt
		if err := db.Where("id IN ?", ids).Find(&rows).Error; err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to fetch prompts"})
			return
		}
		out := make([]PromptSummaryRead, 0, len(rows))
		for i := range rows {
			out = append(out, PromptSummaryRead{ID: rows[i].ID, Title: rows[i].Title})
		}
		c.JSON(http.StatusOK, out)
	})
}

// Payload type for prompt slot references in create/update payloads.
type promptSlotRef struct {
	SlotID int `json:"slotId"`
}

// -----------------------------
// Helpers / assemblers
// -----------------------------

func toSlotTypeRead(t *PromptSlotType) PromptSlotTypeRead {
	return PromptSlotTypeRead{
		ID: t.ID, Name: t.Name, Position: t.Position,
		CreatedAt: timePtr(t.CreatedAt), UpdatedAt: timePtr(t.UpdatedAt),
	}
}

func toSlotVariantRead(v *PromptSlotVariant) PromptSlotVariantRead {
	var st *PromptSlotTypeRead
	if v.PromptSlotType != nil {
		tmp := toSlotTypeRead(v.PromptSlotType)
		st = &tmp
	}
	return PromptSlotVariantRead{
		ID:               v.ID,
		PromptSlotTypeID: v.PromptSlotTypeID,
		PromptSlotType:   st,
		Name:             v.Name,
		Prompt:           v.Prompt,
		Description:      v.Description,
		ExampleImageURL:  strPtrOrNil(publicSlotVariantExampleURL(v.ExampleImageFilename)),
		CreatedAt:        timePtr(v.CreatedAt),
		UpdatedAt:        timePtr(v.UpdatedAt),
	}
}

func toPromptRead(db *gorm.DB, p *Prompt) PromptRead {
	var cat *PromptCategoryRead
	if p.Category != nil {
		pc := p.Category
		cat = &PromptCategoryRead{
			ID: pc.ID, Name: pc.Name,
			PromptsCount: 0, SubcategoriesCount: 0, // align with Kotlin assembler (no counts here)
			CreatedAt: timePtr(pc.CreatedAt), UpdatedAt: timePtr(pc.UpdatedAt),
		}
	}
	var subcat *PromptSubCategoryRead
	if p.Subcategory != nil {
		sc := p.Subcategory
		subcat = &PromptSubCategoryRead{
			ID: sc.ID, PromptCategoryID: sc.PromptCategoryID,
			Name: sc.Name, Description: sc.Description,
			PromptsCount: 0,
			CreatedAt:    timePtr(sc.CreatedAt), UpdatedAt: timePtr(sc.UpdatedAt),
		}
	}

	slots := make([]PromptSlotVariantRead, 0, len(p.PromptSlotVariantMappings))
	for i := range p.PromptSlotVariantMappings {
		m := p.PromptSlotVariantMappings[i]
		if m.PromptSlotVariant != nil {
			slots = append(slots, toSlotVariantRead(m.PromptSlotVariant))
		}
	}
	return PromptRead{
		ID:              p.ID,
		Title:           p.Title,
		PromptText:      p.PromptText,
		CategoryID:      p.CategoryID,
		Category:        cat,
		SubcategoryID:   p.SubcategoryID,
		Subcategory:     subcat,
		Active:          p.Active,
		Slots:           slots,
		ExampleImageURL: strPtrOrNil(publicPromptExampleURL(p.ExampleImageFilename)),
		CreatedAt:       timePtr(p.CreatedAt),
		UpdatedAt:       timePtr(p.UpdatedAt),
	}
}

func toPublicPromptRead(p *Prompt) PublicPromptRead {
	var cat *PublicPromptCategoryRead
	if p.Category != nil {
		cat = &PublicPromptCategoryRead{ID: p.Category.ID, Name: p.Category.Name}
	}
	var subcat *PublicPromptSubCategoryRead
	if p.Subcategory != nil {
		subcat = &PublicPromptSubCategoryRead{ID: p.Subcategory.ID, Name: p.Subcategory.Name, Description: p.Subcategory.Description}
	}
	slots := make([]PublicPromptSlotRead, 0, len(p.PromptSlotVariantMappings))
	for i := range p.PromptSlotVariantMappings {
		m := p.PromptSlotVariantMappings[i]
		v := m.PromptSlotVariant
		if v == nil {
			continue
		}
		var st *PublicPromptSlotTypeRead
		if v.PromptSlotType != nil {
			st = &PublicPromptSlotTypeRead{ID: v.PromptSlotType.ID, Name: v.PromptSlotType.Name, Position: v.PromptSlotType.Position}
		}
		slots = append(slots, PublicPromptSlotRead{
			ID:              v.ID,
			Name:            v.Name,
			Description:     v.Description,
			ExampleImageURL: strPtrOrNil(publicSlotVariantExampleURL(v.ExampleImageFilename)),
			SlotType:        st,
		})
	}
	return PublicPromptRead{
		ID:              p.ID,
		Title:           p.Title,
		ExampleImageURL: strPtrOrNil(publicPromptExampleURL(p.ExampleImageFilename)),
		Category:        cat,
		Subcategory:     subcat,
		Slots:           slots,
	}
}

func loadPromptWithRelations(db *gorm.DB, id int) (*Prompt, error) {
	var row Prompt
	err := db.Where("id = ?", id).
		Preload("Category").
		Preload("Subcategory").
		Preload("PromptSlotVariantMappings").
		Preload("PromptSlotVariantMappings.PromptSlotVariant").
		Preload("PromptSlotVariantMappings.PromptSlotVariant.PromptSlotType").
		First(&row).Error
	if err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, nil
		}
		return nil, err
	}
	return &row, nil
}

func allPromptsWithRelations(db *gorm.DB) ([]Prompt, error) {
	var rows []Prompt
	err := db.
		Preload("Category").
		Preload("Subcategory").
		Preload("PromptSlotVariantMappings").
		Preload("PromptSlotVariantMappings.PromptSlotVariant").
		Preload("PromptSlotVariantMappings.PromptSlotVariant.PromptSlotType").
		Order("id desc").
		Find(&rows).Error
	return rows, err
}

func countPromptsByCategory(db *gorm.DB, categoryID int) int {
	var cnt int64
	db.Model(&Prompt{}).Where("category_id = ?", categoryID).Count(&cnt)
	return int(cnt)
}

func countSubCategoriesByCategory(db *gorm.DB, categoryID int) int {
	var cnt int64
	db.Model(&PromptSubCategory{}).Where("prompt_category_id = ?", categoryID).Count(&cnt)
	return int(cnt)
}

func countPromptsBySubcategory(db *gorm.DB, subcategoryID int) int {
	var cnt int64
	db.Model(&Prompt{}).Where("subcategory_id = ?", subcategoryID).Count(&cnt)
	return int(cnt)
}

func toSubCategoryRead(db *gorm.DB, sc *PromptSubCategory) PromptSubCategoryRead {
	return PromptSubCategoryRead{
		ID: sc.ID, PromptCategoryID: sc.PromptCategoryID,
		Name: sc.Name, Description: sc.Description,
		PromptsCount: countPromptsBySubcategory(db, sc.ID),
		CreatedAt:    timePtr(sc.CreatedAt), UpdatedAt: timePtr(sc.UpdatedAt),
	}
}

// Utility: parse ids=1,2 or repeated ids params
func parseIDs(repeated []string, commaSep string) []int {
	vals := []string{}
	for _, v := range repeated {
		if v != "" {
			vals = append(vals, v)
		}
	}
	if commaSep != "" {
		parts := strings.Split(commaSep, ",")
		for _, p := range parts {
			if strings.TrimSpace(p) != "" {
				vals = append(vals, strings.TrimSpace(p))
			}
		}
	}
	uniq := map[int]struct{}{}
	out := []int{}
	for _, v := range vals {
		if n, err := strconv.Atoi(v); err == nil {
			if _, ok := uniq[n]; !ok {
				uniq[n] = struct{}{}
				out = append(out, n)
			}
		}
	}
	return out
}

func uniqueSlotIDs(slots []promptSlotRef) []int {
	seen := map[int]struct{}{}
	out := make([]int, 0, len(slots))
	for _, s := range slots {
		if s.SlotID <= 0 {
			continue
		}
		if _, ok := seen[s.SlotID]; !ok {
			seen[s.SlotID] = struct{}{}
			out = append(out, s.SlotID)
		}
	}
	return out
}

// Generic exists by id helper
func existsByID[T any](db *gorm.DB, id int) bool {
	var cnt int64
	db.Model(new(T)).Where("id = ?", id).Count(&cnt)
	return cnt > 0
}

func publicPromptExampleURL(filename *string) string {
	if filename == nil || *filename == "" {
		return ""
	}
	// Match Python/Kotlin: /public/images/prompt-example-images/{filename}
	return "/public/images/prompt-example-images/" + filepath.Base(*filename)
}

func publicSlotVariantExampleURL(filename *string) string {
	if filename == nil || *filename == "" {
		return ""
	}
	return "/public/images/prompt-slot-variant-example-images/" + filepath.Base(*filename)
}

// Delete a public image (best-effort). kind: "prompt" | "slot-variant"
func safeDeletePublicImage(filename, kind string) {
	loc, err := img.NewStorageLocations()
	if err != nil || strings.TrimSpace(filename) == "" {
		return
	}
	var dir string
	switch kind {
	case "slot-variant":
		dir = loc.PromptSlotVariantExample()
	default:
		dir = loc.PromptExample()
	}
	path := filepath.Join(dir, filepath.Base(filename))
	_ = os.Remove(path)
}

func timePtr(t time.Time) *time.Time { return &t }
func strPtrOrNil(s string) *string {
	if s == "" {
		return nil
	}
	return &s
}
