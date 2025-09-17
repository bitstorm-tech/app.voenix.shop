import { ArticleVariants } from '@/components/admin/articles/ArticleVariants';
import { Accordion, AccordionContent, AccordionItem } from '@/components/ui/Accordion';
import { ArticleImage } from '@/components/ui/ArticleImage';
import { Badge } from '@/components/ui/Badge';
import { Button } from '@/components/ui/Button';
import { Card, CardContent } from '@/components/ui/Card';
import ConfirmationDialog from '@/components/ui/ConfirmationDialog';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/Select';
import { useArticles, useDeleteArticle } from '@/hooks/queries/useArticles';
import { getArticleImage } from '@/lib/articleUtils';
import { cn } from '@/lib/utils';
import type { ArticleType } from '@/types/article';
import * as AccordionPrimitive from '@radix-ui/react-accordion';
import { ChevronDownIcon, Edit, Filter, Package, Plus, Trash2 } from 'lucide-react';
import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useNavigate, useSearchParams } from 'react-router-dom';

const articleTypeColors: Record<ArticleType, string> = {
  MUG: 'bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200',
  SHIRT: 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200',
};

export default function Articles() {
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();
  const [isDeleting, setIsDeleting] = useState(false);
  const [deleteId, setDeleteId] = useState<number | null>(null);
  const { t } = useTranslation('adminArticles');

  const articleTypeLabels: Record<ArticleType, string> = {
    MUG: t('articleTypes.MUG'),
    SHIRT: t('articleTypes.SHIRT'),
  };

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
            <h1 className="text-3xl font-bold tracking-tight">{t('page.title')}</h1>
            <p className="text-muted-foreground mt-2">{t('page.subtitle')}</p>
          </div>

          <div className="flex flex-col gap-3 sm:flex-row sm:items-center">
            {/* Filter */}
            <div className="flex items-center gap-2">
              <Filter className="text-muted-foreground h-4 w-4" aria-hidden="true" />
              <span className="text-muted-foreground text-sm">{t('page.filter.label')}</span>
              <Select value={typeFilter || 'all'} onValueChange={handleTypeFilterChange}>
                <SelectTrigger className="w-[180px]" aria-label={t('page.filter.placeholder')}>
                  <SelectValue placeholder={t('page.filter.placeholder')} />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="all">{t('page.filter.all')}</SelectItem>
                  <SelectItem value="MUG">{t('page.filter.mug')}</SelectItem>
                  <SelectItem value="SHIRT">{t('page.filter.shirt')}</SelectItem>
                </SelectContent>
              </Select>
            </div>

            {/* Add Button */}
            <Button onClick={() => navigate('/admin/articles/new')} className="w-full sm:w-auto">
              <Plus className="mr-2 h-4 w-4" />
              {t('page.actions.new')}
            </Button>
          </div>
        </div>
      </div>

      {/* Table Card */}
      <Card className="py-0">
        <CardContent className="p-0">
          {loading ? (
            <div className="text-muted-foreground flex h-32 flex-col items-center justify-center gap-3">
              <Package className="h-8 w-8 animate-pulse" />
              <p>{t('page.loading')}</p>
            </div>
          ) : articles.length === 0 ? (
            <div className="text-muted-foreground flex h-32 flex-col items-center justify-center gap-3">
              <Package className="h-8 w-8" />
              <p>{t('page.empty.description')}</p>
              <Button variant="outline" size="sm" onClick={() => navigate('/admin/articles/new')}>
                <Plus className="mr-2 h-4 w-4" />
                {t('page.empty.cta')}
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
                          <ChevronDownIcon className="text-muted-foreground pointer-events-none size-4 shrink-0 self-center transition-transform duration-200 [[data-state=open]>&]:rotate-180" />
                          <div className="flex w-full items-center justify-between py-3 pr-24">
                            <div className="grid flex-1 grid-cols-1 items-center gap-4 text-left md:grid-cols-6">
                              {/* Image Column */}
                              <div className="flex items-center">
                                <ArticleImage src={getArticleImage(article)} alt={t('page.imageAlt', { name: article.name })} size="xs" />
                              </div>

                              {/* Name Column */}
                              <div className="flex items-center md:col-span-2">
                                <div className="flex items-center gap-2">
                                  <span className="font-medium">{article.name}</span>
                                  {variantCount > 0 && (
                                    <Badge variant="outline" className="text-xs">
                                      {t('badges.variants', { count: variantCount })}
                                    </Badge>
                                  )}
                                  {article.supplierArticleName && (
                                    <span className="text-muted-foreground text-sm">{article.supplierArticleName}</span>
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
                              <div className="flex items-center">
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
                              <div className="text-muted-foreground flex items-center">{article.supplierName || t('supplier.unknown')}</div>
                            </div>

                            {/* Status Badge */}
                            <Badge
                              variant={article.active ? 'default' : 'secondary'}
                              className={article.active ? 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200' : ''}
                            >
                              {article.active ? t('page.status.active') : t('page.status.inactive')}
                            </Badge>
                          </div>
                        </AccordionPrimitive.Trigger>
                      </AccordionPrimitive.Header>

                      {/* Actions - Positioned absolutely */}
                      <div className="absolute top-1/2 right-6 flex -translate-y-1/2 gap-1 opacity-0 transition-opacity group-hover:opacity-100">
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={(e) => {
                            e.stopPropagation();
                            navigate(`/admin/articles/${article.id}/edit`);
                          }}
                          title={t('page.actions.edit')}
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
                          title={t('page.actions.delete')}
                          className="text-destructive hover:text-destructive"
                        >
                          <Trash2 className="h-4 w-4" />
                        </Button>
                      </div>
                    </div>

                    <AccordionContent className="px-6">
                      <div className="bg-muted/50 rounded-lg p-4">
                        <h4 className="mb-3 text-sm font-medium">{t('page.variantsHeading')}</h4>
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

      <ConfirmationDialog isOpen={isDeleting} onConfirm={confirmDelete} onCancel={cancelDelete} description={t('page.confirmation')} />
    </div>
  );
}
