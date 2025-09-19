package cart

import (
	"net/http"
	"strconv"

	"github.com/gin-gonic/gin"
)

type addToCartRequest struct {
	ArticleID        int            `json:"articleId" binding:"required"`
	VariantID        int            `json:"variantId" binding:"required"`
	Quantity         int            `json:"quantity"`
	CustomData       map[string]any `json:"customData"`
	GeneratedImageID *int           `json:"generatedImageId"`
	PromptID         *int           `json:"promptId"`
}

type updateCartItemRequest struct {
	Quantity int `json:"quantity" binding:"required,min=1"`
}

func addItemHandler(svc *Service) gin.HandlerFunc {
	return func(c *gin.Context) {
		u, ok := requireUser(c)
		if !ok {
			return
		}

		var req addToCartRequest
		if err := c.ShouldBindJSON(&req); err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Invalid request"})
			return
		}

		input := AddItemInput{
			ArticleID:        req.ArticleID,
			VariantID:        req.VariantID,
			Quantity:         req.Quantity,
			CustomData:       req.CustomData,
			GeneratedImageID: req.GeneratedImageID,
			PromptID:         req.PromptID,
		}
		detail, err := svc.AddItem(c.Request.Context(), u.ID, input)
		if err != nil {
			writeServiceError(c, err)
			return
		}
		dto, err := svc.ToCartResponse(c.Request.Context(), detail)
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

		var req updateCartItemRequest
		if err := c.ShouldBindJSON(&req); err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Invalid request"})
			return
		}

		input := UpdateItemQuantityInput{ItemID: itemID, Quantity: req.Quantity}
		detail, err := svc.UpdateItemQuantity(c.Request.Context(), u.ID, input)
		if err != nil {
			writeServiceError(c, err)
			return
		}
		dto, err := svc.ToCartResponse(c.Request.Context(), detail)
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

		detail, err := svc.DeleteItem(c.Request.Context(), u.ID, itemID)
		if err != nil {
			writeServiceError(c, err)
			return
		}
		dto, err := svc.ToCartResponse(c.Request.Context(), detail)
		if err != nil {
			writeServiceError(c, err)
			return
		}
		c.JSON(http.StatusOK, dto)
	}
}
