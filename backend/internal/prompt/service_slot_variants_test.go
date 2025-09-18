package prompt

import (
	"context"
	"errors"
	"testing"

	"gorm.io/driver/sqlite"
	"gorm.io/gorm"

	"voenix/backend/internal/auth"
)

func setupPromptServiceTest(t *testing.T) (*service, *gorm.DB) {
	t.Helper()
	db, err := gorm.Open(sqlite.Open(":memory:"), &gorm.Config{})
	if err != nil {
		t.Fatalf("open db: %v", err)
	}
	if err := db.AutoMigrate(&PromptSlotType{}, &PromptSlotVariant{}, &auth.Role{}, &auth.User{}, &auth.Session{}); err != nil {
		t.Fatalf("auto migrate: %v", err)
	}
	allowedLLMs := []string{
		"gemini-2.5-flash-image-preview",
		"gpt-image-1",
		"flux",
	}
	return newService(db, allowedLLMs), db
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
		LLM:                  "gemini-2.5-flash-image-preview",
	}
	created, err := svc.createSlotVariant(context.Background(), payload)
	if err != nil {
		t.Fatalf("create slot variant: %v", err)
	}
	if created.LLM != "gemini-2.5-flash-image-preview" {
		t.Fatalf("expected llm gemini-2.5-flash-image-preview, got %s", created.LLM)
	}
	var stored PromptSlotVariant
	if err := db.First(&stored, created.ID).Error; err != nil {
		t.Fatalf("load stored variant: %v", err)
	}
	if stored.LLM != "gemini-2.5-flash-image-preview" {
		t.Fatalf("expected stored llm gemini-2.5-flash-image-preview, got %s", stored.LLM)
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
	variant := PromptSlotVariant{PromptSlotTypeID: slotType.ID, Name: "Variant C", LLM: "gpt-image-1"}
	if err := db.Create(&variant).Error; err != nil {
		t.Fatalf("seed variant: %v", err)
	}
	update := slotVariantUpdate{LLM: strPtr("gemini-2.5-flash-image-preview")}
	updated, err := svc.updateSlotVariant(context.Background(), variant.ID, update)
	if err != nil {
		t.Fatalf("update variant: %v", err)
	}
	if updated.LLM != "gemini-2.5-flash-image-preview" {
		t.Fatalf("expected llm gemini-2.5-flash-image-preview after update, got %s", updated.LLM)
	}
	invalid := slotVariantUpdate{LLM: strPtr("  ")}
	if _, err := svc.updateSlotVariant(context.Background(), variant.ID, invalid); !errors.Is(err, errInvalidLLM) {
		t.Fatalf("expected errInvalidLLM for blank llm, got %v", err)
	}
}
