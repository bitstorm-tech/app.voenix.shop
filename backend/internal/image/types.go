package image

import "time"

type CropArea struct {
	X      float64
	Y      float64
	Width  float64
	Height float64
}

type UploadRequest struct {
	ImageType string
	CropArea  *CropArea
}

type UserImageItem struct {
	ID               int
	UUID             string
	Filename         string
	OriginalFilename *string
	Type             string
	ContentType      *string
	FileSize         *int64
	PromptID         *int
	UploadedImageID  *int
	UserID           int
	CreatedAt        string
	ImageURL         string
	ThumbnailURL     *string
}

type UserImagesPage struct {
	Content       []UserImageItem
	CurrentPage   int
	TotalPages    int
	TotalElements int
	Size          int
}

type UploadedImage struct {
	ID               int
	UUID             string
	OriginalFilename string
	StoredFilename   string
	ContentType      string
	FileSize         int64
	UserID           int
	CreatedAt        time.Time
}

type GeneratedImage struct {
	ID              int
	UUID            string
	Filename        string
	PromptID        int
	UserID          *int
	UploadedImageID *int
	CreatedAt       time.Time
	IPAddress       *string
}
