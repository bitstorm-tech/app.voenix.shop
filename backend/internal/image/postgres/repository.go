package postgres

import (
	"context"
	"errors"
	"fmt"

	"gorm.io/gorm"

	"voenix/backend/internal/image"
)

type Repository struct {
	database *gorm.DB
}

func NewRepository(database *gorm.DB) *Repository {
	return &Repository{database: database}
}

var _ image.Repository = (*Repository)(nil)

func (r *Repository) CreateUploadedImage(ctx context.Context, uploadedImage *image.UploadedImage) error {
	row := uploadedImageRow{
		UUID:             uploadedImage.UUID,
		OriginalFilename: uploadedImage.OriginalFilename,
		StoredFilename:   uploadedImage.StoredFilename,
		ContentType:      uploadedImage.ContentType,
		FileSize:         uploadedImage.FileSize,
		UserID:           uploadedImage.UserID,
		CreatedAt:        uploadedImage.CreatedAt,
	}

	if err := r.database.WithContext(ctx).Create(&row).Error; err != nil {
		return err
	}

	uploadedImage.ID = row.ID
	uploadedImage.CreatedAt = row.CreatedAt

	return nil
}

func (r *Repository) CreateGeneratedImage(ctx context.Context, generatedImage *image.GeneratedImage) error {
	row := generatedImageRow{
		UUID:            generatedImage.UUID,
		Filename:        generatedImage.Filename,
		PromptID:        generatedImage.PromptID,
		UserID:          generatedImage.UserID,
		UploadedImageID: generatedImage.UploadedImageID,
		CreatedAt:       generatedImage.CreatedAt,
		IPAddress:       generatedImage.IPAddress,
	}

	if err := r.database.WithContext(ctx).Create(&row).Error; err != nil {
		return err
	}

	generatedImage.ID = row.ID
	generatedImage.CreatedAt = row.CreatedAt

	return nil
}

func (r *Repository) GetUploadedImagesByUserID(ctx context.Context, userID int, filter image.UserImageFilter) ([]image.UploadedImage, int, error) {
	var rows []uploadedImageRow
	var total int64

	query := r.database.WithContext(ctx).Model(&uploadedImageRow{}).Where("user_id = ?", userID)

	// Count total records
	if err := query.Count(&total).Error; err != nil {
		return nil, 0, fmt.Errorf("failed to count uploaded images: %w", err)
	}

	// Apply sorting
	orderClause := "created_at DESC"
	if filter.SortBy == "type" {
		orderClause = fmt.Sprintf("'uploaded' %s", filter.SortDirection)
	} else if filter.SortDirection == "ASC" {
		orderClause = "created_at ASC"
	}

	// Apply pagination and execute query
	offset := filter.Page * filter.Size
	if err := query.Order(orderClause).Offset(offset).Limit(filter.Size).Find(&rows).Error; err != nil {
		return nil, 0, fmt.Errorf("failed to fetch uploaded images: %w", err)
	}

	// Convert rows to domain objects
	images := make([]image.UploadedImage, len(rows))
	for i, row := range rows {
		images[i] = image.UploadedImage{
			ID:               row.ID,
			UUID:             row.UUID,
			OriginalFilename: row.OriginalFilename,
			StoredFilename:   row.StoredFilename,
			ContentType:      row.ContentType,
			FileSize:         row.FileSize,
			UserID:           row.UserID,
			CreatedAt:        row.CreatedAt,
		}
	}

	return images, int(total), nil
}

func (r *Repository) GetGeneratedImagesByUserID(ctx context.Context, userID int, filter image.UserImageFilter) ([]image.GeneratedImage, int, error) {
	var rows []generatedImageRow
	var total int64

	query := r.database.WithContext(ctx).Model(&generatedImageRow{}).Where("user_id = ?", userID)

	// Count total records
	if err := query.Count(&total).Error; err != nil {
		return nil, 0, fmt.Errorf("failed to count generated images: %w", err)
	}

	// Apply sorting
	orderClause := "created_at DESC"
	if filter.SortBy == "type" {
		orderClause = fmt.Sprintf("'generated' %s", filter.SortDirection)
	} else if filter.SortDirection == "ASC" {
		orderClause = "created_at ASC"
	}

	// Apply pagination and execute query
	offset := filter.Page * filter.Size
	if err := query.Order(orderClause).Offset(offset).Limit(filter.Size).Find(&rows).Error; err != nil {
		return nil, 0, fmt.Errorf("failed to fetch generated images: %w", err)
	}

	// Convert rows to domain objects
	images := make([]image.GeneratedImage, len(rows))
	for i, row := range rows {
		images[i] = image.GeneratedImage{
			ID:              row.ID,
			UUID:            row.UUID,
			Filename:        row.Filename,
			PromptID:        row.PromptID,
			UserID:          row.UserID,
			UploadedImageID: row.UploadedImageID,
			CreatedAt:       row.CreatedAt,
			IPAddress:       row.IPAddress,
		}
	}

	return images, int(total), nil
}

func (r *Repository) GetUploadedImageByFilename(ctx context.Context, filename string) (*image.UploadedImage, error) {
	var row uploadedImageRow
	err := r.database.WithContext(ctx).Where("stored_filename = ?", filename).First(&row).Error
	if err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, fmt.Errorf("uploaded image not found: %w", err)
		}
		return nil, fmt.Errorf("failed to fetch uploaded image: %w", err)
	}

	return &image.UploadedImage{
		ID:               row.ID,
		UUID:             row.UUID,
		OriginalFilename: row.OriginalFilename,
		StoredFilename:   row.StoredFilename,
		ContentType:      row.ContentType,
		FileSize:         row.FileSize,
		UserID:           row.UserID,
		CreatedAt:        row.CreatedAt,
	}, nil
}

func (r *Repository) GetGeneratedImageByFilename(ctx context.Context, filename string) (*image.GeneratedImage, error) {
	var row generatedImageRow
	err := r.database.WithContext(ctx).Where("filename = ?", filename).First(&row).Error
	if err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, fmt.Errorf("generated image not found: %w", err)
		}
		return nil, fmt.Errorf("failed to fetch generated image: %w", err)
	}

	return &image.GeneratedImage{
		ID:              row.ID,
		UUID:            row.UUID,
		Filename:        row.Filename,
		PromptID:        row.PromptID,
		UserID:          row.UserID,
		UploadedImageID: row.UploadedImageID,
		CreatedAt:       row.CreatedAt,
		IPAddress:       row.IPAddress,
	}, nil
}

func (r *Repository) WithTransaction(ctx context.Context, operation func(image.Repository) error) error {
	return r.database.WithContext(ctx).Transaction(func(transactionalDatabase *gorm.DB) error {
		transactionalRepository := &Repository{database: transactionalDatabase}
		return operation(transactionalRepository)
	})
}
