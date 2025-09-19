package postgres

import (
	"time"

	"voenix/backend/internal/cart"
)

type CartRow struct {
	ID        int           `gorm:"primaryKey"`
	UserID    int           `gorm:"column:user_id;not null"`
	Status    string        `gorm:"size:20;not null"`
	Version   int64         `gorm:"column:version;not null;default:0;version"`
	ExpiresAt *time.Time    `gorm:"column:expires_at"`
	Items     []CartItemRow `gorm:"foreignKey:CartID;references:ID"`
	CreatedAt time.Time
	UpdatedAt time.Time
}

func (CartRow) TableName() string { return "carts" }

type CartItemRow struct {
	ID                  int    `gorm:"primaryKey"`
	CartID              int    `gorm:"column:cart_id;not null"`
	ArticleID           int    `gorm:"column:article_id;not null"`
	VariantID           int    `gorm:"column:variant_id;not null"`
	Quantity            int    `gorm:"not null"`
	PriceAtTime         int    `gorm:"column:price_at_time;not null"`
	OriginalPrice       int    `gorm:"column:original_price;not null"`
	PromptPriceAtTime   int    `gorm:"column:prompt_price_at_time;not null;default:0"`
	PromptOriginalPrice int    `gorm:"column:prompt_original_price;not null;default:0"`
	CustomData          string `gorm:"column:custom_data;type:text;not null"`
	GeneratedImageID    *int   `gorm:"column:generated_image_id"`
	PromptID            *int   `gorm:"column:prompt_id"`
	Position            int    `gorm:"not null;default:0"`
	CreatedAt           time.Time
	UpdatedAt           time.Time
}

func (CartItemRow) TableName() string { return "cart_items" }

func (r *CartRow) ToDomain() cart.Cart {
	items := make([]cart.CartItem, 0, len(r.Items))
	for _, item := range r.Items {
		items = append(items, item.ToDomain())
	}
	return cart.Cart{
		ID:        r.ID,
		UserID:    r.UserID,
		Status:    cart.CartStatus(r.Status),
		Version:   r.Version,
		ExpiresAt: r.ExpiresAt,
		Items:     items,
		CreatedAt: r.CreatedAt,
		UpdatedAt: r.UpdatedAt,
	}
}

func (r *CartItemRow) ToDomain() cart.CartItem {
	return cart.CartItem{
		ID:                  r.ID,
		CartID:              r.CartID,
		ArticleID:           r.ArticleID,
		VariantID:           r.VariantID,
		Quantity:            r.Quantity,
		PriceAtTime:         r.PriceAtTime,
		OriginalPrice:       r.OriginalPrice,
		PromptPriceAtTime:   r.PromptPriceAtTime,
		PromptOriginalPrice: r.PromptOriginalPrice,
		CustomData:          r.CustomData,
		GeneratedImageID:    r.GeneratedImageID,
		PromptID:            r.PromptID,
		Position:            r.Position,
		CreatedAt:           r.CreatedAt,
		UpdatedAt:           r.UpdatedAt,
	}
}

func FromDomainCart(c cart.Cart) CartRow {
	items := make([]CartItemRow, 0, len(c.Items))
	for _, item := range c.Items {
		items = append(items, FromDomainItem(item))
	}
	return CartRow{
		ID:        c.ID,
		UserID:    c.UserID,
		Status:    string(c.Status),
		Version:   c.Version,
		ExpiresAt: c.ExpiresAt,
		Items:     items,
		CreatedAt: c.CreatedAt,
		UpdatedAt: c.UpdatedAt,
	}
}

func FromDomainItem(item cart.CartItem) CartItemRow {
	return CartItemRow{
		ID:                  item.ID,
		CartID:              item.CartID,
		ArticleID:           item.ArticleID,
		VariantID:           item.VariantID,
		Quantity:            item.Quantity,
		PriceAtTime:         item.PriceAtTime,
		OriginalPrice:       item.OriginalPrice,
		PromptPriceAtTime:   item.PromptPriceAtTime,
		PromptOriginalPrice: item.PromptOriginalPrice,
		CustomData:          item.CustomData,
		GeneratedImageID:    item.GeneratedImageID,
		PromptID:            item.PromptID,
		Position:            item.Position,
		CreatedAt:           item.CreatedAt,
		UpdatedAt:           item.UpdatedAt,
	}
}
