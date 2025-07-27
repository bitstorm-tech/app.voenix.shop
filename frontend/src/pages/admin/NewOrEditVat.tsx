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
import { useNavigate, useParams } from 'react-router-dom';
import { toast } from 'sonner';

export default function NewOrEditVat() {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const isEditing = !!id;

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
      newErrors.name = 'Name is required';
    }

    if (!formData.percent) {
      newErrors.percent = 'Percent is required';
    } else {
      const percentNum = parseInt(formData.percent);
      if (isNaN(percentNum) || percentNum <= 0) {
        newErrors.percent = 'Percent must be a positive number';
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
    } catch (error: any) {
      toast.error(error.message || 'An error occurred');
    }
  };

  const handleCancel = () => {
    navigate('/admin/vat');
  };

  if (isEditing && isLoadingVat) {
    return (
      <div className="container mx-auto p-6">
        <div className="flex h-64 items-center justify-center">
          <p className="text-gray-500">Loading VAT data...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="container mx-auto p-6">
      <div className="mb-6">
        <button onClick={() => navigate('/admin/vat')} className="mb-4 flex items-center gap-2 text-gray-600 hover:text-gray-900">
          <ArrowLeft className="h-4 w-4" />
          Back to VAT List
        </button>
        <h1 className="text-2xl font-bold">{isEditing ? 'Edit VAT' : 'Create New VAT'}</h1>
      </div>

      <Card className="max-w-2xl">
        <CardHeader>
          <CardTitle>{isEditing ? 'Edit VAT Details' : 'VAT Details'}</CardTitle>
          <CardDescription>{isEditing ? 'Update the VAT information below.' : 'Enter the details for the new VAT entry.'}</CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <Label htmlFor="name">Name *</Label>
              <Input
                id="name"
                value={formData.name}
                onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                placeholder="e.g., Standard VAT"
                className={errors.name ? 'border-red-500' : ''}
              />
              {errors.name && <p className="mt-1 text-sm text-red-500">{errors.name}</p>}
            </div>

            <div>
              <Label htmlFor="percent">Percent (%) *</Label>
              <Input
                id="percent"
                type="number"
                value={formData.percent}
                onChange={(e) => setFormData({ ...formData, percent: e.target.value })}
                placeholder="e.g., 19"
                min="1"
                className={errors.percent ? 'border-red-500' : ''}
              />
              {errors.percent && <p className="mt-1 text-sm text-red-500">{errors.percent}</p>}
            </div>

            <div>
              <Label htmlFor="description">Description</Label>
              <Textarea
                id="description"
                value={formData.description}
                onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                placeholder="Optional description for this VAT rate"
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
                Set as default VAT
              </Label>
            </div>

            <div className="flex gap-3 pt-4">
              <Button type="submit" disabled={createVatMutation.isPending || updateVatMutation.isPending}>
                {createVatMutation.isPending || updateVatMutation.isPending ? 'Saving...' : isEditing ? 'Update VAT' : 'Create VAT'}
              </Button>
              <Button type="button" variant="outline" onClick={handleCancel} disabled={createVatMutation.isPending || updateVatMutation.isPending}>
                Cancel
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}
