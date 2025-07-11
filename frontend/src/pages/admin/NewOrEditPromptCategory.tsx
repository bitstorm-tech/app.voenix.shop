import { Button } from '@/components/ui/Button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/Card';
import { Input } from '@/components/ui/Input';
import { Label } from '@/components/ui/Label';
import type { CreatePromptCategoryRequest, UpdatePromptCategoryRequest } from '@/lib/api';
import { promptCategoriesApi } from '@/lib/api';
import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';

export default function NewOrEditPromptCategory() {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const isEditing = !!id;

  const [formData, setFormData] = useState<CreatePromptCategoryRequest>({
    name: '',
  });
  const [loading, setLoading] = useState(false);
  const [initialLoading, setInitialLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (isEditing) {
      fetchPromptCategory();
    } else {
      setInitialLoading(false);
    }
  }, [id]);

  const fetchPromptCategory = async () => {
    if (!id) return;

    try {
      setInitialLoading(true);
      const category = await promptCategoriesApi.getById(parseInt(id));
      setFormData({
        name: category.name,
      });
    } catch (error) {
      console.error('Error fetching prompt category:', error);
      setError('Failed to load prompt category');
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

    try {
      setLoading(true);
      setError(null);

      if (isEditing) {
        const updateData: UpdatePromptCategoryRequest = {
          name: formData.name,
        };
        await promptCategoriesApi.update(parseInt(id), updateData);
      } else {
        await promptCategoriesApi.create(formData);
      }

      navigate('/admin/prompt-categories');
    } catch (error) {
      console.error('Error saving prompt category:', error);
      setError('Failed to save prompt category. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = () => {
    navigate('/admin/prompt-categories');
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
          <CardTitle>{isEditing ? 'Edit Prompt Category' : 'New Prompt Category'}</CardTitle>
          <CardDescription>
            {isEditing ? 'Update the prompt category details below' : 'Create a new prompt category with the form below'}
          </CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-6">
            {error && <div className="rounded border border-red-200 bg-red-50 px-4 py-3 text-red-700">{error}</div>}

            <div className="space-y-2">
              <Label htmlFor="name">Name</Label>
              <Input
                id="name"
                value={formData.name}
                onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                placeholder="Enter prompt category name"
                required
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
