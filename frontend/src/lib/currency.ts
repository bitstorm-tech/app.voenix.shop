import type { CostCalculation } from '@/types/article';

/**
 * Converts euros to cents for backend storage
 * @param euros The amount in euros (e.g., 10.50)
 * @returns The amount in cents (e.g., 1050)
 */
export function eurosToCents(euros: number): number {
  return Math.round(euros * 100);
}

/**
 * Converts cents to euros for frontend display
 * @param cents The amount in cents (e.g., 1050)
 * @returns The amount in euros (e.g., 10.50)
 */
export function centsToEuros(cents: number): number {
  return cents / 100;
}

/**
 * Converts all currency fields in a cost calculation from euros to cents
 */
export function convertCostCalculationToCents(costCalc: CostCalculation | null | undefined): CostCalculation | null | undefined {
  if (!costCalc) return costCalc;

  return {
    ...costCalc,
    // Purchase section
    purchasePriceNet: eurosToCents(costCalc.purchasePriceNet || 0),
    purchasePriceTax: eurosToCents(costCalc.purchasePriceTax || 0),
    purchasePriceGross: eurosToCents(costCalc.purchasePriceGross || 0),
    purchaseCostNet: eurosToCents(costCalc.purchaseCostNet || 0),
    purchaseCostTax: eurosToCents(costCalc.purchaseCostTax || 0),
    purchaseCostGross: eurosToCents(costCalc.purchaseCostGross || 0),
    purchaseTotalNet: eurosToCents(costCalc.purchaseTotalNet || 0),
    purchaseTotalTax: eurosToCents(costCalc.purchaseTotalTax || 0),
    purchaseTotalGross: eurosToCents(costCalc.purchaseTotalGross || 0),
    // Sales section
    salesMarginNet: eurosToCents(costCalc.salesMarginNet || 0),
    salesMarginTax: eurosToCents(costCalc.salesMarginTax || 0),
    salesMarginGross: eurosToCents(costCalc.salesMarginGross || 0),
    salesMarginPercent: costCalc.salesMarginPercent || 0,
    salesTotalNet: eurosToCents(costCalc.salesTotalNet || 0),
    salesTotalTax: eurosToCents(costCalc.salesTotalTax || 0),
    salesTotalGross: eurosToCents(costCalc.salesTotalGross || 0),
    // Keep non-currency fields as-is
    purchaseCostPercent: costCalc.purchaseCostPercent,
    purchaseVatRatePercent: costCalc.purchaseVatRatePercent,
    salesVatRatePercent: costCalc.salesVatRatePercent,
    purchaseCalculationMode: costCalc.purchaseCalculationMode,
    salesCalculationMode: costCalc.salesCalculationMode,
    purchasePriceCorresponds: costCalc.purchasePriceCorresponds,
    salesPriceCorresponds: costCalc.salesPriceCorresponds,
    purchaseActiveRow: costCalc.purchaseActiveRow,
    salesActiveRow: costCalc.salesActiveRow,
    purchasePriceUnit: costCalc.purchasePriceUnit,
    salesPriceUnit: costCalc.salesPriceUnit,
    purchaseVatRateId: costCalc.purchaseVatRateId,
    salesVatRateId: costCalc.salesVatRateId,
  };
}

/**
 * Converts all currency fields in a cost calculation from cents to euros
 */
export function convertCostCalculationToEuros(costCalc: CostCalculation | null | undefined): CostCalculation | null | undefined {
  if (!costCalc) return costCalc;

  return {
    ...costCalc,
    // Purchase section
    purchasePriceNet: centsToEuros(costCalc.purchasePriceNet || 0),
    purchasePriceTax: centsToEuros(costCalc.purchasePriceTax || 0),
    purchasePriceGross: centsToEuros(costCalc.purchasePriceGross || 0),
    purchaseCostNet: centsToEuros(costCalc.purchaseCostNet || 0),
    purchaseCostTax: centsToEuros(costCalc.purchaseCostTax || 0),
    purchaseCostGross: centsToEuros(costCalc.purchaseCostGross || 0),
    purchaseTotalNet: centsToEuros(costCalc.purchaseTotalNet || 0),
    purchaseTotalTax: centsToEuros(costCalc.purchaseTotalTax || 0),
    purchaseTotalGross: centsToEuros(costCalc.purchaseTotalGross || 0),
    // Sales section
    salesMarginNet: centsToEuros(costCalc.salesMarginNet || 0),
    salesMarginTax: centsToEuros(costCalc.salesMarginTax || 0),
    salesMarginGross: centsToEuros(costCalc.salesMarginGross || 0),
    salesMarginPercent: costCalc.salesMarginPercent || 0,
    salesTotalNet: centsToEuros(costCalc.salesTotalNet || 0),
    salesTotalTax: centsToEuros(costCalc.salesTotalTax || 0),
    salesTotalGross: centsToEuros(costCalc.salesTotalGross || 0),
    // Keep non-currency fields as-is
    purchaseCostPercent: costCalc.purchaseCostPercent,
    purchaseVatRatePercent: costCalc.purchaseVatRatePercent,
    salesVatRatePercent: costCalc.salesVatRatePercent,
    purchaseCalculationMode: costCalc.purchaseCalculationMode,
    salesCalculationMode: costCalc.salesCalculationMode,
    purchasePriceCorresponds: costCalc.purchasePriceCorresponds,
    salesPriceCorresponds: costCalc.salesPriceCorresponds,
    purchaseActiveRow: costCalc.purchaseActiveRow,
    salesActiveRow: costCalc.salesActiveRow,
    purchasePriceUnit: costCalc.purchasePriceUnit,
    salesPriceUnit: costCalc.salesPriceUnit,
    purchaseVatRateId: costCalc.purchaseVatRateId,
    salesVatRateId: costCalc.salesVatRateId,
  };
}

export function createEuroCurrencyFormatter(locale: string, currency: string) {
  const baseFormatter = new Intl.NumberFormat(locale, { style: 'currency', currency });
  const symbol = baseFormatter.formatToParts(0).find((part) => part.type === 'currency')?.value ?? currency;

  const format = (value: number) => {
    const parts = baseFormatter.formatToParts(value);
    const numberPortion = parts
      .filter((part) => part.type !== 'currency')
      .map((part) => part.value)
      .join('')
      .trim();

    return `${numberPortion} ${symbol}`.trim();
  };

  return { format, symbol };
}
