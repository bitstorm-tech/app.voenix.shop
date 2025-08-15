import { MugOption } from '@/components/editor/types';
import { usePublicMugs } from '@/hooks/queries/usePublicMugs';
import type { Mug } from '@/types/mug';
import { useMemo } from 'react';

// Helper function to convert backend Mug to frontend MugOption
const mapMugToOption = (mug: Mug): MugOption => {
  // Filter out inactive variants for customer view
  const activeVariants = mug.variants?.filter((v) => v.active !== false) || [];
  
  // Find the default variant from active variants, or use the first active variant
  const defaultVariant = activeVariants.find((v) => v.isDefault) || activeVariants[0];
  const variantImage = defaultVariant?.exampleImageUrl || mug.image;

  return {
    id: mug.id,
    name: mug.name,
    price: mug.price,
    image: variantImage,
    capacity: mug.fillingQuantity || '',
    description_short: mug.descriptionShort,
    description_long: mug.descriptionLong,
    height_mm: mug.heightMm,
    diameter_mm: mug.diameterMm,
    print_template_width_mm: mug.printTemplateWidthMm,
    print_template_height_mm: mug.printTemplateHeightMm,
    filling_quantity: mug.fillingQuantity,
    dishwasher_safe: mug.dishwasherSafe,
    variants: activeVariants.map((v) => ({
      id: v.id,
      mugId: v.mugId,
      colorCode: v.colorCode,
      exampleImageUrl: v.exampleImageUrl,
      supplierArticleNumber: v.supplierArticleNumber ?? null,
      isDefault: v.isDefault,
      active: v.active,
      exampleImageFilename: v.exampleImageFilename ?? null,
    })),
  };
};

export function useMugs() {
  const { data, isLoading, error } = usePublicMugs();

  const mugs = useMemo(() => {
    if (data && Array.isArray(data)) {
      return data
        .map(mapMugToOption)
        .filter((mug) => mug.variants && mug.variants.length > 0);
    }
    // Return empty array if there's an error or no data
    return [];
  }, [data]);

  return { mugs, loading: isLoading, error: error?.message || null };
}
