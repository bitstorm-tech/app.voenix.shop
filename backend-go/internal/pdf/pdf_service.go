package pdf

import (
    "bytes"
    "errors"
    "fmt"
    "image/png"
    "math"
    "strings"

    "github.com/signintech/gopdf"
    gobold "golang.org/x/image/font/gofont/gobold"
    goregular "golang.org/x/image/font/gofont/goregular"
)

// PDFService implements Service using a PDF backend.
type PDFService struct {
    cfg    Config
    loader ImageLoaderFunc
}

// NewService constructs a new service with options.
func NewService(opt Options) *PDFService {
    cfg := opt.Config
    if cfg.Size.WidthMM == 0 || cfg.Size.HeightMM == 0 {
        cfg = DefaultConfig()
    }
    svc := &PDFService{cfg: cfg}
    if opt.ImageLoader != nil {
        svc.loader = opt.ImageLoader
    } else {
        svc.loader = defaultImageLoader
    }
    return svc
}

func (s *PDFService) GenerateOrderPDF(data OrderPdfData) ([]byte, error) {
    total := data.TotalItemCount()
    if total == 0 {
        total = 1
    }

    // Prepare PDF
    wPt, hPt := s.pageSizeForFirst(data)
    var out bytes.Buffer
    pdf := gopdf.GoPdf{}
    pdf.Start(gopdf.Config{PageSize: gopdf.Rect{W: wPt, H: hPt}})
    pdf.AddPage()

    // Register embedded Go fonts to avoid external font files.
    _ = pdf.AddTTFFontData("Go-Regular", goregular.TTF)
    _ = pdf.AddTTFFontData("Go-Bold", gobold.TTF)
    _ = pdf.SetFont("Go-Regular", "", s.cfg.Fonts.HeaderSizePt)

    pageNum := 1
    for i := range data.Items {
        item := data.Items[i]
        qty := item.Quantity
        if qty <= 0 {
            continue
        }
        for j := 0; j < qty; j++ {
            if pageNum > 1 {
                iwPt, ihPt := s.pageSizeForItem(item)
                pdf.AddPageWithOption(gopdf.PageOption{PageSize: &gopdf.Rect{W: iwPt, H: ihPt}})
                _ = pdf.SetFont("Go-Regular", "", s.cfg.Fonts.HeaderSizePt)
            } else {
                // Resize first page if different
                iwPt, ihPt := s.pageSizeForItem(item)
                if math.Abs(iwPt-wPt) > 0.1 || math.Abs(ihPt-hPt) > 0.1 {
                    pdf.AddPageWithOption(gopdf.PageOption{PageSize: &gopdf.Rect{W: iwPt, H: ihPt}})
                    _ = pdf.SetFont("Go-Regular", "", s.cfg.Fonts.HeaderSizePt)
                }
            }
            if err := s.drawPage(&pdf, data, item, pageNum, total); err != nil {
                return nil, err
            }
            pageNum++
        }
    }

    if pageNum == 1 {
        _ = s.drawPage(&pdf, data, OrderItemPdfData{Quantity: 1, Article: ArticlePdfData{}}, 1, 1)
    }

    if err := pdf.Write(&out); err != nil {
        return nil, err
    }
    return out.Bytes(), nil
}

// drawPage renders a single page following the intended layout.
func (s *PDFService) drawPage(pdf *gopdf.GoPdf, data OrderPdfData, item OrderItemPdfData, page, total int) error {
    pageW, pageH := s.pageSizeForItem(item)

    // Left vertical header (order number + page/total)
    hdr := s.headerText(data, page, total)
    _ = pdf.SetFont("Go-Bold", "", s.cfg.Fonts.HeaderSizePt)
    pdf.Rotate(90, 15, pageH/2)
    pdf.SetX(15)
    pdf.SetY(pageH / 2)
    pdf.Cell(nil, hdr)
    pdf.RotateReset()

    // Right vertical product info
    if info := s.productInfoLine(item); info != "" {
        _ = pdf.SetFont("Go-Regular", "", s.cfg.Fonts.HeaderSizePt)
        pdf.Rotate(90, pageW-5, pageH/2)
        pdf.SetX(pageW - 5)
        pdf.SetY(pageH / 2)
        pdf.Cell(nil, info)
        pdf.RotateReset()
    }

    // Centered product image at intended print size
    if err := s.drawCenteredImage(pdf, data, item, pageW, pageH); err != nil {
        return err
    }

    // QR bottom-left (within margin)
    if data.ID != "" {
        // place QR within page margin at bottom-left
        margin := s.marginForItem(item)
        qrSize := s.cfg.QRCode.SizePt
        x := margin
        y := pageH - margin - qrSize
        if err := s.drawQRCode(pdf, data.ID, x, y, qrSize); err != nil {
            // keep going even if QR fails
            _ = err
        }
    }
    return nil
}

func (s *PDFService) pageSizeForFirst(data OrderPdfData) (float64, float64) {
    if len(data.Items) == 0 {
        return s.cfg.Size.WidthMM * MMToPoints, s.cfg.Size.HeightMM * MMToPoints
    }
    return s.pageSizeForItem(data.Items[0])
}

func (s *PDFService) pageSizeForItem(item OrderItemPdfData) (float64, float64) {
    if md := item.Article.MugDetails; md != nil {
        if md.DocumentFormatWidthMM != nil && md.DocumentFormatHeightMM != nil {
            return float64(*md.DocumentFormatWidthMM) * MMToPoints, float64(*md.DocumentFormatHeightMM) * MMToPoints
        }
    }
    return s.cfg.Size.WidthMM * MMToPoints, s.cfg.Size.HeightMM * MMToPoints
}

func (s *PDFService) marginForItem(item OrderItemPdfData) float64 {
    if md := item.Article.MugDetails; md != nil {
        if md.DocumentFormatMarginBottomMM != nil {
            return float64(*md.DocumentFormatMarginBottomMM) * MMToPoints
        }
    }
    return s.cfg.MarginMM * MMToPoints
}

func (s *PDFService) headerText(data OrderPdfData, page, total int) string {
    ord := "UNKNOWN"
    if data.OrderNumber != nil && *data.OrderNumber != "" {
        ord = *data.OrderNumber
    }
    return fmt.Sprintf("%s (%d/%d)", ord, page, total)
}

func (s *PDFService) productInfoLine(item OrderItemPdfData) string {
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

func (s *PDFService) drawCenteredImage(pdf *gopdf.GoPdf, data OrderPdfData, item OrderItemPdfData, pageW, pageH float64) error {
    margin := s.marginForItem(item)
    imgW := (pageW - 2*margin)
    imgH := (pageH - 2*margin - (15 * MMToPoints))
    if md := item.Article.MugDetails; md != nil {
        if md.PrintTemplateWidthMM > 0 {
            imgW = float64(md.PrintTemplateWidthMM) * MMToPoints
        }
        if md.PrintTemplateHeightMM > 0 {
            imgH = float64(md.PrintTemplateHeightMM) * MMToPoints
        }
    }

    // Load image bytes
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

    holder, err := gopdf.ImageHolderByBytes(b)
    if err != nil {
        // fallback to placeholder
        holder, err = gopdf.ImageHolderByBytes(DefaultPlaceholderPNG())
        if err != nil {
            return err
        }
    }

    x := (pageW - imgW) / 2
    y := (pageH - imgH) / 2
    return pdf.ImageByHolder(holder, x, y, &gopdf.Rect{W: imgW, H: imgH})
}

func (s *PDFService) drawQRCode(pdf *gopdf.GoPdf, payload string, x, y, sizePt float64) error {
    pngBytes, err := generateQRPNG(payload, s.cfg.QRCode.SizePixels)
    if err != nil || len(pngBytes) == 0 {
        // produce a 1x1 transparent PNG as fallback
        var buf bytes.Buffer
        _ = png.Encode(&buf, image1x1Transparent())
        pngBytes = buf.Bytes()
    }
    holder, err := gopdf.ImageHolderByBytes(pngBytes)
    if err != nil {
        return err
    }
    return pdf.ImageByHolder(holder, x, y, &gopdf.Rect{W: sizePt, H: sizePt})
}
