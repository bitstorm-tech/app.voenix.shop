import { Button } from '@/components/ui/Button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/Card';
import { Input } from '@/components/ui/Input';
import { Label } from '@/components/ui/Label';
import { Textarea } from '@/components/ui/Textarea';
import type { CreateMugCategoryRequest, UpdateMugCategoryRequest } from '@/lib/api';
import { mugCategoriesApi } from '@/lib/api';
import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';

export default function NewOrEditMugCategory() {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const isEditing = !!id;

  const [formData, setFormData] = useState<CreateMugCategoryRequest>({
    name: '',
    description: '',
  });
  const [loading, setLoading] = useState(false);
  const [initialLoading, setInitialLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (isEditing) {
      fetchMugCategory();
    } else {
      setInitialLoading(false);
    }
  }, [id]);

  const fetchMugCategory = async () => {
    if (!id) return;

    try {
      setInitialLoading(true);
      const category = await mugCategoriesApi.getById(parseInt(id));
      setFormData({
        name: category.name,
        description: category.description || '',
      });
    } catch (error) {
      console.error('Error fetching mug category:', error);
      setError('Failed to load mug category');
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

    if (formData.name.length > 255) {
      setError('Name must not exceed 255 characters');
      return;
    }

    try {
      setLoading(true);
      setError(null);

      if (isEditing) {
        const updateData: UpdateMugCategoryRequest = {
          name: formData.name,
          description: formData.description || undefined,
        };
        await mugCategoriesApi.update(parseInt(id), updateData);
      } else {
        const createData: CreateMugCategoryRequest = {
          name: formData.name,
          description: formData.description || undefined,
        };
        await mugCategoriesApi.create(createData);
      }

      navigate('/admin/mug-categories');
    } catch (error) {
      console.error('Error saving mug category:', error);
      setError('Failed to save mug category. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = () => {
    navigate('/admin/mug-categories');
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
          <CardTitle>{isEditing ? 'Edit Mug Category' : 'New Mug Category'}</CardTitle>
          <CardDescription>{isEditing ? 'Update the mug category details below' : 'Create a new mug category with the form below'}</CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-6">
            {error && <div className="rounded border border-red-200 bg-red-50 px-4 py-3 text-red-700">{error}</div>}

            <div className="space-y-2">
              <Label htmlFor="name">Name *</Label>
              <Input
                id="name"
                value={formData.name}
                onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                placeholder="Enter mug category name"
                maxLength={255}
                required
              />
              <p className="text-sm text-gray-500">{formData.name.length}/255 characters</p>
            </div>

            <div className="space-y-2">
              <Label htmlFor="description">Description</Label>
              <Textarea
                id="description"
                value={formData.description || ''}
                onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                placeholder="Enter mug category description (optional)"
                rows={4}
              />
            </div>

            <div className="flex gap-4">
              <Button type="submit" disabled={loading}>
                {loading ? 'Saving...' : isEditing ? 'Update Category' : 'Create Category'}
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
