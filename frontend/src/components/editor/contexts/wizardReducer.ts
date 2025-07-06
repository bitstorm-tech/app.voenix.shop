import { WIZARD_STEPS, WizardStep } from '../constants';
import { WizardState } from '../types';
import { WIZARD_ACTIONS, WizardAction } from './wizardActions';

export function createInitialWizardState(isAuthenticated: boolean): WizardState {
  const state = {
    currentStep: 'image-upload' as WizardStep,
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
    // Authentication state
    isAuthenticated,
    isRegistering: false,
    registrationError: null,
    // Prompts state
    prompts: [],
    promptsLoading: true,
    promptsError: null,
    // Navigation state - will be calculated
    canGoNext: false,
    canGoPrevious: false,
  };

  // Calculate navigation state
  state.canGoNext = canProceedFromStep(state, state.currentStep);
  state.canGoPrevious = state.currentStep !== 'image-upload';

  return state;
}

export const initialWizardState = createInitialWizardState(false);

export function canProceedFromStep(state: WizardState, step: WizardStep): boolean {
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

// Update navigation state after any state change
function updateNavigationState(state: WizardState): WizardState {
  return {
    ...state,
    canGoNext: canProceedFromStep(state, state.currentStep),
    canGoPrevious: state.currentStep !== 'image-upload',
  };
}

function getNextStep(currentStep: WizardStep, skipUserData: boolean): WizardStep | null {
  const currentIndex = WIZARD_STEPS.indexOf(currentStep);
  if (currentIndex === -1 || currentIndex === WIZARD_STEPS.length - 1) return null;

  let nextStep = WIZARD_STEPS[currentIndex + 1];
  if (nextStep === 'user-data' && skipUserData) {
    nextStep = 'image-generation';
  }

  return nextStep;
}

function getPreviousStep(currentStep: WizardStep, skipUserData: boolean): WizardStep | null {
  const currentIndex = WIZARD_STEPS.indexOf(currentStep);
  if (currentIndex <= 0) return null;

  let previousStep = WIZARD_STEPS[currentIndex - 1];
  if (previousStep === 'user-data' && skipUserData) {
    previousStep = 'mug-selection';
  }

  return previousStep;
}

export function wizardReducer(state: WizardState, action: WizardAction): WizardState {
  let newState: WizardState;

  switch (action.type) {
    case WIZARD_ACTIONS.SET_STEP:
      newState = { ...state, currentStep: action.payload };
      break;

    case WIZARD_ACTIONS.UPLOAD_IMAGE:
      newState = {
        ...state,
        uploadedImage: action.payload.file,
        uploadedImageUrl: action.payload.url,
        cropData: null, // Reset crop data when new image is uploaded
      };
      break;

    case WIZARD_ACTIONS.CROP_IMAGE:
      newState = { ...state, cropData: action.payload };
      break;

    case WIZARD_ACTIONS.REMOVE_IMAGE:
      // Clean up the object URL if it exists
      if (state.uploadedImageUrl) {
        URL.revokeObjectURL(state.uploadedImageUrl);
      }
      newState = {
        ...state,
        uploadedImage: null,
        uploadedImageUrl: null,
        cropData: null,
      };
      break;

    case WIZARD_ACTIONS.SELECT_PROMPT:
      newState = { ...state, selectedPrompt: action.payload };
      break;

    case WIZARD_ACTIONS.SELECT_MUG:
      newState = { ...state, selectedMug: action.payload };
      break;

    case WIZARD_ACTIONS.SET_USER_DATA:
      newState = { ...state, userData: action.payload };
      break;

    case WIZARD_ACTIONS.SET_GENERATED_IMAGES:
      newState = {
        ...state,
        generatedImageUrls: action.payload,
        selectedGeneratedImage: null, // Reset selection when new images are generated
        generatedImageCropData: null, // Reset crop data when new images are generated
      };
      break;

    case WIZARD_ACTIONS.SELECT_GENERATED_IMAGE:
      newState = {
        ...state,
        selectedGeneratedImage: action.payload,
        generatedImageCropData: null, // Reset crop data when selecting a different image
      };
      break;

    case WIZARD_ACTIONS.UPDATE_GENERATED_IMAGE_CROP_DATA:
      newState = { ...state, generatedImageCropData: action.payload };
      break;

    case WIZARD_ACTIONS.SET_PROCESSING:
      newState = { ...state, isProcessing: action.payload };
      break;

    case WIZARD_ACTIONS.SET_ERROR:
      newState = { ...state, error: action.payload };
      break;

    case WIZARD_ACTIONS.RESET:
      // Clean up the object URL if it exists
      if (state.uploadedImageUrl) {
        URL.revokeObjectURL(state.uploadedImageUrl);
      }
      newState = createInitialWizardState(state.isAuthenticated);
      break;

    case WIZARD_ACTIONS.GO_NEXT: {
      if (!canProceedFromStep(state, state.currentStep)) {
        return state; // Can't proceed, return unchanged
      }

      const nextStep = getNextStep(state.currentStep, state.isAuthenticated);
      if (!nextStep) return state;

      newState = { ...state, currentStep: nextStep };
      break;
    }

    case WIZARD_ACTIONS.GO_PREVIOUS: {
      const previousStep = getPreviousStep(state.currentStep, state.isAuthenticated);
      if (!previousStep) return state;

      newState = { ...state, currentStep: previousStep };
      break;
    }

    // Authentication actions
    case WIZARD_ACTIONS.SET_AUTHENTICATED:
      newState = { ...state, isAuthenticated: action.payload };
      break;

    case WIZARD_ACTIONS.SET_REGISTERING:
      newState = { ...state, isRegistering: action.payload };
      break;

    case WIZARD_ACTIONS.SET_REGISTRATION_ERROR:
      newState = { ...state, registrationError: action.payload };
      break;

    // Prompts actions
    case WIZARD_ACTIONS.SET_PROMPTS:
      newState = { ...state, prompts: action.payload };
      break;

    case WIZARD_ACTIONS.SET_PROMPTS_LOADING:
      newState = { ...state, promptsLoading: action.payload };
      break;

    case WIZARD_ACTIONS.SET_PROMPTS_ERROR:
      newState = { ...state, promptsError: action.payload };
      break;

    default: {
      // Exhaustive check to ensure all actions are handled
      const exhaustiveCheck: never = action;
      throw new Error(`Unhandled action type: ${JSON.stringify(exhaustiveCheck)}`);
    }
  }

  // Update navigation state after any state change
  return updateNavigationState(newState);
}
