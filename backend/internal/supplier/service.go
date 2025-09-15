package supplier

import (
	"context"

	"gorm.io/gorm"
)

// Service encapsulates all Supplier-related data access and business logic.
type Service struct {
	db *gorm.DB
}

// NewService constructs a new Supplier service instance.
func NewService(db *gorm.DB) *Service {
	return &Service{db: db}
}

// ListSuppliers returns all suppliers with their associated country preloaded.
func (s *Service) ListSuppliers(ctx context.Context) ([]Supplier, error) {
	var rows []Supplier
	if err := s.db.WithContext(ctx).Preload("Country").Find(&rows).Error; err != nil {
		return nil, err
	}
	return rows, nil
}

// GetSupplierByID returns a single supplier by id with country preloaded.
func (s *Service) GetSupplierByID(ctx context.Context, id int) (*Supplier, error) {
	var row Supplier
	if err := s.db.WithContext(ctx).Preload("Country").First(&row, "id = ?", id).Error; err != nil {
		return nil, err
	}
	return &row, nil
}

// CreateSupplier inserts a new supplier and returns it with country preloaded.
func (s *Service) CreateSupplier(ctx context.Context, payload SupplierCreate) (*Supplier, error) {
	sup := Supplier{
		Name:         payload.Name,
		Title:        payload.Title,
		FirstName:    payload.FirstName,
		LastName:     payload.LastName,
		Street:       payload.Street,
		HouseNumber:  payload.HouseNumber,
		City:         payload.City,
		PostalCode:   payload.PostalCode,
		CountryID:    payload.CountryID,
		PhoneNumber1: payload.PhoneNumber1,
		PhoneNumber2: payload.PhoneNumber2,
		PhoneNumber3: payload.PhoneNumber3,
		Email:        payload.Email,
		Website:      payload.Website,
	}
	if err := s.db.WithContext(ctx).Create(&sup).Error; err != nil {
		return nil, err
	}
	// best-effort preload of country
	_ = s.db.WithContext(ctx).Preload("Country").First(&sup, sup.ID).Error
	return &sup, nil
}

// UpdateSupplier updates an existing supplier by id and returns it with country preloaded.
func (s *Service) UpdateSupplier(ctx context.Context, id int, payload SupplierUpdate) (*Supplier, error) {
	var existing Supplier
	if err := s.db.WithContext(ctx).First(&existing, "id = ?", id).Error; err != nil {
		return nil, err
	}

	existing.Name = payload.Name
	existing.Title = payload.Title
	existing.FirstName = payload.FirstName
	existing.LastName = payload.LastName
	existing.Street = payload.Street
	existing.HouseNumber = payload.HouseNumber
	existing.City = payload.City
	existing.PostalCode = payload.PostalCode
	existing.CountryID = payload.CountryID
	existing.PhoneNumber1 = payload.PhoneNumber1
	existing.PhoneNumber2 = payload.PhoneNumber2
	existing.PhoneNumber3 = payload.PhoneNumber3
	existing.Email = payload.Email
	existing.Website = payload.Website

	if err := s.db.WithContext(ctx).Save(&existing).Error; err != nil {
		return nil, err
	}
	// refresh with preloaded country
	_ = s.db.WithContext(ctx).Preload("Country").First(&existing, existing.ID).Error
	return &existing, nil
}

// DeleteSupplier deletes a supplier by id.
func (s *Service) DeleteSupplier(ctx context.Context, id int) error {
	return s.db.WithContext(ctx).Delete(&Supplier{}, "id = ?", id).Error
}
