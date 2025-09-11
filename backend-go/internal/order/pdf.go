package order

import (
    "bytes"
    "fmt"
    "time"
    "strings"
)

// A minimal PDF generator without external deps.
// Produces a single-page PDF with basic text summary of the order.

func pdfFilename(orderNumber string) string {
    if orderNumber == "" { orderNumber = "ORDER" }
    return fmt.Sprintf("%s.pdf", orderNumber)
}

func generateOrderPDFPlaceholder(o *Order) []byte {
    // Very small PDF: write text content, not scaled for printers.
    content := fmt.Sprintf("Order %s\nTotal: %0.2f\nItems: %d\nDate: %s\n",
        o.OrderNumber,
        float64(o.TotalAmount)/100.0,
        len(o.Items),
        o.CreatedAt.Format(time.RFC3339),
    )
    // Basic PDF objects
    // This is a simplistic, valid one-page PDF with text using built-in font.
    var buf bytes.Buffer
    // Header
    buf.WriteString("%PDF-1.4\n")
    // 1: Catalog
    off1 := buf.Len()
    buf.WriteString("1 0 obj<< /Type /Catalog /Pages 2 0 R>>endobj\n")
    // 2: Pages
    off2 := buf.Len()
    buf.WriteString("2 0 obj<< /Type /Pages /Kids [3 0 R] /Count 1>>endobj\n")
    // 3: Page
    off3 := buf.Len()
    buf.WriteString("3 0 obj<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Contents 4 0 R /Resources<< /Font<< /F1 5 0 R>> >> >>endobj\n")
    // 4: Contents
    stream := contentStream(content)
    off4 := buf.Len()
    buf.WriteString(fmt.Sprintf("4 0 obj<< /Length %d >>stream\n", len(stream)))
    buf.Write(stream)
    buf.WriteString("\nendstream\nendobj\n")
    // 5: Font
    off5 := buf.Len()
    buf.WriteString("5 0 obj<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica>>endobj\n")
    // xref
    xref := buf.Len()
    buf.WriteString("xref\n0 6\n0000000000 65535 f \n")
    buf.WriteString(fmt.Sprintf("%010d 00000 n \n", off1))
    buf.WriteString(fmt.Sprintf("%010d 00000 n \n", off2))
    buf.WriteString(fmt.Sprintf("%010d 00000 n \n", off3))
    buf.WriteString(fmt.Sprintf("%010d 00000 n \n", off4))
    buf.WriteString(fmt.Sprintf("%010d 00000 n \n", off5))
    // trailer
    buf.WriteString("trailer<< /Size 6 /Root 1 0 R>>\nstartxref\n")
    buf.WriteString(fmt.Sprintf("%d\n%%EOF\n", xref))
    return buf.Bytes()
}

func contentStream(text string) []byte {
    // Simple text operators: BT/ET, position, font, show text lines.
    var b bytes.Buffer
    b.WriteString("BT\n/F1 14 Tf\n72 720 Td\n")
    for i, ln := range splitLines(text) {
        if i > 0 {
            b.WriteString("T* ") // move to next line (leading set by TL)
        }
        // Set leading (line height) once
        if i == 0 {
            b.WriteString("14 TL\n")
        }
        b.WriteString(fmt.Sprintf("(%s) Tj\n", escapePDFString(ln)))
    }
    b.WriteString("ET")
    return b.Bytes()
}

func splitLines(s string) []string {
    out := []string{}
    cur := ""
    for _, r := range s {
        if r == '\n' {
            out = append(out, cur)
            cur = ""
            continue
        }
        cur += string(r)
    }
    if cur != "" { out = append(out, cur) }
    return out
}

func escapePDFString(s string) string {
    // Escape parentheses and backslashes
    s = strings.ReplaceAll(s, "\\", "\\\\")
    s = strings.ReplaceAll(s, "(", "\\(")
    s = strings.ReplaceAll(s, ")", "\\)")
    return s
}
