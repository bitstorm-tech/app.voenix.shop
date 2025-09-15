package cart

import (
	"encoding/json"
	"errors"

	"gorm.io/gorm"

	"voenix/backend-go/internal/article"
	"voenix/backend-go/internal/prompt"
)

// Business/helpers

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

// Validation

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
