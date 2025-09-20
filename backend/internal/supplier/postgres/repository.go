package postgres

import (
	"context"

	"gorm.io/gorm"

	"voenix/backend/internal/supplier"
)

type Repository struct {
	database *gorm.DB
}

func NewRepository(database *gorm.DB) *Repository {
	return &Repository{database: database}
}

var _ supplier.Repository = (*Repository)(nil)

func (r *Repository) List(contextValue context.Context) ([]supplier.Supplier, error) {
	var rowList []supplierRow
	if errorValue := r.database.WithContext(contextValue).Preload("Country").Find(&rowList).Error; errorValue != nil {
		return nil, errorValue
	}

	supplierList := make([]supplier.Supplier, len(rowList))
	for index, rowValue := range rowList {
		supplierList[index] = convertRowToDomain(rowValue)
	}

	return supplierList, nil
}

func (r *Repository) ByID(contextValue context.Context, supplierID int) (supplier.Supplier, error) {
	var rowValue supplierRow
	if errorValue := r.database.WithContext(contextValue).Preload("Country").First(&rowValue, "id = ?", supplierID).Error; errorValue != nil {
		return supplier.Supplier{}, errorValue
	}

	return convertRowToDomain(rowValue), nil
}

func (r *Repository) Create(contextValue context.Context, supplierValue *supplier.Supplier) error {
	rowValue := convertDomainToRow(*supplierValue)
	if errorValue := r.database.WithContext(contextValue).Create(&rowValue).Error; errorValue != nil {
		return errorValue
	}

	var refreshedRow supplierRow
	if errorValue := r.database.WithContext(contextValue).Preload("Country").First(&refreshedRow, rowValue.ID).Error; errorValue != nil {
		return errorValue
	}

	*supplierValue = convertRowToDomain(refreshedRow)
	return nil
}

func (r *Repository) Update(contextValue context.Context, supplierValue *supplier.Supplier) error {
	rowValue := convertDomainToRow(*supplierValue)
	if errorValue := r.database.WithContext(contextValue).Save(&rowValue).Error; errorValue != nil {
		return errorValue
	}

	var refreshedRow supplierRow
	if errorValue := r.database.WithContext(contextValue).Preload("Country").First(&refreshedRow, rowValue.ID).Error; errorValue != nil {
		return errorValue
	}

	*supplierValue = convertRowToDomain(refreshedRow)
	return nil
}

func (r *Repository) Delete(contextValue context.Context, supplierID int) error {
	return r.database.WithContext(contextValue).Delete(&supplierRow{}, "id = ?", supplierID).Error
}

func convertRowToDomain(rowValue supplierRow) supplier.Supplier {
	return supplier.Supplier{
		ID:           rowValue.ID,
		Name:         rowValue.Name,
		Title:        rowValue.Title,
		FirstName:    rowValue.FirstName,
		LastName:     rowValue.LastName,
		Street:       rowValue.Street,
		HouseNumber:  rowValue.HouseNumber,
		City:         rowValue.City,
		PostalCode:   rowValue.PostalCode,
		CountryID:    rowValue.CountryID,
		Country:      rowValue.Country,
		PhoneNumber1: rowValue.PhoneNumber1,
		PhoneNumber2: rowValue.PhoneNumber2,
		PhoneNumber3: rowValue.PhoneNumber3,
		Email:        rowValue.Email,
		Website:      rowValue.Website,
		CreatedAt:    rowValue.CreatedAt,
		UpdatedAt:    rowValue.UpdatedAt,
	}
}

func convertDomainToRow(domainValue supplier.Supplier) supplierRow {
	return supplierRow{
		ID:           domainValue.ID,
		Name:         domainValue.Name,
		Title:        domainValue.Title,
		FirstName:    domainValue.FirstName,
		LastName:     domainValue.LastName,
		Street:       domainValue.Street,
		HouseNumber:  domainValue.HouseNumber,
		City:         domainValue.City,
		PostalCode:   domainValue.PostalCode,
		CountryID:    domainValue.CountryID,
		PhoneNumber1: domainValue.PhoneNumber1,
		PhoneNumber2: domainValue.PhoneNumber2,
		PhoneNumber3: domainValue.PhoneNumber3,
		Email:        domainValue.Email,
		Website:      domainValue.Website,
		CreatedAt:    domainValue.CreatedAt,
		UpdatedAt:    domainValue.UpdatedAt,
	}
}
