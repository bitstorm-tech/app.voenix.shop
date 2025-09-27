package order

import (
	"context"
	"encoding/json"
	"errors"
	"net/http"
	"net/http/httptest"
	"strconv"
	"strings"
	"sync"
	"testing"
	"time"

	"github.com/gin-gonic/gin"

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

func seedOrderForPDF(t *testing.T, repo *fakeRepository, articleSvc *fakeArticleService, userID int) *Order {
	t.Helper()

	articleID := 101
	variantID := 201
	generatedID := 301

	now := time.Now()
	orderID := int64(1001)
	orderItemID := int64(5001)
	order := Order{
		ID:              orderID,
		OrderNumber:     "ORD-1001",
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
		CreatedAt:       now,
		UpdatedAt:       now,
		Items: []OrderItem{
			{
				ID:               orderItemID,
				OrderID:          orderID,
				ArticleID:        articleID,
				VariantID:        variantID,
				Quantity:         1,
				PricePerItem:     1000,
				TotalPrice:       1000,
				GeneratedImageID: &generatedID,
				CustomData:       "{}",
				CreatedAt:        now,
			},
		},
	}
	repo.addOrder(order)
	repo.setGenerated(generatedID, "orders/generated.png")

	supplierName := "Supplier"
	supplierNumber := "SUP-001"
	articleSvc.articles[articleID] = article.Article{
		ID:                    articleID,
		Name:                  "Test Article",
		SupplierArticleName:   &supplierName,
		SupplierArticleNumber: &supplierNumber,
		ArticleType:           article.ArticleTypeMug,
	}
	articleSvc.summaries[articleID] = article.ArticleResponse{ID: articleID, Name: "Test Article"}
	articleSvc.variants[variantID] = article.MugVariant{ID: variantID, ArticleID: articleID, Name: "Default"}
	articleSvc.details[articleID] = article.MugDetails{PrintTemplateWidthMm: 100, PrintTemplateHeightMm: 50}

	ord, err := repo.OrderByIDForUser(context.Background(), userID, orderID)
	if err != nil {
		t.Fatalf("reload order: %v", err)
	}
	return ord
}

func TestDownloadOrderPDFHandler_DoesNotUpload(t *testing.T) {
	gin.SetMode(gin.TestMode)
	repo := newFakeRepository()
	articleSvc := newFakeArticleService()
	svc := NewService(repo, articleSvc)
	userID := 500
	ord := seedOrderForPDF(t, repo, articleSvc, userID)

	var calls []ftpCall
	uploadPDFToFTPSave := uploadPDFToFTP
	uploadPDFToFTP = func(pdf []byte, server, user, password string, opts *utility.FTPUploadOptions) error {
		calls = append(calls, ftpCall{server: server, user: user, pass: password, path: "unexpected"})
		return nil
	}
	t.Cleanup(func() { uploadPDFToFTP = uploadPDFToFTPSave })

	t.Setenv("ORDER_PDF_FTP_CONFIGS", "primary")
	t.Setenv("ORDER_PDF_FTP_SERVER_PRIMARY", "ftp.example.com")
	t.Setenv("ORDER_PDF_FTP_FOLDER_PRIMARY", "orders")
	t.Setenv("ORDER_PDF_FTP_USER_PRIMARY", "ftp-user")
	t.Setenv("ORDER_PDF_FTP_PASSWORD_PRIMARY", "ftp-pass")
	t.Setenv("ORDER_PDF_FTP_TIMEOUT_PRIMARY", "15")

	orderIDStr := strconv.FormatInt(ord.ID, 10)
	req := httptest.NewRequest(http.MethodGet, "/api/user/orders/"+orderIDStr+"/pdf", nil)
	w := httptest.NewRecorder()
	ctx, _ := gin.CreateTestContext(w)
	ctx.Request = req
	ctx.Set("currentUser", &auth.User{ID: userID})
	ctx.Params = gin.Params{gin.Param{Key: "orderId", Value: orderIDStr}}

	handler := downloadOrderPDFHandler(svc)
	handler(ctx)

	if w.Code != http.StatusOK {
		t.Fatalf("expected 200, got %d", w.Code)
	}
	if len(calls) != 0 {
		t.Fatalf("expected no FTP uploads during download, got %d", len(calls))
	}
	if disp := w.Header().Get("Content-Disposition"); disp == "" {
		t.Fatalf("expected attachment header")
	}
	if ct := w.Header().Get("Content-Type"); ct != "application/pdf" {
		t.Fatalf("expected application/pdf, got %s", ct)
	}
}

func TestSendOrderPDFToFTP_SingleConfig(t *testing.T) {
	gin.SetMode(gin.TestMode)
	repo := newFakeRepository()
	articleSvc := newFakeArticleService()
	service := NewService(repo, articleSvc)
	userID := 600
	order := seedOrderForPDF(t, repo, articleSvc, userID)

	var calls []ftpCall
	var callMutex sync.Mutex
	uploadPDFToFTPSave := uploadPDFToFTP
	uploadPDFToFTP = func(pdf []byte, server, user, password string, opts *utility.FTPUploadOptions) error {
		callMutex.Lock()
		calls = append(calls, ftpCall{server: server, user: user, pass: password, path: opts.RemotePath})
		callMutex.Unlock()
		if len(pdf) == 0 {
			t.Fatalf("pdf bytes should not be empty")
		}
		if opts == nil {
			t.Fatalf("expected options")
		}
		return nil
	}
	t.Cleanup(func() { uploadPDFToFTP = uploadPDFToFTPSave })

	t.Setenv("ORDER_PDF_FTP_CONFIGS", "primary")
	t.Setenv("ORDER_PDF_FTP_SERVER_PRIMARY", "ftp-primary.example.com")
	t.Setenv("ORDER_PDF_FTP_USER_PRIMARY", "primary-user")
	t.Setenv("ORDER_PDF_FTP_PASSWORD_PRIMARY", "primary-pass")
	t.Setenv("ORDER_PDF_FTP_FOLDER_PRIMARY", "primary/orders")

	orderIDStr := strconv.FormatInt(order.ID, 10)
	req := httptest.NewRequest(http.MethodPost, "/api/user/orders/"+orderIDStr+"/pdf/send", nil)
	resp := httptest.NewRecorder()
	ctx, _ := gin.CreateTestContext(resp)
	ctx.Request = req
	ctx.Set("currentUser", &auth.User{ID: userID})
	ctx.Params = gin.Params{gin.Param{Key: "orderId", Value: orderIDStr}}

	handler := sendOrderPDFToFTP(service)
	handler(ctx)

	if resp.Code != http.StatusOK {
		t.Fatalf("expected 200, got %d", resp.Code)
	}
	if len(calls) != 1 {
		t.Fatalf("expected upload to be called once, got %d", len(calls))
	}
	call := calls[0]
	if call.server != "ftp-primary.example.com" || call.user != "primary-user" || call.pass != "primary-pass" {
		t.Fatalf("unexpected primary call: %+v", calls[0])
	}
	if !strings.HasPrefix(call.path, "primary/orders/") {
		t.Fatalf("unexpected primary remote path: %s", call.path)
	}
}

func TestSendOrderPDFToFTP_MultipleConfigs(t *testing.T) {
	gin.SetMode(gin.TestMode)
	repo := newFakeRepository()
	articleSvc := newFakeArticleService()
	service := NewService(repo, articleSvc)
	userID := 601
	order := seedOrderForPDF(t, repo, articleSvc, userID)

	var calls []ftpCall
	var callMutex sync.Mutex
	uploadPDFToFTPSave := uploadPDFToFTP
	uploadPDFToFTP = func(pdf []byte, server, user, password string, opts *utility.FTPUploadOptions) error {
		callMutex.Lock()
		calls = append(calls, ftpCall{server: server, user: user, pass: password, path: opts.RemotePath})
		callMutex.Unlock()
		return nil
	}
	t.Cleanup(func() { uploadPDFToFTP = uploadPDFToFTPSave })

	t.Setenv("ORDER_PDF_FTP_CONFIGS", "primary, backup")
	t.Setenv("ORDER_PDF_FTP_SERVER_PRIMARY", "ftp-primary.example.com")
	t.Setenv("ORDER_PDF_FTP_USER_PRIMARY", "primary-user")
	t.Setenv("ORDER_PDF_FTP_PASSWORD_PRIMARY", "primary-pass")
	t.Setenv("ORDER_PDF_FTP_FOLDER_PRIMARY", "primary/orders")
	t.Setenv("ORDER_PDF_FTP_SERVER_BACKUP", "ftp-backup.example.com")
	t.Setenv("ORDER_PDF_FTP_USER_BACKUP", "backup-user")
	t.Setenv("ORDER_PDF_FTP_PASSWORD_BACKUP", "backup-pass")
	t.Setenv("ORDER_PDF_FTP_FOLDER_BACKUP", "backup/orders")

	orderIDStr := strconv.FormatInt(order.ID, 10)
	req := httptest.NewRequest(http.MethodPost, "/api/user/orders/"+orderIDStr+"/pdf/send", nil)
	resp := httptest.NewRecorder()
	ctx, _ := gin.CreateTestContext(resp)
	ctx.Request = req
	ctx.Set("currentUser", &auth.User{ID: userID})
	ctx.Params = gin.Params{gin.Param{Key: "orderId", Value: orderIDStr}}

	handler := sendOrderPDFToFTP(service)
	handler(ctx)

	if resp.Code != http.StatusOK {
		t.Fatalf("expected 200, got %d", resp.Code)
	}
	if len(calls) != 2 {
		t.Fatalf("expected two FTP uploads, got %d", len(calls))
	}
	callsByServer := make(map[string]ftpCall, len(calls))
	for _, call := range calls {
		callsByServer[call.server] = call
	}
	primaryCall, ok := callsByServer["ftp-primary.example.com"]
	if !ok {
		t.Fatalf("missing primary FTP upload in calls: %+v", calls)
	}
	if primaryCall.user != "primary-user" || primaryCall.pass != "primary-pass" {
		t.Fatalf("unexpected primary call credentials: %+v", primaryCall)
	}
	if !strings.HasPrefix(primaryCall.path, "primary/orders/") {
		t.Fatalf("unexpected primary remote path: %s", primaryCall.path)
	}
	backupCall, ok := callsByServer["ftp-backup.example.com"]
	if !ok {
		t.Fatalf("missing backup FTP upload in calls: %+v", calls)
	}
	if backupCall.user != "backup-user" || backupCall.pass != "backup-pass" {
		t.Fatalf("unexpected backup call credentials: %+v", backupCall)
	}
	if !strings.HasPrefix(backupCall.path, "backup/orders/") {
		t.Fatalf("unexpected backup remote path: %s", backupCall.path)
	}
}

func TestSendOrderPDFToFTP_MissingConfig(t *testing.T) {
	gin.SetMode(gin.TestMode)
	repo := newFakeRepository()
	articleSvc := newFakeArticleService()
	svc := NewService(repo, articleSvc)
	userID := 42
	ord := seedOrderForPDF(t, repo, articleSvc, userID)

	uploadPDFToFTPSave := uploadPDFToFTP
	uploadPDFToFTP = func(pdf []byte, server, user, password string, opts *utility.FTPUploadOptions) error {
		t.Fatalf("upload should not be called when config missing")
		return nil
	}
	t.Cleanup(func() { uploadPDFToFTP = uploadPDFToFTPSave })

	t.Setenv("ORDER_PDF_FTP_CONFIGS", "primary")
	t.Setenv("ORDER_PDF_FTP_SERVER_PRIMARY", "")
	t.Setenv("ORDER_PDF_FTP_USER_PRIMARY", "")
	t.Setenv("ORDER_PDF_FTP_PASSWORD_PRIMARY", "")

	w := httptest.NewRecorder()
	ctx, _ := gin.CreateTestContext(w)
	orderIDStr := strconv.FormatInt(ord.ID, 10)
	ctx.Request = httptest.NewRequest(http.MethodPost, "/api/user/orders/"+orderIDStr+"/pdf/send", nil)
	ctx.Params = gin.Params{gin.Param{Key: "orderId", Value: orderIDStr}}
	ctx.Set("currentUser", &auth.User{ID: userID})

	handler := sendOrderPDFToFTP(svc)
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

func TestSendOrderPDFToFTP_UploadFailure(t *testing.T) {
	gin.SetMode(gin.TestMode)
	repo := newFakeRepository()
	articleSvc := newFakeArticleService()
	svc := NewService(repo, articleSvc)
	userID := 12
	ord := seedOrderForPDF(t, repo, articleSvc, userID)

	uploadPDFToFTPSave := uploadPDFToFTP
	uploadPDFToFTP = func(pdf []byte, server, user, password string, opts *utility.FTPUploadOptions) error {
		return errors.New("upload failed")
	}
	t.Cleanup(func() { uploadPDFToFTP = uploadPDFToFTPSave })

	t.Setenv("ORDER_PDF_FTP_CONFIGS", "primary")
	t.Setenv("ORDER_PDF_FTP_SERVER_PRIMARY", "ftp.example.com")
	t.Setenv("ORDER_PDF_FTP_USER_PRIMARY", "ftp-user")
	t.Setenv("ORDER_PDF_FTP_PASSWORD_PRIMARY", "ftp-pass")

	w := httptest.NewRecorder()
	ctx, _ := gin.CreateTestContext(w)
	orderIDStr := strconv.FormatInt(ord.ID, 10)
	ctx.Request = httptest.NewRequest(http.MethodPost, "/api/user/orders/"+orderIDStr+"/pdf/send", nil)
	ctx.Params = gin.Params{gin.Param{Key: "orderId", Value: orderIDStr}}
	ctx.Set("currentUser", &auth.User{ID: userID})

	handler := sendOrderPDFToFTP(svc)
	handler(ctx)

	if w.Code != http.StatusBadGateway {
		t.Fatalf("expected 502, got %d", w.Code)
	}
	var body map[string]string
	if err := json.Unmarshal(w.Body.Bytes(), &body); err != nil {
		t.Fatalf("unmarshal: %v", err)
	}
	if !strings.Contains(body["detail"], "Failed to upload") {
		t.Fatalf("unexpected detail: %s", body["detail"])
	}
}
