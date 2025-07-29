import { publicApi } from '@/lib/api';
import type { Prompt } from '@/types/prompt';
import { useQuery } from '@tanstack/react-query';

// Query keys
export const publicPromptKeys = {
  all: ['publicPrompts'] as const,
  list: () => [...publicPromptKeys.all, 'list'] as const,
};

// Get all public prompts
export function usePublicPrompts() {
  return useQuery({
    queryKey: publicPromptKeys.list(),
    queryFn: publicApi.fetchPrompts,
    staleTime: 5 * 60 * 1000, // 5 minutes
    cacheTime: 10 * 60 * 1000, // 10 minutes
    retry: 3,
    retryDelay: (attemptIndex) => Math.min(1000 * 2 ** attemptIndex, 30000),
  });
}
