import type { Prompt, PromptCategory } from '@/types/prompt';
import type { Mug, MugCategory, MugSubCategory } from '@/types/mug';

const API_BASE_URL = 'http://localhost:8080/api';

class ApiError extends Error {
  constructor(public status: number, message: string) {
    super(message);
    this.name = 'ApiError';
  }
}

async function handleResponse<T>(response: Response): Promise<T> {
  if (!response.ok) {
    const errorData = await response.json().catch(() => ({ message: 'An error occurred' }));
    throw new ApiError(response.status, errorData.message || `HTTP error! status: ${response.status}`);
  }
  
  if (response.status === 204) {
    return {} as T;
  }
  
  return response.json();
}

export const api = {
  get: async <T>(endpoint: string): Promise<T> => {
    const response = await fetch(`${API_BASE_URL}${endpoint}`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      },
    });
    return handleResponse<T>(response);
  },

  post: async <T>(endpoint: string, data: any): Promise<T> => {
    const response = await fetch(`${API_BASE_URL}${endpoint}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(data),
    });
    return handleResponse<T>(response);
  },

  put: async <T>(endpoint: string, data: any): Promise<T> => {
    const response = await fetch(`${API_BASE_URL}${endpoint}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(data),
    });
    return handleResponse<T>(response);
  },

  delete: async <T>(endpoint: string): Promise<T> => {
    const response = await fetch(`${API_BASE_URL}${endpoint}`, {
      method: 'DELETE',
      headers: {
        'Content-Type': 'application/json',
      },
    });
    return handleResponse<T>(response);
  },
};

// Prompt API endpoints
export const promptsApi = {
  getAll: () => api.get<Prompt[]>('/prompts'),
  getById: (id: number) => api.get<Prompt>(`/prompts/${id}`),
  create: (data: CreatePromptRequest) => api.post<Prompt>('/prompts', data),
  update: (id: number, data: UpdatePromptRequest) => api.put<Prompt>(`/prompts/${id}`, data),
  delete: (id: number) => api.delete<void>(`/prompts/${id}`),
  search: (title: string) => api.get<Prompt[]>(`/prompts/search?title=${encodeURIComponent(title)}`),
};

// Prompt Category API endpoints
export const promptCategoriesApi = {
  getAll: () => api.get<PromptCategory[]>('/prompt-categories'),
  getById: (id: number) => api.get<PromptCategory>(`/prompt-categories/${id}`),
  create: (data: CreatePromptCategoryRequest) => api.post<PromptCategory>('/prompt-categories', data),
  update: (id: number, data: UpdatePromptCategoryRequest) => api.put<PromptCategory>(`/prompt-categories/${id}`, data),
  delete: (id: number) => api.delete<void>(`/prompt-categories/${id}`),
  search: (name: string) => api.get<PromptCategory[]>(`/prompt-categories/search?name=${encodeURIComponent(name)}`),
};

// Type definitions for API requests
export interface CreatePromptRequest {
  title: string;
  content: string;
  categoryId: number;
  active: boolean;
}

export interface UpdatePromptRequest {
  title: string;
  content: string;
  categoryId: number;
  active: boolean;
}

export interface CreatePromptCategoryRequest {
  name: string;
}

export interface UpdatePromptCategoryRequest {
  name?: string;
}

// Mug API endpoints
export const mugsApi = {
  getAll: () => api.get<Mug[]>('/mugs'),
  getActive: () => api.get<Mug[]>('/mugs/active'),
  getById: (id: number) => api.get<Mug>(`/mugs/${id}`),
  create: (data: CreateMugRequest) => api.post<Mug>('/mugs', data),
  update: (id: number, data: UpdateMugRequest) => api.put<Mug>(`/mugs/${id}`, data),
  delete: (id: number) => api.delete<void>(`/mugs/${id}`),
  search: (name: string) => api.get<Mug[]>(`/mugs/search?name=${encodeURIComponent(name)}`),
  getByPriceRange: (minPrice: number, maxPrice: number) => 
    api.get<Mug[]>(`/mugs/price-range?minPrice=${minPrice}&maxPrice=${maxPrice}`),
};

// Mug Category API endpoints
export const mugCategoriesApi = {
  getAll: () => api.get<MugCategory[]>('/mug-categories'),
  getById: (id: number) => api.get<MugCategory>(`/mug-categories/${id}`),
  create: (data: CreateMugCategoryRequest) => api.post<MugCategory>('/mug-categories', data),
  update: (id: number, data: UpdateMugCategoryRequest) => api.put<MugCategory>(`/mug-categories/${id}`, data),
  delete: (id: number) => api.delete<void>(`/mug-categories/${id}`),
  search: (name: string) => api.get<MugCategory[]>(`/mug-categories/search?name=${encodeURIComponent(name)}`),
};

// Mug SubCategory API endpoints
export const mugSubCategoriesApi = {
  getAll: () => api.get<MugSubCategory[]>('/mug-subcategories'),
  getById: (id: number) => api.get<MugSubCategory>(`/mug-subcategories/${id}`),
  getByCategoryId: (categoryId: number) => api.get<MugSubCategory[]>(`/mug-subcategories/by-category/${categoryId}`),
  create: (data: CreateMugSubCategoryRequest) => api.post<MugSubCategory>('/mug-subcategories', data),
  update: (id: number, data: UpdateMugSubCategoryRequest) => api.put<MugSubCategory>(`/mug-subcategories/${id}`, data),
  delete: (id: number) => api.delete<void>(`/mug-subcategories/${id}`),
  search: (name: string) => api.get<MugSubCategory[]>(`/mug-subcategories/search?name=${encodeURIComponent(name)}`),
};

// Type definitions for Mug API requests
export interface CreateMugRequest {
  name: string;
  descriptionLong: string;
  descriptionShort: string;
  image: string;
  price: number;
  heightMm: number;
  diameterMm: number;
  printTemplateWidthMm: number;
  printTemplateHeightMm: number;
  fillingQuantity?: string;
  dishwasherSafe: boolean;
  active: boolean;
  categoryId?: number;
  subCategoryId?: number;
}

export interface UpdateMugRequest {
  name?: string;
  descriptionLong?: string;
  descriptionShort?: string;
  image?: string;
  price?: number;
  heightMm?: number;
  diameterMm?: number;
  printTemplateWidthMm?: number;
  printTemplateHeightMm?: number;
  fillingQuantity?: string;
  dishwasherSafe?: boolean;
  active?: boolean;
  categoryId?: number;
  subCategoryId?: number;
}

export interface CreateMugCategoryRequest {
  name: string;
  description?: string;
}

export interface UpdateMugCategoryRequest {
  name?: string;
  description?: string;
}

export interface CreateMugSubCategoryRequest {
  name: string;
  categoryId: number;
  description?: string;
}

export interface UpdateMugSubCategoryRequest {
  name?: string;
  categoryId?: number;
  description?: string;
}

