import { QueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';

// Type for error objects
interface ApiError {
  status?: number;
  message?: string;
}

export const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 5 * 60 * 1000, // 5 minutes
      gcTime: 10 * 60 * 1000, // 10 minutes
      retry: (failureCount, error: ApiError) => {
        // Don't retry on 4xx errors
        if (error?.status && error.status >= 400 && error.status < 500) {
          return false;
        }
        return failureCount < 2;
      },
      refetchOnWindowFocus: false,
    },
    mutations: {
      retry: 1,
      onError: (error: ApiError) => {
        // Global error handling for mutations
        const message = error?.message || 'An error occurred';
        toast.error(message);
      },
    },
  },
});

// Global error handler for queries
queryClient.getQueryCache().config.onError = (error: ApiError) => {
  // Don't show toast for authentication errors (handled by API)
  if (error?.status === 401 || error?.status === 403) {
    return;
  }

  const message = error?.message || 'Failed to fetch data';
  toast.error(message);
};
