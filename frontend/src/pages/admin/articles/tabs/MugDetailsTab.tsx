import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/Card';
import { Input } from '@/components/ui/Input';
import { Label } from '@/components/ui/Label';
import { Switch } from '@/components/ui/Switch';
import type { CreateMugDetailsRequest } from '@/types/article';

interface MugDetailsTabProps {
  mugDetails?: Partial<CreateMugDetailsRequest>;
  onChange: (details: CreateMugDetailsRequest) => void;
}

export default function MugDetailsTab({ mugDetails, onChange }: MugDetailsTabProps) {
  const details: CreateMugDetailsRequest = {
    heightMm: mugDetails?.heightMm || 0,
    diameterMm: mugDetails?.diameterMm || 0,
    printTemplateWidthMm: mugDetails?.printTemplateWidthMm || 0,
    printTemplateHeightMm: mugDetails?.printTemplateHeightMm || 0,
    fillingQuantity: mugDetails?.fillingQuantity || '',
    dishwasherSafe: mugDetails?.dishwasherSafe ?? true,
  };

  const handleChange = (field: keyof CreateMugDetailsRequest, value: any) => {
    // Ensure numeric fields are integers
    if (['heightMm', 'diameterMm', 'printTemplateWidthMm', 'printTemplateHeightMm'].includes(field)) {
      value = Math.round(value) || 0;
    }
    onChange({ ...details, [field]: value });
  };

  return (
    <Card>
      <CardHeader>
        <CardTitle>Mug Specifications</CardTitle>
        <CardDescription>Physical dimensions and properties</CardDescription>
      </CardHeader>
      <CardContent className="space-y-4">
        <div className="grid grid-cols-2 gap-4">
          <div className="space-y-2">
            <Label htmlFor="heightMm">Height (mm) *</Label>
            <Input
              id="heightMm"
              type="number"
              value={details.heightMm}
              onChange={(e) => handleChange('heightMm', Number(e.target.value))}
              placeholder="95"
              min="0"
              step="1"
            />
          </div>

          <div className="space-y-2">
            <Label htmlFor="diameterMm">Diameter (mm) *</Label>
            <Input
              id="diameterMm"
              type="number"
              value={details.diameterMm}
              onChange={(e) => handleChange('diameterMm', Number(e.target.value))}
              placeholder="82"
              min="0"
              step="1"
            />
          </div>
        </div>

        <div className="grid grid-cols-2 gap-4">
          <div className="space-y-2">
            <Label htmlFor="printTemplateWidthMm">Print Template Width (mm) *</Label>
            <Input
              id="printTemplateWidthMm"
              type="number"
              value={details.printTemplateWidthMm}
              onChange={(e) => handleChange('printTemplateWidthMm', Number(e.target.value))}
              placeholder="200"
              min="0"
              step="1"
            />
          </div>

          <div className="space-y-2">
            <Label htmlFor="printTemplateHeightMm">Print Template Height (mm) *</Label>
            <Input
              id="printTemplateHeightMm"
              type="number"
              value={details.printTemplateHeightMm}
              onChange={(e) => handleChange('printTemplateHeightMm', Number(e.target.value))}
              placeholder="80"
              min="0"
              step="1"
            />
          </div>
        </div>

        <div className="space-y-2">
          <Label htmlFor="fillingQuantity">Filling Quantity</Label>
          <Input
            id="fillingQuantity"
            value={details.fillingQuantity}
            onChange={(e) => handleChange('fillingQuantity', e.target.value)}
            placeholder="250ml"
          />
        </div>

        <div className="flex items-center space-x-2">
          <Switch id="dishwasherSafe" checked={details.dishwasherSafe} onCheckedChange={(checked) => handleChange('dishwasherSafe', checked)} />
          <Label htmlFor="dishwasherSafe">Dishwasher Safe</Label>
        </div>
      </CardContent>
    </Card>
  );
}
