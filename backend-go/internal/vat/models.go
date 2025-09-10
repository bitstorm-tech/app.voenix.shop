package vat

import "time"

// ValueAddedTax represents the VAT table equivalent to the Python SQLModel.
type ValueAddedTax struct {
	ID          int       `gorm:"primaryKey" json:"id"`
	Name        string    `gorm:"size:255;unique;not null" json:"name"`
	Percent     int       `gorm:"not null" json:"percent"`
	Description *string   `gorm:"type:text" json:"description,omitempty"`
	IsDefault   bool      `gorm:"not null;default:false" json:"isDefault"`
	CreatedAt   time.Time `json:"createdAt"`
	UpdatedAt   time.Time `json:"updatedAt"`
}

func (ValueAddedTax) TableName() string { return "value_added_taxes" }

// Payloads
type ValueAddedTaxCreate struct {
	Name        string  `json:"name" binding:"required"`
	Percent     int     `json:"percent" binding:"required"`
	Description *string `json:"description"`
	IsDefault   bool    `json:"isDefault"`
}

type ValueAddedTaxUpdate = ValueAddedTaxCreate
