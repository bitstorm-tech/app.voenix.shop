package cart

import (
	"context"

	"voenix/backend/internal/article"
)

// ArticleService defines the subset of article service capabilities needed by cart flows.
type ArticleService interface {
	GetArticleSummary(ctx context.Context, id int) (article.ArticleResponse, error)
	GetArticle(ctx context.Context, id int) (article.Article, error)
	GetMugVariant(ctx context.Context, id int) (article.MugVariant, error)
	GetCostCalculation(ctx context.Context, articleID int) (*article.Price, error)
	GetCostCalculationByID(ctx context.Context, id int) (*article.Price, error)
}
