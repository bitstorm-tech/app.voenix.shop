import { Prompt } from '@/types/prompt';
import { ReactNode, createContext, useContext, useEffect, useReducer } from 'react';
import { WizardStep } from '../constants';
import { CropData, GeneratedImageCropData, MugOption, UserData, WizardState } from '../types';
import { wizardActions } from './wizardActions';
import { createInitialWizardState, wizardReducer } from './wizardReducer';

interface WizardContextValue extends WizardState {
  // Navigation actions
  goToStep: (step: WizardStep) => void;
  goNext: () => void;
  goPrevious: () => void;
  reset: () => void;

  // Image actions
  uploadImage: (file: File, url: string) => void;
  cropImage: (cropData: CropData) => void;
  removeImage: () => void;

  // Selection actions
  selectPrompt: (prompt: Prompt) => void;
  selectMug: (mug: MugOption) => void;
  setUserData: (data: UserData) => void;

  // Generation actions
  setGeneratedImages: (urls: string[]) => void;
  selectGeneratedImage: (url: string) => void;
  updateGeneratedImageCropData: (cropData: GeneratedImageCropData | null) => void;
  setProcessing: (isProcessing: boolean) => void;
  setError: (error: string | null) => void;

  // Computed values
  getCompletedSteps: () => WizardStep[];
}

const WizardContext = createContext<WizardContextValue | undefined>(undefined);

interface WizardProviderProps {
  children: ReactNode;
}

export function WizardProvider({ children }: WizardProviderProps) {
  const [state, dispatch] = useReducer(wizardReducer, createInitialWizardState());

  // Fetch prompts on mount
  useEffect(() => {
    const controller = new AbortController();
    dispatch(wizardActions.setPromptsLoading(true));
    dispatch(wizardActions.setPromptsError(null));

    fetch('/api/prompts', { signal: controller.signal })
      .then((response) => {
        if (!response.ok) {
          throw new Error('Failed to fetch prompts');
        }
        return response.json();
      })
      .then((data) => {
        dispatch(wizardActions.setPrompts(data));
        dispatch(wizardActions.setPromptsLoading(false));
      })
      .catch((error) => {
        if (error.name !== 'AbortError') {
          dispatch(wizardActions.setPromptsError(error.message));
          dispatch(wizardActions.setPromptsLoading(false));
        }
      });

    return () => {
      controller.abort();
    };
  }, [dispatch]);

  // Scroll to top when step changes
  useEffect(() => {
    window.scrollTo(0, 0);
  }, [state.currentStep]);

  // Action functions that encapsulate dispatch calls
  const goToStep = (step: WizardStep) => dispatch(wizardActions.setStep(step));
  const goNext = () => dispatch(wizardActions.goNext());
  const goPrevious = () => dispatch(wizardActions.goPrevious());
  const reset = () => dispatch(wizardActions.reset());

  const uploadImage = (file: File, url: string) => dispatch(wizardActions.uploadImage(file, url));
  const cropImage = (cropData: CropData) => dispatch(wizardActions.cropImage(cropData));
  const removeImage = () => dispatch(wizardActions.removeImage());

  const selectPrompt = (prompt: Prompt) => dispatch(wizardActions.selectPrompt(prompt));
  const selectMug = (mug: MugOption) => dispatch(wizardActions.selectMug(mug));
  const setUserData = (data: UserData) => dispatch(wizardActions.setUserData(data));

  const setGeneratedImages = (urls: string[]) => dispatch(wizardActions.setGeneratedImages(urls));
  const selectGeneratedImage = (url: string) => dispatch(wizardActions.selectGeneratedImage(url));
  const updateGeneratedImageCropData = (cropData: GeneratedImageCropData | null) => dispatch(wizardActions.updateGeneratedImageCropData(cropData));
  const setProcessing = (isProcessing: boolean) => dispatch(wizardActions.setProcessing(isProcessing));
  const setError = (error: string | null) => dispatch(wizardActions.setError(error));

  const getCompletedSteps = (): WizardStep[] => {
    const completed: WizardStep[] = [];
    if (state.uploadedImage && state.cropData) completed.push('image-upload');
    if (state.selectedPrompt) completed.push('prompt-selection');
    if (state.selectedMug) completed.push('mug-selection');
    if (state.userData) completed.push('user-data');
    if (state.selectedGeneratedImage) completed.push('image-generation');
    return completed;
  };

  const value: WizardContextValue = {
    ...state,
    // Navigation actions
    goToStep,
    goNext,
    goPrevious,
    reset,
    // Image actions
    uploadImage,
    cropImage,
    removeImage,
    // Selection actions
    selectPrompt,
    selectMug,
    setUserData,
    // Generation actions
    setGeneratedImages,
    selectGeneratedImage,
    updateGeneratedImageCropData,
    setProcessing,
    setError,
    getCompletedSteps,
  };

  return <WizardContext.Provider value={value}>{children}</WizardContext.Provider>;
}

export function useWizardContext() {
  const context = useContext(WizardContext);
  if (!context) {
    throw new Error('useWizardContext must be used within a WizardProvider');
  }
  return context;
}
