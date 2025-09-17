import { Alert, AlertDescription } from '@/components/ui/Alert';
import { useWizardStore } from '@/stores/editor/useWizardStore';
import { Loader2, Sparkles } from 'lucide-react';
import { useEffect, useMemo, useRef, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useImageGeneration } from '../../hooks/useImageGeneration';
import ImageVariantSelector from '../shared/ImageVariantSelector';

export default function ImageGenerationStep() {
  const { t } = useTranslation('editor');
  const uploadedImage = useWizardStore((state) => state.uploadedImage);
  const selectedPrompt = useWizardStore((state) => state.selectedPrompt);
  const generatedImages = useWizardStore((state) => state.generatedImages);
  const selectedGeneratedImage = useWizardStore((state) => state.selectedGeneratedImage);
  const cropData = useWizardStore((state) => state.cropData);
  const setProcessing = useWizardStore((state) => state.setProcessing);
  const setGeneratedImagesInfo = useWizardStore((state) => state.setGeneratedImagesInfo);
  const selectGeneratedImageInfo = useWizardStore((state) => state.selectGeneratedImageInfo);
  const { isGenerating, error, generateImages } = useImageGeneration();
  const hasStartedGeneration = useRef(false);
  const [currentMessageIndex, setCurrentMessageIndex] = useState(0);
  const funnyMessages = useMemo(() => (t('steps.imageGeneration.messages', { returnObjects: true }) as string[]) ?? [], [t]);

  useEffect(() => {
    if (isGenerating && funnyMessages.length > 0) {
      const interval = setInterval(() => {
        setCurrentMessageIndex((prevIndex) => (prevIndex + 1) % funnyMessages.length);
      }, 5000);

      return () => clearInterval(interval);
    }
  }, [isGenerating, funnyMessages.length]);

  useEffect(() => {
    setCurrentMessageIndex(0);
  }, [funnyMessages.length]);

  useEffect(() => {
    if (!generatedImages && uploadedImage && selectedPrompt?.id && !hasStartedGeneration.current) {
      hasStartedGeneration.current = true;
      const performGeneration = async () => {
        setProcessing(true);
        const result = await generateImages(uploadedImage, selectedPrompt.id, cropData || undefined, 'OPENAI');
        if (result) {
          // Combine URLs and IDs into GeneratedImageInfo objects
          const imagesInfo = result.urls.map((url, index) => ({
            url,
            generatedImageId: result.ids[index],
          }));
          setGeneratedImagesInfo(imagesInfo);
        }
        setProcessing(false);
      };
      performGeneration();
    }
  }, [uploadedImage, selectedPrompt?.id, generatedImages, generateImages, setProcessing, setGeneratedImagesInfo, cropData]);

  if (isGenerating) {
    return (
      <div className="flex flex-col items-center justify-center py-12">
        <div className="relative">
          <Loader2 className="text-primary h-16 w-16 animate-spin" />
          <Sparkles className="absolute inset-0 h-16 w-16 animate-pulse text-yellow-500" />
        </div>
        <h3 className="mt-6 text-lg font-semibold">{t('steps.imageGeneration.loadingTitle')}</h3>
        {funnyMessages.length > 0 && (
          <p className="mt-2 animate-pulse text-gray-600">
            {t('steps.imageGeneration.loadingHint', { message: funnyMessages[currentMessageIndex] })}
          </p>
        )}
      </div>
    );
  }

  if (error) {
    return (
      <div className="space-y-4">
        <Alert variant="destructive">
          <AlertDescription>{error}</AlertDescription>
        </Alert>
        <p className="text-sm text-gray-600">{t('errors.generationRetry')}</p>
      </div>
    );
  }

  if (!generatedImages) {
    return null;
  }

  return (
    <div className="space-y-6">
      <div>
        <h3 className="mb-2 text-lg font-semibold">{t('steps.imageGeneration.title')}</h3>
        <p className="text-sm text-gray-600">{t('steps.imageGeneration.subtitle')}</p>
      </div>

      <ImageVariantSelector
        variants={generatedImages.map((img) => img.url)}
        selectedVariant={selectedGeneratedImage}
        onVariantSelect={(url) => {
          // Find the corresponding image info and select it
          const imageInfo = generatedImages.find((img) => img.url === url);
          if (imageInfo) {
            selectGeneratedImageInfo(imageInfo);
          }
        }}
      />

      {selectedGeneratedImage && (
        <div className="rounded-lg bg-green-50 p-4">
          <p className="text-sm font-medium text-green-800">{t('steps.imageGeneration.selected')}</p>
        </div>
      )}
    </div>
  );
}
