package image

import (
	"encoding/json"
	"mime"
	"os"
	"path/filepath"
	"sort"
	"strconv"
	"strings"
	"time"
)

func UserImagesDir(userID int) (string, error) {
	storageLocations, err := NewStorageLocations()
	if err != nil {
		return "", err
	}
	return filepath.Join(storageLocations.PrivateImages(), strconv.Itoa(userID)), nil
}

func ScanUserImages(userID int) ([]UserImageItem, error) {
	directory, err := UserImagesDir(userID)
	if err != nil {
		return nil, err
	}
	if _, err := os.Stat(directory); err != nil {
		return []UserImageItem{}, nil
	}
	entries, err := os.ReadDir(directory)
	if err != nil {
		return nil, err
	}
	var items []UserImageItem
	index := 1
	for _, entry := range entries {
		if !entry.Type().IsRegular() {
			continue
		}
		name := entry.Name()
		var imageType string
		var identifier string
		if strings.Contains(name, "_generated_") {
			imageType = "generated"
			identifier = strings.SplitN(name, "_generated_", 2)[0]
		} else if strings.Contains(name, "_original") {
			imageType = "uploaded"
			identifier = strings.SplitN(name, "_original", 2)[0]
		} else {
			imageType = "uploaded"
			identifier = strings.TrimSuffix(name, filepath.Ext(name))
		}

		if !looksLikeUUID(identifier) {
			identifier = randomHex(16)
		}

		fullPath := filepath.Join(directory, name)
		information, err := os.Stat(fullPath)
		if err != nil {
			continue
		}
		createdAt := information.ModTime().Format(time.RFC3339)
		contentType := mime.TypeByExtension(filepath.Ext(name))
		size := information.Size()
		item := UserImageItem{
			ID:               index,
			UUID:             identifier,
			Filename:         name,
			OriginalFilename: nil,
			Type:             imageType,
			ContentType:      stringPtrOrNil(contentType),
			FileSize:         &size,
			PromptID:         nil,
			UploadedImageID:  nil,
			UserID:           userID,
			CreatedAt:        createdAt,
			ImageURL:         "/api/user/images/" + name,
			ThumbnailURL:     nil,
		}
		items = append(items, item)
		index++
	}
	return items, nil
}

func looksLikeUUID(value string) bool {
	if len(value) == 36 && strings.Count(value, "-") == 4 {
		return true
	}
	return false
}

func randomHex(length int) string {
	if randomNameValue, err := randomName(); err == nil {
		if len(randomNameValue) >= length*2 {
			return randomNameValue[:length*2]
		}
		return randomNameValue
	}
	return strconv.FormatInt(time.Now().UnixNano(), 16)
}

func stringPtrOrNil(value string) *string {
	if value == "" {
		return nil
	}
	return &value
}

func SortFilterPaginate(items []UserImageItem, typeFilter, sortBy, sortDirection string, page, size int) UserImagesPage {
	filter := strings.ToLower(typeFilter)
	if filter == "uploaded" || filter == "generated" {
		filtered := make([]UserImageItem, 0, len(items))
		for _, item := range items {
			if item.Type == filter {
				filtered = append(filtered, item)
			}
		}
		items = filtered
	}

	if sortBy == "type" {
		sort.Slice(items, func(firstIndex, secondIndex int) bool {
			return items[firstIndex].Type < items[secondIndex].Type
		})
	} else {
		sort.Slice(items, func(firstIndex, secondIndex int) bool {
			return items[firstIndex].CreatedAt < items[secondIndex].CreatedAt
		})
	}
	if strings.ToUpper(sortDirection) == "DESC" {
		for left, right := 0, len(items)-1; left < right; left, right = left+1, right-1 {
			items[left], items[right] = items[right], items[left]
		}
	}

	totalElements := len(items)
	totalPages := 1
	if size > 0 {
		totalPages = (totalElements + size - 1) / size
	}
	start := page * size
	end := start + size
	var pageItems []UserImageItem
	if start < totalElements {
		if end > totalElements {
			end = totalElements
		}
		pageItems = items[start:end]
	} else {
		pageItems = []UserImageItem{}
	}

	return UserImagesPage{
		Content:       pageItems,
		CurrentPage:   page,
		TotalPages:    totalPages,
		TotalElements: totalElements,
		Size:          size,
	}
}

func (item UserImageItem) MarshalJSON() ([]byte, error) {
	type userImageItemJSON struct {
		ID               int     `json:"id"`
		UUID             string  `json:"uuid"`
		Filename         string  `json:"filename"`
		OriginalFilename *string `json:"originalFilename"`
		Type             string  `json:"type"`
		ContentType      *string `json:"contentType"`
		FileSize         *int64  `json:"fileSize"`
		PromptID         *int    `json:"promptId"`
		UploadedImageID  *int    `json:"uploadedImageId"`
		UserID           int     `json:"userId"`
		CreatedAt        string  `json:"createdAt"`
		ImageURL         string  `json:"imageUrl"`
		ThumbnailURL     *string `json:"thumbnailUrl"`
	}

	payload := userImageItemJSON(item)

	return json.Marshal(payload)
}

func (page UserImagesPage) MarshalJSON() ([]byte, error) {
	type userImagesPageJSON struct {
		Content       []UserImageItem `json:"content"`
		CurrentPage   int             `json:"currentPage"`
		TotalPages    int             `json:"totalPages"`
		TotalElements int             `json:"totalElements"`
		Size          int             `json:"size"`
	}

	payload := userImagesPageJSON(page)

	return json.Marshal(payload)
}
