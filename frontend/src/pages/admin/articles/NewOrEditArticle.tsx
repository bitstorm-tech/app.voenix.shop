import { Button } from '@/components/ui/Button';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/Tabs';
import { useArticleForm } from '@/hooks/useArticleForm';
import { useArticleFormStore } from '@/stores/admin/articles/useArticleFormStore';
import { useCostCalculationStore } from '@/stores/admin/articles/useCostCalculationStore';
import { useVariantStore } from '@/stores/admin/articles/useVariantStore';
import { ArrowLeft, Calculator, ChevronRight, Coffee, FileText, Layers, Loader2, Package, Save, Settings, Shirt } from 'lucide-react';
import { useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import CostCalculationTab from './components/CostCalculationTab';
import GeneralTab from './components/GeneralTab';
import VariantsTab from './components/VariantsTab';
import MugDetailsTab from './tabs/MugDetailsTab';
import ShirtDetailsTab from './tabs/ShirtDetailsTab';

const articleTypeIcons = {
  MUG: Coffee,
  SHIRT: Shirt,
};

export default function NewOrEditArticle() {
  const navigate = useNavigate();
  const { id } = useParams();
  const isEdit = !!id;

  // Store hooks
  const { article, isLoading, initializeForm, updateMugDetails, updateShirtDetails, resetForm } = useArticleFormStore();

  const { setMugVariants, setShirtVariants, resetVariants } = useVariantStore();
  const { setCostCalculation, resetCostCalculation } = useCostCalculationStore();

  // Form hook
  const { saveArticle, isSaving } = useArticleForm();

  // Initialize form on mount
  useEffect(() => {
    initializeForm(id ? Number(id) : undefined);

    // Set existing variants if editing
    if (id && article.id) {
      if (article.mugVariants) {
        setMugVariants(article.mugVariants);
      }
      if (article.shirtVariants) {
        setShirtVariants(article.shirtVariants);
      }
      if (article.costCalculation) {
        setCostCalculation(article.costCalculation);
      }
    }

    // Cleanup on unmount
    return () => {
      resetForm();
      resetVariants();
      resetCostCalculation();
    };
  }, [id]);

  // Loading state
  if (isLoading) {
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
            <Button onClick={saveArticle} disabled={isSaving} size="sm">
              {isSaving ? <Loader2 className="mr-2 h-4 w-4 animate-spin" /> : <Save className="mr-2 h-4 w-4" />}
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

          <TabsContent value="general">
            <GeneralTab />
          </TabsContent>

          {article.articleType === 'MUG' && (
            <TabsContent value="specifications">
              <MugDetailsTab mugDetails={article.mugDetails || {}} onChange={updateMugDetails} />
            </TabsContent>
          )}

          {article.articleType === 'SHIRT' && (
            <TabsContent value="details">
              <ShirtDetailsTab shirtDetails={article.shirtDetails || {}} onChange={updateShirtDetails} />
            </TabsContent>
          )}

          <TabsContent value="variants">
            <VariantsTab />
          </TabsContent>

          <TabsContent value="pricing">
            <CostCalculationTab />
          </TabsContent>
        </Tabs>
      </div>
    </div>
  );
}
