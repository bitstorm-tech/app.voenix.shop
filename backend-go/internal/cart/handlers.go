package cart

import (
    "fmt"
    "net/http"
    "path/filepath"
    "time"
    "encoding/json"

    "github.com/gin-gonic/gin"
    "gorm.io/gorm"

    "voenix/backend-go/internal/article"
    "voenix/backend-go/internal/auth"
    img "voenix/backend-go/internal/image"
    "voenix/backend-go/internal/supplier"
)

// RegisterRoutes mounts user cart routes under /api/user/cart
func RegisterRoutes(r *gin.Engine, db *gorm.DB) {
    grp := r.Group("/api/user/cart")
    grp.Use(auth.RequireRoles(db, "USER", "ADMIN"))

    grp.GET("/", func(c *gin.Context) {
        u := currentUser(c)
        if u == nil {
            c.JSON(http.StatusUnauthorized, gin.H{"detail": "Not authenticated"})
            return
        }
        cart, err := getOrCreateActiveCart(db, u.ID)
        if err != nil {
            c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to load cart"})
            return
        }
        dto, err := assembleCartDto(db, cart)
        if err != nil {
            c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to assemble cart"})
            return
        }
        c.JSON(http.StatusOK, dto)
    })

    grp.GET("/summary", func(c *gin.Context) {
        u := currentUser(c)
        if u == nil {
            c.JSON(http.StatusUnauthorized, gin.H{"detail": "Not authenticated"})
            return
        }
        cart, err := loadActiveCart(db, u.ID)
        if err != nil {
            c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to load cart"})
            return
        }
        if cart == nil || len(cart.Items) == 0 {
            c.JSON(http.StatusOK, CartSummaryDto{ItemCount: 0, TotalPrice: 0, HasItems: false})
            return
        }
        itemCount := 0
        total := 0
        for _, it := range cart.Items {
            itemCount += it.Quantity
            total += it.PriceAtTime * it.Quantity
        }
        c.JSON(http.StatusOK, CartSummaryDto{ItemCount: itemCount, TotalPrice: total, HasItems: itemCount > 0})
    })

    grp.POST("/items", func(c *gin.Context) {
        u := currentUser(c)
        if u == nil {
            c.JSON(http.StatusUnauthorized, gin.H{"detail": "Not authenticated"})
            return
        }
        var req AddToCartRequest
        if err := c.ShouldBindJSON(&req); err != nil {
            c.JSON(http.StatusBadRequest, gin.H{"detail": "Invalid request"})
            return
        }
        if req.Quantity <= 0 {
            req.Quantity = 1
        }
        if req.CustomData == nil {
            req.CustomData = map[string]any{}
        }
        if err := validateArticleAndVariant(db, req.ArticleID, req.VariantID); err != nil {
            c.JSON(http.StatusBadRequest, gin.H{"detail": err.Error()})
            return
        }
        if err := validatePromptIfProvided(db, req.PromptID); err != nil {
            c.JSON(http.StatusBadRequest, gin.H{"detail": err.Error()})
            return
        }
        cart, err := getOrCreateActiveCart(db, u.ID)
        if err != nil {
            c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to load cart"})
            return
        }
        // price at time
        price, err := currentGrossPrice(db, req.ArticleID)
        if err != nil {
            c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to fetch price"})
            return
        }
        // marshal custom data to JSON string for storage
        cdStr := "{}"
        if req.CustomData != nil {
            if b, err := json.Marshal(req.CustomData); err == nil {
                cdStr = string(b)
            }
        }
        item := CartItem{
            CartID:           cart.ID,
            ArticleID:        req.ArticleID,
            VariantID:        req.VariantID,
            Quantity:         req.Quantity,
            PriceAtTime:      price,
            OriginalPrice:    price,
            CustomData:       cdStr,
            GeneratedImageID: req.GeneratedImageID,
            PromptID:         req.PromptID,
        }
        mergeOrAppendItem(cart, item)
        if err := db.Save(cart).Error; err != nil {
            c.JSON(http.StatusBadRequest, gin.H{"detail": "Cart update failed"})
            return
        }
        // reload items in order
        if err := db.Preload("Items", func(tx *gorm.DB) *gorm.DB { return tx.Order("position asc, created_at asc") }).First(cart, cart.ID).Error; err != nil {
            // continue with existing in-memory items
        }
        dto, err := assembleCartDto(db, cart)
        if err != nil {
            c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to assemble cart"})
            return
        }
        c.JSON(http.StatusCreated, dto)
    })

    grp.PUT("/items/:itemId", func(c *gin.Context) {
        u := currentUser(c)
        if u == nil {
            c.JSON(http.StatusUnauthorized, gin.H{"detail": "Not authenticated"})
            return
        }
        var itemID int
        if _, err := fmt.Sscan(c.Param("itemId"), &itemID); err != nil {
            c.JSON(http.StatusBadRequest, gin.H{"detail": "Invalid item id"})
            return
        }
        var req UpdateCartItemRequest
        if err := c.ShouldBindJSON(&req); err != nil {
            c.JSON(http.StatusBadRequest, gin.H{"detail": "Invalid request"})
            return
        }
        if req.Quantity <= 0 {
            c.JSON(http.StatusBadRequest, gin.H{"detail": "Quantity must be at least 1"})
            return
        }
        cart, err := loadActiveCart(db, u.ID)
        if err != nil {
            c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to load cart"})
            return
        }
        if cart == nil {
            c.JSON(http.StatusNotFound, gin.H{"detail": "Active cart not found"})
            return
        }
        found := false
        for i := range cart.Items {
            if cart.Items[i].ID == itemID {
                cart.Items[i].Quantity = req.Quantity
                found = true
                break
            }
        }
        if !found {
            c.JSON(http.StatusNotFound, gin.H{"detail": "Cart item not found"})
            return
        }
        if err := db.Save(cart).Error; err != nil {
            c.JSON(http.StatusBadRequest, gin.H{"detail": "Cart update failed"})
            return
        }
        // reload
        _ = db.Preload("Items", func(tx *gorm.DB) *gorm.DB { return tx.Order("position asc, created_at asc") }).First(cart, cart.ID).Error
        dto, err := assembleCartDto(db, cart)
        if err != nil {
            c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to assemble cart"})
            return
        }
        c.JSON(http.StatusOK, dto)
    })

    grp.DELETE("/items/:itemId", func(c *gin.Context) {
        u := currentUser(c)
        if u == nil {
            c.JSON(http.StatusUnauthorized, gin.H{"detail": "Not authenticated"})
            return
        }
        var itemID int
        if _, err := fmt.Sscan(c.Param("itemId"), &itemID); err != nil {
            c.JSON(http.StatusBadRequest, gin.H{"detail": "Invalid item id"})
            return
        }
        cart, err := loadActiveCart(db, u.ID)
        if err != nil {
            c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to load cart"})
            return
        }
        if cart == nil {
            c.JSON(http.StatusNotFound, gin.H{"detail": "Active cart not found"})
            return
        }
        // delete item
        removed := false
        for i := range cart.Items {
            if cart.Items[i].ID == itemID {
                // delete via DB to keep FKs and positions clean
                if err := db.Delete(&CartItem{}, cart.Items[i].ID).Error; err != nil {
                    c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to remove item"})
                    return
                }
                removed = true
                break
            }
        }
        if !removed {
            c.JSON(http.StatusNotFound, gin.H{"detail": "Cart item not found"})
            return
        }
        // Reorder positions
        var items []CartItem
        _ = db.Where("cart_id = ?", cart.ID).Order("position asc, created_at asc").Find(&items).Error
        for idx := range items {
            if items[idx].Position != idx {
                items[idx].Position = idx
                _ = db.Model(&items[idx]).Update("position", idx).Error
            }
        }
        // reload
        _ = db.Preload("Items", func(tx *gorm.DB) *gorm.DB { return tx.Order("position asc, created_at asc") }).First(cart, cart.ID).Error
        dto, err := assembleCartDto(db, cart)
        if err != nil {
            c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to assemble cart"})
            return
        }
        c.JSON(http.StatusOK, dto)
    })

    grp.DELETE("/", func(c *gin.Context) {
        u := currentUser(c)
        if u == nil {
            c.JSON(http.StatusUnauthorized, gin.H{"detail": "Not authenticated"})
            return
        }
        cart, err := loadActiveCart(db, u.ID)
        if err != nil {
            c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to load cart"})
            return
        }
        if cart == nil {
            c.JSON(http.StatusNotFound, gin.H{"detail": "Active cart not found"})
            return
        }
        // delete all items
        if err := db.Where("cart_id = ?", cart.ID).Delete(&CartItem{}).Error; err != nil {
            c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to clear cart"})
            return
        }
        // reload empty cart
        _ = db.Preload("Items").First(cart, cart.ID).Error
        dto, err := assembleCartDto(db, cart)
        if err != nil {
            c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to assemble cart"})
            return
        }
        c.JSON(http.StatusOK, dto)
    })

    grp.POST("/refresh-prices", func(c *gin.Context) {
        u := currentUser(c)
        if u == nil {
            c.JSON(http.StatusUnauthorized, gin.H{"detail": "Not authenticated"})
            return
        }
        cart, err := loadActiveCart(db, u.ID)
        if err != nil {
            c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to load cart"})
            return
        }
        if cart == nil {
            c.JSON(http.StatusNotFound, gin.H{"detail": "Active cart not found"})
            return
        }
        // Update OriginalPrice to current for each item
        changed := false
        for i := range cart.Items {
            cur, err := currentGrossPrice(db, cart.Items[i].ArticleID)
            if err != nil {
                c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to refresh prices"})
                return
            }
            if cart.Items[i].OriginalPrice != cur {
                cart.Items[i].OriginalPrice = cur
                if err := db.Model(&cart.Items[i]).Update("original_price", cur).Error; err != nil {
                    c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to update prices"})
                    return
                }
                changed = true
            }
        }
        if changed {
            _ = db.Preload("Items", func(tx *gorm.DB) *gorm.DB { return tx.Order("position asc, created_at asc") }).First(cart, cart.ID).Error
        }
        dto, err := assembleCartDto(db, cart)
        if err != nil {
            c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to assemble cart"})
            return
        }
        c.JSON(http.StatusOK, dto)
    })
}

func currentUser(c *gin.Context) *auth.User {
    uVal, _ := c.Get("currentUser")
    u, _ := uVal.(*auth.User)
    return u
}

// assembleCartDto converts a Cart+Items into a response DTO.
func assembleCartDto(db *gorm.DB, c *Cart) (*CartDto, error) {
    items := make([]CartItemDto, 0, len(c.Items))
    totalCount := 0
    totalPrice := 0
    for i := range c.Items {
        ci := c.Items[i]
        art, err := loadArticleRead(db, ci.ArticleID)
        if err != nil {
            return nil, err
        }
        mv, _ := loadMugVariantDto(db, ci.VariantID)
        cd := parseJSONMap(ci.CustomData)
        item := CartItemDto{
            ID:              ci.ID,
            Article:         art,
            Variant:         mv,
            Quantity:        ci.Quantity,
            PriceAtTime:     ci.PriceAtTime,
            OriginalPrice:   ci.OriginalPrice,
            HasPriceChanged: ci.PriceAtTime != ci.OriginalPrice,
            TotalPrice:      ci.PriceAtTime * ci.Quantity,
            CustomData:      cd,
            GeneratedImageID: ci.GeneratedImageID,
            // We don't have a DB of user images to map id->filename in Go backend
            GeneratedImageFilename: nil,
            PromptID:        ci.PromptID,
            Position:        ci.Position,
            CreatedAt:       ci.CreatedAt,
            UpdatedAt:       ci.UpdatedAt,
        }
        items = append(items, item)
        totalCount += ci.Quantity
        totalPrice += item.TotalPrice
    }
    dto := &CartDto{
        ID:             c.ID,
        UserID:         c.UserID,
        Status:         c.Status,
        Version:        c.Version,
        ExpiresAt:      c.ExpiresAt,
        Items:          items,
        TotalItemCount: totalCount,
        TotalPrice:     totalPrice,
        IsEmpty:        len(items) == 0,
        CreatedAt:      c.CreatedAt,
        UpdatedAt:      c.UpdatedAt,
    }
    return dto, nil
}

// loadArticleRead produces article.ArticleRead for the given ID.
func loadArticleRead(db *gorm.DB, id int) (article.ArticleRead, error) {
    var a article.Article
    if err := db.First(&a, "id = ?", id).Error; err != nil {
        return article.ArticleRead{}, err
    }
    // fetch names
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
        CreatedAt:             timePtr(a.CreatedAt),
        UpdatedAt:             timePtr(a.UpdatedAt),
    }
    return out, nil
}

// loadMugVariantDto builds a simplified variant DTO for cart.
func loadMugVariantDto(db *gorm.DB, id int) (*MugVariantDto, error) {
    var v article.MugVariant
    if err := db.First(&v, "id = ?", id).Error; err != nil {
        return nil, err
    }
    url := publicMugVariantExampleURL(v.ExampleImageFilename)
    return &MugVariantDto{
        ID:                    v.ID,
        ArticleID:             v.ArticleID,
        ColorCode:             v.OutsideColorCode,
        ExampleImageURL:       strPtrOrNil(url),
        SupplierArticleNumber: v.ArticleVariantNumber,
        IsDefault:             v.IsDefault,
        ExampleImageFilename:  v.ExampleImageFilename,
    }, nil
}

// Helpers copied inline to avoid cross-package dependency on private funcs.
func timePtr(t time.Time) *time.Time { return &t }
func strPtrOrNil(s string) *string {
    if s == "" {
        return nil
    }
    return &s
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

// no-op
