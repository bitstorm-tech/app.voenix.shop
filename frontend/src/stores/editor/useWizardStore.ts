import { WIZARD_STEPS, WizardStep } from '@/components/editor/constants';
import { CropData, GeneratedImageCropData, MugOption, MugVariant, UserData } from '@/components/editor/types';
import { User } from '@/types/auth';
import { Prompt } from '@/types/prompt';
import { create } from 'zustand';
import { immer } from 'zustand/middleware/immer';

interface GeneratedImageInfo {
  url: string;
  generatedImageId?: number;
}

interface WizardStore {
  // ========== State ==========
  currentStep: WizardStep;
  uploadedImage: File | null;
  uploadedImageUrl: string | null;
  cropData: CropData | null;
  selectedPrompt: Prompt | null;
  selectedMug: MugOption | null;
  selectedVariant: MugVariant | null;
  userData: UserData | null;
  generatedImages: GeneratedImageInfo[] | null;
  selectedGeneratedImage: string | null;
  selectedGeneratedImageInfo: GeneratedImageInfo | null;
  generatedImageCropData: GeneratedImageCropData | null;
  generationPrompt: string | null;
  isProcessing: boolean;
  error: string | null;

  // Authentication state
  isAuthenticated: boolean;
  user: User | null;

  // Navigation state - computed
  canGoNext: boolean;
  canGoPrevious: boolean;

  // ========== Navigation Actions ==========
  goToStep: (step: WizardStep) => void;
  goNext: () => void;
  goPrevious: () => void;
  reset: () => void;

  // ========== Image Actions ==========
  uploadImage: (file: File, url: string) => void;
  cropImage: (cropData: CropData) => void;
  removeImage: () => void;
  setPreloadedImage: (imageUrl: string, filename?: string) => Promise<void>;

  // ========== Selection Actions ==========
  selectPrompt: (prompt: Prompt) => void;
  selectMug: (mug: MugOption) => void;
  selectVariant: (variant: MugVariant) => void;
  setUserData: (data: UserData) => void;

  // ========== Generation Actions ==========
  setGeneratedImages: (urls: string[]) => void;
  setGeneratedImagesInfo: (images: GeneratedImageInfo[], prompt?: string | null) => void;
  selectGeneratedImage: (url: string) => void;
  selectGeneratedImageInfo: (imageInfo: GeneratedImageInfo) => void;
  updateGeneratedImageCropData: (cropData: GeneratedImageCropData | null) => void;
  setProcessing: (isProcessing: boolean) => void;
  setError: (error: string | null) => void;

  // ========== Computed Values ==========
  getCompletedSteps: () => WizardStep[];

  // ========== Authentication Actions ==========
  setAuthenticated: (isAuthenticated: boolean, user: User | null) => void;

  // ========== State Preservation Actions ==========
  preserveState: () => void;
  restoreState: () => void;
  clearPreservedState: () => void;
  hasPreservedState: () => boolean;
}

// Helper function to determine if can proceed from current step
function canProceedFromStep(
  state: Pick<
    WizardStore,
    'currentStep' | 'uploadedImage' | 'selectedPrompt' | 'selectedMug' | 'userData' | 'selectedGeneratedImage' | 'isAuthenticated'
  >,
  step: WizardStep,
): boolean {
  switch (step) {
    case 'image-upload':
      return state.uploadedImage !== null;
    case 'prompt-selection':
      return state.selectedPrompt !== null;
    case 'mug-selection':
      return state.selectedMug !== null;
    case 'user-data':
      // Authenticated users can always proceed from this step (they skip it)
      return (
        state.isAuthenticated ||
        (state.userData !== null && state.userData.email.length > 0 && /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(state.userData.email))
      );
    case 'image-generation':
      return state.selectedGeneratedImage !== null;
    case 'preview':
      return state.selectedMug !== null && state.selectedGeneratedImage !== null;
    default:
      return false;
  }
}

// Helper to get next/previous step
function getNextStep(currentStep: WizardStep, isAuthenticated: boolean): WizardStep | null {
  const currentIndex = WIZARD_STEPS.indexOf(currentStep);
  if (currentIndex === -1 || currentIndex === WIZARD_STEPS.length - 1) return null;

  const nextStep = WIZARD_STEPS[currentIndex + 1];

  // Skip user-data step for authenticated users
  if (nextStep === 'user-data' && isAuthenticated) {
    return WIZARD_STEPS[currentIndex + 2]; // Skip to image-generation
  }

  return nextStep;
}

function getPreviousStep(currentStep: WizardStep, isAuthenticated: boolean): WizardStep | null {
  const currentIndex = WIZARD_STEPS.indexOf(currentStep);
  if (currentIndex <= 0) return null;

  const previousStep = WIZARD_STEPS[currentIndex - 1];

  // Skip user-data step for authenticated users when going backwards
  if (previousStep === 'user-data' && isAuthenticated) {
    return WIZARD_STEPS[currentIndex - 2]; // Skip to mug-selection
  }

  return previousStep;
}

export const useWizardStore = create<WizardStore>()(
  immer((set, get) => ({
    // ========== Initial State ==========
    currentStep: 'image-upload',
    uploadedImage: null,
    uploadedImageUrl: null,
    cropData: null,
    selectedPrompt: null,
    selectedMug: null,
    selectedVariant: null,
    userData: null,
    generatedImages: null,
    selectedGeneratedImage: null,
    selectedGeneratedImageInfo: null,
    generatedImageCropData: null,
    generationPrompt: null,
    isProcessing: false,
    error: null,
    canGoNext: false,
    canGoPrevious: false,

    // Authentication state
    isAuthenticated: false,
    user: null,

    // ========== Navigation Actions ==========
    goToStep: (step) => {
      set((state) => {
        state.currentStep = step;
        state.canGoNext = canProceedFromStep(state, step);
        state.canGoPrevious = step !== 'image-upload';
      });
      // Scroll to top when step changes
      window.scrollTo(0, 0);
    },

    goNext: () => {
      const state = get();
      if (!canProceedFromStep(state, state.currentStep)) return;

      const nextStep = getNextStep(state.currentStep, state.isAuthenticated);
      if (!nextStep) return;

      set((state) => {
        state.currentStep = nextStep;
        state.canGoNext = canProceedFromStep(state, nextStep);
        state.canGoPrevious = true;
      });
      window.scrollTo(0, 0);
    },

    goPrevious: () => {
      const state = get();
      const previousStep = getPreviousStep(state.currentStep, state.isAuthenticated);
      if (!previousStep) return;

      set((state) => {
        state.currentStep = previousStep;
        state.canGoNext = canProceedFromStep(state, previousStep);
        state.canGoPrevious = previousStep !== 'image-upload';
      });
      window.scrollTo(0, 0);
    },

    reset: () => {
      const state = get();
      // Clean up the object URL if it exists
      if (state.uploadedImageUrl) {
        URL.revokeObjectURL(state.uploadedImageUrl);
      }

      set((state) => {
        state.currentStep = 'image-upload';
        state.uploadedImage = null;
        state.uploadedImageUrl = null;
        state.cropData = null;
        state.selectedPrompt = null;
        state.selectedMug = null;
        state.selectedVariant = null;
        state.userData = null;
        state.generatedImages = null;
        state.selectedGeneratedImage = null;
        state.selectedGeneratedImageInfo = null;
        state.generatedImageCropData = null;
        state.generationPrompt = null;
        state.isProcessing = false;
        state.error = null;
        state.canGoNext = false;
        state.canGoPrevious = false;
      });
    },

    // ========== Image Actions ==========
    uploadImage: (file, url) => {
      set((state) => {
        state.uploadedImage = file;
        state.uploadedImageUrl = url;
        state.cropData = null; // Reset crop data when new image is uploaded
        state.canGoNext = true;
      });
    },

    cropImage: (cropData) => {
      set((state) => {
        state.cropData = cropData;
      });
    },

    removeImage: () => {
      const state = get();
      // Clean up the object URL if it exists
      if (state.uploadedImageUrl) {
        URL.revokeObjectURL(state.uploadedImageUrl);
      }

      set((state) => {
        state.uploadedImage = null;
        state.uploadedImageUrl = null;
        state.cropData = null;
        state.canGoNext = false;
      });
    },

    setPreloadedImage: async (imageUrl: string, filename = 'preloaded-image.jpg') => {
      try {
        // Fetch the image from the URL
        const response = await fetch(imageUrl);
        const blob = await response.blob();

        // Create a File object from the blob
        const file = new File([blob], filename, {
          type: blob.type || 'image/jpeg',
          lastModified: Date.now(),
        });

        // Create object URL for display
        const objectUrl = URL.createObjectURL(file);

        set((state) => {
          state.uploadedImage = file;
          state.uploadedImageUrl = objectUrl;
          state.cropData = null; // Reset crop data for new image
          state.canGoNext = true;
        });
      } catch (error) {
        console.error('Failed to preload image:', error);
        set((state) => {
          state.error = 'Failed to load image';
        });
      }
    },

    // ========== Selection Actions ==========
    selectPrompt: (prompt) => {
      set((state) => {
        state.selectedPrompt = prompt;
        state.canGoNext = true;
      });
    },

    selectMug: (mug) => {
      set((state) => {
        state.selectedMug = mug;
        // Automatically select the default variant if available
        const defaultVariant = mug.variants?.find((v) => v.isDefault) || mug.variants?.[0];
        state.selectedVariant = defaultVariant || null;
        state.canGoNext = true;
      });
    },

    selectVariant: (variant) => {
      set((state) => {
        state.selectedVariant = variant;
      });
    },

    setUserData: (data) => {
      set((state) => {
        state.userData = data;
        state.canGoNext = data.email.length > 0 && /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(data.email);
      });
    },

    // ========== Generation Actions ==========
    setGeneratedImages: (urls) => {
      set((state) => {
        state.generatedImages = urls.map((url) => ({ url }));
        state.selectedGeneratedImage = null; // Reset selection when new images are generated
        state.selectedGeneratedImageInfo = null;
        state.generatedImageCropData = null; // Reset crop data when new images are generated
        state.generationPrompt = null;
        state.canGoNext = false;
      });
    },

    setGeneratedImagesInfo: (images, prompt) => {
      set((state) => {
        state.generatedImages = images;
        state.selectedGeneratedImage = null;
        state.selectedGeneratedImageInfo = null;
        state.generatedImageCropData = null;
        state.generationPrompt = prompt ?? null;
        state.canGoNext = false;
      });
    },

    selectGeneratedImage: (url) => {
      set((state) => {
        state.selectedGeneratedImage = url;
        const imageInfo = state.generatedImages?.find((img) => img.url === url);
        state.selectedGeneratedImageInfo = imageInfo || null;
        state.generatedImageCropData = null; // Reset crop data when selecting a different image
        state.canGoNext = true;
      });
    },

    selectGeneratedImageInfo: (imageInfo) => {
      set((state) => {
        state.selectedGeneratedImageInfo = imageInfo;
        state.selectedGeneratedImage = imageInfo.url;
        state.generatedImageCropData = null;
        state.canGoNext = true;
      });
    },

    updateGeneratedImageCropData: (cropData) => {
      set((state) => {
        state.generatedImageCropData = cropData;
      });
    },

    setProcessing: (isProcessing) => {
      set((state) => {
        state.isProcessing = isProcessing;
      });
    },

    setError: (error) => {
      set((state) => {
        state.error = error;
      });
    },

    // ========== Computed Values ==========
    getCompletedSteps: () => {
      const state = get();
      const completed: WizardStep[] = [];
      if (state.uploadedImage && state.cropData) completed.push('image-upload');
      if (state.selectedPrompt) completed.push('prompt-selection');
      if (state.selectedMug) completed.push('mug-selection');
      if (state.userData || state.isAuthenticated) completed.push('user-data');
      if (state.selectedGeneratedImage) completed.push('image-generation');
      return completed;
    },

    // ========== Authentication Actions ==========
    setAuthenticated: (isAuthenticated, user) => {
      set((state) => {
        state.isAuthenticated = isAuthenticated;
        state.user = user;

        // Update navigation state based on authentication
        state.canGoNext = canProceedFromStep(state, state.currentStep);
      });
    },

    // ========== State Preservation Actions ==========
    preserveState: () => {
      const state = get();
      const stateToPreserve = {
        currentStep: state.currentStep,
        uploadedImage: state.uploadedImage
          ? {
              name: state.uploadedImage.name,
              size: state.uploadedImage.size,
              type: state.uploadedImage.type,
              lastModified: state.uploadedImage.lastModified,
            }
          : null,
        uploadedImageUrl: state.uploadedImageUrl,
        cropData: state.cropData,
        selectedPrompt: state.selectedPrompt,
        selectedMug: state.selectedMug,
        selectedVariant: state.selectedVariant,
        // Deliberately not preserving userData for privacy
        generatedImages: state.generatedImages,
        selectedGeneratedImage: state.selectedGeneratedImage,
        selectedGeneratedImageInfo: state.selectedGeneratedImageInfo,
        generatedImageCropData: state.generatedImageCropData,
      };

      // Store as JSON in sessionStorage
      sessionStorage.setItem('wizardState', JSON.stringify(stateToPreserve));

      // Store the actual image file data as base64 if it exists
      if (state.uploadedImage) {
        const reader = new FileReader();
        reader.onloadend = () => {
          sessionStorage.setItem('wizardImageData', reader.result as string);
        };
        reader.readAsDataURL(state.uploadedImage);
      }
    },

    restoreState: () => {
      const preservedStateStr = sessionStorage.getItem('wizardState');
      const preservedImageData = sessionStorage.getItem('wizardImageData');

      if (!preservedStateStr) return;

      try {
        const preservedState = JSON.parse(preservedStateStr);

        set((state) => {
          // Restore non-file data
          state.currentStep = preservedState.currentStep || 'image-upload';
          state.cropData = preservedState.cropData;
          state.selectedPrompt = preservedState.selectedPrompt;
          state.selectedMug = preservedState.selectedMug;
          state.selectedVariant = preservedState.selectedVariant;
          state.generatedImages = preservedState.generatedImages;
          state.selectedGeneratedImage = preservedState.selectedGeneratedImage;
          state.selectedGeneratedImageInfo = preservedState.selectedGeneratedImageInfo;
          state.generatedImageCropData = preservedState.generatedImageCropData;

          // Update navigation state
          state.canGoNext = canProceedFromStep(state, state.currentStep);
          state.canGoPrevious = state.currentStep !== 'image-upload';
        });

        // Restore the uploaded image if we have the data
        if (preservedState.uploadedImage && preservedImageData) {
          // Convert base64 back to File
          fetch(preservedImageData)
            .then((res) => res.blob())
            .then((blob) => {
              const file = new File([blob], preservedState.uploadedImage.name, {
                type: preservedState.uploadedImage.type,
                lastModified: preservedState.uploadedImage.lastModified,
              });
              const url = URL.createObjectURL(file);

              set((state) => {
                state.uploadedImage = file;
                state.uploadedImageUrl = url;
              });
            });
        }

        // Clear the preserved state after restoration
        get().clearPreservedState();
      } catch (error) {
        console.error('Failed to restore wizard state:', error);
      }
    },

    clearPreservedState: () => {
      sessionStorage.removeItem('wizardState');
      sessionStorage.removeItem('wizardImageData');
    },

    hasPreservedState: () => {
      return sessionStorage.getItem('wizardState') !== null;
    },
  })),
);
