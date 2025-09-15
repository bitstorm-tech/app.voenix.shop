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
	loc, err := NewStorageLocations()
	if err != nil {
		return "", err
	}
	return filepath.Join(loc.PrivateImages(), strconv.Itoa(userID)), nil
}

func ScanUserImages(userID int) ([]UserImageItem, error) {
	dir, err := UserImagesDir(userID)
	if err != nil {
		return nil, err
	}
	if _, err := os.Stat(dir); err != nil {
		// Directory missing -> empty list
		return []UserImageItem{}, nil
	}
	entries, err := os.ReadDir(dir)
	if err != nil {
		return nil, err
	}
	var items []UserImageItem
	i := 1
	for _, e := range entries {
		if !e.Type().IsRegular() {
			continue
		}
		name := e.Name()
		var imgType, uuidStr string
		if strings.Contains(name, "_generated_") {
			imgType = "generated"
			uuidStr = strings.SplitN(name, "_generated_", 2)[0]
		} else if strings.Contains(name, "_original") {
			imgType = "uploaded"
			uuidStr = strings.SplitN(name, "_original", 2)[0]
		} else {
			imgType = "uploaded"
			uuidStr = strings.TrimSuffix(name, filepath.Ext(name))
		}

		// Validate UUID-ish: accept hex with dashes length 36, else generate random placeholder
		if !looksLikeUUID(uuidStr) {
			// generate pseudo-uuid (not strictly UUID, good enough for UI keying)
			uuidStr = randomHex(16)
		}

		fullPath := filepath.Join(dir, name)
		info, err := os.Stat(fullPath)
		if err != nil {
			continue
		}
		ctime := info.ModTime().Format(time.RFC3339)
		ct := mime.TypeByExtension(filepath.Ext(name))
		sz := info.Size()
		it := UserImageItem{
			ID:               i,
			UUID:             uuidStr,
			Filename:         name,
			OriginalFilename: nil,
			Type:             imgType,
			ContentType:      stringPtrOrNil(ct),
			FileSize:         &sz,
			PromptID:         nil,
			UploadedImageID:  nil,
			UserID:           userID,
			CreatedAt:        ctime,
			ImageURL:         "/api/user/images/" + name,
			ThumbnailURL:     nil,
		}
		items = append(items, it)
		i++
	}
	return items, nil
}

func looksLikeUUID(s string) bool {
	if len(s) == 36 && strings.Count(s, "-") == 4 {
		return true
	}
	return false
}

func randomHex(n int) string {
	// We already have a cryptographic random in storage.go; reuse minimal impl here by calling randomName
	if rn, err := randomName(); err == nil {
		if len(rn) >= n*2 {
			return rn[:n*2]
		}
		return rn
	}
	// worst-case fallback
	return strconv.FormatInt(time.Now().UnixNano(), 16)
}

func stringPtrOrNil(s string) *string {
	if s == "" {
		return nil
	}
	return &s
}

func SortFilterPaginate(items []UserImageItem, typeFilter, sortBy, sortDir string, page, size int) UserImagesPage {
	t := strings.ToLower(typeFilter)
	if t == "uploaded" || t == "generated" {
		filtered := make([]UserImageItem, 0, len(items))
		for _, it := range items {
			if it.Type == t {
				filtered = append(filtered, it)
			}
		}
		items = filtered
	}

	if sortBy == "type" {
		sort.Slice(items, func(i, j int) bool { return items[i].Type < items[j].Type })
	} else {
		// createdAt
		sort.Slice(items, func(i, j int) bool { return items[i].CreatedAt < items[j].CreatedAt })
	}
	if strings.ToUpper(sortDir) == "DESC" {
		// reverse
		for i, j := 0, len(items)-1; i < j; i, j = i+1, j-1 {
			items[i], items[j] = items[j], items[i]
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

// MarshalJSON ensures deterministic key order if needed (not strictly necessary).
func (p UserImagesPage) MarshalJSON() ([]byte, error) {
	type alias UserImagesPage
	return json.Marshal(alias(p))
}
