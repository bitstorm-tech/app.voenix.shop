package order

import (
	"context"
	"strconv"

	"github.com/google/uuid"

	"voenix/backend/internal/article"
	"voenix/backend/internal/pdf"
)

// buildOrderPdfData constructs the pdf.OrderPdfData from Order and related records.
func buildOrderPdfData(ctx context.Context, articleSvc ArticleService, repo Repository, o *Order) (pdf.OrderPdfData, error) {
	out := pdf.OrderPdfData{
		ID:          strconv.FormatInt(o.ID, 10),
		OrderNumber: &o.OrderNumber,
		UserID:      o.UserID,
		Items:       make([]pdf.OrderItemPdfData, 0, len(o.Items)),
	}

	// Preload caches for articles/variants/mug details to avoid N+1 as much as possible
	artIDs := make(map[int]struct{})
	varIDs := make(map[int]struct{})
	for i := range o.Items {
		artIDs[o.Items[i].ArticleID] = struct{}{}
		varIDs[o.Items[i].VariantID] = struct{}{}
	}

	articles := make(map[int]article.Article)
	mugDetails := make(map[int]article.MugDetails)
	variants := make(map[int]article.MugVariant)

	for id := range artIDs {
		a, err := articleSvc.GetArticle(ctx, id)
		if err != nil {
			return pdf.OrderPdfData{}, err
		}
		articles[id] = a
		md, err := articleSvc.GetMugDetails(ctx, id)
		if err != nil {
			return pdf.OrderPdfData{}, err
		}
		if md != nil {
			mugDetails[id] = *md
		}
	}
	for id := range varIDs {
		v, err := articleSvc.GetMugVariant(ctx, id)
		if err != nil {
			return pdf.OrderPdfData{}, err
		}
		variants[id] = v
	}

	generatedIDs := make([]int, 0, len(o.Items))
	for i := range o.Items {
		if o.Items[i].GeneratedImageID != nil {
			generatedIDs = append(generatedIDs, *o.Items[i].GeneratedImageID)
		}
	}
	generatedFilenames, err := repo.FetchGeneratedImageFilenames(ctx, generatedIDs)
	if err != nil {
		return pdf.OrderPdfData{}, err
	}

	for i := range o.Items {
		it := o.Items[i]
		a := articles[it.ArticleID]
		v := variants[it.VariantID]
		md, hasMD := mugDetails[it.ArticleID]

		var mdPtr *pdf.MugDetailsPdfData
		if hasMD {
			mdPtr = &pdf.MugDetailsPdfData{
				PrintTemplateWidthMM:         md.PrintTemplateWidthMm,
				PrintTemplateHeightMM:        md.PrintTemplateHeightMm,
				DocumentFormatWidthMM:        md.DocumentFormatWidthMm,
				DocumentFormatHeightMM:       md.DocumentFormatHeightMm,
				DocumentFormatMarginBottomMM: md.DocumentFormatMarginBottomMm,
			}
		}

		var variantName *string
		if v.Name != "" {
			variantName = &v.Name
		}

		var genFilename *string
		if it.GeneratedImageID != nil {
			if fn, ok := generatedFilenames[*it.GeneratedImageID]; ok && fn != "" {
				genFilename = &fn
			}
		}

		out.Items = append(out.Items, pdf.OrderItemPdfData{
			ID:                     uuid.New().String(),
			Quantity:               it.Quantity,
			GeneratedImageFilename: genFilename,
			Article: pdf.ArticlePdfData{
				ID:                    it.ArticleID,
				MugDetails:            mdPtr,
				SupplierArticleName:   a.SupplierArticleName,
				SupplierArticleNumber: a.SupplierArticleNumber,
			},
			VariantID:   it.VariantID,
			VariantName: variantName,
		})
	}
	return out, nil
}
