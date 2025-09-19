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
	"voenix/backend/internal/auth/postgres"
)

func TestAdminLLMsEndpoint(t *testing.T) {
	gin.SetMode(gin.TestMode)
	db, err := gorm.Open(sqlite.Open(":memory:"), &gorm.Config{})
	if err != nil {
		t.Fatalf("open db: %v", err)
	}
	if err := db.AutoMigrate(&postgres.RoleRow{}, &postgres.UserRow{}, &postgres.SessionRow{}); err != nil {
		t.Fatalf("auto migrate: %v", err)
	}
	roleRow := postgres.RoleRow{Name: "ADMIN"}
	if err := db.Create(&roleRow).Error; err != nil {
		t.Fatalf("create role: %v", err)
	}
	userRow := postgres.UserRow{Email: "admin@example.com"}
	if err := db.Create(&userRow).Error; err != nil {
		t.Fatalf("create user: %v", err)
	}
	if err := db.Model(&userRow).Association("Roles").Append(&roleRow); err != nil {
		t.Fatalf("attach role: %v", err)
	}
	expiresAt := time.Now().Add(time.Hour)
	sessionRow := postgres.SessionRow{ID: "test-session", UserID: userRow.ID, ExpiresAt: &expiresAt}
	if err := db.Create(&sessionRow).Error; err != nil {
		t.Fatalf("create session: %v", err)
	}

	repo := postgres.NewRepository(db)
	svc := auth.NewService(repo)

	router := gin.New()
	auth.RegisterRoutes(router, svc)
	RegisterRoutes(router, db)

	req := httptest.NewRequest(http.MethodGet, "/api/admin/ai/llms", nil)
	req.AddCookie(&http.Cookie{Name: "session_id", Value: sessionRow.ID})
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
