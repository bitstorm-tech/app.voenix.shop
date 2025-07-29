import { MugOption } from '@/components/editor/types';
import { usePublicMugs } from '@/hooks/queries/usePublicMugs';
import type { Mug } from '@/types/mug';
import { useMemo } from 'react';

const mockMugs: MugOption[] = [
  {
    id: 1,
    name: 'Classic White Mug',
    price: 14.99,
    image: '/images/mugs/white-mug.jpg',
    capacity: '11oz',
    description_short: 'Our bestselling ceramic mug',
    description_long: 'High-quality ceramic mug with a glossy finish. Perfect for your morning coffee.',
    height_mm: 95,
    diameter_mm: 80,
    print_template_width_mm: 200,
    print_template_height_mm: 85,
    filling_quantity: '330ml',
    dishwasher_safe: true,
  },
  {
    id: 2,
    name: 'Large Black Mug',
    price: 16.99,
    image: '/images/mugs/black-mug.jpg',
    capacity: '15oz',
    special: 'Popular',
    description_short: 'Extra large for serious coffee lovers',
    description_long: 'Premium black ceramic mug with extra capacity. Dishwasher and microwave safe.',
    height_mm: 110,
    diameter_mm: 85,
    print_template_width_mm: 220,
    print_template_height_mm: 95,
    filling_quantity: '450ml',
    dishwasher_safe: true,
  },
  {
    id: 3,
    name: 'Travel Mug',
    price: 24.99,
    image: '/images/mugs/travel-mug.jpg',
    capacity: '16oz',
    special: 'Insulated',
    description_short: 'Stainless steel with lid',
    description_long: 'Double-wall insulated stainless steel travel mug. Keeps drinks hot for hours.',
    height_mm: 160,
    diameter_mm: 75,
    print_template_width_mm: 180,
    print_template_height_mm: 120,
    filling_quantity: '470ml',
    dishwasher_safe: false,
  },
];

// Helper function to convert backend Mug to frontend MugOption
const mapMugToOption = (mug: Mug): MugOption => ({
  id: mug.id,
  name: mug.name,
  price: mug.price,
  image: mug.image,
  capacity: mug.fillingQuantity || '',
  description_short: mug.descriptionShort,
  description_long: mug.descriptionLong,
  height_mm: mug.heightMm,
  diameter_mm: mug.diameterMm,
  print_template_width_mm: mug.printTemplateWidthMm,
  print_template_height_mm: mug.printTemplateHeightMm,
  filling_quantity: mug.fillingQuantity,
  dishwasher_safe: mug.dishwasherSafe,
});

export function useMugs() {
  const { data, isLoading, error } = usePublicMugs();

  const mugs = useMemo(() => {
    if (data) {
      return data.map(mapMugToOption);
    }
    // Use mock data as fallback if there's an error
    if (error) {
      console.error('Error fetching mugs, using mock data:', error);
      return mockMugs;
    }
    return [];
  }, [data, error]);

  return { mugs, loading: isLoading, error: error?.message || null };
}
