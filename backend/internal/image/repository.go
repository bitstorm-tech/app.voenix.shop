package image

import "context"

// UserImageFilter defines filtering options for user images
type UserImageFilter struct {
	UserID        int
	Type          string // "uploaded", "generated", or "all"
	Page          int
	Size          int
	SortBy        string // "createdAt", "type"
	SortDirection string // "ASC", "DESC"
}

// Repository defines the interface for image data operations.
// It provides methods for creating, querying, and managing both uploaded and generated images.
type Repository interface {
	// CreateUploadedImage persists a new uploaded image record to the database.
	// The ID and CreatedAt fields will be populated after successful creation.
	CreateUploadedImage(context.Context, *UploadedImage) error

	// CreateGeneratedImage persists a new generated image record to the database.
	// The ID and CreatedAt fields will be populated after successful creation.
	CreateGeneratedImage(context.Context, *GeneratedImage) error

	// GetUploadedImagesByUserID retrieves uploaded images for a specific user with filtering and pagination.
	// Returns the filtered images and the total count of all matching records (before pagination).
	GetUploadedImagesByUserID(context.Context, int, UserImageFilter) ([]UploadedImage, int, error)

	// GetGeneratedImagesByUserID retrieves generated images for a specific user with filtering and pagination.
	// Returns the filtered images and the total count of all matching records (before pagination).
	GetGeneratedImagesByUserID(context.Context, int, UserImageFilter) ([]GeneratedImage, int, error)

	// GetUploadedImageByFilename retrieves a single uploaded image by its stored filename.
	// Returns an error if the image is not found.
	GetUploadedImageByFilename(context.Context, string) (*UploadedImage, error)

	// GetGeneratedImageByFilename retrieves a single generated image by its filename.
	// Returns an error if the image is not found.
	GetGeneratedImageByFilename(context.Context, string) (*GeneratedImage, error)

	// WithTransaction executes the given operation within a database transaction.
	// If the operation returns an error, the transaction is rolled back.
	// Otherwise, the transaction is committed.
	WithTransaction(context.Context, func(Repository) error) error
}
