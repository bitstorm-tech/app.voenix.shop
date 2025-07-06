import { Button } from '@/components/ui/Button';
import { ArrowLeft, ArrowRight, Loader2, ShoppingCart } from 'lucide-react';
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useWizardContext } from '../contexts/WizardContext';

export default function WizardNavigationButtons() {
  const navigate = useNavigate();
  const {
    currentStep,
    canGoNext,
    canGoPrevious,
    goNext,
    goPrevious,
    isProcessing,
    selectedMug,
    selectedGeneratedImage,
    userData,
    uploadedImageUrl,
    cropData,
    selectedPrompt,
  } = useWizardContext();
  const [isAddingToCart, setIsAddingToCart] = useState(false);

  const handleNextStep = async () => {
    if (currentStep === 'preview' && canGoNext) {
      setIsAddingToCart(true);

      try {
        // Extract the original image filename from the uploaded image URL
        const originalImagePath = uploadedImageUrl?.split('/').pop() || null;

        // Ensure we're sending the filename, not the full data URL
        const generatedImagePath = selectedGeneratedImage?.startsWith('data:') ? null : selectedGeneratedImage;

        if (!generatedImagePath) {
          throw new Error('Generated image must be saved before adding to cart');
        }

        const response = await fetch('/api/cart/items', {
          method: 'POST',
          body: JSON.stringify({
            mug_id: selectedMug?.id,
            generated_image_path: generatedImagePath,
            original_image_path: originalImagePath,
            crop_data: cropData,
            prompt_id: selectedPrompt?.id || null,
            quantity: 1,
            customization_data: {
              user_data: userData,
            },
          }),
        });

        if (!response.ok) {
          const error = await response.json();
          throw new Error(error.message || 'Failed to add item to cart');
        }

        // Navigate to cart page
        navigate('/cart');
      } catch (error) {
        console.error('Error adding to cart:', error);
        alert('Failed to add item to cart. Please try again.');
      } finally {
        setIsAddingToCart(false);
      }
    } else {
      goNext();
    }
  };

  return (
    <div className="flex items-center justify-between">
      <Button
        variant="outline"
        onClick={goPrevious}
        disabled={!canGoPrevious || isProcessing}
        size="default"
        className="sm:h-12 sm:px-6"
      >
        <ArrowLeft className="h-4 w-4 sm:h-5 sm:w-5" />
        <span className="hidden sm:inline">Back</span>
      </Button>

      <Button
        onClick={handleNextStep}
        disabled={!canGoNext || isProcessing || isAddingToCart}
        className="gap-2 sm:h-12 sm:px-6"
        size="default"
      >
        {isProcessing || isAddingToCart ? (
          <>
            <Loader2 className="h-4 w-4 animate-spin sm:h-5 sm:w-5" />
            <span className="hidden sm:inline">{isAddingToCart ? 'Adding to Cart...' : 'Processing...'}</span>
          </>
        ) : currentStep === 'preview' ? (
          <>
            <ShoppingCart className="h-4 w-4 sm:h-5 sm:w-5" />
            <span className="hidden sm:inline">Add to Cart</span>
          </>
        ) : (
          <>
            <span className="hidden sm:inline">Next</span>
            <ArrowRight className="h-4 w-4 sm:h-5 sm:w-5" />
          </>
        )}
      </Button>
    </div>
  );
}
