package order

import (
	"context"

	"voenix/backend/internal/cart"
)

type Repository interface {
	ActiveCart(ctx context.Context, userID int) (*cart.Cart, error)
	OrderExistsForCart(ctx context.Context, cartID int) (bool, error)
	CreateOrder(ctx context.Context, ord *Order) error
	OrderByIDForUser(ctx context.Context, userID int, orderID int64) (*Order, error)
	ListOrdersForUser(ctx context.Context, userID int, page, size int) (OrderPage, error)
	FetchGeneratedImageFilenames(ctx context.Context, ids []int) (map[int]string, error)
}
