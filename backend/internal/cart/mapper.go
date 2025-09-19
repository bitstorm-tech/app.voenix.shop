package cart

import (
	"context"
	"path/filepath"

	"voenix/backend/internal/article"
	img "voenix/backend/internal/image"
)

// assembleCartDto converts a Cart+Items into a response DTO.
func assembleCartDto(
	ctx context.Context,
	articleSvc ArticleService,
	c *Cart,
	generatedImageFilenames map[int]string,
	promptTitles map[int]string,
) (*CartDto, error) {
	items := make([]CartItemDto, 0, len(c.Items))
	totalCount := 0
	totalPrice := 0
	for i := range c.Items {
		ci := c.Items[i]
		art, err := loadArticleResponse(ctx, articleSvc, ci.ArticleID)
		if err != nil {
			return nil, err
		}
		mv, _ := loadMugVariantDto(ctx, articleSvc, ci.VariantID)
		cd := parseJSONMap(ci.CustomData)
		var genFilename *string
		if ci.GeneratedImageID != nil && generatedImageFilenames != nil {
			if fn, ok := generatedImageFilenames[*ci.GeneratedImageID]; ok && fn != "" {
				genFilename = &fn
			}
		}
		articlePriceAtTime := ci.PriceAtTime
		promptPriceAtTime := ci.PromptPriceAtTime
		articleOriginalPrice := ci.OriginalPrice
		promptOriginalPrice := ci.PromptOriginalPrice
		totalPerItem := (articlePriceAtTime + promptPriceAtTime) * ci.Quantity
		hasArticlePriceChanged := articlePriceAtTime != articleOriginalPrice
		hasPromptPriceChanged := promptPriceAtTime != promptOriginalPrice
		hasPriceChanged := hasArticlePriceChanged || hasPromptPriceChanged
		var promptTitle *string
		if ci.PromptID != nil && promptTitles != nil {
			if title, ok := promptTitles[*ci.PromptID]; ok && title != "" {
				t := title
				promptTitle = &t
			}
		}
		item := CartItemDto{
			ID:                     ci.ID,
			Article:                art,
			Variant:                mv,
			Quantity:               ci.Quantity,
			PriceAtTime:            articlePriceAtTime,
			OriginalPrice:          articleOriginalPrice,
			ArticlePriceAtTime:     articlePriceAtTime,
			PromptPriceAtTime:      promptPriceAtTime,
			ArticleOriginalPrice:   articleOriginalPrice,
			PromptOriginalPrice:    promptOriginalPrice,
			HasPriceChanged:        hasPriceChanged,
			HasPromptPriceChanged:  hasPromptPriceChanged,
			TotalPrice:             totalPerItem,
			CustomData:             cd,
			GeneratedImageID:       ci.GeneratedImageID,
			GeneratedImageFilename: genFilename,
			PromptID:               ci.PromptID,
			PromptTitle:            promptTitle,
			Position:               ci.Position,
			CreatedAt:              ci.CreatedAt,
			UpdatedAt:              ci.UpdatedAt,
		}
		items = append(items, item)
		totalCount += ci.Quantity
		totalPrice += item.TotalPrice
	}
	dto := &CartDto{
		ID:             c.ID,
		UserID:         c.UserID,
		Status:         string(c.Status),
		Version:        c.Version,
		ExpiresAt:      c.ExpiresAt,
		Items:          items,
		TotalItemCount: totalCount,
		TotalPrice:     totalPrice,
		IsEmpty:        len(items) == 0,
		CreatedAt:      c.CreatedAt,
		UpdatedAt:      c.UpdatedAt,
	}
	return dto, nil
}

// loadArticleResponse produces article.ArticleResponse for the given ID using the article service.
func loadArticleResponse(ctx context.Context, articleSvc ArticleService, id int) (article.ArticleResponse, error) {
	resp, err := articleSvc.GetArticleSummary(ctx, id)
	if err != nil {
		return article.ArticleResponse{}, err
	}
	return resp, nil
}

// loadMugVariantDto builds a simplified variant DTO for cart.
func loadMugVariantDto(ctx context.Context, articleSvc ArticleService, id int) (*MugVariantDto, error) {
	v, err := articleSvc.GetMugVariant(ctx, id)
	if err != nil {
		return nil, err
	}
	url := publicMugVariantExampleURL(v.ExampleImageFilename)
	return &MugVariantDto{
		ID:                    v.ID,
		ArticleID:             v.ArticleID,
		ColorCode:             v.OutsideColorCode,
		ExampleImageURL:       strPtrOrNil(url),
		SupplierArticleNumber: v.ArticleVariantNumber,
		IsDefault:             v.IsDefault,
		ExampleImageFilename:  v.ExampleImageFilename,
	}, nil
}

func strPtrOrNil(s string) *string {
	if s == "" {
		return nil
	}
	return &s
}

func publicMugVariantExampleURL(filename *string) string {
	if filename == nil || *filename == "" {
		return ""
	}
	if loc, err := img.NewStorageLocations(); err == nil {
		dir := loc.MugVariantExample()
		if rel, rerr := filepath.Rel(loc.Root, dir); rerr == nil {
			relURL := filepath.ToSlash(rel)
			return "/" + relURL + "/" + filepath.Base(*filename)
		}
	}
	return "/public/images/articles/mugs/variant-example-images/" + filepath.Base(*filename)
}
