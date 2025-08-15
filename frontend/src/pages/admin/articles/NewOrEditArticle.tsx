import { Button } from '@/components/ui/Button';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/Tabs';
import { useArticleForm } from '@/hooks/useArticleForm';
import { useArticleFormStore } from '@/stores/admin/articles/useArticleFormStore';
import { ArrowLeft, Calculator, ChevronRight, Coffee, FileText, Layers, Loader2, Package, Save, Shirt } from 'lucide-react';
import { useEffect } from 'react';
import { useLocation, useNavigate, useParams } from 'react-router-dom';
import GeneralTab from './components/GeneralTab';
import PriceCalculationTab from './components/PriceCalculationTab';
import VariantsTab from './components/VariantsTab';
import MugDetailsTab from './tabs/MugDetailsTab';
import ShirtDetailsTab from './tabs/ShirtDetailsTab';

const articleTypeIcons = {
  MUG: Coffee,
  SHIRT: Shirt,
};

export default function NewOrEditArticle() {
  const navigate = useNavigate();
  const location = useLocation();
  const { id } = useParams();
  const articleId = id ? Number(id) : undefined;

  // Use the new consolidated store
  const { article, activeTab, setActiveTab, initializeForm, resetForm, updateShirtDetails } = useArticleFormStore();

  // Use the new React Query based hook
  const { isLoading, isSaving, saveArticle, categories, subcategories } = useArticleForm(articleId);

  // Initialize form on mount
  useEffect(() => {
    // If no articleId, initialize with empty form
    if (!articleId) {
      initializeForm();
    }

    // Cleanup on unmount
    return () => {
      resetForm();
    };
  }, [articleId, initializeForm, resetForm]);

  // Set active tab from navigation state if provided
  useEffect(() => {
    const stateActiveTab = location.state?.activeTab;
    if (stateActiveTab && typeof stateActiveTab === 'string') {
      setActiveTab(stateActiveTab);
    }
  }, [location.state, setActiveTab]);

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
  const isEdit = !!articleId;

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
            <Button onClick={saveArticle} disabled={isSaving} className="gap-2">
              {isSaving ? <Loader2 className="h-4 w-4 animate-spin" /> : <Save className="h-4 w-4" />}
              {isEdit ? 'Update' : 'Create'} Article
            </Button>
          </div>
        </div>
      </div>

      {/* Main Content */}
      <div className="container mx-auto max-w-7xl p-4 md:p-6">
        <div className="space-y-6">
          {/* Article Type Header */}
          <div className="flex items-center gap-3">
            <div className="bg-primary/10 text-primary flex h-12 w-12 items-center justify-center rounded-lg">
              <ArticleIcon className="h-6 w-6" />
            </div>
            <div>
              <h1 className="text-2xl font-bold">{isEdit ? `Edit ${article.articleType === 'MUG' ? 'Mug' : 'Shirt'}` : 'Create New Article'}</h1>
              {article.name && <p className="text-muted-foreground text-sm">{article.name}</p>}
            </div>
          </div>

          {/* Tabs */}
          <Tabs value={activeTab} onValueChange={setActiveTab} className="space-y-6">
            <TabsList className="grid w-full grid-cols-4 lg:w-auto lg:grid-flow-col">
              <TabsTrigger value="general" className="gap-2">
                <FileText className="h-4 w-4" />
                <span className="hidden sm:inline">General</span>
              </TabsTrigger>

              {article.articleType === 'MUG' && (
                <TabsTrigger value="mug-details" className="gap-2">
                  <Coffee className="h-4 w-4" />
                  <span className="hidden sm:inline">Mug Details</span>
                </TabsTrigger>
              )}

              {article.articleType === 'SHIRT' && (
                <TabsTrigger value="shirt-details" className="gap-2">
                  <Shirt className="h-4 w-4" />
                  <span className="hidden sm:inline">Shirt Details</span>
                </TabsTrigger>
              )}

              <TabsTrigger value="variants" className="gap-2">
                <Layers className="h-4 w-4" />
                <span className="hidden sm:inline">Variants</span>
              </TabsTrigger>

              <TabsTrigger value="cost-calculation" className="gap-2">
                <Calculator className="h-4 w-4" />
                <span className="hidden sm:inline">Price Calculation</span>
              </TabsTrigger>
            </TabsList>

            {/* Tab Contents */}
            <TabsContent value="general">
              <GeneralTab categories={categories} subcategories={subcategories} />
            </TabsContent>

            {article.articleType === 'MUG' && (
              <TabsContent value="mug-details">
                <MugDetailsTab />
              </TabsContent>
            )}

            {article.articleType === 'SHIRT' && (
              <TabsContent value="shirt-details">
                <ShirtDetailsTab shirtDetails={article.shirtDetails} onChange={updateShirtDetails} />
              </TabsContent>
            )}

            <TabsContent value="variants">
              <VariantsTab />
            </TabsContent>

            <TabsContent value="cost-calculation">
              <PriceCalculationTab />
            </TabsContent>
          </Tabs>
        </div>
      </div>
    </div>
  );
}
