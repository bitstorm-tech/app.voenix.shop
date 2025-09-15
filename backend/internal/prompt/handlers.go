package prompt

import (
	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
)

// RegisterRoutes mounts prompt admin and public routes.
func RegisterRoutes(r *gin.Engine, db *gorm.DB) {
	svc := newService(db)
	// Admin
	registerAdminSlotTypeRoutes(r, db, svc)
	registerAdminSlotVariantRoutes(r, db, svc)
	registerAdminCategoryRoutes(r, db, svc)
	registerAdminSubCategoryRoutes(r, db, svc)
	registerAdminPromptRoutes(r, db, svc)

	// Public
	registerPublicPromptRoutes(r, svc)
}
