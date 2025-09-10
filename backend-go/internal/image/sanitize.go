package image

import (
	"errors"
	"regexp"
)

var filenameRe = regexp.MustCompile(`^[A-Za-z0-9._\-]+$`)

// SafeFilename validates a filename using a whitelist pattern and returns it
// or an error if invalid.
func SafeFilename(name string) (string, error) {
	if !filenameRe.MatchString(name) {
		return "", errors.New("Invalid filename")
	}
	return name, nil
}
