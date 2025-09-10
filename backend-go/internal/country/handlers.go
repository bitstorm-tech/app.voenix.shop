package country

import (
	"net/http"

	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
)

// RegisterRoutes mounts public country routes under /api/public/countries.
func RegisterRoutes(r *gin.Engine, db *gorm.DB) {
	grp := r.Group("/api/public/countries")

	// GET /api/public/countries -> list all countries (public)
	grp.GET("/", func(c *gin.Context) {
		var rows []Country
		if err := db.Find(&rows).Error; err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to fetch countries"})
			return
		}
		c.JSON(http.StatusOK, rows)
	})
}
