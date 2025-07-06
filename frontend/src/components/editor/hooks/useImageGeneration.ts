import { apiFetch } from '@/lib/utils';
import { useState } from 'react';

interface UseImageGenerationReturn {
  isGenerating: boolean;
  error: string | null;
  generateImages: (file: File, promptId: number) => Promise<string[] | null>;
}

export function useImageGeneration(): UseImageGenerationReturn {
  const [isGenerating, setIsGenerating] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const generateImages = async (file: File, promptId: number): Promise<string[] | null> => {
    setIsGenerating(true);
    setError(null);

    try {
      const formData = new FormData();
      formData.append('image', file);
      formData.append('prompt_id', promptId.toString());
      formData.append('store_images', 'true');
      formData.append('n', '4');

      const response = await apiFetch('/api/upload-image', {
        method: 'POST',
        body: formData,
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || 'Failed to generate images');
      }

      const data = await response.json();

      if (data.success && data.generated_image_paths) {
        // Return just the filenames, not the full paths
        return data.generated_image_paths.map((path: string) => path.replace('generated/', ''));
      } else if (data.success && data.generated_image_urls) {
        // Fallback to URLs if paths are not available
        return data.generated_image_urls;
      } else {
        throw new Error(data.message || 'Failed to generate images');
      }
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'An unexpected error occurred';
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
