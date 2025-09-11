package order

import (
	"crypto/rand"
	"encoding/hex"
	"encoding/json"
	"errors"
	"fmt"
	"os"
	"path/filepath"
	"strings"
	"time"

	"gorm.io/gorm"

	"voenix/backend-go/internal/article"
	"voenix/backend-go/internal/cart"
	img "voenix/backend-go/internal/image"
	"voenix/backend-go/internal/supplier"
)

const (
	taxRate           = 0.08
	shippingFlatCents = 499
)

// CreateOrderFromCart creates an order from the user's active cart.
func CreateOrderFromCart(db *gorm.DB, userID int, req CreateOrderRequest) (*Order, error) {
	// Validate cart exists and has items
	c, err := loadActiveCartForOrder(db, userID)
	if err != nil {
		return nil, err
	}
	if c == nil || len(c.Items) == 0 {
		return nil, fmt.Errorf("no active cart found or cart is empty")
	}
	// Ensure no existing order for this cart
	var count int64
	if err := db.Model(&Order{}).Where("cart_id = ?", c.ID).Count(&count).Error; err != nil {
		return nil, err
	}
	if count > 0 {
		return nil, fmt.Errorf("order already exists for cart: %d", c.ID)
	}

	// Totals
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

	// Build order entity
	ship := req.ShippingAddress
	bill := req.BillingAddress
	if (bill == nil) || (req.UseShippingAsBilling != nil && *req.UseShippingAsBilling) {
		bill = &ship
	}

	ord := &Order{
		ID:              newUUIDv4(),
		OrderNumber:     newOrderNumber(),
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

	// Items
	items := make([]OrderItem, 0, len(c.Items))
	for _, ci := range c.Items {
		cd := canonicalizeJSON(ci.CustomData)
		itm := OrderItem{
			ID:                 newUUIDv4(),
			OrderID:            ord.ID,
			ArticleID:          ci.ArticleID,
			VariantID:          ci.VariantID,
			Quantity:           ci.Quantity,
			PricePerItem:       int64(ci.PriceAtTime),
			TotalPrice:         int64(ci.PriceAtTime * ci.Quantity),
			GeneratedImageID:   ci.GeneratedImageID,
			GeneratedImageFile: nil,
			PromptID:           ci.PromptID,
			CustomData:         cd,
		}
		items = append(items, itm)
	}

	// Transaction: save order + items, mark cart converted
	err = db.Transaction(func(tx *gorm.DB) error {
		if err := tx.Create(ord).Error; err != nil {
			return err
		}
		for i := range items {
			if err := tx.Create(&items[i]).Error; err != nil {
				return err
			}
		}
		// Mark cart as converted
		if err := tx.Model(c).Update("status", string(cart.CartStatusConverted)).Error; err != nil {
			return err
		}
		return nil
	})
	if err != nil {
		return nil, err
	}

	// Reload with items
	if err := db.Preload("Items").First(ord, "id = ?", ord.ID).Error; err != nil {
		return nil, err
	}
	return ord, nil
}

// FindOrder ensures order belongs to user and returns it with items.
func FindOrder(db *gorm.DB, userID int, orderID string) (*Order, error) {
	var o Order
	if err := db.Preload("Items").First(&o, "id = ? AND user_id = ?", orderID, userID).Error; err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, fmt.Errorf("order not found")
		}
		return nil, err
	}
	return &o, nil
}

// ListOrders returns a page of orders (newest first) for user.
func ListOrders(db *gorm.DB, userID int, page, size int) (PaginatedResponse[Order], error) {
	if size <= 0 {
		size = 20
	}
	if page < 0 {
		page = 0
	}
	var total int64
	if err := db.Model(&Order{}).Where("user_id = ?", userID).Count(&total).Error; err != nil {
		return PaginatedResponse[Order]{}, err
	}
	var orders []Order
	if err := db.Preload("Items").Where("user_id = ?", userID).Order("created_at desc").Limit(size).Offset(page * size).Find(&orders).Error; err != nil {
		return PaginatedResponse[Order]{}, err
	}
	tp := int((total + int64(size) - 1) / int64(size))
	return PaginatedResponse[Order]{
		Content:       orders,
		CurrentPage:   page,
		TotalPages:    tp,
		TotalElements: total,
		Size:          size,
	}, nil
}

// Mapping

func toAddressDtoFromOrder(o *Order) (AddressDto, *AddressDto) {
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

func toOrderDto(db *gorm.DB, o *Order, baseURL string) (OrderDto, error) {
	ship, bill := toAddressDtoFromOrder(o)
	items := make([]OrderItemDto, 0, len(o.Items))
	for i := range o.Items {
		it := o.Items[i]
		art, err := loadArticleRead(db, it.ArticleID)
		if err != nil {
			return OrderDto{}, err
		}
		mv, _ := loadMugVariantDto(db, it.VariantID)
		items = append(items, OrderItemDto{
			ID:                     it.ID,
			Article:                art,
			Variant:                mv,
			Quantity:               it.Quantity,
			PricePerItem:           it.PricePerItem,
			TotalPrice:             it.TotalPrice,
			GeneratedImageID:       it.GeneratedImageID,
			GeneratedImageFilename: it.GeneratedImageFile,
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

// Helpers reusing cart package utilities
// loadActiveCartForOrder replicates cart.loadActiveCart with items preload
func loadActiveCartForOrder(db *gorm.DB, userID int) (*cart.Cart, error) {
	var c cart.Cart
	err := db.Preload("Items", func(tx *gorm.DB) *gorm.DB { return tx.Order("position asc, created_at asc") }).
		Where("user_id = ? AND status = ?", userID, string(cart.CartStatusActive)).
		First(&c).Error
	if err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, nil
		}
		return nil, err
	}
	return &c, nil
}

// loadArticleRead replicates cart.loadArticleRead to avoid cross-package exposure.
func loadArticleRead(db *gorm.DB, id int) (article.ArticleRead, error) {
	var a article.Article
	if err := db.First(&a, "id = ?", id).Error; err != nil {
		return article.ArticleRead{}, err
	}
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
		CreatedAt:             &a.CreatedAt,
		UpdatedAt:             &a.UpdatedAt,
	}
	return out, nil
}

// loadMugVariantDto replicates cart.loadMugVariantDto
func loadMugVariantDto(db *gorm.DB, id int) (*cart.MugVariantDto, error) {
	var v article.MugVariant
	if err := db.First(&v, "id = ?", id).Error; err != nil {
		return nil, err
	}
	url := publicMugVariantExampleURL(v.ExampleImageFilename)
	return &cart.MugVariantDto{
		ID:                    v.ID,
		ArticleID:             v.ArticleID,
		ColorCode:             v.OutsideColorCode,
		ExampleImageURL:       strPtr(url),
		SupplierArticleNumber: v.ArticleVariantNumber,
		IsDefault:             v.IsDefault,
		ExampleImageFilename:  v.ExampleImageFilename,
	}, nil
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

func strPtr(s string) *string {
	if s == "" {
		return nil
	}
	return &s
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

// UUID and order number generators without external deps
func newUUIDv4() string {
	b := make([]byte, 16)
	_, _ = rand.Read(b)
	// Set version (4) and variant (RFC 4122)
	b[6] = (b[6] & 0x0f) | 0x40
	b[8] = (b[8] & 0x3f) | 0x80
	// Format 8-4-4-4-12
	hexs := hex.EncodeToString(b)
	return fmt.Sprintf("%s-%s-%s-%s-%s", hexs[0:8], hexs[8:12], hexs[12:16], hexs[16:20], hexs[20:32])
}

func newOrderNumber() string {
	ts := time.Now().UTC().Format("20060102")
	r := make([]byte, 3)
	_, _ = rand.Read(r)
	return fmt.Sprintf("ORD-%s-%s", ts, strings.ToUpper(hex.EncodeToString(r)))
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
	// Default to Go server http port
	return "http://localhost:8081"
}
