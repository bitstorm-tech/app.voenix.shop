import type { PromptSlotVariant } from './promptSlotVariant';

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

export type PromptSlot = PromptSlotVariant;

export interface Prompt {
  id: number;
  title: string;
  promptText?: string;
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
