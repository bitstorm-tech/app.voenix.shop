package country

import "time"

// Country mirrors the Python SQLModel Country table and the Kotlin CountryDto shape.
type Country struct {
	ID        int       `gorm:"primaryKey" json:"id"`
	Name      string    `gorm:"size:255;unique;not null" json:"name"`
	CreatedAt time.Time `json:"createdAt"`
	UpdatedAt time.Time `json:"updatedAt"`
}

func (Country) TableName() string { return "countries" }
