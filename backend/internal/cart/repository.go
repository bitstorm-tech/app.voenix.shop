package cart

import (
	"context"

	"voenix/backend/internal/prompt"
)

type Repository interface {
	GetOrCreateActiveCart(ctx context.Context, userID int) (*Cart, error)
	LoadActiveCart(ctx context.Context, userID int) (*Cart, error)
	SaveCart(ctx context.Context, cart Cart) (*Cart, error)
	UpdateItemQuantity(ctx context.Context, cartID, itemID, quantity int) (bool, error)
	DeleteItem(ctx context.Context, cartID, itemID int) (bool, error)
	ClearCartItems(ctx context.Context, cartID int) error
	ReloadCart(ctx context.Context, cartID int) (*Cart, error)
	FetchGeneratedImageFilenames(ctx context.Context, ids []int) (map[int]string, error)
	FetchPromptTitles(ctx context.Context, ids []int) (map[int]string, error)
	PromptExists(ctx context.Context, id int) (bool, error)
	LoadPrompt(ctx context.Context, id int) (*prompt.Prompt, error)
	WithTx(ctx context.Context, fn func(Repository) error) error
}
