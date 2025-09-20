package postgres

import (
	"time"

	"voenix/backend/internal/order"
)

type OrderRow struct {
	ID              string         `gorm:"primaryKey;column:id;size:36"`
	OrderNumber     string         `gorm:"column:order_number;size:50;uniqueIndex;->"`
	UserID          int            `gorm:"column:user_id;not null"`
	CustomerEmail   string         `gorm:"column:customer_email;size:255;not null"`
	CustomerFirst   string         `gorm:"column:customer_first_name;size:255;not null"`
	CustomerLast    string         `gorm:"column:customer_last_name;size:255;not null"`
	CustomerPhone   *string        `gorm:"column:customer_phone;size:50"`
	ShippingStreet1 string         `gorm:"column:shipping_street_address_1;size:255;not null"`
	ShippingStreet2 *string        `gorm:"column:shipping_street_address_2;size:255"`
	ShippingCity    string         `gorm:"column:shipping_city;size:100;not null"`
	ShippingState   string         `gorm:"column:shipping_state;size:100;not null"`
	ShippingPostal  string         `gorm:"column:shipping_postal_code;size:20;not null"`
	ShippingCountry string         `gorm:"column:shipping_country;size:100;not null"`
	BillingStreet1  *string        `gorm:"column:billing_street_address_1;size:255"`
	BillingStreet2  *string        `gorm:"column:billing_street_address_2;size:255"`
	BillingCity     *string        `gorm:"column:billing_city;size:100"`
	BillingState    *string        `gorm:"column:billing_state;size:100"`
	BillingPostal   *string        `gorm:"column:billing_postal_code;size:20"`
	BillingCountry  *string        `gorm:"column:billing_country;size:100"`
	Subtotal        int64          `gorm:"column:subtotal;not null"`
	TaxAmount       int64          `gorm:"column:tax_amount;not null"`
	ShippingAmount  int64          `gorm:"column:shipping_amount;not null"`
	TotalAmount     int64          `gorm:"column:total_amount;not null"`
	Status          string         `gorm:"column:status;size:20;not null"`
	CartID          int            `gorm:"column:cart_id;not null"`
	Notes           *string        `gorm:"column:notes;type:text"`
	Items           []OrderItemRow `gorm:"foreignKey:OrderID;references:ID"`
	CreatedAt       time.Time      `gorm:"column:created_at;autoCreateTime"`
	UpdatedAt       time.Time      `gorm:"column:updated_at;autoUpdateTime"`
}

func (OrderRow) TableName() string { return "orders" }

type OrderItemRow struct {
	ID               string    `gorm:"primaryKey;column:id;size:36"`
	OrderID          string    `gorm:"column:order_id;size:36;not null;index"`
	ArticleID        int       `gorm:"column:article_id;not null"`
	VariantID        int       `gorm:"column:variant_id;not null"`
	Quantity         int       `gorm:"column:quantity;not null"`
	PricePerItem     int64     `gorm:"column:price_per_item;not null"`
	TotalPrice       int64     `gorm:"column:total_price;not null"`
	GeneratedImageID *int      `gorm:"column:generated_image_id"`
	PromptID         *int      `gorm:"column:prompt_id"`
	CustomData       string    `gorm:"column:custom_data;type:text;not null"`
	CreatedAt        time.Time `gorm:"column:created_at;autoCreateTime"`
}

func (OrderItemRow) TableName() string { return "order_items" }

func orderRowFromDomain(o order.Order) OrderRow {
	items := make([]OrderItemRow, 0, len(o.Items))
	for _, it := range o.Items {
		items = append(items, orderItemRowFromDomain(it))
	}
	return OrderRow{
		ID:              o.ID,
		OrderNumber:     o.OrderNumber,
		UserID:          o.UserID,
		CustomerEmail:   o.CustomerEmail,
		CustomerFirst:   o.CustomerFirst,
		CustomerLast:    o.CustomerLast,
		CustomerPhone:   o.CustomerPhone,
		ShippingStreet1: o.ShippingStreet1,
		ShippingStreet2: o.ShippingStreet2,
		ShippingCity:    o.ShippingCity,
		ShippingState:   o.ShippingState,
		ShippingPostal:  o.ShippingPostal,
		ShippingCountry: o.ShippingCountry,
		BillingStreet1:  o.BillingStreet1,
		BillingStreet2:  o.BillingStreet2,
		BillingCity:     o.BillingCity,
		BillingState:    o.BillingState,
		BillingPostal:   o.BillingPostal,
		BillingCountry:  o.BillingCountry,
		Subtotal:        o.Subtotal,
		TaxAmount:       o.TaxAmount,
		ShippingAmount:  o.ShippingAmount,
		TotalAmount:     o.TotalAmount,
		Status:          o.Status,
		CartID:          o.CartID,
		Notes:           o.Notes,
		Items:           items,
		CreatedAt:       o.CreatedAt,
		UpdatedAt:       o.UpdatedAt,
	}
}

func (r OrderRow) toDomain() order.Order {
	items := make([]order.OrderItem, 0, len(r.Items))
	for _, it := range r.Items {
		items = append(items, it.toDomain())
	}
	return order.Order{
		ID:              r.ID,
		OrderNumber:     r.OrderNumber,
		UserID:          r.UserID,
		CustomerEmail:   r.CustomerEmail,
		CustomerFirst:   r.CustomerFirst,
		CustomerLast:    r.CustomerLast,
		CustomerPhone:   r.CustomerPhone,
		ShippingStreet1: r.ShippingStreet1,
		ShippingStreet2: r.ShippingStreet2,
		ShippingCity:    r.ShippingCity,
		ShippingState:   r.ShippingState,
		ShippingPostal:  r.ShippingPostal,
		ShippingCountry: r.ShippingCountry,
		BillingStreet1:  r.BillingStreet1,
		BillingStreet2:  r.BillingStreet2,
		BillingCity:     r.BillingCity,
		BillingState:    r.BillingState,
		BillingPostal:   r.BillingPostal,
		BillingCountry:  r.BillingCountry,
		Subtotal:        r.Subtotal,
		TaxAmount:       r.TaxAmount,
		ShippingAmount:  r.ShippingAmount,
		TotalAmount:     r.TotalAmount,
		Status:          r.Status,
		CartID:          r.CartID,
		Notes:           r.Notes,
		Items:           items,
		CreatedAt:       r.CreatedAt,
		UpdatedAt:       r.UpdatedAt,
	}
}

func orderItemRowFromDomain(i order.OrderItem) OrderItemRow {
	return OrderItemRow{
		ID:               i.ID,
		OrderID:          i.OrderID,
		ArticleID:        i.ArticleID,
		VariantID:        i.VariantID,
		Quantity:         i.Quantity,
		PricePerItem:     i.PricePerItem,
		TotalPrice:       i.TotalPrice,
		GeneratedImageID: i.GeneratedImageID,
		PromptID:         i.PromptID,
		CustomData:       i.CustomData,
		CreatedAt:        i.CreatedAt,
	}
}

func (r OrderItemRow) toDomain() order.OrderItem {
	return order.OrderItem{
		ID:               r.ID,
		OrderID:          r.OrderID,
		ArticleID:        r.ArticleID,
		VariantID:        r.VariantID,
		Quantity:         r.Quantity,
		PricePerItem:     r.PricePerItem,
		TotalPrice:       r.TotalPrice,
		GeneratedImageID: r.GeneratedImageID,
		PromptID:         r.PromptID,
		CustomData:       r.CustomData,
		CreatedAt:        r.CreatedAt,
	}
}
