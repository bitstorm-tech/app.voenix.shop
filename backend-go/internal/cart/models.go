package cart

import (
    "time"
)

// CartStatus mirrors Kotlin values but kept simple as string.
// Typical values: "active", "abandoned", "converted".
type CartStatus string

const (
    CartStatusActive    CartStatus = "active"
    CartStatusAbandoned CartStatus = "abandoned"
    CartStatusConverted CartStatus = "converted"
)

// GORM models aligning to Kotlin entities and table names.

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

type CartItem struct {
    ID               int                `gorm:"primaryKey" json:"id"`
    CartID           int                `gorm:"column:cart_id;not null" json:"cartId"`
    ArticleID        int                `gorm:"column:article_id;not null" json:"articleId"`
    VariantID        int                `gorm:"column:variant_id;not null" json:"variantId"`
    Quantity         int                `gorm:"not null" json:"quantity"`
    PriceAtTime      int                `gorm:"column:price_at_time;not null" json:"priceAtTime"`
    OriginalPrice    int                `gorm:"column:original_price;not null" json:"originalPrice"`
    CustomData       string             `gorm:"column:custom_data;type:text;not null" json:"-"`
    GeneratedImageID *int               `gorm:"column:generated_image_id" json:"generatedImageId"`
    PromptID         *int               `gorm:"column:prompt_id" json:"promptId"`
    Position         int                `gorm:"not null;default:0" json:"position"`
    CreatedAt        time.Time          `json:"createdAt"`
    UpdatedAt        time.Time          `json:"updatedAt"`
}

func (CartItem) TableName() string { return "cart_items" }

// Response DTOs for /api/user/cart mirroring FE types (frontend/src/types/cart.ts)

type CartSummaryDto struct {
    ItemCount int  `json:"itemCount"`
    TotalPrice int `json:"totalPrice"`
    HasItems bool  `json:"hasItems"`
}

// Variant DTO used in cart responses (simplified for FE)
type MugVariantDto struct {
    ID                   int     `json:"id"`
    ArticleID            int     `json:"articleId"`
    ColorCode            string  `json:"colorCode"`
    ExampleImageURL      *string `json:"exampleImageUrl"`
    SupplierArticleNumber *string `json:"supplierArticleNumber"`
    IsDefault            bool    `json:"isDefault"`
    ExampleImageFilename *string `json:"exampleImageFilename"`
}

type CartItemDto struct {
    ID                    int               `json:"id"`
    Article               interface{}       `json:"article"` // article.ArticleRead (avoid import cycle in DTO file)
    Variant               *MugVariantDto    `json:"variant"`
    Quantity              int               `json:"quantity"`
    PriceAtTime           int               `json:"priceAtTime"`
    OriginalPrice         int               `json:"originalPrice"`
    HasPriceChanged       bool              `json:"hasPriceChanged"`
    TotalPrice            int               `json:"totalPrice"`
    CustomData            map[string]any    `json:"customData"`
    GeneratedImageID      *int              `json:"generatedImageId,omitempty"`
    GeneratedImageFilename *string          `json:"generatedImageFilename,omitempty"`
    PromptID              *int              `json:"promptId,omitempty"`
    Position              int               `json:"position"`
    CreatedAt             time.Time         `json:"createdAt"`
    UpdatedAt             time.Time         `json:"updatedAt"`
}

type CartDto struct {
    ID             int          `json:"id"`
    UserID         int          `json:"userId"`
    Status         string       `json:"status"`
    Version        int64        `json:"version"`
    ExpiresAt      *time.Time   `json:"expiresAt"`
    Items          []CartItemDto `json:"items"`
    TotalItemCount int          `json:"totalItemCount"`
    TotalPrice     int          `json:"totalPrice"`
    IsEmpty        bool         `json:"isEmpty"`
    CreatedAt      time.Time    `json:"createdAt"`
    UpdatedAt      time.Time    `json:"updatedAt"`
}

// Requests
type AddToCartRequest struct {
    ArticleID        int                 `json:"articleId" binding:"required"`
    VariantID        int                 `json:"variantId" binding:"required"`
    Quantity         int                 `json:"quantity"`
    CustomData       map[string]any      `json:"customData"`
    GeneratedImageID *int                `json:"generatedImageId"`
    PromptID         *int                `json:"promptId"`
}

type UpdateCartItemRequest struct {
    Quantity int `json:"quantity" binding:"required,min=1"`
}
