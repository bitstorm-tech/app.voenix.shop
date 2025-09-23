package order

import (
	"errors"
	"log"
	"net/http"
	"os"
	"strconv"
	"time"

	"github.com/gin-gonic/gin"
	"gorm.io/gorm"

	"voenix/backend/internal/article"
	"voenix/backend/internal/auth"
	"voenix/backend/internal/pdf"
)

type AddressRequest struct {
	StreetAddress1 string  `json:"streetAddress1" binding:"required"`
	StreetAddress2 *string `json:"streetAddress2"`
	City           string  `json:"city" binding:"required"`
	State          string  `json:"state" binding:"required"`
	PostalCode     string  `json:"postalCode" binding:"required"`
	Country        string  `json:"country" binding:"required"`
}

type AddressResponse struct {
	StreetAddress1 string  `json:"streetAddress1"`
	StreetAddress2 *string `json:"streetAddress2"`
	City           string  `json:"city"`
	State          string  `json:"state"`
	PostalCode     string  `json:"postalCode"`
	Country        string  `json:"country"`
}

type CreateOrderRequest struct {
	CustomerEmail        string          `json:"customerEmail" binding:"required,email"`
	CustomerFirstName    string          `json:"customerFirstName" binding:"required"`
	CustomerLastName     string          `json:"customerLastName" binding:"required"`
	CustomerPhone        *string         `json:"customerPhone"`
	ShippingAddress      AddressRequest  `json:"shippingAddress" binding:"required"`
	BillingAddress       *AddressRequest `json:"billingAddress"`
	UseShippingAsBilling *bool           `json:"useShippingAsBilling"`
	Notes                *string         `json:"notes"`
}

type OrderItemResponse struct {
	ID                     int64                   `json:"id"`
	Article                article.ArticleResponse `json:"article"`
	Variant                *article.MugVariant     `json:"variant"`
	Quantity               int                     `json:"quantity"`
	PricePerItem           int64                   `json:"pricePerItem"`
	TotalPrice             int64                   `json:"totalPrice"`
	GeneratedImageID       *int                    `json:"generatedImageId,omitempty"`
	GeneratedImageFilename *string                 `json:"generatedImageFilename,omitempty"`
	PromptID               *int                    `json:"promptId,omitempty"`
	CustomData             map[string]any          `json:"customData"`
	CreatedAt              time.Time               `json:"createdAt"`
}

type OrderResponse struct {
	ID              int64               `json:"id"`
	OrderNumber     string              `json:"orderNumber"`
	CustomerEmail   string              `json:"customerEmail"`
	CustomerFirst   string              `json:"customerFirstName"`
	CustomerLast    string              `json:"customerLastName"`
	CustomerPhone   *string             `json:"customerPhone,omitempty"`
	ShippingAddress AddressResponse     `json:"shippingAddress"`
	BillingAddress  *AddressResponse    `json:"billingAddress,omitempty"`
	Subtotal        int64               `json:"subtotal"`
	TaxAmount       int64               `json:"taxAmount"`
	ShippingAmount  int64               `json:"shippingAmount"`
	TotalAmount     int64               `json:"totalAmount"`
	Status          string              `json:"status"`
	CartID          int                 `json:"cartId"`
	Notes           *string             `json:"notes,omitempty"`
	Items           []OrderItemResponse `json:"items"`
	PDFURL          string              `json:"pdfUrl"`
	CreatedAt       time.Time           `json:"createdAt"`
	UpdatedAt       time.Time           `json:"updatedAt"`
}

type PaginatedResponse[T any] struct {
	Content       []T   `json:"content"`
	CurrentPage   int   `json:"currentPage"`
	TotalPages    int   `json:"totalPages"`
	TotalElements int64 `json:"totalElements"`
	Size          int   `json:"size"`
}

// RegisterRoutes mounts user order routes under /api/user.
func RegisterRoutes(r *gin.Engine, db *gorm.DB, svc *Service) {
	grp := r.Group("/api/user")
	grp.Use(auth.RequireRoles(db, "USER", "ADMIN"))

	grp.POST("/checkout", createOrderHandler(svc))
	grp.GET("/orders", listOrdersHandler(svc))
	grp.GET("/orders/:orderId", getOrderHandler(svc))
	grp.GET("/orders/:orderId/pdf", downloadOrderPDFHandler(svc))
}

func createOrderHandler(svc *Service) gin.HandlerFunc {
	return func(c *gin.Context) {
		u, ok := requireUser(c)
		if !ok {
			return
		}
		var req CreateOrderRequest
		if err := c.ShouldBindJSON(&req); err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Invalid request"})
			return
		}
		ord, err := svc.CreateOrderFromCart(c.Request.Context(), u.ID, req)
		if err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"detail": err.Error()})
			return
		}
		response, err := svc.BuildOrderResponse(c.Request.Context(), *ord, BaseURL())
		if err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to assemble order"})
			return
		}
		c.JSON(http.StatusCreated, response)
	}
}

func listOrdersHandler(svc *Service) gin.HandlerFunc {
	return func(c *gin.Context) {
		u, ok := requireUser(c)
		if !ok {
			return
		}
		page, _ := strconv.Atoi(c.Query("page"))
		size, _ := strconv.Atoi(c.Query("size"))
		pageResult, err := svc.ListOrders(c.Request.Context(), u.ID, page, size)
		if err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to list orders"})
			return
		}
		out := PaginatedResponse[OrderResponse]{
			Content:       make([]OrderResponse, 0, len(pageResult.Orders)),
			CurrentPage:   pageResult.CurrentPage,
			TotalPages:    pageResult.TotalPages,
			TotalElements: pageResult.TotalElements,
			Size:          pageResult.Size,
		}
		for _, ord := range pageResult.Orders {
			resp, err := svc.BuildOrderResponse(c.Request.Context(), ord, BaseURL())
			if err != nil {
				c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to assemble orders"})
				return
			}
			out.Content = append(out.Content, resp)
		}
		c.JSON(http.StatusOK, out)
	}
}

func getOrderHandler(svc *Service) gin.HandlerFunc {
	return func(c *gin.Context) {
		u, ok := requireUser(c)
		if !ok {
			return
		}
		orderIDParam := c.Param("orderId")
		orderID, parseErr := strconv.ParseInt(orderIDParam, 10, 64)
		if parseErr != nil {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Invalid order id"})
			return
		}
		ord, err := svc.GetOrder(c.Request.Context(), u.ID, orderID)
		if err != nil {
			if errors.Is(err, ErrNotFound) {
				c.JSON(http.StatusNotFound, gin.H{"detail": "Order not found"})
				return
			}
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to load order"})
			return
		}
		resp, err := svc.BuildOrderResponse(c.Request.Context(), *ord, BaseURL())
		if err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to assemble order"})
			return
		}
		c.JSON(http.StatusOK, resp)
	}
}

func downloadOrderPDFHandler(svc *Service) gin.HandlerFunc {
	return func(c *gin.Context) {
		u, ok := requireUser(c)
		if !ok {
			return
		}
		orderIDParam := c.Param("orderId")
		orderID, parseErr := strconv.ParseInt(orderIDParam, 10, 64)
		if parseErr != nil {
			c.JSON(http.StatusBadRequest, gin.H{"detail": "Invalid order id"})
			return
		}
		ord, err := svc.GetOrder(c.Request.Context(), u.ID, orderID)
		if err != nil {
			if errors.Is(err, ErrNotFound) {
				c.JSON(http.StatusNotFound, gin.H{"detail": "Order not found"})
				return
			}
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to load order"})
			return
		}
		dto, derr := svc.BuildOrderPDFData(c.Request.Context(), *ord)
		if derr != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to prepare PDF"})
			return
		}
		gen := pdf.NewService(pdf.Options{Config: pdf.DefaultConfig()})
		pdfBytes, gerr := gen.GenerateOrderPDF(dto)
		if gerr != nil || len(pdfBytes) == 0 {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to generate PDF"})
			return
		}
		filename := pdf.FilenameFromOrderNumber(ord.OrderNumber)

		configs, configErr := loadOrderPDFFTPConfigs(os.Getenv)
		if configErr != nil {
			if errors.Is(configErr, errOrderPDFFTPConfigMissing) {
				c.JSON(http.StatusInternalServerError, gin.H{"detail": "FTP configuration missing"})
				return
			}
			log.Printf("order pdf ftp configuration error: %v", configErr)
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Invalid FTP configuration"})
			return
		}

		for _, config := range configs {
			remotePath := remoteOrderPDFPath(config.Folder, filename)
			if err := uploadPDFToFTP(pdfBytes, config.Server, config.User, config.Password, config.options(remotePath)); err != nil {
				log.Printf("order pdf ftp upload failed for config %s at %s: %v", config.Name, remotePath, err)
				c.JSON(http.StatusBadGateway, gin.H{"detail": "Failed to upload order PDF"})
				return
			}
		}
		c.Header("Content-Disposition", "attachment; filename=\""+filename+"\"")
		c.Data(http.StatusOK, "application/pdf", pdfBytes)
	}
}

// helpers
func currentUser(c *gin.Context) *auth.User {
	uVal, _ := c.Get("currentUser")
	u, _ := uVal.(*auth.User)
	return u
}

func requireUser(c *gin.Context) (*auth.User, bool) {
	u := currentUser(c)
	if u == nil {
		c.JSON(http.StatusUnauthorized, gin.H{"detail": "Not authenticated"})
		return nil, false
	}
	return u, true
}
