package order

import (
	"time"

	"voenix/backend/internal/article"
	"voenix/backend/internal/cart"
)

// Requests

type AddressDto struct {
	StreetAddress1 string  `json:"streetAddress1" binding:"required"`
	StreetAddress2 *string `json:"streetAddress2"`
	City           string  `json:"city" binding:"required"`
	State          string  `json:"state" binding:"required"`
	PostalCode     string  `json:"postalCode" binding:"required"`
	Country        string  `json:"country" binding:"required"`
}

type CreateOrderRequest struct {
	CustomerEmail        string      `json:"customerEmail" binding:"required,email"`
	CustomerFirstName    string      `json:"customerFirstName" binding:"required"`
	CustomerLastName     string      `json:"customerLastName" binding:"required"`
	CustomerPhone        *string     `json:"customerPhone"`
	ShippingAddress      AddressDto  `json:"shippingAddress" binding:"required"`
	BillingAddress       *AddressDto `json:"billingAddress"`
	UseShippingAsBilling *bool       `json:"useShippingAsBilling"`
	Notes                *string     `json:"notes"`
}

// DTOs matching frontend expectations (frontend/src/types/order.ts)

type OrderItemDto struct {
	ID                     string              `json:"id"`
	Article                article.ArticleRead `json:"article"`
	Variant                *cart.MugVariantDto `json:"variant"`
	Quantity               int                 `json:"quantity"`
	PricePerItem           int64               `json:"pricePerItem"`
	TotalPrice             int64               `json:"totalPrice"`
	GeneratedImageID       *int                `json:"generatedImageId,omitempty"`
	GeneratedImageFilename *string             `json:"generatedImageFilename,omitempty"`
	PromptID               *int                `json:"promptId,omitempty"`
	CustomData             map[string]any      `json:"customData"`
	CreatedAt              time.Time           `json:"createdAt"`
}

type OrderDto struct {
	ID              string         `json:"id"`
	OrderNumber     string         `json:"orderNumber"`
	CustomerEmail   string         `json:"customerEmail"`
	CustomerFirst   string         `json:"customerFirstName"`
	CustomerLast    string         `json:"customerLastName"`
	CustomerPhone   *string        `json:"customerPhone,omitempty"`
	ShippingAddress AddressDto     `json:"shippingAddress"`
	BillingAddress  *AddressDto    `json:"billingAddress,omitempty"`
	Subtotal        int64          `json:"subtotal"`
	TaxAmount       int64          `json:"taxAmount"`
	ShippingAmount  int64          `json:"shippingAmount"`
	TotalAmount     int64          `json:"totalAmount"`
	Status          string         `json:"status"`
	CartID          int            `json:"cartId"`
	Notes           *string        `json:"notes,omitempty"`
	Items           []OrderItemDto `json:"items"`
	PDFURL          string         `json:"pdfUrl"`
	CreatedAt       time.Time      `json:"createdAt"`
	UpdatedAt       time.Time      `json:"updatedAt"`
}

type PaginatedResponse[T any] struct {
	Content       []T   `json:"content"`
	CurrentPage   int   `json:"currentPage"`
	TotalPages    int   `json:"totalPages"`
	TotalElements int64 `json:"totalElements"`
	Size          int   `json:"size"`
}
