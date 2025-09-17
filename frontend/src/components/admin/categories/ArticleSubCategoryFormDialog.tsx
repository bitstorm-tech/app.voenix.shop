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
import { useTranslation } from 'react-i18next';

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
  const [errorKey, setErrorKey] = useState<string | null>(null);
  const { t } = useTranslation('adminArticleCategories');

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
    setErrorKey(null);
  }, [subcategory, categoryId, open]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!formData.name.trim()) {
      setErrorKey('forms.common.errors.nameRequired');
      return;
    }

    if (!formData.articleCategoryId) {
      setErrorKey('forms.common.errors.categoryRequired');
      return;
    }

    try {
      setLoading(true);
      setErrorKey(null);

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
      setErrorKey('forms.subcategory.errors.saveFailed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[525px]">
        <form onSubmit={handleSubmit}>
          <DialogHeader>
            <DialogTitle>{isEditing ? t('forms.subcategory.title.edit') : t('forms.subcategory.title.create')}</DialogTitle>
            <DialogDescription>{isEditing ? t('forms.subcategory.description.edit') : t('forms.subcategory.description.create')}</DialogDescription>
          </DialogHeader>
          <div className="grid gap-4 py-4">
            {errorKey && <div className="rounded border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">{t(errorKey)}</div>}
            <div className="grid gap-2">
              <Label htmlFor="category">{t('forms.common.labels.category')}</Label>
              <Select
                value={formData.articleCategoryId ? formData.articleCategoryId.toString() : undefined}
                onValueChange={(value) => setFormData({ ...formData, articleCategoryId: parseInt(value) })}
              >
                <SelectTrigger id="category">
                  <SelectValue placeholder={t('forms.common.placeholders.category')} />
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
              <Label htmlFor="name">{t('forms.common.labels.name')}</Label>
              <Input
                id="name"
                value={formData.name}
                onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                placeholder={t('forms.subcategory.placeholders.name')}
                required
              />
            </div>
            <div className="grid gap-2">
              <Label htmlFor="description">{t('forms.common.labels.description')}</Label>
              <Textarea
                id="description"
                value={formData.description}
                onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                placeholder={t('forms.subcategory.placeholders.description')}
                rows={3}
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
