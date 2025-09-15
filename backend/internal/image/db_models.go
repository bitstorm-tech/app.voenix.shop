package image

import "time"

// UploadedImage represents a user-uploaded image persisted in the database.
type UploadedImage struct {
	ID               int       `gorm:"primaryKey" json:"id"`
	UUID             string    `gorm:"type:uuid;uniqueIndex;not null" json:"uuid"`
	OriginalFilename string    `gorm:"size:255;not null" json:"originalFilename"`
	StoredFilename   string    `gorm:"size:255;uniqueIndex;not null" json:"storedFilename"`
	ContentType      string    `gorm:"size:100;not null" json:"contentType"`
	FileSize         int64     `gorm:"not null" json:"fileSize"`
	UserID           int       `gorm:"not null" json:"userId"`
	CreatedAt        time.Time `json:"createdAt"`
}

func (UploadedImage) TableName() string { return "uploaded_images" }

// GeneratedImage represents an AI-generated image persisted in the database.
type GeneratedImage struct {
	ID              int       `gorm:"primaryKey" json:"id"`
	UUID            string    `gorm:"type:uuid;uniqueIndex;not null" json:"uuid"`
	Filename        string    `gorm:"size:255;uniqueIndex;not null" json:"filename"`
	PromptID        int       `gorm:"column:prompt_id;not null" json:"promptId"`
	UserID          *int      `gorm:"column:user_id" json:"userId"`
	UploadedImageID *int      `gorm:"column:uploaded_image_id" json:"uploadedImageId"`
	CreatedAt       time.Time `json:"createdAt"`
	IPAddress       *string   `gorm:"column:ip_address" json:"ipAddress"`
}

func (GeneratedImage) TableName() string { return "generated_images" }
