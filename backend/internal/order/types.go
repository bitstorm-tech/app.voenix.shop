package order

import "time"

const (
	StatusPending    = "PENDING"
	StatusProcessing = "PROCESSING"
	StatusShipped    = "SHIPPED"
	StatusDelivered  = "DELIVERED"
	StatusCancelled  = "CANCELLED"
)

// Order captures the domain representation of a customer order.
type Order struct {
	ID              string
	OrderNumber     string
	UserID          int
	CustomerEmail   string
	CustomerFirst   string
	CustomerLast    string
	CustomerPhone   *string
	ShippingStreet1 string
	ShippingStreet2 *string
	ShippingCity    string
	ShippingState   string
	ShippingPostal  string
	ShippingCountry string
	BillingStreet1  *string
	BillingStreet2  *string
	BillingCity     *string
	BillingState    *string
	BillingPostal   *string
	BillingCountry  *string
	Subtotal        int64
	TaxAmount       int64
	ShippingAmount  int64
	TotalAmount     int64
	Status          string
	CartID          int
	Notes           *string
	Items           []OrderItem
	CreatedAt       time.Time
	UpdatedAt       time.Time
}

// OrderItem represents a purchased item within an order.
type OrderItem struct {
	ID                     string
	OrderID                string
	ArticleID              int
	VariantID              int
	Quantity               int
	PricePerItem           int64
	TotalPrice             int64
	GeneratedImageID       *int
	GeneratedImageFilename *string
	PromptID               *int
	CustomData             string
	CreatedAt              time.Time
}

// OrderPage bundles paginated order results for a user.
type OrderPage struct {
	Orders        []Order
	CurrentPage   int
	TotalPages    int
	TotalElements int64
	Size          int
}
