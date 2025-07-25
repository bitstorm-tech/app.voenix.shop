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
      formData.append('promptId', promptId.toString());
      formData.append('n', '1');

      const response = await fetch('/api/admin/openai/image-edit', {
        method: 'POST',
        body: formData,
        credentials: 'include',
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || 'Failed to generate images');
      }

      const data = await response.json();

      if (data.imageFilenames && data.imageFilenames.length > 0) {
        return data.imageFilenames;
      } else {
        throw new Error('Failed to generate images');
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
