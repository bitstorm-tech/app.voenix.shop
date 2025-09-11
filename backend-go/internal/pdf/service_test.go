package pdf

import (
    "bytes"
    "testing"
)

func TestGenerateOrderPDF_Minimal(t *testing.T) {
    svc := NewService(Options{Config: DefaultConfig()})
    ordNum := "ORD-TEST-123"
    data := OrderPdfData{
        ID:          "00000000-0000-0000-0000-000000000000",
        OrderNumber: &ordNum,
        UserID:      1,
        Items: []OrderItemPdfData{
            {
                ID:       "11111111-1111-1111-1111-111111111111",
                Quantity: 1,
                Article:  ArticlePdfData{},
            },
        },
    }
    b, err := svc.GenerateOrderPDF(data)
    if err != nil {
        t.Fatalf("GenerateOrderPDF error: %v", err)
    }
    if len(b) == 0 {
        t.Fatalf("no pdf bytes produced")
    }
    if !bytes.HasPrefix(b, []byte("%PDF")) {
        t.Fatalf("pdf does not start with %PDF header")
    }
}

