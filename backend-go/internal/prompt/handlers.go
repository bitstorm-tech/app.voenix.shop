package prompt

import (
	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
)

// RegisterRoutes mounts prompt admin and public routes.
func RegisterRoutes(r *gin.Engine, db *gorm.DB) {
	// Admin
	registerAdminSlotTypeRoutes(r, db)
	registerAdminSlotVariantRoutes(r, db)
	registerAdminCategoryRoutes(r, db)
	registerAdminSubCategoryRoutes(r, db)
	registerAdminPromptRoutes(r, db)

	// Public
	registerPublicPromptRoutes(r, db)
}
