import { publicApi } from '@/lib/api';
import type { Mug } from '@/types/mug';
import { useQuery } from '@tanstack/react-query';

// Query keys
export const publicMugKeys = {
  all: ['publicMugs'] as const,
  list: () => [...publicMugKeys.all, 'list'] as const,
};

// Get all public mugs
export function usePublicMugs() {
  return useQuery({
    queryKey: publicMugKeys.list(),
    queryFn: publicApi.fetchMugs,
    staleTime: 5 * 60 * 1000, // 5 minutes
    cacheTime: 10 * 60 * 1000, // 10 minutes
    retry: 3,
    retryDelay: (attemptIndex) => Math.min(1000 * 2 ** attemptIndex, 30000),
  });
}
