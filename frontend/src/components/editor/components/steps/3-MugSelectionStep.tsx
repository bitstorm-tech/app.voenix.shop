import { MugOption, MugVariant } from '@/components/editor/types';
import { useMugs } from '@/hooks/useMugs';
import { cn } from '@/lib/utils';
import { useWizardStore } from '@/stores/editor/useWizardStore';
import { Check, Loader2, Star } from 'lucide-react';

export default function MugSelectionStep() {
  const selectedMug = useWizardStore((state) => state.selectedMug);
  const selectedVariant = useWizardStore((state) => state.selectedVariant);
  const selectMug = useWizardStore((state) => state.selectMug);
  const selectVariant = useWizardStore((state) => state.selectVariant);
  const { mugs, loading, error } = useMugs();
  if (loading) {
    return (
      <div className="flex items-center justify-center py-12">
        <Loader2 className="h-8 w-8 animate-spin text-gray-400" />
      </div>
    );
  }

  if (error) {
    return (
      <div className="rounded-lg bg-red-50 p-4 text-center">
        <p className="text-sm text-red-600">Failed to load mugs. Please try again later.</p>
      </div>
    );
  }

  if (mugs.length === 0) {
    return (
      <div className="rounded-lg bg-gray-50 p-4 text-center">
        <p className="text-sm text-gray-600">No mugs available at the moment.</p>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div>
        <h3 className="mb-2 text-lg font-semibold">Choose Your Mug</h3>
        <p className="text-sm text-gray-600">Select the perfect mug to showcase your personalized design</p>
      </div>

      <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-4">
        {mugs.map((mug: MugOption) => {
          const isSelected = selectedMug?.id === mug.id;
          // Show selected variant image if this is the selected mug
          // For non-selected mugs, the image is already set to the default variant in useMugs hook
          const displayImage = isSelected && selectedVariant?.exampleImageUrl ? selectedVariant.exampleImageUrl : mug.image;

          return (
            <div
              key={mug.id}
              onClick={() => selectMug(mug)}
              className={cn(
                'relative cursor-pointer overflow-hidden rounded-lg border-2 transition-all',
                isSelected ? 'border-primary shadow-lg' : 'border-gray-200 hover:border-gray-300 hover:shadow-md',
              )}
            >
              {isSelected && (
                <div className="absolute top-2 right-2 z-10">
                  <div className="bg-primary flex h-8 w-8 items-center justify-center rounded-full text-white shadow-md">
                    <Check className="h-5 w-5" />
                  </div>
                </div>
              )}

              {mug.special && (
                <div className="absolute top-2 left-2 z-10">
                  <div className="flex items-center gap-1 rounded-full bg-yellow-100 px-2 py-1 text-xs font-medium text-yellow-800">
                    <Star className="h-3 w-3" />
                    {mug.special}
                  </div>
                </div>
              )}

              <div className="aspect-square bg-gray-100 p-4">
                <div className="flex h-full items-center justify-center">
                  {displayImage ? (
                    <img src={displayImage} alt={mug.name} className="h-full w-full object-contain" />
                  ) : (
                    <div className="h-32 w-32 rounded-lg bg-gray-300" />
                  )}
                </div>
              </div>

              <div className="p-4">
                <h4 className="mb-1 font-medium">{mug.name}</h4>
                <p className="mb-2 text-sm text-gray-600">
                  {mug.filling_quantity || mug.capacity}
                  {mug.height_mm && mug.diameter_mm && (
                    <span>
                      {' '}
                      · {mug.height_mm}×{mug.diameter_mm}mm
                    </span>
                  )}
                </p>
                {mug.description_short && <p className="mb-2 text-xs text-gray-500">{mug.description_short}</p>}
                <div className="mb-2 flex items-center gap-2 text-xs text-gray-500">
                  {mug.dishwasher_safe !== undefined && (
                    <span
                      className={cn(
                        'inline-flex items-center rounded-full px-2 py-0.5',
                        mug.dishwasher_safe ? 'bg-green-50 text-green-700' : 'bg-gray-100 text-gray-600',
                      )}
                    >
                      {mug.dishwasher_safe ? '✓ Dishwasher Safe' : 'Hand Wash Only'}
                    </span>
                  )}
                </div>

                {/* Variant color swatches - show for all mugs with variants */}
                {mug.variants && mug.variants.length >= 1 && (
                  <div className="mb-3">
                    <p className="mb-1.5 text-xs font-medium text-gray-700">Color Options:</p>
                    <div className="flex flex-wrap gap-2">
                      {mug.variants.map((variant: MugVariant) => {
                        const isVariantSelected = isSelected && selectedVariant?.id === variant.id;
                        const showAsDefault = !isSelected && variant.isDefault;
                        return (
                          <button
                            key={variant.id}
                            onClick={(e) => {
                              e.stopPropagation();
                              // If clicking on a variant of a non-selected mug, select the mug first
                              if (!isSelected) {
                                selectMug(mug);
                              }
                              selectVariant(variant);
                            }}
                            className={cn(
                              'relative h-7 w-7 rounded-full border-2 transition-all',
                              isVariantSelected || showAsDefault
                                ? 'border-primary scale-110 shadow-md'
                                : 'border-gray-300 hover:scale-105 hover:border-gray-400',
                            )}
                            style={{
                              backgroundColor: variant.colorCode.startsWith('#') ? variant.colorCode : `#${variant.colorCode}`,
                            }}
                            aria-label={`Select ${variant.name} color`}
                            title={variant.name}
                          >
                            {(isVariantSelected || showAsDefault) && (
                              <div className="absolute inset-0 flex items-center justify-center">
                                <Check className="h-3 w-3 text-white drop-shadow-md" />
                              </div>
                            )}
                          </button>
                        );
                      })}
                    </div>
                  </div>
                )}

                <p className="text-primary text-lg font-semibold">${mug.price.toFixed(2)}</p>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}
