package image

import (
	"context"
	"fmt"
	"io"
	"os"
	"path/filepath"
)

type Service struct {
	repository Repository
}

func NewService(repository Repository) *Service {
	return &Service{repository: repository}
}

func (s *Service) CreateUploadedImage(ctx context.Context, uploadedImage *UploadedImage) error {
	return s.repository.CreateUploadedImage(ctx, uploadedImage)
}

func (s *Service) CreateGeneratedImage(ctx context.Context, generatedImage *GeneratedImage) error {
	return s.repository.CreateGeneratedImage(ctx, generatedImage)
}

func (s *Service) WithTransaction(ctx context.Context, operation func(*Service) error) error {
	return s.repository.WithTransaction(ctx, func(nestedRepository Repository) error {
		transactionalService := &Service{repository: nestedRepository}
		return operation(transactionalService)
	})
}

func (s *Service) UploadAdminImage(ctx context.Context, fileReader io.Reader, imageType string, cropArea *CropArea) (string, string, error) {
	imageData, err := io.ReadAll(fileReader)
	if err != nil {
		return "", "", fmt.Errorf("failed to read uploaded file: %w", err)
	}

	if cropArea != nil {
		imageData = CropImageBytes(imageData, cropArea.X, cropArea.Y, cropArea.Width, cropArea.Height)
	}

	webpImageBytes, err := ConvertImageToWebPBytes(imageData)
	if err != nil {
		return "", "", fmt.Errorf("failed to process image: %w", err)
	}

	storageLocations, err := NewStorageLocations()
	if err != nil {
		return "", "", fmt.Errorf("failed to initialize storage: %w", err)
	}

	targetDirectory, err := storageLocations.ResolveAdminDir(imageType)
	if err != nil {
		return "", "", fmt.Errorf("failed to resolve target directory: %w", err)
	}

	storedPath, err := StoreImageBytes(webpImageBytes, targetDirectory, "", "webp", false)
	if err != nil {
		return "", "", fmt.Errorf("failed to store image: %w", err)
	}

	return filepath.Base(storedPath), imageType, nil
}

func (s *Service) GetUserImage(ctx context.Context, userID int, filename string) ([]byte, string, error) {
	safeFilename, err := SafeFilename(filename)
	if err != nil {
		return nil, "", fmt.Errorf("invalid filename: %w", err)
	}

	userImageDir, err := UserImagesDir(userID)
	if err != nil {
		return nil, "", fmt.Errorf("failed to get user image directory: %w", err)
	}

	filePath := filepath.Join(userImageDir, safeFilename)
	imageBytes, contentType, err := LoadImageBytesAndType(filePath)
	if err != nil {
		return nil, "", fmt.Errorf("failed to load image: %w", err)
	}

	return imageBytes, contentType, nil
}

func (s *Service) GetPromptTestImage(ctx context.Context, filename string) ([]byte, string, error) {
	safeFilename, err := SafeFilename(filename)
	if err != nil {
		return nil, "", fmt.Errorf("invalid filename: %w", err)
	}

	storageLocations, err := NewStorageLocations()
	if err != nil {
		return nil, "", fmt.Errorf("failed to initialize storage: %w", err)
	}

	filePath := filepath.Join(storageLocations.PromptTest(), safeFilename)
	imageBytes, contentType, err := LoadImageBytesAndType(filePath)
	if err != nil {
		return nil, "", fmt.Errorf("failed to load image: %w", err)
	}

	return imageBytes, contentType, nil
}

func (s *Service) DeletePromptTestImage(ctx context.Context, filename string) error {
	safeFilename, err := SafeFilename(filename)
	if err != nil {
		return fmt.Errorf("invalid filename: %w", err)
	}

	storageLocations, err := NewStorageLocations()
	if err != nil {
		return fmt.Errorf("failed to initialize storage: %w", err)
	}

	filePath := filepath.Join(storageLocations.PromptTest(), safeFilename)
	if _, err := os.Stat(filePath); err == nil {
		if err := os.Remove(filePath); err != nil {
			return fmt.Errorf("failed to delete file: %w", err)
		}
	}

	return nil
}

func (s *Service) ListUserImages(userID int) ([]UserImageItem, error) {
	return ScanUserImages(userID)
}

func (s *Service) BuildUserImagesPage(items []UserImageItem, typeFilter, sortBy, sortDirection string, page, size int) UserImagesPage {
	return SortFilterPaginate(items, typeFilter, sortBy, sortDirection, page, size)
}
