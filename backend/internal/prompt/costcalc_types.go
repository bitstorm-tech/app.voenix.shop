package prompt

// costCalculationRequest mirrors the article module's price fields and is reused
// for both request payloads and embedded reads in Prompt DTOs (cents).
type costCalculationRequest struct {
	PurchasePriceNet         int     `json:"purchasePriceNet"`
	PurchasePriceTax         int     `json:"purchasePriceTax"`
	PurchasePriceGross       int     `json:"purchasePriceGross"`
	PurchaseCostNet          int     `json:"purchaseCostNet"`
	PurchaseCostTax          int     `json:"purchaseCostTax"`
	PurchaseCostGross        int     `json:"purchaseCostGross"`
	PurchaseCostPercent      float64 `json:"purchaseCostPercent"`
	PurchaseTotalNet         int     `json:"purchaseTotalNet"`
	PurchaseTotalTax         int     `json:"purchaseTotalTax"`
	PurchaseTotalGross       int     `json:"purchaseTotalGross"`
	PurchasePriceUnit        string  `json:"purchasePriceUnit"`
	PurchaseVatRateId        *int    `json:"purchaseVatRateId"`
	PurchaseVatRatePercent   float64 `json:"purchaseVatRatePercent"`
	PurchaseCalculationMode  string  `json:"purchaseCalculationMode"`
	SalesVatRateId           *int    `json:"salesVatRateId"`
	SalesVatRatePercent      float64 `json:"salesVatRatePercent"`
	SalesMarginNet           int     `json:"salesMarginNet"`
	SalesMarginTax           int     `json:"salesMarginTax"`
	SalesMarginGross         int     `json:"salesMarginGross"`
	SalesMarginPercent       float64 `json:"salesMarginPercent"`
	SalesTotalNet            int     `json:"salesTotalNet"`
	SalesTotalTax            int     `json:"salesTotalTax"`
	SalesTotalGross          int     `json:"salesTotalGross"`
	SalesPriceUnit           string  `json:"salesPriceUnit"`
	SalesCalculationMode     string  `json:"salesCalculationMode"`
	PurchasePriceCorresponds *bool   `json:"purchasePriceCorresponds"`
	SalesPriceCorresponds    *bool   `json:"salesPriceCorresponds"`
	PurchaseActiveRow        string  `json:"purchaseActiveRow"`
	SalesActiveRow           string  `json:"salesActiveRow"`
}
