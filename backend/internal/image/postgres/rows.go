package postgres

import "time"

type uploadedImageRow struct {
	ID               int    `gorm:"primaryKey"`
	UUID             string `gorm:"type:uuid;uniqueIndex;not null"`
	OriginalFilename string `gorm:"size:255;not null"`
	StoredFilename   string `gorm:"size:255;uniqueIndex;not null"`
	ContentType      string `gorm:"size:100;not null"`
	FileSize         int64  `gorm:"not null"`
	UserID           int    `gorm:"not null"`
	CreatedAt        time.Time
}

func (uploadedImageRow) TableName() string { return "uploaded_images" }

type generatedImageRow struct {
	ID              int    `gorm:"primaryKey"`
	UUID            string `gorm:"type:uuid;uniqueIndex;not null"`
	Filename        string `gorm:"size:255;uniqueIndex;not null"`
	PromptID        int    `gorm:"column:prompt_id;not null"`
	UserID          *int   `gorm:"column:user_id"`
	UploadedImageID *int   `gorm:"column:uploaded_image_id"`
	CreatedAt       time.Time
	IPAddress       *string `gorm:"column:ip_address"`
}

func (generatedImageRow) TableName() string { return "generated_images" }
