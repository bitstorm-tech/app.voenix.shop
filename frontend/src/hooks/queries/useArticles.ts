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

export function useArticles(params?: UseArticlesParams) {
  return useQuery({
    queryKey: articleKeys.list(params),
    queryFn: () => articlesApi.getAll(params),
  });
}

export function useArticle(id: number | undefined) {
  return useQuery({
    queryKey: articleKeys.detail(id!),
    queryFn: () => articlesApi.getById(id!),
    enabled: !!id,
  });
}

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

export function useUpdateArticle() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: UpdateArticleRequest }) => articlesApi.update(id, data),
    onSuccess: (updatedArticle, { id }) => {
      queryClient.setQueryData(articleKeys.detail(id), updatedArticle);

      // Invalidate lists
      queryClient.invalidateQueries({ queryKey: articleKeys.lists() });

      toast.success('Article updated successfully');
    },
  });
}

export function useDeleteArticle() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (id: number) => articlesApi.delete(id),
    onSuccess: (_, id) => {
      queryClient.removeQueries({ queryKey: articleKeys.detail(id) });

      // Invalidate lists
      queryClient.invalidateQueries({ queryKey: articleKeys.lists() });

      toast.success('Article deleted successfully');
    },
  });
}
