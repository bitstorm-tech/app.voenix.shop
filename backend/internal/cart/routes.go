package cart

import (
	"net/http"

	"voenix/backend/internal/auth"

	"github.com/gin-gonic/gin"
)

// RegisterRoutes mounts user cart routes under /api/user/cart
func RegisterRoutes(r *gin.Engine, middleware gin.HandlerFunc, svc *Service) {
	grp := r.Group("/api/user/cart")

	grp.Use(middleware)

	grp.GET("", getCartHandler(svc))
	grp.GET("/summary", getCartSummaryHandler(svc))
	grp.POST("/items", addItemHandler(svc))
	grp.PUT("/items/:itemId", updateItemHandler(svc))
	grp.DELETE("/items/:itemId", deleteItemHandler(svc))
	grp.DELETE("", clearCartHandler(svc))
	grp.POST("/refresh-prices", refreshPricesHandler(svc))
}

// currentUser extracts the authenticated user from context.
func currentUser(c *gin.Context) *auth.User {
	uVal, _ := c.Get("currentUser")
	u, _ := uVal.(*auth.User)
	return u
}

// requireUser writes 401 if no user and returns ok=false.
func requireUser(c *gin.Context) (*auth.User, bool) {
	u := currentUser(c)
	if u == nil {
		c.JSON(http.StatusUnauthorized, gin.H{"detail": "Not authenticated"})
		return nil, false
	}
	return u, true
}
