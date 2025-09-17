import { Button } from '@/components/ui/Button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/Card';
import { Input } from '@/components/ui/Input';
import { Label } from '@/components/ui/Label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/Select';
import { useCountries } from '@/hooks/queries/useCountries';
import { useCreateSupplier, useSupplier, useUpdateSupplier } from '@/hooks/queries/useSuppliers';
import type { CreateSupplierRequest, UpdateSupplierRequest } from '@/types/supplier';
import { ArrowLeft } from 'lucide-react';
import { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useNavigate, useParams } from 'react-router-dom';
import { toast } from 'sonner';

export default function NewOrEditSupplier() {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const isEditing = !!id;
  const { t } = useTranslation('admin');

  const [formData, setFormData] = useState<{
    name: string;
    title: string;
    firstName: string;
    lastName: string;
    street: string;
    houseNumber: string;
    city: string;
    postalCode: string;
    countryId: string;
    phoneNumber1: string;
    phoneNumber2: string;
    phoneNumber3: string;
    email: string;
    website: string;
  }>({
    name: '',
    title: '',
    firstName: '',
    lastName: '',
    street: '',
    houseNumber: '',
    city: '',
    postalCode: '',
    countryId: '',
    phoneNumber1: '',
    phoneNumber2: '',
    phoneNumber3: '',
    email: '',
    website: '',
  });
  const [errors, setErrors] = useState<Record<string, string>>({});

  const { data: existingSupplier, isLoading: isLoadingSupplier } = useSupplier(id ? parseInt(id) : undefined);
  const { data: countries, isLoading: isLoadingCountries } = useCountries();
  const createSupplierMutation = useCreateSupplier();
  const updateSupplierMutation = useUpdateSupplier();

  useEffect(() => {
    if (existingSupplier && isEditing && countries) {
      setFormData({
        name: existingSupplier.name || '',
        title: existingSupplier.title || '',
        firstName: existingSupplier.firstName || '',
        lastName: existingSupplier.lastName || '',
        street: existingSupplier.street || '',
        houseNumber: existingSupplier.houseNumber || '',
        city: existingSupplier.city || '',
        postalCode: existingSupplier.postalCode?.toString() || '',
        countryId: existingSupplier.country?.id ? existingSupplier.country.id.toString() : '',
        phoneNumber1: existingSupplier.phoneNumber1 || '',
        phoneNumber2: existingSupplier.phoneNumber2 || '',
        phoneNumber3: existingSupplier.phoneNumber3 || '',
        email: existingSupplier.email || '',
        website: existingSupplier.website || '',
      });
    }
  }, [existingSupplier, isEditing, countries]);

  const validateForm = () => {
    const newErrors: Record<string, string> = {};

    if (formData.email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
      newErrors.email = t('supplier.errors.invalidEmail');
    }

    if (formData.postalCode && (isNaN(Number(formData.postalCode)) || Number(formData.postalCode) <= 0)) {
      newErrors.postalCode = t('supplier.errors.postalCode');
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
      name: formData.name.trim() || null,
      title: formData.title.trim() || null,
      firstName: formData.firstName.trim() || null,
      lastName: formData.lastName.trim() || null,
      street: formData.street.trim() || null,
      houseNumber: formData.houseNumber.trim() || null,
      city: formData.city.trim() || null,
      postalCode: formData.postalCode ? parseInt(formData.postalCode) : null,
      countryId: formData.countryId ? parseInt(formData.countryId) : null,
      phoneNumber1: formData.phoneNumber1.trim() || null,
      phoneNumber2: formData.phoneNumber2.trim() || null,
      phoneNumber3: formData.phoneNumber3.trim() || null,
      email: formData.email.trim() || null,
      website: formData.website.trim() || null,
    };

    try {
      if (isEditing) {
        await updateSupplierMutation.mutateAsync({
          id: parseInt(id),
          data: data as UpdateSupplierRequest,
        });
      } else {
        await createSupplierMutation.mutateAsync(data as CreateSupplierRequest);
      }
      navigate('/admin/suppliers');
    } catch (error: unknown) {
      const fallbackMessage = t('common.errors.generic');
      const errorMessage = error instanceof Error ? error.message : fallbackMessage;
      toast.error(errorMessage);
    }
  };

  const handleCancel = () => {
    navigate('/admin/suppliers');
  };

  if ((isEditing && isLoadingSupplier) || isLoadingCountries || (isEditing && !existingSupplier)) {
    return (
      <div className="container mx-auto p-6">
        <div className="flex h-64 items-center justify-center">
          <p className="text-gray-500">{t('common.loading')}</p>
        </div>
      </div>
    );
  }

  return (
    <div className="container mx-auto p-6">
      <div className="mb-6">
        <button onClick={() => navigate('/admin/suppliers')} className="mb-4 flex items-center gap-2 text-gray-600 hover:text-gray-900">
          <ArrowLeft className="h-4 w-4" />
          {t('supplier.breadcrumb.back')}
        </button>
        <h1 className="text-2xl font-bold">{isEditing ? t('supplier.title.edit') : t('supplier.title.new')}</h1>
      </div>

      <form onSubmit={handleSubmit} className="space-y-6">
        {/* Company Information */}
        <Card>
          <CardHeader>
            <CardTitle>{t('supplier.sections.company.title')}</CardTitle>
            <CardDescription>{t('supplier.sections.company.description')}</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div>
              <Label htmlFor="name">{t('supplier.form.name')}</Label>
              <Input
                id="name"
                value={formData.name}
                onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                placeholder={t('supplier.form.namePlaceholder')}
              />
            </div>

            <div>
              <Label htmlFor="website">{t('supplier.form.website')}</Label>
              <Input
                id="website"
                type="url"
                value={formData.website}
                onChange={(e) => setFormData({ ...formData, website: e.target.value })}
                placeholder={t('supplier.form.websitePlaceholder')}
              />
            </div>
          </CardContent>
        </Card>

        {/* Contact Person */}
        <Card>
          <CardHeader>
            <CardTitle>{t('supplier.sections.contact.title')}</CardTitle>
            <CardDescription>{t('supplier.sections.contact.description')}</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="grid gap-4 md:grid-cols-3">
              <div>
                <Label htmlFor="title">{t('supplier.form.title')}</Label>
                <Input
                  id="title"
                  value={formData.title}
                  onChange={(e) => setFormData({ ...formData, title: e.target.value })}
                  placeholder={t('supplier.form.titlePlaceholder')}
                />
              </div>

              <div>
                <Label htmlFor="firstName">{t('supplier.form.firstName')}</Label>
                <Input
                  id="firstName"
                  value={formData.firstName}
                  onChange={(e) => setFormData({ ...formData, firstName: e.target.value })}
                  placeholder={t('supplier.form.firstNamePlaceholder')}
                />
              </div>

              <div>
                <Label htmlFor="lastName">{t('supplier.form.lastName')}</Label>
                <Input
                  id="lastName"
                  value={formData.lastName}
                  onChange={(e) => setFormData({ ...formData, lastName: e.target.value })}
                  placeholder={t('supplier.form.lastNamePlaceholder')}
                />
              </div>
            </div>

            <div>
              <Label htmlFor="email">{t('supplier.form.email')}</Label>
              <Input
                id="email"
                type="email"
                value={formData.email}
                onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                placeholder={t('supplier.form.emailPlaceholder')}
                className={errors.email ? 'border-red-500' : ''}
              />
              {errors.email && <p className="mt-1 text-sm text-red-500">{errors.email}</p>}
            </div>
          </CardContent>
        </Card>

        {/* Address */}
        <Card>
          <CardHeader>
            <CardTitle>{t('supplier.sections.address.title')}</CardTitle>
            <CardDescription>{t('supplier.sections.address.description')}</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="grid gap-4 md:grid-cols-2">
              <div>
                <Label htmlFor="street">{t('supplier.form.street')}</Label>
                <Input
                  id="street"
                  value={formData.street}
                  onChange={(e) => setFormData({ ...formData, street: e.target.value })}
                  placeholder={t('supplier.form.streetPlaceholder')}
                />
              </div>

              <div>
                <Label htmlFor="houseNumber">{t('supplier.form.houseNumber')}</Label>
                <Input
                  id="houseNumber"
                  value={formData.houseNumber}
                  onChange={(e) => setFormData({ ...formData, houseNumber: e.target.value })}
                  placeholder={t('supplier.form.houseNumberPlaceholder')}
                />
              </div>
            </div>

            <div className="grid gap-4 md:grid-cols-3">
              <div>
                <Label htmlFor="city">{t('supplier.form.city')}</Label>
                <Input
                  id="city"
                  value={formData.city}
                  onChange={(e) => setFormData({ ...formData, city: e.target.value })}
                  placeholder={t('supplier.form.cityPlaceholder')}
                />
              </div>

              <div>
                <Label htmlFor="postalCode">{t('supplier.form.postalCode')}</Label>
                <Input
                  id="postalCode"
                  value={formData.postalCode}
                  onChange={(e) => setFormData({ ...formData, postalCode: e.target.value })}
                  placeholder={t('supplier.form.postalCodePlaceholder')}
                  className={errors.postalCode ? 'border-red-500' : ''}
                />
                {errors.postalCode && <p className="mt-1 text-sm text-red-500">{errors.postalCode}</p>}
              </div>

              <div>
                <Label htmlFor="country">{t('supplier.form.country')}</Label>
                <Select value={formData.countryId || undefined} onValueChange={(value) => setFormData({ ...formData, countryId: value })}>
                  <SelectTrigger id="country">
                    <SelectValue placeholder={t('supplier.form.countryPlaceholder')} />
                  </SelectTrigger>
                  <SelectContent>
                    {countries?.map((country) => (
                      <SelectItem key={country.id} value={country.id.toString()}>
                        {country.name}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
            </div>
          </CardContent>
        </Card>

        {/* Phone Numbers */}
        <Card>
          <CardHeader>
            <CardTitle>{t('supplier.sections.phone.title')}</CardTitle>
            <CardDescription>{t('supplier.sections.phone.description')}</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="grid gap-4 md:grid-cols-3">
              <div>
                <Label htmlFor="phoneNumber1">{t('supplier.form.phoneNumber1')}</Label>
                <Input
                  id="phoneNumber1"
                  type="tel"
                  value={formData.phoneNumber1}
                  onChange={(e) => setFormData({ ...formData, phoneNumber1: e.target.value })}
                  placeholder={t('supplier.form.phoneNumber1Placeholder')}
                />
              </div>

              <div>
                <Label htmlFor="phoneNumber2">{t('supplier.form.phoneNumber2')}</Label>
                <Input
                  id="phoneNumber2"
                  type="tel"
                  value={formData.phoneNumber2}
                  onChange={(e) => setFormData({ ...formData, phoneNumber2: e.target.value })}
                  placeholder={t('supplier.form.phoneNumber2Placeholder')}
                />
              </div>

              <div>
                <Label htmlFor="phoneNumber3">{t('supplier.form.phoneNumber3')}</Label>
                <Input
                  id="phoneNumber3"
                  type="tel"
                  value={formData.phoneNumber3}
                  onChange={(e) => setFormData({ ...formData, phoneNumber3: e.target.value })}
                  placeholder={t('supplier.form.phoneNumber3Placeholder')}
                />
              </div>
            </div>
          </CardContent>
        </Card>

        {/* Actions */}
        <div className="flex gap-3">
          <Button type="submit" disabled={createSupplierMutation.isPending || updateSupplierMutation.isPending}>
            {createSupplierMutation.isPending || updateSupplierMutation.isPending
              ? t('common.status.saving')
              : isEditing
                ? t('supplier.actions.update')
                : t('supplier.actions.create')}
          </Button>
          <Button
            type="button"
            variant="outline"
            onClick={handleCancel}
            disabled={createSupplierMutation.isPending || updateSupplierMutation.isPending}
          >
            {t('common.actions.cancel')}
          </Button>
        </div>
      </form>
    </div>
  );
}
