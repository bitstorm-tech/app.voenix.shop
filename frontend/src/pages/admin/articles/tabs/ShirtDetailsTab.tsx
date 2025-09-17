import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/Card';
import { Checkbox } from '@/components/ui/Checkbox';
import { FieldLabel } from '@/components/ui/FieldLabel';
import { Input } from '@/components/ui/Input';
import { Label } from '@/components/ui/Label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/Select';
import { Textarea } from '@/components/ui/Textarea';
import type { CreateShirtDetailsRequest, FitType } from '@/types/article';
import { useTranslation } from 'react-i18next';

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
  const { t } = useTranslation('adminArticles');

  const handleChange = (field: keyof CreateShirtDetailsRequest, value: string | string[] | FitType) => {
    onChange({ ...details, [field]: value });
  };

  const handleSizeToggle = (size: string) => {
    const newSizes = details.availableSizes.includes(size) ? details.availableSizes.filter((s) => s !== size) : [...details.availableSizes, size];
    handleChange('availableSizes', newSizes);
  };

  return (
    <Card>
      <CardHeader>
        <CardTitle>{t('form.shirtDetails.title')}</CardTitle>
        <CardDescription>{t('form.shirtDetails.description')}</CardDescription>
      </CardHeader>
      <CardContent className="space-y-4">
        <div className="space-y-2">
          <FieldLabel htmlFor="material" required>
            {t('form.shirtDetails.fields.material.label')}
          </FieldLabel>
          <Input
            id="material"
            value={details.material}
            onChange={(e) => handleChange('material', e.target.value)}
            placeholder={t('form.shirtDetails.fields.material.placeholder')}
          />
        </div>

        <div className="space-y-2">
          <FieldLabel htmlFor="fitType" required>
            {t('form.shirtDetails.fields.fitType.label')}
          </FieldLabel>
          <Select value={details.fitType} onValueChange={(value) => handleChange('fitType', value as FitType)}>
            <SelectTrigger id="fitType">
              <SelectValue placeholder={t('form.shirtDetails.fields.fitType.placeholder')} />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="REGULAR">{t('form.shirtDetails.fields.fitType.options.regular')}</SelectItem>
              <SelectItem value="SLIM">{t('form.shirtDetails.fields.fitType.options.slim')}</SelectItem>
              <SelectItem value="LOOSE">{t('form.shirtDetails.fields.fitType.options.loose')}</SelectItem>
            </SelectContent>
          </Select>
        </div>

        <div className="space-y-2">
          <FieldLabel htmlFor="careInstructions" optional>
            {t('form.shirtDetails.fields.careInstructions.label')}
          </FieldLabel>
          <Textarea
            id="careInstructions"
            value={details.careInstructions}
            onChange={(e) => handleChange('careInstructions', e.target.value)}
            placeholder={t('form.shirtDetails.fields.careInstructions.placeholder')}
            rows={3}
          />
        </div>

        <div className="space-y-2">
          <FieldLabel required>{t('form.shirtDetails.fields.availableSizes.label')}</FieldLabel>
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
