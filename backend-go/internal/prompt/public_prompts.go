package prompt

import (
	"net/http"

	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
)

func registerPublicPromptRoutes(r *gin.Engine, db *gorm.DB) {
	grp := r.Group("/api/prompts")

	grp.GET("/", func(c *gin.Context) {
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

	grp.GET("/batch", func(c *gin.Context) {
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
