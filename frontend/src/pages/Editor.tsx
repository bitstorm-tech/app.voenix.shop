import WizardNavigationButtons from '@/components/editor/components/WizardNavigationButtons';
import WizardStepIndicator from '@/components/editor/components/WizardStepIndicator';
import ImageUploadStep from '@/components/editor/components/steps/1-ImageUploadStep';
import PromptSelectionStep from '@/components/editor/components/steps/2-PromptSelectionStep';
import MugSelectionStep from '@/components/editor/components/steps/3-MugSelectionStep';
import UserDataStep from '@/components/editor/components/steps/4-UserDataStep';
import ImageGenerationStep from '@/components/editor/components/steps/5-ImageGenerationStep';
import PreviewStep from '@/components/editor/components/steps/6-PreviewStep';
import { WizardProvider, useWizardContext } from '@/components/editor/contexts/WizardContext';
import { Head } from '@inertiajs/react';

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

          {wizard.currentStep === 'user-data' && (
            <>
              <UserDataStep />
              {wizard.registrationError && (
                <div className="mt-4 rounded-md bg-red-50 p-4">
                  <p className="text-sm text-red-800">{wizard.registrationError}</p>
                </div>
              )}
            </>
          )}

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

      {wizard.isRegistering && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50">
          <div className="rounded-lg bg-white p-6 shadow-xl">
            <div className="flex items-center gap-3">
              <div className="h-5 w-5 animate-spin rounded-full border-2 border-gray-300 border-t-blue-600" />
              <p className="text-sm font-medium">Creating your account...</p>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default function EditorPage() {
  return (
    <WizardProvider auth={auth}>
      <Head title="Editor" />
      <EditorContent />
    </WizardProvider>
  );
}
