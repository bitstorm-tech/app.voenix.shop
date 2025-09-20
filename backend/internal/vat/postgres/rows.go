package postgres

import (
	"time"

	"voenix/backend/internal/vat"
)

type valueAddedTaxRow struct {
	ID          int     `gorm:"primaryKey"`
	Name        string  `gorm:"size:255;unique;not null"`
	Percent     int     `gorm:"not null"`
	Description *string `gorm:"type:text"`
	IsDefault   bool    `gorm:"not null;default:false"`
	CreatedAt   time.Time
	UpdatedAt   time.Time
}

func (valueAddedTaxRow) TableName() string {
	return "value_added_taxes"
}

func newValueAddedTaxRow(value vat.ValueAddedTax) valueAddedTaxRow {
	return valueAddedTaxRow{
		ID:          value.ID,
		Name:        value.Name,
		Percent:     value.Percent,
		Description: value.Description,
		IsDefault:   value.IsDefault,
		CreatedAt:   value.CreatedAt,
		UpdatedAt:   value.UpdatedAt,
	}
}

func (row valueAddedTaxRow) toDomain() vat.ValueAddedTax {
	return vat.ValueAddedTax{
		ID:          row.ID,
		Name:        row.Name,
		Percent:     row.Percent,
		Description: row.Description,
		IsDefault:   row.IsDefault,
		CreatedAt:   row.CreatedAt,
		UpdatedAt:   row.UpdatedAt,
	}
}

func (row valueAddedTaxRow) applyTo(value *vat.ValueAddedTax) {
	value.ID = row.ID
	value.Name = row.Name
	value.Percent = row.Percent
	value.Description = row.Description
	value.IsDefault = row.IsDefault
	value.CreatedAt = row.CreatedAt
	value.UpdatedAt = row.UpdatedAt
}
