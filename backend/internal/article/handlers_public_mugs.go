package article

import (
	"net/http"
	"time"

	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
)

// Responses for public mug endpoints
type publicMugVariantResponse struct {
	ID                   int        `json:"id"`
	MugID                int        `json:"mugId"`
	ColorCode            string     `json:"colorCode"`
	Name                 string     `json:"name"`
	ExampleImageURL      *string    `json:"exampleImageUrl"`
	ArticleVariantNumber *string    `json:"articleVariantNumber"`
	IsDefault            bool       `json:"isDefault"`
	Active               bool       `json:"active"`
	ExampleImageFilename *string    `json:"exampleImageFilename"`
	CreatedAt            *time.Time `json:"createdAt"`
	UpdatedAt            *time.Time `json:"updatedAt"`
}

type publicMugResponse struct {
	ID                    int                        `json:"id"`
	Name                  string                     `json:"name"`
	Price                 float64                    `json:"price"`
	Image                 *string                    `json:"image"`
	FillingQuantity       *string                    `json:"fillingQuantity"`
	DescriptionShort      *string                    `json:"descriptionShort"`
	DescriptionLong       *string                    `json:"descriptionLong"`
	HeightMm              int                        `json:"heightMm"`
	DiameterMm            int                        `json:"diameterMm"`
	PrintTemplateWidthMm  int                        `json:"printTemplateWidthMm"`
	PrintTemplateHeightMm int                        `json:"printTemplateHeightMm"`
	DishwasherSafe        bool                       `json:"dishwasherSafe"`
	Variants              []publicMugVariantResponse `json:"variants"`
}

func registerPublicMugRoutes(r *gin.Engine, db *gorm.DB) {
	grp := r.Group("/api/mugs")

	grp.GET("", func(c *gin.Context) {
		var mugs []Article
		if err := db.Where("article_type = ? AND active = ?", ArticleTypeMug, true).Order("id desc").Find(&mugs).Error; err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to fetch mugs"})
			return
		}
		out := make([]publicMugResponse, 0, len(mugs))
		for i := range mugs {
			a := mugs[i]
			// Load mug details
			var md MugDetails
			if err := db.First(&md, "article_id = ?", a.ID).Error; err != nil {
				// skip mugs without details
				continue
			}
			// Load only active variants
			var vs []MugVariant
			_ = db.Where("article_id = ? AND active = ?", a.ID, true).Order("id asc").Find(&vs).Error
			def := ""
			if dv := defaultMugVariant(vs); dv != nil {
				def = publicMugVariantExampleURL(dv.ExampleImageFilename)
			}
			// price in euros: salesTotalGross is in cents
			var cc CostCalculation
			_ = db.First(&cc, "article_id = ?", a.ID).Error
			price := 0.0
			if cc.ID != 0 {
				price = float64(cc.SalesTotalGross) / 100.0
			}
			// variants mapping
			variants := make([]publicMugVariantResponse, 0, len(vs))
			for j := range vs {
				v := vs[j]
				variants = append(variants, publicMugVariantResponse{
					ID:                   v.ID,
					MugID:                a.ID,
					ColorCode:            v.OutsideColorCode,
					Name:                 v.Name,
					ExampleImageURL:      strPtrOrNil(publicMugVariantExampleURL(v.ExampleImageFilename)),
					ArticleVariantNumber: v.ArticleVariantNumber,
					IsDefault:            v.IsDefault,
					Active:               v.Active,
					ExampleImageFilename: v.ExampleImageFilename,
					CreatedAt:            timePtr(v.CreatedAt),
					UpdatedAt:            timePtr(v.UpdatedAt),
				})
			}
			imageURL := def
			out = append(out, publicMugResponse{
				ID:                    a.ID,
				Name:                  a.Name,
				Price:                 price,
				Image:                 strPtrOrNil(imageURL),
				FillingQuantity:       md.FillingQuantity,
				DescriptionShort:      &a.DescriptionShort,
				DescriptionLong:       &a.DescriptionLong,
				HeightMm:              md.HeightMm,
				DiameterMm:            md.DiameterMm,
				PrintTemplateWidthMm:  md.PrintTemplateWidthMm,
				PrintTemplateHeightMm: md.PrintTemplateHeightMm,
				DishwasherSafe:        md.DishwasherSafe,
				Variants:              variants,
			})
		}
		c.JSON(http.StatusOK, out)
	})
}
