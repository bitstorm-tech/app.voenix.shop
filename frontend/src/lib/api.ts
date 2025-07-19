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
  getAll: () => api.get<Prompt[]>('/admin/prompts'),
  getById: (id: number) => api.get<Prompt>(`/admin/prompts/${id}`),
  create: (data: CreatePromptRequest) => api.post<Prompt>('/admin/prompts', data),
  update: (id: number, data: UpdatePromptRequest) => api.put<Prompt>(`/admin/prompts/${id}`, data),
  delete: (id: number) => api.delete<void>(`/admin/prompts/${id}`),
  search: (title: string) => api.get<Prompt[]>(`/admin/prompts/search?title=${encodeURIComponent(title)}`),
  // Slot management endpoints
  addSlots: (id: number, slotIds: number[]) => api.post<Prompt>(`/admin/prompts/${id}/slots`, { slotIds }),
  updateSlots: (id: number, slots: PromptSlotUpdate[]) => api.put<Prompt>(`/admin/prompts/${id}/slots`, { slots }),
};

// Prompt Category API endpoints
export const promptCategoriesApi = {
  getAll: () => api.get<PromptCategory[]>('/admin/prompts/categories'),
  getById: (id: number) => api.get<PromptCategory>(`/admin/prompts/categories/${id}`),
  create: (data: CreatePromptCategoryRequest) => api.post<PromptCategory>('/admin/prompts/categories', data),
  update: (id: number, data: UpdatePromptCategoryRequest) => api.put<PromptCategory>(`/admin/prompts/categories/${id}`, data),
  delete: (id: number) => api.delete<void>(`/admin/prompts/categories/${id}`),
  search: (name: string) => api.get<PromptCategory[]>(`/admin/prompts/categories/search?name=${encodeURIComponent(name)}`),
};

// Prompt SubCategory API endpoints
export const promptSubCategoriesApi = {
  getAll: () => api.get<PromptSubCategory[]>('/admin/prompts/subcategories'),
  getById: (id: number) => api.get<PromptSubCategory>(`/admin/prompts/subcategories/${id}`),
  getByCategory: (categoryId: number) => api.get<PromptSubCategory[]>(`/admin/prompts/subcategories/category/${categoryId}`),
  create: (data: CreatePromptSubCategoryRequest) => api.post<PromptSubCategory>('/admin/prompts/subcategories', data),
  update: (id: number, data: UpdatePromptSubCategoryRequest) => api.put<PromptSubCategory>(`/admin/prompts/subcategories/${id}`, data),
  delete: (id: number) => api.delete<void>(`/admin/prompts/subcategories/${id}`),
  search: (name: string) => api.get<PromptSubCategory[]>(`/admin/prompts/subcategories/search?name=${encodeURIComponent(name)}`),
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
  getAll: () => api.get<Mug[]>('/admin/mugs'),
  getById: (id: number) => api.get<Mug>(`/admin/mugs/${id}`),
  create: (data: CreateMugRequest) => api.post<Mug>('/admin/mugs', data),
  update: (id: number, data: UpdateMugRequest) => api.put<Mug>(`/admin/mugs/${id}`, data),
  delete: (id: number) => api.delete<void>(`/admin/mugs/${id}`),
  search: (name: string) => api.get<Mug[]>(`/admin/mugs/search?name=${encodeURIComponent(name)}`),
  getByPriceRange: (minPrice: number, maxPrice: number) => api.get<Mug[]>(`/admin/mugs/price-range?minPrice=${minPrice}&maxPrice=${maxPrice}`),
};

// Mug Category API endpoints
export const mugCategoriesApi = {
  getAll: () => api.get<MugCategory[]>('/admin/mugs/categories'),
  getById: (id: number) => api.get<MugCategory>(`/admin/mugs/categories/${id}`),
  create: (data: CreateMugCategoryRequest) => api.post<MugCategory>('/admin/mugs/categories', data),
  update: (id: number, data: UpdateMugCategoryRequest) => api.put<MugCategory>(`/admin/mugs/categories/${id}`, data),
  delete: (id: number) => api.delete<void>(`/admin/mugs/categories/${id}`),
  search: (name: string) => api.get<MugCategory[]>(`/admin/mugs/categories/search?name=${encodeURIComponent(name)}`),
};

// Mug SubCategory API endpoints
export const mugSubCategoriesApi = {
  getAll: () => api.get<MugSubCategory[]>('/admin/mugs/subcategories'),
  getById: (id: number) => api.get<MugSubCategory>(`/admin/mugs/subcategories/${id}`),
  getByCategoryId: (categoryId: number) => api.get<MugSubCategory[]>(`/admin/mugs/subcategories/category/${categoryId}`),
  create: (data: CreateMugSubCategoryRequest) => api.post<MugSubCategory>('/admin/mugs/subcategories', data),
  update: (id: number, data: UpdateMugSubCategoryRequest) => api.put<MugSubCategory>(`/admin/mugs/subcategories/${id}`, data),
  delete: (id: number) => api.delete<void>(`/admin/mugs/subcategories/${id}`),
  search: (name: string) => api.get<MugSubCategory[]>(`/admin/mugs/subcategories/search?name=${encodeURIComponent(name)}`),
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
  getAll: () => api.get<MugVariant[]>('/admin/mugs/variants'),
  getById: (id: number) => api.get<MugVariant>(`/admin/mugs/variants/${id}`),
  getByMugId: (mugId: number) => api.get<MugVariant[]>(`/admin/mugs/variants/mug/${mugId}`),
  create: (data: CreateMugVariantRequest) => api.post<MugVariant>('/admin/mugs/variants', data),
  update: (id: number, data: UpdateMugVariantRequest) => api.put<MugVariant>(`/admin/mugs/variants/${id}`, data),
  delete: (id: number) => api.delete<void>(`/admin/mugs/variants/${id}`),
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
  getAll: () => api.get<SlotType[]>('/admin/prompts/slot-types'),
  getById: (id: number) => api.get<SlotType>(`/admin/prompts/slot-types/${id}`),
  create: (data: CreateSlotTypeRequest) => api.post<SlotType>('/admin/prompts/slot-types', data),
  update: (id: number, data: UpdateSlotTypeRequest) => api.put<SlotType>(`/admin/prompts/slot-types/${id}`, data),
  delete: (id: number) => api.delete<void>(`/admin/prompts/slot-types/${id}`),
  updatePositions: async (positions: { id: number; position: number }[]) => {
    // Get all current slot types to find max position
    const allSlotTypes = await api.get<SlotType[]>('/admin/prompts/slot-types');
    const maxPosition = Math.max(...allSlotTypes.map((st) => st.position), ...positions.map((p) => p.position));

    // Phase 1: Move all items to temporary positions (beyond max)
    const tempOffset = maxPosition + 10;
    for (const update of positions) {
      await api.put<SlotType>(`/admin/prompts/slot-types/${update.id}`, { position: update.position + tempOffset });
    }

    // Phase 2: Move items to their final positions
    for (const update of positions) {
      await api.put<SlotType>(`/admin/prompts/slot-types/${update.id}`, { position: update.position });
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
  getAll: () => api.get<Slot[]>('/admin/prompts/slots'),
  getById: (id: number) => api.get<Slot>(`/admin/prompts/slots/${id}`),
  getByTypeId: (typeId: number) => api.get<Slot[]>(`/admin/prompts/slots/type/${typeId}`),
  create: (data: CreateSlotRequest) => api.post<Slot>('/admin/prompts/slots', data),
  update: (id: number, data: UpdateSlotRequest) => api.put<Slot>(`/admin/prompts/slots/${id}`, data),
  delete: (id: number) => api.delete<void>(`/admin/prompts/slots/${id}`),
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

    const response = await fetch('/api/admin/images', {
      method: 'POST',
      credentials: 'include',
      body: formData,
    });
    return handleResponse<{ filename: string; imageType: string }>(response);
  },
  download: (filename: string) => api.get<Blob>(`/admin/images/${filename}/download`),
  delete: async (filename: string) => api.delete<void>(`/admin/images/${filename}`),
};

// Authentication API endpoints
export const authApi = {
  login: (data: LoginRequest) => api.post<LoginResponse>('/auth/login', data),
  logout: () => api.post<void>('/auth/logout', {}),
  checkSession: () => api.get<SessionInfo>('/auth/session'),
};

// User API endpoints
export const userApi = {
  getProfile: () => api.get<UserDto>('/user/profile'),
  updateProfile: (data: UpdateUserRequest) => api.put<UserDto>('/user/profile', data),
  getSession: () => api.get<SessionInfo>('/user/session'),
  logout: () => api.post<void>('/user/logout', {}),
  deleteAccount: () => api.delete<void>('/user/account'),
};

// Admin User API endpoints
export const adminUsersApi = {
  getAll: () => api.get<UserDto[]>('/admin/users'),
  getById: (id: number) => api.get<UserDto>(`/admin/users/${id}`),
  create: (data: CreateUserRequest) => api.post<UserDto>('/admin/users', data),
  update: (id: number, data: UpdateUserRequest) => api.put<UserDto>(`/admin/users/${id}`, data),
  delete: (id: number) => api.delete<void>(`/admin/users/${id}`),
};

// Service API endpoints
export const servicesApi = {
  // OpenAI Image Edit
  editImage: async (image: File, mask: File, prompt: string, size: string, quality: string, background: string) => {
    const formData = new FormData();
    formData.append('image', image);
    formData.append('mask', mask);
    formData.append('prompt', prompt);
    formData.append('size', size);
    formData.append('quality', quality);
    formData.append('background', background);

    const response = await fetch('/api/admin/openai/image-edit', {
      method: 'POST',
      credentials: 'include',
      body: formData,
    });
    return handleResponse<ImageEditResponse>(response);
  },
  // PDF Generation
  generatePdf: (data: GeneratePdfRequest) => api.post<PdfResponse>('/admin/pdf/generate', data),
};

// Type definitions for new API requests
export interface UserDto {
  id: number;
  email: string;
  firstName?: string;
  lastName?: string;
  roles: string[];
  createdAt: string;
  updatedAt: string;
}

export interface CreateUserRequest {
  email: string;
  password: string;
  firstName?: string;
  lastName?: string;
}

export interface UpdateUserRequest {
  email?: string;
  password?: string;
  firstName?: string;
  lastName?: string;
}

export interface ImageEditResponse {
  imageUrl: string;
  filename: string;
}

export interface GeneratePdfRequest {
  content: string;
  size: 'A4' | 'A3' | 'LETTER' | 'LEGAL';
}

export interface PdfResponse {
  pdfContent: string; // Base64 encoded
}
