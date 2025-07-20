import { Button } from '@/components/ui/Button';
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from '@/components/ui/Dialog';
import { Input } from '@/components/ui/Input';
import { Label } from '@/components/ui/Label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/Select';
import { Textarea } from '@/components/ui/Textarea';
import type { CreateArticleSubCategoryRequest, UpdateArticleSubCategoryRequest } from '@/lib/api';
import { articleSubCategoriesApi } from '@/lib/api';
import { ArticleCategory, ArticleSubCategory } from '@/types/mug';
import { useEffect, useState } from 'react';

interface ArticleSubCategoryFormDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  subcategory: ArticleSubCategory | null;
  categoryId: number | null;
  categories: ArticleCategory[];
  onSaved: () => void;
}

export default function ArticleSubCategoryFormDialog({
  open,
  onOpenChange,
  subcategory,
  categoryId,
  categories,
  onSaved,
}: ArticleSubCategoryFormDialogProps) {
  const isEditing = !!subcategory;
  const [formData, setFormData] = useState<CreateArticleSubCategoryRequest>({
    articleCategoryId: categoryId || 0,
    name: '',
    description: '',
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (subcategory) {
      setFormData({
        articleCategoryId: subcategory.articleCategoryId,
        name: subcategory.name,
        description: subcategory.description || '',
      });
    } else {
      setFormData({
        articleCategoryId: categoryId || 0,
        name: '',
        description: '',
      });
    }
    setError(null);
  }, [subcategory, categoryId, open]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!formData.name.trim()) {
      setError('Name is required');
      return;
    }

    if (!formData.articleCategoryId) {
      setError('Category is required');
      return;
    }

    try {
      setLoading(true);
      setError(null);

      if (isEditing && subcategory) {
        const updateData: UpdateArticleSubCategoryRequest = {
          articleCategoryId: formData.articleCategoryId,
          name: formData.name,
          description: formData.description || undefined,
        };
        await articleSubCategoriesApi.update(subcategory.id, updateData);
      } else {
        await articleSubCategoriesApi.create(formData);
      }

      onSaved();
      onOpenChange(false);
    } catch (error) {
      console.error('Error saving subcategory:', error);
      setError('Failed to save subcategory. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[525px]">
        <form onSubmit={handleSubmit}>
          <DialogHeader>
            <DialogTitle>{isEditing ? 'Edit Subcategory' : 'New Subcategory'}</DialogTitle>
            <DialogDescription>
              {isEditing ? 'Update the subcategory details below' : 'Create a new subcategory with the form below'}
            </DialogDescription>
          </DialogHeader>
          <div className="grid gap-4 py-4">
            {error && <div className="rounded border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">{error}</div>}
            <div className="grid gap-2">
              <Label htmlFor="category">Category</Label>
              <Select
                value={formData.articleCategoryId.toString()}
                onValueChange={(value) => setFormData({ ...formData, articleCategoryId: parseInt(value) })}
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
            <div className="grid gap-2">
              <Label htmlFor="name">Name</Label>
              <Input
                id="name"
                value={formData.name}
                onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                placeholder="Enter subcategory name"
                required
              />
            </div>
            <div className="grid gap-2">
              <Label htmlFor="description">Description</Label>
              <Textarea
                id="description"
                value={formData.description}
                onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                placeholder="Enter subcategory description (optional)"
                rows={3}
              />
            </div>
          </div>
          <DialogFooter>
            <Button type="button" variant="outline" onClick={() => onOpenChange(false)}>
              Cancel
            </Button>
            <Button type="submit" disabled={loading}>
              {loading ? 'Saving...' : isEditing ? 'Update' : 'Create'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
