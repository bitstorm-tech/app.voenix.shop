package order

import (
	"context"
	"errors"
	"fmt"
	"sort"
	"time"

	"voenix/backend/internal/article"
	"voenix/backend/internal/cart"
)

type fakeRepository struct {
	activeCarts   map[int]*cart.Cart
	orders        map[string]Order
	ordersByCart  map[int]string
	generated     map[int]string
	nextOrderCode int
}

func newFakeRepository() *fakeRepository {
	return &fakeRepository{
		activeCarts:  make(map[int]*cart.Cart),
		orders:       make(map[string]Order),
		ordersByCart: make(map[int]string),
		generated:    make(map[int]string),
	}
}

func (f *fakeRepository) ActiveCart(_ context.Context, userID int) (*cart.Cart, error) {
	if c, ok := f.activeCarts[userID]; ok {
		clone := *c
		clone.Items = append([]cart.CartItem(nil), c.Items...)
		return &clone, nil
	}
	return nil, nil
}

func (f *fakeRepository) OrderExistsForCart(_ context.Context, cartID int) (bool, error) {
	_, ok := f.ordersByCart[cartID]
	return ok, nil
}

func (f *fakeRepository) CreateOrder(_ context.Context, ord *Order) error {
	if ord == nil {
		return errors.New("order nil")
	}
	if _, exists := f.ordersByCart[ord.CartID]; exists {
		return errors.New("order exists for cart")
	}
	if ord.OrderNumber == "" {
		f.nextOrderCode++
		ord.OrderNumber = time.Now().Format("20060102") + "-" + padInt(f.nextOrderCode)
	}
	now := time.Now()
	if ord.CreatedAt.IsZero() {
		ord.CreatedAt = now
	}
	ord.UpdatedAt = ord.CreatedAt
	clone := *ord
	clone.Items = append([]OrderItem(nil), ord.Items...)
	f.orders[ord.ID] = clone
	f.ordersByCart[ord.CartID] = ord.ID
	return nil
}

func (f *fakeRepository) OrderByIDForUser(_ context.Context, userID int, orderID string) (*Order, error) {
	ord, ok := f.orders[orderID]
	if !ok || ord.UserID != userID {
		return nil, ErrNotFound
	}
	clone := ord
	clone.Items = append([]OrderItem(nil), ord.Items...)
	return &clone, nil
}

func (f *fakeRepository) ListOrdersForUser(_ context.Context, userID int, page, size int) (OrderPage, error) {
	orders := make([]Order, 0)
	for _, ord := range f.orders {
		if ord.UserID == userID {
			orders = append(orders, ord)
		}
	}
	sort.Slice(orders, func(i, j int) bool {
		return orders[i].CreatedAt.After(orders[j].CreatedAt)
	})
	if size <= 0 {
		size = 20
	}
	if page < 0 {
		page = 0
	}
	total := len(orders)
	start := page * size
	if start > total {
		start = total
	}
	end := start + size
	if end > total {
		end = total
	}
	pageOrders := append([]Order(nil), orders[start:end]...)
	totalPages := 0
	if size > 0 {
		totalPages = (total + size - 1) / size
	}
	return OrderPage{
		Orders:        pageOrders,
		CurrentPage:   page,
		TotalPages:    totalPages,
		TotalElements: int64(total),
		Size:          size,
	}, nil
}

func (f *fakeRepository) FetchGeneratedImageFilenames(_ context.Context, ids []int) (map[int]string, error) {
	result := make(map[int]string, len(ids))
	for _, id := range ids {
		if fn, ok := f.generated[id]; ok {
			result[id] = fn
		}
	}
	return result, nil
}

func (f *fakeRepository) setActiveCart(c cart.Cart) {
	clone := c
	clone.Items = append([]cart.CartItem(nil), c.Items...)
	f.activeCarts[c.UserID] = &clone
}

func (f *fakeRepository) addOrder(ord Order) {
	clone := ord
	clone.Items = append([]OrderItem(nil), ord.Items...)
	f.orders[ord.ID] = clone
	f.ordersByCart[ord.CartID] = ord.ID
}

func (f *fakeRepository) setGenerated(id int, filename string) {
	f.generated[id] = filename
}

func padInt(i int) string {
	return fmt.Sprintf("%03d", i)
}

type fakeArticleService struct {
	summaries map[int]article.ArticleResponse
	articles  map[int]article.Article
	variants  map[int]article.MugVariant
	details   map[int]article.MugDetails
}

func newFakeArticleService() *fakeArticleService {
	return &fakeArticleService{
		summaries: make(map[int]article.ArticleResponse),
		articles:  make(map[int]article.Article),
		variants:  make(map[int]article.MugVariant),
		details:   make(map[int]article.MugDetails),
	}
}

func (s *fakeArticleService) GetArticleSummary(_ context.Context, id int) (article.ArticleResponse, error) {
	if resp, ok := s.summaries[id]; ok {
		return resp, nil
	}
	return article.ArticleResponse{}, errors.New("not found")
}

func (s *fakeArticleService) GetArticle(_ context.Context, id int) (article.Article, error) {
	if a, ok := s.articles[id]; ok {
		return a, nil
	}
	return article.Article{}, errors.New("not found")
}

func (s *fakeArticleService) GetMugVariant(_ context.Context, id int) (article.MugVariant, error) {
	if v, ok := s.variants[id]; ok {
		return v, nil
	}
	return article.MugVariant{}, errors.New("not found")
}

func (s *fakeArticleService) GetMugDetails(_ context.Context, articleID int) (*article.MugDetails, error) {
	if md, ok := s.details[articleID]; ok {
		clone := md
		return &clone, nil
	}
	return nil, nil
}
