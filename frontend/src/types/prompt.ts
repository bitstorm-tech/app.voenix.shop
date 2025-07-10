import type { Slot } from './slot';

export interface PromptCategory {
  id: number;
  name: string;
  createdAt?: string;
  updatedAt?: string;
  prompts_count?: number;
}

export interface PromptSubCategory {
  id: number;
  name: string;
  promptCategoryId: number;
  description?: string;
  promptsCount: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface PromptSlot extends Slot {
  position: number;
}

export interface Prompt {
  id: number;
  title: string;
  content?: string;
  createdAt?: string;
  updatedAt?: string;
  categoryId?: number;
  category?: PromptCategory;
  subcategoryId?: number;
  subcategory?: PromptSubCategory;
  active?: boolean;
  slots?: PromptSlot[];
  exampleImageUrl?: string;
}
