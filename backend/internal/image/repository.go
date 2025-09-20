package image

import "context"

type Repository interface {
	CreateUploadedImage(context.Context, *UploadedImage) error
	CreateGeneratedImage(context.Context, *GeneratedImage) error
	WithTransaction(context.Context, func(Repository) error) error
}
