import { Button } from '@/components/ui/Button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/Card';
import { Input } from '@/components/ui/Input';
import { Label } from '@/components/ui/Label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/Select';
import { Textarea } from '@/components/ui/Textarea';
import type { CreateArticleSubCategoryRequest, UpdateArticleSubCategoryRequest } from '@/lib/api';
import { articleCategoriesApi, articleSubCategoriesApi } from '@/lib/api';
import type { ArticleCategory } from '@/types/mug';
import { useCallback, useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useNavigate, useParams } from 'react-router-dom';

export default function NewOrEditArticleSubCategory() {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const isEditing = !!id;
  const { t } = useTranslation('admin');

  const [formData, setFormData] = useState<CreateArticleSubCategoryRequest>({
    articleCategoryId: 0,
    name: '',
    description: '',
  });
  const [categories, setCategories] = useState<ArticleCategory[]>([]);
  const [loading, setLoading] = useState(false);
  const [initialLoading, setInitialLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchCategories = useCallback(async () => {
    try {
      const data = await articleCategoriesApi.getAll();
      setCategories(data);
    } catch (error) {
      console.error('Error fetching article categories:', error);
      setError(t('articleSubCategory.errors.loadCategories'));
    }
  }, [t]);

  const fetchArticleSubCategory = useCallback(async () => {
    if (!id) return;

    try {
      setInitialLoading(true);
      const subCategory = await articleSubCategoriesApi.getById(parseInt(id));
      setFormData({
        articleCategoryId: subCategory.articleCategoryId,
        name: subCategory.name,
        description: subCategory.description || '',
      });
    } catch (error) {
      console.error('Error fetching article subcategory:', error);
      setError(t('articleSubCategory.errors.load'));
    } finally {
      setInitialLoading(false);
    }
  }, [id, t]);

  useEffect(() => {
    fetchCategories();
    if (isEditing) {
      fetchArticleSubCategory();
    } else {
      setInitialLoading(false);
    }
  }, [fetchCategories, fetchArticleSubCategory, isEditing]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!formData.name.trim()) {
      setError(t('articleSubCategory.errors.nameRequired'));
      return;
    }

    if (!formData.articleCategoryId) {
      setError(t('articleSubCategory.errors.categoryRequired'));
      return;
    }

    try {
      setLoading(true);
      setError(null);

      if (isEditing) {
        const updateData: UpdateArticleSubCategoryRequest = {
          articleCategoryId: formData.articleCategoryId,
          name: formData.name,
          description: formData.description || undefined,
        };
        await articleSubCategoriesApi.update(parseInt(id), updateData);
      } else {
        await articleSubCategoriesApi.create(formData);
      }

      navigate('/admin/article-subcategories');
    } catch (error) {
      console.error('Error saving article subcategory:', error);
      setError(t('articleSubCategory.errors.save'));
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = () => {
    navigate('/admin/article-subcategories');
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
          <CardTitle>{isEditing ? t('articleSubCategory.title.edit') : t('articleSubCategory.title.new')}</CardTitle>
          <CardDescription>
            {isEditing ? t('articleSubCategory.description.edit') : t('articleSubCategory.description.new')}
          </CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-6">
            {error && <div className="rounded border border-red-200 bg-red-50 px-4 py-3 text-red-700">{error}</div>}

            <div className="space-y-2">
              <Label htmlFor="category">{t('articleSubCategory.form.category')}</Label>
              <Select
                value={formData.articleCategoryId.toString()}
                onValueChange={(value) => setFormData({ ...formData, articleCategoryId: parseInt(value) })}
              >
                <SelectTrigger id="category">
                  <SelectValue placeholder={t('articleSubCategory.form.categoryPlaceholder')} />
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
              <Label htmlFor="name">{t('articleSubCategory.form.name')}</Label>
              <Input
                id="name"
                value={formData.name}
                onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                placeholder={t('articleSubCategory.form.namePlaceholder')}
                required
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="description">{t('articleSubCategory.form.description')}</Label>
              <Textarea
                id="description"
                value={formData.description}
                onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                placeholder={t('articleSubCategory.form.descriptionPlaceholder')}
                rows={3}
              />
            </div>

            <div className="flex gap-4">
              <Button type="submit" disabled={loading}>
                {loading
                  ? t('common.status.saving')
                  : isEditing
                    ? t('articleSubCategory.actions.update')
                    : t('articleSubCategory.actions.create')}
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
