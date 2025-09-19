package article

import (
	"context"
	"errors"
	"fmt"

	"gorm.io/gorm"
)

// Service exposes article domain operations backed by a repository implementation.
type Service struct {
	repo Repository
}

func NewService(repo Repository) *Service { return &Service{repo: repo} }

var (
	ErrCategoryNotFound    = errors.New("category not found")
	ErrSubcategoryNotFound = errors.New("subcategory not found")
	ErrSupplierNotFound    = errors.New("supplier not found")
	ErrVatNotFound         = errors.New("vat not found")
)

// --- Category operations ---

type CategorySummary struct {
	Category      ArticleCategory
	ArticlesCount int
}

func (s *Service) ListCategories(ctx context.Context) ([]CategorySummary, error) {
	cats, err := s.repo.ListCategories(ctx)
	if err != nil {
		return nil, err
	}
	summaries := make([]CategorySummary, 0, len(cats))
	for i := range cats {
		count, err := s.repo.CountArticlesByCategory(ctx, cats[i].ID)
		if err != nil {
			return nil, err
		}
		summaries = append(summaries, CategorySummary{Category: cats[i], ArticlesCount: count})
	}
	return summaries, nil
}

func (s *Service) GetCategory(ctx context.Context, id int) (ArticleCategory, error) {
	return s.repo.GetCategory(ctx, id)
}

func (s *Service) CreateCategory(ctx context.Context, name string, description *string) (ArticleCategory, error) {
	cat := ArticleCategory{Name: name, Description: description}
	if err := s.repo.CreateCategory(ctx, &cat); err != nil {
		return ArticleCategory{}, err
	}
	return cat, nil
}

func (s *Service) UpdateCategory(ctx context.Context, id int, name *string, description *string) (ArticleCategory, error) {
	cat, err := s.repo.GetCategory(ctx, id)
	if err != nil {
		return ArticleCategory{}, err
	}
	if name != nil {
		cat.Name = *name
	}
	if description != nil {
		cat.Description = description
	}
	if err := s.repo.UpdateCategory(ctx, &cat); err != nil {
		return ArticleCategory{}, err
	}
	return cat, nil
}

func (s *Service) DeleteCategory(ctx context.Context, id int) error {
	return s.repo.DeleteCategory(ctx, id)
}

func (s *Service) CategoryExists(ctx context.Context, id int) (bool, error) {
	return s.repo.CategoryExists(ctx, id)
}

// --- Subcategory operations ---

type SubcategorySummary struct {
	Subcategory   ArticleSubCategory
	ArticlesCount int
}

func (s *Service) ListSubcategories(ctx context.Context) ([]SubcategorySummary, error) {
	subs, err := s.repo.ListSubcategories(ctx)
	if err != nil {
		return nil, err
	}
	items := make([]SubcategorySummary, 0, len(subs))
	for i := range subs {
		count, err := s.repo.CountArticlesBySubcategory(ctx, subs[i].ID)
		if err != nil {
			return nil, err
		}
		items = append(items, SubcategorySummary{Subcategory: subs[i], ArticlesCount: count})
	}
	return items, nil
}

func (s *Service) ListSubcategoriesByCategory(ctx context.Context, categoryID int) ([]SubcategorySummary, error) {
	subs, err := s.repo.ListSubcategoriesByCategory(ctx, categoryID)
	if err != nil {
		return nil, err
	}
	items := make([]SubcategorySummary, 0, len(subs))
	for i := range subs {
		count, err := s.repo.CountArticlesBySubcategory(ctx, subs[i].ID)
		if err != nil {
			return nil, err
		}
		items = append(items, SubcategorySummary{Subcategory: subs[i], ArticlesCount: count})
	}
	return items, nil
}

func (s *Service) GetSubcategory(ctx context.Context, id int) (ArticleSubCategory, error) {
	return s.repo.GetSubcategory(ctx, id)
}

func (s *Service) CreateSubcategory(ctx context.Context, categoryID int, name string, description *string) (ArticleSubCategory, error) {
	sub := ArticleSubCategory{ArticleCategoryID: categoryID, Name: name, Description: description}
	if err := s.repo.CreateSubcategory(ctx, &sub); err != nil {
		return ArticleSubCategory{}, err
	}
	return sub, nil
}

func (s *Service) UpdateSubcategory(ctx context.Context, id int, categoryID *int, name *string, description *string) (ArticleSubCategory, error) {
	sub, err := s.repo.GetSubcategory(ctx, id)
	if err != nil {
		return ArticleSubCategory{}, err
	}
	if categoryID != nil {
		sub.ArticleCategoryID = *categoryID
	}
	if name != nil {
		sub.Name = *name
	}
	if description != nil {
		sub.Description = description
	}
	if err := s.repo.UpdateSubcategory(ctx, &sub); err != nil {
		return ArticleSubCategory{}, err
	}
	return sub, nil
}

func (s *Service) DeleteSubcategory(ctx context.Context, id int) error {
	return s.repo.DeleteSubcategory(ctx, id)
}

func (s *Service) SubcategoryExists(ctx context.Context, id int) (bool, error) {
	return s.repo.SubcategoryExists(ctx, id)
}

// --- Suppliers & VAT ---

func (s *Service) SupplierExists(ctx context.Context, id int) (bool, error) {
	return s.repo.SupplierExists(ctx, id)
}

func (s *Service) SupplierName(ctx context.Context, id int) (*string, error) {
	return s.repo.SupplierName(ctx, id)
}

func (s *Service) VatExists(ctx context.Context, id int) (bool, error) {
	return s.repo.VatExists(ctx, id)
}

// --- Article aggregates ---

type ArticleAdminItem struct {
	Article         Article
	CategoryName    string
	SubcategoryName *string
	SupplierName    *string
	MugVariants     []MugVariant
	ShirtVariants   []ShirtVariant
}

type ArticleDetail struct {
	ArticleAdminItem
	MugDetails      *MugDetails
	ShirtDetails    *ShirtDetails
	CostCalculation *CostCalculation
}

func (s *Service) ListArticles(ctx context.Context, opts ArticleListOptions) ([]ArticleAdminItem, int64, error) {
	articles, total, err := s.repo.ListArticles(ctx, opts)
	if err != nil {
		return nil, 0, err
	}
	items := make([]ArticleAdminItem, 0, len(articles))
	for i := range articles {
		catName, subName, suppName, err := s.articleNames(ctx, &articles[i])
		if err != nil {
			return nil, 0, err
		}
		mugs, err := s.repo.ListMugVariants(ctx, articles[i].ID, false)
		if err != nil {
			return nil, 0, err
		}
		shirts, err := s.repo.ListShirtVariants(ctx, articles[i].ID, false)
		if err != nil {
			return nil, 0, err
		}
		items = append(items, ArticleAdminItem{
			Article:         articles[i],
			CategoryName:    catName,
			SubcategoryName: subName,
			SupplierName:    suppName,
			MugVariants:     mugs,
			ShirtVariants:   shirts,
		})
	}
	return items, total, nil
}

func (s *Service) GetArticleDetail(ctx context.Context, id int) (ArticleDetail, error) {
	art, err := s.repo.GetArticle(ctx, id)
	if err != nil {
		return ArticleDetail{}, err
	}
	catName, subName, suppName, err := s.articleNames(ctx, &art)
	if err != nil {
		return ArticleDetail{}, err
	}
	mugs, err := s.repo.ListMugVariants(ctx, art.ID, false)
	if err != nil {
		return ArticleDetail{}, err
	}
	shirts, err := s.repo.ListShirtVariants(ctx, art.ID, false)
	if err != nil {
		return ArticleDetail{}, err
	}
	detail := ArticleDetail{
		ArticleAdminItem: ArticleAdminItem{
			Article:         art,
			CategoryName:    catName,
			SubcategoryName: subName,
			SupplierName:    suppName,
			MugVariants:     mugs,
			ShirtVariants:   shirts,
		},
	}
	if art.ArticleType == ArticleTypeMug {
		detail.MugDetails, err = s.repo.GetMugDetails(ctx, art.ID)
		if err != nil {
			return ArticleDetail{}, err
		}
	} else if art.ArticleType == ArticleTypeShirt {
		detail.ShirtDetails, err = s.repo.GetShirtDetails(ctx, art.ID)
		if err != nil {
			return ArticleDetail{}, err
		}
	}
	calc, err := s.repo.GetCostCalculation(ctx, art.ID)
	if err != nil {
		return ArticleDetail{}, err
	}
	detail.CostCalculation = calc
	return detail, nil
}

func (s *Service) GetArticle(ctx context.Context, id int) (Article, error) {
	return s.repo.GetArticle(ctx, id)
}

// GetArticleSummary returns basic article metadata used by downstream consumers.
func (s *Service) GetArticleSummary(ctx context.Context, id int) (ArticleResponse, error) {
	art, err := s.repo.GetArticle(ctx, id)
	if err != nil {
		return ArticleResponse{}, err
	}
	catName, subName, suppName, err := s.articleNames(ctx, &art)
	if err != nil {
		return ArticleResponse{}, err
	}
	return toArticleResponse(&art, catName, subName, suppName, nil, nil), nil
}

func (s *Service) CreateArticle(ctx context.Context, art *Article, mugDetails *MugDetails, shirtDetails *ShirtDetails, cost *CostCalculation, mugVariants []MugVariant, shirtVariants []ShirtVariant) (ArticleDetail, error) {
	if err := s.validateArticleReferences(ctx, art.CategoryID, art.SubcategoryID, art.SupplierID); err != nil {
		return ArticleDetail{}, err
	}
	if err := s.repo.CreateArticle(ctx, art); err != nil {
		return ArticleDetail{}, err
	}
	if err := s.applyArticleSpecificDetails(ctx, art, mugDetails, shirtDetails, mugVariants, shirtVariants); err != nil {
		return ArticleDetail{}, err
	}
	if err := s.UpsertCostCalculation(ctx, art.ID, cost); err != nil {
		return ArticleDetail{}, err
	}
	return s.GetArticleDetail(ctx, art.ID)
}

func (s *Service) UpdateArticle(ctx context.Context, art *Article, mugDetails *MugDetails, shirtDetails *ShirtDetails, cost *CostCalculation) (ArticleDetail, error) {
	if err := s.validateArticleReferences(ctx, art.CategoryID, art.SubcategoryID, art.SupplierID); err != nil {
		return ArticleDetail{}, err
	}
	if err := s.repo.UpdateArticle(ctx, art); err != nil {
		return ArticleDetail{}, err
	}
	if err := s.applyArticleSpecificDetails(ctx, art, mugDetails, shirtDetails, nil, nil); err != nil {
		return ArticleDetail{}, err
	}
	if err := s.UpsertCostCalculation(ctx, art.ID, cost); err != nil {
		return ArticleDetail{}, err
	}
	return s.GetArticleDetail(ctx, art.ID)
}

func (s *Service) DeleteArticle(ctx context.Context, id int) error {
	return s.repo.DeleteArticle(ctx, id)
}

func (s *Service) applyArticleSpecificDetails(ctx context.Context, art *Article, mugDetails *MugDetails, shirtDetails *ShirtDetails, mugVariants []MugVariant, shirtVariants []ShirtVariant) error {
	switch art.ArticleType {
	case ArticleTypeMug:
		if mugDetails != nil {
			mugDetails.ArticleID = art.ID
		}
		if err := s.repo.UpsertMugDetails(ctx, mugDetails); err != nil {
			return err
		}
		for i := range mugVariants {
			mugVariants[i].ArticleID = art.ID
			if err := s.repo.CreateMugVariant(ctx, &mugVariants[i]); err != nil {
				return err
			}
		}
	case ArticleTypeShirt:
		if shirtDetails != nil {
			shirtDetails.ArticleID = art.ID
		}
		if err := s.repo.UpsertShirtDetails(ctx, shirtDetails); err != nil {
			return err
		}
		for i := range shirtVariants {
			shirtVariants[i].ArticleID = art.ID
			if err := s.repo.CreateShirtVariant(ctx, &shirtVariants[i]); err != nil {
				return err
			}
		}
	}
	return nil
}

func (s *Service) validateArticleReferences(ctx context.Context, categoryID int, subcategoryID *int, supplierID *int) error {
	exists, err := s.repo.CategoryExists(ctx, categoryID)
	if err != nil {
		return err
	}
	if !exists {
		return fmt.Errorf("%w: %d", ErrCategoryNotFound, categoryID)
	}
	if subcategoryID != nil {
		exists, err = s.repo.SubcategoryExists(ctx, *subcategoryID)
		if err != nil {
			return err
		}
		if !exists {
			return fmt.Errorf("%w: %d", ErrSubcategoryNotFound, *subcategoryID)
		}
	}
	if supplierID != nil {
		exists, err = s.repo.SupplierExists(ctx, *supplierID)
		if err != nil {
			return err
		}
		if !exists {
			return fmt.Errorf("%w: %d", ErrSupplierNotFound, *supplierID)
		}
	}
	return nil
}

// --- Variants ---

func (s *Service) CreateMugVariant(ctx context.Context, variant *MugVariant) (MugVariant, error) {
	if err := s.repo.CreateMugVariant(ctx, variant); err != nil {
		return MugVariant{}, err
	}
	return *variant, nil
}

func (s *Service) UpdateMugVariant(ctx context.Context, variant *MugVariant) (MugVariant, error) {
	if err := s.repo.UpdateMugVariant(ctx, variant); err != nil {
		return MugVariant{}, err
	}
	return *variant, nil
}

func (s *Service) DeleteMugVariant(ctx context.Context, id int) error {
	return s.repo.DeleteMugVariant(ctx, id)
}

func (s *Service) GetMugVariant(ctx context.Context, id int) (MugVariant, error) {
	return s.repo.GetMugVariant(ctx, id)
}

func (s *Service) CreateShirtVariant(ctx context.Context, variant *ShirtVariant) (ShirtVariant, error) {
	if err := s.repo.CreateShirtVariant(ctx, variant); err != nil {
		return ShirtVariant{}, err
	}
	return *variant, nil
}

func (s *Service) UpdateShirtVariant(ctx context.Context, variant *ShirtVariant) (ShirtVariant, error) {
	if err := s.repo.UpdateShirtVariant(ctx, variant); err != nil {
		return ShirtVariant{}, err
	}
	return *variant, nil
}

func (s *Service) DeleteShirtVariant(ctx context.Context, id int) error {
	return s.repo.DeleteShirtVariant(ctx, id)
}

func (s *Service) GetShirtVariant(ctx context.Context, id int) (ShirtVariant, error) {
	return s.repo.GetShirtVariant(ctx, id)
}

func (s *Service) ListMugVariants(ctx context.Context, articleID int, onlyActive bool) ([]MugVariant, error) {
	return s.repo.ListMugVariants(ctx, articleID, onlyActive)
}

func (s *Service) ListShirtVariants(ctx context.Context, articleID int) ([]ShirtVariant, error) {
	return s.repo.ListShirtVariants(ctx, articleID, false)
}

func (s *Service) ListMugArticles(ctx context.Context, onlyActive bool, excludeID *int) ([]Article, error) {
	return s.repo.ListMugArticles(ctx, onlyActive, excludeID)
}

// --- Details & cost ---

func (s *Service) GetMugDetails(ctx context.Context, articleID int) (*MugDetails, error) {
	return s.repo.GetMugDetails(ctx, articleID)
}

func (s *Service) UpsertMugDetails(ctx context.Context, details *MugDetails) error {
	return s.repo.UpsertMugDetails(ctx, details)
}

func (s *Service) GetShirtDetails(ctx context.Context, articleID int) (*ShirtDetails, error) {
	return s.repo.GetShirtDetails(ctx, articleID)
}

func (s *Service) UpsertShirtDetails(ctx context.Context, details *ShirtDetails) error {
	return s.repo.UpsertShirtDetails(ctx, details)
}

func (s *Service) GetCostCalculation(ctx context.Context, articleID int) (*CostCalculation, error) {
	return s.repo.GetCostCalculation(ctx, articleID)
}

func (s *Service) GetCostCalculationByID(ctx context.Context, id int) (*CostCalculation, error) {
	return s.repo.GetCostCalculationByID(ctx, id)
}

func (s *Service) UpsertCostCalculation(ctx context.Context, articleID int, calc *CostCalculation) error {
	if calc == nil {
		return nil
	}
	if calc.PurchaseVatRateID != nil {
		exists, err := s.repo.VatExists(ctx, *calc.PurchaseVatRateID)
		if err != nil {
			return err
		}
		if !exists {
			return fmt.Errorf("purchase %w: %d", ErrVatNotFound, *calc.PurchaseVatRateID)
		}
	}
	if calc.SalesVatRateID != nil {
		exists, err := s.repo.VatExists(ctx, *calc.SalesVatRateID)
		if err != nil {
			return err
		}
		if !exists {
			return fmt.Errorf("sales %w: %d", ErrVatNotFound, *calc.SalesVatRateID)
		}
	}
	return s.repo.UpsertCostCalculation(ctx, articleID, calc)
}

// --- Helper methods ---

func (s *Service) articleNames(ctx context.Context, a *Article) (catName string, subName *string, suppName *string, err error) {
	if cat, err := s.repo.GetCategory(ctx, a.CategoryID); err == nil {
		catName = cat.Name
	} else if !errors.Is(err, gorm.ErrRecordNotFound) {
		return "", nil, nil, err
	}
	if a.SubcategoryID != nil {
		sub, err := s.repo.GetSubcategory(ctx, *a.SubcategoryID)
		if err == nil {
			subName = &sub.Name
		} else if !errors.Is(err, gorm.ErrRecordNotFound) {
			return "", nil, nil, err
		}
	}
	if a.SupplierID != nil {
		supp, err := s.repo.SupplierName(ctx, *a.SupplierID)
		if err == nil {
			suppName = supp
		} else if !errors.Is(err, gorm.ErrRecordNotFound) {
			return "", nil, nil, err
		}
	}
	return catName, subName, suppName, nil
}

// defaultMugVariant returns the default variant if present, else the first.
func defaultMugVariant(vs []MugVariant) *MugVariant {
	if len(vs) == 0 {
		return nil
	}
	for i := range vs {
		if vs[i].IsDefault {
			return &vs[i]
		}
	}
	return &vs[0]
}
