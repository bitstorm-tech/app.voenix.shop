package cart

import (
	"context"
	"encoding/json"
	"errors"

	"gorm.io/gorm"
)

var (
	ErrCartNotFound     = errors.New("active cart not found")
	ErrCartItemNotFound = errors.New("cart item not found")
)

type ValidationError struct {
	message string
}

func (e ValidationError) Error() string { return e.message }

func newValidationError(msg string) error { return ValidationError{message: msg} }

func isValidationError(err error) bool {
	var v ValidationError
	return errors.As(err, &v)
}

type Service struct {
	repo       Repository
	articleSvc ArticleService
}

func NewService(repo Repository, articleSvc ArticleService) *Service {
	return &Service{repo: repo, articleSvc: articleSvc}
}

func (s *Service) GetCart(ctx context.Context, userID int) (*CartDto, error) {
	cart, err := s.repo.GetOrCreateActiveCart(ctx, userID)
	if err != nil {
		return nil, err
	}
	return s.prepareCartResponse(ctx, cart)
}

func (s *Service) GetCartSummary(ctx context.Context, userID int) (CartSummaryDto, error) {
	cart, err := s.repo.LoadActiveCart(ctx, userID)
	if err != nil {
		return CartSummaryDto{}, err
	}
	if cart == nil || len(cart.Items) == 0 {
		return CartSummaryDto{ItemCount: 0, TotalPrice: 0, HasItems: false}, nil
	}
	itemCount := 0
	total := 0
	for i := range cart.Items {
		it := cart.Items[i]
		itemCount += it.Quantity
		total += (it.PriceAtTime + it.PromptPriceAtTime) * it.Quantity
	}
	return CartSummaryDto{ItemCount: itemCount, TotalPrice: total, HasItems: itemCount > 0}, nil
}

func (s *Service) AddItem(ctx context.Context, userID int, req AddToCartRequest) (*CartDto, error) {
	quantity := req.Quantity
	if quantity <= 0 {
		quantity = 1
	}
	if req.CustomData == nil {
		req.CustomData = map[string]any{}
	}
	if err := validateArticleAndVariant(ctx, s.articleSvc, req.ArticleID, req.VariantID); err != nil {
		return nil, err
	}
	if err := validatePromptIfProvided(ctx, s.repo, req.PromptID); err != nil {
		return nil, err
	}
	cart, err := s.repo.GetOrCreateActiveCart(ctx, userID)
	if err != nil {
		return nil, err
	}
	price, err := currentGrossPrice(ctx, s.articleSvc, req.ArticleID)
	if err != nil {
		return nil, err
	}
	promptPriceAtTime := 0
	if req.PromptID != nil {
		promptPriceAtTime, err = promptCurrentGrossPrice(ctx, s.repo, s.articleSvc, *req.PromptID)
		if err != nil {
			return nil, err
		}
	}
	cdStr := "{}"
	if len(req.CustomData) > 0 {
		if b, err := json.Marshal(req.CustomData); err == nil {
			cdStr = string(b)
		}
	}
	item := CartItem{
		CartID:              cart.ID,
		ArticleID:           req.ArticleID,
		VariantID:           req.VariantID,
		Quantity:            quantity,
		PriceAtTime:         price,
		OriginalPrice:       price,
		PromptPriceAtTime:   promptPriceAtTime,
		PromptOriginalPrice: promptPriceAtTime,
		CustomData:          cdStr,
		GeneratedImageID:    req.GeneratedImageID,
		PromptID:            req.PromptID,
	}
	mergeOrAppendItem(cart, item)
	saved, err := s.repo.SaveCart(ctx, *cart)
	if err != nil {
		return nil, err
	}
	return s.prepareCartResponse(ctx, saved)
}

func (s *Service) UpdateItemQuantity(ctx context.Context, userID, itemID, quantity int) (*CartDto, error) {
	if quantity <= 0 {
		return nil, newValidationError("quantity must be at least 1")
	}
	cart, err := s.repo.LoadActiveCart(ctx, userID)
	if err != nil {
		return nil, err
	}
	if cart == nil {
		return nil, ErrCartNotFound
	}
	found := false
	for i := range cart.Items {
		if cart.Items[i].ID == itemID {
			found = true
			break
		}
	}
	if !found {
		return nil, ErrCartItemNotFound
	}
	updated, err := s.repo.UpdateItemQuantity(ctx, cart.ID, itemID, quantity)
	if err != nil {
		return nil, err
	}
	if !updated {
		return nil, ErrCartItemNotFound
	}
	refreshed, err := s.repo.ReloadCart(ctx, cart.ID)
	if err != nil {
		return nil, err
	}
	return s.prepareCartResponse(ctx, refreshed)
}

func (s *Service) DeleteItem(ctx context.Context, userID, itemID int) (*CartDto, error) {
	cart, err := s.repo.LoadActiveCart(ctx, userID)
	if err != nil {
		return nil, err
	}
	if cart == nil {
		return nil, ErrCartNotFound
	}
	deleted, err := s.repo.DeleteItem(ctx, cart.ID, itemID)
	if err != nil {
		return nil, err
	}
	if !deleted {
		return nil, ErrCartItemNotFound
	}
	refreshed, err := s.repo.ReloadCart(ctx, cart.ID)
	if err != nil {
		return nil, err
	}
	return s.prepareCartResponse(ctx, refreshed)
}

func (s *Service) ClearCart(ctx context.Context, userID int) (*CartDto, error) {
	cart, err := s.repo.LoadActiveCart(ctx, userID)
	if err != nil {
		return nil, err
	}
	if cart == nil {
		return nil, ErrCartNotFound
	}
	if err := s.repo.ClearCartItems(ctx, cart.ID); err != nil {
		return nil, err
	}
	refreshed, err := s.repo.ReloadCart(ctx, cart.ID)
	if err != nil {
		return nil, err
	}
	return s.prepareCartResponse(ctx, refreshed)
}

func (s *Service) RefreshPrices(ctx context.Context, userID int) (*CartDto, error) {
	cart, err := s.repo.LoadActiveCart(ctx, userID)
	if err != nil {
		return nil, err
	}
	if cart == nil {
		return nil, ErrCartNotFound
	}
	changed := false
	for i := range cart.Items {
		articleCurrent, err := currentGrossPrice(ctx, s.articleSvc, cart.Items[i].ArticleID)
		if err != nil {
			return nil, err
		}
		if cart.Items[i].OriginalPrice != articleCurrent {
			cart.Items[i].OriginalPrice = articleCurrent
			changed = true
		}
		promptCurrent := 0
		if cart.Items[i].PromptID != nil {
			promptCurrent, err = promptCurrentGrossPrice(ctx, s.repo, s.articleSvc, *cart.Items[i].PromptID)
			if err != nil {
				return nil, err
			}
		}
		if cart.Items[i].PromptOriginalPrice != promptCurrent {
			cart.Items[i].PromptOriginalPrice = promptCurrent
			changed = true
		}
	}
	if !changed {
		return s.prepareCartResponse(ctx, cart)
	}
	saved, err := s.repo.SaveCart(ctx, *cart)
	if err != nil {
		return nil, err
	}
	return s.prepareCartResponse(ctx, saved)
}

func (s *Service) prepareCartResponse(ctx context.Context, cart *Cart) (*CartDto, error) {
	generatedImageIDs := make([]int, 0, len(cart.Items))
	promptIDs := make([]int, 0, len(cart.Items))
	seenPrompt := make(map[int]struct{}, len(cart.Items))
	for i := range cart.Items {
		if cart.Items[i].GeneratedImageID != nil {
			generatedImageIDs = append(generatedImageIDs, *cart.Items[i].GeneratedImageID)
		}
		if cart.Items[i].PromptID != nil {
			pid := *cart.Items[i].PromptID
			if _, ok := seenPrompt[pid]; !ok {
				seenPrompt[pid] = struct{}{}
				promptIDs = append(promptIDs, pid)
			}
		}
	}
	generated, err := s.repo.FetchGeneratedImageFilenames(ctx, generatedImageIDs)
	if err != nil {
		return nil, err
	}
	titles, err := s.repo.FetchPromptTitles(ctx, promptIDs)
	if err != nil {
		return nil, err
	}
	return assembleCartDto(ctx, s.articleSvc, cart, generated, titles)
}

// mergeOrAppendItem merges quantity if an item with same articleId, variantId and customData exists; otherwise appends.
func mergeOrAppendItem(c *Cart, item CartItem) {
	item.CustomData = canonicalizeJSON(item.CustomData)
	for i := range c.Items {
		it := &c.Items[i]
		samePrompt := false
		if it.PromptID == nil && item.PromptID == nil {
			samePrompt = true
		} else if it.PromptID != nil && item.PromptID != nil && *it.PromptID == *item.PromptID {
			samePrompt = true
		}
		if it.ArticleID == item.ArticleID && it.VariantID == item.VariantID && samePrompt &&
			canonicalizeJSON(it.CustomData) == item.CustomData &&
			it.PriceAtTime == item.PriceAtTime &&
			it.PromptPriceAtTime == item.PromptPriceAtTime {
			it.Quantity += item.Quantity
			return
		}
	}
	item.Position = len(c.Items)
	c.Items = append(c.Items, item)
}

func parseJSONMap(s string) map[string]any {
	if s == "" {
		return map[string]any{}
	}
	var m map[string]any
	if err := json.Unmarshal([]byte(s), &m); err != nil || m == nil {
		return map[string]any{}
	}
	return m
}

func canonicalizeJSON(s string) string {
	m := parseJSONMap(s)
	b, err := json.Marshal(m)
	if err != nil {
		return "{}"
	}
	return string(b)
}

func validateArticleAndVariant(ctx context.Context, articleSvc ArticleService, articleID, variantID int) error {
	art, err := articleSvc.GetArticle(ctx, articleID)
	if err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return newValidationError("article not found")
		}
		return err
	}
	variant, err := articleSvc.GetMugVariant(ctx, variantID)
	if err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return newValidationError("variant not found")
		}
		return err
	}
	if variant.ArticleID != art.ID {
		return newValidationError("variant does not belong to article")
	}
	return nil
}

func validatePromptIfProvided(ctx context.Context, repo Repository, promptID *int) error {
	if promptID == nil {
		return nil
	}
	exists, err := repo.PromptExists(ctx, *promptID)
	if err != nil {
		return err
	}
	if !exists {
		return newValidationError("prompt not found")
	}
	return nil
}

func currentGrossPrice(ctx context.Context, articleSvc ArticleService, articleID int) (int, error) {
	cc, err := articleSvc.GetCostCalculation(ctx, articleID)
	if err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return 0, nil
		}
		return 0, err
	}
	if cc == nil {
		return 0, nil
	}
	return cc.SalesTotalGross, nil
}

func promptCurrentGrossPrice(ctx context.Context, repo Repository, articleSvc ArticleService, promptID int) (int, error) {
	p, err := repo.LoadPrompt(ctx, promptID)
	if err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return 0, nil
		}
		return 0, err
	}
	if p.Price != nil {
		return p.Price.SalesTotalGross, nil
	}
	if p.PriceID != nil {
		cc, err := articleSvc.GetCostCalculationByID(ctx, *p.PriceID)
		if err != nil {
			return 0, err
		}
		if cc != nil {
			return cc.SalesTotalGross, nil
		}
	}
	return 0, nil
}
