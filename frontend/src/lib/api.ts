import type { LoginRequest, LoginResponse, SessionInfo } from '@/types/auth';
import type { Mug, MugCategory, MugSubCategory, MugVariant } from '@/types/mug';
import type { Prompt, PromptCategory, PromptSubCategory } from '@/types/prompt';
import type { Slot, SlotType } from '@/types/slot';

export class ApiError extends Error {
  constructor(
    public status: number,
    message: string,
  ) {
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
    const response = await fetch(`/api${endpoint}`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      },
      credentials: 'include',
    });
    return handleResponse<T>(response);
  },

  post: async <T>(endpoint: string, data: any): Promise<T> => {
    const response = await fetch(`/api${endpoint}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      credentials: 'include',
      body: JSON.stringify(data),
    });
    return handleResponse<T>(response);
  },

  put: async <T>(endpoint: string, data: any): Promise<T> => {
    const response = await fetch(`/api${endpoint}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
      },
      credentials: 'include',
      body: JSON.stringify(data),
    });
    return handleResponse<T>(response);
  },

  delete: async <T>(endpoint: string): Promise<T> => {
    const response = await fetch(`/api${endpoint}`, {
      method: 'DELETE',
      headers: {
        'Content-Type': 'application/json',
      },
      credentials: 'include',
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
  // Slot management endpoints
  addSlots: (id: number, slotIds: number[]) => api.post<Prompt>(`/prompts/${id}/slots`, { slotIds }),
  updateSlots: (id: number, slots: PromptSlotUpdate[]) => api.put<Prompt>(`/prompts/${id}/slots`, { slots }),
  removeSlot: (id: number, slotId: number) => api.delete<void>(`/prompts/${id}/slots/${slotId}`),
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

// Prompt SubCategory API endpoints
export const promptSubCategoriesApi = {
  getAll: () => api.get<PromptSubCategory[]>('/prompt-subcategories'),
  getById: (id: number) => api.get<PromptSubCategory>(`/prompt-subcategories/${id}`),
  getByCategory: (categoryId: number) => api.get<PromptSubCategory[]>(`/prompt-subcategories/by-category/${categoryId}`),
  create: (data: CreatePromptSubCategoryRequest) => api.post<PromptSubCategory>('/prompt-subcategories', data),
  update: (id: number, data: UpdatePromptSubCategoryRequest) => api.put<PromptSubCategory>(`/prompt-subcategories/${id}`, data),
  delete: (id: number) => api.delete<void>(`/prompt-subcategories/${id}`),
  search: (name: string) => api.get<PromptSubCategory[]>(`/prompt-subcategories/search?name=${encodeURIComponent(name)}`),
};

// Type definitions for API requests
export interface PromptSlotUpdate {
  slotId: number;
}

export interface CreatePromptRequest {
  title: string;
  content?: string;
  categoryId: number;
  subcategoryId?: number;
  active: boolean;
  slots?: PromptSlotUpdate[];
  exampleImageFilename?: string;
}

export interface UpdatePromptRequest {
  title?: string;
  content?: string;
  categoryId?: number;
  subcategoryId?: number;
  active?: boolean;
  slots?: PromptSlotUpdate[];
  exampleImageFilename?: string;
}

export interface CreatePromptCategoryRequest {
  name: string;
}

export interface UpdatePromptCategoryRequest {
  name?: string;
}

export interface CreatePromptSubCategoryRequest {
  promptCategoryId: number;
  name: string;
  description?: string;
}

export interface UpdatePromptSubCategoryRequest {
  promptCategoryId?: number;
  name?: string;
  description?: string;
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
  getByPriceRange: (minPrice: number, maxPrice: number) => api.get<Mug[]>(`/mugs/price-range?minPrice=${minPrice}&maxPrice=${maxPrice}`),
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
  getAll: () => api.get<MugSubCategory[]>('/mug-sub-categories'),
  getById: (id: number) => api.get<MugSubCategory>(`/mug-sub-categories/${id}`),
  getByCategoryId: (categoryId: number) => api.get<MugSubCategory[]>(`/mug-sub-categories/category/${categoryId}`),
  create: (data: CreateMugSubCategoryRequest) => api.post<MugSubCategory>('/mug-sub-categories', data),
  update: (id: number, data: UpdateMugSubCategoryRequest) => api.put<MugSubCategory>(`/mug-sub-categories/${id}`, data),
  delete: (id: number) => api.delete<void>(`/mug-sub-categories/${id}`),
  search: (name: string) => api.get<MugSubCategory[]>(`/mug-sub-categories/search?name=${encodeURIComponent(name)}`),
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
  mugCategoryId: number;
  description?: string;
}

export interface UpdateMugSubCategoryRequest {
  name?: string;
  mugCategoryId?: number;
  description?: string;
}

// Mug Variant API endpoints
export const mugVariantsApi = {
  getAll: () => api.get<MugVariant[]>('/mug-variants'),
  getById: (id: number) => api.get<MugVariant>(`/mug-variants/${id}`),
  getByMugId: (mugId: number) => api.get<MugVariant[]>(`/mugs/${mugId}/variants`),
  create: (data: CreateMugVariantRequest) => api.post<MugVariant>('/mug-variants', data),
  update: (id: number, data: UpdateMugVariantRequest) => api.put<MugVariant>(`/mug-variants/${id}`, data),
  delete: (id: number) => api.delete<void>(`/mug-variants/${id}`),
};

// Type definitions for Mug Variant API requests
export interface CreateMugVariantRequest {
  mugId: number;
  colorCode: string;
  exampleImageFilename: string;
}

export interface UpdateMugVariantRequest {
  colorCode?: string;
  exampleImageFilename?: string;
}

// Slot Type API endpoints
export const slotTypesApi = {
  getAll: () => api.get<SlotType[]>('/slot-types'),
  getById: (id: number) => api.get<SlotType>(`/slot-types/${id}`),
  create: (data: CreateSlotTypeRequest) => api.post<SlotType>('/slot-types', data),
  update: (id: number, data: UpdateSlotTypeRequest) => api.put<SlotType>(`/slot-types/${id}`, data),
  delete: (id: number) => api.delete<void>(`/slot-types/${id}`),
  updatePositions: async (positions: { id: number; position: number }[]) => {
    // Get all current slot types to find max position
    const allSlotTypes = await api.get<SlotType[]>('/slot-types');
    const maxPosition = Math.max(...allSlotTypes.map((st) => st.position), ...positions.map((p) => p.position));

    // Phase 1: Move all items to temporary positions (beyond max)
    const tempOffset = maxPosition + 10;
    for (const update of positions) {
      await api.put<SlotType>(`/slot-types/${update.id}`, { position: update.position + tempOffset });
    }

    // Phase 2: Move items to their final positions
    for (const update of positions) {
      await api.put<SlotType>(`/slot-types/${update.id}`, { position: update.position });
    }

    return positions;
  },
};

// Type definitions for Slot Type API requests
export interface CreateSlotTypeRequest {
  name: string;
  position?: number;
}

export interface UpdateSlotTypeRequest {
  name?: string;
  position?: number;
}

export interface UpdateSlotTypePositionsRequest {
  positions: { id: number; position: number }[];
}

// Slot API endpoints
export const slotsApi = {
  getAll: () => api.get<Slot[]>('/slots'),
  getById: (id: number) => api.get<Slot>(`/slots/${id}`),
  create: (data: CreateSlotRequest) => api.post<Slot>('/slots', data),
  update: (id: number, data: UpdateSlotRequest) => api.put<Slot>(`/slots/${id}`, data),
  delete: (id: number) => api.delete<void>(`/slots/${id}`),
  search: (name: string) => api.get<Slot[]>(`/slots/search?name=${encodeURIComponent(name)}`),
};

// Type definitions for Slot API requests
export interface CreateSlotRequest {
  slotTypeId: number;
  name: string;
  prompt: string;
  description?: string;
  exampleImageFilename?: string;
}

export interface UpdateSlotRequest {
  slotTypeId?: number;
  name?: string;
  prompt?: string;
  description?: string;
  exampleImageFilename?: string;
}

// Image API endpoints
export const imagesApi = {
  upload: async (file: File, imageType: 'PUBLIC' | 'PRIVATE' | 'PROMPT_EXAMPLE' | 'SLOT_EXAMPLE') => {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('imageType', imageType);

    const response = await fetch('/api/images', {
      method: 'POST',
      credentials: 'include',
      body: formData,
    });
    return handleResponse<{ filename: string; imageType: string }>(response);
  },
  delete: async (filename: string) => api.delete<void>(`/images/${filename}`),
};

// Authentication API endpoints
export const authApi = {
  login: (data: LoginRequest) => api.post<LoginResponse>('/public/auth/login', data),
  logout: () => api.post<void>('/public/auth/logout', {}),
  checkSession: () => api.get<SessionInfo>('/public/auth/session'),
};
