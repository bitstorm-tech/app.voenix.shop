package postgres

import "time"

// RoleRow maps the roles table schema for GORM interactions.
type RoleRow struct {
	ID          int     `gorm:"primaryKey"`
	Name        string  `gorm:"size:50;unique;not null"`
	Description *string `gorm:"size:255"`
	CreatedAt   time.Time
	UpdatedAt   time.Time
	Users       []UserRow `gorm:"many2many:user_roles;joinForeignKey:RoleID;joinReferences:UserID"`
}

func (RoleRow) TableName() string { return "roles" }

// UserRow maps the users table schema for GORM interactions.
type UserRow struct {
	ID                       int     `gorm:"primaryKey"`
	Email                    string  `gorm:"size:255;unique;not null"`
	FirstName                *string `gorm:"size:255"`
	LastName                 *string `gorm:"size:255"`
	PhoneNumber              *string `gorm:"size:255"`
	Password                 *string `gorm:"size:255"`
	OneTimePassword          *string `gorm:"size:255"`
	OneTimePasswordCreatedAt *time.Time
	CreatedAt                time.Time
	UpdatedAt                time.Time
	DeletedAt                *time.Time
	Roles                    []RoleRow `gorm:"many2many:user_roles;joinForeignKey:UserID;joinReferences:RoleID"`
}

func (UserRow) TableName() string { return "users" }

// SessionRow maps the sessions table schema for GORM interactions.
type SessionRow struct {
	ID        string `gorm:"primaryKey;size:128"`
	UserID    int    `gorm:"not null"`
	CreatedAt time.Time
	ExpiresAt *time.Time
}

func (SessionRow) TableName() string { return "sessions" }
