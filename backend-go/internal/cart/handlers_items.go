package cart

import (
	"encoding/json"
	"net/http"
	"strconv"

	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
)

func addItemHandler(db *gorm.DB) gin.HandlerFunc {
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
		if req.Quantity <= 0 {
			req.Quantity = 1
		}
		if req.CustomData == nil {
			req.CustomData = map[string]any{}
		}
		if err := validateArticleAndVariant(db, req.ArticleID, req.VariantID); err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"detail": err.Error()})
			return
		}
		if err := validatePromptIfProvided(db, req.PromptID); err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"detail": err.Error()})
			return
		}
		cart, err := getOrCreateActiveCart(db, u.ID)
		if err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to load cart"})
			return
		}
		price, err := currentGrossPrice(db, req.ArticleID)
		if err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to fetch price"})
			return
		}
		cdStr := "{}"
		if req.CustomData != nil {
			if b, err := json.Marshal(req.CustomData); err == nil {
				cdStr = string(b)
			}
		}
		item := CartItem{
			CartID:           cart.ID,
			ArticleID:        req.ArticleID,
			VariantID:        req.VariantID,
			Quantity:         req.Quantity,
			PriceAtTime:      price,
			OriginalPrice:    price,
			CustomData:       cdStr,
			GeneratedImageID: req.GeneratedImageID,
			PromptID:         req.PromptID,
		}
		mergeOrAppendItem(cart, item)
		if err := db.Save(cart).Error; err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Cart update failed"})
			return
		}
		_ = db.Preload("Items", withItemOrder).First(cart, cart.ID).Error
		dto, err := assembleCartDto(db, cart)
		if err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to assemble cart"})
			return
		}
		c.JSON(http.StatusCreated, dto)
	}
}

func updateItemHandler(db *gorm.DB) gin.HandlerFunc {
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
		if req.Quantity <= 0 {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Quantity must be at least 1"})
			return
		}
		cart, err := loadActiveCart(db, u.ID)
		if err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to load cart"})
			return
		}
		if cart == nil {
			c.JSON(http.StatusNotFound, gin.H{"detail": "Active cart not found"})
			return
		}
		found := false
		for i := range cart.Items {
			if cart.Items[i].ID == itemID {
				cart.Items[i].Quantity = req.Quantity
				found = true
				break
			}
		}
		if !found {
			c.JSON(http.StatusNotFound, gin.H{"detail": "Cart item not found"})
			return
		}
		if err := db.Model(&CartItem{}).
			Where("id = ? AND cart_id = ?", itemID, cart.ID).
			Update("quantity", req.Quantity).Error; err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Cart update failed"})
			return
		}
		_ = db.Preload("Items", withItemOrder).First(cart, cart.ID).Error
		dto, err := assembleCartDto(db, cart)
		if err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to assemble cart"})
			return
		}
		c.JSON(http.StatusOK, dto)
	}
}

func deleteItemHandler(db *gorm.DB) gin.HandlerFunc {
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
		cart, err := loadActiveCart(db, u.ID)
		if err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to load cart"})
			return
		}
		if cart == nil {
			c.JSON(http.StatusNotFound, gin.H{"detail": "Active cart not found"})
			return
		}
		removed := false
		for i := range cart.Items {
			if cart.Items[i].ID == itemID {
				if err := db.Delete(&CartItem{}, cart.Items[i].ID).Error; err != nil {
					c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to remove item"})
					return
				}
				removed = true
				break
			}
		}
		if !removed {
			c.JSON(http.StatusNotFound, gin.H{"detail": "Cart item not found"})
			return
		}
		var items []CartItem
		_ = db.Where("cart_id = ?", cart.ID).Order("position asc, created_at asc").Find(&items).Error
		for idx := range items {
			if items[idx].Position != idx {
				items[idx].Position = idx
				_ = db.Model(&items[idx]).Update("position", idx).Error
			}
		}
		_ = db.Preload("Items", withItemOrder).First(cart, cart.ID).Error
		dto, err := assembleCartDto(db, cart)
		if err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to assemble cart"})
			return
		}
		c.JSON(http.StatusOK, dto)
	}
}
