package auth

import "context"

// Repository abstracts persistence concerns for the auth domain.
type Repository interface {
	UserByEmail(ctx context.Context, email string) (*User, error)
	UserByID(ctx context.Context, id int) (*User, error)
	CreateSession(ctx context.Context, session *Session) error
	DeleteSession(ctx context.Context, id string) error
	SessionByID(ctx context.Context, id string) (*Session, error)
}
