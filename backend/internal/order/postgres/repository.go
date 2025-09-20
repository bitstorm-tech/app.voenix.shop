package postgres

import (
	"context"
	"errors"

	"gorm.io/gorm"

	"voenix/backend/internal/cart"
	cartpg "voenix/backend/internal/cart/postgres"
	"voenix/backend/internal/order"
)

type Repository struct {
	db *gorm.DB
}

func NewRepository(db *gorm.DB) *Repository {
	return &Repository{db: db}
}

var _ order.Repository = (*Repository)(nil)

func (r *Repository) ActiveCart(ctx context.Context, userID int) (*cart.Cart, error) {
	var row cartpg.CartRow
	err := r.db.WithContext(ctx).
		Preload("Items", orderCartItemsOrder).
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

func (r *Repository) OrderExistsForCart(ctx context.Context, cartID int) (bool, error) {
	var count int64
	if err := r.db.WithContext(ctx).Model(&OrderRow{}).Where("cart_id = ?", cartID).Count(&count).Error; err != nil {
		return false, err
	}
	return count > 0, nil
}

func (r *Repository) CreateOrder(ctx context.Context, ord *order.Order) error {
	if ord == nil {
		return errors.New("order is nil")
	}
	row := orderRowFromDomain(*ord)
	err := r.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		if row.ID == "" {
			return errors.New("order ID missing")
		}
		if err := tx.Omit("Items.*").Create(&row).Error; err != nil {
			return err
		}
		if len(row.Items) > 0 {
			for idx := range row.Items {
				row.Items[idx].OrderID = row.ID
			}
			if err := tx.Create(&row.Items).Error; err != nil {
				return err
			}
		}
		if err := tx.Model(&cartpg.CartRow{}).
			Where("id = ?", ord.CartID).
			Update("status", string(cart.CartStatusConverted)).Error; err != nil {
			return err
		}
		return nil
	})
	if err != nil {
		return err
	}
	var refreshed OrderRow
	if err := r.db.WithContext(ctx).
		Preload("Items").
		First(&refreshed, "id = ?", row.ID).Error; err != nil {
		return err
	}
	domain := refreshed.toDomain()
	*ord = domain
	return nil
}

func (r *Repository) OrderByIDForUser(ctx context.Context, userID int, orderID string) (*order.Order, error) {
	var row OrderRow
	err := r.db.WithContext(ctx).
		Preload("Items").
		First(&row, "id = ? AND user_id = ?", orderID, userID).Error
	if err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, order.ErrNotFound
		}
		return nil, err
	}
	domain := row.toDomain()
	return &domain, nil
}

func (r *Repository) ListOrdersForUser(ctx context.Context, userID int, page, size int) (order.OrderPage, error) {
	if size <= 0 {
		size = 20
	}
	if page < 0 {
		page = 0
	}
	var total int64
	if err := r.db.WithContext(ctx).
		Model(&OrderRow{}).
		Where("user_id = ?", userID).
		Count(&total).Error; err != nil {
		return order.OrderPage{}, err
	}
	var rows []OrderRow
	if err := r.db.WithContext(ctx).
		Preload("Items").
		Where("user_id = ?", userID).
		Order("created_at desc").
		Limit(size).
		Offset(page * size).
		Find(&rows).Error; err != nil {
		return order.OrderPage{}, err
	}
	orders := make([]order.Order, 0, len(rows))
	for _, row := range rows {
		orders = append(orders, row.toDomain())
	}
	totalPages := int((total + int64(size) - 1) / int64(size))
	return order.OrderPage{
		Orders:        orders,
		CurrentPage:   page,
		TotalPages:    totalPages,
		TotalElements: total,
		Size:          size,
	}, nil
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

func orderCartItemsOrder(tx *gorm.DB) *gorm.DB {
	return tx.Order("position asc, created_at asc")
}
