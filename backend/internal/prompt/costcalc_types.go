package prompt

import (
	"encoding/json"
	"fmt"
	"strings"
)

// costCalculationRequest mirrors the article module's price fields and is reused
// for both request payloads and embedded reads in Prompt DTOs (cents).
type costCalculationRequest struct {
	PurchasePriceNet         int             `json:"purchasePriceNet"`
	PurchasePriceTax         int             `json:"purchasePriceTax"`
	PurchasePriceGross       int             `json:"purchasePriceGross"`
	PurchaseCostNet          int             `json:"purchaseCostNet"`
	PurchaseCostTax          int             `json:"purchaseCostTax"`
	PurchaseCostGross        int             `json:"purchaseCostGross"`
	PurchaseCostPercent      float64         `json:"purchaseCostPercent"`
	PurchaseTotalNet         int             `json:"purchaseTotalNet"`
	PurchaseTotalTax         int             `json:"purchaseTotalTax"`
	PurchaseTotalGross       int             `json:"purchaseTotalGross"`
	PurchasePriceUnit        string          `json:"purchasePriceUnit"`
	PurchaseVatRateId        *int            `json:"purchaseVatRateId"`
	PurchaseVatRatePercent   float64         `json:"purchaseVatRatePercent"`
	PurchaseCalculationMode  string          `json:"purchaseCalculationMode"`
	SalesVatRateId           *int            `json:"salesVatRateId"`
	SalesVatRatePercent      float64         `json:"salesVatRatePercent"`
	SalesMarginNet           int             `json:"salesMarginNet"`
	SalesMarginTax           int             `json:"salesMarginTax"`
	SalesMarginGross         int             `json:"salesMarginGross"`
	SalesMarginPercent       float64         `json:"salesMarginPercent"`
	SalesTotalNet            int             `json:"salesTotalNet"`
	SalesTotalTax            int             `json:"salesTotalTax"`
	SalesTotalGross          int             `json:"salesTotalGross"`
	SalesPriceUnit           string          `json:"salesPriceUnit"`
	SalesCalculationMode     string          `json:"salesCalculationMode"`
	PurchasePriceCorresponds *netGrossChoice `json:"purchasePriceCorresponds"`
	SalesPriceCorresponds    *netGrossChoice `json:"salesPriceCorresponds"`
	PurchaseActiveRow        string          `json:"purchaseActiveRow"`
	SalesActiveRow           string          `json:"salesActiveRow"`
}

// netGrossChoice accepts either boolean values or NET/GROSS strings when decoding JSON.
type netGrossChoice bool

func (c *netGrossChoice) UnmarshalJSON(data []byte) error {
	if string(data) == "null" {
		return nil
	}
	var boolValue bool
	if err := json.Unmarshal(data, &boolValue); err == nil {
		*c = netGrossChoice(boolValue)
		return nil
	}
	var stringValue string
	if err := json.Unmarshal(data, &stringValue); err == nil {
		normalized := strings.ToUpper(strings.TrimSpace(stringValue))
		switch normalized {
		case "NET":
			*c = true
			return nil
		case "GROSS":
			*c = false
			return nil
		case "":
			return nil
		}
		return fmt.Errorf("invalid net/gross value: %s", stringValue)
	}
	return fmt.Errorf("netGrossChoice must be a boolean or NET/GROSS string")
}

func stringToNetGrossChoice(value string) *netGrossChoice {
	normalized := strings.ToUpper(strings.TrimSpace(value))
	switch normalized {
	case "NET":
		result := netGrossChoice(true)
		return &result
	case "GROSS":
		result := netGrossChoice(false)
		return &result
	default:
		return nil
	}
}
