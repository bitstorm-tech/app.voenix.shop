package postgres

import (
	"context"
	"errors"

	"gorm.io/gorm"

	"voenix/backend/internal/auth"
)

// Repository persists auth domain entities in Postgres via GORM.
type Repository struct {
	db *gorm.DB
}

// NewRepository wires a Postgres-backed auth repository.
func NewRepository(db *gorm.DB) *Repository {
	return &Repository{db: db}
}

var _ auth.Repository = (*Repository)(nil)

// UserByEmail loads a user and their roles by email address.
func (r *Repository) UserByEmail(ctx context.Context, email string) (*auth.User, error) {
	var row UserRow
	err := r.db.WithContext(ctx).Preload("Roles").Where("email = ?", email).First(&row).Error
	if errors.Is(err, gorm.ErrRecordNotFound) {
		return nil, nil
	}
	if err != nil {
		return nil, err
	}
	user := userFromRow(&row)
	return &user, nil
}

// UserByID loads a user and their roles by primary key.
func (r *Repository) UserByID(ctx context.Context, id int) (*auth.User, error) {
	var row UserRow
	err := r.db.WithContext(ctx).Preload("Roles").First(&row, "id = ?", id).Error
	if errors.Is(err, gorm.ErrRecordNotFound) {
		return nil, nil
	}
	if err != nil {
		return nil, err
	}
	user := userFromRow(&row)
	return &user, nil
}

// CreateSession stores a new session row for the user.
func (r *Repository) CreateSession(ctx context.Context, session *auth.Session) error {
	row := sessionToRow(session)
	if err := r.db.WithContext(ctx).Create(&row).Error; err != nil {
		return err
	}
	session.CreatedAt = row.CreatedAt
	return nil
}

// DeleteSession removes a session row.
func (r *Repository) DeleteSession(ctx context.Context, id string) error {
	return r.db.WithContext(ctx).Delete(&SessionRow{ID: id}).Error
}

// SessionByID loads a session row.
func (r *Repository) SessionByID(ctx context.Context, id string) (*auth.Session, error) {
	var row SessionRow
	err := r.db.WithContext(ctx).First(&row, "id = ?", id).Error
	if errors.Is(err, gorm.ErrRecordNotFound) {
		return nil, nil
	}
	if err != nil {
		return nil, err
	}
	session := sessionFromRow(&row)
	return &session, nil
}

func userFromRow(row *UserRow) auth.User {
	if row == nil {
		return auth.User{}
	}
	roles := make([]auth.Role, 0, len(row.Roles))
	for _, r := range row.Roles {
		roles = append(roles, roleFromRow(&r))
	}
	return auth.User{
		ID:                       row.ID,
		Email:                    row.Email,
		FirstName:                row.FirstName,
		LastName:                 row.LastName,
		PhoneNumber:              row.PhoneNumber,
		Password:                 row.Password,
		OneTimePassword:          row.OneTimePassword,
		OneTimePasswordCreatedAt: row.OneTimePasswordCreatedAt,
		CreatedAt:                row.CreatedAt,
		UpdatedAt:                row.UpdatedAt,
		DeletedAt:                row.DeletedAt,
		Roles:                    roles,
	}
}

func roleFromRow(row *RoleRow) auth.Role {
	if row == nil {
		return auth.Role{}
	}
	return auth.Role{
		ID:          row.ID,
		Name:        row.Name,
		Description: row.Description,
		CreatedAt:   row.CreatedAt,
		UpdatedAt:   row.UpdatedAt,
	}
}

func sessionToRow(session *auth.Session) SessionRow {
	if session == nil {
		return SessionRow{}
	}
	return SessionRow{
		ID:        session.ID,
		UserID:    session.UserID,
		CreatedAt: session.CreatedAt,
		ExpiresAt: session.ExpiresAt,
	}
}

func sessionFromRow(row *SessionRow) auth.Session {
	if row == nil {
		return auth.Session{}
	}
	return auth.Session{
		ID:        row.ID,
		UserID:    row.UserID,
		CreatedAt: row.CreatedAt,
		ExpiresAt: row.ExpiresAt,
	}
}
