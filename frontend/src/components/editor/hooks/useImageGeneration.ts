import { CropData } from '@/components/editor/types';
import { publicApi, userApi } from '@/lib/api';
import { useWizardStore } from '@/stores/editor/useWizardStore';
import { useState } from 'react';
import { useTranslation } from 'react-i18next';

type Provider = 'OPENAI' | 'GOOGLE' | 'FLUX';

interface GeneratedImageData {
  urls: string[];
  ids: number[];
}

interface UseImageGenerationReturn {
  isGenerating: boolean;
  error: string | null;
  generateImages: (file: File, promptId: number, cropData?: CropData, provider?: Provider) => Promise<GeneratedImageData | null>;
}

export function useImageGeneration(): UseImageGenerationReturn {
  const [isGenerating, setIsGenerating] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const isAuthenticated = useWizardStore((state) => state.isAuthenticated);
  const { t } = useTranslation('editor');

  const generateImages = async (
    file: File,
    promptId: number,
    cropData?: CropData,
    provider: Provider = 'OPENAI',
  ): Promise<GeneratedImageData | null> => {
    setIsGenerating(true);
    setError(null);

    try {
      // Use authenticated endpoint if user is logged in
      const response = isAuthenticated
        ? await userApi.generateImage(file, promptId, cropData, provider)
        : await publicApi.generateImage(file, promptId, cropData, provider);

      // Return both URLs and IDs
      return {
        urls: response.imageUrls,
        ids: response.generatedImageIds,
      };
    } catch (err: unknown) {
      let errorMessage = t('errors.unexpected');

      // Type guard for error objects with status property
      const isErrorWithStatus = (error: unknown): error is { status: number } => {
        return typeof error === 'object' && error !== null && 'status' in error;
      };

      // Handle rate limiting error specifically
      if (isErrorWithStatus(err) && err.status === 429) {
        errorMessage = isAuthenticated ? t('errors.rateLimitAuthenticated') : t('errors.rateLimitGuest');
      } else if (isErrorWithStatus(err) && err.status === 401) {
        errorMessage = t('errors.sessionExpired');
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
