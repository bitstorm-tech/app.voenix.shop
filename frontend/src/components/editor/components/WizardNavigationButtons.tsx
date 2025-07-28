import { Button } from '@/components/ui/Button';
import { ApiError, publicApi } from '@/lib/api';
import { useWizardStore } from '@/stores/editor/useWizardStore';
import { ArrowLeft, ArrowRight, Download, Loader2 } from 'lucide-react';
import { useState } from 'react';

export default function WizardNavigationButtons() {
  const currentStep = useWizardStore((state) => state.currentStep);
  const canGoNext = useWizardStore((state) => state.canGoNext);
  const canGoPrevious = useWizardStore((state) => state.canGoPrevious);
  const goNext = useWizardStore((state) => state.goNext);
  const goPrevious = useWizardStore((state) => state.goPrevious);
  const isProcessing = useWizardStore((state) => state.isProcessing);
  const selectedMug = useWizardStore((state) => state.selectedMug);
  const selectedGeneratedImage = useWizardStore((state) => state.selectedGeneratedImage);
  const [isGeneratingPdf, setIsGeneratingPdf] = useState(false);

  const handleNextStep = async () => {
    if (currentStep === 'preview' && canGoNext) {
      setIsGeneratingPdf(true);

      try {
        // Validate we have the required data
        if (!selectedGeneratedImage) {
          throw new Error('Please select a generated image before downloading PDF');
        }

        if (!selectedMug?.id) {
          throw new Error('Please select a mug before downloading PDF');
        }

        // Handle different image URL formats:
        // - http:// or https:// URLs are used as-is
        // - data: URLs are used as-is
        // - URLs starting with /api/ are already complete (e.g., /api/public/images/...)
        // - Otherwise, assume it's just a filename and construct the full URL
        let imageUrl = selectedGeneratedImage;
        if (
          !selectedGeneratedImage.startsWith('http') &&
          !selectedGeneratedImage.startsWith('data:') &&
          !selectedGeneratedImage.startsWith('/api/')
        ) {
          imageUrl = `/api/images/${selectedGeneratedImage}`;
        }

        // Use the public API endpoint for PDF generation
        const blob = await publicApi.generatePdf(selectedMug.id, imageUrl);
        const url = URL.createObjectURL(blob);

        // Generate a default filename since we can't access headers from the blob
        const filename = `mug_design_${Date.now()}.pdf`;

        // Create download link and trigger download
        const link = document.createElement('a');
        link.href = url;
        link.download = filename;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);

        // Clean up the blob URL after a short delay
        setTimeout(() => URL.revokeObjectURL(url), 100);

        // Show success feedback
        console.log('PDF downloaded successfully');
      } catch (error) {
        console.error('Error generating PDF:', error);

        let errorMessage = 'Failed to generate PDF. Please try again.';

        if (error instanceof ApiError) {
          if (error.status === 400) {
            errorMessage = error.message; // Validation error from backend
          } else if (error.status === 404) {
            errorMessage = 'The selected mug was not found. Please try selecting a different mug.';
          } else if (error.status === 429) {
            errorMessage = 'Too many PDF generation requests. Please try again later.';
          } else if (error.status === 500) {
            errorMessage = 'Server error while generating PDF. Please try again later.';
          } else {
            errorMessage = error.message;
          }
        } else if (error instanceof Error) {
          errorMessage = error.message;
        }

        alert(errorMessage);
      } finally {
        setIsGeneratingPdf(false);
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

      <Button onClick={handleNextStep} disabled={!canGoNext || isProcessing || isGeneratingPdf} className="gap-2 sm:h-12 sm:px-6" size="default">
        {isProcessing || isGeneratingPdf ? (
          <>
            <Loader2 className="h-4 w-4 animate-spin sm:h-5 sm:w-5" />
            <span className="hidden sm:inline">{isGeneratingPdf ? 'Generating PDF...' : 'Processing...'}</span>
          </>
        ) : currentStep === 'preview' ? (
          <>
            <Download className="h-4 w-4 sm:h-5 sm:w-5" />
            <span className="hidden sm:inline">Download PDF</span>
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
