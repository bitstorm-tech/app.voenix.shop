import { WIZARD_STEPS, WizardStep } from '@/components/editor/constants';
import { CropData, GeneratedImageCropData, MugOption, UserData } from '@/components/editor/types';
import { publicApi } from '@/lib/api';
import { Prompt } from '@/types/prompt';
import { create } from 'zustand';
import { immer } from 'zustand/middleware/immer';

interface WizardStore {
  // ========== State ==========
  currentStep: WizardStep;
  uploadedImage: File | null;
  uploadedImageUrl: string | null;
  cropData: CropData | null;
  selectedPrompt: Prompt | null;
  selectedMug: MugOption | null;
  userData: UserData | null;
  generatedImageUrls: string[] | null;
  selectedGeneratedImage: string | null;
  generatedImageCropData: GeneratedImageCropData | null;
  isProcessing: boolean;
  error: string | null;

  // Prompts state
  prompts: Prompt[];
  promptsLoading: boolean;
  promptsError: string | null;

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

  // ========== Selection Actions ==========
  selectPrompt: (prompt: Prompt) => void;
  selectMug: (mug: MugOption) => void;
  setUserData: (data: UserData) => void;

  // ========== Generation Actions ==========
  setGeneratedImages: (urls: string[]) => void;
  selectGeneratedImage: (url: string) => void;
  updateGeneratedImageCropData: (cropData: GeneratedImageCropData | null) => void;
  setProcessing: (isProcessing: boolean) => void;
  setError: (error: string | null) => void;

  // ========== Prompts Actions ==========
  setPrompts: (prompts: Prompt[]) => void;
  setPromptsLoading: (loading: boolean) => void;
  setPromptsError: (error: string | null) => void;
  fetchPrompts: () => Promise<void>;

  // ========== Computed Values ==========
  getCompletedSteps: () => WizardStep[];
}

// Helper function to determine if can proceed from current step
function canProceedFromStep(
  state: Pick<WizardStore, 'currentStep' | 'uploadedImage' | 'selectedPrompt' | 'selectedMug' | 'userData' | 'selectedGeneratedImage'>,
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
      return state.userData !== null && state.userData.email.length > 0;
    case 'image-generation':
      return state.selectedGeneratedImage !== null;
    case 'preview':
      return state.selectedMug !== null && state.selectedGeneratedImage !== null;
    default:
      return false;
  }
}

// Helper to get next/previous step
function getNextStep(currentStep: WizardStep): WizardStep | null {
  const currentIndex = WIZARD_STEPS.indexOf(currentStep);
  if (currentIndex === -1 || currentIndex === WIZARD_STEPS.length - 1) return null;
  return WIZARD_STEPS[currentIndex + 1];
}

function getPreviousStep(currentStep: WizardStep): WizardStep | null {
  const currentIndex = WIZARD_STEPS.indexOf(currentStep);
  if (currentIndex <= 0) return null;
  return WIZARD_STEPS[currentIndex - 1];
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
    userData: null,
    generatedImageUrls: null,
    selectedGeneratedImage: null,
    generatedImageCropData: null,
    isProcessing: false,
    error: null,
    prompts: [],
    promptsLoading: true,
    promptsError: null,
    canGoNext: false,
    canGoPrevious: false,

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

      const nextStep = getNextStep(state.currentStep);
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
      const previousStep = getPreviousStep(state.currentStep);
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
        state.userData = null;
        state.generatedImageUrls = null;
        state.selectedGeneratedImage = null;
        state.generatedImageCropData = null;
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
        state.canGoNext = true;
      });
    },

    setUserData: (data) => {
      set((state) => {
        state.userData = data;
        state.canGoNext = data.email.length > 0;
      });
    },

    // ========== Generation Actions ==========
    setGeneratedImages: (urls) => {
      set((state) => {
        state.generatedImageUrls = urls;
        state.selectedGeneratedImage = null; // Reset selection when new images are generated
        state.generatedImageCropData = null; // Reset crop data when new images are generated
        state.canGoNext = false;
      });
    },

    selectGeneratedImage: (url) => {
      set((state) => {
        state.selectedGeneratedImage = url;
        state.generatedImageCropData = null; // Reset crop data when selecting a different image
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

    // ========== Prompts Actions ==========
    setPrompts: (prompts) => {
      set((state) => {
        state.prompts = prompts;
      });
    },

    setPromptsLoading: (loading) => {
      set((state) => {
        state.promptsLoading = loading;
      });
    },

    setPromptsError: (error) => {
      set((state) => {
        state.promptsError = error;
      });
    },

    fetchPrompts: async () => {
      set((state) => {
        state.promptsLoading = true;
        state.promptsError = null;
      });

      try {
        const data = await publicApi.fetchPrompts();
        set((state) => {
          state.prompts = data;
          state.promptsLoading = false;
        });
      } catch (error) {
        const errorMessage = error instanceof Error ? error.message : 'Failed to fetch prompts';
        set((state) => {
          state.promptsError = errorMessage;
          state.promptsLoading = false;
        });
      }
    },

    // ========== Computed Values ==========
    getCompletedSteps: () => {
      const state = get();
      const completed: WizardStep[] = [];
      if (state.uploadedImage && state.cropData) completed.push('image-upload');
      if (state.selectedPrompt) completed.push('prompt-selection');
      if (state.selectedMug) completed.push('mug-selection');
      if (state.userData) completed.push('user-data');
      if (state.selectedGeneratedImage) completed.push('image-generation');
      return completed;
    },
  })),
);
