package supplier

import "context"

type Service struct {
	repository Repository
}

func NewService(repository Repository) *Service {
	return &Service{repository: repository}
}

func (s *Service) ListSuppliers(contextValue context.Context) ([]Supplier, error) {
	return s.repository.List(contextValue)
}

func (s *Service) GetSupplierByID(contextValue context.Context, supplierID int) (*Supplier, error) {
	supplierValue, errorValue := s.repository.ByID(contextValue, supplierID)
	if errorValue != nil {
		return nil, errorValue
	}

	return &supplierValue, nil
}

func (s *Service) CreateSupplier(contextValue context.Context, supplier Supplier) (*Supplier, error) {
	supplierCopy := supplier
	if errorValue := s.repository.Create(contextValue, &supplierCopy); errorValue != nil {
		return nil, errorValue
	}

	return &supplierCopy, nil
}

func (s *Service) UpdateSupplier(contextValue context.Context, supplierID int, updates Supplier) (*Supplier, error) {
	existingSupplier, errorValue := s.repository.ByID(contextValue, supplierID)
	if errorValue != nil {
		return nil, errorValue
	}

	existingSupplier.Name = updates.Name
	existingSupplier.Title = updates.Title
	existingSupplier.FirstName = updates.FirstName
	existingSupplier.LastName = updates.LastName
	existingSupplier.Street = updates.Street
	existingSupplier.HouseNumber = updates.HouseNumber
	existingSupplier.City = updates.City
	existingSupplier.PostalCode = updates.PostalCode
	existingSupplier.CountryID = updates.CountryID
	existingSupplier.PhoneNumber1 = updates.PhoneNumber1
	existingSupplier.PhoneNumber2 = updates.PhoneNumber2
	existingSupplier.PhoneNumber3 = updates.PhoneNumber3
	existingSupplier.Email = updates.Email
	existingSupplier.Website = updates.Website

	if errorValue := s.repository.Update(contextValue, &existingSupplier); errorValue != nil {
		return nil, errorValue
	}

	return &existingSupplier, nil
}

func (s *Service) DeleteSupplier(contextValue context.Context, supplierID int) error {
	return s.repository.Delete(contextValue, supplierID)
}
