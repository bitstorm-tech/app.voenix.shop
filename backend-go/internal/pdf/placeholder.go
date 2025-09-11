package pdf

import (
    "bytes"
    "image"
    "image/color"
    "image/draw"
    "image/png"
)

// DefaultPlaceholderPNG returns a simple in-memory PNG placeholder.
func DefaultPlaceholderPNG() []byte {
    width := 400
    height := 300
    rgba := image.NewRGBA(image.Rect(0, 0, width, height))
    // background
    draw.Draw(rgba, rgba.Bounds(), &image.Uniform{C: color.RGBA{R: 220, G: 220, B: 220, A: 255}}, image.Point{}, draw.Src)
    // simple border
    for x := 0; x < width; x++ {
        rgba.Set(x, 0, color.Black)
        rgba.Set(x, height-1, color.Black)
    }
    for y := 0; y < height; y++ {
        rgba.Set(0, y, color.Black)
        rgba.Set(width-1, y, color.Black)
    }
    // Note: We avoid drawing text here to keep the helper minimal and dependency-free.
    var buf bytes.Buffer
    _ = png.Encode(&buf, rgba)
    return buf.Bytes()
}

