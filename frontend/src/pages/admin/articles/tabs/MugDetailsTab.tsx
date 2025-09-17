import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/Card';
import { FieldLabel } from '@/components/ui/FieldLabel';
import { Input } from '@/components/ui/Input';
import { Switch } from '@/components/ui/Switch';
import { useArticleFormStore } from '@/stores/admin/articles/useArticleFormStore';
import type { CreateMugDetailsRequest } from '@/types/article';
import { useTranslation } from 'react-i18next';

export default function MugDetailsTab() {
  const { article, updateMugDetails } = useArticleFormStore();
  const mugDetails = article.mugDetails;
  const { t } = useTranslation('adminArticles');

  const handleChange = (field: keyof CreateMugDetailsRequest, value: string | number | boolean | undefined) => {
    // Ensure numeric fields are integers
    const numericFields = [
      'heightMm',
      'diameterMm',
      'printTemplateWidthMm',
      'printTemplateHeightMm',
      'documentFormatWidthMm',
      'documentFormatHeightMm',
      'documentFormatMarginBottomMm',
    ];
    if (numericFields.includes(field) && typeof value === 'number') {
      value = Math.round(value) || 0;
    }

    // Handle empty values for optional document format fields
    if (['documentFormatWidthMm', 'documentFormatHeightMm', 'documentFormatMarginBottomMm'].includes(field) && (value === '' || value === 0)) {
      value = undefined;
    }

    updateMugDetails({ [field]: value });
  };

  const validateDocumentFormat = () => {
    const errors: string[] = [];
    const printWidth = mugDetails?.printTemplateWidthMm || 0;
    const printHeight = mugDetails?.printTemplateHeightMm || 0;
    const docWidth = mugDetails?.documentFormatWidthMm;
    const docHeight = mugDetails?.documentFormatHeightMm;
    const docMargin = mugDetails?.documentFormatMarginBottomMm;

    if (docWidth !== undefined) {
      if (docWidth <= 0) errors.push(t('form.mugDetails.validation.documentWidthPositive'));
      if (docWidth <= printWidth) errors.push(t('form.mugDetails.validation.documentWidthLarger'));
    }

    if (docHeight !== undefined) {
      if (docHeight <= 0) errors.push(t('form.mugDetails.validation.documentHeightPositive'));
      if (docHeight <= printHeight) errors.push(t('form.mugDetails.validation.documentHeightLarger'));
    }

    if (docMargin !== undefined && (docMargin < 0 || docMargin > 100)) {
      errors.push(t('form.mugDetails.validation.documentMarginRange'));
    }

    return errors;
  };

  const validationErrors = validateDocumentFormat();

  return (
    <Card>
      <CardHeader>
        <CardTitle>{t('form.mugDetails.title')}</CardTitle>
        <CardDescription>{t('form.mugDetails.description')}</CardDescription>
      </CardHeader>
      <CardContent className="space-y-4">
        <div className="grid grid-cols-2 gap-4">
          <div className="space-y-2">
            <FieldLabel htmlFor="heightMm" required>
              {t('form.mugDetails.fields.height.label', { unit: 'mm' })}
            </FieldLabel>
            <Input
              id="heightMm"
              type="number"
              value={mugDetails?.heightMm || 0}
              onChange={(e) => handleChange('heightMm', Number(e.target.value))}
              placeholder={t('form.mugDetails.fields.height.placeholder')}
              min="0"
              step="1"
            />
          </div>

          <div className="space-y-2">
            <FieldLabel htmlFor="diameterMm" required>
              {t('form.mugDetails.fields.diameter.label', { unit: 'mm' })}
            </FieldLabel>
            <Input
              id="diameterMm"
              type="number"
              value={mugDetails?.diameterMm || 0}
              onChange={(e) => handleChange('diameterMm', Number(e.target.value))}
              placeholder={t('form.mugDetails.fields.diameter.placeholder')}
              min="0"
              step="1"
            />
          </div>
        </div>

        <div className="space-y-2">
          <FieldLabel htmlFor="fillingQuantity" optional>
            {t('form.mugDetails.fields.fillingQuantity.label')}
          </FieldLabel>
          <Input
            id="fillingQuantity"
            value={mugDetails?.fillingQuantity || ''}
            onChange={(e) => handleChange('fillingQuantity', e.target.value)}
            placeholder={t('form.mugDetails.fields.fillingQuantity.placeholder')}
          />
        </div>

        <div className="flex items-center space-x-2">
          <Switch
            id="dishwasherSafe"
            checked={mugDetails?.dishwasherSafe ?? true}
            onCheckedChange={(checked) => handleChange('dishwasherSafe', checked)}
          />
          <FieldLabel htmlFor="dishwasherSafe">{t('form.mugDetails.fields.dishwasherSafe')}</FieldLabel>
        </div>

        {/* Document Format Section */}
        <div className="border-t pt-6">
          <div className="mb-4">
            <h3 className="text-lg font-medium">{t('form.mugDetails.documentFormat.title')}</h3>
            <p className="mt-1 text-sm text-gray-600">{t('form.mugDetails.documentFormat.description')}</p>
          </div>

          {validationErrors.length > 0 && (
            <div className="mb-4 rounded-md border border-red-200 bg-red-50 p-3">
              <div className="text-sm text-red-700">
                <ul className="list-inside list-disc space-y-1">
                  {validationErrors.map((error, index) => (
                    <li key={index}>{error}</li>
                  ))}
                </ul>
              </div>
            </div>
          )}

          <div className="mb-4 grid grid-cols-2 gap-4">
            <div className="space-y-2">
              <FieldLabel htmlFor="printTemplateWidthMm" required>
                {t('form.mugDetails.fields.printTemplateWidth.label', { unit: 'mm' })}
              </FieldLabel>
              <Input
                id="printTemplateWidthMm"
                type="number"
                value={mugDetails?.printTemplateWidthMm || 0}
                onChange={(e) => handleChange('printTemplateWidthMm', Number(e.target.value))}
                placeholder={t('form.mugDetails.fields.printTemplateWidth.placeholder')}
                min="0"
                step="1"
              />
            </div>

            <div className="space-y-2">
              <FieldLabel htmlFor="printTemplateHeightMm" required>
                {t('form.mugDetails.fields.printTemplateHeight.label', { unit: 'mm' })}
              </FieldLabel>
              <Input
                id="printTemplateHeightMm"
                type="number"
                value={mugDetails?.printTemplateHeightMm || 0}
                onChange={(e) => handleChange('printTemplateHeightMm', Number(e.target.value))}
                placeholder={t('form.mugDetails.fields.printTemplateHeight.placeholder')}
                min="0"
                step="1"
              />
            </div>
          </div>

          <div className="mb-4 grid grid-cols-2 gap-4">
            <div className="space-y-2">
              <FieldLabel htmlFor="documentFormatWidthMm" required>
                {t('form.mugDetails.fields.documentWidth.label', { unit: 'mm' })}
              </FieldLabel>
              <Input
                id="documentFormatWidthMm"
                type="number"
                value={mugDetails?.documentFormatWidthMm || ''}
                onChange={(e) => {
                  const value = e.target.value;
                  handleChange('documentFormatWidthMm', value ? Number(value) : undefined);
                }}
                placeholder={t('form.mugDetails.fields.documentWidth.placeholder')}
                min="1"
                step="1"
              />
            </div>

            <div className="space-y-2">
              <FieldLabel htmlFor="documentFormatHeightMm" required>
                {t('form.mugDetails.fields.documentHeight.label', { unit: 'mm' })}
              </FieldLabel>
              <Input
                id="documentFormatHeightMm"
                type="number"
                value={mugDetails?.documentFormatHeightMm || ''}
                onChange={(e) => {
                  const value = e.target.value;
                  handleChange('documentFormatHeightMm', value ? Number(value) : undefined);
                }}
                placeholder={t('form.mugDetails.fields.documentHeight.placeholder')}
                min="1"
                step="1"
              />
            </div>
          </div>

          <div className="space-y-2">
            <FieldLabel htmlFor="documentFormatMarginBottomMm" required>
              {t('form.mugDetails.fields.documentMargin.label', { unit: 'mm' })}
            </FieldLabel>
            <Input
              id="documentFormatMarginBottomMm"
              type="number"
              value={mugDetails?.documentFormatMarginBottomMm || ''}
              onChange={(e) => {
                const value = e.target.value;
                handleChange('documentFormatMarginBottomMm', value ? Number(value) : undefined);
              }}
              placeholder={t('form.mugDetails.fields.documentMargin.placeholder')}
              min="0"
              max="100"
              step="1"
            />
          </div>
        </div>
      </CardContent>
    </Card>
  );
}
