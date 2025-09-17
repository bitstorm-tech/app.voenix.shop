import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/Card';
import { CurrencyInput } from '@/components/ui/CurrencyInput';
import { FieldLabel } from '@/components/ui/FieldLabel';
import { Input } from '@/components/ui/Input';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/Select';
import { useVats } from '@/hooks/queries/useVat';
import { usePromptPriceStore } from '@/stores/admin/prompts/usePromptPriceStore';
import { useCallback, useEffect } from 'react';
import { useTranslation } from 'react-i18next';

export default function PriceCalculationTab() {
  const { t } = useTranslation('admin');
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

  const handlePurchasePriceCorrespondsChange = useCallback(
    (value: string) => {
      if (value === 'NET' || value === 'GROSS') {
        updateCostField('purchasePriceCorresponds', value);
      }
    },
    [updateCostField],
  );

  const handleSalesPriceCorrespondsChange = useCallback(
    (value: string) => {
      if (value === 'NET' || value === 'GROSS') {
        updateCostField('salesPriceCorresponds', value);
      }
    },
    [updateCostField],
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
          <CardTitle>{t('prompt.priceCalculation.purchase.title')}</CardTitle>
          <CardDescription>{t('prompt.priceCalculation.purchase.description')}</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          {/* Tax Rate */}
          <div className="grid grid-cols-4 items-center gap-4">
            <FieldLabel optional>{t('prompt.priceCalculation.purchase.taxRateLabel')}</FieldLabel>
            <Select value={costCalculation.purchaseVatRateId?.toString() || ''} onValueChange={handlePurchaseVatRateChange}>
              <SelectTrigger>
                <SelectValue placeholder={t('prompt.priceCalculation.purchase.taxRatePlaceholder')} />
              </SelectTrigger>
              <SelectContent>
                {vats.map((vat) => (
                  <SelectItem key={vat.id} value={vat.id.toString()}>
                    {vat.name} ({vat.percent}%)
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
            <div className="text-muted-foreground col-span-2 text-sm">{t('prompt.priceCalculation.purchase.taxRateHint')}</div>
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
              <span>{t('prompt.priceCalculation.purchase.mode.net')}</span>
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
              <span>{t('prompt.priceCalculation.purchase.mode.gross')}</span>
            </label>
          </div>

          {/* Headers */}
          <div className="text-muted-foreground grid grid-cols-4 gap-4 text-sm font-medium">
            <div></div>
            <div className="text-center">{t('prompt.priceCalculation.purchase.headers.net')}</div>
            <div className="text-center">{t('prompt.priceCalculation.purchase.headers.tax')}</div>
            <div className="text-center">{t('prompt.priceCalculation.purchase.headers.gross')}</div>
          </div>

          {/* Purchase Price */}
          <div className="grid grid-cols-4 items-center gap-4">
            <FieldLabel optional>{t('prompt.priceCalculation.purchase.priceLabel')}</FieldLabel>
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
                {t('prompt.priceCalculation.purchase.costLabel')}
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
                {t('prompt.priceCalculation.purchase.costPercentLabel')}
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
            <FieldLabel>{t('prompt.priceCalculation.purchase.totalLabel')}</FieldLabel>
            <CurrencyInput value={costCalculation.purchaseTotalNet} onChange={() => {}} disabled />
            <CurrencyInput value={costCalculation.purchaseTotalTax} onChange={() => {}} disabled />
            <CurrencyInput value={costCalculation.purchaseTotalGross} onChange={() => {}} disabled />
          </div>

          {/* Price corresponds selection */}
          <div className="flex flex-wrap items-center gap-2 pt-2">
            <FieldLabel htmlFor="purchasePriceCorresponds" className="text-sm font-normal" optional>
              {t('prompt.priceCalculation.purchase.priceCorrespondsLabel')}
            </FieldLabel>
            <Select value={costCalculation.purchasePriceCorresponds} onValueChange={handlePurchasePriceCorrespondsChange}>
              <SelectTrigger id="purchasePriceCorresponds" className="w-32">
                <SelectValue placeholder={t('prompt.priceCalculation.purchase.priceCorrespondsPlaceholder')} />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="NET">{t('prompt.priceCalculation.purchase.mode.net')}</SelectItem>
                <SelectItem value="GROSS">{t('prompt.priceCalculation.purchase.mode.gross')}</SelectItem>
              </SelectContent>
            </Select>
            <Input
              type="text"
              value={costCalculation.purchasePriceUnit}
              onChange={(e) => updateCostField('purchasePriceUnit', e.target.value)}
              className="w-32"
              placeholder={t('prompt.priceCalculation.purchase.unitPlaceholder')}
            />
            <span className="text-muted-foreground text-sm">{t('prompt.priceCalculation.purchase.unitHint')}</span>
          </div>
        </CardContent>
      </Card>

      {/* Sales Section */}
      <Card>
        <CardHeader>
          <CardTitle>{t('prompt.priceCalculation.sales.title')}</CardTitle>
          <CardDescription>{t('prompt.priceCalculation.sales.description')}</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          {/* Tax Rate */}
          <div className="grid grid-cols-4 items-center gap-4">
            <FieldLabel optional>{t('prompt.priceCalculation.sales.taxRateLabel')}</FieldLabel>
            <Select value={costCalculation.salesVatRateId?.toString() || ''} onValueChange={handleSalesVatRateChange}>
              <SelectTrigger>
                <SelectValue placeholder={t('prompt.priceCalculation.sales.taxRatePlaceholder')} />
              </SelectTrigger>
              <SelectContent>
                {vats.map((vat) => (
                  <SelectItem key={vat.id} value={vat.id.toString()}>
                    {vat.name} ({vat.percent}%)
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
            <div className="text-muted-foreground col-span-2 text-sm">{t('prompt.priceCalculation.sales.taxRateHint')}</div>
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
              <span>{t('prompt.priceCalculation.sales.mode.net')}</span>
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
              <span>{t('prompt.priceCalculation.sales.mode.gross')}</span>
            </label>
          </div>

          {/* Headers */}
          <div className="text-muted-foreground grid grid-cols-4 gap-4 text-sm font-medium">
            <div></div>
            <div className="text-center">{t('prompt.priceCalculation.sales.headers.net')}</div>
            <div className="text-center">{t('prompt.priceCalculation.sales.headers.tax')}</div>
            <div className="text-center">{t('prompt.priceCalculation.sales.headers.gross')}</div>
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
                {t('prompt.priceCalculation.sales.marginLabel')}
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
                {t('prompt.priceCalculation.sales.marginPercentLabel')}
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
              <FieldLabel htmlFor="salesTotalRadio">{t('prompt.priceCalculation.sales.totalLabel')}</FieldLabel>
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

          {/* Price corresponds selection */}
          <div className="flex flex-wrap items-center gap-2 pt-2">
            <FieldLabel htmlFor="salesPriceCorresponds" className="text-sm font-normal" optional>
              {t('prompt.priceCalculation.sales.priceCorrespondsLabel')}
            </FieldLabel>
            <Select value={costCalculation.salesPriceCorresponds} onValueChange={handleSalesPriceCorrespondsChange}>
              <SelectTrigger id="salesPriceCorresponds" className="w-32">
                <SelectValue placeholder={t('prompt.priceCalculation.sales.priceCorrespondsPlaceholder')} />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="NET">{t('prompt.priceCalculation.sales.mode.net')}</SelectItem>
                <SelectItem value="GROSS">{t('prompt.priceCalculation.sales.mode.gross')}</SelectItem>
              </SelectContent>
            </Select>
            <Input
              type="text"
              value={costCalculation.salesPriceUnit}
              onChange={(e) => updateCostField('salesPriceUnit', e.target.value)}
              className="w-32"
              placeholder={t('prompt.priceCalculation.sales.unitPlaceholder')}
            />
            <span className="text-muted-foreground text-sm">{t('prompt.priceCalculation.sales.unitHint')}</span>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
