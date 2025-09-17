import { Button } from '@/components/ui/Button';
import { useSession } from '@/hooks/queries/useAuth';
import { useAddToCart } from '@/hooks/queries/useCart';
import { useWizardStore } from '@/stores/editor/useWizardStore';
import { ArrowLeft, ArrowRight, Loader2, LogIn, ShoppingCart } from 'lucide-react';
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';

export default function WizardNavigationButtons() {
  const navigate = useNavigate();
  const { data: session } = useSession();
  const { t } = useTranslation('editor');
  const currentStep = useWizardStore((state) => state.currentStep);
  const canGoNext = useWizardStore((state) => state.canGoNext);
  const canGoPrevious = useWizardStore((state) => state.canGoPrevious);
  const goNext = useWizardStore((state) => state.goNext);
  const goPrevious = useWizardStore((state) => state.goPrevious);
  const isProcessing = useWizardStore((state) => state.isProcessing);
  const selectedMug = useWizardStore((state) => state.selectedMug);
  const selectedVariant = useWizardStore((state) => state.selectedVariant);
  const selectedGeneratedImage = useWizardStore((state) => state.selectedGeneratedImage);
  const selectedGeneratedImageInfo = useWizardStore((state) => state.selectedGeneratedImageInfo);
  const selectedPrompt = useWizardStore((state) => state.selectedPrompt);
  const generatedImageCropData = useWizardStore((state) => state.generatedImageCropData);
  const cropData = useWizardStore((state) => state.cropData);

  // For authenticated users, use API-backed cart
  const addToCartMutation = useAddToCart();

  const [isAddingToCart, setIsAddingToCart] = useState(false);

  const handleNextStep = async () => {
    if (currentStep === 'preview' && canGoNext) {
      // For non-authenticated users, redirect to login
      if (!session?.authenticated) {
        const returnUrl = encodeURIComponent(window.location.pathname);
        navigate(`/login?returnUrl=${returnUrl}`);
        return;
      }

      setIsAddingToCart(true);

      try {
        if (!selectedGeneratedImage) {
          throw new Error(t('errors.imageMissing'));
        }

        if (!selectedGeneratedImageInfo?.generatedImageId) {
          throw new Error(t('errors.imageInfoMissing'));
        }

        if (!selectedMug) {
          throw new Error(t('errors.mugMissing'));
        }

        if (!selectedVariant) {
          throw new Error(t('errors.variantMissing'));
        }

        // Determine which crop data to use
        const cropDataToUse = generatedImageCropData || cropData || undefined;

        // For authenticated users, use the API-backed cart
        await addToCartMutation.mutateAsync({
          articleId: selectedMug.id,
          variantId: selectedVariant.id,
          quantity: 1,
          // Structured fields for generated image and prompt references
          generatedImageId: selectedGeneratedImageInfo?.generatedImageId,
          promptId: selectedPrompt?.id,
          // Custom data contains only crop data for image positioning
          customData: {
            cropData: cropDataToUse,
          },
        });

        // Navigate to cart page
        navigate('/cart');
      } catch (error) {
        console.error('Error adding to cart:', error);

        let errorMessage = t('errors.addToCart');
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
      <Button
        variant="outline"
        onClick={goPrevious}
        disabled={!canGoPrevious || isProcessing}
        size="default"
        className="sm:h-12 sm:px-6"
        data-wizard-role="previous"
      >
        <ArrowLeft className="h-4 w-4 sm:h-5 sm:w-5" />
        <span className="hidden sm:inline">{t('navigation.back')}</span>
      </Button>

      <Button
        onClick={handleNextStep}
        disabled={!canGoNext || isProcessing || isAddingToCart}
        className="gap-2 sm:h-12 sm:px-6"
        size="default"
        data-wizard-role="next"
      >
        {isProcessing || isAddingToCart ? (
          <>
            <Loader2 className="h-4 w-4 animate-spin sm:h-5 sm:w-5" />
            <span className="hidden sm:inline">{isAddingToCart ? t('navigation.adding') : t('navigation.processing')}</span>
          </>
        ) : currentStep === 'preview' ? (
          <>
            {session?.authenticated ? (
              <>
                <ShoppingCart className="h-4 w-4 sm:h-5 sm:w-5" />
                <span className="hidden sm:inline">{t('navigation.addToCart')}</span>
              </>
            ) : (
              <>
                <LogIn className="h-4 w-4 sm:h-5 sm:w-5" />
                <span className="hidden sm:inline">{t('navigation.signIn')}</span>
              </>
            )}
          </>
        ) : (
          <>
            <span className="hidden sm:inline">{t('navigation.next')}</span>
            <ArrowRight className="h-4 w-4 sm:h-5 sm:w-5" />
          </>
        )}
      </Button>
    </div>
  );
}
