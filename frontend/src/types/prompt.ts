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
  category_id?: number;
  category?: PromptCategory;
  prompts_count?: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface Prompt {
  id: number;
  title: string;
  content?: string;
  createdAt?: string;
  updatedAt?: string;
  category_id?: number;
  category?: PromptCategory;
  active?: boolean;
}
