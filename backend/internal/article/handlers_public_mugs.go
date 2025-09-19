package article

import (
	"net/http"
	"time"

	"github.com/gin-gonic/gin"
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

func registerPublicMugRoutes(r *gin.Engine, svc *Service) {
	grp := r.Group("/api/mugs")

	grp.GET("", func(c *gin.Context) {
		mugs, err := svc.ListMugArticles(c.Request.Context(), true, nil)
		if err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to fetch mugs"})
			return
		}
		out := make([]publicMugResponse, 0, len(mugs))
		for i := range mugs {
			a := mugs[i]
			md, err := svc.GetMugDetails(c.Request.Context(), a.ID)
			if err != nil {
				c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to fetch mug details"})
				return
			}
			if md == nil {
				continue
			}
			vs, err := svc.ListMugVariants(c.Request.Context(), a.ID, true)
			if err != nil {
				c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to fetch variants"})
				return
			}
			def := ""
			if dv := defaultMugVariant(vs); dv != nil {
				def = publicMugVariantExampleURL(dv.ExampleImageFilename)
			}
			calc, err := svc.GetCostCalculation(c.Request.Context(), a.ID)
			if err != nil {
				c.JSON(http.StatusInternalServerError, gin.H{"detail": "Failed to fetch pricing"})
				return
			}
			price := 0.0
			if calc != nil && calc.SalesTotalGross != 0 {
				price = float64(calc.SalesTotalGross) / 100.0
			}
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
			out = append(out, publicMugResponse{
				ID:                    a.ID,
				Name:                  a.Name,
				Price:                 price,
				Image:                 strPtrOrNil(def),
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
