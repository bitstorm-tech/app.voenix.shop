package postgres

import (
	"context"
	"errors"
	"time"

	"gorm.io/gorm"

	"voenix/backend/internal/cart"
	"voenix/backend/internal/prompt"
)

const defaultCartExpiryDays = 30

type Repository struct {
	db *gorm.DB
}

func NewRepository(db *gorm.DB) *Repository {
	return &Repository{db: db}
}

var _ cart.Repository = (*Repository)(nil)

func (r *Repository) GetOrCreateActiveCart(ctx context.Context, userID int) (*cart.Cart, error) {
	var row CartRow
	err := r.db.WithContext(ctx).
		Preload("Items", withItemOrder).
		Where("user_id = ? AND status = ?", userID, string(cart.CartStatusActive)).
		First(&row).Error
	if err == nil {
		domain := row.ToDomain()
		return &domain, nil
	}
	if !errors.Is(err, gorm.ErrRecordNotFound) {
		return nil, err
	}
	expiry := time.Now().Add(defaultCartExpiryDays * 24 * time.Hour)
	row = CartRow{UserID: userID, Status: string(cart.CartStatusActive), ExpiresAt: &expiry}
	if err := r.db.WithContext(ctx).Create(&row).Error; err != nil {
		return nil, err
	}
	if err := r.db.WithContext(ctx).Preload("Items", withItemOrder).First(&row, row.ID).Error; err != nil {
		return nil, err
	}
	domain := row.ToDomain()
	return &domain, nil
}

func (r *Repository) LoadActiveCart(ctx context.Context, userID int) (*cart.Cart, error) {
	var row CartRow
	err := r.db.WithContext(ctx).
		Preload("Items", withItemOrder).
		Where("user_id = ? AND status = ?", userID, string(cart.CartStatusActive)).
		First(&row).Error
	if err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, nil
		}
		return nil, err
	}
	domain := row.ToDomain()
	return &domain, nil
}

func (r *Repository) SaveCart(ctx context.Context, c cart.Cart) (*cart.Cart, error) {
	row := FromDomainCart(c)
	if err := r.db.WithContext(ctx).
		Session(&gorm.Session{FullSaveAssociations: true}).
		Save(&row).Error; err != nil {
		return nil, err
	}
	if err := r.db.WithContext(ctx).Preload("Items", withItemOrder).First(&row, row.ID).Error; err != nil {
		return nil, err
	}
	domain := row.ToDomain()
	return &domain, nil
}

func (r *Repository) UpdateItemQuantity(ctx context.Context, cartID, itemID, quantity int) (bool, error) {
	res := r.db.WithContext(ctx).
		Model(&CartItemRow{}).
		Where("id = ? AND cart_id = ?", itemID, cartID).
		Update("quantity", quantity)
	if res.Error != nil {
		return false, res.Error
	}
	return res.RowsAffected > 0, nil
}

func (r *Repository) DeleteItem(ctx context.Context, cartID, itemID int) (bool, error) {
	tx := r.db.WithContext(ctx)
	res := tx.Where("id = ? AND cart_id = ?", itemID, cartID).Delete(&CartItemRow{})
	if res.Error != nil {
		return false, res.Error
	}
	if res.RowsAffected == 0 {
		return false, nil
	}
	var items []CartItemRow
	if err := tx.Where("cart_id = ?", cartID).Order("position asc, created_at asc").Find(&items).Error; err != nil {
		return false, err
	}
	for idx := range items {
		if items[idx].Position != idx {
			if err := tx.Model(&CartItemRow{}).Where("id = ?", items[idx].ID).Update("position", idx).Error; err != nil {
				return false, err
			}
		}
	}
	return true, nil
}

func (r *Repository) ClearCartItems(ctx context.Context, cartID int) error {
	return r.db.WithContext(ctx).Where("cart_id = ?", cartID).Delete(&CartItemRow{}).Error
}

func (r *Repository) ReloadCart(ctx context.Context, cartID int) (*cart.Cart, error) {
	var row CartRow
	if err := r.db.WithContext(ctx).Preload("Items", withItemOrder).First(&row, cartID).Error; err != nil {
		return nil, err
	}
	domain := row.ToDomain()
	return &domain, nil
}

func (r *Repository) FetchGeneratedImageFilenames(ctx context.Context, ids []int) (map[int]string, error) {
	result := make(map[int]string, len(ids))
	if len(ids) == 0 {
		return result, nil
	}
	type row struct {
		ID       int
		Filename string
	}
	var rows []row
	if err := r.db.WithContext(ctx).
		Table("generated_images").
		Select("id, filename").
		Where("id IN ?", ids).
		Scan(&rows).Error; err != nil {
		return nil, err
	}
	for _, r := range rows {
		result[r.ID] = r.Filename
	}
	return result, nil
}

func (r *Repository) FetchPromptTitles(ctx context.Context, ids []int) (map[int]string, error) {
	result := make(map[int]string, len(ids))
	if len(ids) == 0 {
		return result, nil
	}
	type row struct {
		ID    int
		Title string
	}
	var rows []row
	if err := r.db.WithContext(ctx).
		Table("prompts").
		Select("id, title").
		Where("id IN ?", ids).
		Scan(&rows).Error; err != nil {
		return nil, err
	}
	for _, r := range rows {
		result[r.ID] = r.Title
	}
	return result, nil
}

func (r *Repository) PromptExists(ctx context.Context, id int) (bool, error) {
	if id == 0 {
		return false, nil
	}
	var count int64
	if err := r.db.WithContext(ctx).Model(&prompt.Prompt{}).Where("id = ?", id).Count(&count).Error; err != nil {
		return false, err
	}
	return count > 0, nil
}

func (r *Repository) LoadPrompt(ctx context.Context, id int) (*prompt.Prompt, error) {
	if id == 0 {
		return nil, gorm.ErrRecordNotFound
	}
	var row prompt.Prompt
	if err := r.db.WithContext(ctx).First(&row, "id = ?", id).Error; err != nil {
		return nil, err
	}
	return &row, nil
}

func (r *Repository) WithTx(ctx context.Context, fn func(cart.Repository) error) error {
	return r.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		return fn(&Repository{db: tx})
	})
}

func withItemOrder(tx *gorm.DB) *gorm.DB {
	return tx.Order("position asc, created_at asc")
}
