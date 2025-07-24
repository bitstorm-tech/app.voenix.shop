import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/Card';
import { Checkbox } from '@/components/ui/Checkbox';
import { FieldLabel } from '@/components/ui/FieldLabel';
import { Input } from '@/components/ui/Input';
import { Label } from '@/components/ui/Label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/Select';
import { Textarea } from '@/components/ui/Textarea';
import type { CreateShirtDetailsRequest, FitType } from '@/types/article';

interface ShirtDetailsTabProps {
  shirtDetails?: Partial<CreateShirtDetailsRequest>;
  onChange: (details: CreateShirtDetailsRequest) => void;
}

const AVAILABLE_SIZES = ['XS', 'S', 'M', 'L', 'XL', 'XXL', 'XXXL'];

export default function ShirtDetailsTab({ shirtDetails, onChange }: ShirtDetailsTabProps) {
  const details: CreateShirtDetailsRequest = {
    material: shirtDetails?.material || '',
    careInstructions: shirtDetails?.careInstructions || '',
    fitType: shirtDetails?.fitType || 'REGULAR',
    availableSizes: shirtDetails?.availableSizes || [],
  };

  const handleChange = (field: keyof CreateShirtDetailsRequest, value: any) => {
    onChange({ ...details, [field]: value });
  };

  const handleSizeToggle = (size: string) => {
    const newSizes = details.availableSizes.includes(size) ? details.availableSizes.filter((s) => s !== size) : [...details.availableSizes, size];
    handleChange('availableSizes', newSizes);
  };

  return (
    <Card>
      <CardHeader>
        <CardTitle>Shirt Details</CardTitle>
        <CardDescription>Material and sizing information</CardDescription>
      </CardHeader>
      <CardContent className="space-y-4">
        <div className="space-y-2">
          <FieldLabel htmlFor="material" required>
            Material
          </FieldLabel>
          <Input id="material" value={details.material} onChange={(e) => handleChange('material', e.target.value)} placeholder="100% Cotton" />
        </div>

        <div className="space-y-2">
          <FieldLabel htmlFor="fitType" required>
            Fit Type
          </FieldLabel>
          <Select value={details.fitType} onValueChange={(value) => handleChange('fitType', value as FitType)}>
            <SelectTrigger id="fitType">
              <SelectValue placeholder="Select fit type" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="REGULAR">Regular</SelectItem>
              <SelectItem value="SLIM">Slim</SelectItem>
              <SelectItem value="LOOSE">Loose</SelectItem>
            </SelectContent>
          </Select>
        </div>

        <div className="space-y-2">
          <FieldLabel htmlFor="careInstructions" optional>
            Care Instructions
          </FieldLabel>
          <Textarea
            id="careInstructions"
            value={details.careInstructions}
            onChange={(e) => handleChange('careInstructions', e.target.value)}
            placeholder="Machine wash cold, tumble dry low"
            rows={3}
          />
        </div>

        <div className="space-y-2">
          <FieldLabel required>Available Sizes</FieldLabel>
          <div className="grid grid-cols-4 gap-3">
            {AVAILABLE_SIZES.map((size) => (
              <div key={size} className="flex items-center space-x-2">
                <Checkbox id={`size-${size}`} checked={details.availableSizes.includes(size)} onCheckedChange={() => handleSizeToggle(size)} />
                <Label htmlFor={`size-${size}`} className="cursor-pointer text-sm font-normal">
                  {size}
                </Label>
              </div>
            ))}
          </div>
        </div>
      </CardContent>
    </Card>
  );
}
