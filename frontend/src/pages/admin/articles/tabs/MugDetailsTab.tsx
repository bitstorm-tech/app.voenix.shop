import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/Card';
import { FieldLabel } from '@/components/ui/FieldLabel';
import { Input } from '@/components/ui/Input';
import { Switch } from '@/components/ui/Switch';
import { useArticleFormStore } from '@/stores/admin/articles/useArticleFormStore';
import type { CreateMugDetailsRequest } from '@/types/article';

export default function MugDetailsTab() {
  const { article, updateMugDetails } = useArticleFormStore();
  const mugDetails = article.mugDetails;

  const handleChange = (field: keyof CreateMugDetailsRequest, value: string | number | boolean) => {
    // Ensure numeric fields are integers
    if (['heightMm', 'diameterMm', 'printTemplateWidthMm', 'printTemplateHeightMm'].includes(field) && typeof value === 'number') {
      value = Math.round(value) || 0;
    }
    updateMugDetails({ [field]: value });
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
            <FieldLabel htmlFor="heightMm" required>
              Height (mm)
            </FieldLabel>
            <Input
              id="heightMm"
              type="number"
              value={mugDetails?.heightMm || 0}
              onChange={(e) => handleChange('heightMm', Number(e.target.value))}
              placeholder="95"
              min="0"
              step="1"
            />
          </div>

          <div className="space-y-2">
            <FieldLabel htmlFor="diameterMm" required>
              Diameter (mm)
            </FieldLabel>
            <Input
              id="diameterMm"
              type="number"
              value={mugDetails?.diameterMm || 0}
              onChange={(e) => handleChange('diameterMm', Number(e.target.value))}
              placeholder="82"
              min="0"
              step="1"
            />
          </div>
        </div>

        <div className="grid grid-cols-2 gap-4">
          <div className="space-y-2">
            <FieldLabel htmlFor="printTemplateWidthMm" required>
              Print Template Width (mm)
            </FieldLabel>
            <Input
              id="printTemplateWidthMm"
              type="number"
              value={mugDetails?.printTemplateWidthMm || 0}
              onChange={(e) => handleChange('printTemplateWidthMm', Number(e.target.value))}
              placeholder="200"
              min="0"
              step="1"
            />
          </div>

          <div className="space-y-2">
            <FieldLabel htmlFor="printTemplateHeightMm" required>
              Print Template Height (mm)
            </FieldLabel>
            <Input
              id="printTemplateHeightMm"
              type="number"
              value={mugDetails?.printTemplateHeightMm || 0}
              onChange={(e) => handleChange('printTemplateHeightMm', Number(e.target.value))}
              placeholder="80"
              min="0"
              step="1"
            />
          </div>
        </div>

        <div className="space-y-2">
          <FieldLabel htmlFor="fillingQuantity" optional>
            Filling Quantity
          </FieldLabel>
          <Input
            id="fillingQuantity"
            value={mugDetails?.fillingQuantity || ''}
            onChange={(e) => handleChange('fillingQuantity', e.target.value)}
            placeholder="250ml"
          />
        </div>

        <div className="flex items-center space-x-2">
          <Switch
            id="dishwasherSafe"
            checked={mugDetails?.dishwasherSafe ?? true}
            onCheckedChange={(checked) => handleChange('dishwasherSafe', checked)}
          />
          <FieldLabel htmlFor="dishwasherSafe">Dishwasher Safe</FieldLabel>
        </div>
      </CardContent>
    </Card>
  );
}
