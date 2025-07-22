import { articlesApi, type CreateArticleRequest, type UpdateArticleRequest } from '@/lib/api';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';

// Query keys
export const articleKeys = {
  all: ['articles'] as const,
  lists: () => [...articleKeys.all, 'list'] as const,
  list: (params?: any) => [...articleKeys.lists(), params] as const,
  details: () => [...articleKeys.all, 'detail'] as const,
  detail: (id: number) => [...articleKeys.details(), id] as const,
};

interface UseArticlesParams {
  page?: number;
  size?: number;
  type?: string;
  categoryId?: number;
  subcategoryId?: number;
  active?: boolean;
  search?: string;
}

// Get paginated articles
export function useArticles(params?: UseArticlesParams) {
  return useQuery({
    queryKey: articleKeys.list(params),
    queryFn: () => articlesApi.getAll(params),
  });
}

// Get article by ID
export function useArticle(id: number | undefined) {
  return useQuery({
    queryKey: articleKeys.detail(id!),
    queryFn: () => articlesApi.getById(id!),
    enabled: !!id,
  });
}

// Create article mutation
export function useCreateArticle() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (data: CreateArticleRequest) => articlesApi.create(data),
    onSuccess: () => {
      // Invalidate all article lists (different filters might be affected)
      queryClient.invalidateQueries({ queryKey: articleKeys.lists() });
      toast.success('Article created successfully');
    },
  });
}

// Update article mutation
export function useUpdateArticle() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: UpdateArticleRequest }) => articlesApi.update(id, data),
    onSuccess: (updatedArticle, { id }) => {
      // Update the specific article in cache
      queryClient.setQueryData(articleKeys.detail(id), updatedArticle);

      // Invalidate lists
      queryClient.invalidateQueries({ queryKey: articleKeys.lists() });

      toast.success('Article updated successfully');
    },
  });
}

// Delete article mutation
export function useDeleteArticle() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (id: number) => articlesApi.delete(id),
    onSuccess: (_, id) => {
      // Remove from cache
      queryClient.removeQueries({ queryKey: articleKeys.detail(id) });

      // Invalidate lists
      queryClient.invalidateQueries({ queryKey: articleKeys.lists() });

      toast.success('Article deleted successfully');
    },
  });
}

// Create article variant mutation
export function useCreateArticleVariant() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ articleId, data }: { articleId: number; data: any }) => articlesApi.createVariant(articleId, data),
    onSuccess: (_, { articleId }) => {
      // Invalidate the article detail to refetch with new variant
      queryClient.invalidateQueries({ queryKey: articleKeys.detail(articleId) });

      toast.success('Variant created successfully');
    },
  });
}

// Update article variant mutation
export function useUpdateArticleVariant() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ variantId, data }: { variantId: number; data: any }) => articlesApi.updateVariant(variantId, data),
    onSuccess: () => {
      // Invalidate all article details (we don't know which article this variant belongs to)
      queryClient.invalidateQueries({ queryKey: articleKeys.details() });

      toast.success('Variant updated successfully');
    },
  });
}

// Delete article variant mutation
export function useDeleteArticleVariant() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (variantId: number) => articlesApi.deleteVariant(variantId),
    onSuccess: () => {
      // Invalidate all article details
      queryClient.invalidateQueries({ queryKey: articleKeys.details() });

      toast.success('Variant deleted successfully');
    },
  });
}
