package order

import (
	"context"
	"testing"

	"voenix/backend/internal/cart"
)

func TestCreateOrderFromCart(t *testing.T) {
	repo := newFakeRepository()
	var articleSvc ArticleService
	svc := NewService(repo, articleSvc)

	userID := 100
	repo.setActiveCart(cart.Cart{
		ID:     1,
		UserID: userID,
		Items: []cart.CartItem{
			{ArticleID: 1, VariantID: 1, Quantity: 2, PriceAtTime: 1500, CustomData: "{}"},
		},
	})

	req := CreateOrderRequest{
		CustomerEmail:     "john@example.com",
		CustomerFirstName: "John",
		CustomerLastName:  "Doe",
		ShippingAddress:   AddressRequest{StreetAddress1: "123 Main", City: "City", State: "ST", PostalCode: "00000", Country: "USA"},
	}

	ord, err := svc.CreateOrderFromCart(context.Background(), userID, req)
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
	if len(ord.Items) != 1 {
		t.Fatalf("items count = %d", len(ord.Items))
	}
}
