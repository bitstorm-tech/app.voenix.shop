package order

import (
	"context"

	"gorm.io/gorm"

	"voenix/backend/internal/article"
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

func (s *stubArticleService) GetMugDetails(ctx context.Context, articleID int) (*article.MugDetails, error) {
	var md article.MugDetails
	err := s.db.WithContext(ctx).First(&md, "article_id = ?", articleID).Error
	if err != nil {
		if err == gorm.ErrRecordNotFound {
			return nil, nil
		}
		return nil, err
	}
	return &md, nil
}
