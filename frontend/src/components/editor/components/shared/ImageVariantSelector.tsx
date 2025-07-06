import { cn } from '@/lib/utils';
import { Check } from 'lucide-react';

interface ImageVariantSelectorProps {
  variants: string[];
  selectedVariant: string | null;
  onVariantSelect: (variant: string) => void;
}

export default function ImageVariantSelector({ variants, selectedVariant, onVariantSelect }: ImageVariantSelectorProps) {
  return (
    <div className="grid grid-cols-2 gap-4">
      {variants.map((variant, index) => {
        const isSelected = selectedVariant === variant;

        return (
          <div
            key={index}
            onClick={() => onVariantSelect(variant)}
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

            <div className="absolute top-2 left-2 z-10">
              <div className="flex h-6 w-6 items-center justify-center rounded-full bg-black/50 text-xs font-bold text-white">{index + 1}</div>
            </div>

            <img
              src={variant.startsWith('data:') ? variant : `/api/images/${variant}`}
              alt={`Variant ${index + 1}`}
              className="h-full w-full object-cover"
            />
          </div>
        );
      })}
    </div>
  );
}
