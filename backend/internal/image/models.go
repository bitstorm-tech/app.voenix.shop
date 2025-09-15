package image

type CropArea struct {
	X      float64 `json:"x"`
	Y      float64 `json:"y"`
	Width  float64 `json:"width"`
	Height float64 `json:"height"`
}

type UploadRequest struct {
	ImageType string    `json:"imageType"`
	CropArea  *CropArea `json:"cropArea,omitempty"`
}

// UserImageItem mirrors the Python structure for frontend compatibility.
type UserImageItem struct {
	ID               int     `json:"id"`
	UUID             string  `json:"uuid"`
	Filename         string  `json:"filename"`
	OriginalFilename *string `json:"originalFilename"`
	Type             string  `json:"type"` // uploaded | generated
	ContentType      *string `json:"contentType"`
	FileSize         *int64  `json:"fileSize"`
	PromptID         *int    `json:"promptId"`
	UploadedImageID  *int    `json:"uploadedImageId"`
	UserID           int     `json:"userId"`
	CreatedAt        string  `json:"createdAt"`
	ImageURL         string  `json:"imageUrl"`
	ThumbnailURL     *string `json:"thumbnailUrl"`
}

type UserImagesPage struct {
	Content       []UserImageItem `json:"content"`
	CurrentPage   int             `json:"currentPage"`
	TotalPages    int             `json:"totalPages"`
	TotalElements int             `json:"totalElements"`
	Size          int             `json:"size"`
}
