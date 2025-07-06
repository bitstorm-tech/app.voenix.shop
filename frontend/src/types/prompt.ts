interface Category {
  id: number;
  name: string;
}

export interface Prompt {
  id: number;
  name: string;
  prompt: string;
  category?: Category;
  subcategory?: Category;
  example_image_url?: string;
  created_at?: string;
  updated_at?: string;
}