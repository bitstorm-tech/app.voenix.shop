package cart

import (
	"errors"
	"net/http"

	"github.com/gin-gonic/gin"
)

func getCartHandler(svc *Service) gin.HandlerFunc {
	return func(c *gin.Context) {
		u, ok := requireUser(c)
		if !ok {
			return
		}
		dto, err := svc.GetCart(c.Request.Context(), u.ID)
		if err != nil {
			writeServiceError(c, err)
			return
		}
		c.JSON(http.StatusOK, dto)
	}
}

func getCartSummaryHandler(svc *Service) gin.HandlerFunc {
	return func(c *gin.Context) {
		u, ok := requireUser(c)
		if !ok {
			return
		}
		summary, err := svc.GetCartSummary(c.Request.Context(), u.ID)
		if err != nil {
			writeServiceError(c, err)
			return
		}
		c.JSON(http.StatusOK, summary)
	}
}

func clearCartHandler(svc *Service) gin.HandlerFunc {
	return func(c *gin.Context) {
		u, ok := requireUser(c)
		if !ok {
			return
		}
		dto, err := svc.ClearCart(c.Request.Context(), u.ID)
		if err != nil {
			writeServiceError(c, err)
			return
		}
		c.JSON(http.StatusOK, dto)
	}
}

func refreshPricesHandler(svc *Service) gin.HandlerFunc {
	return func(c *gin.Context) {
		u, ok := requireUser(c)
		if !ok {
			return
		}
		dto, err := svc.RefreshPrices(c.Request.Context(), u.ID)
		if err != nil {
			writeServiceError(c, err)
			return
		}
		c.JSON(http.StatusOK, dto)
	}
}

func writeServiceError(c *gin.Context, err error) {
	switch {
	case errors.Is(err, ErrCartNotFound):
		c.JSON(http.StatusNotFound, gin.H{"detail": ErrCartNotFound.Error()})
	case errors.Is(err, ErrCartItemNotFound):
		c.JSON(http.StatusNotFound, gin.H{"detail": ErrCartItemNotFound.Error()})
	case isValidationError(err):
		c.JSON(http.StatusBadRequest, gin.H{"detail": err.Error()})
	default:
		c.JSON(http.StatusInternalServerError, gin.H{"detail": "Unexpected error"})
	}
}
