package vat

import "time"

// ValueAddedTax represents the VAT domain model.
type ValueAddedTax struct {
	ID          int
	Name        string
	Percent     int
	Description *string
	IsDefault   bool
	CreatedAt   time.Time
	UpdatedAt   time.Time
}

// ValueAddedTaxCreate captures the attributes required to create a VAT record.
type ValueAddedTaxCreate struct {
	Name        string
	Percent     int
	Description *string
	IsDefault   bool
}

// ValueAddedTaxUpdate mirrors ValueAddedTaxCreate for update operations.
type ValueAddedTaxUpdate = ValueAddedTaxCreate
