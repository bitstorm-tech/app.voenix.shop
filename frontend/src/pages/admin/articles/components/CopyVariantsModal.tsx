import { Button } from '@/components/ui/Button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/Card';
import { Checkbox } from '@/components/ui/Checkbox';
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from '@/components/ui/Dialog';
import { articlesApi } from '@/lib/api';
import { cn } from '@/lib/utils';
import type { MugWithVariantsSummary } from '@/types/copyVariants';
import { AlertCircle, Copy, Image as ImageIcon, Loader2 } from 'lucide-react';
import { useEffect, useState } from 'react';
import { toast } from 'sonner';

interface CopyVariantsModalProps {
  isOpen: boolean;
  onClose: () => void;
  onCopy: (variantIds: number[]) => void;
  currentMugId?: number;
}

export default function CopyVariantsModal({ isOpen, onClose, onCopy, currentMugId }: CopyVariantsModalProps) {
  const [mugs, setMugs] = useState<MugWithVariantsSummary[]>([]);
  const [selectedVariantIds, setSelectedVariantIds] = useState<Set<number>>(new Set());
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (isOpen) {
      fetchMugsWithVariants();
      setSelectedVariantIds(new Set());
    }
  }, [isOpen, currentMugId]);

  const fetchMugsWithVariants = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await articlesApi.getVariantsCatalog(currentMugId);
      setMugs(data);
    } catch (err) {
      console.error('Error fetching variants catalog:', err);
      setError('Failed to load mugs and variants. Please try again.');
    } finally {
      setLoading(false);
    }
  };

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

  const handleCopy = () => {
    const variantIds = Array.from(selectedVariantIds);
    if (variantIds.length === 0) {
      toast.error('Please select at least one variant to copy');
      return;
    }
    onCopy(variantIds);
    onClose();
  };

  const handleClose = () => {
    setSelectedVariantIds(new Set());
    setError(null);
    onClose();
  };

  const selectedCount = selectedVariantIds.size;
  const hasVariants = mugs.some((mug) => mug.variants.length > 0);

  return (
    <Dialog open={isOpen} onOpenChange={handleClose}>
      <DialogContent className="max-w-4xl max-h-[80vh] overflow-hidden">
        <DialogHeader>
          <DialogTitle>Copy Mug Variants</DialogTitle>
          <DialogDescription>
            Select variants from existing mugs to copy to the current mug. You can select individual variants or all variants from a mug.
          </DialogDescription>
        </DialogHeader>

        <div className="flex-1 overflow-y-auto">
          {loading && (
            <div className="flex items-center justify-center py-12">
              <Loader2 className="h-6 w-6 animate-spin" />
              <span className="ml-2 text-sm text-gray-600">Loading mugs and variants...</span>
            </div>
          )}

          {error && (
            <div className="flex items-center gap-2 rounded-lg border border-red-200 bg-red-50 p-4 text-sm text-red-800">
              <AlertCircle className="h-4 w-4 flex-shrink-0" />
              {error}
              <Button variant="outline" size="sm" onClick={fetchMugsWithVariants} className="ml-auto">
                Retry
              </Button>
            </div>
          )}

          {!loading && !error && mugs.length === 0 && (
            <div className="flex flex-col items-center justify-center py-12 text-center">
              <Copy className="h-12 w-12 text-gray-400 mb-4" />
              <h3 className="text-lg font-medium text-gray-900 mb-2">No Other Mugs Found</h3>
              <p className="text-sm text-gray-600 max-w-sm">
                There are no other mugs with variants available to copy from. Create some variants on other mugs first.
              </p>
            </div>
          )}

          {!loading && !error && mugs.length > 0 && !hasVariants && (
            <div className="flex flex-col items-center justify-center py-12 text-center">
              <Copy className="h-12 w-12 text-gray-400 mb-4" />
              <h3 className="text-lg font-medium text-gray-900 mb-2">No Variants Available</h3>
              <p className="text-sm text-gray-600 max-w-sm">
                While there are other mugs in the system, none of them have variants to copy. Create some variants on other mugs first.
              </p>
            </div>
          )}

          {!loading && !error && hasVariants && (
            <div className="space-y-4">
              {mugs
                .filter((mug) => mug.variants.length > 0)
                .map((mug) => {
                  const mugVariantIds = mug.variants.map((v) => v.id);
                  const selectedFromMug = mugVariantIds.filter((id) => selectedVariantIds.has(id));
                  const allSelected = selectedFromMug.length === mug.variants.length;
                  const someSelected = selectedFromMug.length > 0;

                  return (
                    <Card key={mug.id}>
                      <CardHeader className="pb-3">
                        <div className="flex items-center justify-between">
                          <div>
                            <CardTitle className="text-lg">{mug.name}</CardTitle>
                            {mug.supplierArticleName && (
                              <CardDescription className="mt-1">
                                Supplier: {mug.supplierArticleName}
                              </CardDescription>
                            )}
                            <CardDescription className="mt-1">
                              {mug.variants.length} variant{mug.variants.length !== 1 ? 's' : ''} available
                            </CardDescription>
                          </div>
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
                            <label htmlFor={`mug-${mug.id}`} className="text-sm font-medium cursor-pointer">
                              Select All
                            </label>
                          </div>
                        </div>
                      </CardHeader>
                      <CardContent>
                        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-3">
                          {mug.variants.map((variant) => (
                            <div
                              key={variant.id}
                              className={cn(
                                'flex items-center space-x-3 rounded-lg border p-3 transition-colors',
                                selectedVariantIds.has(variant.id)
                                  ? 'border-blue-200 bg-blue-50'
                                  : 'border-gray-200 hover:border-gray-300',
                                !variant.active && 'opacity-60'
                              )}
                            >
                              <Checkbox
                                id={`variant-${variant.id}`}
                                checked={selectedVariantIds.has(variant.id)}
                                onCheckedChange={(checked) => handleVariantToggle(variant.id, checked === true)}
                              />
                              <div className="flex-1 min-w-0">
                                <div className="flex items-center gap-2 mb-2">
                                  <label htmlFor={`variant-${variant.id}`} className="font-medium cursor-pointer truncate">
                                    {variant.name}
                                  </label>
                                  {!variant.active && (
                                    <span className="inline-flex items-center rounded-full bg-gray-100 px-2 py-1 text-xs font-medium text-gray-600">
                                      Inactive
                                    </span>
                                  )}
                                </div>
                                <div className="flex items-center gap-2 mb-2">
                                  <div className="flex items-center gap-1">
                                    <div
                                      className="w-4 h-4 rounded border border-gray-300 flex-shrink-0"
                                      style={{ backgroundColor: variant.insideColorCode }}
                                      title={`Inside: ${variant.insideColorCode}`}
                                    />
                                    <div
                                      className="w-4 h-4 rounded border border-gray-300 flex-shrink-0"
                                      style={{ backgroundColor: variant.outsideColorCode }}
                                      title={`Outside: ${variant.outsideColorCode}`}
                                    />
                                  </div>
                                  {variant.articleVariantNumber && (
                                    <span className="text-xs text-gray-500 truncate">
                                      {variant.articleVariantNumber}
                                    </span>
                                  )}
                                </div>
                                {variant.exampleImageUrl ? (
                                  <img
                                    src={variant.exampleImageUrl}
                                    alt={`${variant.name} example`}
                                    className="w-10 h-10 rounded border object-cover"
                                  />
                                ) : (
                                  <div className="w-10 h-10 flex items-center justify-center rounded border bg-gray-100">
                                    <ImageIcon className="w-4 h-4 text-gray-400" />
                                  </div>
                                )}
                              </div>
                            </div>
                          ))}
                        </div>
                      </CardContent>
                    </Card>
                  );
                })}
            </div>
          )}
        </div>

        <DialogFooter>
          <Button variant="outline" onClick={handleClose}>
            Cancel
          </Button>
          <Button
            onClick={handleCopy}
            disabled={selectedCount === 0}
            className="min-w-[140px]"
          >
            <Copy className="w-4 h-4 mr-2" />
            Copy {selectedCount > 0 ? `${selectedCount} ` : ''}Variant{selectedCount !== 1 ? 's' : ''}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}