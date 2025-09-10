package image

import (
	"bytes"
	"image"
	_ "image/gif"
	_ "image/jpeg"
	"image/png"
	"io"
	"os"
	"path/filepath"
)

// ConvertImageToPNGBytes converts an input image (bytes, reader, or file path) to PNG bytes.
func ConvertImageToPNGBytes(input any) ([]byte, error) {
	var r io.Reader
	switch v := input.(type) {
	case []byte:
		r = bytes.NewReader(v)
	case io.Reader:
		r = v
	case string:
		f, err := os.Open(v)
		if err != nil {
			return nil, err
		}
		defer func() { _ = f.Close() }()
		r = f
	default:
		return nil, ErrUnsupportedInput
	}

	img, _, err := image.Decode(r)
	if err != nil {
		return nil, err
	}

	// Encode as PNG
	var buf bytes.Buffer
	if err := png.Encode(&buf, img); err != nil {
		return nil, err
	}
	return buf.Bytes(), nil
}

// ConvertImageToPNGFile converts an image at inputPath into a PNG file.
// If outputPath is empty, writes alongside input with .png extension.
func ConvertImageToPNGFile(inputPath string, outputPath string, overwrite bool) (string, error) {
	if outputPath == "" {
		outputPath = inputPath[:len(inputPath)-len(filepath.Ext(inputPath))] + ".png"
	}
	if _, err := os.Stat(outputPath); err == nil && !overwrite {
		return "", os.ErrExist
	}
	b, err := ConvertImageToPNGBytes(inputPath)
	if err != nil {
		return "", err
	}
	if err := os.WriteFile(outputPath, b, 0o644); err != nil {
		return "", err
	}
	return outputPath, nil
}

// CropImageBytes crops an image from bytes and returns PNG bytes.
// On any error, returns the original bytes (fail-open behavior).
func CropImageBytes(imageBytes []byte, x, y, width, height float64) []byte {
	img, _, err := image.Decode(bytes.NewReader(imageBytes))
	if err != nil {
		return imageBytes
	}
	b := img.Bounds()
	ix := clampInt(int(x), 0, b.Dx()-1)
	iy := clampInt(int(y), 0, b.Dy()-1)
	iw := clampInt(int(width), 1, b.Dx()-ix)
	ih := clampInt(int(height), 1, b.Dy()-iy)

	rect := image.Rect(ix, iy, ix+iw, iy+ih)
	sub, ok := subImage(img, rect)
	if !ok {
		return imageBytes
	}
	var buf bytes.Buffer
	if err := png.Encode(&buf, sub); err != nil {
		return imageBytes
	}
	return buf.Bytes()
}

func clampInt(v, lo, hi int) int {
	if v < lo {
		return lo
	}
	if v > hi {
		return hi
	}
	return v
}

// subImage attempts to extract a sub-image regardless of underlying type.
func subImage(img image.Image, r image.Rectangle) (image.Image, bool) {
	type subImager interface {
		SubImage(image.Rectangle) image.Image
	}
	if si, ok := img.(subImager); ok {
		return si.SubImage(r), true
	}
	// Fallback: draw into a new RGBA
	dst := image.NewRGBA(r)
	// Draw bounds assume src origin at (0,0); offset rect.
	for y := r.Min.Y; y < r.Max.Y; y++ {
		for x := r.Min.X; x < r.Max.X; x++ {
			dst.Set(x, y, img.At(x, y))
		}
	}
	return dst, true
}

var ErrUnsupportedInput = os.ErrInvalid
