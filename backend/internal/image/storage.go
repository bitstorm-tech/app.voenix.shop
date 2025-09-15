package image

import (
	"crypto/rand"
	"encoding/hex"
	"errors"
	"os"
	"path/filepath"
)

func ensureExt(ext string) string {
	if ext == "" {
		return ".png"
	}
	if ext[0] != '.' {
		return "." + ext
	}
	return ext
}

func randomName() (string, error) {
	var b [16]byte
	if _, err := rand.Read(b[:]); err != nil {
		return "", err
	}
	return hex.EncodeToString(b[:]), nil
}

// StoreImageBytes writes image bytes to a directory and returns the full path.
// - Creates directory if it does not exist.
// - If filename has no extension, appends ext (default .png).
// - If filename is empty, a random name is generated with ext.
// - If overwrite is false and the file exists, returns an error.
func StoreImageBytes(data []byte, directory string, filename string, ext string, overwrite bool) (string, error) {
	if err := os.MkdirAll(directory, 0o755); err != nil {
		return "", err
	}
	var finalName string
	if filename != "" {
		name := filepath.Base(filename)
		if filepath.Ext(name) == "" {
			finalName = name + ensureExt(ext)
		} else {
			finalName = name
		}
	} else {
		rn, err := randomName()
		if err != nil {
			return "", err
		}
		finalName = rn + ensureExt(ext)
	}

	dest := filepath.Join(directory, finalName)
	if _, err := os.Stat(dest); err == nil && !overwrite {
		return "", errors.New("file already exists: " + dest)
	}

	tmp := dest + ".tmp"
	if err := os.WriteFile(tmp, data, 0o644); err != nil {
		return "", err
	}
	if err := os.Rename(tmp, dest); err != nil {
		// Attempt cleanup on rename failure
		_ = os.Remove(tmp)
		return "", err
	}
	return dest, nil
}
