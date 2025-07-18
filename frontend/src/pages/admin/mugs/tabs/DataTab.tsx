import { Checkbox } from '@/components/ui/Checkbox';
import { Input } from '@/components/ui/Input';
import { Label } from '@/components/ui/Label';
import type { CreateMugRequest } from '@/lib/api';

interface DataTabProps {
  formData: CreateMugRequest;
  setFormData: (data: CreateMugRequest) => void;
}

export default function DataTab({ formData, setFormData }: DataTabProps) {
  return (
    <div className="space-y-6">
      <div className="space-y-2">
        <Label htmlFor="fillingQuantity">Filling Quantity</Label>
        <Input
          id="fillingQuantity"
          value={formData.fillingQuantity}
          onChange={(e) => setFormData({ ...formData, fillingQuantity: e.target.value })}
          placeholder="e.g., 330ml"
        />
        <p className="text-sm text-gray-500">The volume capacity of the mug</p>
      </div>

      <div className="grid grid-cols-2 gap-4">
        <div className="space-y-2">
          <Label htmlFor="heightMm">Height (mm)</Label>
          <Input
            id="heightMm"
            type="number"
            value={formData.heightMm}
            onChange={(e) => setFormData({ ...formData, heightMm: parseInt(e.target.value) || 0 })}
            placeholder="0"
            required
          />
        </div>

        <div className="space-y-2">
          <Label htmlFor="diameterMm">Diameter (mm)</Label>
          <Input
            id="diameterMm"
            type="number"
            value={formData.diameterMm}
            onChange={(e) => setFormData({ ...formData, diameterMm: parseInt(e.target.value) || 0 })}
            placeholder="0"
            required
          />
        </div>
      </div>

      <div className="space-y-4">
        <h3 className="text-sm font-medium">Print Template Dimensions</h3>
        <div className="grid grid-cols-2 gap-4">
          <div className="space-y-2">
            <Label htmlFor="printTemplateWidthMm">Width (mm)</Label>
            <Input
              id="printTemplateWidthMm"
              type="number"
              value={formData.printTemplateWidthMm}
              onChange={(e) => setFormData({ ...formData, printTemplateWidthMm: parseInt(e.target.value) || 0 })}
              placeholder="0"
              required
            />
          </div>

          <div className="space-y-2">
            <Label htmlFor="printTemplateHeightMm">Height (mm)</Label>
            <Input
              id="printTemplateHeightMm"
              type="number"
              value={formData.printTemplateHeightMm}
              onChange={(e) => setFormData({ ...formData, printTemplateHeightMm: parseInt(e.target.value) || 0 })}
              placeholder="0"
              required
            />
          </div>
        </div>
      </div>

      <div className="flex items-center space-x-2">
        <Checkbox
          id="dishwasherSafe"
          checked={formData.dishwasherSafe}
          onCheckedChange={(checked) => setFormData({ ...formData, dishwasherSafe: checked as boolean })}
        />
        <Label htmlFor="dishwasherSafe" className="text-sm font-normal">
          Dishwasher Safe
        </Label>
      </div>
    </div>
  );
}
