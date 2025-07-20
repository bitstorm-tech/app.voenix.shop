import { Button } from '@/components/ui/Button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/Card';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/Tabs';
import type { CreateMugRequest, UpdateMugRequest } from '@/lib/api';
import { articleCategoriesApi, articleSubCategoriesApi, mugsApi } from '@/lib/api';
import type { ArticleCategory, ArticleSubCategory } from '@/types/mug';
import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import CostsTab from './tabs/CostsTab';
import DataTab from './tabs/DataTab';
import DescriptionTab from './tabs/DescriptionTab';
import LieferantTab from './tabs/LieferantTab';
import VariantsTab from './tabs/VariantsTab';
import VersandTab from './tabs/VersandTab';

export default function NewOrEditMug() {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const isEditing = !!id;

  const [formData, setFormData] = useState<CreateMugRequest>({
    name: '',
    descriptionLong: '',
    descriptionShort: '',
    image: '',
    price: 0,
    heightMm: 0,
    diameterMm: 0,
    printTemplateWidthMm: 0,
    printTemplateHeightMm: 0,
    fillingQuantity: '',
    dishwasherSafe: true,
    active: true,
    categoryId: undefined,
    subCategoryId: undefined,
  });
  const [categories, setCategories] = useState<ArticleCategory[]>([]);
  const [subCategories, setSubCategories] = useState<ArticleSubCategory[]>([]);
  const [loading, setLoading] = useState(false);
  const [initialLoading, setInitialLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchCategories();
    if (isEditing) {
      fetchMug();
    } else {
      setInitialLoading(false);
    }
  }, [id]);

  useEffect(() => {
    if (formData.categoryId) {
      fetchSubCategories(formData.categoryId);
    } else {
      setSubCategories([]);
      setFormData((prev) => ({ ...prev, subCategoryId: undefined }));
    }
  }, [formData.categoryId]);

  const fetchCategories = async () => {
    try {
      const data = await articleCategoriesApi.getAll();
      setCategories(data);
    } catch (error) {
      console.error('Error fetching article categories:', error);
      setError('Failed to load article categories');
    }
  };

  const fetchSubCategories = async (categoryId: number) => {
    try {
      const data = await articleSubCategoriesApi.getByCategoryId(categoryId);
      setSubCategories(data);
    } catch (error) {
      console.error('Error fetching article subcategories:', error);
    }
  };

  const fetchMug = async () => {
    if (!id) return;

    try {
      setInitialLoading(true);
      const mug = await mugsApi.getById(parseInt(id));
      setFormData({
        name: mug.name,
        descriptionLong: mug.descriptionLong,
        descriptionShort: mug.descriptionShort,
        image: mug.image,
        price: mug.price,
        heightMm: mug.heightMm,
        diameterMm: mug.diameterMm,
        printTemplateWidthMm: mug.printTemplateWidthMm,
        printTemplateHeightMm: mug.printTemplateHeightMm,
        fillingQuantity: mug.fillingQuantity || '',
        dishwasherSafe: mug.dishwasherSafe,
        active: mug.active,
        categoryId: mug.category?.id,
        subCategoryId: mug.subCategory?.id,
      });
    } catch (error) {
      console.error('Error fetching mug:', error);
      setError('Failed to load mug');
    } finally {
      setInitialLoading(false);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!formData.name.trim()) {
      setError('Name is required');
      return;
    }

    if (!formData.descriptionShort.trim()) {
      setError('Short description is required');
      return;
    }

    if (!formData.descriptionLong.trim()) {
      setError('Long description is required');
      return;
    }

    if (formData.price <= 0) {
      setError('Price must be greater than 0');
      return;
    }

    if (formData.heightMm <= 0 || formData.diameterMm <= 0) {
      setError('Height and diameter must be greater than 0');
      return;
    }

    if (formData.printTemplateWidthMm <= 0 || formData.printTemplateHeightMm <= 0) {
      setError('Print template dimensions must be greater than 0');
      return;
    }

    try {
      setLoading(true);
      setError(null);

      if (isEditing) {
        const updateData: UpdateMugRequest = {
          ...formData,
          fillingQuantity: formData.fillingQuantity || undefined,
        };
        await mugsApi.update(parseInt(id), updateData);
      } else {
        await mugsApi.create(formData);
      }

      navigate('/admin/mugs');
    } catch (error) {
      console.error('Error saving mug:', error);
      setError('Failed to save mug. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = () => {
    navigate('/admin/mugs');
  };

  if (initialLoading) {
    return (
      <div className="container mx-auto p-6">
        <div className="flex h-64 items-center justify-center">
          <p className="text-gray-500">Loading...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="container mx-auto p-6">
      <Card className="mx-auto max-w-2xl">
        <CardHeader>
          <CardTitle>{isEditing ? 'Edit Mug' : 'New Mug'}</CardTitle>
          <CardDescription>{isEditing ? 'Update the mug details below' : 'Create a new mug with the form below'}</CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-6">
            {error && <div className="rounded border border-red-200 bg-red-50 px-4 py-3 text-red-700">{error}</div>}

            <Tabs defaultValue="description" className="w-full">
              <TabsList className="grid w-full grid-cols-6">
                <TabsTrigger value="description">Description</TabsTrigger>
                <TabsTrigger value="data">Data</TabsTrigger>
                <TabsTrigger value="costs">Costs</TabsTrigger>
                <TabsTrigger value="lieferant">Lieferant</TabsTrigger>
                <TabsTrigger value="versand">Versand</TabsTrigger>
                <TabsTrigger value="variants">Variants</TabsTrigger>
              </TabsList>

              <TabsContent value="description" className="mt-6">
                <DescriptionTab formData={formData} setFormData={setFormData} categories={categories} subCategories={subCategories} />
              </TabsContent>

              <TabsContent value="costs" className="mt-6">
                <CostsTab formData={formData} setFormData={setFormData} />
              </TabsContent>

              <TabsContent value="data" className="mt-6">
                <DataTab formData={formData} setFormData={setFormData} />
              </TabsContent>

              <TabsContent value="lieferant" className="mt-6">
                <LieferantTab formData={formData} setFormData={setFormData} />
              </TabsContent>

              <TabsContent value="versand" className="mt-6">
                <VersandTab formData={formData} setFormData={setFormData} />
              </TabsContent>

              <TabsContent value="variants" className="mt-6">
                <VariantsTab mugId={id ? parseInt(id) : undefined} />
              </TabsContent>
            </Tabs>

            <div className="flex gap-4 pt-6">
              <Button type="submit" disabled={loading}>
                {loading ? 'Saving...' : isEditing ? 'Update Mug' : 'Create Mug'}
              </Button>
              <Button type="button" variant="outline" onClick={handleCancel}>
                Cancel
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}
