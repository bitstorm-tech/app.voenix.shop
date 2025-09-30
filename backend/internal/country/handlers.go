package country

import (
	"net/http"
	"time"

	"github.com/gin-gonic/gin"
)

type listCountriesRequest struct{}

type countryResponse struct {
	ID        int       `json:"id"`
	Name      string    `json:"name"`
	CreatedAt time.Time `json:"createdAt"`
	UpdatedAt time.Time `json:"updatedAt"`
}

// RegisterRoutes mounts public country routes under /api/countries.
func RegisterRoutes(r *gin.Engine, svc *Service) {
	grp := r.Group("/api/countries")

	// GET /api/countries -> list all countries (public)
	grp.GET("", func(c *gin.Context) {
		var req listCountriesRequest
		if err := c.ShouldBindQuery(&req); err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Invalid query"})
			return
		}

		countries, err := svc.All(c.Request.Context())
		if err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to fetch countries"})
			return
		}

		resp := make([]countryResponse, len(countries))
		for i := range countries {
			resp[i] = countryResponse(countries[i])
		}

		c.JSON(http.StatusOK, resp)
	})
}
