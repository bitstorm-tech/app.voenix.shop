package cart

import (
	"net/http"

	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
)

func getCartHandler(db *gorm.DB, articleSvc ArticleService) gin.HandlerFunc {
	return func(c *gin.Context) {
		u, ok := requireUser(c)
		if !ok {
			return
		}
		cart, err := getOrCreateActiveCart(db, u.ID)
		if err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to load cart"})
			return
		}
		dto, err := assembleCartDto(c.Request.Context(), db, articleSvc, cart)
		if err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to assemble cart"})
			return
		}
		c.JSON(http.StatusOK, dto)
	}
}

func getCartSummaryHandler(db *gorm.DB) gin.HandlerFunc {
	return func(c *gin.Context) {
		u, ok := requireUser(c)
		if !ok {
			return
		}
		cart, err := loadActiveCart(db, u.ID)
		if err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to load cart"})
			return
		}
		if cart == nil || len(cart.Items) == 0 {
			c.JSON(http.StatusOK, CartSummaryDto{ItemCount: 0, TotalPrice: 0, HasItems: false})
			return
		}
		itemCount := 0
		total := 0
		for _, it := range cart.Items {
			itemCount += it.Quantity
			total += (it.PriceAtTime + it.PromptPriceAtTime) * it.Quantity
		}
		c.JSON(http.StatusOK, CartSummaryDto{ItemCount: itemCount, TotalPrice: total, HasItems: itemCount > 0})
	}
}

func clearCartHandler(db *gorm.DB, articleSvc ArticleService) gin.HandlerFunc {
	return func(c *gin.Context) {
		u, ok := requireUser(c)
		if !ok {
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
		if err := db.Where("cart_id = ?", cart.ID).Delete(&CartItem{}).Error; err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to clear cart"})
			return
		}
		_ = db.Preload("Items", withItemOrder).First(cart, cart.ID).Error
		dto, err := assembleCartDto(c.Request.Context(), db, articleSvc, cart)
		if err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to assemble cart"})
			return
		}
		c.JSON(http.StatusOK, dto)
	}
}

func refreshPricesHandler(db *gorm.DB, articleSvc ArticleService) gin.HandlerFunc {
	return func(c *gin.Context) {
		u, ok := requireUser(c)
		if !ok {
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
		changed := false
		for i := range cart.Items {
			articleCurrent, err := currentGrossPrice(c.Request.Context(), articleSvc, cart.Items[i].ArticleID)
			if err != nil {
				c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to refresh prices"})
				return
			}
			if cart.Items[i].OriginalPrice != articleCurrent {
				cart.Items[i].OriginalPrice = articleCurrent
				if err := db.Model(&cart.Items[i]).Update("original_price", articleCurrent).Error; err != nil {
					c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to update prices"})
					return
				}
				changed = true
			}
			promptCurrent := 0
			if cart.Items[i].PromptID != nil {
				pc, perr := promptCurrentGrossPrice(c.Request.Context(), db, articleSvc, *cart.Items[i].PromptID)
				if perr != nil {
					c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to refresh prompt prices"})
					return
				}
				promptCurrent = pc
			}
			if cart.Items[i].PromptOriginalPrice != promptCurrent {
				cart.Items[i].PromptOriginalPrice = promptCurrent
				if err := db.Model(&cart.Items[i]).Update("prompt_original_price", promptCurrent).Error; err != nil {
					c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to update prompt prices"})
					return
				}
				changed = true
			}
		}
		if changed {
			_ = db.Preload("Items", withItemOrder).First(cart, cart.ID).Error
		}
		dto, err := assembleCartDto(c.Request.Context(), db, articleSvc, cart)
		if err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to assemble cart"})
			return
		}
		c.JSON(http.StatusOK, dto)
	}
}
