package prompt

import (
	"net/http"
	"strconv"
	"strings"

	"github.com/gin-gonic/gin"
	"gorm.io/gorm"

	"voenix/backend-go/internal/auth"
)

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

func registerAdminSubCategoryRoutes(r *gin.Engine, db *gorm.DB) {
	grp := r.Group("/api/admin/prompts/subcategories")
	grp.Use(auth.RequireAdmin(db))

	grp.GET("/", func(c *gin.Context) {
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

	grp.GET("/category/:categoryId", func(c *gin.Context) {
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

	grp.POST("/", func(c *gin.Context) {
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

	grp.PUT("/:id", func(c *gin.Context) {
		var existing PromptSubCategory
		if err := db.First(&existing, "id = ?", c.Param("id")).Error; err != nil {
			if errorsIsNotFound(err) {
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

	grp.DELETE("/:id", func(c *gin.Context) {
		if err := db.Delete(&PromptSubCategory{}, "id = ?", c.Param("id")).Error; err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to delete subcategory"})
			return
		}
		c.Status(http.StatusNoContent)
	})
}
