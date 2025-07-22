import {
  articleCategoriesApi,
  articleSubCategoriesApi,
  promptCategoriesApi,
  promptSubCategoriesApi,
  type CreateArticleCategoryRequest,
  type CreateArticleSubCategoryRequest,
  type CreatePromptCategoryRequest,
  type CreatePromptSubCategoryRequest,
  type UpdateArticleCategoryRequest,
  type UpdateArticleSubCategoryRequest,
  type UpdatePromptCategoryRequest,
  type UpdatePromptSubCategoryRequest,
} from '@/lib/api';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';

// Query keys for prompt categories
export const promptCategoryKeys = {
  all: ['promptCategories'] as const,
  lists: () => [...promptCategoryKeys.all, 'list'] as const,
  details: () => [...promptCategoryKeys.all, 'detail'] as const,
  detail: (id: number) => [...promptCategoryKeys.details(), id] as const,
};

// Query keys for prompt subcategories
export const promptSubCategoryKeys = {
  all: ['promptSubCategories'] as const,
  lists: () => [...promptSubCategoryKeys.all, 'list'] as const,
  byCategory: (categoryId: number) => [...promptSubCategoryKeys.all, 'byCategory', categoryId] as const,
  details: () => [...promptSubCategoryKeys.all, 'detail'] as const,
  detail: (id: number) => [...promptSubCategoryKeys.details(), id] as const,
};

// Query keys for article categories
export const articleCategoryKeys = {
  all: ['articleCategories'] as const,
  lists: () => [...articleCategoryKeys.all, 'list'] as const,
  details: () => [...articleCategoryKeys.all, 'detail'] as const,
  detail: (id: number) => [...articleCategoryKeys.details(), id] as const,
};

// Query keys for article subcategories
export const articleSubCategoryKeys = {
  all: ['articleSubCategories'] as const,
  lists: () => [...articleSubCategoryKeys.all, 'list'] as const,
  byCategory: (categoryId: number) => [...articleSubCategoryKeys.all, 'byCategory', categoryId] as const,
  details: () => [...articleSubCategoryKeys.all, 'detail'] as const,
  detail: (id: number) => [...articleSubCategoryKeys.details(), id] as const,
};

// Prompt Categories
export function usePromptCategories() {
  return useQuery({
    queryKey: promptCategoryKeys.lists(),
    queryFn: promptCategoriesApi.getAll,
  });
}

export function usePromptCategory(id: number | undefined) {
  return useQuery({
    queryKey: promptCategoryKeys.detail(id!),
    queryFn: () => promptCategoriesApi.getById(id!),
    enabled: !!id,
  });
}

export function useCreatePromptCategory() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (data: CreatePromptCategoryRequest) => promptCategoriesApi.create(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: promptCategoryKeys.lists() });
      toast.success('Category created successfully');
    },
  });
}

export function useUpdatePromptCategory() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: UpdatePromptCategoryRequest }) => promptCategoriesApi.update(id, data),
    onSuccess: (_, { id }) => {
      queryClient.invalidateQueries({ queryKey: promptCategoryKeys.lists() });
      queryClient.invalidateQueries({ queryKey: promptCategoryKeys.detail(id) });
      toast.success('Category updated successfully');
    },
  });
}

export function useDeletePromptCategory() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (id: number) => promptCategoriesApi.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: promptCategoryKeys.lists() });
      queryClient.invalidateQueries({ queryKey: promptSubCategoryKeys.all });
      toast.success('Category deleted successfully');
    },
  });
}

// Prompt SubCategories
export function usePromptSubCategories() {
  return useQuery({
    queryKey: promptSubCategoryKeys.lists(),
    queryFn: promptSubCategoriesApi.getAll,
  });
}

export function usePromptSubCategoriesByCategory(categoryId: number | undefined) {
  return useQuery({
    queryKey: promptSubCategoryKeys.byCategory(categoryId!),
    queryFn: () => promptSubCategoriesApi.getByCategory(categoryId!),
    enabled: !!categoryId,
  });
}

export function usePromptSubCategory(id: number | undefined) {
  return useQuery({
    queryKey: promptSubCategoryKeys.detail(id!),
    queryFn: () => promptSubCategoriesApi.getById(id!),
    enabled: !!id,
  });
}

export function useCreatePromptSubCategory() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (data: CreatePromptSubCategoryRequest) => promptSubCategoriesApi.create(data),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: promptSubCategoryKeys.lists() });
      queryClient.invalidateQueries({
        queryKey: promptSubCategoryKeys.byCategory(variables.promptCategoryId),
      });
      toast.success('Subcategory created successfully');
    },
  });
}

export function useUpdatePromptSubCategory() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: UpdatePromptSubCategoryRequest }) => promptSubCategoriesApi.update(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: promptSubCategoryKeys.all });
      toast.success('Subcategory updated successfully');
    },
  });
}

export function useDeletePromptSubCategory() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (id: number) => promptSubCategoriesApi.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: promptSubCategoryKeys.all });
      toast.success('Subcategory deleted successfully');
    },
  });
}

// Article Categories
export function useArticleCategories() {
  return useQuery({
    queryKey: articleCategoryKeys.lists(),
    queryFn: articleCategoriesApi.getAll,
  });
}

export function useArticleCategory(id: number | undefined) {
  return useQuery({
    queryKey: articleCategoryKeys.detail(id!),
    queryFn: () => articleCategoriesApi.getById(id!),
    enabled: !!id,
  });
}

export function useCreateArticleCategory() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (data: CreateArticleCategoryRequest) => articleCategoriesApi.create(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: articleCategoryKeys.lists() });
      toast.success('Category created successfully');
    },
  });
}

export function useUpdateArticleCategory() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: UpdateArticleCategoryRequest }) => articleCategoriesApi.update(id, data),
    onSuccess: (_, { id }) => {
      queryClient.invalidateQueries({ queryKey: articleCategoryKeys.lists() });
      queryClient.invalidateQueries({ queryKey: articleCategoryKeys.detail(id) });
      toast.success('Category updated successfully');
    },
  });
}

export function useDeleteArticleCategory() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (id: number) => articleCategoriesApi.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: articleCategoryKeys.lists() });
      queryClient.invalidateQueries({ queryKey: articleSubCategoryKeys.all });
      toast.success('Category deleted successfully');
    },
  });
}

// Article SubCategories
export function useArticleSubCategories() {
  return useQuery({
    queryKey: articleSubCategoryKeys.lists(),
    queryFn: articleSubCategoriesApi.getAll,
  });
}

export function useArticleSubCategoriesByCategory(categoryId: number | undefined) {
  return useQuery({
    queryKey: articleSubCategoryKeys.byCategory(categoryId!),
    queryFn: () => articleSubCategoriesApi.getByCategoryId(categoryId!),
    enabled: !!categoryId,
  });
}

export function useArticleSubCategory(id: number | undefined) {
  return useQuery({
    queryKey: articleSubCategoryKeys.detail(id!),
    queryFn: () => articleSubCategoriesApi.getById(id!),
    enabled: !!id,
  });
}

export function useCreateArticleSubCategory() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (data: CreateArticleSubCategoryRequest) => articleSubCategoriesApi.create(data),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: articleSubCategoryKeys.lists() });
      queryClient.invalidateQueries({
        queryKey: articleSubCategoryKeys.byCategory(variables.articleCategoryId),
      });
      toast.success('Subcategory created successfully');
    },
  });
}

export function useUpdateArticleSubCategory() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: UpdateArticleSubCategoryRequest }) => articleSubCategoriesApi.update(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: articleSubCategoryKeys.all });
      toast.success('Subcategory updated successfully');
    },
  });
}

export function useDeleteArticleSubCategory() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (id: number) => articleSubCategoriesApi.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: articleSubCategoryKeys.all });
      toast.success('Subcategory deleted successfully');
    },
  });
}
