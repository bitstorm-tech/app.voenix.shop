package postgres

import (
	"time"

	"voenix/backend/internal/country"
)

type supplierRow struct {
	ID           int     `gorm:"primaryKey"`
	Name         *string `gorm:"size:255"`
	Title        *string `gorm:"size:100"`
	FirstName    *string `gorm:"size:255"`
	LastName     *string `gorm:"size:255"`
	Street       *string `gorm:"size:255"`
	HouseNumber  *string `gorm:"size:50"`
	City         *string `gorm:"size:255"`
	PostalCode   *int
	CountryID    *int
	Country      *country.Country `gorm:"foreignKey:CountryID"`
	PhoneNumber1 *string          `gorm:"size:50"`
	PhoneNumber2 *string          `gorm:"size:50"`
	PhoneNumber3 *string          `gorm:"size:50"`
	Email        *string          `gorm:"size:255"`
	Website      *string          `gorm:"size:500"`
	CreatedAt    time.Time
	UpdatedAt    time.Time
}

func (supplierRow) TableName() string { return "suppliers" }
