package prompt

import (
	"net/http"

	"github.com/gin-gonic/gin"
)

func registerPublicPromptRoutes(r *gin.Engine, svc *Service) {
	grp := r.Group("/api/prompts")

	grp.GET("", func(c *gin.Context) {
		rows, err := svc.ListPublicPrompts(c.Request.Context())
		if err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to fetch prompts"})
			return
		}
		c.JSON(http.StatusOK, rows)
	})

	grp.GET("/batch", func(c *gin.Context) {
		// Support both ids=1,2 and repeated ids parameters
		ids := parseIDs(c.QueryArray("ids"), c.Query("ids"))
		rows, err := svc.BatchPromptSummaries(c.Request.Context(), ids)
		if err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to fetch prompts"})
			return
		}
		c.JSON(http.StatusOK, rows)
	})
}
