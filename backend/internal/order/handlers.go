package order

import (
	"errors"
	"log"
	"net/http"
	"os"
	"strconv"

	"github.com/gin-gonic/gin"
	"gorm.io/gorm"

	"voenix/backend/internal/auth"
	"voenix/backend/internal/pdf"
)

// RegisterRoutes mounts user order routes under /api/user
func RegisterRoutes(r *gin.Engine, db *gorm.DB) {
	grp := r.Group("/api/user")
	grp.Use(auth.RequireRoles(db, "USER", "ADMIN"))

	grp.POST("/checkout", createOrderHandler(db))
	grp.GET("/orders", listOrdersHandler(db))
	grp.GET("/orders/:orderId", getOrderHandler(db))
	grp.GET("/orders/:orderId/pdf", downloadOrderPDFHandler(db))
}

func createOrderHandler(db *gorm.DB) gin.HandlerFunc {
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
		ord, err := CreateOrderFromCart(db, u.ID, req)
		if err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"detail": err.Error()})
			return
		}
		dto, err := toOrderDto(db, ord, BaseURL())
		if err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to assemble order"})
			return
		}
		c.JSON(http.StatusCreated, dto)
	}
}

func listOrdersHandler(db *gorm.DB) gin.HandlerFunc {
	return func(c *gin.Context) {
		u, ok := requireUser(c)
		if !ok {
			return
		}
		page, _ := strconv.Atoi(c.Query("page"))
		size, _ := strconv.Atoi(c.Query("size"))
		pageResult, err := ListOrders(db, u.ID, page, size)
		if err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to list orders"})
			return
		}
		// Map each order to DTO lazily
		out := PaginatedResponse[OrderDto]{
			Content:       make([]OrderDto, 0, len(pageResult.Content)),
			CurrentPage:   pageResult.CurrentPage,
			TotalPages:    pageResult.TotalPages,
			TotalElements: pageResult.TotalElements,
			Size:          pageResult.Size,
		}
		for i := range pageResult.Content {
			dto, err := toOrderDto(db, &pageResult.Content[i], BaseURL())
			if err != nil {
				c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to assemble orders"})
				return
			}
			out.Content = append(out.Content, dto)
		}
		c.JSON(http.StatusOK, out)
	}
}

func getOrderHandler(db *gorm.DB) gin.HandlerFunc {
	return func(c *gin.Context) {
		u, ok := requireUser(c)
		if !ok {
			return
		}
		orderID := c.Param("orderId")
		ord, err := FindOrder(db, u.ID, orderID)
		if err != nil {
			c.JSON(http.StatusNotFound, gin.H{"detail": "Order not found"})
			return
		}
		dto, err := toOrderDto(db, ord, BaseURL())
		if err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to assemble order"})
			return
		}
		c.JSON(http.StatusOK, dto)
	}
}

func downloadOrderPDFHandler(db *gorm.DB) gin.HandlerFunc {
	return func(c *gin.Context) {
		u, ok := requireUser(c)
		if !ok {
			return
		}
		orderID := c.Param("orderId")
		ord, err := FindOrder(db, u.ID, orderID)
		if err != nil {
			c.JSON(http.StatusNotFound, gin.H{"detail": "Order not found"})
			return
		}
		// Build DTO for pdf module
		dto, derr := buildOrderPdfData(db, ord)
		if derr != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to prepare PDF"})
			return
		}
		// Generate PDF using UniPDF
		gen := pdf.NewService(pdf.Options{Config: pdf.DefaultConfig()})
		pdfBytes, gerr := gen.GenerateOrderPDF(dto)
		if gerr != nil || len(pdfBytes) == 0 {
			// Fail the request when generation fails; no fallback
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to generate PDF"})
			return
		}
		filename := pdf.FilenameFromOrderNumber(ord.OrderNumber)

		cfg, cfgErr := loadOrderPDFFTPConfig(os.Getenv)
		if cfgErr != nil {
			if errors.Is(cfgErr, errOrderPDFFTPConfigMissing) {
				c.JSON(http.StatusInternalServerError, gin.H{"detail": "FTP configuration missing"})
				return
			}
			log.Printf("order pdf ftp configuration error: %v", cfgErr)
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Invalid FTP configuration"})
			return
		}

		remotePath := remoteOrderPDFPath(cfg.Folder, filename)
		if err := uploadPDFToFTP(pdfBytes, cfg.Server, cfg.User, cfg.Password, cfg.options(remotePath)); err != nil {
			log.Printf("order pdf ftp upload failed for %s: %v", remotePath, err)
			c.JSON(http.StatusBadGateway, gin.H{"detail": "Failed to upload order PDF"})
			return
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
