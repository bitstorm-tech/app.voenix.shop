package cart

import "time"

// Typical values: "active", "abandoned", "converted".
type CartStatus string

const (
	CartStatusActive    CartStatus = "active"
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

// AddItemInput represents the information needed to add an item to a cart.
type AddItemInput struct {
	ArticleID        int
	VariantID        int
	Quantity         int
	CustomData       map[string]any
	GeneratedImageID *int
	PromptID         *int
}

// UpdateItemQuantityInput carries the information required to update an item's quantity.
type UpdateItemQuantityInput struct {
	ItemID   int
	Quantity int
}

// CartSummary captures high-level totals for a cart.
type CartSummary struct {
	ItemCount  int
	TotalPrice int
	HasItems   bool
}

// CartDetail bundles a cart with supporting lookup data for presentation.
type CartDetail struct {
	Cart                    *Cart
	GeneratedImageFilenames map[int]string
	PromptTitles            map[int]string
}
