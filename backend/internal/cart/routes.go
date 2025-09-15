package cart

import (
	"net/http"

	"github.com/gin-gonic/gin"
	"gorm.io/gorm"

	"voenix/backend/internal/auth"
)

// RegisterRoutes mounts user cart routes under /api/user/cart
func RegisterRoutes(r *gin.Engine, db *gorm.DB) {
	grp := r.Group("/api/user/cart")
	grp.Use(auth.RequireRoles(db, "USER", "ADMIN"))

	grp.GET("", getCartHandler(db))
	grp.GET("/summary", getCartSummaryHandler(db))
	grp.POST("/items", addItemHandler(db))
	grp.PUT("/items/:itemId", updateItemHandler(db))
	grp.DELETE("/items/:itemId", deleteItemHandler(db))
	grp.DELETE("", clearCartHandler(db))
	grp.POST("/refresh-prices", refreshPricesHandler(db))
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
