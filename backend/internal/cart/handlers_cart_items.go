package cart

import (
	"net/http"
	"strconv"

	"github.com/gin-gonic/gin"
)

func addItemHandler(svc *Service) gin.HandlerFunc {
	return func(c *gin.Context) {
		u, ok := requireUser(c)
		if !ok {
			return
		}

		var req AddToCartRequest
		if err := c.ShouldBindJSON(&req); err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Invalid request"})
			return
		}

		dto, err := svc.AddItem(c.Request.Context(), u.ID, req)
		if err != nil {
			writeServiceError(c, err)
			return
		}
		c.JSON(http.StatusCreated, dto)
	}
}

func updateItemHandler(svc *Service) gin.HandlerFunc {
	return func(c *gin.Context) {
		u, ok := requireUser(c)
		if !ok {
			return
		}

		itemID, err := strconv.Atoi(c.Param("itemId"))
		if err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Invalid item id"})
			return
		}

		var req UpdateCartItemRequest
		if err := c.ShouldBindJSON(&req); err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Invalid request"})
			return
		}

		dto, err := svc.UpdateItemQuantity(c.Request.Context(), u.ID, itemID, req.Quantity)
		if err != nil {
			writeServiceError(c, err)
			return
		}
		c.JSON(http.StatusOK, dto)
	}
}

func deleteItemHandler(svc *Service) gin.HandlerFunc {
	return func(c *gin.Context) {
		u, ok := requireUser(c)
		if !ok {
			return
		}

		itemID, err := strconv.Atoi(c.Param("itemId"))
		if err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Invalid item id"})
			return
		}

		dto, err := svc.DeleteItem(c.Request.Context(), u.ID, itemID)
		if err != nil {
			writeServiceError(c, err)
			return
		}
		c.JSON(http.StatusOK, dto)
	}
}
