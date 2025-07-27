import { lazy } from 'react';

// Lazy load all wizard steps
export const ImageUploadStep = lazy(() => import('./components/steps/1-ImageUploadStep'));
export const PromptSelectionStep = lazy(() => import('./components/steps/2-PromptSelectionStep'));
export const MugSelectionStep = lazy(() => import('./components/steps/3-MugSelectionStep'));
export const UserDataStep = lazy(() => import('./components/steps/4-UserDataStep'));
export const ImageGenerationStep = lazy(() => import('./components/steps/5-ImageGenerationStep'));
export const PreviewStep = lazy(() => import('./components/steps/6-PreviewStep'));
