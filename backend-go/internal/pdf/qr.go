package pdf

import (
    "bytes"
    "image"
    "image/color"
    "image/png"

    "github.com/boombuler/barcode"
    barqr "github.com/boombuler/barcode/qr"
)

func generateQRPNG(payload string, size int) ([]byte, error) {
    if size <= 0 {
        size = 100
    }
    // Encode with medium error correction and auto mode.
    code, err := barqr.Encode(payload, barqr.M, barqr.Auto)
    if err != nil {
        return nil, err
    }
    // Scale to requested pixel size.
    code, err = barcode.Scale(code, size, size)
    if err != nil {
        return nil, err
    }
    var buf bytes.Buffer
    if err := png.Encode(&buf, code); err != nil {
        return nil, err
    }
    return buf.Bytes(), nil
}

func image1x1Transparent() image.Image {
    img := image.NewRGBA(image.Rect(0, 0, 1, 1))
    img.Set(0, 0, color.RGBA{0, 0, 0, 0})
    return img
}
