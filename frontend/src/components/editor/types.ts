import { Prompt } from '@/types/prompt';
import { WizardStep } from './constants';

export interface MugVariant {
  id: number;
  mugId: number;
  colorCode: string;
  exampleImageUrl: string | null;
  supplierArticleNumber: string | null;
  isDefault: boolean;
  exampleImageFilename: string | null;
}

export interface MugOption {
  id: number;
  name: string;
  price: number;
  image: string;
  capacity: string;
  special?: string;
  description_short?: string;
  description_long?: string;
  height_mm?: number;
  diameter_mm?: number;
  print_template_width_mm?: number;
  print_template_height_mm?: number;
  filling_quantity?: string;
  dishwasher_safe?: boolean;
  variants?: MugVariant[];
}

export interface CropData {
  // Crop position and zoom
  crop: { x: number; y: number };
  zoom: number;
  // The actual cropped area in pixels
  croppedAreaPixels: {
    x: number;
    y: number;
    width: number;
    height: number;
  };
}

export interface GeneratedImageCropData {
  // Crop position and zoom
  crop: { x: number; y: number };
  zoom: number;
  // The actual cropped area in pixels
  croppedAreaPixels: {
    x: number;
    y: number;
    width: number;
    height: number;
  };
}

export interface UserData {
  email: string;
  firstName?: string;
  lastName?: string;
  phoneNumber?: string;
}

export interface WizardState {
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
  prompts: Prompt[];
  promptsLoading: boolean;
  promptsError: string | null;
  // Navigation state
  canGoNext: boolean;
  canGoPrevious: boolean;
}
