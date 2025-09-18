package ai

import (
	"encoding/json"
	"net/http"
	"net/http/httptest"
	"testing"
	"time"

	"github.com/gin-gonic/gin"
	"gorm.io/driver/sqlite"
	"gorm.io/gorm"

	"voenix/backend/internal/auth"
)

func TestAdminLLMsEndpoint(t *testing.T) {
	gin.SetMode(gin.TestMode)
	db, err := gorm.Open(sqlite.Open(":memory:"), &gorm.Config{})
	if err != nil {
		t.Fatalf("open db: %v", err)
	}
	if err := db.AutoMigrate(&auth.Role{}, &auth.User{}, &auth.Session{}); err != nil {
		t.Fatalf("auto migrate: %v", err)
	}
	role := auth.Role{Name: "ADMIN"}
	if err := db.Create(&role).Error; err != nil {
		t.Fatalf("create role: %v", err)
	}
	user := auth.User{Email: "admin@example.com"}
	if err := db.Create(&user).Error; err != nil {
		t.Fatalf("create user: %v", err)
	}
	if err := db.Model(&user).Association("Roles").Append(&role); err != nil {
		t.Fatalf("attach role: %v", err)
	}
	expiresAt := time.Now().Add(time.Hour)
	session := auth.Session{ID: "test-session", UserID: user.ID, ExpiresAt: &expiresAt}
	if err := db.Create(&session).Error; err != nil {
		t.Fatalf("create session: %v", err)
	}

	router := gin.New()
	RegisterRoutes(router, db)

	req := httptest.NewRequest(http.MethodGet, "/api/admin/ai/llms", nil)
	req.AddCookie(&http.Cookie{Name: "session_id", Value: session.ID})
	w := httptest.NewRecorder()

	router.ServeHTTP(w, req)

	if w.Code != http.StatusOK {
		t.Fatalf("expected status 200, got %d", w.Code)
	}
	var resp struct {
		LLMs []ProviderLLM `json:"llms"`
	}
	if err := json.Unmarshal(w.Body.Bytes(), &resp); err != nil {
		t.Fatalf("decode response: %v", err)
	}
	if len(resp.LLMs) != len(ProviderLLMs()) {
		t.Fatalf("expected %d llms, got %d", len(ProviderLLMs()), len(resp.LLMs))
	}
}
