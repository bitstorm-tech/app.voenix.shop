import { Button } from '@/components/ui/Button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/Card';
import { Checkbox } from '@/components/ui/Checkbox';
import { Input } from '@/components/ui/Input';
import { Label } from '@/components/ui/Label';
import { Textarea } from '@/components/ui/Textarea';
import { useCreateVat, useUpdateVat, useVat } from '@/hooks/queries/useVat';
import type { CreateValueAddedTaxRequest, UpdateValueAddedTaxRequest } from '@/types/vat';
import { ArrowLeft } from 'lucide-react';
import { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useNavigate, useParams } from 'react-router-dom';
import { toast } from 'sonner';

export default function NewOrEditVat() {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const isEditing = !!id;
  const { t } = useTranslation('admin');

  const [formData, setFormData] = useState({
    name: '',
    percent: '',
    description: '',
    isDefault: false,
  });
  const [errors, setErrors] = useState<Record<string, string>>({});

  const { data: existingVat, isLoading: isLoadingVat } = useVat(id ? parseInt(id) : undefined);
  const createVatMutation = useCreateVat();
  const updateVatMutation = useUpdateVat();

  useEffect(() => {
    if (existingVat && isEditing) {
      setFormData({
        name: existingVat.name,
        percent: existingVat.percent.toString(),
        description: existingVat.description || '',
        isDefault: existingVat.isDefault || false,
      });
    }
  }, [existingVat, isEditing]);

  const validateForm = () => {
    const newErrors: Record<string, string> = {};

    if (!formData.name.trim()) {
      newErrors.name = t('vatForm.errors.nameRequired');
    }

    if (!formData.percent) {
      newErrors.percent = t('vatForm.errors.percentRequired');
    } else {
      const percentNum = parseInt(formData.percent);
      if (isNaN(percentNum) || percentNum <= 0) {
        newErrors.percent = t('vatForm.errors.percentPositive');
      }
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!validateForm()) {
      return;
    }

    const data = {
      name: formData.name.trim(),
      percent: parseInt(formData.percent),
      description: formData.description.trim() || null,
      isDefault: formData.isDefault,
    };

    try {
      if (isEditing) {
        await updateVatMutation.mutateAsync({
          id: parseInt(id),
          data: data as UpdateValueAddedTaxRequest,
        });
      } else {
        await createVatMutation.mutateAsync(data as CreateValueAddedTaxRequest);
      }
      navigate('/admin/vat');
    } catch (error: unknown) {
      const fallbackMessage = t('common.errors.generic');
      const errorMessage = error instanceof Error ? error.message : fallbackMessage;
      toast.error(errorMessage);
    }
  };

  const handleCancel = () => {
    navigate('/admin/vat');
  };

  if (isEditing && isLoadingVat) {
    return (
      <div className="container mx-auto p-6">
        <div className="flex h-64 items-center justify-center">
          <p className="text-gray-500">{t('vatForm.loading')}</p>
        </div>
      </div>
    );
  }

  return (
    <div className="container mx-auto p-6">
      <div className="mb-6">
        <button onClick={() => navigate('/admin/vat')} className="mb-4 flex items-center gap-2 text-gray-600 hover:text-gray-900">
          <ArrowLeft className="h-4 w-4" />
          {t('vatForm.breadcrumb.back')}
        </button>
        <h1 className="text-2xl font-bold">{isEditing ? t('vatForm.title.edit') : t('vatForm.title.new')}</h1>
      </div>

      <Card className="max-w-2xl">
        <CardHeader>
          <CardTitle>{isEditing ? t('vatForm.cardTitle.edit') : t('vatForm.cardTitle.new')}</CardTitle>
          <CardDescription>{isEditing ? t('vatForm.cardDescription.edit') : t('vatForm.cardDescription.new')}</CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <Label htmlFor="name">{t('vatForm.form.name')}</Label>
              <Input
                id="name"
                value={formData.name}
                onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                placeholder={t('vatForm.form.namePlaceholder')}
                className={errors.name ? 'border-red-500' : ''}
              />
              {errors.name && <p className="mt-1 text-sm text-red-500">{errors.name}</p>}
            </div>

            <div>
              <Label htmlFor="percent">{t('vatForm.form.percent')}</Label>
              <Input
                id="percent"
                type="number"
                value={formData.percent}
                onChange={(e) => setFormData({ ...formData, percent: e.target.value })}
                placeholder={t('vatForm.form.percentPlaceholder')}
                min="1"
                className={errors.percent ? 'border-red-500' : ''}
              />
              {errors.percent && <p className="mt-1 text-sm text-red-500">{errors.percent}</p>}
            </div>

            <div>
              <Label htmlFor="description">{t('vatForm.form.description')}</Label>
              <Textarea
                id="description"
                value={formData.description}
                onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                placeholder={t('vatForm.form.descriptionPlaceholder')}
                rows={3}
              />
            </div>

            <div className="flex items-center space-x-2">
              <Checkbox
                id="isDefault"
                checked={formData.isDefault}
                onCheckedChange={(checked) => setFormData({ ...formData, isDefault: checked === true })}
              />
              <Label htmlFor="isDefault" className="cursor-pointer text-sm font-normal">
                {t('vatForm.form.isDefault')}
              </Label>
            </div>

            <div className="flex gap-3 pt-4">
              <Button type="submit" disabled={createVatMutation.isPending || updateVatMutation.isPending}>
                {createVatMutation.isPending || updateVatMutation.isPending
                  ? t('common.status.saving')
                  : isEditing
                    ? t('vatForm.actions.update')
                    : t('vatForm.actions.create')}
              </Button>
              <Button type="button" variant="outline" onClick={handleCancel} disabled={createVatMutation.isPending || updateVatMutation.isPending}>
                {t('common.actions.cancel')}
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}
