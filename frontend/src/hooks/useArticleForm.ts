import { useCreateArticle, useUpdateArticle } from '@/hooks/queries/useArticles';
import { useArticleFormStore } from '@/stores/admin/articles/useArticleFormStore';
import { useCostCalculationStore } from '@/stores/admin/articles/useCostCalculationStore';
import { useVariantStore } from '@/stores/admin/articles/useVariantStore';
import type { CreateArticleRequest, UpdateArticleRequest } from '@/types/article';
import { useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { toast } from 'sonner';

export function useArticleForm() {
  const navigate = useNavigate();
  const createArticleMutation = useCreateArticle();
  const updateArticleMutation = useUpdateArticle();

  // Store states
  const { article, isEdit } = useArticleFormStore();
  const { temporaryMugVariants, temporaryShirtVariants } = useVariantStore();
  const { costCalculation } = useCostCalculationStore();

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

        toast.success('Article updated successfully');
        navigate('/admin/articles');
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

        toast.success('Article created successfully');
        navigate('/admin/articles');
      }
    } catch (error) {
      console.error('Error saving article:', error);
      if (error instanceof Error) {
        toast.error(error.message);
      } else {
        toast.error('Failed to save article');
      }
    }
  }, [
    article,
    costCalculation,
    temporaryMugVariants,
    temporaryShirtVariants,
    isEdit,
    validateForm,
    navigate,
    createArticleMutation,
    updateArticleMutation,
  ]);

  return {
    saveArticle,
    isSaving: createArticleMutation.isPending || updateArticleMutation.isPending,
  };
}
