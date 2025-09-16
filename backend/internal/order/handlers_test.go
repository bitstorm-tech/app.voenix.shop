package order

import (
	"encoding/json"
	"errors"
	"net/http"
	"net/http/httptest"
	"strings"
	"testing"

	"github.com/gin-gonic/gin"
	"gorm.io/gorm"

	"voenix/backend/internal/article"
	"voenix/backend/internal/auth"
	"voenix/backend/internal/utility"
)

type ftpCall struct {
	server string
	user   string
	pass   string
	path   string
}

func seedOrderForPDF(t *testing.T, db *gorm.DB, userID int) *Order {
	t.Helper()

	art := article.Article{ID: 101, Name: "Test Article", ArticleType: article.ArticleTypeMug, Active: true}
	if err := db.Create(&art).Error; err != nil {
		t.Fatalf("seed article: %v", err)
	}
	variant := article.MugVariant{ID: 201, ArticleID: art.ID, Name: "Default", Active: true}
	if err := db.Create(&variant).Error; err != nil {
		t.Fatalf("seed variant: %v", err)
	}
	orderID := newUUIDv4()
	order := Order{
		ID:              orderID,
		UserID:          userID,
		CustomerEmail:   "user@example.com",
		CustomerFirst:   "Test",
		CustomerLast:    "User",
		ShippingStreet1: "123 Main",
		ShippingCity:    "City",
		ShippingState:   "ST",
		ShippingPostal:  "00000",
		ShippingCountry: "USA",
		Subtotal:        1000,
		TaxAmount:       80,
		ShippingAmount:  200,
		TotalAmount:     1280,
		Status:          StatusPending,
		CartID:          1,
	}
	if err := db.Create(&order).Error; err != nil {
		t.Fatalf("create order: %v", err)
	}
	if err := db.Model(&order).Update("order_number", "ORD-1001").Error; err != nil {
		t.Fatalf("set order number: %v", err)
	}
	item := OrderItem{
		ID:         newUUIDv4(),
		OrderID:    order.ID,
		ArticleID:  art.ID,
		VariantID:  variant.ID,
		Quantity:   1,
		CustomData: "{}",
	}
	if err := db.Create(&item).Error; err != nil {
		t.Fatalf("create order item: %v", err)
	}
	if err := db.Preload("Items").First(&order, "id = ?", order.ID).Error; err != nil {
		t.Fatalf("reload order: %v", err)
	}
	return &order
}

func TestDownloadOrderPDFHandler_UploadsBeforeResponding(t *testing.T) {
	gin.SetMode(gin.TestMode)
	db := setupTestDB(t)
	userID := 500
	ord := seedOrderForPDF(t, db, userID)

	var calls []ftpCall
	uploadPDFToFTPSave := uploadPDFToFTP
	uploadPDFToFTP = func(pdf []byte, server, user, password string, opts *utility.FTPUploadOptions) error {
		calls = append(calls, ftpCall{server: server, user: user, pass: password, path: opts.RemotePath})
		if len(pdf) == 0 {
			t.Fatalf("pdf bytes should not be empty")
		}
		if opts == nil {
			t.Fatalf("expected options")
		}
		return nil
	}
	t.Cleanup(func() { uploadPDFToFTP = uploadPDFToFTPSave })

	t.Setenv("ORDER_PDF_FTP_SERVER", "ftp.example.com")
	t.Setenv("ORDER_PDF_FTP_FOLDER", "orders")
	t.Setenv("ORDER_PDF_FTP_USER", "ftp-user")
	t.Setenv("ORDER_PDF_FTP_PASSWORD", "ftp-pass")
	t.Setenv("ORDER_PDF_FTP_TIMEOUT", "15")

	req := httptest.NewRequest(http.MethodGet, "/api/user/orders/"+ord.ID+"/pdf", nil)
	w := httptest.NewRecorder()
	ctx, _ := gin.CreateTestContext(w)
	ctx.Request = req
	ctx.Set("currentUser", &auth.User{ID: userID})
	ctx.Params = gin.Params{gin.Param{Key: "orderId", Value: ord.ID}}

	handler := downloadOrderPDFHandler(db)
	handler(ctx)

	if w.Code != http.StatusOK {
		t.Fatalf("expected 200, got %d", w.Code)
	}
	if len(calls) != 1 {
		t.Fatalf("expected upload to be called once, got %d", len(calls))
	}
	call := calls[0]
	if call.server != "ftp.example.com" {
		t.Fatalf("server = %s", call.server)
	}
	if call.user != "ftp-user" || call.pass != "ftp-pass" {
		t.Fatalf("ftp credentials not propagated")
	}
	if !strings.HasPrefix(call.path, "orders/") {
		t.Fatalf("expected remote path prefixed with orders/, got %s", call.path)
	}
	if disp := w.Header().Get("Content-Disposition"); disp == "" {
		t.Fatalf("expected attachment header")
	}
	if ct := w.Header().Get("Content-Type"); ct != "application/pdf" {
		t.Fatalf("expected application/pdf, got %s", ct)
	}
}

func TestDownloadOrderPDFHandler_MissingConfig(t *testing.T) {
	gin.SetMode(gin.TestMode)
	db := setupTestDB(t)
	userID := 42
	ord := seedOrderForPDF(t, db, userID)

	uploadPDFToFTPSave := uploadPDFToFTP
	uploadPDFToFTP = func(pdf []byte, server, user, password string, opts *utility.FTPUploadOptions) error {
		t.Fatalf("upload should not be called when config missing")
		return nil
	}
	t.Cleanup(func() { uploadPDFToFTP = uploadPDFToFTPSave })

	t.Setenv("ORDER_PDF_FTP_SERVER", "")
	t.Setenv("ORDER_PDF_FTP_USER", "")
	t.Setenv("ORDER_PDF_FTP_PASSWORD", "")

	w := httptest.NewRecorder()
	ctx, _ := gin.CreateTestContext(w)
	ctx.Request = httptest.NewRequest(http.MethodGet, "/api/user/orders/"+ord.ID+"/pdf", nil)
	ctx.Params = gin.Params{gin.Param{Key: "orderId", Value: ord.ID}}
	ctx.Set("currentUser", &auth.User{ID: userID})

	handler := downloadOrderPDFHandler(db)
	handler(ctx)

	if w.Code != http.StatusInternalServerError {
		t.Fatalf("expected 500, got %d", w.Code)
	}
	var body map[string]string
	if err := json.Unmarshal(w.Body.Bytes(), &body); err != nil {
		t.Fatalf("unmarshal: %v", err)
	}
	if body["detail"] != "FTP configuration missing" {
		t.Fatalf("unexpected detail: %s", body["detail"])
	}
}

func TestDownloadOrderPDFHandler_UploadFailure(t *testing.T) {
	gin.SetMode(gin.TestMode)
	db := setupTestDB(t)
	userID := 12
	ord := seedOrderForPDF(t, db, userID)

	uploadPDFToFTPSave := uploadPDFToFTP
	uploadPDFToFTP = func(pdf []byte, server, user, password string, opts *utility.FTPUploadOptions) error {
		return errors.New("upload failed")
	}
	t.Cleanup(func() { uploadPDFToFTP = uploadPDFToFTPSave })

	t.Setenv("ORDER_PDF_FTP_SERVER", "ftp.example.com")
	t.Setenv("ORDER_PDF_FTP_USER", "ftp-user")
	t.Setenv("ORDER_PDF_FTP_PASSWORD", "ftp-pass")

	w := httptest.NewRecorder()
	ctx, _ := gin.CreateTestContext(w)
	ctx.Request = httptest.NewRequest(http.MethodGet, "/api/user/orders/"+ord.ID+"/pdf", nil)
	ctx.Params = gin.Params{gin.Param{Key: "orderId", Value: ord.ID}}
	ctx.Set("currentUser", &auth.User{ID: userID})

	handler := downloadOrderPDFHandler(db)
	handler(ctx)

	if w.Code != http.StatusBadGateway {
		t.Fatalf("expected 502, got %d", w.Code)
	}
	var body map[string]string
	if err := json.Unmarshal(w.Body.Bytes(), &body); err != nil {
		t.Fatalf("unmarshal: %v", err)
	}
	if body["detail"] != "Failed to upload order PDF" {
		t.Fatalf("unexpected detail: %s", body["detail"])
	}
}
