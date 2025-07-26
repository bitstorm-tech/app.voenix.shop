import type { CostCalculation, CreateCostCalculationRequest } from '@/types/article';
import { create } from 'zustand';
import { immer } from 'zustand/middleware/immer';

interface CostCalculationStoreState {
  // Cost calculation data
  costCalculation: CostCalculation;

  // UI state
  purchasePriceCorresponds: boolean;
  salesPriceCorresponds: boolean;
  purchaseActiveRow: 'cost' | 'costPercent';
  salesActiveRow: 'margin' | 'marginPercent' | 'total';

  // Actions
  setCostCalculation: (data: Partial<CreateCostCalculationRequest>) => void;
  updateField: <K extends keyof CostCalculation>(field: K, value: CostCalculation[K]) => void;

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

  // UI state actions
  setPurchasePriceCorresponds: (value: boolean) => void;
  setSalesPriceCorresponds: (value: boolean) => void;
  setPurchaseActiveRow: (row: 'cost' | 'costPercent') => void;
  setSalesActiveRow: (row: 'margin' | 'marginPercent' | 'total') => void;

  // Reset
  resetCostCalculation: () => void;
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

  // Sales section
  salesVatRatePercent: 19,
  marginNet: 0,
  marginTax: 0,
  marginGross: 0,
  marginPercent: 100,
  salesTotalNet: 0,
  salesTotalTax: 0,
  salesTotalGross: 0,
  salesPriceUnit: '1.00',

  // Calculation mode
  purchaseCalculationMode: 'NET',
  salesCalculationMode: 'NET',
};

export const useCostCalculationStore = create<CostCalculationStoreState>()(
  immer((set) => ({
    // Initial state
    costCalculation: { ...initialCostCalculation },
    purchasePriceCorresponds: false,
    salesPriceCorresponds: false,
    purchaseActiveRow: 'cost',
    salesActiveRow: 'margin',

    // Set entire cost calculation
    setCostCalculation: (data) => {
      set((state) => {
        state.costCalculation = { ...initialCostCalculation, ...data };
      });
    },

    // Update single field
    updateField: (field, value) => {
      set((state) => {
        state.costCalculation[field] = value;
      });
    },

    // Purchase price calculations
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

    // Purchase cost calculations
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

    // Purchase cost percent calculation
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

    // Purchase VAT rate
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

    // Purchase calculation mode
    setPurchaseCalculationMode: (mode) => {
      set((state) => {
        state.costCalculation.purchaseCalculationMode = mode;
      });
    },

    // Margin calculations
    updateMargin: (field, value) => {
      set((state) => {
        if (state.costCalculation.salesCalculationMode === 'NET') {
          if (field === 'net') {
            const newSalesNet = state.costCalculation.purchaseTotalNet + value;
            const newSalesTax = newSalesNet * (state.costCalculation.salesVatRatePercent / 100);
            const newSalesGross = newSalesNet + newSalesTax;
            const newMarginPercent = state.costCalculation.purchaseTotalNet > 0 ? (value / state.costCalculation.purchaseTotalNet) * 100 : 0;

            state.costCalculation.marginNet = value;
            state.costCalculation.marginTax = newSalesTax - state.costCalculation.purchaseTotalTax;
            state.costCalculation.marginGross = newSalesGross - state.costCalculation.purchaseTotalGross;
            state.costCalculation.marginPercent = newMarginPercent;
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

            state.costCalculation.marginNet = newSalesNet - state.costCalculation.purchaseTotalNet;
            state.costCalculation.marginTax = newSalesTax - state.costCalculation.purchaseTotalTax;
            state.costCalculation.marginGross = value;
            state.costCalculation.marginPercent = newMarginPercent;
            state.costCalculation.salesTotalNet = newSalesNet;
            state.costCalculation.salesTotalTax = newSalesTax;
            state.costCalculation.salesTotalGross = newSalesGross;
          }
        }
      });
    },

    // Margin percent calculation
    updateMarginPercent: (value) => {
      set((state) => {
        const salesNet = state.costCalculation.purchaseTotalNet * (1 + value / 100);
        const salesTax = salesNet * (state.costCalculation.salesVatRatePercent / 100);
        const salesGross = salesNet + salesTax;

        const marginNet = salesNet - state.costCalculation.purchaseTotalNet;
        const marginTax = salesTax - state.costCalculation.purchaseTotalTax;
        const marginGross = salesGross - state.costCalculation.purchaseTotalGross;

        state.costCalculation.marginPercent = value;
        state.costCalculation.marginNet = marginNet;
        state.costCalculation.marginTax = marginTax;
        state.costCalculation.marginGross = marginGross;
        state.costCalculation.salesTotalNet = salesNet;
        state.costCalculation.salesTotalTax = salesTax;
        state.costCalculation.salesTotalGross = salesGross;
      });
    },

    // Sales total calculations
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
            state.costCalculation.marginNet = marginNet;
            state.costCalculation.marginTax = marginTax;
            state.costCalculation.marginGross = marginGross;
            state.costCalculation.marginPercent = marginPercent;
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
            state.costCalculation.marginNet = marginNet;
            state.costCalculation.marginTax = marginTax;
            state.costCalculation.marginGross = marginGross;
            state.costCalculation.marginPercent = marginPercent;
          }
        }
      });
    },

    // Sales VAT rate
    updateSalesVatRate: (percent, vatRateId) => {
      set((state) => {
        state.costCalculation.salesVatRatePercent = percent;
        if (vatRateId !== undefined) {
          state.costCalculation.salesVatRateId = vatRateId;
        }

        // Recalculate sales totals with new VAT rate if not directly editing sales total
        if (state.salesActiveRow !== 'total') {
          const salesNet = state.costCalculation.purchaseTotalNet * (1 + state.costCalculation.marginPercent / 100);
          const salesTax = salesNet * (percent / 100);
          const salesGross = salesNet + salesTax;

          const marginNet = salesNet - state.costCalculation.purchaseTotalNet;
          const marginTax = salesTax - state.costCalculation.purchaseTotalTax;
          const marginGross = salesGross - state.costCalculation.purchaseTotalGross;

          state.costCalculation.salesTotalNet = salesNet;
          state.costCalculation.salesTotalTax = salesTax;
          state.costCalculation.salesTotalGross = salesGross;
          state.costCalculation.marginNet = marginNet;
          state.costCalculation.marginTax = marginTax;
          state.costCalculation.marginGross = marginGross;
        }
      });
    },

    // Sales calculation mode
    setSalesCalculationMode: (mode) => {
      set((state) => {
        state.costCalculation.salesCalculationMode = mode;
      });
    },

    // UI state actions
    setPurchasePriceCorresponds: (value) => {
      set((state) => {
        state.purchasePriceCorresponds = value;
      });
    },

    setSalesPriceCorresponds: (value) => {
      set((state) => {
        state.salesPriceCorresponds = value;
      });
    },

    setPurchaseActiveRow: (row) => {
      set((state) => {
        state.purchaseActiveRow = row;
      });
    },

    setSalesActiveRow: (row) => {
      set((state) => {
        state.salesActiveRow = row;
      });
    },

    // Reset
    resetCostCalculation: () => {
      set((state) => {
        state.costCalculation = { ...initialCostCalculation };
        state.purchasePriceCorresponds = false;
        state.salesPriceCorresponds = false;
        state.purchaseActiveRow = 'cost';
        state.salesActiveRow = 'margin';
      });
    },
  })),
);
