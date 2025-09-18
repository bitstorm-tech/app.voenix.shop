import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/Card';
import { FieldLabel } from '@/components/ui/FieldLabel';
import { Input } from '@/components/ui/Input';
import { InputWithCopy } from '@/components/ui/InputWithCopy';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/Select';
import { Switch } from '@/components/ui/Switch';
import { Textarea } from '@/components/ui/Textarea';
import { useSuppliers } from '@/hooks/queries/useSuppliers';
import { generateArticleNumber, getArticleNumberPlaceholder } from '@/lib/articleNumberUtils';
import { useArticleFormStore } from '@/stores/admin/articles/useArticleFormStore';
import type { ArticleType } from '@/types/article';
import type { ArticleCategory, ArticleSubCategory } from '@/types/mug';
import { Coffee, Shirt } from 'lucide-react';
import { useTranslation } from 'react-i18next';

interface GeneralTabProps {
  categories: ArticleCategory[];
  subcategories: ArticleSubCategory[];
}

export default function GeneralTab({ categories, subcategories }: GeneralTabProps) {
  const { data: suppliers = [] } = useSuppliers();
  const { article, isEdit, updateArticle, setArticleType, setCategory, setSubcategory } = useArticleFormStore();
  const { t } = useTranslation('adminArticles');

  return (
    <div className="space-y-6">
      {/* Article Type Card (only for new articles) */}
      {!isEdit && (
        <Card>
          <CardHeader>
            <CardTitle>{t('form.general.articleType.title')}</CardTitle>
            <CardDescription>{t('form.general.articleType.description')}</CardDescription>
          </CardHeader>
          <CardContent>
            <Select value={article.articleType} onValueChange={(value) => setArticleType(value as ArticleType)}>
              <SelectTrigger className="w-full">
                <SelectValue placeholder={t('form.general.articleType.placeholder')} />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="MUG">
                  <div className="flex items-center gap-2">
                    <Coffee className="h-4 w-4" />
                    {t('articleTypes.MUG')}
                  </div>
                </SelectItem>
                <SelectItem value="SHIRT">
                  <div className="flex items-center gap-2">
                    <Shirt className="h-4 w-4" />
                    {t('articleTypes.SHIRT')}
                  </div>
                </SelectItem>
              </SelectContent>
            </Select>
          </CardContent>
        </Card>
      )}

      {/* Basic Information Card */}
      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <div>
              <CardTitle>{t('form.general.basicInformation.title')}</CardTitle>
              <CardDescription>{t('form.general.basicInformation.description')}</CardDescription>
            </div>
            <div className="flex items-center gap-2">
              <FieldLabel htmlFor="active" className="text-sm font-normal">
                {t('form.general.basicInformation.fields.active')}
              </FieldLabel>
              <Switch id="active" checked={article.active} onCheckedChange={(checked) => updateArticle('active', checked)} />
            </div>
          </div>
        </CardHeader>
        <CardContent className="space-y-6">
          <div className="space-y-2">
            <FieldLabel htmlFor="name" required>
              {t('form.general.basicInformation.fields.name.label')}
            </FieldLabel>
            <Input
              id="name"
              value={article.name}
              onChange={(e) => updateArticle('name', e.target.value)}
              placeholder={t('form.general.basicInformation.fields.name.placeholder')}
              className="max-w-xl"
            />
          </div>

          <div className="space-y-2">
            <FieldLabel htmlFor="descriptionShort" required>
              {t('form.general.basicInformation.fields.descriptionShort.label')}
            </FieldLabel>
            <Textarea
              id="descriptionShort"
              value={article.descriptionShort}
              onChange={(e) => updateArticle('descriptionShort', e.target.value)}
              placeholder={t('form.general.basicInformation.fields.descriptionShort.placeholder')}
              rows={2}
              className="max-w-xl"
            />
          </div>

          <div className="space-y-2">
            <FieldLabel htmlFor="descriptionLong" required>
              {t('form.general.basicInformation.fields.descriptionLong.label')}
            </FieldLabel>
            <Textarea
              id="descriptionLong"
              value={article.descriptionLong}
              onChange={(e) => updateArticle('descriptionLong', e.target.value)}
              placeholder={t('form.general.basicInformation.fields.descriptionLong.placeholder')}
              rows={4}
            />
          </div>
        </CardContent>
      </Card>

      {/* Organization Card */}
      <Card>
        <CardHeader>
          <CardTitle>{t('form.general.organization.title')}</CardTitle>
          <CardDescription>{t('form.general.organization.description')}</CardDescription>
        </CardHeader>
        <CardContent className="space-y-6">
          <div className="grid gap-6 md:grid-cols-2">
            <div className="space-y-2">
              <FieldLabel htmlFor="category" required>
                {t('form.general.organization.fields.category.label')}
              </FieldLabel>
              <Select value={article.categoryId?.toString() || ''} onValueChange={(value) => setCategory(Number(value))}>
                <SelectTrigger id="category">
                  <SelectValue placeholder={t('form.general.organization.fields.category.placeholder')} />
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
              <FieldLabel htmlFor="subcategory" optional>
                {t('form.general.organization.fields.subcategory.label')}
              </FieldLabel>
              <Select
                value={article.subcategoryId?.toString() || ''}
                onValueChange={(value) => setSubcategory(value ? Number(value) : undefined)}
                disabled={!article.categoryId}
              >
                <SelectTrigger id="subcategory">
                  <SelectValue
                    placeholder={
                      article.categoryId
                        ? t('form.general.organization.fields.subcategory.placeholder')
                        : t('form.general.organization.fields.subcategory.disabledPlaceholder')
                    }
                  />
                </SelectTrigger>
                <SelectContent>
                  {subcategories.map((subcategory) => (
                    <SelectItem key={subcategory.id} value={subcategory.id.toString()}>
                      {subcategory.name}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
          </div>

          <div className="grid gap-6 md:grid-cols-2">
            <div className="space-y-2">
              <FieldLabel htmlFor="supplier" optional>
                {t('form.general.organization.fields.supplier.label')}
              </FieldLabel>
              <Select
                value={article.supplierId?.toString() || 'none'}
                onValueChange={(value) => updateArticle('supplierId', value === 'none' ? undefined : Number(value))}
              >
                <SelectTrigger id="supplier">
                  <SelectValue placeholder={t('form.general.organization.fields.supplier.placeholder')} />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="none">{t('form.general.organization.fields.supplier.none')}</SelectItem>
                  {suppliers.map((supplier) => (
                    <SelectItem key={supplier.id} value={supplier.id.toString()}>
                      {supplier.name || `${supplier.firstName} ${supplier.lastName}`}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            <div className="space-y-2">
              <FieldLabel htmlFor="articleNumber">{t('form.general.organization.fields.articleNumber.label')}</FieldLabel>
              <InputWithCopy
                id="articleNumber"
                value={generateArticleNumber(article.categoryId, article.subcategoryId, article.id) || getArticleNumberPlaceholder()}
                placeholder={getArticleNumberPlaceholder()}
                className="[&_input]:bg-muted"
              />
            </div>
          </div>

          {article.supplierId && (
            <div className="grid gap-6 md:grid-cols-2">
              <div className="space-y-2">
                <FieldLabel htmlFor="supplierArticleName" optional>
                  {t('form.general.organization.fields.supplierArticleName.label')}
                </FieldLabel>
                <Input
                  id="supplierArticleName"
                  value={article.supplierArticleName || ''}
                  onChange={(e) => updateArticle('supplierArticleName', e.target.value || undefined)}
                  placeholder={t('form.general.organization.fields.supplierArticleName.placeholder')}
                />
              </div>

              <div className="space-y-2">
                <FieldLabel htmlFor="supplierArticleNumber" optional>
                  {t('form.general.organization.fields.supplierArticleNumber.label')}
                </FieldLabel>
                <Input
                  id="supplierArticleNumber"
                  value={article.supplierArticleNumber || ''}
                  onChange={(e) => updateArticle('supplierArticleNumber', e.target.value || undefined)}
                  placeholder={t('form.general.organization.fields.supplierArticleNumber.placeholder')}
                />
              </div>
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
