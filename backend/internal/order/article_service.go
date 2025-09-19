package order

import (
	"context"

	"voenix/backend/internal/article"
)

// ArticleService defines article data access needed by the order package.
type ArticleService interface {
	GetArticleSummary(ctx context.Context, id int) (article.ArticleResponse, error)
	GetArticle(ctx context.Context, id int) (article.Article, error)
	GetMugVariant(ctx context.Context, id int) (article.MugVariant, error)
	GetMugDetails(ctx context.Context, articleID int) (*article.MugDetails, error)
}
