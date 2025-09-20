package cart

import (
	"context"
	"path/filepath"

	"voenix/backend/internal/article"
	img "voenix/backend/internal/image"
)

// buildCartResponse converts a Cart and related lookup data into an API response.
func buildCartResponse(
	ctx context.Context,
	articleSvc ArticleService,
	c *Cart,
	generatedImageFilenames map[int]string,
	promptTitles map[int]string,
) (*CartResponse, error) {
	items := make([]CartItemResponse, 0, len(c.Items))
	totalCount := 0
	totalPrice := 0
	for i := range c.Items {
		ci := c.Items[i]
		art, err := loadArticleResponse(ctx, articleSvc, ci.ArticleID)
		if err != nil {
			return nil, err
		}
		mv, _ := loadMugVariantResponse(ctx, articleSvc, ci.VariantID)
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
		item := CartItemResponse{
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
	dto := &CartResponse{
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

// loadMugVariantResponse builds a simplified variant response for cart.
func loadMugVariantResponse(ctx context.Context, articleSvc ArticleService, id int) (*MugVariantResponse, error) {
	v, err := articleSvc.GetMugVariant(ctx, id)
	if err != nil {
		return nil, err
	}
	return BuildMugVariantResponse(&v), nil
}

// BuildMugVariantResponse converts an article mug variant into the response schema shared with the order API.
func BuildMugVariantResponse(variant *article.MugVariant) *MugVariantResponse {
	if variant == nil {
		return nil
	}
	url := publicMugVariantExampleURL(variant.ExampleImageFilename)
	return &MugVariantResponse{
		ID:                    variant.ID,
		ArticleID:             variant.ArticleID,
		ColorCode:             variant.OutsideColorCode,
		ExampleImageURL:       strPtrOrNil(url),
		SupplierArticleNumber: variant.ArticleVariantNumber,
		IsDefault:             variant.IsDefault,
		ExampleImageFilename:  variant.ExampleImageFilename,
	}
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
