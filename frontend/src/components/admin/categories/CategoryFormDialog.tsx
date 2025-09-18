import { Button } from '@/components/ui/Button';
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from '@/components/ui/Dialog';
import { Input } from '@/components/ui/Input';
import { Label } from '@/components/ui/Label';
import type { CreatePromptCategoryRequest, UpdatePromptCategoryRequest } from '@/lib/api';
import { promptCategoriesApi } from '@/lib/api';
import { PromptCategory } from '@/types/prompt';
import { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';

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
  const [errorKey, setErrorKey] = useState<string | null>(null);
  const { t } = useTranslation('adminPromptCategories');

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
    setErrorKey(null);
  }, [category, open]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!formData.name.trim()) {
      setErrorKey('forms.common.errors.nameRequired');
      return;
    }

    try {
      setLoading(true);
      setErrorKey(null);

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
      setErrorKey('forms.category.errors.saveFailed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[425px]">
        <form onSubmit={handleSubmit}>
          <DialogHeader>
            <DialogTitle>{isEditing ? t('forms.category.title.edit') : t('forms.category.title.create')}</DialogTitle>
            <DialogDescription>{isEditing ? t('forms.category.description.edit') : t('forms.category.description.create')}</DialogDescription>
          </DialogHeader>
          <div className="grid gap-4 py-4">
            {errorKey && <div className="rounded border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">{t(errorKey)}</div>}
            <div className="grid gap-2">
              <Label htmlFor="name">{t('forms.common.labels.name')}</Label>
              <Input
                id="name"
                value={formData.name}
                onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                placeholder={t('forms.category.placeholders.name')}
                required
              />
            </div>
          </div>
          <DialogFooter>
            <Button type="button" variant="outline" onClick={() => onOpenChange(false)}>
              {t('forms.common.actions.cancel')}
            </Button>
            <Button type="submit" disabled={loading}>
              {loading ? t('forms.common.actions.saving') : isEditing ? t('forms.common.actions.update') : t('forms.common.actions.create')}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
