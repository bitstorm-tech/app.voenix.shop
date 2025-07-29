import { MugOption } from '@/components/editor/types';
import { usePublicMugs } from '@/hooks/queries/usePublicMugs';
import type { Mug } from '@/types/mug';
import { useMemo } from 'react';

// Helper function to convert backend Mug to frontend MugOption
const mapMugToOption = (mug: Mug): MugOption => {
  // Find the default variant to use its image if available
  const defaultVariant = mug.variants?.find((v) => v.isDefault) || mug.variants?.[0];
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
    variants: mug.variants?.map((v) => ({
      id: v.id,
      mugId: v.mugId,
      colorCode: v.colorCode,
      exampleImageUrl: v.exampleImageUrl,
      supplierArticleNumber: v.supplierArticleNumber ?? null,
      isDefault: v.isDefault,
      exampleImageFilename: v.exampleImageFilename ?? null,
    })),
  };
};

export function useMugs() {
  const { data, isLoading, error } = usePublicMugs();

  const mugs = useMemo(() => {
    if (data && Array.isArray(data)) {
      return data.map(mapMugToOption);
    }
    // Return empty array if there's an error or no data
    return [];
  }, [data]);

  return { mugs, loading: isLoading, error: error?.message || null };
}
