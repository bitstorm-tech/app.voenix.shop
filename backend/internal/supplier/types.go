package supplier

import (
	"time"

	"voenix/backend/internal/country"
)

type Supplier struct {
	ID           int
	Name         *string
	Title        *string
	FirstName    *string
	LastName     *string
	Street       *string
	HouseNumber  *string
	City         *string
	PostalCode   *int
	CountryID    *int
	Country      *country.Country
	PhoneNumber1 *string
	PhoneNumber2 *string
	PhoneNumber3 *string
	Email        *string
	Website      *string
	CreatedAt    time.Time
	UpdatedAt    time.Time
}
