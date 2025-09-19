package order

import (
	"testing"

	"gorm.io/driver/sqlite"
	"gorm.io/gorm"

	"voenix/backend/internal/article"
	"voenix/backend/internal/cart"
)

func setupTestDB(t *testing.T) *gorm.DB {
	t.Helper()
	db, err := gorm.Open(sqlite.Open(":memory:"), &gorm.Config{})
	if err != nil {
		t.Fatalf("open db: %v", err)
	}
	// Migrate minimal models used
	if err := db.AutoMigrate(
		&article.ArticleCategory{}, &article.ArticleSubCategory{}, &article.Article{}, &article.MugVariant{}, &article.ShirtVariant{}, &article.MugDetails{}, &article.ShirtDetails{}, &article.Price{},
		&cart.Cart{}, &cart.CartItem{},
		&Order{}, &OrderItem{},
	); err != nil {
		t.Fatalf("migrate: %v", err)
	}
	return db
}

func TestCreateOrderFromCart(t *testing.T) {
	db := setupTestDB(t)

	// Seed article + variant
	a := article.Article{ID: 1, Name: "Mug", DescriptionShort: "d1", DescriptionLong: "d2", Active: true, ArticleType: article.ArticleTypeMug, CategoryID: 1}
	if err := db.Create(&a).Error; err != nil {
		t.Fatalf("seed article: %v", err)
	}
	mv := article.MugVariant{ID: 1, ArticleID: 1, Name: "Default", OutsideColorCode: "#000000", InsideColorCode: "#FFFFFF", IsDefault: true, Active: true}
	if err := db.Create(&mv).Error; err != nil {
		t.Fatalf("seed variant: %v", err)
	}

	// Seed cart + item
	c := cart.Cart{UserID: 100, Status: cart.CartStatusActive}
	if err := db.Create(&c).Error; err != nil {
		t.Fatalf("seed cart: %v", err)
	}
	ci := cart.CartItem{CartID: c.ID, ArticleID: 1, VariantID: 1, Quantity: 2, PriceAtTime: 1500, OriginalPrice: 1500, CustomData: "{}"}
	if err := db.Create(&ci).Error; err != nil {
		t.Fatalf("seed cart item: %v", err)
	}

	// Create order
	req := CreateOrderRequest{
		CustomerEmail:     "john@example.com",
		CustomerFirstName: "John",
		CustomerLastName:  "Doe",
		ShippingAddress:   AddressDto{StreetAddress1: "123 Main", City: "City", State: "ST", PostalCode: "00000", Country: "USA"},
	}
	ord, err := CreateOrderFromCart(db, 100, req)
	if err != nil {
		t.Fatalf("create order: %v", err)
	}
	if ord.Status != StatusPending {
		t.Fatalf("status = %s", ord.Status)
	}
	if ord.Subtotal != 3000 {
		t.Fatalf("subtotal = %d", ord.Subtotal)
	}
	if ord.TaxAmount != 240 {
		t.Fatalf("tax = %d", ord.TaxAmount)
	}
	if ord.ShippingAmount != 499 {
		t.Fatalf("shipping = %d", ord.ShippingAmount)
	}
	if ord.TotalAmount != 3739 {
		t.Fatalf("total = %d", ord.TotalAmount)
	}
	if len(ord.Items) != 2 && len(ord.Items) != 0 {
		// ord.Items may not be preloaded here; reload:
		var count int64
		_ = db.Model(&OrderItem{}).Where("order_id = ?", ord.ID).Count(&count).Error
		if count != 1 {
			t.Fatalf("items count = %d", count)
		}
	}
}
