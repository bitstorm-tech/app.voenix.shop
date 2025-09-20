package supplier

import "context"

type Repository interface {
	List(ctx context.Context) ([]Supplier, error)
	ByID(ctx context.Context, supplierID int) (Supplier, error)
	Create(ctx context.Context, supplier *Supplier) error
	Update(ctx context.Context, supplier *Supplier) error
	Delete(ctx context.Context, supplierID int) error
}
