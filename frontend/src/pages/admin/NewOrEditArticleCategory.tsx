import { Button } from '@/components/ui/Button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/Card';
import { Input } from '@/components/ui/Input';
import { Label } from '@/components/ui/Label';
import { Textarea } from '@/components/ui/Textarea';
import type { CreateArticleCategoryRequest, UpdateArticleCategoryRequest } from '@/lib/api';
import { articleCategoriesApi } from '@/lib/api';
import { useCallback, useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useNavigate, useParams } from 'react-router-dom';

export default function NewOrEditArticleCategory() {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const isEditing = !!id;
  const { t } = useTranslation('admin');

  const [formData, setFormData] = useState<CreateArticleCategoryRequest>({
    name: '',
    description: '',
  });
  const [loading, setLoading] = useState(false);
  const [initialLoading, setInitialLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchArticleCategory = useCallback(async () => {
    if (!id) return;

    try {
      setInitialLoading(true);
      const category = await articleCategoriesApi.getById(parseInt(id));
      setFormData({
        name: category.name,
        description: category.description || '',
      });
    } catch (error) {
      console.error('Error fetching article category:', error);
      setError(t('articleCategory.errors.load'));
    } finally {
      setInitialLoading(false);
    }
  }, [id, t]);

  useEffect(() => {
    if (isEditing) {
      fetchArticleCategory();
    } else {
      setInitialLoading(false);
    }
  }, [fetchArticleCategory, isEditing]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!formData.name.trim()) {
      setError(t('articleCategory.errors.nameRequired'));
      return;
    }

    if (formData.name.length > 255) {
      setError(t('articleCategory.errors.nameMaxLength'));
      return;
    }

    try {
      setLoading(true);
      setError(null);

      if (isEditing) {
        const updateData: UpdateArticleCategoryRequest = {
          name: formData.name,
          description: formData.description || undefined,
        };
        await articleCategoriesApi.update(parseInt(id), updateData);
      } else {
        const createData: CreateArticleCategoryRequest = {
          name: formData.name,
          description: formData.description || undefined,
        };
        await articleCategoriesApi.create(createData);
      }

      navigate('/admin/article-categories');
    } catch (error) {
      console.error('Error saving article category:', error);
      setError(t('articleCategory.errors.save'));
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = () => {
    navigate('/admin/article-categories');
  };

  if (initialLoading) {
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
      <Card className="mx-auto max-w-2xl">
        <CardHeader>
          <CardTitle>{isEditing ? t('articleCategory.title.edit') : t('articleCategory.title.new')}</CardTitle>
          <CardDescription>{isEditing ? t('articleCategory.description.edit') : t('articleCategory.description.new')}</CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-6">
            {error && <div className="rounded border border-red-200 bg-red-50 px-4 py-3 text-red-700">{error}</div>}

            <div className="space-y-2">
              <Label htmlFor="name">{t('articleCategory.form.name')}</Label>
              <Input
                id="name"
                value={formData.name}
                onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                placeholder={t('articleCategory.form.namePlaceholder')}
                maxLength={255}
                required
              />
              <p className="text-sm text-gray-500">{t('articleCategory.form.charCount', { count: formData.name.length, max: 255 })}</p>
            </div>

            <div className="space-y-2">
              <Label htmlFor="description">{t('articleCategory.form.description')}</Label>
              <Textarea
                id="description"
                value={formData.description || ''}
                onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                placeholder={t('articleCategory.form.descriptionPlaceholder')}
                rows={4}
              />
            </div>

            <div className="flex gap-4">
              <Button type="submit" disabled={loading}>
                {loading ? t('common.status.saving') : isEditing ? t('articleCategory.actions.update') : t('articleCategory.actions.create')}
              </Button>
              <Button type="button" variant="outline" onClick={handleCancel}>
                {t('common.actions.cancel')}
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}
