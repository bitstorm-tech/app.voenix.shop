package pdf

import (
	"path/filepath"

	img "voenix/backend/internal/image"
)

// defaultImageLoader loads from the private user images directory.
func defaultImageLoader(userID int, filename string) ([]byte, string, error) {
	base, err := img.UserImagesDir(userID)
	if err != nil {
		return nil, "", err
	}
	path := filepath.Join(base, filepath.Base(filename))
	b, ct, err := img.LoadImageBytesAndType(path)
	if err != nil {
		return nil, "", err
	}
	return b, ct, nil
}
