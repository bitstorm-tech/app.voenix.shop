package article

import "github.com/gin-gonic/gin"

// RegisterRoutes mounts admin + public article routes.
func RegisterRoutes(r *gin.Engine, adminMiddleware gin.HandlerFunc, svc *Service) {
	registerAdminCategoryRoutes(r, adminMiddleware, svc)
	registerAdminSubCategoryRoutes(r, adminMiddleware, svc)
	registerAdminArticleRoutes(r, adminMiddleware, svc)
	registerAdminMugVariantRoutes(r, adminMiddleware, svc)
	registerAdminShirtVariantRoutes(r, adminMiddleware, svc)
	registerPublicMugRoutes(r, svc)
}
