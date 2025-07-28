import { Alert, AlertDescription } from '@/components/ui/Alert';
import { Loader2, Sparkles } from 'lucide-react';
import { useEffect, useRef, useState } from 'react';
import { useWizardContext } from '../../contexts/WizardContext';
import { useImageGeneration } from '../../hooks/useImageGeneration';
import ImageVariantSelector from '../shared/ImageVariantSelector';

const funnyMessages = [
  'Teaching pixels to dance in perfect formation...',
  'Bribing the AI with virtual cookies for better results...',
  'Convincing the neural network this is its masterpiece...',
  'Sprinkling digital fairy dust on your image...',
  'Having a serious conversation with the algorithm...',
  'Mixing artistic genius with a pinch of chaos...',
  'Asking the AI nicely to make it extra special...',
  "Channeling the spirit of famous artists (they're on speed dial)...",
  'Performing ancient rituals to summon creativity...',
  'Whispering sweet nothings to the machine learning models...',
];

export default function ImageGenerationStep() {
  const { uploadedImage, selectedPrompt, generatedImageUrls, selectedGeneratedImage, setProcessing, setGeneratedImages, selectGeneratedImage } =
    useWizardContext();
  const { isGenerating, error, generateImages } = useImageGeneration();
  const hasStartedGeneration = useRef(false);
  const [currentMessageIndex, setCurrentMessageIndex] = useState(0);

  useEffect(() => {
    if (isGenerating) {
      const interval = setInterval(() => {
        setCurrentMessageIndex((prevIndex) => (prevIndex + 1) % funnyMessages.length);
      }, 5000);

      return () => clearInterval(interval);
    }
  }, [isGenerating]);

  useEffect(() => {
    if (!generatedImageUrls && uploadedImage && selectedPrompt?.id && !hasStartedGeneration.current) {
      hasStartedGeneration.current = true;
      const performGeneration = async () => {
        setProcessing(true);
        const urls = await generateImages(uploadedImage, selectedPrompt.id);
        if (urls) {
          setGeneratedImages(urls);
        }
        setProcessing(false);
      };
      performGeneration();
    }
  }, [uploadedImage, selectedPrompt?.id, generatedImageUrls, generateImages, setProcessing, setGeneratedImages]);

  if (isGenerating) {
    return (
      <div className="flex flex-col items-center justify-center py-12">
        <div className="relative">
          <Loader2 className="text-primary h-16 w-16 animate-spin" />
          <Sparkles className="absolute inset-0 h-16 w-16 animate-pulse text-yellow-500" />
        </div>
        <h3 className="mt-6 text-lg font-semibold">Creating Your Magic Designs (this can take up to 30 seconds)</h3>
        <p className="mt-2 animate-pulse text-gray-600">{funnyMessages[currentMessageIndex]}</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="space-y-4">
        <Alert variant="destructive">
          <AlertDescription>{error}</AlertDescription>
        </Alert>
        <p className="text-sm text-gray-600">Please go back and try again with a different image or prompt.</p>
      </div>
    );
  }

  if (!generatedImageUrls) {
    return null;
  }

  return (
    <div className="space-y-6">
      <div>
        <h3 className="mb-2 text-lg font-semibold">Choose Your Favorite Design</h3>
        <p className="text-sm text-gray-600">Select the design that best captures your vision from these AI-generated variations</p>
      </div>

      <ImageVariantSelector variants={generatedImageUrls} selectedVariant={selectedGeneratedImage} onVariantSelect={selectGeneratedImage} />

      {selectedGeneratedImage && (
        <div className="rounded-lg bg-green-50 p-4">
          <p className="text-sm font-medium text-green-800">Great choice! Click Next to see how it looks on your selected mug.</p>
        </div>
      )}
    </div>
  );
}
