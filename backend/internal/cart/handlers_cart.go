package cart

import (
	"errors"
	"net/http"
	"time"

	"github.com/gin-gonic/gin"

	"voenix/backend/internal/article"
)

type cartSummaryResponse struct {
	ItemCount  int  `json:"itemCount"`
	TotalPrice int  `json:"totalPrice"`
	HasItems   bool `json:"hasItems"`
}

type MugVariantResponse struct {
	ID                    int     `json:"id"`
	ArticleID             int     `json:"articleId"`
	ColorCode             string  `json:"colorCode"`
	ExampleImageURL       *string `json:"exampleImageUrl"`
	SupplierArticleNumber *string `json:"supplierArticleNumber"`
	IsDefault             bool    `json:"isDefault"`
	ExampleImageFilename  *string `json:"exampleImageFilename"`
}

type CartItemResponse struct {
	ID                     int                     `json:"id"`
	Article                article.ArticleResponse `json:"article"`
	Variant                *MugVariantResponse     `json:"variant"`
	Quantity               int                     `json:"quantity"`
	PriceAtTime            int                     `json:"priceAtTime"`
	OriginalPrice          int                     `json:"originalPrice"`
	ArticlePriceAtTime     int                     `json:"articlePriceAtTime"`
	PromptPriceAtTime      int                     `json:"promptPriceAtTime"`
	ArticleOriginalPrice   int                     `json:"articleOriginalPrice"`
	PromptOriginalPrice    int                     `json:"promptOriginalPrice"`
	HasPriceChanged        bool                    `json:"hasPriceChanged"`
	HasPromptPriceChanged  bool                    `json:"hasPromptPriceChanged"`
	TotalPrice             int                     `json:"totalPrice"`
	CustomData             map[string]any          `json:"customData"`
	GeneratedImageID       *int                    `json:"generatedImageId"`
	GeneratedImageFilename *string                 `json:"generatedImageFilename"`
	PromptID               *int                    `json:"promptId"`
	PromptTitle            *string                 `json:"promptTitle,omitempty"`
	Position               int                     `json:"position"`
	CreatedAt              time.Time               `json:"createdAt"`
	UpdatedAt              time.Time               `json:"updatedAt"`
}

type CartResponse struct {
	ID             int                `json:"id"`
	UserID         int                `json:"userId"`
	Status         string             `json:"status"`
	Version        int64              `json:"version"`
	ExpiresAt      *time.Time         `json:"expiresAt"`
	Items          []CartItemResponse `json:"items"`
	TotalItemCount int                `json:"totalItemCount"`
	TotalPrice     int                `json:"totalPrice"`
	IsEmpty        bool               `json:"isEmpty"`
	CreatedAt      time.Time          `json:"createdAt"`
	UpdatedAt      time.Time          `json:"updatedAt"`
}

func getCartHandler(svc *Service) gin.HandlerFunc {
	return func(c *gin.Context) {
		u, ok := requireUser(c)
		if !ok {
			return
		}
		detail, err := svc.GetCart(c.Request.Context(), u.ID)
		if err != nil {
			writeServiceError(c, err)
			return
		}
		dto, err := svc.ToCartResponse(c.Request.Context(), detail)
		if err != nil {
			writeServiceError(c, err)
			return
		}
		c.JSON(http.StatusOK, dto)
	}
}

func getCartSummaryHandler(svc *Service) gin.HandlerFunc {
	return func(c *gin.Context) {
		u, ok := requireUser(c)
		if !ok {
			return
		}
		summary, err := svc.GetCartSummary(c.Request.Context(), u.ID)
		if err != nil {
			writeServiceError(c, err)
			return
		}
		c.JSON(http.StatusOK, cartSummaryResponse(summary))
	}
}

func clearCartHandler(svc *Service) gin.HandlerFunc {
	return func(c *gin.Context) {
		u, ok := requireUser(c)
		if !ok {
			return
		}
		detail, err := svc.ClearCart(c.Request.Context(), u.ID)
		if err != nil {
			writeServiceError(c, err)
			return
		}
		dto, err := svc.ToCartResponse(c.Request.Context(), detail)
		if err != nil {
			writeServiceError(c, err)
			return
		}
		c.JSON(http.StatusOK, dto)
	}
}

func refreshPricesHandler(svc *Service) gin.HandlerFunc {
	return func(c *gin.Context) {
		u, ok := requireUser(c)
		if !ok {
			return
		}
		detail, err := svc.RefreshPrices(c.Request.Context(), u.ID)
		if err != nil {
			writeServiceError(c, err)
			return
		}
		dto, err := svc.ToCartResponse(c.Request.Context(), detail)
		if err != nil {
			writeServiceError(c, err)
			return
		}
		c.JSON(http.StatusOK, dto)
	}
}

func writeServiceError(c *gin.Context, err error) {
	switch {
	case errors.Is(err, ErrCartNotFound):
		c.JSON(http.StatusNotFound, gin.H{"detail": ErrCartNotFound.Error()})
	case errors.Is(err, ErrCartItemNotFound):
		c.JSON(http.StatusNotFound, gin.H{"detail": ErrCartItemNotFound.Error()})
	case isValidationError(err):
		c.JSON(http.StatusBadRequest, gin.H{"detail": err.Error()})
	default:
		c.JSON(http.StatusInternalServerError, gin.H{"detail": "Unexpected error"})
	}
}
