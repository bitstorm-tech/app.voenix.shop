import { Button } from '@/components/ui/Button';
import { useSession } from '@/hooks/queries/useAuth';
import { useAddToCart } from '@/hooks/queries/useCart';
import { useCartStore } from '@/stores/cartStore';
import { useWizardStore } from '@/stores/editor/useWizardStore';
import { ArrowLeft, ArrowRight, Loader2, ShoppingCart } from 'lucide-react';
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';

export default function WizardNavigationButtons() {
  const navigate = useNavigate();
  const { data: session } = useSession();
  const currentStep = useWizardStore((state) => state.currentStep);
  const canGoNext = useWizardStore((state) => state.canGoNext);
  const canGoPrevious = useWizardStore((state) => state.canGoPrevious);
  const goNext = useWizardStore((state) => state.goNext);
  const goPrevious = useWizardStore((state) => state.goPrevious);
  const isProcessing = useWizardStore((state) => state.isProcessing);
  const selectedMug = useWizardStore((state) => state.selectedMug);
  const selectedVariant = useWizardStore((state) => state.selectedVariant);
  const selectedGeneratedImage = useWizardStore((state) => state.selectedGeneratedImage);
  const selectedPrompt = useWizardStore((state) => state.selectedPrompt);
  const generatedImageCropData = useWizardStore((state) => state.generatedImageCropData);
  const cropData = useWizardStore((state) => state.cropData);

  // For authenticated users, use API-backed cart
  const addToCartMutation = useAddToCart();

  // For non-authenticated users, use local cart store
  const addItemLocal = useCartStore((state) => state.addItem);

  const [isAddingToCart, setIsAddingToCart] = useState(false);

  const handleNextStep = async () => {
    if (currentStep === 'preview' && canGoNext) {
      setIsAddingToCart(true);

      try {
        if (!selectedGeneratedImage) {
          throw new Error('Please select a generated image before adding to cart');
        }

        if (!selectedMug) {
          throw new Error('Please select a mug before adding to cart');
        }

        if (!selectedVariant) {
          throw new Error('Please select a mug variant before adding to cart');
        }

        // Determine which image and crop data to use
        const imageToUse = selectedGeneratedImage;
        const cropDataToUse = generatedImageCropData || cropData;

        if (session?.authenticated) {
          // For authenticated users, use the API-backed cart
          await addToCartMutation.mutateAsync({
            articleId: selectedMug.id,
            variantId: selectedVariant.id,
            quantity: 1,
            customData: {
              imageUrl: imageToUse,
              cropData: cropDataToUse,
              promptInfo: selectedPrompt
                ? {
                    promptId: selectedPrompt.id,
                    promptText: selectedPrompt.promptText || selectedPrompt.title,
                  }
                : undefined,
            },
          });
        } else {
          // For non-authenticated users, use local cart store
          addItemLocal({
            mug: selectedMug,
            variant: selectedVariant,
            image: imageToUse,
            cropData: cropDataToUse,
            prompt: selectedPrompt,
            price: selectedMug.price,
          });
        }

        // Navigate to cart page
        navigate('/cart');
      } catch (error) {
        console.error('Error adding to cart:', error);

        let errorMessage = 'Failed to add item to cart. Please try again.';
        if (error instanceof Error) {
          errorMessage = error.message;
        }

        alert(errorMessage);
      } finally {
        setIsAddingToCart(false);
      }
    } else {
      goNext();
    }
  };

  return (
    <div className="flex items-center justify-between">
      <Button variant="outline" onClick={goPrevious} disabled={!canGoPrevious || isProcessing} size="default" className="sm:h-12 sm:px-6">
        <ArrowLeft className="h-4 w-4 sm:h-5 sm:w-5" />
        <span className="hidden sm:inline">Back</span>
      </Button>

      <Button onClick={handleNextStep} disabled={!canGoNext || isProcessing || isAddingToCart} className="gap-2 sm:h-12 sm:px-6" size="default">
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
