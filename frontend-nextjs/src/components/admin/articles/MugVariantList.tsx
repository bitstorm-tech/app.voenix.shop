import { Badge } from '@/components/ui/Badge';
import { ColorSwatch } from '@/components/ui/ColorSwatch';
import { formatArticleNumber } from '@/lib/utils';
import type { Article, ArticleMugVariant } from '@/types/article';

interface MugVariantListProps {
  variants: ArticleMugVariant[];
  article: Article;
}

export function MugVariantList({ variants, article }: MugVariantListProps) {
  if (variants.length === 0) {
    return <div className="text-muted-foreground py-4 text-center text-sm">No variants available</div>;
  }

  return (
    <div className="space-y-3">
      {variants.map((variant) => (
        <div key={variant.id} className="flex flex-col gap-2 rounded-lg border p-3 sm:flex-row sm:items-center sm:justify-between">
          <div className="flex flex-col gap-2 sm:flex-row sm:items-center sm:gap-4">
            <div className="flex items-center gap-2">
              <span className="font-medium">{variant.name}</span>
              {variant.isDefault && (
                <Badge variant="secondary" className="text-xs">
                  Default
                </Badge>
              )}
            </div>

            <div className="flex items-center gap-4">
              <ColorSwatch color={variant.insideColorCode} label="Inside" size="sm" />
              <ColorSwatch color={variant.outsideColorCode} label="Outside" size="sm" />
            </div>
          </div>

          <div className="text-muted-foreground text-sm">
            {formatArticleNumber(article.categoryId, article.subcategoryId, article.id, variant.articleVariantNumber)}
          </div>
        </div>
      ))}
    </div>
  );
}