import { Button } from '@/components/ui/Button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/Card';
import { Input } from '@/components/ui/Input';
import { Label } from '@/components/ui/Label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/Select';
import { Textarea } from '@/components/ui/Textarea';
import type { CreatePromptSubCategoryRequest, UpdatePromptSubCategoryRequest } from '@/lib/api';
import { promptCategoriesApi, promptSubCategoriesApi } from '@/lib/api';
import type { PromptCategory } from '@/types/prompt';
import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';

export default function NewOrEditPromptSubCategory() {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const isEditing = !!id;

  const [formData, setFormData] = useState<CreatePromptSubCategoryRequest>({
    promptCategoryId: 0,
    name: '',
    description: '',
  });
  const [categories, setCategories] = useState<PromptCategory[]>([]);
  const [loading, setLoading] = useState(false);
  const [initialLoading, setInitialLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchCategories();
    if (isEditing) {
      fetchPromptSubCategory();
    } else {
      setInitialLoading(false);
    }
  }, [id]);

  const fetchCategories = async () => {
    try {
      const data = await promptCategoriesApi.getAll();
      setCategories(data);
    } catch (error) {
      console.error('Error fetching prompt categories:', error);
      setError('Failed to load prompt categories');
    }
  };

  const fetchPromptSubCategory = async () => {
    if (!id) return;

    try {
      setInitialLoading(true);
      const subCategory = await promptSubCategoriesApi.getById(parseInt(id));
      setFormData({
        promptCategoryId: subCategory.promptCategoryId,
        name: subCategory.name,
        description: subCategory.description || '',
      });
    } catch (error) {
      console.error('Error fetching prompt subcategory:', error);
      setError('Failed to load prompt subcategory');
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

    if (!formData.promptCategoryId) {
      setError('Category is required');
      return;
    }

    try {
      setLoading(true);
      setError(null);

      if (isEditing) {
        const updateData: UpdatePromptSubCategoryRequest = {
          promptCategoryId: formData.promptCategoryId,
          name: formData.name,
          description: formData.description || undefined,
        };
        await promptSubCategoriesApi.update(parseInt(id), updateData);
      } else {
        await promptSubCategoriesApi.create(formData);
      }

      navigate('/admin/prompt-subcategories');
    } catch (error) {
      console.error('Error saving prompt subcategory:', error);
      setError('Failed to save prompt subcategory. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = () => {
    navigate('/admin/prompt-subcategories');
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
          <CardTitle>{isEditing ? 'Edit Prompt Subcategory' : 'New Prompt Subcategory'}</CardTitle>
          <CardDescription>
            {isEditing ? 'Update the prompt subcategory details below' : 'Create a new prompt subcategory with the form below'}
          </CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-6">
            {error && <div className="rounded border border-red-200 bg-red-50 px-4 py-3 text-red-700">{error}</div>}

            <div className="space-y-2">
              <Label htmlFor="category">Category</Label>
              <Select
                value={formData.promptCategoryId.toString()}
                onValueChange={(value) => setFormData({ ...formData, promptCategoryId: parseInt(value) })}
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
