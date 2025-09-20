package postgres

import (
	"context"
	"errors"
	"strings"

	"gorm.io/gorm"

	"voenix/backend/internal/vat"
)

// Repository persists VAT data using a PostgreSQL database.
type Repository struct {
	database *gorm.DB
}

// NewRepository constructs a PostgreSQL-backed VAT repository.
func NewRepository(database *gorm.DB) *Repository {
	return &Repository{database: database}
}

var _ vat.Repository = (*Repository)(nil)

func (repository *Repository) List(ctx context.Context) ([]vat.ValueAddedTax, error) {
	var rows []valueAddedTaxRow
	if err := repository.database.WithContext(ctx).Find(&rows).Error; err != nil {
		return nil, err
	}
	values := make([]vat.ValueAddedTax, len(rows))
	for index, row := range rows {
		values[index] = row.toDomain()
	}
	return values, nil
}

func (repository *Repository) ByID(ctx context.Context, identifier int) (vat.ValueAddedTax, error) {
	var row valueAddedTaxRow
	if err := repository.database.WithContext(ctx).First(&row, "id = ?", identifier).Error; err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return vat.ValueAddedTax{}, vat.ErrNotFound
		}
		return vat.ValueAddedTax{}, err
	}
	return row.toDomain(), nil
}

func (repository *Repository) Create(ctx context.Context, value *vat.ValueAddedTax) error {
	row := newValueAddedTaxRow(*value)
	if err := repository.database.WithContext(ctx).Create(&row).Error; err != nil {
		if isUniqueViolation(err) {
			return vat.ErrConflict
		}
		return err
	}
	row.applyTo(value)
	return nil
}

func (repository *Repository) Update(ctx context.Context, value *vat.ValueAddedTax) error {
	row := newValueAddedTaxRow(*value)
	if err := repository.database.WithContext(ctx).Save(&row).Error; err != nil {
		if isUniqueViolation(err) {
			return vat.ErrConflict
		}
		return err
	}
	row.applyTo(value)
	return nil
}

func (repository *Repository) Delete(ctx context.Context, identifier int) error {
	return repository.database.WithContext(ctx).Delete(&valueAddedTaxRow{}, "id = ?", identifier).Error
}

func (repository *Repository) ClearDefault(ctx context.Context) error {
	return repository.database.WithContext(ctx).
		Model(&valueAddedTaxRow{}).
		Where("is_default = ?", true).
		Update("is_default", false).
		Error
}

func (repository *Repository) WithTransaction(ctx context.Context, operation func(vat.Repository) error) error {
	return repository.database.WithContext(ctx).Transaction(func(transaction *gorm.DB) error {
		return operation(&Repository{database: transaction})
	})
}

func isUniqueViolation(err error) bool {
	if err == nil {
		return false
	}
	errorMessage := strings.ToLower(err.Error())
	return strings.Contains(errorMessage, "unique constraint") ||
		strings.Contains(errorMessage, "duplicate key value") ||
		strings.Contains(errorMessage, "unique failed")
}
