import { apiFetch } from '@/lib/utils';
import type { Auth } from '@/types';
import { Prompt } from '@/types/prompt';
import { ReactNode, createContext, useContext, useEffect, useReducer } from 'react';
import { WizardStep } from '../constants';
import { CropData, GeneratedImageCropData, MugOption, UserData, WizardState } from '../types';
import { wizardActions } from './wizardActions';
import { createInitialWizardState, wizardReducer } from './wizardReducer';

interface WizardContextValue extends WizardState {
  // Additional context-only values
  auth: Auth;

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

  // Complex handlers (keep these as they contain business logic)
  handleNext: () => Promise<void>;
  handleUserRegistration: () => Promise<boolean>;

  // Computed values
  getCompletedSteps: () => WizardStep[];
}

const WizardContext = createContext<WizardContextValue | undefined>(undefined);

interface WizardProviderProps {
  children: ReactNode;
  auth: Auth;
}

export function WizardProvider({ children, auth }: WizardProviderProps) {
  const [state, dispatch] = useReducer(wizardReducer, createInitialWizardState(!!auth.user));

  // Fetch prompts on mount
  useEffect(() => {
    const controller = new AbortController();
    dispatch(wizardActions.setPromptsLoading(true));
    dispatch(wizardActions.setPromptsError(null));

    apiFetch('/api/prompts', { signal: controller.signal })
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

  // If user is not authenticated and tries to access image generation step, redirect back
  useEffect(() => {
    if (!state.isAuthenticated && state.currentStep === 'image-generation') {
      dispatch(wizardActions.setStep('user-data'));
    }
  }, [state.currentStep, state.isAuthenticated]);

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

  // Complex handlers with business logic
  const handleUserRegistration = async (): Promise<boolean> => {
    if (state.currentStep !== 'user-data' || !state.userData) {
      return true; // Not on user data step, proceed normally
    }

    dispatch(wizardActions.setRegistering(true));
    dispatch(wizardActions.setRegistrationError(null));

    try {
      const response = await apiFetch('/api/register-or-login', {
        method: 'POST',
        body: JSON.stringify({
          email: state.userData.email,
          first_name: state.userData.firstName || null,
          last_name: state.userData.lastName || null,
          phone_number: state.userData.phoneNumber || null,
        }),
      });

      if (!response.ok) {
        const error = await response.json();
        throw new Error(error.message || 'Registration failed');
      }

      const data = await response.json();
      dispatch(wizardActions.setAuthenticated(data.authenticated));

      return data.authenticated;
    } catch (error) {
      dispatch(wizardActions.setRegistrationError(error instanceof Error ? error.message : 'An error occurred during registration'));
      return false; // Error, prevent navigation
    } finally {
      dispatch(wizardActions.setRegistering(false));
    }
  };

  const handleNext = async () => {
    // Only handle registration if on user-data step and not authenticated
    if (state.currentStep === 'user-data' && !state.isAuthenticated) {
      const success = await handleUserRegistration();
      if (success) {
        goNext();
      }
    } else {
      goNext();
    }
  };

  const getCompletedSteps = (): WizardStep[] => {
    const completed: WizardStep[] = [];
    if (state.uploadedImage && state.cropData) completed.push('image-upload');
    if (state.selectedPrompt) completed.push('prompt-selection');
    if (state.selectedMug) completed.push('mug-selection');
    // Mark user-data as completed if user is authenticated OR if userData is filled
    if (state.isAuthenticated || state.userData) completed.push('user-data');
    if (state.selectedGeneratedImage) completed.push('image-generation');
    return completed;
  };

  const value: WizardContextValue = {
    ...state,
    auth,
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
    // Complex handlers
    handleNext,
    handleUserRegistration,
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
