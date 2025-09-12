import type { CostCalculation } from '@/types/article';
import { create } from 'zustand';
import { immer } from 'zustand/middleware/immer';

interface PromptPriceState {
  costCalculation: CostCalculation;

  // Field updates
  updateCostField: <K extends keyof CostCalculation>(field: K, value: CostCalculation[K]) => void;

  // Purchase calculations
  updatePurchasePrice: (field: 'net' | 'gross', value: number) => void;
  updatePurchaseCost: (field: 'net' | 'gross', value: number) => void;
  updatePurchaseCostPercent: (value: number) => void;
  updatePurchaseVatRate: (percent: number, vatRateId?: number) => void;
  setPurchaseCalculationMode: (mode: 'NET' | 'GROSS') => void;

  // Sales calculations
  updateMargin: (field: 'net' | 'gross', value: number) => void;
  updateMarginPercent: (value: number) => void;
  updateSalesTotal: (field: 'net' | 'gross', value: number) => void;
  updateSalesVatRate: (percent: number, vatRateId?: number) => void;
  setSalesCalculationMode: (mode: 'NET' | 'GROSS') => void;
}

const initialCostCalculation: CostCalculation = {
  // Purchase section
  purchasePriceNet: 0,
  purchasePriceTax: 0,
  purchasePriceGross: 0,
  purchaseCostNet: 0,
  purchaseCostTax: 0,
  purchaseCostGross: 0,
  purchaseCostPercent: 0,
  purchaseTotalNet: 0,
  purchaseTotalTax: 0,
  purchaseTotalGross: 0,
  purchasePriceUnit: '1.00',
  purchaseVatRatePercent: 19,
  purchaseVatRateId: undefined,

  // Sales section
  salesVatRatePercent: 19,
  salesVatRateId: undefined,
  salesMarginNet: 0,
  salesMarginTax: 0,
  salesMarginGross: 0,
  salesMarginPercent: 0,
  salesTotalNet: 0,
  salesTotalTax: 0,
  salesTotalGross: 0,
  salesPriceUnit: '1.00',

  // Calculation mode
  purchaseCalculationMode: 'NET',
  salesCalculationMode: 'NET',

  // UI state
  purchasePriceCorresponds: false,
  salesPriceCorresponds: false,
  purchaseActiveRow: 'COST',
  salesActiveRow: 'MARGIN',
};

export const usePromptPriceStore = create<PromptPriceState>()(
  immer((set) => ({
    costCalculation: { ...initialCostCalculation },

    updateCostField: (field, value) => {
      set((state) => {
        state.costCalculation[field] = value as any;
      });
    },

    updatePurchasePrice: (field, value) => {
      set((state) => {
        const taxRate = state.costCalculation.purchaseVatRatePercent / 100;

        if (state.costCalculation.purchaseCalculationMode === 'NET') {
          if (field === 'net') {
            const tax = value * taxRate;
            state.costCalculation.purchasePriceNet = value;
            state.costCalculation.purchasePriceTax = tax;
            state.costCalculation.purchasePriceGross = value + tax;
          }
        } else {
          if (field === 'gross') {
            const net = value / (1 + taxRate);
            const tax = value - net;
            state.costCalculation.purchasePriceNet = net;
            state.costCalculation.purchasePriceTax = tax;
            state.costCalculation.purchasePriceGross = value;
          }
        }

        // Update totals
        state.costCalculation.purchaseTotalNet = state.costCalculation.purchasePriceNet + state.costCalculation.purchaseCostNet;
        state.costCalculation.purchaseTotalTax = state.costCalculation.purchasePriceTax + state.costCalculation.purchaseCostTax;
        state.costCalculation.purchaseTotalGross = state.costCalculation.purchasePriceGross + state.costCalculation.purchaseCostGross;
      });
    },

    updatePurchaseCost: (field, value) => {
      set((state) => {
        const taxRate = state.costCalculation.purchaseVatRatePercent / 100;

        if (state.costCalculation.purchaseCalculationMode === 'NET') {
          if (field === 'net') {
            const tax = value * taxRate;
            state.costCalculation.purchaseCostNet = value;
            state.costCalculation.purchaseCostTax = tax;
            state.costCalculation.purchaseCostGross = value + tax;

            // Update cost percent
            if (state.costCalculation.purchasePriceNet > 0) {
              state.costCalculation.purchaseCostPercent = (value / state.costCalculation.purchasePriceNet) * 100;
            }
          }
        } else {
          if (field === 'gross') {
            const net = value / (1 + taxRate);
            const tax = value - net;
            state.costCalculation.purchaseCostNet = net;
            state.costCalculation.purchaseCostTax = tax;
            state.costCalculation.purchaseCostGross = value;

            // Update cost percent
            if (state.costCalculation.purchasePriceNet > 0) {
              state.costCalculation.purchaseCostPercent = (net / state.costCalculation.purchasePriceNet) * 100;
            }
          }
        }

        // Update totals
        state.costCalculation.purchaseTotalNet = state.costCalculation.purchasePriceNet + state.costCalculation.purchaseCostNet;
        state.costCalculation.purchaseTotalTax = state.costCalculation.purchasePriceTax + state.costCalculation.purchaseCostTax;
        state.costCalculation.purchaseTotalGross = state.costCalculation.purchasePriceGross + state.costCalculation.purchaseCostGross;
      });
    },

    updatePurchaseCostPercent: (value) => {
      set((state) => {
        const costNet = state.costCalculation.purchasePriceNet * (value / 100);
        const taxRate = state.costCalculation.purchaseVatRatePercent / 100;
        const costTax = costNet * taxRate;
        const costGross = costNet + costTax;

        state.costCalculation.purchaseCostPercent = value;
        state.costCalculation.purchaseCostNet = costNet;
        state.costCalculation.purchaseCostTax = costTax;
        state.costCalculation.purchaseCostGross = costGross;

        // Update totals
        state.costCalculation.purchaseTotalNet = state.costCalculation.purchasePriceNet + costNet;
        state.costCalculation.purchaseTotalTax = state.costCalculation.purchasePriceTax + costTax;
        state.costCalculation.purchaseTotalGross = state.costCalculation.purchasePriceGross + costGross;
      });
    },

    updatePurchaseVatRate: (percent, vatRateId) => {
      set((state) => {
        state.costCalculation.purchaseVatRatePercent = percent;
        if (vatRateId !== undefined) {
          state.costCalculation.purchaseVatRateId = vatRateId;
        }

        // Recalculate with new VAT rate
        const taxRate = percent / 100;

        if (state.costCalculation.purchaseCalculationMode === 'NET') {
          // Recalculate tax and gross from net values
          state.costCalculation.purchasePriceTax = state.costCalculation.purchasePriceNet * taxRate;
          state.costCalculation.purchasePriceGross = state.costCalculation.purchasePriceNet + state.costCalculation.purchasePriceTax;
          state.costCalculation.purchaseCostTax = state.costCalculation.purchaseCostNet * taxRate;
          state.costCalculation.purchaseCostGross = state.costCalculation.purchaseCostNet + state.costCalculation.purchaseCostTax;
        } else {
          // Recalculate net and tax from gross values
          state.costCalculation.purchasePriceNet = state.costCalculation.purchasePriceGross / (1 + taxRate);
          state.costCalculation.purchasePriceTax = state.costCalculation.purchasePriceGross - state.costCalculation.purchasePriceNet;
          state.costCalculation.purchaseCostNet = state.costCalculation.purchaseCostGross / (1 + taxRate);
          state.costCalculation.purchaseCostTax = state.costCalculation.purchaseCostGross - state.costCalculation.purchaseCostNet;
        }

        // Update totals
        state.costCalculation.purchaseTotalNet = state.costCalculation.purchasePriceNet + state.costCalculation.purchaseCostNet;
        state.costCalculation.purchaseTotalTax = state.costCalculation.purchasePriceTax + state.costCalculation.purchaseCostTax;
        state.costCalculation.purchaseTotalGross = state.costCalculation.purchasePriceGross + state.costCalculation.purchaseCostGross;
      });
    },

    setPurchaseCalculationMode: (mode) => {
      set((state) => {
        state.costCalculation.purchaseCalculationMode = mode;
      });
    },

    updateMargin: (field, value) => {
      set((state) => {
        if (state.costCalculation.salesCalculationMode === 'NET') {
          if (field === 'net') {
            const newSalesNet = state.costCalculation.purchaseTotalNet + value;
            const newSalesTax = newSalesNet * (state.costCalculation.salesVatRatePercent / 100);
            const newSalesGross = newSalesNet + newSalesTax;
            const newMarginPercent = state.costCalculation.purchaseTotalNet > 0 ? (value / state.costCalculation.purchaseTotalNet) * 100 : 0;

            state.costCalculation.salesMarginNet = value;
            state.costCalculation.salesMarginTax = newSalesTax - state.costCalculation.purchaseTotalTax;
            state.costCalculation.salesMarginGross = newSalesGross - state.costCalculation.purchaseTotalGross;
            state.costCalculation.salesMarginPercent = newMarginPercent;
            state.costCalculation.salesTotalNet = newSalesNet;
            state.costCalculation.salesTotalTax = newSalesTax;
            state.costCalculation.salesTotalGross = newSalesGross;
          }
        } else {
          if (field === 'gross') {
            const newSalesGross = state.costCalculation.purchaseTotalGross + value;
            const newSalesNet = newSalesGross / (1 + state.costCalculation.salesVatRatePercent / 100);
            const newSalesTax = newSalesGross - newSalesNet;
            const newMarginPercent =
              state.costCalculation.purchaseTotalNet > 0
                ? ((newSalesNet - state.costCalculation.purchaseTotalNet) / state.costCalculation.purchaseTotalNet) * 100
                : 0;

            state.costCalculation.salesMarginNet = newSalesNet - state.costCalculation.purchaseTotalNet;
            state.costCalculation.salesMarginTax = newSalesTax - state.costCalculation.purchaseTotalTax;
            state.costCalculation.salesMarginGross = value;
            state.costCalculation.salesMarginPercent = newMarginPercent;
            state.costCalculation.salesTotalNet = newSalesNet;
            state.costCalculation.salesTotalTax = newSalesTax;
            state.costCalculation.salesTotalGross = newSalesGross;
          }
        }
      });
    },

    updateMarginPercent: (value) => {
      set((state) => {
        const salesNet = state.costCalculation.purchaseTotalNet * (1 + value / 100);
        const salesTax = salesNet * (state.costCalculation.salesVatRatePercent / 100);
        const salesGross = salesNet + salesTax;

        const marginNet = salesNet - state.costCalculation.purchaseTotalNet;
        const marginTax = salesTax - state.costCalculation.purchaseTotalTax;
        const marginGross = salesGross - state.costCalculation.purchaseTotalGross;

        state.costCalculation.salesMarginPercent = value;
        state.costCalculation.salesMarginNet = marginNet;
        state.costCalculation.salesMarginTax = marginTax;
        state.costCalculation.salesMarginGross = marginGross;
        state.costCalculation.salesTotalNet = salesNet;
        state.costCalculation.salesTotalTax = salesTax;
        state.costCalculation.salesTotalGross = salesGross;
      });
    },

    updateSalesTotal: (field, value) => {
      set((state) => {
        const taxRate = state.costCalculation.salesVatRatePercent / 100;

        if (state.costCalculation.salesCalculationMode === 'NET') {
          if (field === 'net') {
            const tax = value * taxRate;
            const gross = value + tax;
            const marginNet = value - state.costCalculation.purchaseTotalNet;
            const marginTax = tax - state.costCalculation.purchaseTotalTax;
            const marginGross = gross - state.costCalculation.purchaseTotalGross;
            const marginPercent = state.costCalculation.purchaseTotalNet > 0 ? (marginNet / state.costCalculation.purchaseTotalNet) * 100 : 0;

            state.costCalculation.salesTotalNet = value;
            state.costCalculation.salesTotalTax = tax;
            state.costCalculation.salesTotalGross = gross;
            state.costCalculation.salesMarginNet = marginNet;
            state.costCalculation.salesMarginTax = marginTax;
            state.costCalculation.salesMarginGross = marginGross;
            state.costCalculation.salesMarginPercent = marginPercent;
          }
        } else {
          if (field === 'gross') {
            const net = value / (1 + taxRate);
            const tax = value - net;
            const marginNet = net - state.costCalculation.purchaseTotalNet;
            const marginTax = tax - state.costCalculation.purchaseTotalTax;
            const marginGross = value - state.costCalculation.purchaseTotalGross;
            const marginPercent = state.costCalculation.purchaseTotalNet > 0 ? (marginNet / state.costCalculation.purchaseTotalNet) * 100 : 0;

            state.costCalculation.salesTotalNet = net;
            state.costCalculation.salesTotalTax = tax;
            state.costCalculation.salesTotalGross = value;
            state.costCalculation.salesMarginNet = marginNet;
            state.costCalculation.salesMarginTax = marginTax;
            state.costCalculation.salesMarginGross = marginGross;
            state.costCalculation.salesMarginPercent = marginPercent;
          }
        }
      });
    },

    updateSalesVatRate: (percent, vatRateId) => {
      set((state) => {
        state.costCalculation.salesVatRatePercent = percent;
        if (vatRateId !== undefined) {
          state.costCalculation.salesVatRateId = vatRateId;
        }

        // Recalculate sales totals with new VAT rate if not directly editing sales total
        if (state.costCalculation.salesActiveRow !== 'TOTAL') {
          const salesNet = state.costCalculation.purchaseTotalNet * (1 + state.costCalculation.salesMarginPercent / 100);
          const salesTax = salesNet * (percent / 100);
          const salesGross = salesNet + salesTax;

          const marginNet = salesNet - state.costCalculation.purchaseTotalNet;
          const marginTax = salesTax - state.costCalculation.purchaseTotalTax;
          const marginGross = salesGross - state.costCalculation.purchaseTotalGross;

          state.costCalculation.salesTotalNet = salesNet;
          state.costCalculation.salesTotalTax = salesTax;
          state.costCalculation.salesTotalGross = salesGross;
          state.costCalculation.salesMarginNet = marginNet;
          state.costCalculation.salesMarginTax = marginTax;
          state.costCalculation.salesMarginGross = marginGross;
        }
      });
    },

    setSalesCalculationMode: (mode) => {
      set((state) => {
        state.costCalculation.salesCalculationMode = mode;
      });
    },
  })),
);
