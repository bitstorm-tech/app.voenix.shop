package prompt

import (
	"context"
	"errors"
	"testing"
	"time"

	"gorm.io/gorm"

	"voenix/backend/internal/article"
)

type mockRepository struct {
	slotTypes    map[int]PromptSlotType
	slotVariants map[int]PromptSlotVariant
	nextTypeID   int
	nextVarID    int
}

func newMockRepository() *mockRepository {
	return &mockRepository{
		slotTypes:    make(map[int]PromptSlotType),
		slotVariants: make(map[int]PromptSlotVariant),
		nextTypeID:   1,
		nextVarID:    1,
	}
}

func (m *mockRepository) addSlotType(name string, position int) PromptSlotType {
	id := m.nextTypeID
	m.nextTypeID++
	st := PromptSlotType{ID: id, Name: name, Position: position, CreatedAt: time.Now(), UpdatedAt: time.Now()}
	m.slotTypes[id] = st
	return st
}

func (m *mockRepository) cloneVariant(v PromptSlotVariant) PromptSlotVariant {
	clone := v
	if v.Prompt != nil {
		p := *v.Prompt
		clone.Prompt = &p
	}
	if v.Description != nil {
		d := *v.Description
		clone.Description = &d
	}
	if v.ExampleImageFilename != nil {
		f := *v.ExampleImageFilename
		clone.ExampleImageFilename = &f
	}
	if v.PromptSlotType != nil {
		st := *v.PromptSlotType
		clone.PromptSlotType = &st
	}
	return clone
}

func (m *mockRepository) ListSlotTypes(context.Context) ([]PromptSlotType, error) {
	panic("not implemented")
}

func (m *mockRepository) SlotTypeByID(_ context.Context, id int) (*PromptSlotType, error) {
	st, ok := m.slotTypes[id]
	if !ok {
		return nil, gorm.ErrRecordNotFound
	}
	clone := st
	return &clone, nil
}

func (m *mockRepository) SlotTypeNameExists(_ context.Context, name string, excludeID *int) (bool, error) {
	for id, st := range m.slotTypes {
		if excludeID != nil && id == *excludeID {
			continue
		}
		if st.Name == name {
			return true, nil
		}
	}
	return false, nil
}

func (m *mockRepository) SlotTypePositionExists(_ context.Context, position int, excludeID *int) (bool, error) {
	for id, st := range m.slotTypes {
		if excludeID != nil && id == *excludeID {
			continue
		}
		if st.Position == position {
			return true, nil
		}
	}
	return false, nil
}

func (m *mockRepository) CreateSlotType(_ context.Context, slotType *PromptSlotType) error {
	st := m.addSlotType(slotType.Name, slotType.Position)
	*slotType = st
	return nil
}

func (m *mockRepository) SaveSlotType(context.Context, *PromptSlotType) error {
	panic("not implemented")
}

func (m *mockRepository) DeleteSlotType(context.Context, int) error {
	panic("not implemented")
}

func (m *mockRepository) ListSlotVariants(context.Context) ([]PromptSlotVariant, error) {
	panic("not implemented")
}

func (m *mockRepository) SlotVariantByID(_ context.Context, id int) (*PromptSlotVariant, error) {
	variant, ok := m.slotVariants[id]
	if !ok {
		return nil, gorm.ErrRecordNotFound
	}
	clone := m.cloneVariant(variant)
	return &clone, nil
}

func (m *mockRepository) SlotVariantNameExists(_ context.Context, name string, excludeID *int) (bool, error) {
	for id, v := range m.slotVariants {
		if excludeID != nil && id == *excludeID {
			continue
		}
		if v.Name == name {
			return true, nil
		}
	}
	return false, nil
}

func (m *mockRepository) CreateSlotVariant(_ context.Context, variant *PromptSlotVariant) error {
	id := m.nextVarID
	m.nextVarID++
	now := time.Now()
	variant.ID = id
	variant.CreatedAt = now
	variant.UpdatedAt = now
	clone := m.cloneVariant(*variant)
	m.slotVariants[id] = clone
	return nil
}

func (m *mockRepository) SaveSlotVariant(_ context.Context, variant *PromptSlotVariant) error {
	if _, ok := m.slotVariants[variant.ID]; !ok {
		return gorm.ErrRecordNotFound
	}
	variant.UpdatedAt = time.Now()
	clone := m.cloneVariant(*variant)
	m.slotVariants[variant.ID] = clone
	return nil
}

func (m *mockRepository) DeleteSlotVariant(context.Context, int) error {
	panic("not implemented")
}

func (m *mockRepository) SlotTypeExists(_ context.Context, id int) (bool, error) {
	_, ok := m.slotTypes[id]
	return ok, nil
}

func (m *mockRepository) SlotVariantsExist(_ context.Context, ids []int) (bool, error) {
	for _, id := range ids {
		if _, ok := m.slotVariants[id]; !ok {
			return false, nil
		}
	}
	return true, nil
}

func (m *mockRepository) ListCategories(context.Context) ([]PromptCategory, error) {
	panic("not implemented")
}

func (m *mockRepository) CreateCategory(context.Context, *PromptCategory) error {
	panic("not implemented")
}

func (m *mockRepository) CategoryByID(context.Context, int) (*PromptCategory, error) {
	panic("not implemented")
}

func (m *mockRepository) SaveCategory(context.Context, *PromptCategory) error {
	panic("not implemented")
}

func (m *mockRepository) DeleteCategory(context.Context, int) error {
	panic("not implemented")
}

func (m *mockRepository) CountPromptsByCategory(context.Context, int) (int, error) {
	panic("not implemented")
}

func (m *mockRepository) CountSubCategoriesByCategory(context.Context, int) (int, error) {
	panic("not implemented")
}

func (m *mockRepository) ListSubCategories(context.Context) ([]PromptSubCategory, error) {
	panic("not implemented")
}

func (m *mockRepository) ListSubCategoriesByCategory(context.Context, int) ([]PromptSubCategory, error) {
	panic("not implemented")
}

func (m *mockRepository) CreateSubCategory(context.Context, *PromptSubCategory) error {
	panic("not implemented")
}

func (m *mockRepository) SubCategoryByID(context.Context, int) (*PromptSubCategory, error) {
	panic("not implemented")
}

func (m *mockRepository) SaveSubCategory(context.Context, *PromptSubCategory) error {
	panic("not implemented")
}

func (m *mockRepository) DeleteSubCategory(context.Context, int) error {
	panic("not implemented")
}

func (m *mockRepository) CountPromptsBySubCategory(context.Context, int) (int, error) {
	panic("not implemented")
}

func (m *mockRepository) CategoryExists(context.Context, int) (bool, error) {
	panic("not implemented")
}

func (m *mockRepository) ListPrompts(context.Context) ([]Prompt, error) {
	panic("not implemented")
}

func (m *mockRepository) ListPublicPrompts(context.Context) ([]Prompt, error) {
	panic("not implemented")
}

func (m *mockRepository) PromptByID(context.Context, int) (*Prompt, error) {
	panic("not implemented")
}

func (m *mockRepository) PromptsByIDs(context.Context, []int) ([]Prompt, error) {
	panic("not implemented")
}

func (m *mockRepository) CreatePrompt(context.Context, *Prompt) error {
	panic("not implemented")
}

func (m *mockRepository) SavePrompt(context.Context, *Prompt) error {
	panic("not implemented")
}

func (m *mockRepository) DeletePrompt(context.Context, int) error {
	panic("not implemented")
}

func (m *mockRepository) ReplacePromptSlotVariantMappings(context.Context, int, []int) error {
	panic("not implemented")
}

func (m *mockRepository) CreatePrice(context.Context, *article.Price) error {
	panic("not implemented")
}

func (m *mockRepository) PriceByID(context.Context, int) (*article.Price, error) {
	panic("not implemented")
}

func (m *mockRepository) SavePrice(context.Context, *article.Price) error {
	panic("not implemented")
}

func (m *mockRepository) VatExists(context.Context, int) (bool, error) {
	panic("not implemented")
}

func setupPromptServiceTest(t *testing.T) (*Service, *mockRepository) {
	t.Helper()
	repo := newMockRepository()
	allowedLLMs := []string{
		"gemini-2.5-flash-image-preview",
		"gpt-image-1",
		"flux",
	}
	return NewService(repo, allowedLLMs), repo
}

func strPtr(v string) *string { return &v }

func TestCreateSlotVariantPersistsLLM(t *testing.T) {
	svc, repo := setupPromptServiceTest(t)
	slotType := repo.addSlotType("Primary", 1)
	promptText := "Example prompt"
	payload := slotVariantCreate{
		PromptSlotTypeID:     slotType.ID,
		Name:                 "Variant A",
		Prompt:               &promptText,
		Description:          nil,
		ExampleImageFilename: nil,
		LLM:                  "gemini-2.5-flash-image-preview",
	}
	created, err := svc.CreateSlotVariant(context.Background(), payload)
	if err != nil {
		t.Fatalf("create slot variant: %v", err)
	}
	if created.LLM != "gemini-2.5-flash-image-preview" {
		t.Fatalf("expected llm gemini-2.5-flash-image-preview, got %s", created.LLM)
	}
	stored, err := repo.SlotVariantByID(context.Background(), created.ID)
	if err != nil {
		t.Fatalf("load stored variant: %v", err)
	}
	if stored.LLM != "gemini-2.5-flash-image-preview" {
		t.Fatalf("expected stored llm gemini-2.5-flash-image-preview, got %s", stored.LLM)
	}
}

func TestCreateSlotVariantRejectsInvalidLLM(t *testing.T) {
	svc, repo := setupPromptServiceTest(t)
	slotType := repo.addSlotType("Primary", 1)
	payload := slotVariantCreate{
		PromptSlotTypeID:     slotType.ID,
		Name:                 "Variant B",
		Prompt:               nil,
		Description:          nil,
		ExampleImageFilename: nil,
		LLM:                  "not-a-real-llm",
	}
	if _, err := svc.CreateSlotVariant(context.Background(), payload); !errors.Is(err, errInvalidLLM) {
		t.Fatalf("expected errInvalidLLM, got %v", err)
	}
}

func TestUpdateSlotVariantValidatesLLM(t *testing.T) {
	svc, repo := setupPromptServiceTest(t)
	slotType := repo.addSlotType("Primary", 1)
	variant := PromptSlotVariant{PromptSlotTypeID: slotType.ID, Name: "Variant C", LLM: "gpt-image-1"}
	if err := repo.CreateSlotVariant(context.Background(), &variant); err != nil {
		t.Fatalf("seed variant: %v", err)
	}
	update := slotVariantUpdate{LLM: strPtr("gemini-2.5-flash-image-preview")}
	updated, err := svc.UpdateSlotVariant(context.Background(), variant.ID, update)
	if err != nil {
		t.Fatalf("update variant: %v", err)
	}
	if updated.LLM != "gemini-2.5-flash-image-preview" {
		t.Fatalf("expected llm gemini-2.5-flash-image-preview after update, got %s", updated.LLM)
	}
	invalid := slotVariantUpdate{LLM: strPtr("  ")}
	if _, err := svc.UpdateSlotVariant(context.Background(), variant.ID, invalid); !errors.Is(err, errInvalidLLM) {
		t.Fatalf("expected errInvalidLLM for blank llm, got %v", err)
	}
}
