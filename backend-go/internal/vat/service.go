package vat

import (
	"context"
	"errors"
	"fmt"
	"strings"

	"gorm.io/gorm"
)

// Domain errors for service consumers to map to HTTP status codes.
var (
	ErrNotFound = errors.New("not found")
	ErrConflict = errors.New("conflict")
)

// Service defines VAT business operations.
type Service interface {
	List(ctx context.Context) ([]ValueAddedTax, error)
	Get(ctx context.Context, id int) (ValueAddedTax, error)
	Create(ctx context.Context, payload ValueAddedTaxCreate) (ValueAddedTax, error)
	Update(ctx context.Context, id int, payload ValueAddedTaxUpdate) (ValueAddedTax, error)
	Delete(ctx context.Context, id int) error
}

type service struct {
	db *gorm.DB
}

// NewVATService constructs a VAT service implementation.
func NewVATService(db *gorm.DB) Service { return &service{db: db} }

func (s *service) List(ctx context.Context) ([]ValueAddedTax, error) {
	var rows []ValueAddedTax
	if err := s.db.WithContext(ctx).Find(&rows).Error; err != nil {
		return nil, err
	}
	return rows, nil
}

func (s *service) Get(ctx context.Context, id int) (ValueAddedTax, error) {
	var row ValueAddedTax
	if err := s.db.WithContext(ctx).First(&row, "id = ?", id).Error; err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return ValueAddedTax{}, ErrNotFound
		}
		return ValueAddedTax{}, err
	}
	return row, nil
}

func (s *service) Create(ctx context.Context, payload ValueAddedTaxCreate) (ValueAddedTax, error) {
	var created ValueAddedTax
	err := s.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		if payload.IsDefault {
			if err := tx.Model(&ValueAddedTax{}).Where("is_default = ?", true).Update("is_default", false).Error; err != nil {
				return err
			}
		}
		created = ValueAddedTax{
			Name:        payload.Name,
			Percent:     payload.Percent,
			Description: payload.Description,
			IsDefault:   payload.IsDefault,
		}
		if err := tx.Create(&created).Error; err != nil {
			if isUniqueViolation(err) {
				return fmt.Errorf("%w: vat with this name exists", ErrConflict)
			}
			return err
		}
		return nil
	})
	if err != nil {
		return ValueAddedTax{}, err
	}
	return created, nil
}

func (s *service) Update(ctx context.Context, id int, payload ValueAddedTaxUpdate) (ValueAddedTax, error) {
	var existing ValueAddedTax
	if err := s.db.WithContext(ctx).First(&existing, "id = ?", id).Error; err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return ValueAddedTax{}, ErrNotFound
		}
		return ValueAddedTax{}, err
	}

	if err := s.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		if payload.IsDefault {
			if err := tx.Model(&ValueAddedTax{}).Where("is_default = ?", true).Update("is_default", false).Error; err != nil {
				return err
			}
		}
		existing.Name = payload.Name
		existing.Percent = payload.Percent
		existing.Description = payload.Description
		existing.IsDefault = payload.IsDefault
		if err := tx.Save(&existing).Error; err != nil {
			if isUniqueViolation(err) {
				return fmt.Errorf("%w: vat with this name exists", ErrConflict)
			}
			return err
		}
		return nil
	}); err != nil {
		return ValueAddedTax{}, err
	}
	return existing, nil
}

func (s *service) Delete(ctx context.Context, id int) error {
	// Match previous behavior: do not error if nothing deleted.
	return s.db.WithContext(ctx).Delete(&ValueAddedTax{}, "id = ?", id).Error
}

// isUniqueViolation attempts to detect unique constraint errors from common drivers.
func isUniqueViolation(err error) bool {
	if err == nil {
		return false
	}
	s := strings.ToLower(err.Error())
	return strings.Contains(s, "unique constraint") ||
		strings.Contains(s, "duplicate key value") ||
		strings.Contains(s, "unique failed")
}
