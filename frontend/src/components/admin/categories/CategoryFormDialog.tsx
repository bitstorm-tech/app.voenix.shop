import { Button } from '@/components/ui/Button';
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from '@/components/ui/Dialog';
import { Input } from '@/components/ui/Input';
import { Label } from '@/components/ui/Label';
import type { CreatePromptCategoryRequest, UpdatePromptCategoryRequest } from '@/lib/api';
import { promptCategoriesApi } from '@/lib/api';
import { PromptCategory } from '@/types/prompt';
import { useEffect, useState } from 'react';

interface CategoryFormDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  category: PromptCategory | null;
  onSaved: () => void;
}

export default function CategoryFormDialog({ open, onOpenChange, category, onSaved }: CategoryFormDialogProps) {
  const isEditing = !!category;
  const [formData, setFormData] = useState<CreatePromptCategoryRequest>({
    name: '',
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (category) {
      setFormData({
        name: category.name,
      });
    } else {
      setFormData({
        name: '',
      });
    }
    setError(null);
  }, [category, open]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!formData.name.trim()) {
      setError('Name is required');
      return;
    }

    try {
      setLoading(true);
      setError(null);

      if (isEditing && category) {
        const updateData: UpdatePromptCategoryRequest = {
          name: formData.name,
        };
        await promptCategoriesApi.update(category.id, updateData);
      } else {
        await promptCategoriesApi.create(formData);
      }

      onSaved();
      onOpenChange(false);
    } catch (error) {
      console.error('Error saving category:', error);
      setError('Failed to save category. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[425px]">
        <form onSubmit={handleSubmit}>
          <DialogHeader>
            <DialogTitle>{isEditing ? 'Edit Category' : 'New Category'}</DialogTitle>
            <DialogDescription>{isEditing ? 'Update the category details below' : 'Create a new category with the form below'}</DialogDescription>
          </DialogHeader>
          <div className="grid gap-4 py-4">
            {error && <div className="rounded border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">{error}</div>}
            <div className="grid gap-2">
              <Label htmlFor="name">Name</Label>
              <Input
                id="name"
                value={formData.name}
                onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                placeholder="Enter category name"
                required
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
