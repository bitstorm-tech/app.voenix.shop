import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/Card';
import { Checkbox } from '@/components/ui/Checkbox';
import { CurrencyInput } from '@/components/ui/CurrencyInput';
import { FieldLabel } from '@/components/ui/FieldLabel';
import { Input } from '@/components/ui/Input';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/Select';
import { useVats } from '@/hooks/queries/useVat';
import { usePromptPriceStore } from '@/stores/admin/prompts/usePromptPriceStore';
import { useCallback, useEffect } from 'react';

export default function PriceCalculationTab() {
  const { data: vats = [] } = useVats();

  const {
    costCalculation,
    updateCostField,
    updatePurchasePrice,
    updatePurchaseCost,
    updatePurchaseCostPercent,
    updatePurchaseVatRate,
    setPurchaseCalculationMode,
    updateMargin,
    updateMarginPercent,
    updateSalesTotal,
    updateSalesVatRate,
    setSalesCalculationMode,
  } = usePromptPriceStore();

  const handlePurchaseVatRateChange = useCallback(
    (vatRateId: string) => {
      const selectedVat = vats.find((v) => v.id.toString() === vatRateId);
      if (selectedVat) {
        updatePurchaseVatRate(selectedVat.percent, selectedVat.id);
      }
    },
    [vats, updatePurchaseVatRate],
  );

  const handleSalesVatRateChange = useCallback(
    (vatRateId: string) => {
      const selectedVat = vats.find((v) => v.id.toString() === vatRateId);
      if (selectedVat) {
        updateSalesVatRate(selectedVat.percent, selectedVat.id);
      }
    },
    [vats, updateSalesVatRate],
  );

  // Auto-select default VAT when component loads or when VATs change
  useEffect(() => {
    if (vats.length > 0) {
      const defaultVat = vats.find((v) => v.isDefault);
      if (defaultVat) {
        // Only set default if no VAT is currently selected
        if (!costCalculation.purchaseVatRateId) {
          updatePurchaseVatRate(defaultVat.percent, defaultVat.id);
        }
        if (!costCalculation.salesVatRateId) {
          updateSalesVatRate(defaultVat.percent, defaultVat.id);
        }
      }
    }
  }, [vats, costCalculation.purchaseVatRateId, costCalculation.salesVatRateId, updatePurchaseVatRate, updateSalesVatRate]);

  return (
    <div className="space-y-6">
      {/* Purchase Section */}
      <Card>
        <CardHeader>
          <CardTitle>Purchase</CardTitle>
          <CardDescription>Purchase price and price calculations</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          {/* Tax Rate */}
          <div className="grid grid-cols-4 items-center gap-4">
            <FieldLabel optional>Tax Rate</FieldLabel>
            <Select value={costCalculation.purchaseVatRateId?.toString() || ''} onValueChange={handlePurchaseVatRateChange}>
              <SelectTrigger>
                <SelectValue placeholder="Select VAT rate" />
              </SelectTrigger>
              <SelectContent>
                {vats.map((vat) => (
                  <SelectItem key={vat.id} value={vat.id.toString()}>
                    {vat.name} ({vat.percent}%)
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
            <div className="text-muted-foreground col-span-2 text-sm">(from article or service group)</div>
          </div>

          {/* Calculation Mode */}
          <div className="flex items-center space-x-4">
            <label className="flex items-center space-x-2">
              <input
                type="radio"
                name="purchaseMode"
                value="NET"
                checked={costCalculation.purchaseCalculationMode === 'NET'}
                onChange={() => setPurchaseCalculationMode('NET')}
                className="h-4 w-4"
              />
              <span>Net</span>
            </label>
            <label className="flex items-center space-x-2">
              <input
                type="radio"
                name="purchaseMode"
                value="GROSS"
                checked={costCalculation.purchaseCalculationMode === 'GROSS'}
                onChange={() => setPurchaseCalculationMode('GROSS')}
                className="h-4 w-4"
              />
              <span>Gross</span>
            </label>
          </div>

          {/* Headers */}
          <div className="text-muted-foreground grid grid-cols-4 gap-4 text-sm font-medium">
            <div></div>
            <div className="text-center">Net</div>
            <div className="text-center">Tax</div>
            <div className="text-center">Gross</div>
          </div>

          {/* Purchase Price */}
          <div className="grid grid-cols-4 items-center gap-4">
            <FieldLabel optional>Purchase Price</FieldLabel>
            <CurrencyInput
              value={costCalculation.purchasePriceNet}
              onChange={(value) => updatePurchasePrice('net', value)}
              disabled={costCalculation.purchaseCalculationMode === 'GROSS'}
              min={0}
            />
            <CurrencyInput value={costCalculation.purchasePriceTax} onChange={() => {}} disabled min={0} />
            <CurrencyInput
              value={costCalculation.purchasePriceGross}
              onChange={(value) => updatePurchasePrice('gross', value)}
              disabled={costCalculation.purchaseCalculationMode === 'NET'}
              min={0}
            />
          </div>

          {/* Purchase Cost */}
          <div className="grid grid-cols-4 items-center gap-4">
            <div className="flex items-center gap-2">
              <input
                type="radio"
                id="purchaseCostRadio"
                name="purchaseActiveField"
                value="cost"
                checked={costCalculation.purchaseActiveRow === 'COST'}
                onChange={() => updateCostField('purchaseActiveRow', 'COST')}
                className="h-4 w-4"
              />
              <FieldLabel htmlFor="purchaseCostRadio" optional>
                Purchase Cost
              </FieldLabel>
            </div>
            <CurrencyInput
              value={costCalculation.purchaseCostNet}
              onChange={(value) => updatePurchaseCost('net', value)}
              disabled={costCalculation.purchaseCalculationMode === 'GROSS' || costCalculation.purchaseActiveRow !== 'COST'}
              min={0}
            />
            <CurrencyInput value={costCalculation.purchaseCostTax} onChange={() => {}} disabled min={0} />
            <CurrencyInput
              value={costCalculation.purchaseCostGross}
              onChange={(value) => updatePurchaseCost('gross', value)}
              disabled={costCalculation.purchaseCalculationMode === 'NET' || costCalculation.purchaseActiveRow !== 'COST'}
              min={0}
            />
          </div>

          {/* Purchase Cost % */}
          <div className="grid grid-cols-4 items-center gap-4">
            <div className="flex items-center gap-2">
              <input
                type="radio"
                id="purchaseCostPercentRadio"
                name="purchaseActiveField"
                value="costPercent"
                checked={costCalculation.purchaseActiveRow === 'COST_PERCENT'}
                onChange={() => updateCostField('purchaseActiveRow', 'COST_PERCENT')}
                className="h-4 w-4"
              />
              <FieldLabel htmlFor="purchaseCostPercentRadio" optional>
                Purchase Cost %
              </FieldLabel>
            </div>
            <CurrencyInput
              value={costCalculation.purchaseCostPercent}
              onChange={updatePurchaseCostPercent}
              disabled={costCalculation.purchaseActiveRow !== 'COST_PERCENT'}
              min={0}
              max={100}
              currency="%"
            />
            <div></div>
            <div className="flex items-center space-x-2">
              <CurrencyInput value={costCalculation.purchaseCostPercent} onChange={() => {}} disabled min={0} currency="%" />
            </div>
          </div>

          {/* Purchase Total */}
          <div className="grid grid-cols-4 items-center gap-4 font-semibold">
            <FieldLabel>Purchase Total</FieldLabel>
            <CurrencyInput value={costCalculation.purchaseTotalNet} onChange={() => {}} disabled />
            <CurrencyInput value={costCalculation.purchaseTotalTax} onChange={() => {}} disabled />
            <CurrencyInput value={costCalculation.purchaseTotalGross} onChange={() => {}} disabled />
          </div>

          {/* Price corresponds checkbox */}
          <div className="flex items-center space-x-2 pt-2">
            <Checkbox
              id="purchasePriceCorresponds"
              checked={costCalculation.purchasePriceCorresponds}
              onCheckedChange={(checked) => updateCostField('purchasePriceCorresponds', checked === true)}
            />
            <FieldLabel htmlFor="purchasePriceCorresponds" className="text-sm font-normal" optional>
              Price corresponds to
            </FieldLabel>
            <Input
              type="text"
              value={costCalculation.purchasePriceUnit}
              onChange={(e) => updateCostField('purchasePriceUnit', e.target.value)}
              className="w-32"
              placeholder="1.00"
            />
            <span className="text-muted-foreground text-sm">Quantity unit(s) or packaging</span>
          </div>
        </CardContent>
      </Card>

      {/* Sales Section */}
      <Card>
        <CardHeader>
          <CardTitle>Sales</CardTitle>
          <CardDescription>Sales price and margin calculations</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          {/* Tax Rate */}
          <div className="grid grid-cols-4 items-center gap-4">
            <FieldLabel optional>Tax Rate</FieldLabel>
            <Select value={costCalculation.salesVatRateId?.toString() || ''} onValueChange={handleSalesVatRateChange}>
              <SelectTrigger>
                <SelectValue placeholder="Select VAT rate" />
              </SelectTrigger>
              <SelectContent>
                {vats.map((vat) => (
                  <SelectItem key={vat.id} value={vat.id.toString()}>
                    {vat.name} ({vat.percent}%)
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
            <div className="text-muted-foreground col-span-2 text-sm">(from article or service group)</div>
          </div>

          {/* Calculation Mode */}
          <div className="flex items-center space-x-4">
            <label className="flex items-center space-x-2">
              <input
                type="radio"
                name="salesMode"
                value="NET"
                checked={costCalculation.salesCalculationMode === 'NET'}
                onChange={() => setSalesCalculationMode('NET')}
                className="h-4 w-4"
              />
              <span>Net</span>
            </label>
            <label className="flex items-center space-x-2">
              <input
                type="radio"
                name="salesMode"
                value="GROSS"
                checked={costCalculation.salesCalculationMode === 'GROSS'}
                onChange={() => setSalesCalculationMode('GROSS')}
                className="h-4 w-4"
              />
              <span>Gross</span>
            </label>
          </div>

          {/* Headers */}
          <div className="text-muted-foreground grid grid-cols-4 gap-4 text-sm font-medium">
            <div></div>
            <div className="text-center">Net</div>
            <div className="text-center">Tax</div>
            <div className="text-center">Gross</div>
          </div>

          {/* Margin */}
          <div className="grid grid-cols-4 items-center gap-4">
            <div className="flex items-center gap-2">
              <input
                type="radio"
                id="salesMarginRadio"
                name="salesActiveField"
                value="margin"
                checked={costCalculation.salesActiveRow === 'MARGIN'}
                onChange={() => updateCostField('salesActiveRow', 'MARGIN')}
                className="h-4 w-4"
              />
              <FieldLabel htmlFor="salesMarginRadio" optional>
                Margin
              </FieldLabel>
            </div>
            <CurrencyInput
              value={costCalculation.salesMarginNet}
              onChange={(value) => updateMargin('net', value)}
              disabled={costCalculation.salesCalculationMode === 'GROSS' || costCalculation.salesActiveRow !== 'MARGIN'}
            />
            <CurrencyInput value={costCalculation.salesMarginTax} onChange={() => {}} disabled />
            <CurrencyInput
              value={costCalculation.salesMarginGross}
              onChange={(value) => updateMargin('gross', value)}
              disabled={costCalculation.salesCalculationMode === 'NET' || costCalculation.salesActiveRow !== 'MARGIN'}
            />
          </div>

          {/* Margin % */}
          <div className="grid grid-cols-4 items-center gap-4">
            <div className="flex items-center gap-2">
              <input
                type="radio"
                id="salesMarginPercentRadio"
                name="salesActiveField"
                value="marginPercent"
                checked={costCalculation.salesActiveRow === 'MARGIN_PERCENT'}
                onChange={() => updateCostField('salesActiveRow', 'MARGIN_PERCENT')}
                className="h-4 w-4"
              />
              <FieldLabel htmlFor="salesMarginPercentRadio" optional>
                Margin %
              </FieldLabel>
            </div>
            <CurrencyInput
              value={costCalculation.salesMarginPercent}
              onChange={updateMarginPercent}
              disabled={costCalculation.salesActiveRow !== 'MARGIN_PERCENT'}
              min={0}
              currency="%"
            />
            <div></div>
            <div className="flex items-center space-x-2">
              <CurrencyInput value={costCalculation.salesMarginPercent} onChange={() => {}} disabled min={0} currency="%" />
            </div>
          </div>

          {/* Sales Total */}
          <div className="grid grid-cols-4 items-center gap-4 font-semibold">
            <div className="flex items-center gap-2">
              <input
                type="radio"
                id="salesTotalRadio"
                name="salesActiveField"
                value="total"
                checked={costCalculation.salesActiveRow === 'TOTAL'}
                onChange={() => updateCostField('salesActiveRow', 'TOTAL')}
                className="h-4 w-4"
              />
              <FieldLabel htmlFor="salesTotalRadio">Sales Total</FieldLabel>
            </div>
            <CurrencyInput
              value={costCalculation.salesTotalNet}
              onChange={(value) => updateSalesTotal('net', value)}
              disabled={costCalculation.salesCalculationMode === 'GROSS' || costCalculation.salesActiveRow !== 'TOTAL'}
            />
            <CurrencyInput value={costCalculation.salesTotalTax} onChange={() => {}} disabled />
            <CurrencyInput
              value={costCalculation.salesTotalGross}
              onChange={(value) => updateSalesTotal('gross', value)}
              disabled={costCalculation.salesCalculationMode === 'NET' || costCalculation.salesActiveRow !== 'TOTAL'}
            />
          </div>

          {/* Price corresponds checkbox */}
          <div className="flex items-center space-x-2 pt-2">
            <Checkbox
              id="salesPriceCorresponds"
              checked={costCalculation.salesPriceCorresponds}
              onCheckedChange={(checked) => updateCostField('salesPriceCorresponds', checked === true)}
            />
            <FieldLabel htmlFor="salesPriceCorresponds" className="text-sm font-normal" optional>
              Price corresponds to
            </FieldLabel>
            <Input
              type="text"
              value={costCalculation.salesPriceUnit}
              onChange={(e) => updateCostField('salesPriceUnit', e.target.value)}
              className="w-32"
              placeholder="1.00"
            />
            <span className="text-muted-foreground text-sm">Quantity unit(s) or packaging</span>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
