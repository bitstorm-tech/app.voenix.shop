package pdf

// Service defines the PDF generator API.
type Service interface {
    GenerateOrderPDF(data OrderPdfData) ([]byte, error)
}

// Options allows customizing behavior of the generator.
type Options struct {
    Config       Config
    ImageLoader  ImageLoaderFunc // optional; if nil, uses default loader
}

// ImageLoaderFunc loads image bytes (and optional content-type) for a user-owned filename.
// Return content-type best effort (e.g., image/png). If not known, return empty.
type ImageLoaderFunc func(userID int, filename string) (data []byte, contentType string, err error)
