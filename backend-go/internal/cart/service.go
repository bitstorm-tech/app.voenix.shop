package cart

import (
    "encoding/json"
    "errors"
    "time"

    "gorm.io/gorm"

    "voenix/backend-go/internal/article"
    "voenix/backend-go/internal/prompt"
)

const defaultCartExpiryDays = 30

// getOrCreateActiveCart returns the active cart for user or creates one.
func getOrCreateActiveCart(db *gorm.DB, userID int) (*Cart, error) {
    var c Cart
    err := db.Preload("Items", withItemOrder).Where("user_id = ? AND status = ?", userID, string(CartStatusActive)).First(&c).Error
    if err == nil {
        return &c, nil
    }
    if !errors.Is(err, gorm.ErrRecordNotFound) {
        return nil, err
    }
    // Create new
    exp := time.Now().Add(defaultCartExpiryDays * 24 * time.Hour)
    c = Cart{UserID: userID, Status: string(CartStatusActive), ExpiresAt: &exp}
    if err := db.Create(&c).Error; err != nil {
        return nil, err
    }
    return &c, nil
}

// loadActiveCart loads the existing active cart for user (with items), or returns nil if not found.
func loadActiveCart(db *gorm.DB, userID int) (*Cart, error) {
    var c Cart
    err := db.Preload("Items", withItemOrder).Where("user_id = ? AND status = ?", userID, string(CartStatusActive)).First(&c).Error
    if err != nil {
        if errors.Is(err, gorm.ErrRecordNotFound) {
            return nil, nil
        }
        return nil, err
    }
    return &c, nil
}

// currentGrossPrice returns SalesTotalGross (cents) for articleID, or 0 if not found.
func currentGrossPrice(db *gorm.DB, articleID int) (int, error) {
    var cc article.CostCalculation
    if err := db.First(&cc, "article_id = ?", articleID).Error; err != nil {
        if errors.Is(err, gorm.ErrRecordNotFound) {
            return 0, nil
        }
        return 0, err
    }
    return cc.SalesTotalGross, nil
}

// validateArticleAndVariant ensures article and mug variant exist and match.
func validateArticleAndVariant(db *gorm.DB, articleID, variantID int) error {
    var a article.Article
    if err := db.First(&a, "id = ?", articleID).Error; err != nil {
        if errors.Is(err, gorm.ErrRecordNotFound) {
            return errors.New("article not found")
        }
        return err
    }
    var v article.MugVariant
    if err := db.First(&v, "id = ?", variantID).Error; err != nil {
        if errors.Is(err, gorm.ErrRecordNotFound) {
            return errors.New("variant not found")
        }
        return err
    }
    if v.ArticleID != a.ID {
        return errors.New("variant does not belong to article")
    }
    return nil
}

func validatePromptIfProvided(db *gorm.DB, promptID *int) error {
    if promptID == nil {
        return nil
    }
    var p prompt.Prompt
    if err := db.First(&p, "id = ?", *promptID).Error; err != nil {
        if errors.Is(err, gorm.ErrRecordNotFound) {
            return errors.New("prompt not found")
        }
        return err
    }
    return nil
}

// mergeOrAppendItem merges quantity if an item with same articleId, variantId and customData exists; otherwise appends.
func mergeOrAppendItem(c *Cart, item CartItem) {
    // Normalize custom data JSON once for stable comparisons
    item.CustomData = canonicalizeJSON(item.CustomData)
    for i := range c.Items {
        it := &c.Items[i]
        if it.ArticleID == item.ArticleID && it.VariantID == item.VariantID &&
            canonicalizeJSON(it.CustomData) == item.CustomData {
            it.Quantity += item.Quantity
            return
        }
    }
    // append as new
    item.Position = len(c.Items)
    c.Items = append(c.Items, item)
}

// deepEqualJSONValue compares primitive JSON-like values with limited recursion for maps.
// parseJSONMap safely parses a JSON object string to a map.
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

// canonicalizeJSON returns a stable JSON string for comparisons.
// It tolerates empty/invalid input by returning "{}".
func canonicalizeJSON(s string) string {
    m := parseJSONMap(s)
    b, err := json.Marshal(m)
    if err != nil {
        return "{}"
    }
    return string(b)
}

// withItemOrder applies a consistent ordering for preloading items.
func withItemOrder(tx *gorm.DB) *gorm.DB { return tx.Order("position asc, created_at asc") }
