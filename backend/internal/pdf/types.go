package pdf

import (
	"time"
)

// MMToPoints is the conversion factor from millimeters to PDF points.
const MMToPoints = 2.8346457

type Config struct {
	Size struct {
		WidthMM  float64
		HeightMM float64
	}
	MarginMM float64
	Fonts    struct {
		HeaderSizePt      float64
		PlaceholderSizePt float64
	}
	QRCode struct {
		SizePixels int
		SizePt     float64
	}
}

func DefaultConfig() Config {
	var c Config
	c.Size.WidthMM = 239
	c.Size.HeightMM = 99
	c.MarginMM = 1
	c.Fonts.HeaderSizePt = 14
	c.Fonts.PlaceholderSizePt = 12
	c.QRCode.SizePixels = 100
	c.QRCode.SizePt = 40
	return c
}

type OrderPdfData struct {
	ID          string
	OrderNumber *string
	UserID      int
	Items       []OrderItemPdfData
}

func (o OrderPdfData) TotalItemCount() int {
	total := 0
	for i := range o.Items {
		if o.Items[i].Quantity > 0 {
			total += o.Items[i].Quantity
		}
	}
	return total
}

type OrderItemPdfData struct {
	ID                     string
	Quantity               int
	GeneratedImageFilename *string
	GeneratedImageBytes    []byte // optional: if provided, takes precedence over filename
	Article                ArticlePdfData
	VariantID              int
	VariantName            *string
}

type ArticlePdfData struct {
	ID                    int
	MugDetails            *MugDetailsPdfData
	SupplierArticleName   *string
	SupplierArticleNumber *string
}

type MugDetailsPdfData struct {
	PrintTemplateWidthMM         int
	PrintTemplateHeightMM        int
	DocumentFormatWidthMM        *int
	DocumentFormatHeightMM       *int
	DocumentFormatMarginBottomMM *int
}

func FilenameFromOrderNumber(orderNumber string) string {
	if orderNumber == "" {
		orderNumber = "ORDER"
	}
	// Use a compact timestamp for cross-language parity
	ts := time.Now().Format("20060102_150405")
	return "order_" + orderNumber + "_" + ts + ".pdf"
}
