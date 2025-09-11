package cart

import (
	"time"

	"voenix/backend-go/internal/article"
)

// Requests
type AddToCartRequest struct {
	ArticleID        int            `json:"articleId" binding:"required"`
	VariantID        int            `json:"variantId" binding:"required"`
	Quantity         int            `json:"quantity"`
	CustomData       map[string]any `json:"customData"`
	GeneratedImageID *int           `json:"generatedImageId"`
	PromptID         *int           `json:"promptId"`
}

type UpdateCartItemRequest struct {
	Quantity int `json:"quantity" binding:"required,min=1"`
}

// DTOs
type CartSummaryDto struct {
	ItemCount  int  `json:"itemCount"`
	TotalPrice int  `json:"totalPrice"`
	HasItems   bool `json:"hasItems"`
}

type MugVariantDto struct {
	ID                    int     `json:"id"`
	ArticleID             int     `json:"articleId"`
	ColorCode             string  `json:"colorCode"`
	ExampleImageURL       *string `json:"exampleImageUrl"`
	SupplierArticleNumber *string `json:"supplierArticleNumber"`
	IsDefault             bool    `json:"isDefault"`
	ExampleImageFilename  *string `json:"exampleImageFilename"`
}

type CartItemDto struct {
	ID                     int                 `json:"id"`
	Article                article.ArticleRead `json:"article"`
	Variant                *MugVariantDto      `json:"variant"`
	Quantity               int                 `json:"quantity"`
	PriceAtTime            int                 `json:"priceAtTime"`
	OriginalPrice          int                 `json:"originalPrice"`
	HasPriceChanged        bool                `json:"hasPriceChanged"`
	TotalPrice             int                 `json:"totalPrice"`
	CustomData             map[string]any      `json:"customData"`
	GeneratedImageID       *int                `json:"generatedImageId"`
	GeneratedImageFilename *string             `json:"generatedImageFilename"`
	PromptID               *int                `json:"promptId"`
	Position               int                 `json:"position"`
	CreatedAt              time.Time           `json:"createdAt"`
	UpdatedAt              time.Time           `json:"updatedAt"`
}

type CartDto struct {
	ID             int           `json:"id"`
	UserID         int           `json:"userId"`
	Status         string        `json:"status"`
	Version        int64         `json:"version"`
	ExpiresAt      *time.Time    `json:"expiresAt"`
	Items          []CartItemDto `json:"items"`
	TotalItemCount int           `json:"totalItemCount"`
	TotalPrice     int           `json:"totalPrice"`
	IsEmpty        bool          `json:"isEmpty"`
	CreatedAt      time.Time     `json:"createdAt"`
	UpdatedAt      time.Time     `json:"updatedAt"`
}
