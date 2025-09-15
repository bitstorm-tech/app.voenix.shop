package vat

// ValueAddedTaxCreate represents the payload for creating/updating VAT.
type ValueAddedTaxCreate struct {
	Name        string  `json:"name" binding:"required"`
	Percent     int     `json:"percent" binding:"required"`
	Description *string `json:"description"`
	IsDefault   bool    `json:"isDefault"`
}

// ValueAddedTaxUpdate uses the same fields as create.
type ValueAddedTaxUpdate = ValueAddedTaxCreate
