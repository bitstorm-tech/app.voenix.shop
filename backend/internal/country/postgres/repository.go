package postgres

import (
	"context"

	"gorm.io/gorm"

	"voenix/backend/internal/country"
)

type Repository struct {
	db *gorm.DB
}

func NewRepository(db *gorm.DB) *Repository {
	return &Repository{db: db}
}

var _ country.Repository = (*Repository)(nil)

func (r *Repository) All(ctx context.Context) ([]country.Country, error) {
	var rows []countryRow
	if err := r.db.WithContext(ctx).Find(&rows).Error; err != nil {
		return nil, err
	}

	countries := make([]country.Country, len(rows))
	for i, row := range rows {
		countries[i] = country.Country{
			ID:        row.ID,
			Name:      row.Name,
			CreatedAt: row.CreatedAt,
			UpdatedAt: row.UpdatedAt,
		}
	}

	return countries, nil
}
