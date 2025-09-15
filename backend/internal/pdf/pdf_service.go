package pdf

import (
	"bytes"
	"errors"
	"fmt"
	"image"
	"image/draw"
	"image/png"
	"math"
	"strings"

	"github.com/signintech/gopdf"
	gobold "golang.org/x/image/font/gofont/gobold"
	goregular "golang.org/x/image/font/gofont/goregular"
)

// PDFService implements Service using a PDF backend.
type PDFService struct {
	config Config
	loader ImageLoaderFunc
}

// NewService constructs a new service with options.
func NewService(options Options) *PDFService {
	config := options.Config
	if config.Size.WidthMM == 0 || config.Size.HeightMM == 0 {
		config = DefaultConfig()
	}
	service := &PDFService{config: config}
	if options.ImageLoader != nil {
		service.loader = options.ImageLoader
	} else {
		service.loader = defaultImageLoader
	}
	return service
}

func (service *PDFService) GenerateOrderPDF(data OrderPdfData) ([]byte, error) {
	total := data.TotalItemCount()
	if total == 0 {
		total = 1
	}

	// Prepare PDF
	widthPoints, heightPoints := service.pageSizeForFirst(data)
	var out bytes.Buffer
	document := gopdf.GoPdf{}
	document.Start(gopdf.Config{PageSize: gopdf.Rect{W: widthPoints, H: heightPoints}})
	document.AddPage()

	// Register embedded Go fonts to avoid external font files.
	_ = document.AddTTFFontData("Go-Regular", goregular.TTF)
	_ = document.AddTTFFontData("Go-Bold", gobold.TTF)
	_ = document.SetFont("Go-Regular", "", service.config.Fonts.HeaderSizePt)

	pageNumber := 1
	for i := range data.Items {
		item := data.Items[i]
		quantity := item.Quantity
		if quantity <= 0 {
			continue
		}
		for j := 0; j < quantity; j++ {
			if pageNumber > 1 {
				itemWidthPoints, itemHeightPoints := service.pageSizeForItem(item)
				document.AddPageWithOption(gopdf.PageOption{PageSize: &gopdf.Rect{W: itemWidthPoints, H: itemHeightPoints}})
				_ = document.SetFont("Go-Regular", "", service.config.Fonts.HeaderSizePt)
			} else {
				// Resize first page if different
				itemWidthPoints, itemHeightPoints := service.pageSizeForItem(item)
				if math.Abs(itemWidthPoints-widthPoints) > 0.1 || math.Abs(itemHeightPoints-heightPoints) > 0.1 {
					document.AddPageWithOption(gopdf.PageOption{PageSize: &gopdf.Rect{W: itemWidthPoints, H: itemHeightPoints}})
					_ = document.SetFont("Go-Regular", "", service.config.Fonts.HeaderSizePt)
				}
			}
			if err := service.drawPage(&document, data, item, pageNumber, total); err != nil {
				return nil, err
			}
			pageNumber++
		}
	}

	if pageNumber == 1 {
		_ = service.drawPage(&document, data, OrderItemPdfData{Quantity: 1, Article: ArticlePdfData{}}, 1, 1)
	}

	if _, err := document.WriteTo(&out); err != nil {
		return nil, err
	}
	return out.Bytes(), nil
}

// drawPage renders a single page following the intended layout.
func (service *PDFService) drawPage(document *gopdf.GoPdf, data OrderPdfData, item OrderItemPdfData, page, total int) error {
	pageWidth, pageHeight := service.pageSizeForItem(item)

	// Right vertical product info
	if info := service.productInfoLine(item); info != "" {
		_ = document.SetFont("Arial", "", service.config.Fonts.HeaderSizePt)
		if w, err := document.MeasureTextWidth(info); err == nil {
			x := pageWidth - 15.0
			y := (pageHeight + w) / 2
			document.SetX(x)
			document.SetY(y)
			document.Rotate(90, x, y)
			_ = document.Cell(nil, info)
			document.RotateReset()
		} else {
			document.SetX(pageWidth - 5)
			document.SetY(pageHeight / 2)
			document.Rotate(90, pageWidth-5, pageHeight/2)
			_ = document.Cell(nil, info)
			document.RotateReset()
		}
	}

	// Centered product image at intended print size
	if err := service.drawCenteredImage(document, data, item, pageWidth, pageHeight); err != nil {
		return err
	}

	// QR bottom-left (within margin)
	if data.ID != "" {
		margin := service.marginForItem(item)
		qrSize := service.config.QRCode.SizePt
		x := margin
		y := pageHeight - margin - qrSize
		document.RotateReset()
		if err := service.drawQRCode(document, data.ID, x, y, qrSize); err != nil {
			_ = err
		}
	}

	// Left vertical header (order number + page/total)
	if header := service.headerText(data, page, total); header != "" {
		_ = document.SetFont("Arial", "", service.config.Fonts.HeaderSizePt)
		if w, err := document.MeasureTextWidth(header); err == nil {
			x := 5.0
			y := (pageHeight + w) / 2
			document.SetX(x)
			document.SetY(y)
			document.Rotate(90, x, y)
			_ = document.Cell(nil, header)
			document.RotateReset()
		} else {
			document.SetX(5)
			document.SetY(pageHeight / 2)
			document.Rotate(90, 5, pageHeight/2)
			_ = document.Cell(nil, header)
			document.RotateReset()
		}
	}
	return nil
}

func (service *PDFService) pageSizeForFirst(data OrderPdfData) (float64, float64) {
	if len(data.Items) == 0 {
		return service.config.Size.WidthMM * MMToPoints, service.config.Size.HeightMM * MMToPoints
	}
	return service.pageSizeForItem(data.Items[0])
}

func (service *PDFService) pageSizeForItem(item OrderItemPdfData) (float64, float64) {
	width := service.config.Size.WidthMM * MMToPoints
	height := service.config.Size.HeightMM * MMToPoints
	if mugDetails := item.Article.MugDetails; mugDetails != nil {
		if mugDetails.DocumentFormatWidthMM != nil {
			width = float64(*mugDetails.DocumentFormatWidthMM) * MMToPoints
		}
		if mugDetails.DocumentFormatHeightMM != nil {
			height = float64(*mugDetails.DocumentFormatHeightMM) * MMToPoints
		}
	}
	return width, height
}

func (service *PDFService) marginForItem(item OrderItemPdfData) float64 {
	if mugDetails := item.Article.MugDetails; mugDetails != nil {
		if mugDetails.DocumentFormatMarginBottomMM != nil {
			return float64(*mugDetails.DocumentFormatMarginBottomMM) * MMToPoints
		}
	}
	return service.config.MarginMM * MMToPoints
}

func (service *PDFService) headerText(data OrderPdfData, page, total int) string {
	order := "UNKNOWN"
	if data.OrderNumber != nil && *data.OrderNumber != "" {
		order = *data.OrderNumber
	}
	return fmt.Sprintf("%s (%d/%d)", order, page, total)
}

func (service *PDFService) productInfoLine(item OrderItemPdfData) string {
	values := make([]string, 0, 3)
	if name := item.Article.SupplierArticleName; name != nil && *name != "" {
		values = append(values, *name)
	}
	if number := item.Article.SupplierArticleNumber; number != nil && *number != "" {
		values = append(values, *number)
	}
	if variant := item.VariantName; variant != nil && *variant != "" {
		values = append(values, *variant)
	}
	return strings.Join(values, " | ")
}

func (service *PDFService) drawCenteredImage(document *gopdf.GoPdf, data OrderPdfData, item OrderItemPdfData, pageWidth, pageHeight float64) error {
	margin := service.marginForItem(item)
	imageWidth := (pageWidth - 2*margin)
	imageHeight := (pageHeight - 2*margin - (15 * MMToPoints))
	if mugDetails := item.Article.MugDetails; mugDetails != nil {
		if mugDetails.PrintTemplateWidthMM > 0 {
			imageWidth = float64(mugDetails.PrintTemplateWidthMM) * MMToPoints
		}
		if mugDetails.PrintTemplateHeightMM > 0 {
			imageHeight = float64(mugDetails.PrintTemplateHeightMM) * MMToPoints
		}
	}

	// Load image bytes
	var imageBytes []byte
	if len(item.GeneratedImageBytes) > 0 {
		imageBytes = item.GeneratedImageBytes
	} else if item.GeneratedImageFilename != nil && *item.GeneratedImageFilename != "" {
		if service.loader == nil {
			return errors.New("image loader not configured")
		}
		loadedBytes, _, err := service.loader(data.UserID, *item.GeneratedImageFilename)
		if err == nil && len(loadedBytes) > 0 {
			imageBytes = loadedBytes
		}
	}
	if len(imageBytes) == 0 {
		imageBytes = DefaultPlaceholderPNG()
	}

	// Normalize PNGs to 8-bit depth to avoid decoder limitations
	imageBytes = normalizePNGTo8Bit(imageBytes)

	imageHolder, err := gopdf.ImageHolderByBytes(imageBytes)
	if err != nil {
		// fallback to placeholder
		imageHolder, err = gopdf.ImageHolderByBytes(DefaultPlaceholderPNG())
		if err != nil {
			return err
		}
	}

	xPosition := (pageWidth - imageWidth) / 2
	yPosition := (pageHeight - imageHeight) / 2
	return document.ImageByHolder(imageHolder, xPosition, yPosition, &gopdf.Rect{W: imageWidth, H: imageHeight})
}

// normalizePNGTo8Bit attempts to decode PNG bytes and re-encode as 8-bit RGBA PNG.
// If data is not PNG or decoding fails, returns the original bytes unchanged.
func normalizePNGTo8Bit(data []byte) []byte {
	// Fast path: try to decode as PNG
	img, err := png.Decode(bytes.NewReader(data))
	if err != nil {
		return data
	}
	// Convert to 8-bit RGBA
	b := img.Bounds()
	rgba := image.NewRGBA(b)
	draw.Draw(rgba, b, img, b.Min, draw.Src)
	var buf bytes.Buffer
	if err := png.Encode(&buf, rgba); err != nil {
		return data
	}
	return buf.Bytes()
}

func (service *PDFService) drawQRCode(document *gopdf.GoPdf, payload string, xPosition, yPosition, sizePoints float64) error {
	pngBytes, err := generateQRPNG(payload, service.config.QRCode.SizePixels)
	if err != nil || len(pngBytes) == 0 {
		// produce a 1x1 transparent PNG as fallback
		var buf bytes.Buffer
		_ = png.Encode(&buf, image1x1Transparent())
		pngBytes = buf.Bytes()
	}
	// Ensure QR PNG is 8-bit RGBA for decoder compatibility
	pngBytes = normalizePNGTo8Bit(pngBytes)
	imageHolder, err := gopdf.ImageHolderByBytes(pngBytes)
	if err != nil {
		return err
	}
	return document.ImageByHolder(imageHolder, xPosition, yPosition, &gopdf.Rect{W: sizePoints, H: sizePoints})
}
