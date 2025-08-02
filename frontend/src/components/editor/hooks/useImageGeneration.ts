import { publicApi, userApi } from '@/lib/api';
import { useWizardStore } from '@/stores/editor/useWizardStore';
import { useState } from 'react';

interface UseImageGenerationReturn {
  isGenerating: boolean;
  error: string | null;
  generateImages: (file: File, promptId: number) => Promise<string[] | null>;
}

export function useImageGeneration(): UseImageGenerationReturn {
  const [isGenerating, setIsGenerating] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const isAuthenticated = useWizardStore((state) => state.isAuthenticated);

  const generateImages = async (file: File, promptId: number): Promise<string[] | null> => {
    setIsGenerating(true);
    setError(null);

    try {
      // Use authenticated endpoint if user is logged in
      const response = isAuthenticated ? await userApi.generateImage(file, promptId) : await publicApi.generateImage(file, promptId);

      // Return the image URLs
      return response.imageUrls;
    } catch (err: unknown) {
      let errorMessage = 'An unexpected error occurred';

      // Type guard for error objects with status property
      const isErrorWithStatus = (error: unknown): error is { status: number } => {
        return typeof error === 'object' && error !== null && 'status' in error;
      };

      // Handle rate limiting error specifically
      if (isErrorWithStatus(err) && err.status === 429) {
        errorMessage = isAuthenticated
          ? "You've reached your image generation limit. Please try again later."
          : "You've reached the limit for image generation. Please try again in an hour.";
      } else if (isErrorWithStatus(err) && err.status === 401) {
        errorMessage = 'Your session has expired. Please refresh the page and try again.';
      } else if (err instanceof Error) {
        errorMessage = err.message;
      }

      setError(errorMessage);
      return null;
    } finally {
      setIsGenerating(false);
    }
  };

  return {
    isGenerating,
    error,
    generateImages,
  };
}
