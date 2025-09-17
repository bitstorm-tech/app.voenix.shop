import { ColorSwatch } from '@/components/ui/ColorSwatch';
import type { ArticleShirtVariant } from '@/types/article';
import { useTranslation } from 'react-i18next';

interface ShirtVariantListProps {
  variants: ArticleShirtVariant[];
}

export function ShirtVariantList({ variants }: ShirtVariantListProps) {
  const { t } = useTranslation('adminArticles');

  if (variants.length === 0) {
    return <div className="text-muted-foreground py-4 text-center text-sm">{t('variants.none')}</div>;
  }

  // Group variants by color
  const variantsByColor = variants.reduce(
    (acc, variant) => {
      if (!acc[variant.color]) {
        acc[variant.color] = [];
      }
      acc[variant.color].push(variant);
      return acc;
    },
    {} as Record<string, ArticleShirtVariant[]>,
  );

  return (
    <div className="space-y-3">
      {Object.entries(variantsByColor).map(([color, colorVariants]) => (
        <div key={color} className="rounded-lg border p-3">
          <div className="mb-2 flex items-center gap-3">
            <ColorSwatch color={color} size="sm" />
            <span className="font-medium">{color}</span>
          </div>

          <div className="flex flex-wrap gap-2">
            {colorVariants.map((variant) => (
              <div key={variant.id} className="bg-muted rounded-md px-3 py-1 text-sm">
                {t('variants.size', { size: variant.size })}
              </div>
            ))}
          </div>

          {colorVariants[0]?.exampleImageUrl && (
            <div className="mt-2">
              <img src={colorVariants[0].exampleImageUrl} alt={t('variants.colorAlt', { color })} className="h-16 w-16 rounded-md object-cover" />
            </div>
          )}
        </div>
      ))}
    </div>
  );
}
