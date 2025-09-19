package postgres

import (
	"context"
	"errors"
	"strings"

	"gorm.io/gorm"

	"voenix/backend/internal/article"
	"voenix/backend/internal/supplier"
	"voenix/backend/internal/vat"
)

type Repository struct{ db *gorm.DB }

func NewRepository(db *gorm.DB) *Repository { return &Repository{db: db} }

var _ article.Repository = (*Repository)(nil)

// --- Category operations ---

func (r *Repository) ListCategories(ctx context.Context) ([]article.ArticleCategory, error) {
	var rows []articleCategoryRow
	if err := r.db.WithContext(ctx).Order("id desc").Find(&rows).Error; err != nil {
		return nil, err
	}
	out := make([]article.ArticleCategory, 0, len(rows))
	for i := range rows {
		out = append(out, toArticleCategory(&rows[i]))
	}
	return out, nil
}

func (r *Repository) GetCategory(ctx context.Context, id int) (article.ArticleCategory, error) {
	var row articleCategoryRow
	if err := r.db.WithContext(ctx).First(&row, "id = ?", id).Error; err != nil {
		return article.ArticleCategory{}, err
	}
	return toArticleCategory(&row), nil
}

func (r *Repository) CreateCategory(ctx context.Context, cat *article.ArticleCategory) error {
	row := fromArticleCategory(cat)
	if err := r.db.WithContext(ctx).Create(row).Error; err != nil {
		return err
	}
	cat.ID = row.ID
	cat.CreatedAt = row.CreatedAt
	cat.UpdatedAt = row.UpdatedAt
	return nil
}

func (r *Repository) UpdateCategory(ctx context.Context, cat *article.ArticleCategory) error {
	row := fromArticleCategory(cat)
	if err := r.db.WithContext(ctx).Save(row).Error; err != nil {
		return err
	}
	cat.CreatedAt = row.CreatedAt
	cat.UpdatedAt = row.UpdatedAt
	return nil
}

func (r *Repository) DeleteCategory(ctx context.Context, id int) error {
	return r.db.WithContext(ctx).Delete(&articleCategoryRow{}, id).Error
}

func (r *Repository) CountArticlesByCategory(ctx context.Context, categoryID int) (int, error) {
	var cnt int64
	if err := r.db.WithContext(ctx).Model(&articleRow{}).Where("category_id = ?", categoryID).Count(&cnt).Error; err != nil {
		return 0, err
	}
	return int(cnt), nil
}

func (r *Repository) CategoryExists(ctx context.Context, id int) (bool, error) {
	var cnt int64
	if err := r.db.WithContext(ctx).Model(&articleCategoryRow{}).Where("id = ?", id).Count(&cnt).Error; err != nil {
		return false, err
	}
	return cnt > 0, nil
}

// --- Subcategory operations ---

func (r *Repository) ListSubcategories(ctx context.Context) ([]article.ArticleSubCategory, error) {
	var rows []articleSubCategoryRow
	if err := r.db.WithContext(ctx).Order("id desc").Find(&rows).Error; err != nil {
		return nil, err
	}
	out := make([]article.ArticleSubCategory, 0, len(rows))
	for i := range rows {
		out = append(out, toArticleSubCategory(&rows[i]))
	}
	return out, nil
}

func (r *Repository) ListSubcategoriesByCategory(ctx context.Context, categoryID int) ([]article.ArticleSubCategory, error) {
	var rows []articleSubCategoryRow
	if err := r.db.WithContext(ctx).Where("article_category_id = ?", categoryID).Order("id desc").Find(&rows).Error; err != nil {
		return nil, err
	}
	out := make([]article.ArticleSubCategory, 0, len(rows))
	for i := range rows {
		out = append(out, toArticleSubCategory(&rows[i]))
	}
	return out, nil
}

func (r *Repository) GetSubcategory(ctx context.Context, id int) (article.ArticleSubCategory, error) {
	var row articleSubCategoryRow
	if err := r.db.WithContext(ctx).First(&row, "id = ?", id).Error; err != nil {
		return article.ArticleSubCategory{}, err
	}
	return toArticleSubCategory(&row), nil
}

func (r *Repository) CreateSubcategory(ctx context.Context, sub *article.ArticleSubCategory) error {
	row := fromArticleSubCategory(sub)
	if err := r.db.WithContext(ctx).Create(row).Error; err != nil {
		return err
	}
	sub.ID = row.ID
	sub.CreatedAt = row.CreatedAt
	sub.UpdatedAt = row.UpdatedAt
	return nil
}

func (r *Repository) UpdateSubcategory(ctx context.Context, sub *article.ArticleSubCategory) error {
	row := fromArticleSubCategory(sub)
	if err := r.db.WithContext(ctx).Save(row).Error; err != nil {
		return err
	}
	sub.CreatedAt = row.CreatedAt
	sub.UpdatedAt = row.UpdatedAt
	return nil
}

func (r *Repository) DeleteSubcategory(ctx context.Context, id int) error {
	return r.db.WithContext(ctx).Delete(&articleSubCategoryRow{}, id).Error
}

func (r *Repository) CountArticlesBySubcategory(ctx context.Context, subcategoryID int) (int, error) {
	var cnt int64
	if err := r.db.WithContext(ctx).Model(&articleRow{}).Where("subcategory_id = ?", subcategoryID).Count(&cnt).Error; err != nil {
		return 0, err
	}
	return int(cnt), nil
}

func (r *Repository) SubcategoryExists(ctx context.Context, id int) (bool, error) {
	var cnt int64
	if err := r.db.WithContext(ctx).Model(&articleSubCategoryRow{}).Where("id = ?", id).Count(&cnt).Error; err != nil {
		return false, err
	}
	return cnt > 0, nil
}

// --- Supplier & VAT ---

func (r *Repository) SupplierExists(ctx context.Context, id int) (bool, error) {
	var cnt int64
	if err := r.db.WithContext(ctx).Model(&supplier.Supplier{}).Where("id = ?", id).Count(&cnt).Error; err != nil {
		return false, err
	}
	return cnt > 0, nil
}

func (r *Repository) SupplierName(ctx context.Context, id int) (*string, error) {
	var s supplier.Supplier
	if err := r.db.WithContext(ctx).Select("name").First(&s, "id = ?", id).Error; err != nil {
		return nil, err
	}
	return s.Name, nil
}

func (r *Repository) VatExists(ctx context.Context, id int) (bool, error) {
	var cnt int64
	if err := r.db.WithContext(ctx).Model(&vat.ValueAddedTax{}).Where("id = ?", id).Count(&cnt).Error; err != nil {
		return false, err
	}
	return cnt > 0, nil
}

// --- Article operations ---

func (r *Repository) ListArticles(ctx context.Context, opts article.ArticleListOptions) ([]article.Article, int64, error) {
	tx := r.db.WithContext(ctx).Model(&articleRow{})
	if strings.TrimSpace(opts.ArticleType) != "" {
		tx = tx.Where("article_type = ?", opts.ArticleType)
	}
	if opts.CategoryID != nil {
		tx = tx.Where("category_id = ?", *opts.CategoryID)
	}
	if opts.SubcategoryID != nil {
		tx = tx.Where("subcategory_id = ?", *opts.SubcategoryID)
	}
	if opts.Active != nil {
		tx = tx.Where("active = ?", *opts.Active)
	}
	if search := strings.TrimSpace(opts.Search); search != "" {
		like := "%" + strings.ToLower(search) + "%"
		tx = tx.Where("LOWER(name) LIKE ? OR LOWER(description_short) LIKE ?", like, like)
	}
	var total int64
	if err := tx.Count(&total).Error; err != nil {
		return nil, 0, err
	}
	page := opts.Page
	if page < 0 {
		page = 0
	}
	size := opts.Size
	if size <= 0 {
		size = 50
	}
	var rows []articleRow
	if err := tx.Order("id desc").Limit(size).Offset(page * size).Find(&rows).Error; err != nil {
		return nil, 0, err
	}
	out := make([]article.Article, 0, len(rows))
	for i := range rows {
		out = append(out, toArticle(&rows[i]))
	}
	return out, total, nil
}

func (r *Repository) GetArticle(ctx context.Context, id int) (article.Article, error) {
	var row articleRow
	if err := r.db.WithContext(ctx).First(&row, "id = ?", id).Error; err != nil {
		return article.Article{}, err
	}
	return toArticle(&row), nil
}

func (r *Repository) CreateArticle(ctx context.Context, art *article.Article) error {
	row := fromArticle(art)
	if err := r.db.WithContext(ctx).Create(row).Error; err != nil {
		return err
	}
	art.ID = row.ID
	art.CreatedAt = row.CreatedAt
	art.UpdatedAt = row.UpdatedAt
	return nil
}

func (r *Repository) UpdateArticle(ctx context.Context, art *article.Article) error {
	row := fromArticle(art)
	if err := r.db.WithContext(ctx).Save(row).Error; err != nil {
		return err
	}
	art.CreatedAt = row.CreatedAt
	art.UpdatedAt = row.UpdatedAt
	return nil
}

func (r *Repository) DeleteArticle(ctx context.Context, id int) error {
	tx := r.db.WithContext(ctx)
	if err := tx.Delete(&mugVariantRow{}, "article_id = ?", id).Error; err != nil {
		return err
	}
	if err := tx.Delete(&shirtVariantRow{}, "article_id = ?", id).Error; err != nil {
		return err
	}
	if err := tx.Delete(&mugDetailsRow{}, "article_id = ?", id).Error; err != nil {
		return err
	}
	if err := tx.Delete(&shirtDetailsRow{}, "article_id = ?", id).Error; err != nil {
		return err
	}
	if err := tx.Delete(&costCalculationRow{}, "article_id = ?", id).Error; err != nil {
		return err
	}
	return tx.Delete(&articleRow{}, id).Error
}

// --- Mug variants ---

func (r *Repository) ListMugVariants(ctx context.Context, articleID int, onlyActive bool) ([]article.MugVariant, error) {
	tx := r.db.WithContext(ctx).Where("article_id = ?", articleID)
	if onlyActive {
		tx = tx.Where("active = ?", true)
	}
	var rows []mugVariantRow
	if err := tx.Order("id asc").Find(&rows).Error; err != nil {
		return nil, err
	}
	out := make([]article.MugVariant, 0, len(rows))
	for i := range rows {
		out = append(out, toMugVariant(&rows[i]))
	}
	return out, nil
}

func (r *Repository) GetMugVariant(ctx context.Context, id int) (article.MugVariant, error) {
	var row mugVariantRow
	if err := r.db.WithContext(ctx).First(&row, "id = ?", id).Error; err != nil {
		return article.MugVariant{}, err
	}
	return toMugVariant(&row), nil
}

func (r *Repository) CreateMugVariant(ctx context.Context, variant *article.MugVariant) error {
	row := fromMugVariant(variant)
	if err := r.db.WithContext(ctx).Create(row).Error; err != nil {
		return err
	}
	variant.ID = row.ID
	variant.CreatedAt = row.CreatedAt
	variant.UpdatedAt = row.UpdatedAt
	return nil
}

func (r *Repository) UpdateMugVariant(ctx context.Context, variant *article.MugVariant) error {
	row := fromMugVariant(variant)
	if err := r.db.WithContext(ctx).Save(row).Error; err != nil {
		return err
	}
	variant.CreatedAt = row.CreatedAt
	variant.UpdatedAt = row.UpdatedAt
	return nil
}

func (r *Repository) DeleteMugVariant(ctx context.Context, id int) error {
	return r.db.WithContext(ctx).Delete(&mugVariantRow{}, id).Error
}

// --- Shirt variants ---

func (r *Repository) ListShirtVariants(ctx context.Context, articleID int, _ bool) ([]article.ShirtVariant, error) {
	var rows []shirtVariantRow
	if err := r.db.WithContext(ctx).Where("article_id = ?", articleID).Order("id asc").Find(&rows).Error; err != nil {
		return nil, err
	}
	out := make([]article.ShirtVariant, 0, len(rows))
	for i := range rows {
		out = append(out, toShirtVariant(&rows[i]))
	}
	return out, nil
}

func (r *Repository) GetShirtVariant(ctx context.Context, id int) (article.ShirtVariant, error) {
	var row shirtVariantRow
	if err := r.db.WithContext(ctx).First(&row, "id = ?", id).Error; err != nil {
		return article.ShirtVariant{}, err
	}
	return toShirtVariant(&row), nil
}

func (r *Repository) CreateShirtVariant(ctx context.Context, variant *article.ShirtVariant) error {
	row := fromShirtVariant(variant)
	if err := r.db.WithContext(ctx).Create(row).Error; err != nil {
		return err
	}
	variant.ID = row.ID
	variant.CreatedAt = row.CreatedAt
	variant.UpdatedAt = row.UpdatedAt
	return nil
}

func (r *Repository) UpdateShirtVariant(ctx context.Context, variant *article.ShirtVariant) error {
	row := fromShirtVariant(variant)
	if err := r.db.WithContext(ctx).Save(row).Error; err != nil {
		return err
	}
	variant.CreatedAt = row.CreatedAt
	variant.UpdatedAt = row.UpdatedAt
	return nil
}

func (r *Repository) DeleteShirtVariant(ctx context.Context, id int) error {
	return r.db.WithContext(ctx).Delete(&shirtVariantRow{}, id).Error
}

// --- Details & pricing ---

func (r *Repository) GetMugDetails(ctx context.Context, articleID int) (*article.MugDetails, error) {
	var row mugDetailsRow
	err := r.db.WithContext(ctx).First(&row, "article_id = ?", articleID).Error
	if errors.Is(err, gorm.ErrRecordNotFound) {
		return nil, nil
	}
	if err != nil {
		return nil, err
	}
	res := toMugDetails(&row)
	return &res, nil
}

func (r *Repository) UpsertMugDetails(ctx context.Context, details *article.MugDetails) error {
	if details == nil {
		return nil
	}
	row := fromMugDetails(details)
	var existing mugDetailsRow
	err := r.db.WithContext(ctx).First(&existing, "article_id = ?", details.ArticleID).Error
	if errors.Is(err, gorm.ErrRecordNotFound) {
		if err := r.db.WithContext(ctx).Create(row).Error; err != nil {
			return err
		}
		details.CreatedAt = row.CreatedAt
		details.UpdatedAt = row.UpdatedAt
		return nil
	}
	if err != nil {
		return err
	}
	row.CreatedAt = existing.CreatedAt
	if err := r.db.WithContext(ctx).Save(row).Error; err != nil {
		return err
	}
	details.CreatedAt = row.CreatedAt
	details.UpdatedAt = row.UpdatedAt
	return nil
}

func (r *Repository) DeleteMugDetails(ctx context.Context, articleID int) error {
	return r.db.WithContext(ctx).Delete(&mugDetailsRow{}, "article_id = ?", articleID).Error
}

func (r *Repository) GetShirtDetails(ctx context.Context, articleID int) (*article.ShirtDetails, error) {
	var row shirtDetailsRow
	err := r.db.WithContext(ctx).First(&row, "article_id = ?", articleID).Error
	if errors.Is(err, gorm.ErrRecordNotFound) {
		return nil, nil
	}
	if err != nil {
		return nil, err
	}
	res := toShirtDetails(&row)
	return &res, nil
}

func (r *Repository) UpsertShirtDetails(ctx context.Context, details *article.ShirtDetails) error {
	if details == nil {
		return nil
	}
	row := fromShirtDetails(details)
	var existing shirtDetailsRow
	err := r.db.WithContext(ctx).First(&existing, "article_id = ?", details.ArticleID).Error
	if errors.Is(err, gorm.ErrRecordNotFound) {
		if err := r.db.WithContext(ctx).Create(row).Error; err != nil {
			return err
		}
		details.CreatedAt = row.CreatedAt
		details.UpdatedAt = row.UpdatedAt
		return nil
	}
	if err != nil {
		return err
	}
	row.CreatedAt = existing.CreatedAt
	if err := r.db.WithContext(ctx).Save(row).Error; err != nil {
		return err
	}
	details.CreatedAt = row.CreatedAt
	details.UpdatedAt = row.UpdatedAt
	return nil
}

func (r *Repository) DeleteShirtDetails(ctx context.Context, articleID int) error {
	return r.db.WithContext(ctx).Delete(&shirtDetailsRow{}, "article_id = ?", articleID).Error
}

func (r *Repository) GetCostCalculation(ctx context.Context, articleID int) (*article.CostCalculation, error) {
	var row costCalculationRow
	err := r.db.WithContext(ctx).First(&row, "article_id = ?", articleID).Error
	if errors.Is(err, gorm.ErrRecordNotFound) {
		return nil, nil
	}
	if err != nil {
		return nil, err
	}
	res := toCostCalculation(&row)
	return &res, nil
}

func (r *Repository) UpsertCostCalculation(ctx context.Context, articleID int, calc *article.CostCalculation) error {
	if calc == nil {
		return nil
	}
	row := fromCostCalculation(calc)
	row.ArticleID = &articleID
	var existing costCalculationRow
	err := r.db.WithContext(ctx).First(&existing, "article_id = ?", articleID).Error
	if errors.Is(err, gorm.ErrRecordNotFound) {
		if err := r.db.WithContext(ctx).Create(row).Error; err != nil {
			return err
		}
		calc.ID = row.ID
		calc.ArticleID = row.ArticleID
		calc.CreatedAt = row.CreatedAt
		calc.UpdatedAt = row.UpdatedAt
		return nil
	}
	if err != nil {
		return err
	}
	row.ID = existing.ID
	row.CreatedAt = existing.CreatedAt
	if err := r.db.WithContext(ctx).Save(row).Error; err != nil {
		return err
	}
	calc.ID = row.ID
	calc.ArticleID = row.ArticleID
	calc.CreatedAt = row.CreatedAt
	calc.UpdatedAt = row.UpdatedAt
	return nil
}

func (r *Repository) DeleteCostCalculation(ctx context.Context, articleID int) error {
	return r.db.WithContext(ctx).Delete(&costCalculationRow{}, "article_id = ?", articleID).Error
}

// --- Listings & helpers ---

func (r *Repository) ListMugArticles(ctx context.Context, onlyActive bool, excludeID *int) ([]article.Article, error) {
	tx := r.db.WithContext(ctx).Where("article_type = ?", article.ArticleTypeMug)
	if onlyActive {
		tx = tx.Where("active = ?", true)
	}
	if excludeID != nil {
		tx = tx.Where("id <> ?", *excludeID)
	}
	var rows []articleRow
	if err := tx.Order("id desc").Find(&rows).Error; err != nil {
		return nil, err
	}
	out := make([]article.Article, 0, len(rows))
	for i := range rows {
		out = append(out, toArticle(&rows[i]))
	}
	return out, nil
}

// --- Conversion helpers ---

func toArticleCategory(row *articleCategoryRow) article.ArticleCategory {
	return article.ArticleCategory{
		ID:          row.ID,
		Name:        row.Name,
		Description: row.Description,
		CreatedAt:   row.CreatedAt,
		UpdatedAt:   row.UpdatedAt,
	}
}

func fromArticleCategory(cat *article.ArticleCategory) *articleCategoryRow {
	if cat == nil {
		return nil
	}
	return &articleCategoryRow{
		ID:          cat.ID,
		Name:        cat.Name,
		Description: cat.Description,
		CreatedAt:   cat.CreatedAt,
		UpdatedAt:   cat.UpdatedAt,
	}
}

func toArticleSubCategory(row *articleSubCategoryRow) article.ArticleSubCategory {
	return article.ArticleSubCategory{
		ID:                row.ID,
		ArticleCategoryID: row.ArticleCategoryID,
		Name:              row.Name,
		Description:       row.Description,
		CreatedAt:         row.CreatedAt,
		UpdatedAt:         row.UpdatedAt,
	}
}

func fromArticleSubCategory(sub *article.ArticleSubCategory) *articleSubCategoryRow {
	if sub == nil {
		return nil
	}
	return &articleSubCategoryRow{
		ID:                sub.ID,
		ArticleCategoryID: sub.ArticleCategoryID,
		Description:       sub.Description,
		Name:              sub.Name,
		CreatedAt:         sub.CreatedAt,
		UpdatedAt:         sub.UpdatedAt,
	}
}

func toArticle(row *articleRow) article.Article {
	return article.Article{
		ID:                    row.ID,
		Name:                  row.Name,
		DescriptionShort:      row.DescriptionShort,
		DescriptionLong:       row.DescriptionLong,
		Active:                row.Active,
		ArticleType:           row.ArticleType,
		CategoryID:            row.CategoryID,
		SubcategoryID:         row.SubcategoryID,
		SupplierID:            row.SupplierID,
		SupplierArticleName:   row.SupplierArticleName,
		SupplierArticleNumber: row.SupplierArticleNumber,
		CreatedAt:             row.CreatedAt,
		UpdatedAt:             row.UpdatedAt,
	}
}

func fromArticle(art *article.Article) *articleRow {
	if art == nil {
		return nil
	}
	return &articleRow{
		ID:                    art.ID,
		Name:                  art.Name,
		DescriptionShort:      art.DescriptionShort,
		DescriptionLong:       art.DescriptionLong,
		Active:                art.Active,
		ArticleType:           art.ArticleType,
		CategoryID:            art.CategoryID,
		SubcategoryID:         art.SubcategoryID,
		SupplierID:            art.SupplierID,
		SupplierArticleName:   art.SupplierArticleName,
		SupplierArticleNumber: art.SupplierArticleNumber,
		CreatedAt:             art.CreatedAt,
		UpdatedAt:             art.UpdatedAt,
	}
}

func toMugVariant(row *mugVariantRow) article.MugVariant {
	return article.MugVariant{
		ID:                   row.ID,
		ArticleID:            row.ArticleID,
		InsideColorCode:      row.InsideColorCode,
		OutsideColorCode:     row.OutsideColorCode,
		Name:                 row.Name,
		ExampleImageFilename: row.ExampleImageFilename,
		ArticleVariantNumber: row.ArticleVariantNumber,
		IsDefault:            row.IsDefault,
		Active:               row.Active,
		CreatedAt:            row.CreatedAt,
		UpdatedAt:            row.UpdatedAt,
	}
}

func fromMugVariant(v *article.MugVariant) *mugVariantRow {
	if v == nil {
		return nil
	}
	return &mugVariantRow{
		ID:                   v.ID,
		ArticleID:            v.ArticleID,
		InsideColorCode:      v.InsideColorCode,
		OutsideColorCode:     v.OutsideColorCode,
		Name:                 v.Name,
		ExampleImageFilename: v.ExampleImageFilename,
		ArticleVariantNumber: v.ArticleVariantNumber,
		IsDefault:            v.IsDefault,
		Active:               v.Active,
		CreatedAt:            v.CreatedAt,
		UpdatedAt:            v.UpdatedAt,
	}
}

func toShirtVariant(row *shirtVariantRow) article.ShirtVariant {
	return article.ShirtVariant{
		ID:                   row.ID,
		ArticleID:            row.ArticleID,
		Color:                row.Color,
		Size:                 row.Size,
		ExampleImageFilename: row.ExampleImageFilename,
		CreatedAt:            row.CreatedAt,
		UpdatedAt:            row.UpdatedAt,
	}
}

func fromShirtVariant(v *article.ShirtVariant) *shirtVariantRow {
	if v == nil {
		return nil
	}
	return &shirtVariantRow{
		ID:                   v.ID,
		ArticleID:            v.ArticleID,
		Color:                v.Color,
		Size:                 v.Size,
		ExampleImageFilename: v.ExampleImageFilename,
		CreatedAt:            v.CreatedAt,
		UpdatedAt:            v.UpdatedAt,
	}
}

func toMugDetails(row *mugDetailsRow) article.MugDetails {
	return article.MugDetails{
		ArticleID:                    row.ArticleID,
		HeightMm:                     row.HeightMm,
		DiameterMm:                   row.DiameterMm,
		PrintTemplateWidthMm:         row.PrintTemplateWidthMm,
		PrintTemplateHeightMm:        row.PrintTemplateHeightMm,
		DocumentFormatWidthMm:        row.DocumentFormatWidthMm,
		DocumentFormatHeightMm:       row.DocumentFormatHeightMm,
		DocumentFormatMarginBottomMm: row.DocumentFormatMarginBottomMm,
		FillingQuantity:              row.FillingQuantity,
		DishwasherSafe:               row.DishwasherSafe,
		CreatedAt:                    row.CreatedAt,
		UpdatedAt:                    row.UpdatedAt,
	}
}

func fromMugDetails(d *article.MugDetails) *mugDetailsRow {
	if d == nil {
		return nil
	}
	return &mugDetailsRow{
		ArticleID:                    d.ArticleID,
		HeightMm:                     d.HeightMm,
		DiameterMm:                   d.DiameterMm,
		PrintTemplateWidthMm:         d.PrintTemplateWidthMm,
		PrintTemplateHeightMm:        d.PrintTemplateHeightMm,
		DocumentFormatWidthMm:        d.DocumentFormatWidthMm,
		DocumentFormatHeightMm:       d.DocumentFormatHeightMm,
		DocumentFormatMarginBottomMm: d.DocumentFormatMarginBottomMm,
		FillingQuantity:              d.FillingQuantity,
		DishwasherSafe:               d.DishwasherSafe,
		CreatedAt:                    d.CreatedAt,
		UpdatedAt:                    d.UpdatedAt,
	}
}

func toShirtDetails(row *shirtDetailsRow) article.ShirtDetails {
	return article.ShirtDetails{
		ArticleID:        row.ArticleID,
		Material:         row.Material,
		CareInstructions: row.CareInstructions,
		FitType:          row.FitType,
		AvailableSizes:   row.AvailableSizes,
		CreatedAt:        row.CreatedAt,
		UpdatedAt:        row.UpdatedAt,
	}
}

func fromShirtDetails(d *article.ShirtDetails) *shirtDetailsRow {
	if d == nil {
		return nil
	}
	return &shirtDetailsRow{
		ArticleID:        d.ArticleID,
		Material:         d.Material,
		CareInstructions: d.CareInstructions,
		FitType:          d.FitType,
		AvailableSizes:   d.AvailableSizes,
		CreatedAt:        d.CreatedAt,
		UpdatedAt:        d.UpdatedAt,
	}
}

func toCostCalculation(row *costCalculationRow) article.CostCalculation {
	return article.CostCalculation{
		ID:                       row.ID,
		ArticleID:                row.ArticleID,
		PurchasePriceNet:         row.PurchasePriceNet,
		PurchasePriceTax:         row.PurchasePriceTax,
		PurchasePriceGross:       row.PurchasePriceGross,
		PurchaseCostNet:          row.PurchaseCostNet,
		PurchaseCostTax:          row.PurchaseCostTax,
		PurchaseCostGross:        row.PurchaseCostGross,
		PurchaseCostPercent:      row.PurchaseCostPercent,
		PurchaseTotalNet:         row.PurchaseTotalNet,
		PurchaseTotalTax:         row.PurchaseTotalTax,
		PurchaseTotalGross:       row.PurchaseTotalGross,
		PurchasePriceUnit:        row.PurchasePriceUnit,
		PurchaseVatRateID:        row.PurchaseVatRateID,
		PurchaseVatRatePercent:   row.PurchaseVatRatePercent,
		PurchaseCalculationMode:  row.PurchaseCalculationMode,
		SalesVatRateID:           row.SalesVatRateID,
		SalesVatRatePercent:      row.SalesVatRatePercent,
		SalesMarginNet:           row.SalesMarginNet,
		SalesMarginTax:           row.SalesMarginTax,
		SalesMarginGross:         row.SalesMarginGross,
		SalesMarginPercent:       row.SalesMarginPercent,
		SalesTotalNet:            row.SalesTotalNet,
		SalesTotalTax:            row.SalesTotalTax,
		SalesTotalGross:          row.SalesTotalGross,
		SalesPriceUnit:           row.SalesPriceUnit,
		SalesCalculationMode:     row.SalesCalculationMode,
		PurchasePriceCorresponds: row.PurchasePriceCorresponds,
		SalesPriceCorresponds:    row.SalesPriceCorresponds,
		PurchaseActiveRow:        row.PurchaseActiveRow,
		SalesActiveRow:           row.SalesActiveRow,
		CreatedAt:                row.CreatedAt,
		UpdatedAt:                row.UpdatedAt,
	}
}

func fromCostCalculation(c *article.CostCalculation) *costCalculationRow {
	if c == nil {
		return nil
	}
	return &costCalculationRow{
		ID:                       c.ID,
		ArticleID:                c.ArticleID,
		PurchasePriceNet:         c.PurchasePriceNet,
		PurchasePriceTax:         c.PurchasePriceTax,
		PurchasePriceGross:       c.PurchasePriceGross,
		PurchaseCostNet:          c.PurchaseCostNet,
		PurchaseCostTax:          c.PurchaseCostTax,
		PurchaseCostGross:        c.PurchaseCostGross,
		PurchaseCostPercent:      c.PurchaseCostPercent,
		PurchaseTotalNet:         c.PurchaseTotalNet,
		PurchaseTotalTax:         c.PurchaseTotalTax,
		PurchaseTotalGross:       c.PurchaseTotalGross,
		PurchasePriceUnit:        c.PurchasePriceUnit,
		PurchaseVatRateID:        c.PurchaseVatRateID,
		PurchaseVatRatePercent:   c.PurchaseVatRatePercent,
		PurchaseCalculationMode:  c.PurchaseCalculationMode,
		SalesVatRateID:           c.SalesVatRateID,
		SalesVatRatePercent:      c.SalesVatRatePercent,
		SalesMarginNet:           c.SalesMarginNet,
		SalesMarginTax:           c.SalesMarginTax,
		SalesMarginGross:         c.SalesMarginGross,
		SalesMarginPercent:       c.SalesMarginPercent,
		SalesTotalNet:            c.SalesTotalNet,
		SalesTotalTax:            c.SalesTotalTax,
		SalesTotalGross:          c.SalesTotalGross,
		SalesPriceUnit:           c.SalesPriceUnit,
		SalesCalculationMode:     c.SalesCalculationMode,
		PurchasePriceCorresponds: c.PurchasePriceCorresponds,
		SalesPriceCorresponds:    c.SalesPriceCorresponds,
		PurchaseActiveRow:        c.PurchaseActiveRow,
		SalesActiveRow:           c.SalesActiveRow,
		CreatedAt:                c.CreatedAt,
		UpdatedAt:                c.UpdatedAt,
	}
}
