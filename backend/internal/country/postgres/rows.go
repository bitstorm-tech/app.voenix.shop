package postgres

import "time"

type countryRow struct {
	ID        int    `gorm:"primaryKey"`
	Name      string `gorm:"size:255;unique;not null"`
	CreatedAt time.Time
	UpdatedAt time.Time
}

func (countryRow) TableName() string { return "countries" }
