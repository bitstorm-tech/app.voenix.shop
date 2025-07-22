import { Button } from '@/components/ui/Button';
import { ArrowLeft, ArrowRight, Download, Loader2 } from 'lucide-react';
import { useState } from 'react';
import { useWizardContext } from '../contexts/WizardContext';

export default function WizardNavigationButtons() {
  const { currentStep, canGoNext, canGoPrevious, goNext, goPrevious, isProcessing, selectedMug, selectedGeneratedImage } = useWizardContext();
  const [isGeneratingPdf, setIsGeneratingPdf] = useState(false);

  const handleNextStep = async () => {
    if (currentStep === 'preview' && canGoNext) {
      setIsGeneratingPdf(true);

      try {
        // Extract filename from the selected generated image
        const imageFilename = selectedGeneratedImage?.startsWith('data:') ? null : selectedGeneratedImage;

        if (!imageFilename) {
          throw new Error('Please select a generated image before downloading PDF');
        }

        if (!selectedMug?.id) {
          throw new Error('Please select a mug before downloading PDF');
        }

        const response = await fetch('/api/admin/pdf/generate', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          credentials: 'include',
          body: JSON.stringify({
            mugId: selectedMug.id,
            imageFilename: imageFilename,
          }),
        });

        if (!response.ok) {
          const error = await response.json();
          throw new Error(error.message || 'Failed to generate PDF');
        }

        // Handle PDF download
        const blob = await response.blob();
        const url = URL.createObjectURL(blob);

        // Extract filename from Content-Disposition header or use default
        const contentDisposition = response.headers.get('Content-Disposition');
        const filenameMatch = contentDisposition?.match(/filename="(.+)"/);
        const filename = filenameMatch ? filenameMatch[1] : `mug_design_${Date.now()}.pdf`;

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
        alert(error instanceof Error ? error.message : 'Failed to generate PDF. Please try again.');
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
