import { Button } from '@/components/ui/Button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/Card';
import { Input } from '@/components/ui/Input';
import { Label } from '@/components/ui/Label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/Select';
import { Textarea } from '@/components/ui/Textarea';
import type { CreateMugSubCategoryRequest, UpdateMugSubCategoryRequest } from '@/lib/api';
import { mugCategoriesApi, mugSubCategoriesApi } from '@/lib/api';
import type { MugCategory } from '@/types/mug';
import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';

export default function NewOrEditMugSubCategory() {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const isEditing = !!id;

  const [formData, setFormData] = useState<CreateMugSubCategoryRequest>({
    mugCategoryId: 0,
    name: '',
    description: '',
  });
  const [categories, setCategories] = useState<MugCategory[]>([]);
  const [loading, setLoading] = useState(false);
  const [initialLoading, setInitialLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchCategories();
    if (isEditing) {
      fetchMugSubCategory();
    } else {
      setInitialLoading(false);
    }
  }, [id]);

  const fetchCategories = async () => {
    try {
      const data = await mugCategoriesApi.getAll();
      setCategories(data);
    } catch (error) {
      console.error('Error fetching mug categories:', error);
      setError('Failed to load mug categories');
    }
  };

  const fetchMugSubCategory = async () => {
    if (!id) return;

    try {
      setInitialLoading(true);
      const subCategory = await mugSubCategoriesApi.getById(parseInt(id));
      setFormData({
        mugCategoryId: subCategory.mugCategoryId,
        name: subCategory.name,
        description: subCategory.description || '',
      });
    } catch (error) {
      console.error('Error fetching mug subcategory:', error);
      setError('Failed to load mug subcategory');
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

    if (!formData.mugCategoryId) {
      setError('Category is required');
      return;
    }

    try {
      setLoading(true);
      setError(null);

      if (isEditing) {
        const updateData: UpdateMugSubCategoryRequest = {
          mugCategoryId: formData.mugCategoryId,
          name: formData.name,
          description: formData.description || undefined,
        };
        await mugSubCategoriesApi.update(parseInt(id), updateData);
      } else {
        await mugSubCategoriesApi.create(formData);
      }

      navigate('/admin/mug-subcategories');
    } catch (error) {
      console.error('Error saving mug subcategory:', error);
      setError('Failed to save mug subcategory. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = () => {
    navigate('/admin/mug-subcategories');
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
          <CardTitle>{isEditing ? 'Edit Mug Subcategory' : 'New Mug Subcategory'}</CardTitle>
          <CardDescription>
            {isEditing ? 'Update the mug subcategory details below' : 'Create a new mug subcategory with the form below'}
          </CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-6">
            {error && <div className="rounded border border-red-200 bg-red-50 px-4 py-3 text-red-700">{error}</div>}

            <div className="space-y-2">
              <Label htmlFor="category">Category</Label>
              <Select
                value={formData.mugCategoryId.toString()}
                onValueChange={(value) => setFormData({ ...formData, mugCategoryId: parseInt(value) })}
              >
                <SelectTrigger id="category">
                  <SelectValue placeholder="Select a category" />
                </SelectTrigger>
                <SelectContent>
                  {categories.map((category) => (
                    <SelectItem key={category.id} value={category.id.toString()}>
                      {category.name}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            <div className="space-y-2">
              <Label htmlFor="name">Name</Label>
              <Input
                id="name"
                value={formData.name}
                onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                placeholder="Enter subcategory name"
                required
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="description">Description</Label>
              <Textarea
                id="description"
                value={formData.description}
                onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                placeholder="Enter subcategory description (optional)"
                rows={3}
              />
            </div>

            <div className="flex gap-4">
              <Button type="submit" disabled={loading}>
                {loading ? 'Saving...' : isEditing ? 'Update Subcategory' : 'Create Subcategory'}
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
