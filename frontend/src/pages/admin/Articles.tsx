import { ArticleVariants } from '@/components/admin/articles/ArticleVariants';
import { Accordion, AccordionContent, AccordionItem } from '@/components/ui/Accordion';
import * as AccordionPrimitive from '@radix-ui/react-accordion';
import { Badge } from '@/components/ui/Badge';
import { Button } from '@/components/ui/Button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/Card';
import ConfirmationDialog from '@/components/ui/ConfirmationDialog';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/Select';
import { useArticles, useDeleteArticle } from '@/hooks/queries/useArticles';
import type { ArticleType } from '@/types/article';
import { ChevronDownIcon, Edit, Filter, Package, Plus, Trash2 } from 'lucide-react';
import { useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { cn } from '@/lib/utils';

const articleTypeLabels: Record<ArticleType, string> = {
  MUG: 'Mug',
  SHIRT: 'T-Shirt',
};

const articleTypeColors: Record<ArticleType, string> = {
  MUG: 'bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200',
  SHIRT: 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200',
};

export default function Articles() {
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();
  const [isDeleting, setIsDeleting] = useState(false);
  const [deleteId, setDeleteId] = useState<number | null>(null);

  const typeFilter = searchParams.get('type') as ArticleType | null;
  const page = parseInt(searchParams.get('page') || '0');
  const size = parseInt(searchParams.get('size') || '50');

  const { data, isLoading: loading } = useArticles({
    page,
    size,
    type: typeFilter || undefined,
  });

  const deleteArticleMutation = useDeleteArticle();
  const articles = data?.content || [];

  const handleTypeFilterChange = (value: string) => {
    if (value === 'all') {
      searchParams.delete('type');
    } else {
      searchParams.set('type', value);
    }
    setSearchParams(searchParams);
  };

  const handleDelete = async (id: number) => {
    setDeleteId(id);
    setIsDeleting(true);
  };

  const confirmDelete = async () => {
    if (deleteId) {
      deleteArticleMutation.mutate(deleteId, {
        onSuccess: () => {
          setIsDeleting(false);
          setDeleteId(null);
        },
        onError: () => {
          setIsDeleting(false);
          setDeleteId(null);
        },
      });
    }
  };

  const cancelDelete = () => {
    setIsDeleting(false);
    setDeleteId(null);
  };

  return (
    <div className="container mx-auto max-w-7xl p-4 md:p-6">
      {/* Page Header */}
      <div className="mb-8">
        <div className="flex flex-col gap-6 md:flex-row md:items-center md:justify-between">
          <div>
            <h1 className="text-3xl font-bold tracking-tight">Articles</h1>
            <p className="text-muted-foreground mt-2">Manage your product catalog and inventory</p>
          </div>

          <div className="flex flex-col gap-3 sm:flex-row sm:items-center">
            {/* Filter */}
            <div className="flex items-center gap-2">
              <Filter className="text-muted-foreground h-4 w-4" />
              <Select value={typeFilter || 'all'} onValueChange={handleTypeFilterChange}>
                <SelectTrigger className="w-[180px]">
                  <SelectValue placeholder="Filter by type" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="all">All Types</SelectItem>
                  <SelectItem value="MUG">Mugs</SelectItem>
                  <SelectItem value="SHIRT">T-Shirts</SelectItem>
                </SelectContent>
              </Select>
            </div>

            {/* Add Button */}
            <Button onClick={() => navigate('/admin/articles/new')} className="w-full sm:w-auto">
              <Plus className="mr-2 h-4 w-4" />
              New Article
            </Button>
          </div>
        </div>
      </div>

      {/* Table Card */}
      <Card>
        <CardHeader className="px-6 py-4">
          <div className="flex items-center justify-between">
            <CardTitle className="text-base font-medium">{typeFilter ? `${articleTypeLabels[typeFilter]}s` : 'All Articles'}</CardTitle>
            <CardDescription>
              {articles.length} {articles.length === 1 ? 'item' : 'items'}
            </CardDescription>
          </div>
        </CardHeader>

        <CardContent className="p-0">
          {loading ? (
            <div className="text-muted-foreground flex h-32 flex-col items-center justify-center gap-3">
              <Package className="h-8 w-8 animate-pulse" />
              <p>Loading articles...</p>
            </div>
          ) : articles.length === 0 ? (
            <div className="text-muted-foreground flex h-32 flex-col items-center justify-center gap-3">
              <Package className="h-8 w-8" />
              <p>No articles found</p>
              <Button variant="outline" size="sm" onClick={() => navigate('/admin/articles/new')}>
                <Plus className="mr-2 h-4 w-4" />
                Add your first article
              </Button>
            </div>
          ) : (
            <Accordion type="multiple" className="w-full">
              {articles.map((article) => {
                const variantCount = article.articleType === 'MUG' ? article.mugVariants?.length || 0 : article.shirtVariants?.length || 0;

                return (
                  <AccordionItem key={article.id} value={`article-${article.id}`} className="group border-b last:border-b-0">
                    <div className="relative">
                      <AccordionPrimitive.Header className="flex">
                        <AccordionPrimitive.Trigger
                          data-slot="accordion-trigger"
                          className={cn(
                            'focus-visible:border-ring focus-visible:ring-ring/50 flex w-full items-start gap-4 rounded-md px-6 py-0 text-left text-sm font-medium transition-all outline-none hover:no-underline focus-visible:ring-[3px] disabled:pointer-events-none disabled:opacity-50',
                          )}
                        >
                          <ChevronDownIcon className="text-muted-foreground pointer-events-none mt-4 size-4 shrink-0 transition-transform duration-200 [[data-state=open]>&]:rotate-180" />
                          <div className="flex w-full items-center justify-between py-4 pr-24">
                            <div className="grid flex-1 grid-cols-1 gap-4 text-left md:grid-cols-5">
                              {/* Name Column */}
                              <div className="md:col-span-2">
                                <div className="flex items-center gap-2">
                                  <span className="font-medium">{article.name}</span>
                                  {variantCount > 0 && (
                                    <Badge variant="outline" className="text-xs">
                                      {variantCount} variant{variantCount !== 1 ? 's' : ''}
                                    </Badge>
                                  )}
                                </div>
                              </div>

                              {/* Type Column */}
                              <div className="flex items-center gap-2">
                                <Badge variant="secondary" className={articleTypeColors[article.articleType]}>
                                  {articleTypeLabels[article.articleType]}
                                </Badge>
                              </div>

                              {/* Category Column */}
                              <div>
                                <div className="flex flex-wrap items-center">
                                  <span className="font-medium">{article.categoryName || '-'}</span>
                                  {article.subcategoryName && (
                                    <>
                                      <span className="px-1">&gt;</span>
                                      <span className="text-muted-foreground text-sm">{article.subcategoryName}</span>
                                    </>
                                  )}
                                </div>
                              </div>

                              {/* Supplier Column */}
                              <div className="text-muted-foreground">{article.supplierName || '-'}</div>
                            </div>

                            {/* Status Badge */}
                            <Badge
                              variant={article.active ? 'default' : 'secondary'}
                              className={article.active ? 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200' : ''}
                            >
                              {article.active ? 'Active' : 'Inactive'}
                            </Badge>
                          </div>
                        </AccordionPrimitive.Trigger>
                      </AccordionPrimitive.Header>

                      {/* Actions - Positioned absolutely */}
                      <div className="absolute right-6 top-1/2 flex -translate-y-1/2 gap-1 opacity-0 transition-opacity group-hover:opacity-100">
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={(e) => {
                            e.stopPropagation();
                            navigate(`/admin/articles/${article.id}/edit`);
                          }}
                          title="Edit article"
                        >
                          <Edit className="h-4 w-4" />
                        </Button>
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={(e) => {
                            e.stopPropagation();
                            handleDelete(article.id);
                          }}
                          title="Delete article"
                          className="text-destructive hover:text-destructive"
                        >
                          <Trash2 className="h-4 w-4" />
                        </Button>
                      </div>
                    </div>

                    <AccordionContent className="px-6">
                      <div className="bg-muted/50 rounded-lg p-4">
                        <h4 className="mb-3 text-sm font-medium">Variants</h4>
                        <ArticleVariants article={article} />
                      </div>
                    </AccordionContent>
                  </AccordionItem>
                );
              })}
            </Accordion>
          )}
        </CardContent>
      </Card>

      <ConfirmationDialog
        isOpen={isDeleting}
        onConfirm={confirmDelete}
        onCancel={cancelDelete}
        description="Are you sure you want to delete this article? This action cannot be undone."
      />
    </div>
  );
}
