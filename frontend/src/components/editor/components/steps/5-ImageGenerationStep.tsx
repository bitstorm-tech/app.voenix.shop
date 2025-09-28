import { Accordion, AccordionContent, AccordionItem, AccordionTrigger } from '@/components/ui/Accordion';
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
  const selectedMug = useWizardStore((state) => state.selectedMug);
  const generatedImages = useWizardStore((state) => state.generatedImages);
  const selectedGeneratedImage = useWizardStore((state) => state.selectedGeneratedImage);
  const cropData = useWizardStore((state) => state.cropData);
  const generationPrompt = useWizardStore((state) => state.generationPrompt);
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
    if (!generatedImages && uploadedImage && selectedPrompt?.id && selectedMug?.id && !hasStartedGeneration.current) {
      hasStartedGeneration.current = true;
      const performGeneration = async () => {
        setProcessing(true);
        const result = await generateImages(uploadedImage, selectedPrompt.id, selectedMug.id, cropData || undefined);
        if (result) {
          // Combine URLs and IDs into GeneratedImageInfo objects
          const imagesInfo = result.urls.map((url, index) => ({
            url,
            generatedImageId: result.ids[index],
          }));
          setGeneratedImagesInfo(imagesInfo, result.prompt ?? null);
        }
        setProcessing(false);
      };
      performGeneration();
    }
  }, [uploadedImage, selectedPrompt?.id, selectedMug?.id, generatedImages, generateImages, setProcessing, setGeneratedImagesInfo, cropData]);

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

      {generationPrompt && (
        <Accordion type="single" collapsible className="rounded-lg border border-gray-200 bg-white shadow-sm">
          <AccordionItem value="prompt" className="border-0">
            <AccordionTrigger className="px-4 py-2 text-left text-sm font-semibold text-gray-800">
              {t('steps.imageGeneration.promptSection.title')}
            </AccordionTrigger>
            <AccordionContent className="space-y-2 px-4 pb-4 text-sm text-gray-700">
              <p>{t('steps.imageGeneration.promptSection.description')}</p>
              <pre className="rounded-md bg-gray-100 p-3 text-left font-sans text-sm whitespace-pre-wrap text-gray-800">{generationPrompt}</pre>
            </AccordionContent>
          </AccordionItem>
        </Accordion>
      )}

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
