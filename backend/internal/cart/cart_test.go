package cart_test

import (
	"context"
	"testing"

	"gorm.io/driver/sqlite"
	"gorm.io/gorm"

	"voenix/backend/internal/article"
	"voenix/backend/internal/auth"
	authpostgres "voenix/backend/internal/auth/postgres"
	cartpkg "voenix/backend/internal/cart"
	cartpostgres "voenix/backend/internal/cart/postgres"
	"voenix/backend/internal/prompt"
	promptpostgres "voenix/backend/internal/prompt/postgres"
)

type stubArticleService struct {
	db *gorm.DB
}

func (s *stubArticleService) GetArticleSummary(ctx context.Context, id int) (article.ArticleResponse, error) {
	var a article.Article
	if err := s.db.WithContext(ctx).First(&a, "id = ?", id).Error; err != nil {
		return article.ArticleResponse{}, err
	}
	created := a.CreatedAt
	updated := a.UpdatedAt
	return article.ArticleResponse{
		ID:                    a.ID,
		Name:                  a.Name,
		DescriptionShort:      a.DescriptionShort,
		DescriptionLong:       a.DescriptionLong,
		Active:                a.Active,
		ArticleType:           a.ArticleType,
		CategoryID:            a.CategoryID,
		SubcategoryID:         a.SubcategoryID,
		SupplierID:            a.SupplierID,
		SupplierArticleName:   a.SupplierArticleName,
		SupplierArticleNumber: a.SupplierArticleNumber,
		CreatedAt:             &created,
		UpdatedAt:             &updated,
	}, nil
}

func (s *stubArticleService) GetArticle(ctx context.Context, id int) (article.Article, error) {
	var a article.Article
	if err := s.db.WithContext(ctx).First(&a, "id = ?", id).Error; err != nil {
		return article.Article{}, err
	}
	return a, nil
}

func (s *stubArticleService) GetMugVariant(ctx context.Context, id int) (article.MugVariant, error) {
	var v article.MugVariant
	if err := s.db.WithContext(ctx).First(&v, "id = ?", id).Error; err != nil {
		return article.MugVariant{}, err
	}
	return v, nil
}

func (s *stubArticleService) GetCostCalculation(ctx context.Context, articleID int) (*article.Price, error) {
	var cc article.Price
	err := s.db.WithContext(ctx).First(&cc, "article_id = ?", articleID).Error
	if err != nil {
		if err == gorm.ErrRecordNotFound {
			return nil, nil
		}
		return nil, err
	}
	return &cc, nil
}

func (s *stubArticleService) GetCostCalculationByID(ctx context.Context, id int) (*article.Price, error) {
	var cc article.Price
	err := s.db.WithContext(ctx).First(&cc, "id = ?", id).Error
	if err != nil {
		if err == gorm.ErrRecordNotFound {
			return nil, nil
		}
		return nil, err
	}
	return &cc, nil
}

func setupCartTestDB(t *testing.T) *gorm.DB {
	t.Helper()
	db, err := gorm.Open(sqlite.Open(":memory:"), &gorm.Config{})
	if err != nil {
		t.Fatalf("open db: %v", err)
	}
	models := []any{
		&article.Article{},
		&article.ArticleCategory{},
		&article.ArticleSubCategory{},
		&article.MugVariant{},
		&article.Price{},
		&cartpostgres.CartRow{},
		&cartpostgres.CartItemRow{},
		&authpostgres.UserRow{},
	}
	models = append(models,
		&promptpostgres.PromptCategoryRow{},
		&promptpostgres.PromptSubCategoryRow{},
		&promptpostgres.PromptSlotTypeRow{},
		&promptpostgres.PromptSlotVariantRow{},
		&promptpostgres.PromptSlotVariantMappingRow{},
		&promptpostgres.PromptRow{},
	)
	if err := db.AutoMigrate(models...); err != nil {
		t.Fatalf("migrate: %v", err)
	}
	return db
}

func TestCartResponseIncludesPromptPricing(t *testing.T) {
	db := setupCartTestDB(t)

	art := article.Article{ID: 1, Name: "Mug", DescriptionShort: "short", DescriptionLong: "long", Active: true, ArticleType: article.ArticleTypeMug}
	if err := db.Create(&art).Error; err != nil {
		t.Fatalf("seed article: %v", err)
	}
	variant := article.MugVariant{ID: 1, ArticleID: art.ID, Name: "Default", OutsideColorCode: "#ffffff", InsideColorCode: "#000000", IsDefault: true, Active: true}
	if err := db.Create(&variant).Error; err != nil {
		t.Fatalf("seed variant: %v", err)
	}
	promptRepo := promptpostgres.NewRepository(db)
	promptRow := prompt.Prompt{Title: "Snowy Scene", Active: true}
	if err := promptRepo.CreatePrompt(context.Background(), &promptRow); err != nil {
		t.Fatalf("seed prompt: %v", err)
	}
	userRow := authpostgres.UserRow{ID: 42, Email: "user@example.com"}
	if err := db.Create(&userRow).Error; err != nil {
		t.Fatalf("seed user: %v", err)
	}
	user := auth.User{ID: userRow.ID, Email: userRow.Email}
	repo := cartpostgres.NewRepository(db)
	cartDomain, err := repo.GetOrCreateActiveCart(context.Background(), user.ID)
	if err != nil {
		t.Fatalf("create cart: %v", err)
	}
	cartDomain.Status = cartpkg.CartStatusActive
	promptID := promptRow.ID
	cartDomain.Items = []cartpkg.CartItem{
		{
			CartID:              cartDomain.ID,
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
		},
		{
			CartID:              cartDomain.ID,
			ArticleID:           art.ID,
			VariantID:           variant.ID,
			Quantity:            1,
			PriceAtTime:         1500,
			OriginalPrice:       1500,
			PromptPriceAtTime:   0,
			PromptOriginalPrice: 0,
			CustomData:          "{}",
			Position:            1,
		},
	}
	if _, err := repo.SaveCart(context.Background(), *cartDomain); err != nil {
		t.Fatalf("save cart: %v", err)
	}

	svc := cartpkg.NewService(repo, &stubArticleService{db: db})
	detail, err := svc.GetCart(context.Background(), user.ID)
	if err != nil {
		t.Fatalf("load cart: %v", err)
	}
	dto, err := svc.ToCartResponse(context.Background(), detail)
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

	var promptItem *cartpkg.CartItemResponse
	var plainItem *cartpkg.CartItemResponse
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
	db := setupCartTestDB(t)

	art := article.Article{ID: 5, Name: "Another Mug", DescriptionShort: "s", DescriptionLong: "l", Active: true, ArticleType: article.ArticleTypeMug}
	if err := db.Create(&art).Error; err != nil {
		t.Fatalf("seed article: %v", err)
	}
	variant := article.MugVariant{ID: 9, ArticleID: art.ID, Name: "Variant", OutsideColorCode: "#ffffff", InsideColorCode: "#000000", IsDefault: true, Active: true}
	if err := db.Create(&variant).Error; err != nil {
		t.Fatalf("seed variant: %v", err)
	}
	promptRepo := promptpostgres.NewRepository(db)
	promptRow := prompt.Prompt{Title: "Starry", Active: true}
	if err := promptRepo.CreatePrompt(context.Background(), &promptRow); err != nil {
		t.Fatalf("seed prompt: %v", err)
	}
	userRow := authpostgres.UserRow{ID: 77, Email: "summary@example.com"}
	if err := db.Create(&userRow).Error; err != nil {
		t.Fatalf("seed user: %v", err)
	}
	user := auth.User{ID: userRow.ID, Email: userRow.Email}
	repo := cartpostgres.NewRepository(db)
	cartDomain, err := repo.GetOrCreateActiveCart(context.Background(), user.ID)
	if err != nil {
		t.Fatalf("create cart: %v", err)
	}
	promptID := promptRow.ID
	cartDomain.Items = []cartpkg.CartItem{
		{
			CartID:              cartDomain.ID,
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
		},
	}
	if _, err := repo.SaveCart(context.Background(), *cartDomain); err != nil {
		t.Fatalf("save cart: %v", err)
	}

	summary, err := cartpkg.NewService(repo, &stubArticleService{db: db}).GetCartSummary(context.Background(), user.ID)
	if err != nil {
		t.Fatalf("get summary: %v", err)
	}
	if summary.TotalPrice != (800+150)*3 {
		t.Fatalf("total price = %d", summary.TotalPrice)
	}
	if summary.ItemCount != 3 {
		t.Fatalf("item count = %d", summary.ItemCount)
	}
	if !summary.HasItems {
		t.Fatalf("expected hasItems true")
	}
}
