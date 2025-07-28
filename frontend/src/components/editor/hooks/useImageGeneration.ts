import { publicApi } from '@/lib/api';
import { useState } from 'react';

interface UseImageGenerationReturn {
  isGenerating: boolean;
  error: string | null;
  sessionToken: string | null;
  generateImages: (file: File, promptId: number) => Promise<string[] | null>;
}

export function useImageGeneration(): UseImageGenerationReturn {
  const [isGenerating, setIsGenerating] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [sessionToken, setSessionToken] = useState<string | null>(null);

  const generateImages = async (file: File, promptId: number): Promise<string[] | null> => {
    setIsGenerating(true);
    setError(null);

    try {
      const response = await publicApi.generateImage(file, promptId);

      // Store the session token for potential future use
      setSessionToken(response.sessionToken);

      // Return the image URLs
      return response.imageUrls;
    } catch (err: any) {
      let errorMessage = 'An unexpected error occurred';

      // Handle rate limiting error specifically
      if (err.status === 429) {
        errorMessage = "You've reached the limit for image generation. Please try again in an hour.";
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
    sessionToken,
    generateImages,
  };
}
