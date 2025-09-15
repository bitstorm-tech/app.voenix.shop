package supplier

import (
	"time"

	"voenix/backend/internal/country"
)

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

// SupplierRead is a response DTO. Not yet wired into handlers but provided for clarity.
// Keeping parity with entity fields that are serialized.
type SupplierRead struct {
	ID           int              `json:"id"`
	Name         *string          `json:"name"`
	Title        *string          `json:"title"`
	FirstName    *string          `json:"firstName"`
	LastName     *string          `json:"lastName"`
	Street       *string          `json:"street"`
	HouseNumber  *string          `json:"houseNumber"`
	City         *string          `json:"city"`
	PostalCode   *int             `json:"postalCode"`
	CountryID    *int             `json:"countryId"`
	Country      *country.Country `json:"country,omitempty"`
	PhoneNumber1 *string          `json:"phoneNumber1"`
	PhoneNumber2 *string          `json:"phoneNumber2"`
	PhoneNumber3 *string          `json:"phoneNumber3"`
	Email        *string          `json:"email"`
	Website      *string          `json:"website"`
	CreatedAt    time.Time        `json:"createdAt"`
	UpdatedAt    time.Time        `json:"updatedAt"`
}
