import { clsx, type ClassValue } from 'clsx';
import { twMerge } from 'tailwind-merge';

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

/**
 * Formats an article number according to the pattern:
 * A-{categoryId}-{subcategoryId|1000}-{articleId}-{variantNumber}
 */
export function formatArticleNumber(
  categoryId: number,
  subcategoryId: number | undefined | null,
  articleId: number,
  variantNumber: string | undefined | null,
): string {
  const subcategoryPart = subcategoryId ?? 1000;
  const parts = ['A', categoryId, subcategoryPart, articleId];

  if (variantNumber) {
    parts.push(variantNumber);
  }

  return parts.join('-');
}
