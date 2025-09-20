package postgres

import (
	"context"

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

func (r *Repository) WithTransaction(ctx context.Context, operation func(image.Repository) error) error {
	return r.database.WithContext(ctx).Transaction(func(transactionalDatabase *gorm.DB) error {
		transactionalRepository := &Repository{database: transactionalDatabase}
		return operation(transactionalRepository)
	})
}
