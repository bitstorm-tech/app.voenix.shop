package supplier

import (
	"time"

	country "voenix/backend-go/internal/country"
)

// Supplier mirrors the Python SQLModel Supplier table.
type Supplier struct {
	ID           int              `gorm:"primaryKey" json:"id"`
	Name         *string          `gorm:"size:255" json:"name"`
	Title        *string          `gorm:"size:100" json:"title"`
	FirstName    *string          `gorm:"size:255" json:"firstName"`
	LastName     *string          `gorm:"size:255" json:"lastName"`
	Street       *string          `gorm:"size:255" json:"street"`
	HouseNumber  *string          `gorm:"size:50" json:"houseNumber"`
	City         *string          `gorm:"size:255" json:"city"`
	PostalCode   *int             `json:"postalCode"`
	CountryID    *int             `json:"countryId"`
	Country      *country.Country `gorm:"foreignKey:CountryID" json:"country,omitempty"`
	PhoneNumber1 *string          `gorm:"size:50" json:"phoneNumber1"`
	PhoneNumber2 *string          `gorm:"size:50" json:"phoneNumber2"`
	PhoneNumber3 *string          `gorm:"size:50" json:"phoneNumber3"`
	Email        *string          `gorm:"size:255" json:"email"`
	Website      *string          `gorm:"size:500" json:"website"`
	CreatedAt    time.Time        `json:"createdAt"`
	UpdatedAt    time.Time        `json:"updatedAt"`
}

func (Supplier) TableName() string { return "suppliers" }

// Create/Update payloads (pointer fields allow nulls)
type SupplierCreate struct {
	Name         *string `json:"name"`
	Title        *string `json:"title"`
	FirstName    *string `json:"firstName"`
	LastName     *string `json:"lastName"`
	Street       *string `json:"street"`
	HouseNumber  *string `json:"houseNumber"`
	City         *string `json:"city"`
	PostalCode   *int    `json:"postalCode"`
	CountryID    *int    `json:"countryId"`
	PhoneNumber1 *string `json:"phoneNumber1"`
	PhoneNumber2 *string `json:"phoneNumber2"`
	PhoneNumber3 *string `json:"phoneNumber3"`
	Email        *string `json:"email"`
	Website      *string `json:"website"`
}

type SupplierUpdate = SupplierCreate
