package prompt

import (
	"context"
	"encoding/json"
	"errors"
	"net/http"
	"net/http/httptest"
	"testing"
	"time"

	"github.com/gin-gonic/gin"
	"gorm.io/driver/sqlite"
	"gorm.io/gorm"

	"voenix/backend/internal/auth"
)

func setupPromptServiceTest(t *testing.T) (*service, *gorm.DB) {
	t.Helper()
	t.Setenv(llmOptionsEnv, "gpt-4o:OpenAI GPT-4o,gemini-1.5-pro:Gemini 1.5 Pro")
	db, err := gorm.Open(sqlite.Open(":memory:"), &gorm.Config{})
	if err != nil {
		t.Fatalf("open db: %v", err)
	}
	if err := db.AutoMigrate(&PromptSlotType{}, &PromptSlotVariant{}, &auth.Role{}, &auth.User{}, &auth.Session{}); err != nil {
		t.Fatalf("auto migrate: %v", err)
	}
	return newService(db), db
}

func strPtr(v string) *string {
	return &v
}

func TestCreateSlotVariantPersistsLLM(t *testing.T) {
	svc, db := setupPromptServiceTest(t)
	slotType := PromptSlotType{Name: "Primary", Position: 1}
	if err := db.Create(&slotType).Error; err != nil {
		t.Fatalf("seed slot type: %v", err)
	}
	promptText := "Example prompt"
	payload := slotVariantCreate{
		PromptSlotTypeID:     slotType.ID,
		Name:                 "Variant A",
		Prompt:               &promptText,
		Description:          nil,
		ExampleImageFilename: nil,
		LLM:                  "gemini-1.5-pro",
	}
	created, err := svc.createSlotVariant(context.Background(), payload)
	if err != nil {
		t.Fatalf("create slot variant: %v", err)
	}
	if created.LLM != "gemini-1.5-pro" {
		t.Fatalf("expected llm gemini-1.5-pro, got %s", created.LLM)
	}
	var stored PromptSlotVariant
	if err := db.First(&stored, created.ID).Error; err != nil {
		t.Fatalf("load stored variant: %v", err)
	}
	if stored.LLM != "gemini-1.5-pro" {
		t.Fatalf("expected stored llm gemini-1.5-pro, got %s", stored.LLM)
	}
}

func TestCreateSlotVariantRejectsInvalidLLM(t *testing.T) {
	svc, db := setupPromptServiceTest(t)
	slotType := PromptSlotType{Name: "Primary", Position: 1}
	if err := db.Create(&slotType).Error; err != nil {
		t.Fatalf("seed slot type: %v", err)
	}
	payload := slotVariantCreate{
		PromptSlotTypeID:     slotType.ID,
		Name:                 "Variant B",
		Prompt:               nil,
		Description:          nil,
		ExampleImageFilename: nil,
		LLM:                  "not-a-real-llm",
	}
	if _, err := svc.createSlotVariant(context.Background(), payload); !errors.Is(err, errInvalidLLM) {
		t.Fatalf("expected errInvalidLLM, got %v", err)
	}
}

func TestUpdateSlotVariantValidatesLLM(t *testing.T) {
	svc, db := setupPromptServiceTest(t)
	slotType := PromptSlotType{Name: "Primary", Position: 1}
	if err := db.Create(&slotType).Error; err != nil {
		t.Fatalf("seed slot type: %v", err)
	}
	variant := PromptSlotVariant{PromptSlotTypeID: slotType.ID, Name: "Variant C", LLM: "gpt-4o"}
	if err := db.Create(&variant).Error; err != nil {
		t.Fatalf("seed variant: %v", err)
	}
	update := slotVariantUpdate{LLM: strPtr("gemini-1.5-pro")}
	updated, err := svc.updateSlotVariant(context.Background(), variant.ID, update)
	if err != nil {
		t.Fatalf("update variant: %v", err)
	}
	if updated.LLM != "gemini-1.5-pro" {
		t.Fatalf("expected llm gemini-1.5-pro after update, got %s", updated.LLM)
	}
	invalid := slotVariantUpdate{LLM: strPtr("  ")}
	if _, err := svc.updateSlotVariant(context.Background(), variant.ID, invalid); !errors.Is(err, errInvalidLLM) {
		t.Fatalf("expected errInvalidLLM for blank llm, got %v", err)
	}
}

func TestAdminLLMOptionsHandler(t *testing.T) {
	gin.SetMode(gin.TestMode)
	svc, db := setupPromptServiceTest(t)
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
	registerAdminLLMRoutes(router, db, svc)

	req := httptest.NewRequest(http.MethodGet, "/api/admin/prompts/llms", nil)
	req.AddCookie(&http.Cookie{Name: "session_id", Value: session.ID})
	w := httptest.NewRecorder()

	router.ServeHTTP(w, req)

	if w.Code != http.StatusOK {
		t.Fatalf("expected status 200, got %d", w.Code)
	}
	var resp struct {
		LLMs []LLMOption `json:"llms"`
	}
	if err := json.Unmarshal(w.Body.Bytes(), &resp); err != nil {
		t.Fatalf("decode response: %v", err)
	}
	if len(resp.LLMs) == 0 {
		t.Fatalf("expected at least one llm option")
	}
}
