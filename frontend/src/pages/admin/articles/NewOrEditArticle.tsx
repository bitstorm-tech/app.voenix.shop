import { Button } from '@/components/ui/Button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/Card';
import { FieldLabel } from '@/components/ui/FieldLabel';
import { Input } from '@/components/ui/Input';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/Select';
import { Switch } from '@/components/ui/Switch';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/Tabs';
import { Textarea } from '@/components/ui/Textarea';
import { useCreateArticle, useUpdateArticle } from '@/hooks/queries/useArticles';
import { useSuppliers } from '@/hooks/queries/useSuppliers';
import { articleCategoriesApi, articlesApi, articleSubCategoriesApi } from '@/lib/api';
import type {
  Article,
  ArticleType,
  CreateArticleMugVariantRequest,
  CreateArticleRequest,
  CreateArticleShirtVariantRequest,
  CreateCostCalculationRequest,
  CreateMugDetailsRequest,
  CreateShirtDetailsRequest,
  UpdateArticleRequest,
} from '@/types/article';
import type { ArticleCategory, ArticleSubCategory } from '@/types/mug';
import { ArrowLeft, Calculator, ChevronRight, Coffee, FileText, Layers, Loader2, Package, Save, Settings, Shirt } from 'lucide-react';
import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { toast } from 'sonner';
import CostCalculationTab from './tabs/CostCalculationTab';
import MugDetailsTab from './tabs/MugDetailsTab';
import ShirtDetailsTab from './tabs/ShirtDetailsTab';
import VariantsTab from './tabs/VariantsTab';

type ArticleFormData = Omit<Article, 'mugDetails' | 'shirtDetails' | 'costCalculation'> & {
  mugDetails?: CreateMugDetailsRequest;
  shirtDetails?: CreateShirtDetailsRequest;
  costCalculation?: CreateCostCalculationRequest;
};

const articleTypeIcons = {
  MUG: Coffee,
  SHIRT: Shirt,
};

export default function NewOrEditArticle() {
  const navigate = useNavigate();
  const { id } = useParams();
  const isEdit = !!id;

  const createArticleMutation = useCreateArticle();
  const updateArticleMutation = useUpdateArticle();
  const { data: suppliers = [] } = useSuppliers();

  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [article, setArticle] = useState<Partial<ArticleFormData>>({
    name: '',
    descriptionShort: '',
    descriptionLong: '',
    active: true,
    articleType: 'MUG',
    categoryId: 0,
    supplierId: undefined,
    mugVariants: [],
    shirtVariants: [],
    // Initialize default mug details since MUG is the default type
    mugDetails: {
      heightMm: 0,
      diameterMm: 0,
      printTemplateWidthMm: 0,
      printTemplateHeightMm: 0,
      fillingQuantity: '',
      dishwasherSafe: true,
    },
    costCalculation: {
      purchasePriceNet: 0,
      purchasePriceTax: 0,
      purchasePriceGross: 0,
      purchaseCostNet: 0,
      purchaseCostTax: 0,
      purchaseCostGross: 0,
      purchaseCostPercent: 0,
      purchaseTotalNet: 0,
      purchaseTotalTax: 0,
      purchaseTotalGross: 0,
      purchasePriceUnit: '1.00',
      purchaseVatRatePercent: 19,
      salesVatRatePercent: 19,
      marginNet: 0,
      marginTax: 0,
      marginGross: 0,
      marginPercent: 100,
      salesTotalNet: 0,
      salesTotalTax: 0,
      salesTotalGross: 0,
      salesPriceUnit: '1.00',
      purchaseCalculationMode: 'NET',
      salesCalculationMode: 'NET',
    },
  });
  const [categories, setCategories] = useState<ArticleCategory[]>([]);
  const [subcategories, setSubcategories] = useState<ArticleSubCategory[]>([]);
  const [temporaryMugVariants, setTemporaryMugVariants] = useState<CreateArticleMugVariantRequest[]>([]);
  const [temporaryShirtVariants, setTemporaryShirtVariants] = useState<CreateArticleShirtVariantRequest[]>([]);

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

  const handleAddTemporaryMugVariant = (variant: CreateArticleMugVariantRequest) => {
    setTemporaryMugVariants([...temporaryMugVariants, variant]);
  };

  const handleDeleteTemporaryMugVariant = (index: number) => {
    setTemporaryMugVariants(temporaryMugVariants.filter((_, i) => i !== index));
  };

  const handleUpdateTemporaryMugVariant = (index: number, variant: CreateArticleMugVariantRequest) => {
    setTemporaryMugVariants(temporaryMugVariants.map((v, i) => (i === index ? variant : v)));
  };

  const handleAddTemporaryShirtVariant = (variant: CreateArticleShirtVariantRequest) => {
    setTemporaryShirtVariants([...temporaryShirtVariants, variant]);
  };

  const handleDeleteTemporaryShirtVariant = (index: number) => {
    setTemporaryShirtVariants(temporaryShirtVariants.filter((_, i) => i !== index));
  };

  const handleUpdateTemporaryShirtVariant = (index: number, variant: CreateArticleShirtVariantRequest) => {
    setTemporaryShirtVariants(temporaryShirtVariants.map((v, i) => (i === index ? variant : v)));
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
      }

      if (isEdit) {
        const updateData: UpdateArticleRequest = {
          name: article.name || '',
          descriptionShort: article.descriptionShort || '',
          descriptionLong: article.descriptionLong || '',
          active: article.active || false,
          categoryId: article.categoryId || 0,
          subcategoryId: article.subcategoryId,
          supplierId: article.supplierId,
          mugDetails: article.mugDetails,
          shirtDetails: article.shirtDetails,
          costCalculation: article.costCalculation,
        };
        console.log('Updating article with data:', updateData);
        updateArticleMutation.mutate(
          { id: Number(id), data: updateData },
          {
            onSuccess: () => {
              navigate('/admin/articles');
            },
          },
        );
      } else {
        const createData: CreateArticleRequest = {
          name: article.name || '',
          descriptionShort: article.descriptionShort || '',
          descriptionLong: article.descriptionLong || '',
          active: article.active || false,
          articleType: article.articleType as ArticleType,
          categoryId: article.categoryId || 0,
          subcategoryId: article.subcategoryId,
          supplierId: article.supplierId,
          mugVariants: article.articleType === 'MUG' ? temporaryMugVariants : undefined,
          shirtVariants: article.articleType === 'SHIRT' ? temporaryShirtVariants : undefined,
          mugDetails: article.mugDetails,
          shirtDetails: article.shirtDetails,
          costCalculation: article.costCalculation,
        };
        console.log('Creating article with data:', createData);
        createArticleMutation.mutate(createData, {
          onSuccess: () => {
            navigate('/admin/articles');
          },
        });
      }
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
      <div className="container mx-auto max-w-7xl p-4 md:p-6">
        <div className="space-y-6">
          {/* Header Skeleton */}
          <div className="flex items-center gap-4">
            <div className="bg-muted h-10 w-10 animate-pulse rounded" />
            <div className="bg-muted h-8 w-48 animate-pulse rounded" />
          </div>

          {/* Tabs Skeleton */}
          <div className="bg-muted h-10 w-full animate-pulse rounded" />

          {/* Content Skeleton */}
          <div className="space-y-4">
            <div className="bg-muted h-32 animate-pulse rounded-lg" />
            <div className="bg-muted h-32 animate-pulse rounded-lg" />
          </div>
        </div>
      </div>
    );
  }

  const ArticleIcon = article.articleType ? articleTypeIcons[article.articleType] : Package;

  return (
    <div className="bg-background min-h-screen">
      {/* Sticky Header */}
      <div className="bg-background sticky top-0 z-10 border-b">
        <div className="container mx-auto max-w-7xl px-4 md:px-6">
          <div className="flex h-16 items-center justify-between">
            {/* Breadcrumb */}
            <div className="flex items-center gap-2 text-sm">
              <Button variant="ghost" size="sm" onClick={() => navigate('/admin/articles')} className="gap-1">
                <ArrowLeft className="h-4 w-4" />
                Articles
              </Button>
              <ChevronRight className="text-muted-foreground h-4 w-4" />
              <span className="font-medium">{isEdit ? 'Edit' : 'New'} Article</span>
            </div>

            {/* Save Button */}
            <Button onClick={handleSubmit} disabled={saving || createArticleMutation.isPending || updateArticleMutation.isPending} size="sm">
              {saving || createArticleMutation.isPending || updateArticleMutation.isPending ? (
                <Loader2 className="mr-2 h-4 w-4 animate-spin" />
              ) : (
                <Save className="mr-2 h-4 w-4" />
              )}
              Save Article
            </Button>
          </div>
        </div>
      </div>

      {/* Main Content */}
      <div className="container mx-auto max-w-7xl p-4 md:p-6">
        {/* Page Title */}
        <div className="mb-8">
          <div className="mb-2 flex items-center gap-3">
            <div className="bg-primary/10 rounded-lg p-2">
              <ArticleIcon className="text-primary h-6 w-6" />
            </div>
            <div>
              <h1 className="text-3xl font-bold tracking-tight">{isEdit ? 'Edit Article' : 'Create New Article'}</h1>
              <p className="text-muted-foreground mt-1">
                {isEdit ? `Editing ${article.name || 'article'}` : 'Fill in the details to create a new product'}
              </p>
            </div>
          </div>
        </div>

        <Tabs defaultValue="general" className="space-y-6">
          <TabsList className="grid h-auto w-full grid-cols-2 p-1 md:grid-cols-5">
            <TabsTrigger value="general" className="gap-2">
              <FileText className="h-4 w-4" />
              <span className="hidden sm:inline">General</span>
            </TabsTrigger>

            {article.articleType === 'MUG' && (
              <TabsTrigger value="specifications" className="gap-2">
                <Settings className="h-4 w-4" />
                <span className="hidden sm:inline">Specs</span>
              </TabsTrigger>
            )}
            {article.articleType === 'SHIRT' && (
              <TabsTrigger value="details" className="gap-2">
                <Settings className="h-4 w-4" />
                <span className="hidden sm:inline">Details</span>
              </TabsTrigger>
            )}

            <TabsTrigger value="variants" className="gap-2">
              <Layers className="h-4 w-4" />
              <span className="hidden sm:inline">Variants</span>
            </TabsTrigger>

            <TabsTrigger value="pricing" className="gap-2">
              <Calculator className="h-4 w-4" />
              <span className="hidden sm:inline">Pricing</span>
            </TabsTrigger>
          </TabsList>

          <TabsContent value="general" className="space-y-6">
            {/* Article Type & Status Card */}
            {!isEdit && (
              <Card>
                <CardHeader>
                  <CardTitle>Article Type</CardTitle>
                  <CardDescription>Choose the type of product you want to create</CardDescription>
                </CardHeader>
                <CardContent>
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
                          break;
                        case 'SHIRT':
                          updates.shirtDetails = {
                            material: '',
                            careInstructions: '',
                            fitType: 'REGULAR',
                            availableSizes: [],
                          };
                          delete updates.mugDetails;
                          break;
                      }

                      setArticle(updates);
                    }}
                  >
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
                    <Switch
                      id="active"
                      checked={article.active}
                      onCheckedChange={(checked: boolean) => setArticle({ ...article, active: checked })}
                    />
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
                    onChange={(e) => setArticle({ ...article, name: e.target.value })}
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
                    onChange={(e) => setArticle({ ...article, descriptionShort: e.target.value })}
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
                    onChange={(e) => setArticle({ ...article, descriptionLong: e.target.value })}
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
                    <Select
                      value={article.categoryId?.toString() || ''}
                      onValueChange={(value) => setArticle({ ...article, categoryId: Number(value), subcategoryId: undefined })}
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
                    <FieldLabel htmlFor="subcategory" optional>
                      Subcategory
                    </FieldLabel>
                    <Select
                      value={article.subcategoryId?.toString() || ''}
                      onValueChange={(value) => setArticle({ ...article, subcategoryId: value ? Number(value) : undefined })}
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
                    onValueChange={(value) => setArticle({ ...article, supplierId: value === 'none' ? undefined : Number(value) })}
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

          <TabsContent value="variants">
            <VariantsTab
              articleId={article.id}
              articleType={article.articleType as ArticleType}
              mugVariants={article.mugVariants || []}
              shirtVariants={article.shirtVariants || []}
              temporaryMugVariants={temporaryMugVariants}
              temporaryShirtVariants={temporaryShirtVariants}
              onAddTemporaryMugVariant={handleAddTemporaryMugVariant}
              onAddTemporaryShirtVariant={handleAddTemporaryShirtVariant}
              onDeleteTemporaryMugVariant={handleDeleteTemporaryMugVariant}
              onDeleteTemporaryShirtVariant={handleDeleteTemporaryShirtVariant}
              onUpdateTemporaryMugVariant={handleUpdateTemporaryMugVariant}
              onUpdateTemporaryShirtVariant={handleUpdateTemporaryShirtVariant}
            />
          </TabsContent>

          <TabsContent value="pricing">
            <CostCalculationTab
              costCalculation={article.costCalculation || {}}
              onChange={(costCalculation) => setArticle({ ...article, costCalculation })}
            />
          </TabsContent>
        </Tabs>
      </div>
    </div>
  );
}
