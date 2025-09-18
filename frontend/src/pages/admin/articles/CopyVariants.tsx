import { Button } from '@/components/ui/Button';
import { Checkbox } from '@/components/ui/Checkbox';
import { Input } from '@/components/ui/Input';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/Table';
import { articleKeys } from '@/hooks/queries/useArticles';
import { articlesApi } from '@/lib/api';
import { cn } from '@/lib/utils';
import type { MugWithVariantsSummary } from '@/types/copyVariants';
import { useQueryClient } from '@tanstack/react-query';
import { AlertCircle, ArrowLeft, Copy, Image as ImageIcon, Loader2, Search } from 'lucide-react';
import { useCallback, useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { toast } from 'sonner';

export default function CopyVariants() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const currentMugId = id ? parseInt(id) : undefined;
  const { t } = useTranslation('adminArticles');

  const [mugs, setMugs] = useState<MugWithVariantsSummary[]>([]);
  const [filteredMugs, setFilteredMugs] = useState<MugWithVariantsSummary[]>([]);
  const [selectedVariantIds, setSelectedVariantIds] = useState<Set<number>>(new Set());
  const [loading, setLoading] = useState(false);
  const [errorKey, setErrorKey] = useState<string | null>(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [copying, setCopying] = useState(false);

  const fetchMugsWithVariants = useCallback(async () => {
    setLoading(true);
    setErrorKey(null);
    try {
      const data = await articlesApi.getVariantsCatalog(currentMugId);
      setMugs(data);
    } catch (err) {
      console.error('Error fetching variants catalog:', err);
      setErrorKey('fetchFailed');
    } finally {
      setLoading(false);
    }
  }, [currentMugId]);

  useEffect(() => {
    fetchMugsWithVariants();
  }, [fetchMugsWithVariants]);

  useEffect(() => {
    // Filter mugs based on search term
    if (!searchTerm.trim()) {
      setFilteredMugs(mugs);
    } else {
      const filtered = mugs.filter(
        (mug) =>
          mug.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
          mug.supplierArticleName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
          mug.variants.some((variant) => variant.name.toLowerCase().includes(searchTerm.toLowerCase())),
      );
      setFilteredMugs(filtered);
    }
  }, [mugs, searchTerm]);

  const handleVariantToggle = (variantId: number, checked: boolean) => {
    const newSelected = new Set(selectedVariantIds);
    if (checked) {
      newSelected.add(variantId);
    } else {
      newSelected.delete(variantId);
    }
    setSelectedVariantIds(newSelected);
  };

  const handleSelectAllFromMug = (mug: MugWithVariantsSummary, selectAll: boolean) => {
    const newSelected = new Set(selectedVariantIds);
    mug.variants.forEach((variant) => {
      if (selectAll) {
        newSelected.add(variant.id);
      } else {
        newSelected.delete(variant.id);
      }
    });
    setSelectedVariantIds(newSelected);
  };

  const handleCopy = async () => {
    if (!currentMugId) {
      toast.error(t('copyVariants.toasts.invalidMug'));
      return;
    }

    const variantIds = Array.from(selectedVariantIds);
    if (variantIds.length === 0) {
      toast.error(t('copyVariants.toasts.selectVariant'));
      return;
    }

    setCopying(true);
    try {
      const copiedVariants = await articlesApi.copyVariants(currentMugId, variantIds);
      toast.success(t('copyVariants.toasts.copied', { count: copiedVariants.length }));

      // Invalidate the article cache to ensure fresh data is loaded
      queryClient.invalidateQueries({ queryKey: articleKeys.detail(currentMugId) });

      navigate(`/admin/articles/${currentMugId}/edit`, { state: { activeTab: 'variants' } });
    } catch (error) {
      console.error('Error copying variants:', error);
      toast.error(t('copyVariants.toasts.copyFailed'));
    } finally {
      setCopying(false);
    }
  };

  const handleCancel = () => {
    navigate(`/admin/articles/${currentMugId}/edit`, { state: { activeTab: 'variants' } });
  };

  const selectedCount = selectedVariantIds.size;
  const hasVariants = mugs.some((mug) => mug.variants.length > 0);
  const displayedMugs = filteredMugs.filter((mug) => mug.variants.length > 0);

  return (
    <div className="container mx-auto space-y-6 p-6 py-6">
      {/* Header with breadcrumb */}
      <div className="flex items-center gap-4">
        <Link to={`/admin/articles/${currentMugId}/edit`} className="flex items-center gap-2 text-gray-600 transition-colors hover:text-gray-900">
          <ArrowLeft className="h-4 w-4" />
          {t('copyVariants.breadcrumb.back')}
        </Link>
        <div className="flex items-center gap-2 text-sm text-gray-500">
          <span>/</span>
          <span>{t('copyVariants.breadcrumb.current')}</span>
        </div>
      </div>

      {/* Page Header */}
      <div>
        <h1 className="text-2xl font-bold text-gray-900">{t('copyVariants.title')}</h1>
        <p className="mt-1 text-gray-600">{t('copyVariants.description')}</p>
      </div>

      {/* Search and Filter */}
      <div className="pb-6">
        <h2 className="mb-4 text-lg font-semibold text-gray-900">{t('copyVariants.search.title')}</h2>
        <div className="flex items-center gap-4">
          <div className="relative flex-1">
            <Search className="absolute top-1/2 left-3 h-4 w-4 -translate-y-1/2 transform text-gray-400" />
            <Input
              placeholder={t('copyVariants.search.placeholder')}
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="pl-10"
            />
          </div>
          {searchTerm && (
            <Button variant="outline" onClick={() => setSearchTerm('')}>
              {t('copyVariants.search.clear')}
            </Button>
          )}
        </div>
      </div>

      {/* Main Content */}
      <div className="space-y-6">
        {loading && (
          <div className="flex items-center justify-center py-12">
            <Loader2 className="h-6 w-6 animate-spin" />
            <span className="ml-2 text-sm text-gray-600">{t('copyVariants.loading')}</span>
          </div>
        )}

        {errorKey && (
          <div className="flex items-center gap-2 rounded-lg border border-red-200 bg-red-50 p-4 text-sm text-red-800">
            <AlertCircle className="h-4 w-4 flex-shrink-0" />
            {t(`copyVariants.error.${errorKey}`)}
            <Button variant="outline" size="sm" onClick={fetchMugsWithVariants} className="ml-auto">
              {t('copyVariants.error.retry')}
            </Button>
          </div>
        )}

        {!loading && !errorKey && mugs.length === 0 && (
          <div className="flex flex-col items-center justify-center py-12 text-center">
            <Copy className="mb-4 h-12 w-12 text-gray-400" />
            <h3 className="mb-2 text-lg font-medium text-gray-900">{t('copyVariants.empty.noOtherMugs.title')}</h3>
            <p className="max-w-sm text-sm text-gray-600">{t('copyVariants.empty.noOtherMugs.description')}</p>
          </div>
        )}

        {!loading && !errorKey && mugs.length > 0 && !hasVariants && (
          <div className="flex flex-col items-center justify-center py-12 text-center">
            <Copy className="mb-4 h-12 w-12 text-gray-400" />
            <h3 className="mb-2 text-lg font-medium text-gray-900">{t('copyVariants.empty.noVariants.title')}</h3>
            <p className="max-w-sm text-sm text-gray-600">{t('copyVariants.empty.noVariants.description')}</p>
          </div>
        )}

        {!loading && !errorKey && hasVariants && displayedMugs.length === 0 && searchTerm && (
          <div className="flex flex-col items-center justify-center py-12 text-center">
            <Search className="mb-4 h-12 w-12 text-gray-400" />
            <h3 className="mb-2 text-lg font-medium text-gray-900">{t('copyVariants.empty.noResults.title')}</h3>
            <p className="max-w-sm text-sm text-gray-600">{t('copyVariants.empty.noResults.description')}</p>
          </div>
        )}

        {!loading && !errorKey && hasVariants && displayedMugs.length > 0 && (
          <>
            {/* Variants by Mug */}
            <div className="space-y-6">
              {displayedMugs.map((mug) => {
                const mugVariantIds = mug.variants.map((v) => v.id);
                const selectedFromMug = mugVariantIds.filter((id) => selectedVariantIds.has(id));
                const allSelected = selectedFromMug.length === mug.variants.length;
                const someSelected = selectedFromMug.length > 0;

                return (
                  <div key={mug.id} className="pb-6">
                    {/* Mug Header */}
                    <div className="mb-4">
                      <h3 className="text-lg font-semibold text-gray-900">{mug.name}</h3>
                      {mug.supplierArticleName && (
                        <p className="mt-1 text-sm text-gray-600">{t('copyVariants.table.supplierLabel', { supplier: mug.supplierArticleName })}</p>
                      )}
                    </div>

                    {/* Variants Table */}
                    <div className="overflow-hidden rounded-lg border border-gray-200">
                      <Table>
                        <TableHeader>
                          <TableRow>
                            <TableHead className="w-12">
                              <div className="flex items-center space-x-2">
                                <Checkbox
                                  id={`mug-${mug.id}`}
                                  checked={allSelected}
                                  ref={(el) => {
                                    if (el) {
                                      const checkboxElement = el.querySelector('input[type="checkbox"]') as HTMLInputElement;
                                      if (checkboxElement) {
                                        checkboxElement.indeterminate = someSelected && !allSelected;
                                      }
                                    }
                                  }}
                                  onCheckedChange={(checked) => handleSelectAllFromMug(mug, checked === true)}
                                />
                              </div>
                            </TableHead>
                            <TableHead>{t('copyVariants.table.variantName')}</TableHead>
                            <TableHead>{t('copyVariants.table.articleNumber')}</TableHead>
                            <TableHead>{t('copyVariants.table.colors')}</TableHead>
                            <TableHead>{t('copyVariants.table.status')}</TableHead>
                            <TableHead>{t('copyVariants.table.preview')}</TableHead>
                          </TableRow>
                        </TableHeader>
                        <TableBody>
                          {mug.variants.map((variant) => (
                            <TableRow
                              key={variant.id}
                              data-state={selectedVariantIds.has(variant.id) ? 'selected' : undefined}
                              className={cn(!variant.active && 'opacity-60')}
                            >
                              <TableCell>
                                <Checkbox
                                  id={`variant-${variant.id}`}
                                  checked={selectedVariantIds.has(variant.id)}
                                  onCheckedChange={(checked) => handleVariantToggle(variant.id, checked === true)}
                                />
                              </TableCell>
                              <TableCell>
                                <label htmlFor={`variant-${variant.id}`} className="cursor-pointer font-medium">
                                  {variant.name}
                                </label>
                              </TableCell>
                              <TableCell className="text-sm text-gray-600">{variant.articleVariantNumber || '-'}</TableCell>
                              <TableCell>
                                <div className="flex items-center gap-1">
                                  <div
                                    className="h-4 w-4 rounded border border-gray-300"
                                    style={{ backgroundColor: variant.insideColorCode }}
                                    title={t('copyVariants.table.insideColorTitle', { color: variant.insideColorCode })}
                                  />
                                  <div
                                    className="h-4 w-4 rounded border border-gray-300"
                                    style={{ backgroundColor: variant.outsideColorCode }}
                                    title={t('copyVariants.table.outsideColorTitle', { color: variant.outsideColorCode })}
                                  />
                                </div>
                              </TableCell>
                              <TableCell>
                                {variant.active ? (
                                  <span className="inline-flex items-center rounded-full bg-green-100 px-2 py-1 text-xs font-medium text-green-700">
                                    {t('form.common.active')}
                                  </span>
                                ) : (
                                  <span className="inline-flex items-center rounded-full bg-gray-100 px-2 py-1 text-xs font-medium text-gray-600">
                                    {t('form.common.inactive')}
                                  </span>
                                )}
                              </TableCell>
                              <TableCell>
                                {variant.exampleImageUrl ? (
                                  <img
                                    src={variant.exampleImageUrl}
                                    alt={t('copyVariants.table.imageAlt', { name: variant.name })}
                                    className="h-12 w-12 rounded border object-cover"
                                  />
                                ) : (
                                  <div className="flex h-12 w-12 items-center justify-center rounded border bg-gray-100">
                                    <ImageIcon className="h-4 w-4 text-gray-400" />
                                  </div>
                                )}
                              </TableCell>
                            </TableRow>
                          ))}
                        </TableBody>
                      </Table>
                    </div>
                  </div>
                );
              })}
            </div>
          </>
        )}
      </div>

      {/* Fixed Footer with Actions */}
      {hasVariants && displayedMugs.length > 0 && (
        <div className="fixed right-0 bottom-0 left-0 z-10 border-t bg-white shadow-lg">
          <div className="container mx-auto px-6 py-4">
            <div className="flex items-center justify-between">
              <div className="text-sm text-gray-600">
                {selectedCount > 0 ? (
                  <span>{t('copyVariants.footer.selected', { count: selectedCount })}</span>
                ) : (
                  <span>{t('copyVariants.footer.noneSelected')}</span>
                )}
              </div>
              <div className="flex items-center gap-3">
                <Button variant="outline" onClick={handleCancel} disabled={copying}>
                  {t('copyVariants.actions.cancel')}
                </Button>
                <Button onClick={handleCopy} disabled={selectedCount === 0 || copying} className="min-w-[140px]">
                  {copying ? (
                    <>
                      <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                      {t('copyVariants.actions.copying')}
                    </>
                  ) : (
                    <>
                      <Copy className="mr-2 h-4 w-4" />
                      {selectedCount > 0 ? t('copyVariants.actions.copySelected', { count: selectedCount }) : t('copyVariants.actions.copy')}
                    </>
                  )}
                </Button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Bottom padding to account for fixed footer */}
      {hasVariants && displayedMugs.length > 0 && <div className="h-20" />}
    </div>
  );
}
