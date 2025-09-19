package article

import "context"

// ArticleListOptions enumerates filters for admin article listing.
type ArticleListOptions struct {
	Page          int
	Size          int
	ArticleType   string
	CategoryID    *int
	SubcategoryID *int
	Active        *bool
	Search        string
}

// Repository defines storage operations required by the article service.
type Repository interface {
	// Categories
	ListCategories(ctx context.Context) ([]ArticleCategory, error)
	GetCategory(ctx context.Context, id int) (ArticleCategory, error)
	CreateCategory(ctx context.Context, cat *ArticleCategory) error
	UpdateCategory(ctx context.Context, cat *ArticleCategory) error
	DeleteCategory(ctx context.Context, id int) error
	CountArticlesByCategory(ctx context.Context, categoryID int) (int, error)
	CategoryExists(ctx context.Context, id int) (bool, error)

	// Subcategories
	ListSubcategories(ctx context.Context) ([]ArticleSubCategory, error)
	ListSubcategoriesByCategory(ctx context.Context, categoryID int) ([]ArticleSubCategory, error)
	GetSubcategory(ctx context.Context, id int) (ArticleSubCategory, error)
	CreateSubcategory(ctx context.Context, sub *ArticleSubCategory) error
	UpdateSubcategory(ctx context.Context, sub *ArticleSubCategory) error
	DeleteSubcategory(ctx context.Context, id int) error
	CountArticlesBySubcategory(ctx context.Context, subcategoryID int) (int, error)
	SubcategoryExists(ctx context.Context, id int) (bool, error)

	// Suppliers & VAT
	SupplierExists(ctx context.Context, id int) (bool, error)
	SupplierName(ctx context.Context, id int) (*string, error)
	VatExists(ctx context.Context, id int) (bool, error)

	// Articles
	ListArticles(ctx context.Context, opts ArticleListOptions) ([]Article, int64, error)
	GetArticle(ctx context.Context, id int) (Article, error)
	CreateArticle(ctx context.Context, art *Article) error
	UpdateArticle(ctx context.Context, art *Article) error
	DeleteArticle(ctx context.Context, id int) error

	// Variants - mugs
	ListMugVariants(ctx context.Context, articleID int, onlyActive bool) ([]MugVariant, error)
	GetMugVariant(ctx context.Context, id int) (MugVariant, error)
	CreateMugVariant(ctx context.Context, variant *MugVariant) error
	UpdateMugVariant(ctx context.Context, variant *MugVariant) error
	DeleteMugVariant(ctx context.Context, id int) error

	// Variants - shirts
	ListShirtVariants(ctx context.Context, articleID int, onlyActive bool) ([]ShirtVariant, error)
	GetShirtVariant(ctx context.Context, id int) (ShirtVariant, error)
	CreateShirtVariant(ctx context.Context, variant *ShirtVariant) error
	UpdateShirtVariant(ctx context.Context, variant *ShirtVariant) error
	DeleteShirtVariant(ctx context.Context, id int) error

	// Details & pricing
	GetMugDetails(ctx context.Context, articleID int) (*MugDetails, error)
	UpsertMugDetails(ctx context.Context, details *MugDetails) error
	DeleteMugDetails(ctx context.Context, articleID int) error

	GetShirtDetails(ctx context.Context, articleID int) (*ShirtDetails, error)
	UpsertShirtDetails(ctx context.Context, details *ShirtDetails) error
	DeleteShirtDetails(ctx context.Context, articleID int) error

	GetCostCalculation(ctx context.Context, articleID int) (*Price, error)
	GetCostCalculationByID(ctx context.Context, id int) (*Price, error)
	UpsertCostCalculation(ctx context.Context, articleID int, calc *Price) error
	DeleteCostCalculation(ctx context.Context, articleID int) error

	// Listings & helpers
	ListMugArticles(ctx context.Context, onlyActive bool, excludeID *int) ([]Article, error)
}
