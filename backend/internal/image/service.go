package image

import "context"

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

func (s *Service) ListUserImages(userID int) ([]UserImageItem, error) {
	return ScanUserImages(userID)
}

func (s *Service) BuildUserImagesPage(items []UserImageItem, typeFilter, sortBy, sortDirection string, page, size int) UserImagesPage {
	return SortFilterPaginate(items, typeFilter, sortBy, sortDirection, page, size)
}
