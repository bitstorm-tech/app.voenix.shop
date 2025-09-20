package vat

import (
	"context"
	"errors"
)

// Domain errors for service consumers to map to HTTP status codes.
var (
	ErrNotFound = errors.New("not found")
	ErrConflict = errors.New("conflict")
)

// Service defines VAT business operations.
type Service interface {
	List(context.Context) ([]ValueAddedTax, error)
	Get(context.Context, int) (ValueAddedTax, error)
	Create(context.Context, ValueAddedTaxCreate) (ValueAddedTax, error)
	Update(context.Context, int, ValueAddedTaxUpdate) (ValueAddedTax, error)
	Delete(context.Context, int) error
}

type service struct {
	repository Repository
}

// NewService constructs a VAT service implementation.
func NewService(repository Repository) Service {
	return &service{repository: repository}
}

func (s *service) List(ctx context.Context) ([]ValueAddedTax, error) {
	return s.repository.List(ctx)
}

func (s *service) Get(ctx context.Context, identifier int) (ValueAddedTax, error) {
	return s.repository.ByID(ctx, identifier)
}

func (s *service) Create(ctx context.Context, payload ValueAddedTaxCreate) (ValueAddedTax, error) {
	created := ValueAddedTax{
		Name:        payload.Name,
		Percent:     payload.Percent,
		Description: payload.Description,
		IsDefault:   payload.IsDefault,
	}

	err := s.repository.WithTransaction(ctx, func(repository Repository) error {
		if payload.IsDefault {
			if err := repository.ClearDefault(ctx); err != nil {
				return err
			}
		}
		if err := repository.Create(ctx, &created); err != nil {
			return err
		}
		return nil
	})
	if err != nil {
		return ValueAddedTax{}, err
	}
	return created, nil
}

func (s *service) Update(ctx context.Context, identifier int, payload ValueAddedTaxUpdate) (ValueAddedTax, error) {
	existing, err := s.repository.ByID(ctx, identifier)
	if err != nil {
		return ValueAddedTax{}, err
	}
	existing.Name = payload.Name
	existing.Percent = payload.Percent
	existing.Description = payload.Description
	existing.IsDefault = payload.IsDefault

	err = s.repository.WithTransaction(ctx, func(repository Repository) error {
		if payload.IsDefault {
			if err := repository.ClearDefault(ctx); err != nil {
				return err
			}
		}
		if err := repository.Update(ctx, &existing); err != nil {
			return err
		}
		return nil
	})
	if err != nil {
		return ValueAddedTax{}, err
	}
	return existing, nil
}

func (s *service) Delete(ctx context.Context, identifier int) error {
	return s.repository.Delete(ctx, identifier)
}
