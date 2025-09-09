package auth

import "time"

// Join table user_roles is implicit via many2many in GORM.

type Role struct {
    ID          int        `gorm:"primaryKey" json:"id"`
    Name        string     `gorm:"size:50;unique;not null" json:"name"`
    Description *string    `gorm:"size:255" json:"description,omitempty"`
    CreatedAt   time.Time  `json:"createdAt"`
    UpdatedAt   time.Time  `json:"updatedAt"`
    Users       []User     `gorm:"many2many:user_roles;" json:"-"`
}

func (Role) TableName() string { return "roles" }

type User struct {
    ID                         int        `gorm:"primaryKey" json:"id"`
    Email                      string     `gorm:"size:255;unique;not null" json:"email"`
    FirstName                  *string    `gorm:"size:255" json:"firstName,omitempty"`
    LastName                   *string    `gorm:"size:255" json:"lastName,omitempty"`
    PhoneNumber                *string    `gorm:"size:255" json:"phoneNumber,omitempty"`
    Password                   *string    `gorm:"size:255" json:"-"`
    OneTimePassword            *string    `gorm:"size:255" json:"-"`
    OneTimePasswordCreatedAt   *time.Time `json:"-"`
    CreatedAt                  time.Time  `json:"createdAt"`
    UpdatedAt                  time.Time  `json:"updatedAt"`
    DeletedAt                  *time.Time `json:"deletedAt,omitempty"`
    Roles                      []Role     `gorm:"many2many:user_roles;" json:"roles,omitempty"`
}

func (User) TableName() string { return "users" }

type Session struct {
    ID        string     `gorm:"primaryKey;size:128" json:"id"`
    UserID    int        `gorm:"not null" json:"userId"`
    CreatedAt time.Time  `json:"createdAt"`
    ExpiresAt *time.Time `json:"expiresAt,omitempty"`
}

func (Session) TableName() string { return "sessions" }

// Computed convenience
func (u *User) IsActive() bool { return u != nil && u.DeletedAt == nil }

