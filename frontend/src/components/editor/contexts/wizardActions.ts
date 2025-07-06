import { Prompt } from '@/types/prompt';
import { WizardStep } from '../constants';
import { CropData, GeneratedImageCropData, MugOption, UserData } from '../types';

export const WIZARD_ACTIONS = {
  SET_STEP: 'SET_STEP',
  UPLOAD_IMAGE: 'UPLOAD_IMAGE',
  CROP_IMAGE: 'CROP_IMAGE',
  REMOVE_IMAGE: 'REMOVE_IMAGE',
  SELECT_PROMPT: 'SELECT_PROMPT',
  SELECT_MUG: 'SELECT_MUG',
  SET_USER_DATA: 'SET_USER_DATA',
  SET_GENERATED_IMAGES: 'SET_GENERATED_IMAGES',
  SELECT_GENERATED_IMAGE: 'SELECT_GENERATED_IMAGE',
  UPDATE_GENERATED_IMAGE_CROP_DATA: 'UPDATE_GENERATED_IMAGE_CROP_DATA',
  SET_PROCESSING: 'SET_PROCESSING',
  SET_ERROR: 'SET_ERROR',
  RESET: 'RESET',
  GO_NEXT: 'GO_NEXT',
  GO_PREVIOUS: 'GO_PREVIOUS',
  // Authentication actions
  SET_AUTHENTICATED: 'SET_AUTHENTICATED',
  SET_REGISTERING: 'SET_REGISTERING',
  SET_REGISTRATION_ERROR: 'SET_REGISTRATION_ERROR',
  // Prompts actions
  SET_PROMPTS: 'SET_PROMPTS',
  SET_PROMPTS_LOADING: 'SET_PROMPTS_LOADING',
  SET_PROMPTS_ERROR: 'SET_PROMPTS_ERROR',
} as const;

export type WizardAction =
  | { type: typeof WIZARD_ACTIONS.SET_STEP; payload: WizardStep }
  | { type: typeof WIZARD_ACTIONS.UPLOAD_IMAGE; payload: { file: File; url: string } }
  | { type: typeof WIZARD_ACTIONS.CROP_IMAGE; payload: CropData }
  | { type: typeof WIZARD_ACTIONS.REMOVE_IMAGE }
  | { type: typeof WIZARD_ACTIONS.SELECT_PROMPT; payload: Prompt }
  | { type: typeof WIZARD_ACTIONS.SELECT_MUG; payload: MugOption }
  | { type: typeof WIZARD_ACTIONS.SET_USER_DATA; payload: UserData }
  | { type: typeof WIZARD_ACTIONS.SET_GENERATED_IMAGES; payload: string[] }
  | { type: typeof WIZARD_ACTIONS.SELECT_GENERATED_IMAGE; payload: string }
  | { type: typeof WIZARD_ACTIONS.UPDATE_GENERATED_IMAGE_CROP_DATA; payload: GeneratedImageCropData | null }
  | { type: typeof WIZARD_ACTIONS.SET_PROCESSING; payload: boolean }
  | { type: typeof WIZARD_ACTIONS.SET_ERROR; payload: string | null }
  | { type: typeof WIZARD_ACTIONS.RESET }
  | { type: typeof WIZARD_ACTIONS.GO_NEXT }
  | { type: typeof WIZARD_ACTIONS.GO_PREVIOUS }
  // Authentication actions
  | { type: typeof WIZARD_ACTIONS.SET_AUTHENTICATED; payload: boolean }
  | { type: typeof WIZARD_ACTIONS.SET_REGISTERING; payload: boolean }
  | { type: typeof WIZARD_ACTIONS.SET_REGISTRATION_ERROR; payload: string | null }
  // Prompts actions
  | { type: typeof WIZARD_ACTIONS.SET_PROMPTS; payload: Prompt[] }
  | { type: typeof WIZARD_ACTIONS.SET_PROMPTS_LOADING; payload: boolean }
  | { type: typeof WIZARD_ACTIONS.SET_PROMPTS_ERROR; payload: string | null };

// Action creators for type-safe dispatch
export const wizardActions = {
  setStep: (step: WizardStep): WizardAction => ({
    type: WIZARD_ACTIONS.SET_STEP,
    payload: step,
  }),

  uploadImage: (file: File, url: string): WizardAction => ({
    type: WIZARD_ACTIONS.UPLOAD_IMAGE,
    payload: { file, url },
  }),

  cropImage: (cropData: CropData): WizardAction => ({
    type: WIZARD_ACTIONS.CROP_IMAGE,
    payload: cropData,
  }),

  removeImage: (): WizardAction => ({
    type: WIZARD_ACTIONS.REMOVE_IMAGE,
  }),

  selectPrompt: (prompt: Prompt): WizardAction => ({
    type: WIZARD_ACTIONS.SELECT_PROMPT,
    payload: prompt,
  }),

  selectMug: (mug: MugOption): WizardAction => ({
    type: WIZARD_ACTIONS.SELECT_MUG,
    payload: mug,
  }),

  setUserData: (userData: UserData): WizardAction => ({
    type: WIZARD_ACTIONS.SET_USER_DATA,
    payload: userData,
  }),

  setGeneratedImages: (urls: string[]): WizardAction => ({
    type: WIZARD_ACTIONS.SET_GENERATED_IMAGES,
    payload: urls,
  }),

  selectGeneratedImage: (url: string): WizardAction => ({
    type: WIZARD_ACTIONS.SELECT_GENERATED_IMAGE,
    payload: url,
  }),

  updateGeneratedImageCropData: (cropData: GeneratedImageCropData | null): WizardAction => ({
    type: WIZARD_ACTIONS.UPDATE_GENERATED_IMAGE_CROP_DATA,
    payload: cropData,
  }),

  setProcessing: (isProcessing: boolean): WizardAction => ({
    type: WIZARD_ACTIONS.SET_PROCESSING,
    payload: isProcessing,
  }),

  setError: (error: string | null): WizardAction => ({
    type: WIZARD_ACTIONS.SET_ERROR,
    payload: error,
  }),

  reset: (): WizardAction => ({
    type: WIZARD_ACTIONS.RESET,
  }),

  goNext: (): WizardAction => ({
    type: WIZARD_ACTIONS.GO_NEXT,
  }),

  goPrevious: (): WizardAction => ({
    type: WIZARD_ACTIONS.GO_PREVIOUS,
  }),

  // Authentication action creators
  setAuthenticated: (isAuthenticated: boolean): WizardAction => ({
    type: WIZARD_ACTIONS.SET_AUTHENTICATED,
    payload: isAuthenticated,
  }),

  setRegistering: (isRegistering: boolean): WizardAction => ({
    type: WIZARD_ACTIONS.SET_REGISTERING,
    payload: isRegistering,
  }),

  setRegistrationError: (error: string | null): WizardAction => ({
    type: WIZARD_ACTIONS.SET_REGISTRATION_ERROR,
    payload: error,
  }),

  // Prompts action creators
  setPrompts: (prompts: Prompt[]): WizardAction => ({
    type: WIZARD_ACTIONS.SET_PROMPTS,
    payload: prompts,
  }),

  setPromptsLoading: (loading: boolean): WizardAction => ({
    type: WIZARD_ACTIONS.SET_PROMPTS_LOADING,
    payload: loading,
  }),

  setPromptsError: (error: string | null): WizardAction => ({
    type: WIZARD_ACTIONS.SET_PROMPTS_ERROR,
    payload: error,
  }),
};
