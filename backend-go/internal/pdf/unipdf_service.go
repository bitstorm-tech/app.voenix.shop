package pdf

import (
	"bytes"
	"errors"
	"fmt"
	"image/png"
	"math"
	"strings"

	"github.com/unidoc/unipdf/v4/creator"
	"github.com/unidoc/unipdf/v4/model"
)

// UniPDFService implements Service using unidoc/unipdf Creator API.
type UniPDFService struct {
	cfg    Config
	loader ImageLoaderFunc
}

// NewService constructs a new UniPDFService with options.
func NewService(opt Options) *UniPDFService {
	cfg := opt.Config
	if cfg.Size.WidthMM == 0 || cfg.Size.HeightMM == 0 {
		cfg = DefaultConfig()
	}
	svc := &UniPDFService{cfg: cfg}
	if opt.ImageLoader != nil {
		svc.loader = opt.ImageLoader
	} else {
		svc.loader = defaultImageLoader
	}
	return svc
}

func (s *UniPDFService) GenerateOrderPDF(data OrderPdfData) ([]byte, error) {
	total := data.TotalItemCount()
	if total == 0 {
		// still return a single-page placeholder PDF
		total = 1
	}

	c := creator.New()

	// initial page size from first item or defaults
	wPt, hPt := s.pageSizeForFirst(data)
	c.SetPageSize(creator.PageSize{wPt, hPt})
	// Use small, explicit margins rather than default (10% of width)
	c.SetPageMargins(10, 10, 10, 10)

	pageNum := 1
	for i := range data.Items {
		item := data.Items[i]
		qty := item.Quantity
		if qty <= 0 {
			continue
		}
		for j := 0; j < qty; j++ {
			if pageNum > 1 {
				// Per-item page size
				iwPt, ihPt := s.pageSizeForItem(item)
				c.SetPageSize(creator.PageSize{iwPt, ihPt})
				_ = c.NewPage()
			}
			if err := s.drawPage(c, data, item, pageNum, total); err != nil {
				return nil, err
			}
			pageNum++
		}
	}

	// handle case with no items
	if pageNum == 1 {
		_ = c.NewPage()
		_ = s.drawPage(c, data, OrderItemPdfData{Quantity: 1, Article: ArticlePdfData{}}, 1, 1)
	}

	var out bytes.Buffer
	if err := c.Write(&out); err != nil {
		return nil, err
	}
	return out.Bytes(), nil
}

// drawPage draws a single page following the Kotlin layout (simplified):
// - Left vertical header: orderNumber (page/total)
// - Right vertical product info
// - Centered product image (placeholder if missing)
// - Bottom-left QR code (as small image)
func (s *UniPDFService) drawPage(c *creator.Creator, data OrderPdfData, item OrderItemPdfData, page, total int) error {
	ctx := c.Context()
	w := ctx.PageWidth
	h := ctx.PageHeight

	// Header text: order number and page info (vertical along left edge)
	hdr := c.NewParagraph(s.headerText(data, page, total))
	hdr.SetFont(model.NewStandard14FontMustCompile(model.HelveticaBoldName))
	hdr.SetFontSize(s.cfg.Fonts.HeaderSizePt)
	hdr.SetAngle(90)
	hdr.SetPos(15, h/2)
	if err := c.Draw(hdr); err != nil {
		return err
	}

	// Right product info (supplier article and variant)
	info := s.productInfoLine(item)
	if info != "" {
		pi := c.NewParagraph(info)
		pi.SetFont(model.NewStandard14FontMustCompile(model.HelveticaName))
		pi.SetFontSize(s.cfg.Fonts.HeaderSizePt)
		pi.SetAngle(90)
		pi.SetPos(w-5, h/2)
		if err := c.Draw(pi); err != nil {
			return err
		}
	}

	// Center product image
	if err := s.drawCenteredImage(c, data, item, w, h); err != nil {
		return err
	}

	// Bottom-left QR code with order ID
	if data.ID != "" {
		if err := s.drawQRCode(c, data.ID, 0, 0, s.cfg.QRCode.SizePt); err != nil {
			// Don’t fail PDF on QR errors; continue
			_ = err
		}
	}
	return nil
}

func (s *UniPDFService) pageSizeForFirst(data OrderPdfData) (float64, float64) {
	if len(data.Items) == 0 {
		return s.cfg.Size.WidthMM * MMToPoints, s.cfg.Size.HeightMM * MMToPoints
	}
	return s.pageSizeForItem(data.Items[0])
}

func (s *UniPDFService) pageSizeForItem(item OrderItemPdfData) (float64, float64) {
	if md := item.Article.MugDetails; md != nil {
		if md.DocumentFormatWidthMM != nil && md.DocumentFormatHeightMM != nil {
			return float64(*md.DocumentFormatWidthMM) * MMToPoints, float64(*md.DocumentFormatHeightMM) * MMToPoints
		}
	}
	return s.cfg.Size.WidthMM * MMToPoints, s.cfg.Size.HeightMM * MMToPoints
}

func (s *UniPDFService) marginForItem(item OrderItemPdfData) float64 {
	if md := item.Article.MugDetails; md != nil {
		if md.DocumentFormatMarginBottomMM != nil {
			return float64(*md.DocumentFormatMarginBottomMM) * MMToPoints
		}
	}
	return s.cfg.MarginMM * MMToPoints
}

func (s *UniPDFService) headerText(data OrderPdfData, page, total int) string {
	ord := "UNKNOWN"
	if data.OrderNumber != nil && *data.OrderNumber != "" {
		ord = *data.OrderNumber
	}
	return fmt.Sprintf("%s (%d/%d)", ord, page, total)
}

func (s *UniPDFService) productInfoLine(item OrderItemPdfData) string {
	vals := make([]string, 0, 3)
	if n := item.Article.SupplierArticleName; n != nil && *n != "" {
		vals = append(vals, *n)
	}
	if n := item.Article.SupplierArticleNumber; n != nil && *n != "" {
		vals = append(vals, *n)
	}
	if v := item.VariantName; v != nil && *v != "" {
		vals = append(vals, *v)
	}
	return strings.Join(vals, " | ")
}

func (s *UniPDFService) drawCenteredImage(c *creator.Creator, data OrderPdfData, item OrderItemPdfData, pageW, pageH float64) error {
	// Decide final image width/height (in points)
	margin := s.marginForItem(item)
	imgW := (pageW - 2*margin)
	imgH := (pageH - 2*margin - (15 * MMToPoints)) // leave a bit of extra breathing room vertically
	if md := item.Article.MugDetails; md != nil {
		if md.PrintTemplateWidthMM > 0 {
			imgW = float64(md.PrintTemplateWidthMM) * MMToPoints
		}
		if md.PrintTemplateHeightMM > 0 {
			imgH = float64(md.PrintTemplateHeightMM) * MMToPoints
		}
	}

	// Load bytes: priority to inline bytes, then via loader if filename provided
	var b []byte
	if len(item.GeneratedImageBytes) > 0 {
		b = item.GeneratedImageBytes
	} else if item.GeneratedImageFilename != nil && *item.GeneratedImageFilename != "" {
		if s.loader == nil {
			return errors.New("image loader not configured")
		}
		bb, _, err := s.loader(data.UserID, *item.GeneratedImageFilename)
		if err == nil && len(bb) > 0 {
			b = bb
		}
	}
	if len(b) == 0 {
		b = DefaultPlaceholderPNG()
	}

	img, err := c.NewImageFromData(b)
	if err != nil {
		// fallback: encode placeholder to be safe
		p := DefaultPlaceholderPNG()
		img, err = c.NewImageFromData(p)
		if err != nil {
			return err
		}
	}

	// Scale to fit box while preserving aspect
	iw := img.Width()
	ih := img.Height()
	if iw == 0 || ih == 0 {
		// ensure valid non-zero dims
		iw, ih = 1, 1
	}
	scale := math.Min(imgW/float64(iw), imgH/float64(ih))
	img.Scale(scale, scale)

	// Center positioning
	finalW := float64(iw) * scale
	finalH := float64(ih) * scale
	x := (pageW - finalW) / 2
	y := (pageH - finalH) / 2
	img.SetPos(x, y)
	return c.Draw(img)
}

// drawQRCode draws a small QR code PNG containing the payload at x,y with a square size in points.
func (s *UniPDFService) drawQRCode(c *creator.Creator, payload string, x, y, sizePt float64) error {
	// Use stdlib-free approach: leverage an external QR dependency if available.
	// For environments without network during build, keep a small inline PNG (1x1) if generation fails.
	pngBytes, err := generateQRPNG(payload, s.cfg.QRCode.SizePixels)
	if err != nil || len(pngBytes) == 0 {
		// 1x1 transparent PNG fallback
		empty := &png.Encoder{}
		var buf bytes.Buffer
		_ = empty.Encode(&buf, image1x1Transparent())
		pngBytes = buf.Bytes()
	}
	img, err := c.NewImageFromData(pngBytes)
	if err != nil {
		return err
	}
	// Scale to target size
	iw := img.Width()
	if iw <= 0 {
		iw = sizePt
	}
	scale := sizePt / iw
	img.Scale(scale, scale)
	img.SetPos(x, y)
	return c.Draw(img)
}
