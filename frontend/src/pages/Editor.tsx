import WizardNavigationButtons from '@/components/editor/components/WizardNavigationButtons';
import WizardStepIndicator from '@/components/editor/components/WizardStepIndicator';
import ImageUploadStep from '@/components/editor/components/steps/1-ImageUploadStep';
import PromptSelectionStep from '@/components/editor/components/steps/2-PromptSelectionStep';
import MugSelectionStep from '@/components/editor/components/steps/3-MugSelectionStep';
import UserDataStep from '@/components/editor/components/steps/4-UserDataStep';
import ImageGenerationStep from '@/components/editor/components/steps/5-ImageGenerationStep';
import PreviewStep from '@/components/editor/components/steps/6-PreviewStep';
import { WizardProvider, useWizardContext } from '@/components/editor/contexts/WizardContext';
import { useEffect } from 'react';

function EditorContent() {
  const wizard = useWizardContext();

  if (wizard.promptsLoading) {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <div className="text-center">
          <div className="inline-block h-8 w-8 animate-spin rounded-full border-4 border-solid border-current border-r-transparent"></div>
          <p className="mt-2 text-sm text-gray-600">Loading editor...</p>
        </div>
      </div>
    );
  }

  if (wizard.promptsError) {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <div className="text-center">
          <p className="text-red-600">Error loading editor: {wizard.promptsError}</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="mx-auto max-w-5xl px-4 py-8 pb-24 sm:pb-8">
        <div className="mb-8">
          <h1 className="mb-2 text-center text-3xl font-bold">Create Your Custom Mug</h1>
          <p className="text-center text-gray-600">Follow the steps below to design your personalized mug</p>
        </div>

        <div className="mb-8">
          <WizardStepIndicator />
        </div>

        <div className="mb-8 min-h-[400px] rounded-lg bg-white p-6 shadow-sm">
          {wizard.currentStep === 'image-upload' && <ImageUploadStep />}

          {wizard.currentStep === 'prompt-selection' && <PromptSelectionStep />}

          {wizard.currentStep === 'mug-selection' && <MugSelectionStep />}

          {wizard.currentStep === 'user-data' && <UserDataStep />}

          {wizard.currentStep === 'image-generation' && <ImageGenerationStep />}

          {wizard.currentStep === 'preview' && <PreviewStep />}
        </div>
      </div>

      {/* Sticky navigation for all devices */}
      <div className="fixed right-0 bottom-0 left-0 p-4">
        <div className="mx-auto max-w-5xl">
          <WizardNavigationButtons />
        </div>
      </div>

    </div>
  );
}

export default function Editor() {
  useEffect(() => {
    document.title = 'Editor - Voenix Shop';
  }, []);

  return (
    <WizardProvider>
      <EditorContent />
    </WizardProvider>
  );
}
