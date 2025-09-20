package country

import "context"

type Repository interface {
	All(ctx context.Context) ([]Country, error)
}
