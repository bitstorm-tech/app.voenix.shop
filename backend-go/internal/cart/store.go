package cart

import (
	"errors"
	"time"

	"gorm.io/gorm"

	"voenix/backend-go/internal/article"
)

const defaultCartExpiryDays = 30

// getOrCreateActiveCart returns the active cart for user or creates one.
func getOrCreateActiveCart(db *gorm.DB, userID int) (*Cart, error) {
	var c Cart
	err := db.Preload("Items", withItemOrder).
		Where("user_id = ? AND status = ?", userID, string(CartStatusActive)).
		First(&c).Error
	if err == nil {
		return &c, nil
	}
	if !errors.Is(err, gorm.ErrRecordNotFound) {
		return nil, err
	}
	exp := time.Now().Add(defaultCartExpiryDays * 24 * time.Hour)
	c = Cart{UserID: userID, Status: string(CartStatusActive), ExpiresAt: &exp}
	if err := db.Create(&c).Error; err != nil {
		return nil, err
	}
	return &c, nil
}

// loadActiveCart loads the existing active cart for user (with items), or returns nil if not found.
func loadActiveCart(db *gorm.DB, userID int) (*Cart, error) {
	var c Cart
	err := db.Preload("Items", withItemOrder).
		Where("user_id = ? AND status = ?", userID, string(CartStatusActive)).
		First(&c).Error
	if err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, nil
		}
		return nil, err
	}
	return &c, nil
}

// currentGrossPrice returns SalesTotalGross (cents) for articleID, or 0 if not found.
func currentGrossPrice(db *gorm.DB, articleID int) (int, error) {
	var cc article.CostCalculation
	if err := db.First(&cc, "article_id = ?", articleID).Error; err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return 0, nil
		}
		return 0, err
	}
	return cc.SalesTotalGross, nil
}

// withItemOrder applies a consistent ordering for preloading items.
func withItemOrder(tx *gorm.DB) *gorm.DB { return tx.Order("position asc, created_at asc") }
