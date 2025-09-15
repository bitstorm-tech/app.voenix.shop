package order

import (
	"gorm.io/gorm"

	"voenix/backend/internal/article"
	"voenix/backend/internal/pdf"
)

// buildOrderPdfData constructs the pdf.OrderPdfData from Order and related records.
func buildOrderPdfData(db *gorm.DB, o *Order) (pdf.OrderPdfData, error) {
	out := pdf.OrderPdfData{
		ID:          o.ID,
		OrderNumber: &o.OrderNumber,
		UserID:      o.UserID,
		Items:       make([]pdf.OrderItemPdfData, 0, len(o.Items)),
	}

	// Preload caches for articles/variants/mug details to avoid N+1 as much as possible
	// Collect ids
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
		var a article.Article
		if err := db.First(&a, "id = ?", id).Error; err == nil {
			articles[id] = a
			// fetch mug details (optional)
			var md article.MugDetails
			if err := db.First(&md, "article_id = ?", id).Error; err == nil {
				mugDetails[id] = md
			}
		}
	}
	for id := range varIDs {
		var v article.MugVariant
		_ = db.First(&v, "id = ?", id).Error
		if v.ID != 0 {
			variants[id] = v
		}
	}

	// Preload generated image filenames for any items with GeneratedImageID
	genIDToFilename := map[int]string{}
	{
		ids := make([]int, 0, len(o.Items))
		for i := range o.Items {
			if o.Items[i].GeneratedImageID != nil {
				ids = append(ids, *o.Items[i].GeneratedImageID)
			}
		}
		if len(ids) > 0 {
			type row struct {
				ID       int
				Filename string
			}
			var rows []row
			if err := db.Table("generated_images").Select("id, filename").Where("id IN ?", ids).Scan(&rows).Error; err == nil {
				for _, r := range rows {
					genIDToFilename[r.ID] = r.Filename
				}
			}
		}
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

		// Generated image filename (optional)
		var genFilename *string
		if it.GeneratedImageID != nil {
			if fn, ok := genIDToFilename[*it.GeneratedImageID]; ok && fn != "" {
				genFilename = &fn
			}
		}

		out.Items = append(out.Items, pdf.OrderItemPdfData{
			ID:                     newUUIDv4(),
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
