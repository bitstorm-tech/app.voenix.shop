import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/Card';
import { FieldLabel } from '@/components/ui/FieldLabel';
import { Input } from '@/components/ui/Input';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/Select';
import { Switch } from '@/components/ui/Switch';
import { Textarea } from '@/components/ui/Textarea';
import { useSuppliers } from '@/hooks/queries/useSuppliers';
import { useArticleFormStore } from '@/stores/admin/articles/useArticleFormStore';
import type { ArticleType } from '@/types/article';
import { Coffee, Shirt } from 'lucide-react';

export default function GeneralTab() {
  const { data: suppliers = [] } = useSuppliers();
  const { article, categories, subcategories, isEdit, updateArticle, setArticleType, setCategory, setSubcategory } = useArticleFormStore();

  return (
    <div className="space-y-6">
      {/* Article Type Card (only for new articles) */}
      {!isEdit && (
        <Card>
          <CardHeader>
            <CardTitle>Article Type</CardTitle>
            <CardDescription>Choose the type of product you want to create</CardDescription>
          </CardHeader>
          <CardContent>
            <Select value={article.articleType} onValueChange={(value) => setArticleType(value as ArticleType)}>
              <SelectTrigger className="w-full">
                <SelectValue placeholder="Select article type" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="MUG">
                  <div className="flex items-center gap-2">
                    <Coffee className="h-4 w-4" />
                    Mug
                  </div>
                </SelectItem>
                <SelectItem value="SHIRT">
                  <div className="flex items-center gap-2">
                    <Shirt className="h-4 w-4" />
                    T-Shirt
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
              <CardTitle>Basic Information</CardTitle>
              <CardDescription>Essential details about your article</CardDescription>
            </div>
            <div className="flex items-center gap-2">
              <FieldLabel htmlFor="active" className="text-sm font-normal">
                Active
              </FieldLabel>
              <Switch id="active" checked={article.active} onCheckedChange={(checked) => updateArticle('active', checked)} />
            </div>
          </div>
        </CardHeader>
        <CardContent className="space-y-6">
          <div className="space-y-2">
            <FieldLabel htmlFor="name" required>
              Article Name
            </FieldLabel>
            <Input
              id="name"
              value={article.name}
              onChange={(e) => updateArticle('name', e.target.value)}
              placeholder="e.g., Premium Coffee Mug"
              className="max-w-xl"
            />
          </div>

          <div className="space-y-2">
            <FieldLabel htmlFor="descriptionShort" required>
              Short Description
            </FieldLabel>
            <Textarea
              id="descriptionShort"
              value={article.descriptionShort}
              onChange={(e) => updateArticle('descriptionShort', e.target.value)}
              placeholder="Brief description for product listings"
              rows={2}
              className="max-w-xl"
            />
          </div>

          <div className="space-y-2">
            <FieldLabel htmlFor="descriptionLong" required>
              Detailed Description
            </FieldLabel>
            <Textarea
              id="descriptionLong"
              value={article.descriptionLong}
              onChange={(e) => updateArticle('descriptionLong', e.target.value)}
              placeholder="Full product description with features and benefits"
              rows={4}
            />
          </div>
        </CardContent>
      </Card>

      {/* Organization Card */}
      <Card>
        <CardHeader>
          <CardTitle>Organization</CardTitle>
          <CardDescription>Categorize and organize your article</CardDescription>
        </CardHeader>
        <CardContent className="space-y-6">
          <div className="grid gap-6 md:grid-cols-2">
            <div className="space-y-2">
              <FieldLabel htmlFor="category" required>
                Category
              </FieldLabel>
              <Select value={article.categoryId?.toString() || ''} onValueChange={(value) => setCategory(Number(value))}>
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
              <FieldLabel htmlFor="subcategory" optional>
                Subcategory
              </FieldLabel>
              <Select
                value={article.subcategoryId?.toString() || ''}
                onValueChange={(value) => setSubcategory(value ? Number(value) : undefined)}
                disabled={!article.categoryId}
              >
                <SelectTrigger id="subcategory">
                  <SelectValue placeholder={article.categoryId ? 'Select a subcategory' : 'Select category first'} />
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

          <div className="space-y-2">
            <FieldLabel htmlFor="supplier" optional>
              Supplier
            </FieldLabel>
            <Select
              value={article.supplierId?.toString() || 'none'}
              onValueChange={(value) => updateArticle('supplierId', value === 'none' ? undefined : Number(value))}
            >
              <SelectTrigger id="supplier" className="max-w-xl">
                <SelectValue placeholder="Select a supplier (optional)" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="none">No supplier</SelectItem>
                {suppliers.map((supplier) => (
                  <SelectItem key={supplier.id} value={supplier.id.toString()}>
                    {supplier.name || `${supplier.firstName} ${supplier.lastName}`}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
