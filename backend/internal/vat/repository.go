package vat

import "context"

// Repository defines persistence operations for VAT domain objects.
type Repository interface {
	List(context.Context) ([]ValueAddedTax, error)
	ByID(context.Context, int) (ValueAddedTax, error)
	Create(context.Context, *ValueAddedTax) error
	Update(context.Context, *ValueAddedTax) error
	Delete(context.Context, int) error
	ClearDefault(context.Context) error
	WithTransaction(context.Context, func(Repository) error) error
}
