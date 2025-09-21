package postgres

import (
	"context"
	"testing"

	"gorm.io/driver/sqlite"
	"gorm.io/gorm"

	"voenix/backend/internal/cart"
	cartpg "voenix/backend/internal/cart/postgres"
	"voenix/backend/internal/order"
)

func TestCreateOrderPersistsItemsWithOrderID(t *testing.T) {
	testDatabase, databaseError := gorm.Open(sqlite.Open(":memory:"), &gorm.Config{})
	if databaseError != nil {
		t.Fatalf("open database: %v", databaseError)
	}

	if migrateError := testDatabase.AutoMigrate(&OrderRow{}, &OrderItemRow{}, &cartpg.CartRow{}); migrateError != nil {
		t.Fatalf("auto migrate: %v", migrateError)
	}

	initialCart := cartpg.CartRow{ID: 1, UserID: 77, Status: string(cart.CartStatusActive)}
	if insertCartError := testDatabase.Create(&initialCart).Error; insertCartError != nil {
		t.Fatalf("insert cart: %v", insertCartError)
	}

	domainOrder := order.Order{
		UserID:          initialCart.UserID,
		CustomerEmail:   "buyer@example.com",
		CustomerFirst:   "Buyer",
		CustomerLast:    "Person",
		ShippingStreet1: "123 Any Street",
		ShippingCity:    "Townsville",
		ShippingState:   "TS",
		ShippingPostal:  "12345",
		ShippingCountry: "USA",
		Subtotal:        1000,
		TaxAmount:       80,
		ShippingAmount:  499,
		TotalAmount:     1579,
		Status:          order.StatusPending,
		CartID:          initialCart.ID,
	}

	domainOrder.Items = []order.OrderItem{
		{
			ArticleID:    99,
			VariantID:    42,
			Quantity:     2,
			PricePerItem: 500,
			TotalPrice:   1000,
			CustomData:   "{}",
		},
	}

	repository := NewRepository(testDatabase)
	testContext := context.Background()
	if createOrderError := repository.CreateOrder(testContext, &domainOrder); createOrderError != nil {
		t.Fatalf("create order: %v", createOrderError)
	}

	var storedItems []OrderItemRow
	if queryItemsError := testDatabase.Find(&storedItems).Error; queryItemsError != nil {
		t.Fatalf("query order items: %v", queryItemsError)
	}

	if len(storedItems) != 1 {
		t.Fatalf("expected one order item, got %d", len(storedItems))
	}

	if domainOrder.ID == 0 {
		t.Fatalf("expected order ID to be set, got 0")
	}

	if storedItems[0].OrderID != domainOrder.ID {
		t.Fatalf("expected order_id %d, got %d", domainOrder.ID, storedItems[0].OrderID)
	}

	if len(domainOrder.Items) == 0 {
		t.Fatalf("expected domain order to contain items")
	}

	if domainOrder.Items[0].OrderID != domainOrder.ID {
		t.Fatalf("expected domain item order id %d, got %d", domainOrder.ID, domainOrder.Items[0].OrderID)
	}
}
