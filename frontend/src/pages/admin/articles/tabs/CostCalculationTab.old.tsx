import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/Card';
import { Checkbox } from '@/components/ui/Checkbox';
import { CurrencyInput } from '@/components/ui/CurrencyInput';
import { FieldLabel } from '@/components/ui/FieldLabel';
import { Input } from '@/components/ui/Input';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/Select';
import { useVats } from '@/hooks/queries/useVat';
import type { CostCalculation, CreateCostCalculationRequest } from '@/types/article';
import { useEffect, useState } from 'react';

interface CostCalculationTabProps {
  costCalculation?: Partial<CreateCostCalculationRequest>;
  onChange: (data: CreateCostCalculationRequest) => void;
}

export default function CostCalculationTab({ costCalculation, onChange }: CostCalculationTabProps) {
  const { data: vats = [] } = useVats();

  const [data, setData] = useState<CostCalculation>({
    // Purchase section
    purchasePriceNet: costCalculation?.purchasePriceNet || 0,
    purchasePriceTax: costCalculation?.purchasePriceTax || 0,
    purchasePriceGross: costCalculation?.purchasePriceGross || 0,
    purchaseCostNet: costCalculation?.purchaseCostNet || 0,
    purchaseCostTax: costCalculation?.purchaseCostTax || 0,
    purchaseCostGross: costCalculation?.purchaseCostGross || 0,
    purchaseCostPercent: costCalculation?.purchaseCostPercent || 0,
    purchaseTotalNet: costCalculation?.purchaseTotalNet || 0,
    purchaseTotalTax: costCalculation?.purchaseTotalTax || 0,
    purchaseTotalGross: costCalculation?.purchaseTotalGross || 0,
    purchasePriceUnit: costCalculation?.purchasePriceUnit || '1.00',

    // Purchase tax rate
    purchaseVatRateId: costCalculation?.purchaseVatRateId,
    purchaseVatRatePercent: costCalculation?.purchaseVatRatePercent || 19,

    // Sales section
    salesVatRateId: costCalculation?.salesVatRateId,
    salesVatRatePercent: costCalculation?.salesVatRatePercent || 19,
    marginNet: costCalculation?.marginNet || 0,
    marginTax: costCalculation?.marginTax || 0,
    marginGross: costCalculation?.marginGross || 0,
    marginPercent: costCalculation?.marginPercent || 100,
    salesTotalNet: costCalculation?.salesTotalNet || 0,
    salesTotalTax: costCalculation?.salesTotalTax || 0,
    salesTotalGross: costCalculation?.salesTotalGross || 0,
    salesPriceUnit: costCalculation?.salesPriceUnit || '1.00',

    // Calculation mode
    purchaseCalculationMode: costCalculation?.purchaseCalculationMode || 'NET',
    salesCalculationMode: costCalculation?.salesCalculationMode || 'NET',
  });

  const [purchasePriceCorresponds, setPurchasePriceCorresponds] = useState(false);
  const [salesPriceCorresponds, setSalesPriceCorresponds] = useState(false);
  const [purchaseActiveRow, setPurchaseActiveRow] = useState<'cost' | 'costPercent'>('cost');
  const [salesActiveRow, setSalesActiveRow] = useState<'margin' | 'marginPercent' | 'total'>('margin');

  // Update parent when data changes
  useEffect(() => {
    onChange(data);
  }, [data]);

  // Calculate purchase totals
  useEffect(() => {
    const purchaseTotal = data.purchasePriceNet + data.purchaseCostNet;
    const purchaseTaxTotal = data.purchasePriceTax + data.purchaseCostTax;
    const purchaseGrossTotal = data.purchasePriceGross + data.purchaseCostGross;

    setData((prev) => ({
      ...prev,
      purchaseTotalNet: purchaseTotal,
      purchaseTotalTax: purchaseTaxTotal,
      purchaseTotalGross: purchaseGrossTotal,
    }));
  }, [data.purchasePriceNet, data.purchasePriceTax, data.purchasePriceGross, data.purchaseCostNet, data.purchaseCostTax, data.purchaseCostGross]);

  // Recalculate purchase values when purchase VAT rate changes
  useEffect(() => {
    const taxRate = data.purchaseVatRatePercent / 100;

    if (data.purchaseCalculationMode === 'NET') {
      // Recalculate tax and gross from net values
      const priceTax = data.purchasePriceNet * taxRate;
      const costTax = data.purchaseCostNet * taxRate;

      setData((prev) => ({
        ...prev,
        purchasePriceTax: priceTax,
        purchasePriceGross: data.purchasePriceNet + priceTax,
        purchaseCostTax: costTax,
        purchaseCostGross: data.purchaseCostNet + costTax,
      }));
    } else {
      // Recalculate net and tax from gross values
      const priceNet = data.purchasePriceGross / (1 + taxRate);
      const priceTax = data.purchasePriceGross - priceNet;
      const costNet = data.purchaseCostGross / (1 + taxRate);
      const costTax = data.purchaseCostGross - costNet;

      setData((prev) => ({
        ...prev,
        purchasePriceNet: priceNet,
        purchasePriceTax: priceTax,
        purchaseCostNet: costNet,
        purchaseCostTax: costTax,
      }));
    }
  }, [data.purchaseVatRatePercent]);

  // Calculate sales totals and margins
  useEffect(() => {
    // Skip automatic calculation if user is directly editing sales total
    if (salesActiveRow === 'total') {
      return;
    }

    const salesNet = data.purchaseTotalNet * (1 + data.marginPercent / 100);
    const salesTax = salesNet * (data.salesVatRatePercent / 100);
    const salesGross = salesNet + salesTax;

    const marginNet = salesNet - data.purchaseTotalNet;
    const marginTax = salesTax - data.purchaseTotalTax;
    const marginGross = salesGross - data.purchaseTotalGross;

    setData((prev) => ({
      ...prev,
      salesTotalNet: salesNet,
      salesTotalTax: salesTax,
      salesTotalGross: salesGross,
      marginNet: marginNet,
      marginTax: marginTax,
      marginGross: marginGross,
    }));
  }, [data.purchaseTotalNet, data.purchaseTotalTax, data.purchaseTotalGross, data.marginPercent, data.salesVatRatePercent, salesActiveRow]);

  const handlePurchasePriceChange = (field: 'net' | 'tax' | 'gross', value: number) => {
    const taxRate = data.purchaseVatRatePercent / 100;
    if (data.purchaseCalculationMode === 'NET') {
      if (field === 'net') {
        const tax = value * taxRate;
        setData((prev) => ({
          ...prev,
          purchasePriceNet: value,
          purchasePriceTax: tax,
          purchasePriceGross: value + tax,
        }));
      }
    } else {
      if (field === 'gross') {
        const net = value / (1 + taxRate);
        const tax = value - net;
        setData((prev) => ({
          ...prev,
          purchasePriceNet: net,
          purchasePriceTax: tax,
          purchasePriceGross: value,
        }));
      }
    }
  };

  const handlePurchaseCostChange = (field: 'net' | 'tax' | 'gross', value: number) => {
    const taxRate = data.purchaseVatRatePercent / 100;
    if (data.purchaseCalculationMode === 'NET') {
      if (field === 'net') {
        const tax = value * taxRate;
        setData((prev) => ({
          ...prev,
          purchaseCostNet: value,
          purchaseCostTax: tax,
          purchaseCostGross: value + tax,
        }));
      }
    } else {
      if (field === 'gross') {
        const net = value / (1 + taxRate);
        const tax = value - net;
        setData((prev) => ({
          ...prev,
          purchaseCostNet: net,
          purchaseCostTax: tax,
          purchaseCostGross: value,
        }));
      }
    }
  };

  const handlePurchaseCostPercentChange = (value: number) => {
    const costNet = data.purchasePriceNet * (value / 100);
    const taxRate = data.purchaseVatRatePercent / 100;
    const costTax = costNet * taxRate;
    const costGross = costNet + costTax;

    setData((prev) => ({
      ...prev,
      purchaseCostPercent: value,
      purchaseCostNet: costNet,
      purchaseCostTax: costTax,
      purchaseCostGross: costGross,
    }));
  };

  const handlePurchaseVatRateChange = (vatRateId: string) => {
    const selectedVat = vats.find((v) => v.id.toString() === vatRateId);
    if (selectedVat) {
      setData((prev) => ({
        ...prev,
        purchaseVatRateId: selectedVat.id,
        purchaseVatRatePercent: selectedVat.percent,
      }));
    }
  };

  const handleSalesVatRateChange = (vatRateId: string) => {
    const selectedVat = vats.find((v) => v.id.toString() === vatRateId);
    if (selectedVat) {
      setData((prev) => ({
        ...prev,
        salesVatRateId: selectedVat.id,
        salesVatRatePercent: selectedVat.percent,
      }));
    }
  };

  const handleMarginChange = (field: 'net' | 'tax' | 'gross', value: number) => {
    if (data.salesCalculationMode === 'NET') {
      if (field === 'net') {
        const newSalesNet = data.purchaseTotalNet + value;
        const newSalesTax = newSalesNet * (data.salesVatRatePercent / 100);
        const newSalesGross = newSalesNet + newSalesTax;
        const newMarginPercent = data.purchaseTotalNet > 0 ? (value / data.purchaseTotalNet) * 100 : 0;

        setData((prev) => ({
          ...prev,
          marginNet: value,
          marginTax: newSalesTax - data.purchaseTotalTax,
          marginGross: newSalesGross - data.purchaseTotalGross,
          marginPercent: newMarginPercent,
          salesTotalNet: newSalesNet,
          salesTotalTax: newSalesTax,
          salesTotalGross: newSalesGross,
        }));
      }
    } else {
      if (field === 'gross') {
        const newSalesGross = data.purchaseTotalGross + value;
        const newSalesNet = newSalesGross / (1 + data.salesVatRatePercent / 100);
        const newSalesTax = newSalesGross - newSalesNet;
        const newMarginPercent = data.purchaseTotalNet > 0 ? ((newSalesNet - data.purchaseTotalNet) / data.purchaseTotalNet) * 100 : 0;

        setData((prev) => ({
          ...prev,
          marginNet: newSalesNet - data.purchaseTotalNet,
          marginTax: newSalesTax - data.purchaseTotalTax,
          marginGross: value,
          marginPercent: newMarginPercent,
          salesTotalNet: newSalesNet,
          salesTotalTax: newSalesTax,
          salesTotalGross: newSalesGross,
        }));
      }
    }
  };

  const handleSalesTotalChange = (field: 'net' | 'tax' | 'gross', value: number) => {
    const taxRate = data.salesVatRatePercent / 100;

    if (data.salesCalculationMode === 'NET') {
      if (field === 'net') {
        const tax = value * taxRate;
        const gross = value + tax;
        const marginNet = value - data.purchaseTotalNet;
        const marginTax = tax - data.purchaseTotalTax;
        const marginGross = gross - data.purchaseTotalGross;
        const marginPercent = data.purchaseTotalNet > 0 ? (marginNet / data.purchaseTotalNet) * 100 : 0;

        setData((prev) => ({
          ...prev,
          salesTotalNet: value,
          salesTotalTax: tax,
          salesTotalGross: gross,
          marginNet: marginNet,
          marginTax: marginTax,
          marginGross: marginGross,
          marginPercent: marginPercent,
        }));
      }
    } else {
      if (field === 'gross') {
        const net = value / (1 + taxRate);
        const tax = value - net;
        const marginNet = net - data.purchaseTotalNet;
        const marginTax = tax - data.purchaseTotalTax;
        const marginGross = value - data.purchaseTotalGross;
        const marginPercent = data.purchaseTotalNet > 0 ? (marginNet / data.purchaseTotalNet) * 100 : 0;

        setData((prev) => ({
          ...prev,
          salesTotalNet: net,
          salesTotalTax: tax,
          salesTotalGross: value,
          marginNet: marginNet,
          marginTax: marginTax,
          marginGross: marginGross,
          marginPercent: marginPercent,
        }));
      }
    }
  };

  return (
    <div className="space-y-6">
      {/* Purchase Section */}
      <Card>
        <CardHeader>
          <CardTitle>Purchase</CardTitle>
          <CardDescription>Purchase price and cost calculations</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          {/* Tax Rate */}
          <div className="grid grid-cols-4 items-center gap-4">
            <FieldLabel optional>Tax Rate</FieldLabel>
            <Select value={data.purchaseVatRateId?.toString() || ''} onValueChange={handlePurchaseVatRateChange}>
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
                checked={data.purchaseCalculationMode === 'NET'}
                onChange={() => setData((prev) => ({ ...prev, purchaseCalculationMode: 'NET' }))}
                className="h-4 w-4"
              />
              <span>Net</span>
            </label>
            <label className="flex items-center space-x-2">
              <input
                type="radio"
                name="purchaseMode"
                value="GROSS"
                checked={data.purchaseCalculationMode === 'GROSS'}
                onChange={() => setData((prev) => ({ ...prev, purchaseCalculationMode: 'GROSS' }))}
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
              value={data.purchasePriceNet}
              onChange={(value) => handlePurchasePriceChange('net', value)}
              disabled={data.purchaseCalculationMode === 'GROSS'}
              min={0}
            />
            <CurrencyInput value={data.purchasePriceTax} onChange={() => {}} disabled min={0} />
            <CurrencyInput
              value={data.purchasePriceGross}
              onChange={(value) => handlePurchasePriceChange('gross', value)}
              disabled={data.purchaseCalculationMode === 'NET'}
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
                checked={purchaseActiveRow === 'cost'}
                onChange={() => setPurchaseActiveRow('cost')}
                className="h-4 w-4"
              />
              <FieldLabel htmlFor="purchaseCostRadio" optional>
                Purchase Cost
              </FieldLabel>
            </div>
            <CurrencyInput
              value={data.purchaseCostNet}
              onChange={(value) => handlePurchaseCostChange('net', value)}
              disabled={data.purchaseCalculationMode === 'GROSS' || purchaseActiveRow !== 'cost'}
              min={0}
            />
            <CurrencyInput value={data.purchaseCostTax} onChange={() => {}} disabled min={0} />
            <CurrencyInput
              value={data.purchaseCostGross}
              onChange={(value) => handlePurchaseCostChange('gross', value)}
              disabled={data.purchaseCalculationMode === 'NET' || purchaseActiveRow !== 'cost'}
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
                checked={purchaseActiveRow === 'costPercent'}
                onChange={() => setPurchaseActiveRow('costPercent')}
                className="h-4 w-4"
              />
              <FieldLabel htmlFor="purchaseCostPercentRadio" optional>
                Purchase Cost %
              </FieldLabel>
            </div>
            <CurrencyInput
              value={data.purchaseCostPercent}
              onChange={handlePurchaseCostPercentChange}
              disabled={purchaseActiveRow !== 'costPercent'}
              min={0}
              max={100}
              currency="%"
            />
            <div></div>
            <div className="flex items-center space-x-2">
              <CurrencyInput value={data.purchaseCostPercent} onChange={() => {}} disabled min={0} currency="%" />
            </div>
          </div>

          {/* Purchase Total */}
          <div className="grid grid-cols-4 items-center gap-4 font-semibold">
            <FieldLabel>Purchase Total</FieldLabel>
            <CurrencyInput value={data.purchaseTotalNet} onChange={() => {}} disabled />
            <CurrencyInput value={data.purchaseTotalTax} onChange={() => {}} disabled />
            <CurrencyInput value={data.purchaseTotalGross} onChange={() => {}} disabled />
          </div>

          {/* Price corresponds checkbox */}
          <div className="flex items-center space-x-2 pt-2">
            <Checkbox
              id="purchasePriceCorresponds"
              checked={purchasePriceCorresponds}
              onCheckedChange={(checked) => setPurchasePriceCorresponds(checked === true)}
            />
            <FieldLabel htmlFor="purchasePriceCorresponds" className="text-sm font-normal" optional>
              Price corresponds to
            </FieldLabel>
            <Input
              type="text"
              value={data.purchasePriceUnit}
              onChange={(e) => setData((prev) => ({ ...prev, purchasePriceUnit: e.target.value }))}
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
            <Select value={data.salesVatRateId?.toString() || ''} onValueChange={handleSalesVatRateChange}>
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
                checked={data.salesCalculationMode === 'NET'}
                onChange={() => setData((prev) => ({ ...prev, salesCalculationMode: 'NET' }))}
                className="h-4 w-4"
              />
              <span>Net</span>
            </label>
            <label className="flex items-center space-x-2">
              <input
                type="radio"
                name="salesMode"
                value="GROSS"
                checked={data.salesCalculationMode === 'GROSS'}
                onChange={() => setData((prev) => ({ ...prev, salesCalculationMode: 'GROSS' }))}
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
                checked={salesActiveRow === 'margin'}
                onChange={() => setSalesActiveRow('margin')}
                className="h-4 w-4"
              />
              <FieldLabel htmlFor="salesMarginRadio" optional>
                Margin
              </FieldLabel>
            </div>
            <CurrencyInput
              value={data.marginNet}
              onChange={(value) => handleMarginChange('net', value)}
              disabled={data.salesCalculationMode === 'GROSS' || salesActiveRow !== 'margin'}
            />
            <CurrencyInput value={data.marginTax} onChange={() => {}} disabled />
            <CurrencyInput
              value={data.marginGross}
              onChange={(value) => handleMarginChange('gross', value)}
              disabled={data.salesCalculationMode === 'NET' || salesActiveRow !== 'margin'}
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
                checked={salesActiveRow === 'marginPercent'}
                onChange={() => setSalesActiveRow('marginPercent')}
                className="h-4 w-4"
              />
              <FieldLabel htmlFor="salesMarginPercentRadio" optional>
                Margin %
              </FieldLabel>
            </div>
            <CurrencyInput
              value={data.marginPercent}
              onChange={(value) => setData((prev) => ({ ...prev, marginPercent: value }))}
              disabled={salesActiveRow !== 'marginPercent'}
              min={0}
              currency="%"
            />
            <div></div>
            <div className="flex items-center space-x-2">
              <CurrencyInput value={data.marginPercent} onChange={() => {}} disabled min={0} currency="%" />
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
                checked={salesActiveRow === 'total'}
                onChange={() => setSalesActiveRow('total')}
                className="h-4 w-4"
              />
              <FieldLabel htmlFor="salesTotalRadio">Sales Total</FieldLabel>
            </div>
            <CurrencyInput
              value={data.salesTotalNet}
              onChange={(value) => handleSalesTotalChange('net', value)}
              disabled={data.salesCalculationMode === 'GROSS' || salesActiveRow !== 'total'}
            />
            <CurrencyInput value={data.salesTotalTax} onChange={() => {}} disabled />
            <CurrencyInput
              value={data.salesTotalGross}
              onChange={(value) => handleSalesTotalChange('gross', value)}
              disabled={data.salesCalculationMode === 'NET' || salesActiveRow !== 'total'}
            />
          </div>

          {/* Price corresponds checkbox */}
          <div className="flex items-center space-x-2 pt-2">
            <Checkbox
              id="salesPriceCorresponds"
              checked={salesPriceCorresponds}
              onCheckedChange={(checked) => setSalesPriceCorresponds(checked === true)}
            />
            <FieldLabel htmlFor="salesPriceCorresponds" className="text-sm font-normal" optional>
              Price corresponds to
            </FieldLabel>
            <Input
              type="text"
              value={data.salesPriceUnit}
              onChange={(e) => setData((prev) => ({ ...prev, salesPriceUnit: e.target.value }))}
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
