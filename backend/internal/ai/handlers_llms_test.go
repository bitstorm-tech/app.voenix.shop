package ai

import (
	"context"
	"encoding/json"
	"net/http"
	"net/http/httptest"
	"testing"
	"time"

	"github.com/gin-gonic/gin"
	"gorm.io/driver/sqlite"
	"gorm.io/gorm"

	"voenix/backend/internal/article"
	"voenix/backend/internal/auth"
	authpg "voenix/backend/internal/auth/postgres"
	img "voenix/backend/internal/image"
	imagepg "voenix/backend/internal/image/postgres"
	"voenix/backend/internal/prompt"
)

type fakePromptService struct{}

func (fakePromptService) GetPrompt(context.Context, int) (*prompt.PromptRead, error) {
	return nil, nil
}

type fakeArticleService struct{}

func (fakeArticleService) GetMugDetails(context.Context, int) (*article.MugDetails, error) {
	return nil, nil
}

func TestAdminLLMsEndpoint(t *testing.T) {
	gin.SetMode(gin.TestMode)
	db, err := gorm.Open(sqlite.Open(":memory:"), &gorm.Config{})
	if err != nil {
		t.Fatalf("open db: %v", err)
	}
	if err := db.AutoMigrate(&authpg.RoleRow{}, &authpg.UserRow{}, &authpg.SessionRow{}); err != nil {
		t.Fatalf("auto migrate: %v", err)
	}
	roleRow := authpg.RoleRow{Name: "ADMIN"}
	if err := db.Create(&roleRow).Error; err != nil {
		t.Fatalf("create role: %v", err)
	}
	userRow := authpg.UserRow{Email: "admin@example.com"}
	if err := db.Create(&userRow).Error; err != nil {
		t.Fatalf("create user: %v", err)
	}
	if err := db.Model(&userRow).Association("Roles").Append(&roleRow); err != nil {
		t.Fatalf("attach role: %v", err)
	}
	expiresAt := time.Now().Add(time.Hour)
	sessionRow := authpg.SessionRow{ID: "test-session", UserID: userRow.ID, ExpiresAt: &expiresAt}
	if err := db.Create(&sessionRow).Error; err != nil {
		t.Fatalf("create session: %v", err)
	}

	authRepository := authpg.NewRepository(db)
	authService := auth.NewService(authRepository)

	imageRepository := imagepg.NewRepository(db)
	imageService := img.NewService(imageRepository)

	router := gin.New()
	auth.RegisterRoutes(router, authService)
	RegisterRoutes(router, db, imageService, fakePromptService{}, fakeArticleService{})

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
