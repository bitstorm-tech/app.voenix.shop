import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/Card';
import { Checkbox } from '@/components/ui/Checkbox';
import { Input } from '@/components/ui/Input';
import { Label } from '@/components/ui/Label';
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

    // Sales section
    vatRateId: costCalculation?.vatRateId,
    vatRatePercent: costCalculation?.vatRatePercent || 19,
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
  const [purchaseActiveRow, setPurchaseActiveRow] = useState<'price' | 'cost' | 'costPercent'>('price');
  const [salesActiveRow, setSalesActiveRow] = useState<'margin' | 'marginPercent'>('margin');

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

  // Calculate sales totals and margins
  useEffect(() => {
    const salesNet = data.purchaseTotalNet * (1 + data.marginPercent / 100);
    const salesTax = salesNet * (data.vatRatePercent / 100);
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
  }, [data.purchaseTotalNet, data.purchaseTotalTax, data.purchaseTotalGross, data.marginPercent, data.vatRatePercent]);

  const handlePurchasePriceChange = (field: 'net' | 'tax' | 'gross', value: number) => {
    if (data.purchaseCalculationMode === 'NET') {
      if (field === 'net') {
        const tax = value * 0.19; // Default 19% tax
        setData((prev) => ({
          ...prev,
          purchasePriceNet: value,
          purchasePriceTax: tax,
          purchasePriceGross: value + tax,
        }));
      }
    } else {
      if (field === 'gross') {
        const net = value / 1.19; // Default 19% tax
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
    if (data.purchaseCalculationMode === 'NET') {
      if (field === 'net') {
        const tax = value * 0.19; // Default 19% tax
        setData((prev) => ({
          ...prev,
          purchaseCostNet: value,
          purchaseCostTax: tax,
          purchaseCostGross: value + tax,
        }));
      }
    } else {
      if (field === 'gross') {
        const net = value / 1.19; // Default 19% tax
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
    const costTax = costNet * 0.19; // Default 19% tax
    const costGross = costNet + costTax;

    setData((prev) => ({
      ...prev,
      purchaseCostPercent: value,
      purchaseCostNet: costNet,
      purchaseCostTax: costTax,
      purchaseCostGross: costGross,
    }));
  };

  const handleVatRateChange = (vatRateId: string) => {
    const selectedVat = vats.find((v) => v.id.toString() === vatRateId);
    if (selectedVat) {
      setData((prev) => ({
        ...prev,
        vatRateId: selectedVat.id,
        vatRatePercent: selectedVat.percent,
      }));
    }
  };

  const handleMarginChange = (field: 'net' | 'tax' | 'gross', value: number) => {
    if (data.salesCalculationMode === 'NET') {
      if (field === 'net') {
        const newSalesNet = data.purchaseTotalNet + value;
        const newSalesTax = newSalesNet * (data.vatRatePercent / 100);
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
        const newSalesNet = newSalesGross / (1 + data.vatRatePercent / 100);
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

  const formatCurrency = (value: number) => {
    return value.toFixed(2);
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
            <div className="flex items-center gap-2">
              <input
                type="radio"
                id="purchasePriceRadio"
                name="purchaseActiveField"
                value="price"
                checked={purchaseActiveRow === 'price'}
                onChange={() => setPurchaseActiveRow('price')}
                className="h-4 w-4"
              />
              <Label htmlFor="purchasePriceRadio">Purchase Price:</Label>
            </div>
            <Input
              type="number"
              value={formatCurrency(data.purchasePriceNet)}
              onChange={(e) => handlePurchasePriceChange('net', parseFloat(e.target.value) || 0)}
              disabled={data.purchaseCalculationMode === 'GROSS' || purchaseActiveRow !== 'price'}
              step="0.01"
              min="0"
            />
            <Input type="number" value={formatCurrency(data.purchasePriceTax)} disabled step="0.01" min="0" />
            <div className="flex items-center space-x-2">
              <Input
                type="number"
                value={formatCurrency(data.purchasePriceGross)}
                onChange={(e) => handlePurchasePriceChange('gross', parseFloat(e.target.value) || 0)}
                disabled={data.purchaseCalculationMode === 'NET' || purchaseActiveRow !== 'price'}
                step="0.01"
                min="0"
              />
              <span className="text-muted-foreground text-sm">EUR</span>
            </div>
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
              <Label htmlFor="purchaseCostRadio">Purchase Cost:</Label>
            </div>
            <Input
              type="number"
              value={formatCurrency(data.purchaseCostNet)}
              onChange={(e) => handlePurchaseCostChange('net', parseFloat(e.target.value) || 0)}
              disabled={data.purchaseCalculationMode === 'GROSS' || purchaseActiveRow !== 'cost'}
              step="0.01"
              min="0"
            />
            <Input type="number" value={formatCurrency(data.purchaseCostTax)} disabled step="0.01" min="0" />
            <div className="flex items-center space-x-2">
              <Input
                type="number"
                value={formatCurrency(data.purchaseCostGross)}
                onChange={(e) => handlePurchaseCostChange('gross', parseFloat(e.target.value) || 0)}
                disabled={data.purchaseCalculationMode === 'NET' || purchaseActiveRow !== 'cost'}
                step="0.01"
                min="0"
              />
              <span className="text-muted-foreground text-sm">EUR</span>
            </div>
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
              <Label htmlFor="purchaseCostPercentRadio">Purchase Cost %:</Label>
            </div>
            <Input
              type="number"
              value={formatCurrency(data.purchaseCostPercent)}
              onChange={(e) => handlePurchaseCostPercentChange(parseFloat(e.target.value) || 0)}
              disabled={purchaseActiveRow !== 'costPercent'}
              step="0.01"
              min="0"
              max="100"
            />
            <div></div>
            <div className="flex items-center space-x-2">
              <Input type="number" value={formatCurrency(data.purchaseCostPercent)} disabled step="0.01" min="0" />
              <span className="text-muted-foreground text-sm">%</span>
            </div>
          </div>

          {/* Purchase Total */}
          <div className="grid grid-cols-4 items-center gap-4 font-semibold">
            <Label>Purchase Total:</Label>
            <Input type="number" value={formatCurrency(data.purchaseTotalNet)} disabled step="0.01" />
            <Input type="number" value={formatCurrency(data.purchaseTotalTax)} disabled step="0.01" />
            <div className="flex items-center space-x-2">
              <Input type="number" value={formatCurrency(data.purchaseTotalGross)} disabled step="0.01" />
              <span className="text-muted-foreground text-sm">EUR</span>
            </div>
          </div>

          {/* Price corresponds checkbox */}
          <div className="flex items-center space-x-2 pt-2">
            <Checkbox
              id="purchasePriceCorresponds"
              checked={purchasePriceCorresponds}
              onCheckedChange={(checked) => setPurchasePriceCorresponds(checked === true)}
            />
            <Label htmlFor="purchasePriceCorresponds" className="text-sm font-normal">
              Price corresponds to
            </Label>
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
            <Label>Tax Rate:</Label>
            <Select value={data.vatRateId?.toString() || ''} onValueChange={handleVatRateChange}>
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
              <Label htmlFor="salesMarginRadio">Margin:</Label>
            </div>
            <Input
              type="number"
              value={formatCurrency(data.marginNet)}
              onChange={(e) => handleMarginChange('net', parseFloat(e.target.value) || 0)}
              disabled={data.salesCalculationMode === 'GROSS' || salesActiveRow !== 'margin'}
              step="0.01"
            />
            <Input type="number" value={formatCurrency(data.marginTax)} disabled step="0.01" />
            <div className="flex items-center space-x-2">
              <Input
                type="number"
                value={formatCurrency(data.marginGross)}
                onChange={(e) => handleMarginChange('gross', parseFloat(e.target.value) || 0)}
                disabled={data.salesCalculationMode === 'NET' || salesActiveRow !== 'margin'}
                step="0.01"
              />
              <span className="text-muted-foreground text-sm">EUR</span>
            </div>
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
              <Label htmlFor="salesMarginPercentRadio">Margin %:</Label>
            </div>
            <Input
              type="number"
              value={formatCurrency(data.marginPercent)}
              onChange={(e) => setData((prev) => ({ ...prev, marginPercent: parseFloat(e.target.value) || 0 }))}
              disabled={salesActiveRow !== 'marginPercent'}
              step="0.01"
              min="0"
            />
            <div></div>
            <div className="flex items-center space-x-2">
              <Input type="number" value={formatCurrency(data.marginPercent)} disabled step="0.01" />
              <span className="text-muted-foreground text-sm">%</span>
            </div>
          </div>

          {/* Sales Total */}
          <div className="grid grid-cols-4 items-center gap-4 font-semibold">
            <div className="flex items-center gap-2">
              <input type="radio" id="salesTotalRadio" name="salesActiveField" value="total" checked={false} disabled className="h-4 w-4" />
              <Label htmlFor="salesTotalRadio">Sales Total:</Label>
            </div>
            <Input type="number" value={formatCurrency(data.salesTotalNet)} disabled step="0.01" />
            <Input type="number" value={formatCurrency(data.salesTotalTax)} disabled step="0.01" />
            <div className="flex items-center space-x-2">
              <Input type="number" value={formatCurrency(data.salesTotalGross)} disabled step="0.01" />
              <span className="text-muted-foreground text-sm">EUR</span>
            </div>
          </div>

          {/* Price corresponds checkbox */}
          <div className="flex items-center space-x-2 pt-2">
            <Checkbox
              id="salesPriceCorresponds"
              checked={salesPriceCorresponds}
              onCheckedChange={(checked) => setSalesPriceCorresponds(checked === true)}
            />
            <Label htmlFor="salesPriceCorresponds" className="text-sm font-normal">
              Price corresponds to
            </Label>
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
