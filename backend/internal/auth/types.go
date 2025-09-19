package auth

import "time"

// Domain aggregates without storage-specific annotations.
type Role struct {
	ID          int
	Name        string
	Description *string
	CreatedAt   time.Time
	UpdatedAt   time.Time
}

type User struct {
	ID                       int
	Email                    string
	FirstName                *string
	LastName                 *string
	PhoneNumber              *string
	Password                 *string
	OneTimePassword          *string
	OneTimePasswordCreatedAt *time.Time
	CreatedAt                time.Time
	UpdatedAt                time.Time
	DeletedAt                *time.Time
	Roles                    []Role
}

type Session struct {
	ID        string
	UserID    int
	CreatedAt time.Time
	ExpiresAt *time.Time
}

// Computed convenience helpers.
func (u *User) IsActive() bool {
	return u != nil && u.DeletedAt == nil
}

func RoleNames(u *User) []string {
	if u == nil {
		return nil
	}
	out := make([]string, 0, len(u.Roles))
	for _, r := range u.Roles {
		out = append(out, r.Name)
	}
	return out
}
