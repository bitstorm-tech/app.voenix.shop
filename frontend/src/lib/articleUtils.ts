import type { Article } from '@/types/article';

/**
 * Gets the article image from the default variant or first variant if no default is set
 */
export function getArticleImage(article: Article): string | null {
  if (article.articleType === 'MUG' && article.mugVariants) {
    // Find default variant first
    const defaultVariant = article.mugVariants.find((variant) => variant.isDefault);
    if (defaultVariant?.exampleImageUrl) {
      return defaultVariant.exampleImageUrl;
    }

    // Fall back to first variant if no default
    const firstVariant = article.mugVariants[0];
    return firstVariant?.exampleImageUrl || null;
  }

  if (article.articleType === 'SHIRT' && article.shirtVariants) {
    // Shirt variants don't have isDefault, so use first variant
    const firstVariant = article.shirtVariants[0];
    return firstVariant?.exampleImageUrl || null;
  }

  return null;
}
