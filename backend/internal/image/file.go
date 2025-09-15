package image

import (
	"io"
	"mime"
	"net/http"
	"os"
	"path/filepath"
)

// LoadImageBytesAndType reads the file and returns its bytes and a best-effort content type.
// Falls back to image/png if type cannot be guessed.
func LoadImageBytesAndType(path string) ([]byte, string, error) {
	b, err := os.ReadFile(path)
	if err != nil {
		return nil, "", err
	}
	// Guess by extension first
	ctype := mime.TypeByExtension(filepath.Ext(path))
	if ctype == "" {
		// Fallback to sniffing
		if len(b) >= 512 {
			ctype = http.DetectContentType(b[:512])
		} else {
			ctype = http.DetectContentType(append(b, make([]byte, 512-len(b))...))
		}
	}
	if ctype == "application/octet-stream" || ctype == "" {
		ctype = "image/png"
	}
	return b, ctype, nil
}

// CopyN returns the first n bytes from r without consuming beyond it.
func CopyN(r io.Reader, n int64) ([]byte, error) {
	buf := make([]byte, n)
	m, err := io.ReadFull(r, buf)
	if err != nil && err != io.ErrUnexpectedEOF && err != io.EOF {
		return nil, err
	}
	return buf[:m], nil
}
