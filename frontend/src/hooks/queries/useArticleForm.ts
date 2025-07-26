import { articlesApi } from '@/lib/api';
import { useArticleFormStore } from '@/stores/admin/articles/useArticleFormStore';
import type { CreateArticleRequest, UpdateArticleRequest } from '@/types/article';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useCallback, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { toast } from 'sonner';
import { articleKeys } from './useArticles';
import { useArticleCategories, useArticleSubCategoriesByCategory } from './useCategories';

/**
 * Hook that combines React Query with the article form store
 * for complete article form management
 */
export function useArticleFormQueries(articleId?: number) {
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  // Store actions
  const { article, costCalculation, temporaryMugVariants, temporaryShirtVariants, isEdit, initializeForm } = useArticleFormStore();

  // Fetch article data if editing
  const { data: articleData, isLoading: isLoadingArticle } = useQuery({
    queryKey: articleKeys.detail(articleId!),
    queryFn: () => articlesApi.getById(articleId!),
    enabled: !!articleId,
  });

  // Initialize form when article data is fetched
  useEffect(() => {
    if (articleData && articleId) {
      initializeForm(articleId, articleData);
    }
  }, [articleData, articleId, initializeForm]);

  // Fetch categories
  const { data: categories = [], isLoading: isLoadingCategories } = useArticleCategories();

  // Fetch subcategories based on selected category
  const { data: subcategories = [] } = useArticleSubCategoriesByCategory(article.categoryId);

  // Create article mutation
  const createArticleMutation = useMutation({
    mutationFn: (data: CreateArticleRequest) => articlesApi.create(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: articleKeys.lists() });
      toast.success('Article created successfully');
      navigate('/admin/articles');
    },
  });

  // Update article mutation
  const updateArticleMutation = useMutation({
    mutationFn: ({ id, data }: { id: number; data: UpdateArticleRequest }) => articlesApi.update(id, data),
    onSuccess: (updatedArticle, { id }) => {
      queryClient.setQueryData(articleKeys.detail(id), updatedArticle);
      queryClient.invalidateQueries({ queryKey: articleKeys.lists() });
      toast.success('Article updated successfully');
      navigate('/admin/articles');
    },
  });

  // Validation
  const validateForm = useCallback(() => {
    if (!article.name || !article.categoryId || !article.articleType) {
      toast.error('Please fill in all required fields');
      return false;
    }

    // Validate type-specific details
    switch (article.articleType) {
      case 'MUG':
        if (!article.mugDetails) {
          toast.error('Please fill in mug specifications');
          return false;
        }
        break;
      case 'SHIRT':
        if (!article.shirtDetails) {
          toast.error('Please fill in shirt details');
          return false;
        }
        break;
    }

    return true;
  }, [article]);

  // Save article
  const saveArticle = useCallback(async () => {
    if (!validateForm()) {
      return;
    }

    try {
      if (isEdit && article.id) {
        const updateData: UpdateArticleRequest = {
          name: article.name || '',
          descriptionShort: article.descriptionShort || '',
          descriptionLong: article.descriptionLong || '',
          active: article.active || false,
          categoryId: article.categoryId || 0,
          subcategoryId: article.subcategoryId,
          supplierId: article.supplierId,
          mugDetails: article.mugDetails as any,
          shirtDetails: article.shirtDetails as any,
          costCalculation: costCalculation,
        };

        await updateArticleMutation.mutateAsync({
          id: article.id,
          data: updateData,
        });
      } else {
        const createData: CreateArticleRequest = {
          name: article.name || '',
          descriptionShort: article.descriptionShort || '',
          descriptionLong: article.descriptionLong || '',
          active: article.active || false,
          articleType: article.articleType!,
          categoryId: article.categoryId || 0,
          subcategoryId: article.subcategoryId,
          supplierId: article.supplierId,
          mugVariants: article.articleType === 'MUG' ? temporaryMugVariants : undefined,
          shirtVariants: article.articleType === 'SHIRT' ? temporaryShirtVariants : undefined,
          mugDetails: article.mugDetails as any,
          shirtDetails: article.shirtDetails as any,
          costCalculation: costCalculation,
        };

        await createArticleMutation.mutateAsync(createData);
      }
    } catch (error) {
      // Error handling is done by React Query's global error handler
      console.error('Error saving article:', error);
    }
  }, [article, costCalculation, temporaryMugVariants, temporaryShirtVariants, isEdit, validateForm, createArticleMutation, updateArticleMutation]);

  return {
    // Data
    articleData,
    categories,
    subcategories,

    // Loading states
    isLoading: isLoadingArticle || isLoadingCategories,
    isSaving: createArticleMutation.isPending || updateArticleMutation.isPending,

    // Actions
    saveArticle,

    // Mutations (for direct access if needed)
    createArticleMutation,
    updateArticleMutation,
  };
}
