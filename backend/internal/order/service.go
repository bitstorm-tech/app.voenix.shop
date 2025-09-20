package order

import (
	"context"
	"encoding/json"
	"errors"
	"fmt"
	"os"
	"strings"

	"github.com/google/uuid"

	"voenix/backend/internal/article"
	"voenix/backend/internal/pdf"
)

const (
	taxRate           = 0.08
	shippingFlatCents = 499
)

// Service coordinates order workflows with repositories and downstream services.
type Service struct {
	repo       Repository
	articleSvc ArticleService
}

func NewService(repo Repository, articleSvc ArticleService) *Service {
	return &Service{repo: repo, articleSvc: articleSvc}
}

// CreateOrderFromCart creates an order from the user's active cart.
func (s *Service) CreateOrderFromCart(ctx context.Context, userID int, req CreateOrderRequest) (*Order, error) {
	c, err := s.repo.ActiveCart(ctx, userID)
	if err != nil {
		return nil, err
	}
	if c == nil || len(c.Items) == 0 {
		return nil, fmt.Errorf("no active cart found or cart is empty")
	}
	exists, err := s.repo.OrderExistsForCart(ctx, c.ID)
	if err != nil {
		return nil, err
	}
	if exists {
		return nil, fmt.Errorf("order already exists for cart: %d", c.ID)
	}

	subtotal := int64(0)
	for _, it := range c.Items {
		subtotal += int64(it.PriceAtTime * it.Quantity)
	}
	tax := int64(float64(subtotal) * taxRate)
	shipping := int64(0)
	if len(c.Items) > 0 {
		shipping = shippingFlatCents
	}
	total := subtotal + tax + shipping

	ship := req.ShippingAddress
	bill := req.BillingAddress
	if bill == nil || (req.UseShippingAsBilling != nil && *req.UseShippingAsBilling) {
		bill = &ship
	}

	ord := &Order{
		ID:              uuid.New().String(),
		UserID:          userID,
		CustomerEmail:   req.CustomerEmail,
		CustomerFirst:   req.CustomerFirstName,
		CustomerLast:    req.CustomerLastName,
		CustomerPhone:   req.CustomerPhone,
		ShippingStreet1: ship.StreetAddress1,
		ShippingStreet2: ship.StreetAddress2,
		ShippingCity:    ship.City,
		ShippingState:   ship.State,
		ShippingPostal:  ship.PostalCode,
		ShippingCountry: ship.Country,
		BillingStreet1:  strPtrOrNil(bill.StreetAddress1),
		BillingStreet2:  bill.StreetAddress2,
		BillingCity:     &bill.City,
		BillingState:    &bill.State,
		BillingPostal:   &bill.PostalCode,
		BillingCountry:  &bill.Country,
		Subtotal:        subtotal,
		TaxAmount:       tax,
		ShippingAmount:  shipping,
		TotalAmount:     total,
		Status:          StatusPending,
		CartID:          c.ID,
		Notes:           req.Notes,
	}

	items := make([]OrderItem, 0, len(c.Items))
	for _, ci := range c.Items {
		cd := canonicalizeJSON(ci.CustomData)
		item := OrderItem{
			ID:               uuid.New().String(),
			OrderID:          ord.ID,
			ArticleID:        ci.ArticleID,
			VariantID:        ci.VariantID,
			Quantity:         ci.Quantity,
			PricePerItem:     int64(ci.PriceAtTime),
			TotalPrice:       int64(ci.PriceAtTime * ci.Quantity),
			GeneratedImageID: ci.GeneratedImageID,
			PromptID:         ci.PromptID,
			CustomData:       cd,
		}
		items = append(items, item)
	}
	ord.Items = items

	if err := s.repo.CreateOrder(ctx, ord); err != nil {
		return nil, err
	}
	return ord, nil
}

// ListOrders returns paginated orders for a user.
func (s *Service) ListOrders(ctx context.Context, userID int, page, size int) (OrderPage, error) {
	return s.repo.ListOrdersForUser(ctx, userID, page, size)
}

// GetOrder fetches a single order for a user.
func (s *Service) GetOrder(ctx context.Context, userID int, orderID string) (*Order, error) {
	o, err := s.repo.OrderByIDForUser(ctx, userID, orderID)
	if err != nil {
		if errors.Is(err, ErrNotFound) {
			return nil, ErrNotFound
		}
		return nil, err
	}
	return o, nil
}

func (s *Service) OrderDTO(ctx context.Context, o Order, baseURL string) (OrderDto, error) {
	return s.toOrderDto(ctx, o, baseURL)
}

func (s *Service) BuildOrderPDFData(ctx context.Context, o Order) (pdf.OrderPdfData, error) {
	return buildOrderPdfData(ctx, s.articleSvc, s.repo, &o)
}

func (s *Service) toOrderDto(ctx context.Context, o Order, baseURL string) (OrderDto, error) {
	ship, bill := toAddressDtoFromOrder(o)

	generatedImageIDs := make([]int, 0, len(o.Items))
	for _, it := range o.Items {
		if it.GeneratedImageID != nil {
			generatedImageIDs = append(generatedImageIDs, *it.GeneratedImageID)
		}
	}
	generatedFilenames, err := s.repo.FetchGeneratedImageFilenames(ctx, generatedImageIDs)
	if err != nil {
		return OrderDto{}, err
	}

	items := make([]OrderItemDto, 0, len(o.Items))
	for _, it := range o.Items {
		art, err := s.loadArticleResponse(ctx, it.ArticleID)
		if err != nil {
			return OrderDto{}, err
		}
		mv, _ := s.loadMugVariantDto(ctx, it.VariantID)
		var genFilename *string
		if it.GeneratedImageID != nil {
			if fn, ok := generatedFilenames[*it.GeneratedImageID]; ok && fn != "" {
				genFilename = &fn
			}
		}
		items = append(items, OrderItemDto{
			ID:                     it.ID,
			Article:                art,
			Variant:                mv,
			Quantity:               it.Quantity,
			PricePerItem:           it.PricePerItem,
			TotalPrice:             it.TotalPrice,
			GeneratedImageID:       it.GeneratedImageID,
			GeneratedImageFilename: genFilename,
			PromptID:               it.PromptID,
			CustomData:             parseJSONMap(it.CustomData),
			CreatedAt:              it.CreatedAt,
		})
	}

	pdfURL := fmt.Sprintf("%s/api/user/orders/%s/pdf", strings.TrimRight(baseURL, "/"), o.ID)
	return OrderDto{
		ID:              o.ID,
		OrderNumber:     o.OrderNumber,
		CustomerEmail:   o.CustomerEmail,
		CustomerFirst:   o.CustomerFirst,
		CustomerLast:    o.CustomerLast,
		CustomerPhone:   o.CustomerPhone,
		ShippingAddress: ship,
		BillingAddress:  bill,
		Subtotal:        o.Subtotal,
		TaxAmount:       o.TaxAmount,
		ShippingAmount:  o.ShippingAmount,
		TotalAmount:     o.TotalAmount,
		Status:          o.Status,
		CartID:          o.CartID,
		Notes:           o.Notes,
		Items:           items,
		PDFURL:          pdfURL,
		CreatedAt:       o.CreatedAt,
		UpdatedAt:       o.UpdatedAt,
	}, nil
}

func (s *Service) loadArticleResponse(ctx context.Context, id int) (article.ArticleResponse, error) {
	resp, err := s.articleSvc.GetArticleSummary(ctx, id)
	if err != nil {
		return article.ArticleResponse{}, err
	}
	return resp, nil
}

func (s *Service) loadMugVariantDto(ctx context.Context, id int) (*article.MugVariant, error) {
	v, err := s.articleSvc.GetMugVariant(ctx, id)
	if err != nil {
		return nil, err
	}
	return &article.MugVariant{
		ID:                   v.ID,
		ArticleID:            v.ArticleID,
		OutsideColorCode:     v.OutsideColorCode,
		ArticleVariantNumber: v.ArticleVariantNumber,
		IsDefault:            v.IsDefault,
		ExampleImageFilename: v.ExampleImageFilename,
	}, nil
}

func toAddressDtoFromOrder(o Order) (AddressDto, *AddressDto) {
	ship := AddressDto{
		StreetAddress1: o.ShippingStreet1,
		StreetAddress2: o.ShippingStreet2,
		City:           o.ShippingCity,
		State:          o.ShippingState,
		PostalCode:     o.ShippingPostal,
		Country:        o.ShippingCountry,
	}
	var bill *AddressDto
	if o.BillingStreet1 != nil || o.BillingCity != nil || o.BillingState != nil || o.BillingPostal != nil || o.BillingCountry != nil {
		b := AddressDto{
			StreetAddress1: deref(o.BillingStreet1),
			StreetAddress2: o.BillingStreet2,
			City:           deref(o.BillingCity),
			State:          deref(o.BillingState),
			PostalCode:     deref(o.BillingPostal),
			Country:        deref(o.BillingCountry),
		}
		bill = &b
	}
	return ship, bill
}

func parseJSONMap(s string) map[string]any {
	var m map[string]any
	if s == "" {
		return map[string]any{}
	}
	_ = json.Unmarshal([]byte(s), &m)
	if m == nil {
		return map[string]any{}
	}
	return m
}

func canonicalizeJSON(s string) string {
	var m map[string]any
	if s == "" {
		m = map[string]any{}
	} else {
		_ = json.Unmarshal([]byte(s), &m)
		if m == nil {
			m = map[string]any{}
		}
	}
	b, err := json.Marshal(m)
	if err != nil {
		return "{}"
	}
	return string(b)
}

func strPtrOrNil(s string) *string {
	if s == "" {
		return nil
	}
	return &s
}

func deref(p *string) string {
	if p == nil {
		return ""
	}
	return *p
}

// BaseURL returns APP_BASE_URL or fallback.
func BaseURL() string {
	if v := os.Getenv("APP_BASE_URL"); v != "" {
		return v
	}
	if v := os.Getenv("PUBLIC_APP_BASE_URL"); v != "" {
		return v
	}
	return "http://localhost:8081"
}
