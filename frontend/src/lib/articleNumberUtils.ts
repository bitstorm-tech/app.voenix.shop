/**
 * Generates an article number based on the provided IDs
 * Format: A-{article_category_id}-{article_subcategory_id|1000}-{article_id}
 */
export function generateArticleNumber(
  categoryId: number | null | undefined,
  subcategoryId: number | null | undefined,
  articleId: number | null | undefined,
): string | null {
  // Return null if we don't have all required IDs
  if (!categoryId || !articleId) {
    return null;
  }

  // Use 1000 as default for missing subcategory
  const subcategoryPart = subcategoryId || 1000;

  return `A-${categoryId}-${subcategoryPart}-${articleId}`;
}

/**
 * Generates a prompt number based on the provided IDs
 * Format: P-{prompt_category_id}-{prompt_subcategory_id|1000}-{prompt_id}
 */
export function generatePromptNumber(
  categoryId: number | null | undefined,
  subcategoryId: number | null | undefined,
  promptId: number | null | undefined,
): string | null {
  // Return null if we don't have the required prompt ID
  if (!promptId) {
    return null;
  }

  // Use 1000 as default for missing category/subcategory
  const categoryPart = categoryId || 1000;
  const subcategoryPart = subcategoryId || 1000;

  return `P-${categoryPart}-${subcategoryPart}-${promptId}`;
}

/**
 * Gets a display text for article/prompt number fields when creating new items
 */
export function getArticleNumberPlaceholder(): string {
  return 'Article number will be generated after saving';
}
