import { Button } from '@/components/ui/Button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/Card';
import { Input } from '@/components/ui/Input';
import { Label } from '@/components/ui/Label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/Select';
import { Switch } from '@/components/ui/Switch';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/Tabs';
import { Textarea } from '@/components/ui/Textarea';
import { articleCategoriesApi, articlesApi, articleSubCategoriesApi } from '@/lib/api';
import type {
  Article,
  ArticleType,
  CreateArticleRequest,
  CreateArticleVariantRequest,
  CreateMugDetailsRequest,
  CreatePillowDetailsRequest,
  CreateShirtDetailsRequest,
  UpdateArticleRequest,
} from '@/types/article';
import type { ArticleCategory, ArticleSubCategory } from '@/types/mug';
import { ArrowLeft, Loader2, Save } from 'lucide-react';
import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { toast } from 'sonner';
import MugDetailsTab from './tabs/MugDetailsTab';
import PillowDetailsTab from './tabs/PillowDetailsTab';
import ShirtDetailsTab from './tabs/ShirtDetailsTab';
import VariantsTab from './tabs/VariantsTab';

type ArticleFormData = Omit<Article, 'mugDetails' | 'shirtDetails' | 'pillowDetails'> & {
  mugDetails?: CreateMugDetailsRequest;
  shirtDetails?: CreateShirtDetailsRequest;
  pillowDetails?: CreatePillowDetailsRequest;
};

export default function NewOrEditArticle() {
  const navigate = useNavigate();
  const { id } = useParams();
  const isEdit = !!id;

  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [article, setArticle] = useState<Partial<ArticleFormData>>({
    name: '',
    descriptionShort: '',
    descriptionLong: '',
    mainImage: '',
    price: 0,
    active: true,
    articleType: 'MUG',
    categoryId: 0,
    variants: [],
    // Initialize default mug details since MUG is the default type
    mugDetails: {
      heightMm: 0,
      diameterMm: 0,
      printTemplateWidthMm: 0,
      printTemplateHeightMm: 0,
      fillingQuantity: '',
      dishwasherSafe: true,
    },
  });
  const [categories, setCategories] = useState<ArticleCategory[]>([]);
  const [subcategories, setSubcategories] = useState<ArticleSubCategory[]>([]);
  const [temporaryVariants, setTemporaryVariants] = useState<CreateArticleVariantRequest[]>([]);

  useEffect(() => {
    fetchCategories();
    if (isEdit) {
      fetchArticle();
    }
  }, [id]);

  useEffect(() => {
    if (article.categoryId) {
      fetchSubcategories(article.categoryId);
    } else {
      setSubcategories([]);
    }
  }, [article.categoryId]);

  const fetchArticle = async () => {
    try {
      setLoading(true);
      const data = await articlesApi.getById(Number(id));
      setArticle(data);
    } catch (error) {
      console.error('Error fetching article:', error);
      toast.error('Failed to load article');
    } finally {
      setLoading(false);
    }
  };

  const fetchCategories = async () => {
    try {
      const data = await articleCategoriesApi.getAll();
      setCategories(data);
    } catch (error) {
      console.error('Error fetching categories:', error);
    }
  };

  const fetchSubcategories = async (categoryId: number) => {
    try {
      const data = await articleSubCategoriesApi.getByCategoryId(categoryId);
      setSubcategories(data);
    } catch (error) {
      console.error('Error fetching subcategories:', error);
    }
  };

  const handleAddTemporaryVariant = (variant: CreateArticleVariantRequest) => {
    setTemporaryVariants([...temporaryVariants, variant]);
  };

  const handleDeleteTemporaryVariant = (index: number) => {
    setTemporaryVariants(temporaryVariants.filter((_, i) => i !== index));
  };

  const handleSubmit = async () => {
    try {
      setSaving(true);

      if (!article.name || !article.categoryId || !article.articleType) {
        toast.error('Please fill in all required fields');
        return;
      }

      // Validate type-specific details
      switch (article.articleType) {
        case 'MUG':
          if (!article.mugDetails) {
            toast.error('Please fill in mug specifications');
            return;
          }
          break;
        case 'SHIRT':
          if (!article.shirtDetails) {
            toast.error('Please fill in shirt details');
            return;
          }
          break;
        case 'PILLOW':
          if (!article.pillowDetails) {
            toast.error('Please fill in pillow details');
            return;
          }
          break;
      }

      if (isEdit) {
        const updateData: UpdateArticleRequest = {
          name: article.name || '',
          descriptionShort: article.descriptionShort || '',
          descriptionLong: article.descriptionLong || '',
          mainImage: article.mainImage || '',
          price: Math.round(article.price || 0),
          active: article.active || false,
          categoryId: article.categoryId || 0,
          subcategoryId: article.subcategoryId,
          mugDetails: article.mugDetails,
          shirtDetails: article.shirtDetails,
          pillowDetails: article.pillowDetails,
        };
        console.log('Updating article with data:', updateData);
        await articlesApi.update(Number(id), updateData);
        toast.success('Article updated successfully');
      } else {
        const createData: CreateArticleRequest = {
          name: article.name || '',
          descriptionShort: article.descriptionShort || '',
          descriptionLong: article.descriptionLong || '',
          mainImage: article.mainImage || '',
          price: Math.round(article.price || 0),
          active: article.active || false,
          articleType: article.articleType as ArticleType,
          categoryId: article.categoryId || 0,
          subcategoryId: article.subcategoryId,
          variants: temporaryVariants,
          mugDetails: article.mugDetails,
          shirtDetails: article.shirtDetails,
          pillowDetails: article.pillowDetails,
        };
        console.log('Creating article with data:', createData);
        await articlesApi.create(createData);
        toast.success('Article created successfully');
      }

      navigate('/admin/articles');
    } catch (error) {
      console.error('Error saving article:', error);
      if (error instanceof Error) {
        toast.error(error.message);
      } else {
        toast.error('Failed to save article');
      }
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <div className="flex h-64 items-center justify-center">
        <Loader2 className="h-8 w-8 animate-spin" />
      </div>
    );
  }

  return (
    <div className="container mx-auto p-6">
      <div className="mb-6 flex items-center justify-between">
        <div className="flex items-center gap-4">
          <Button variant="ghost" onClick={() => navigate('/admin/articles')}>
            <ArrowLeft className="h-4 w-4" />
          </Button>
          <h1 className="text-2xl font-bold">{isEdit ? 'Edit Article' : 'New Article'}</h1>
        </div>
        <Button onClick={handleSubmit} disabled={saving}>
          {saving ? <Loader2 className="mr-2 h-4 w-4 animate-spin" /> : <Save className="mr-2 h-4 w-4" />}
          Save
        </Button>
      </div>

      <Tabs defaultValue="description" className="space-y-4">
        <TabsList>
          <TabsTrigger value="description">Description</TabsTrigger>
          {article.articleType === 'MUG' && <TabsTrigger value="specifications">Specifications</TabsTrigger>}
          {article.articleType === 'SHIRT' && <TabsTrigger value="details">Materials & Sizes</TabsTrigger>}
          {article.articleType === 'PILLOW' && <TabsTrigger value="dimensions">Dimensions & Materials</TabsTrigger>}
          <TabsTrigger value="variants">Variants</TabsTrigger>
        </TabsList>

        <TabsContent value="description">
          <Card>
            <CardHeader>
              <CardTitle>Basic Information</CardTitle>
              <CardDescription>General article details</CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              {!isEdit && (
                <div className="space-y-2">
                  <Label htmlFor="articleType">Article Type *</Label>
                  <Select
                    value={article.articleType}
                    onValueChange={(value) => {
                      const newType = value as ArticleType;
                      const updates: any = { ...article, articleType: newType };

                      // Initialize type-specific details when type changes
                      switch (newType) {
                        case 'MUG':
                          updates.mugDetails = {
                            heightMm: 0,
                            diameterMm: 0,
                            printTemplateWidthMm: 0,
                            printTemplateHeightMm: 0,
                            fillingQuantity: '',
                            dishwasherSafe: true,
                          };
                          delete updates.shirtDetails;
                          delete updates.pillowDetails;
                          break;
                        case 'SHIRT':
                          updates.shirtDetails = {
                            material: '',
                            careInstructions: '',
                            fitType: 'REGULAR',
                            availableSizes: [],
                          };
                          delete updates.mugDetails;
                          delete updates.pillowDetails;
                          break;
                        case 'PILLOW':
                          updates.pillowDetails = {
                            widthCm: 0,
                            heightCm: 0,
                            depthCm: 0,
                            material: '',
                            fillingType: '',
                            coverRemovable: true,
                            washable: true,
                          };
                          delete updates.mugDetails;
                          delete updates.shirtDetails;
                          break;
                      }

                      setArticle(updates);
                    }}
                  >
                    <SelectTrigger id="articleType">
                      <SelectValue placeholder="Select article type" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="MUG">Mug</SelectItem>
                      <SelectItem value="SHIRT">T-Shirt</SelectItem>
                      <SelectItem value="PILLOW">Pillow</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
              )}

              <div className="space-y-2">
                <Label htmlFor="name">Name *</Label>
                <Input id="name" value={article.name} onChange={(e) => setArticle({ ...article, name: e.target.value })} placeholder="Article name" />
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="category">Category *</Label>
                  <Select
                    value={article.categoryId?.toString() || ''}
                    onValueChange={(value) => setArticle({ ...article, categoryId: Number(value), subcategoryId: undefined })}
                  >
                    <SelectTrigger id="category">
                      <SelectValue placeholder="Select category" />
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
                  <Label htmlFor="subcategory">Subcategory</Label>
                  <Select
                    value={article.subcategoryId?.toString() || ''}
                    onValueChange={(value) => setArticle({ ...article, subcategoryId: value ? Number(value) : undefined })}
                    disabled={!article.categoryId}
                  >
                    <SelectTrigger id="subcategory">
                      <SelectValue placeholder="Select subcategory" />
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
                <Label htmlFor="descriptionShort">Short Description</Label>
                <Textarea
                  id="descriptionShort"
                  value={article.descriptionShort}
                  onChange={(e) => setArticle({ ...article, descriptionShort: e.target.value })}
                  placeholder="Brief description"
                  rows={2}
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="descriptionLong">Long Description</Label>
                <Textarea
                  id="descriptionLong"
                  value={article.descriptionLong}
                  onChange={(e) => setArticle({ ...article, descriptionLong: e.target.value })}
                  placeholder="Detailed description"
                  rows={4}
                />
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="mainImage">Main Image URL</Label>
                  <Input
                    id="mainImage"
                    value={article.mainImage}
                    onChange={(e) => setArticle({ ...article, mainImage: e.target.value })}
                    placeholder="https://example.com/image.jpg"
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="price">Price ($)</Label>
                  <Input
                    id="price"
                    type="number"
                    value={article.price}
                    onChange={(e) => setArticle({ ...article, price: Math.round(Number(e.target.value)) })}
                    placeholder="0"
                    min="0"
                    step="1"
                  />
                </div>
              </div>

              <div className="flex items-center space-x-2">
                <Switch id="active" checked={article.active} onCheckedChange={(checked) => setArticle({ ...article, active: checked })} />
                <Label htmlFor="active">Active</Label>
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        {article.articleType === 'MUG' && (
          <TabsContent value="specifications">
            <MugDetailsTab mugDetails={article.mugDetails || {}} onChange={(mugDetails) => setArticle({ ...article, mugDetails })} />
          </TabsContent>
        )}

        {article.articleType === 'SHIRT' && (
          <TabsContent value="details">
            <ShirtDetailsTab shirtDetails={article.shirtDetails || {}} onChange={(shirtDetails) => setArticle({ ...article, shirtDetails })} />
          </TabsContent>
        )}

        {article.articleType === 'PILLOW' && (
          <TabsContent value="dimensions">
            <PillowDetailsTab pillowDetails={article.pillowDetails || {}} onChange={(pillowDetails) => setArticle({ ...article, pillowDetails })} />
          </TabsContent>
        )}

        <TabsContent value="variants">
          <VariantsTab
            articleId={article.id}
            articleType={article.articleType as ArticleType}
            variants={article.variants || []}
            temporaryVariants={temporaryVariants}
            onAddTemporaryVariant={handleAddTemporaryVariant}
            onDeleteTemporaryVariant={handleDeleteTemporaryVariant}
          />
        </TabsContent>
      </Tabs>
    </div>
  );
}
