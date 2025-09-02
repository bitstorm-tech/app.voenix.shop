import { userImagesApi } from '@/api/userImagesApi';
import type { UserImagesParams } from '@/types/userImage';
import { useQuery } from '@tanstack/react-query';
import { useMemo } from 'react';

// Query keys
export const userImagesKeys = {
  all: ['userImages'] as const,
  lists: () => [...userImagesKeys.all, 'list'] as const,
  list: (params?: UserImagesParams) => [...userImagesKeys.lists(), { params }] as const,
};

export const promptKeys = {
  all: ['prompts'] as const,
  batch: (ids: number[]) => [...promptKeys.all, 'batch', ids.sort()] as const,
};

// Hook to get user's images with pagination and filtering
export function useUserImages(params?: UserImagesParams) {
  // First, fetch the images
  const {
    data: imagesData,
    error: imagesError,
    isLoading: imagesLoading,
    ...restImageQuery
  } = useQuery({
    queryKey: userImagesKeys.list(params),
    queryFn: () => userImagesApi.getUserImages(params),
    staleTime: 5 * 60 * 1000, // 5 minutes
    gcTime: 10 * 60 * 1000, // 10 minutes cache
  });

  // Extract unique prompt IDs from images
  const promptIds = useMemo(() => {
    if (!imagesData?.content) return [];
    const ids = imagesData.content.map((image) => image.promptId).filter((id): id is number => id != null);
    return [...new Set(ids)];
  }, [imagesData?.content]);

  // Fetch prompt titles for the found prompt IDs
  const {
    data: prompts,
    error: promptsError,
    isLoading: promptsLoading,
  } = useQuery({
    queryKey: promptKeys.batch(promptIds),
    queryFn: () => userImagesApi.getPromptsByIds(promptIds),
    enabled: promptIds.length > 0,
    staleTime: 10 * 60 * 1000, // 10 minutes - prompt titles don't change often
    gcTime: 30 * 60 * 1000, // 30 minutes cache
  });

  // Combine images with prompt titles
  const enrichedData = useMemo(() => {
    if (!imagesData) return undefined;

    const promptsMap =
      prompts?.reduce(
        (acc, prompt) => {
          acc[prompt.id] = prompt;
          return acc;
        },
        {} as Record<number, (typeof prompts)[0]>,
      ) ?? {};

    return {
      ...imagesData,
      content: imagesData.content.map((image) => ({
        ...image,
        promptTitle: image.promptId ? (promptsMap[image.promptId]?.title ?? null) : null,
      })),
    };
  }, [imagesData, prompts]);

  return {
    data: enrichedData,
    error: imagesError || promptsError,
    isLoading: imagesLoading || (promptIds.length > 0 && promptsLoading),
    ...restImageQuery,
  };
}
