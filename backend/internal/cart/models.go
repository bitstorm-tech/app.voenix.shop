package cart

import "time"

// Typical values: "active", "abandoned", "converted".
type CartStatus string

const (
	CartStatusActive    CartStatus = "active"
	CartStatusAbandoned CartStatus = "abandoned"
	CartStatusConverted CartStatus = "converted"
)

// Cart is the shopping cart aggregate root.
type Cart struct {
	ID        int        `gorm:"primaryKey" json:"id"`
	UserID    int        `gorm:"column:user_id;not null" json:"userId"`
	Status    string     `gorm:"size:20;not null" json:"status"`
	Version   int64      `gorm:"column:version;not null;default:0;version" json:"version"`
	ExpiresAt *time.Time `gorm:"column:expires_at" json:"expiresAt"`
	Items     []CartItem `gorm:"foreignKey:CartID;references:ID" json:"-"`
	CreatedAt time.Time  `json:"createdAt"`
	UpdatedAt time.Time  `json:"updatedAt"`
}

func (Cart) TableName() string { return "carts" }

// CartItem is a line item within a cart.
type CartItem struct {
	ID                  int       `gorm:"primaryKey" json:"id"`
	CartID              int       `gorm:"column:cart_id;not null" json:"cartId"`
	ArticleID           int       `gorm:"column:article_id;not null" json:"articleId"`
	VariantID           int       `gorm:"column:variant_id;not null" json:"variantId"`
	Quantity            int       `gorm:"not null" json:"quantity"`
	PriceAtTime         int       `gorm:"column:price_at_time;not null" json:"priceAtTime"`
	OriginalPrice       int       `gorm:"column:original_price;not null" json:"originalPrice"`
	PromptPriceAtTime   int       `gorm:"column:prompt_price_at_time;not null;default:0" json:"promptPriceAtTime"`
	PromptOriginalPrice int       `gorm:"column:prompt_original_price;not null;default:0" json:"promptOriginalPrice"`
	CustomData          string    `gorm:"column:custom_data;type:text;not null" json:"-"`
	GeneratedImageID    *int      `gorm:"column:generated_image_id" json:"generatedImageId"`
	PromptID            *int      `gorm:"column:prompt_id" json:"promptId"`
	Position            int       `gorm:"not null;default:0" json:"position"`
	CreatedAt           time.Time `json:"createdAt"`
	UpdatedAt           time.Time `json:"updatedAt"`
}

func (CartItem) TableName() string { return "cart_items" }
