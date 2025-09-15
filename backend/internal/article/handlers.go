package article

import (
	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
)

// RegisterRoutes mounts admin + public article routes.
func RegisterRoutes(r *gin.Engine, db *gorm.DB) {
	registerAdminCategoryRoutes(r, db)
	registerAdminSubCategoryRoutes(r, db)
	registerAdminArticleRoutes(r, db)
	registerAdminMugVariantRoutes(r, db)
	registerAdminShirtVariantRoutes(r, db)
	registerPublicMugRoutes(r, db)
}
