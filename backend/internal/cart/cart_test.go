package cart

import (
	"encoding/json"
	"net/http/httptest"
	"testing"

	"github.com/gin-gonic/gin"
	"gorm.io/driver/sqlite"
	"gorm.io/gorm"

	"voenix/backend/internal/article"
	"voenix/backend/internal/auth"
	"voenix/backend/internal/prompt"
)

func setupCartTestDB(t *testing.T) *gorm.DB {
	t.Helper()
	db, err := gorm.Open(sqlite.Open(":memory:"), &gorm.Config{})
	if err != nil {
		t.Fatalf("open db: %v", err)
	}
	if err := db.AutoMigrate(
		&article.Article{},
		&article.ArticleCategory{},
		&article.ArticleSubCategory{},
		&article.MugVariant{},
		&article.CostCalculation{},
		&prompt.Prompt{},
		&Cart{},
		&CartItem{},
		&auth.User{},
	); err != nil {
		t.Fatalf("migrate: %v", err)
	}
	return db
}

func TestAssembleCartDtoIncludesPromptPricing(t *testing.T) {
	gin.SetMode(gin.TestMode)
	db := setupCartTestDB(t)

	art := article.Article{ID: 1, Name: "Mug", DescriptionShort: "short", DescriptionLong: "long", Active: true, ArticleType: article.ArticleTypeMug}
	if err := db.Create(&art).Error; err != nil {
		t.Fatalf("seed article: %v", err)
	}
	variant := article.MugVariant{ID: 1, ArticleID: art.ID, Name: "Default", OutsideColorCode: "#ffffff", InsideColorCode: "#000000", IsDefault: true, Active: true}
	if err := db.Create(&variant).Error; err != nil {
		t.Fatalf("seed variant: %v", err)
	}
	promptRow := prompt.Prompt{ID: 1, Title: "Snowy Scene", Active: true}
	if err := db.Create(&promptRow).Error; err != nil {
		t.Fatalf("seed prompt: %v", err)
	}
	user := auth.User{ID: 42, Email: "user@example.com"}
	if err := db.Create(&user).Error; err != nil {
		t.Fatalf("seed user: %v", err)
	}
	cartRow := Cart{UserID: user.ID, Status: string(CartStatusActive)}
	if err := db.Create(&cartRow).Error; err != nil {
		t.Fatalf("seed cart: %v", err)
	}

	promptID := promptRow.ID
	itemWithPrompt := CartItem{
		CartID:              cartRow.ID,
		ArticleID:           art.ID,
		VariantID:           variant.ID,
		Quantity:            2,
		PriceAtTime:         1000,
		OriginalPrice:       1100,
		PromptPriceAtTime:   250,
		PromptOriginalPrice: 300,
		PromptID:            &promptID,
		CustomData:          "{}",
		Position:            0,
	}
	if err := db.Create(&itemWithPrompt).Error; err != nil {
		t.Fatalf("seed item with prompt: %v", err)
	}

	itemWithoutPrompt := CartItem{
		CartID:            cartRow.ID,
		ArticleID:         art.ID,
		VariantID:         variant.ID,
		Quantity:          1,
		PriceAtTime:       1500,
		OriginalPrice:     1500,
		PromptPriceAtTime: 0,
		CustomData:        "{}",
		Position:          1,
	}
	if err := db.Create(&itemWithoutPrompt).Error; err != nil {
		t.Fatalf("seed item without prompt: %v", err)
	}

	var loadedCart Cart
	if err := db.Preload("Items", withItemOrder).First(&loadedCart, cartRow.ID).Error; err != nil {
		t.Fatalf("load cart: %v", err)
	}

	dto, err := assembleCartDto(db, &loadedCart)
	if err != nil {
		t.Fatalf("assemble dto: %v", err)
	}
	expectedTotal := (1000 + 250) * 2
	expectedTotal += 1500
	if dto.TotalPrice != expectedTotal {
		t.Fatalf("total price = %d", dto.TotalPrice)
	}
	if dto.TotalItemCount != 3 {
		t.Fatalf("total item count = %d", dto.TotalItemCount)
	}

	var promptItem *CartItemDto
	var plainItem *CartItemDto
	for i := range dto.Items {
		it := dto.Items[i]
		if it.PromptID != nil {
			promptItem = &it
		} else {
			plainItem = &it
		}
	}
	if promptItem == nil || plainItem == nil {
		t.Fatalf("expected both prompt and plain items in dto")
	}
	if promptItem.ArticlePriceAtTime != 1000 {
		t.Fatalf("article price at time = %d", promptItem.ArticlePriceAtTime)
	}
	if promptItem.PromptPriceAtTime != 250 {
		t.Fatalf("prompt price at time = %d", promptItem.PromptPriceAtTime)
	}
	if promptItem.TotalPrice != (1000+250)*2 {
		t.Fatalf("prompt item total = %d", promptItem.TotalPrice)
	}
	if !promptItem.HasPriceChanged {
		t.Fatalf("expected hasPriceChanged true")
	}
	if !promptItem.HasPromptPriceChanged {
		t.Fatalf("expected hasPromptPriceChanged true")
	}
	if promptItem.PromptTitle == nil || *promptItem.PromptTitle != "Snowy Scene" {
		t.Fatalf("unexpected prompt title: %v", promptItem.PromptTitle)
	}
	if plainItem.PromptPriceAtTime != 0 {
		t.Fatalf("plain item prompt price = %d", plainItem.PromptPriceAtTime)
	}
	if plainItem.TotalPrice != 1500 {
		t.Fatalf("plain item total = %d", plainItem.TotalPrice)
	}
	if plainItem.HasPromptPriceChanged {
		t.Fatalf("expected hasPromptPriceChanged false for plain item")
	}
}

func TestGetCartSummaryIncludesPromptPrice(t *testing.T) {
	gin.SetMode(gin.TestMode)
	db := setupCartTestDB(t)

	art := article.Article{ID: 5, Name: "Another Mug", DescriptionShort: "s", DescriptionLong: "l", Active: true, ArticleType: article.ArticleTypeMug}
	if err := db.Create(&art).Error; err != nil {
		t.Fatalf("seed article: %v", err)
	}
	variant := article.MugVariant{ID: 9, ArticleID: art.ID, Name: "Variant", OutsideColorCode: "#ffffff", InsideColorCode: "#000000", IsDefault: true, Active: true}
	if err := db.Create(&variant).Error; err != nil {
		t.Fatalf("seed variant: %v", err)
	}
	promptRow := prompt.Prompt{ID: 7, Title: "Starry", Active: true}
	if err := db.Create(&promptRow).Error; err != nil {
		t.Fatalf("seed prompt: %v", err)
	}
	user := auth.User{ID: 77, Email: "summary@example.com"}
	if err := db.Create(&user).Error; err != nil {
		t.Fatalf("seed user: %v", err)
	}
	cartRow := Cart{UserID: user.ID, Status: string(CartStatusActive)}
	if err := db.Create(&cartRow).Error; err != nil {
		t.Fatalf("seed cart: %v", err)
	}

	promptID := promptRow.ID
	item := CartItem{
		CartID:              cartRow.ID,
		ArticleID:           art.ID,
		VariantID:           variant.ID,
		Quantity:            3,
		PriceAtTime:         800,
		OriginalPrice:       800,
		PromptPriceAtTime:   150,
		PromptOriginalPrice: 150,
		PromptID:            &promptID,
		CustomData:          "{}",
		Position:            0,
	}
	if err := db.Create(&item).Error; err != nil {
		t.Fatalf("seed item: %v", err)
	}

	w := httptest.NewRecorder()
	ctx, _ := gin.CreateTestContext(w)
	ctx.Request = httptest.NewRequest("GET", "/api/user/cart/summary", nil)
	ctx.Set("currentUser", &user)

	handler := getCartSummaryHandler(db)
	handler(ctx)

	if w.Code != 200 {
		t.Fatalf("status code = %d", w.Code)
	}
	var resp CartSummaryDto
	if err := json.Unmarshal(w.Body.Bytes(), &resp); err != nil {
		t.Fatalf("unmarshal: %v", err)
	}
	if resp.TotalPrice != (800+150)*3 {
		t.Fatalf("total price = %d", resp.TotalPrice)
	}
	if resp.ItemCount != 3 {
		t.Fatalf("item count = %d", resp.ItemCount)
	}
	if !resp.HasItems {
		t.Fatalf("expected hasItems true")
	}
}
