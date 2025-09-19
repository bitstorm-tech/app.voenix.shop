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
	ID        int
	UserID    int
	Status    CartStatus
	Version   int64
	ExpiresAt *time.Time
	Items     []CartItem
	CreatedAt time.Time
	UpdatedAt time.Time
}

// CartItem is a line item within a cart.
type CartItem struct {
	ID                  int
	CartID              int
	ArticleID           int
	VariantID           int
	Quantity            int
	PriceAtTime         int
	OriginalPrice       int
	PromptPriceAtTime   int
	PromptOriginalPrice int
	CustomData          string
	GeneratedImageID    *int
	PromptID            *int
	Position            int
	CreatedAt           time.Time
	UpdatedAt           time.Time
}
