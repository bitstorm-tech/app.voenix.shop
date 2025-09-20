package prompt

import (
	"encoding/json"
	"testing"
)

func TestNetGrossChoiceUnmarshalJSONAcceptsBooleans(t *testing.T) {
	var choice netGrossChoice
	if err := json.Unmarshal([]byte("true"), &choice); err != nil {
		t.Fatalf("expected boolean to decode, got error: %v", err)
	}
	if !bool(choice) {
		t.Fatalf("expected boolean true to map to NET value")
	}
}

func TestNetGrossChoiceUnmarshalJSONAcceptsStrings(t *testing.T) {
	var choice netGrossChoice
	if err := json.Unmarshal([]byte(`"GROSS"`), &choice); err != nil {
		t.Fatalf("expected string to decode, got error: %v", err)
	}
	if bool(choice) {
		t.Fatalf("expected GROSS string to map to false value")
	}
}

func TestNetGrossChoiceUnmarshalJSONRejectsInvalidValues(t *testing.T) {
	var choice netGrossChoice
	if err := json.Unmarshal([]byte(`"invalid"`), &choice); err == nil {
		t.Fatalf("expected invalid string to return an error")
	}
}

func TestStringToNetGrossChoice(t *testing.T) {
	netChoice := stringToNetGrossChoice("NET")
	if netChoice == nil || !bool(*netChoice) {
		t.Fatalf("expected NET string to produce true choice")
	}

	grossChoice := stringToNetGrossChoice("GROSS")
	if grossChoice == nil || bool(*grossChoice) {
		t.Fatalf("expected GROSS string to produce false choice")
	}

	if choice := stringToNetGrossChoice(" "); choice != nil {
		t.Fatalf("expected blank string to return nil choice")
	}
}

func TestCostCalculationRequestUnmarshalAcceptsStringChoices(t *testing.T) {
	payload := []byte(`{
        "purchasePriceNet": 0,
        "purchasePriceTax": 0,
        "purchasePriceGross": 0,
        "purchaseCostNet": 0,
        "purchaseCostTax": 0,
        "purchaseCostGross": 0,
        "purchaseCostPercent": 0,
        "purchaseTotalNet": 0,
        "purchaseTotalTax": 0,
        "purchaseTotalGross": 0,
        "purchasePriceUnit": "1.00",
        "purchaseCalculationMode": "NET",
        "salesCalculationMode": "NET",
        "salesPriceUnit": "1.00",
        "purchasePriceCorresponds": "NET",
        "salesPriceCorresponds": "GROSS",
        "purchaseActiveRow": "COST",
        "salesActiveRow": "MARGIN"
    }`)

	var request costCalculationRequest
	if err := json.Unmarshal(payload, &request); err != nil {
		t.Fatalf("expected payload to decode, got error: %v", err)
	}

	if request.PurchasePriceCorresponds == nil || !bool(*request.PurchasePriceCorresponds) {
		t.Fatalf("expected purchase price correspond NET to map to true choice")
	}

	if request.SalesPriceCorresponds == nil || bool(*request.SalesPriceCorresponds) {
		t.Fatalf("expected sales price correspond GROSS to map to false choice")
	}
}
