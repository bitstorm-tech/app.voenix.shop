package cart

import (
	"path/filepath"
	"time"

	"gorm.io/gorm"

	"voenix/backend-go/internal/article"
	img "voenix/backend-go/internal/image"
	"voenix/backend-go/internal/supplier"
)

// assembleCartDto converts a Cart+Items into a response DTO.
func assembleCartDto(db *gorm.DB, c *Cart) (*CartDto, error) {
	items := make([]CartItemDto, 0, len(c.Items))
	totalCount := 0
	totalPrice := 0
	// Preload generated image filenames for any items with GeneratedImageID
	genIDToFilename := map[int]string{}
	{
		ids := make([]int, 0, len(c.Items))
		for i := range c.Items {
			if c.Items[i].GeneratedImageID != nil {
				ids = append(ids, *c.Items[i].GeneratedImageID)
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
	for i := range c.Items {
		ci := c.Items[i]
		art, err := loadArticleRead(db, ci.ArticleID)
		if err != nil {
			return nil, err
		}
		mv, _ := loadMugVariantDto(db, ci.VariantID)
		cd := parseJSONMap(ci.CustomData)
		var genFilename *string
		if ci.GeneratedImageID != nil {
			if fn, ok := genIDToFilename[*ci.GeneratedImageID]; ok && fn != "" {
				genFilename = &fn
			}
		}
		item := CartItemDto{
			ID:                     ci.ID,
			Article:                art,
			Variant:                mv,
			Quantity:               ci.Quantity,
			PriceAtTime:            ci.PriceAtTime,
			OriginalPrice:          ci.OriginalPrice,
			HasPriceChanged:        ci.PriceAtTime != ci.OriginalPrice,
			TotalPrice:             ci.PriceAtTime * ci.Quantity,
			CustomData:             cd,
			GeneratedImageID:       ci.GeneratedImageID,
			GeneratedImageFilename: genFilename,
			PromptID:               ci.PromptID,
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
		Status:         c.Status,
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

// loadArticleRead produces article.ArticleRead for the given ID.
func loadArticleRead(db *gorm.DB, id int) (article.ArticleRead, error) {
	var a article.Article
	if err := db.First(&a, "id = ?", id).Error; err != nil {
		return article.ArticleRead{}, err
	}
	// fetch names
	var catName string
	if a.CategoryID != 0 {
		var cat article.ArticleCategory
		if err := db.First(&cat, "id = ?", a.CategoryID).Error; err == nil {
			catName = cat.Name
		}
	}
	var subName *string
	if a.SubcategoryID != nil {
		var sub article.ArticleSubCategory
		if err := db.First(&sub, "id = ?", *a.SubcategoryID).Error; err == nil {
			subName = &sub.Name
		}
	}
	var suppName *string
	if a.SupplierID != nil {
		var s supplier.Supplier
		if err := db.First(&s, "id = ?", *a.SupplierID).Error; err == nil {
			suppName = s.Name
		}
	}
	out := article.ArticleRead{
		ID:                    a.ID,
		Name:                  a.Name,
		DescriptionShort:      a.DescriptionShort,
		DescriptionLong:       a.DescriptionLong,
		Active:                a.Active,
		ArticleType:           a.ArticleType,
		CategoryID:            a.CategoryID,
		CategoryName:          catName,
		SubcategoryID:         a.SubcategoryID,
		SubcategoryName:       subName,
		SupplierID:            a.SupplierID,
		SupplierName:          suppName,
		SupplierArticleName:   a.SupplierArticleName,
		SupplierArticleNumber: a.SupplierArticleNumber,
		MugDetails:            nil,
		ShirtDetails:          nil,
		CostCalculation:       nil,
		CreatedAt:             timePtr(a.CreatedAt),
		UpdatedAt:             timePtr(a.UpdatedAt),
	}
	return out, nil
}

// loadMugVariantDto builds a simplified variant DTO for cart.
func loadMugVariantDto(db *gorm.DB, id int) (*MugVariantDto, error) {
	var v article.MugVariant
	if err := db.First(&v, "id = ?", id).Error; err != nil {
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

// helpers
func timePtr(t time.Time) *time.Time { return &t }

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
