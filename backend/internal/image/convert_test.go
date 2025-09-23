package image

import (
	"bytes"
	"image"
	"image/color"
	"image/png"
	"testing"
)

func TestCropImageBytesOutOfBoundsReturnsOriginal(t *testing.T) {
	img := image.NewRGBA(image.Rect(0, 0, 10, 10))
	img.Set(0, 0, color.RGBA{R: 200, G: 100, B: 50, A: 255})
	var buffer bytes.Buffer
	if err := png.Encode(&buffer, img); err != nil {
		t.Fatalf("encode source image: %v", err)
	}
	originalBytes := buffer.Bytes()
	croppedBytes := CropImageBytes(originalBytes, 1000, 1000, 50, 50)
	if !bytes.Equal(croppedBytes, originalBytes) {
		t.Fatalf("expected original bytes when crop is out of bounds")
	}
}

func TestCropImageBytesReturnsCroppedContent(t *testing.T) {
	img := image.NewRGBA(image.Rect(0, 0, 10, 10))
	img.Set(5, 5, color.RGBA{R: 255, G: 0, B: 0, A: 255})
	var buffer bytes.Buffer
	if err := png.Encode(&buffer, img); err != nil {
		t.Fatalf("encode source image: %v", err)
	}
	originalBytes := buffer.Bytes()
	croppedBytes := CropImageBytes(originalBytes, 4, 4, 4, 4)
	if bytes.Equal(croppedBytes, originalBytes) {
		t.Fatalf("expected cropped image to differ from original bytes")
	}
	croppedImage, _, err := image.Decode(bytes.NewReader(croppedBytes))
	if err != nil {
		t.Fatalf("decode cropped image: %v", err)
	}
	if croppedImage.Bounds().Dx() != 4 || croppedImage.Bounds().Dy() != 4 {
		t.Fatalf("unexpected crop dimensions: got %dx%d", croppedImage.Bounds().Dx(), croppedImage.Bounds().Dy())
	}
	converted := color.RGBAModel.Convert(croppedImage.At(1, 1))
	convertedColor, ok := converted.(color.RGBA)
	if !ok {
		t.Fatalf("unexpected color model conversion type %T", converted)
	}
	expectedColor := color.RGBA{R: 255, G: 0, B: 0, A: 255}
	if convertedColor != expectedColor {
		t.Fatalf("cropped image missing expected pixel color")
	}
}
