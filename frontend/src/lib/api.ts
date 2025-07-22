import { useAuthStore } from '@/stores/authStore';
import type { Article, CreateArticleRequest, PaginatedResponse, UpdateArticleRequest } from '@/types/article';
import type { LoginRequest, LoginResponse, SessionInfo } from '@/types/auth';
import type { ArticleCategory, ArticleSubCategory, Mug, MugVariant } from '@/types/mug';
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
    // Handle authentication errors globally
    if (response.status === 401 || response.status === 403) {
      // Clear auth state
      const authStore = useAuthStore.getState();
      authStore.logout().catch(() => {
        // Ignore logout errors, we're redirecting anyway
      });

      // Redirect to login page
      window.location.href = '/login';

      // Still throw the error for proper error handling
      const errorData = await response.json().catch(() => ({ message: 'Authentication required' }));
      throw new ApiError(response.status, errorData.message || 'Authentication required');
    }

    const errorData = await response.json().catch(() => ({ message: 'An error occurred' }));

    // If validation errors exist, append them to the message
    let errorMessage = errorData.message || `HTTP error! status: ${response.status}`;
    if (errorData.validationErrors) {
      const validationMessages = Object.entries(errorData.validationErrors)
        .map(([field, message]) => `${field}: ${message}`)
        .join(', ');
      errorMessage = `${errorMessage} - ${validationMessages}`;
    }

    throw new ApiError(response.status, errorMessage);
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
  promptText?: string;
  categoryId: number;
  subcategoryId?: number;
  active: boolean;
  slots?: PromptSlotUpdate[];
  exampleImageFilename?: string;
}

export interface UpdatePromptRequest {
  title?: string;
  promptText?: string;
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

// Articles API endpoints
export const articlesApi = {
  getAll: (params?: {
    page?: number;
    size?: number;
    type?: string;
    categoryId?: number;
    subcategoryId?: number;
    active?: boolean;
    search?: string;
  }) => {
    const queryParams = new URLSearchParams();
    if (params) {
      Object.entries(params).forEach(([key, value]) => {
        if (value !== undefined && value !== null) {
          queryParams.append(key, value.toString());
        }
      });
    }
    const queryString = queryParams.toString();
    return api.get<PaginatedResponse<Article>>(`/admin/articles${queryString ? `?${queryString}` : ''}`);
  },
  getById: (id: number) => api.get<Article>(`/admin/articles/${id}`),
  create: (data: CreateArticleRequest) => api.post<Article>('/admin/articles', data),
  update: (id: number, data: UpdateArticleRequest) => api.put<Article>(`/admin/articles/${id}`, data),
  delete: (id: number) => api.delete<void>(`/admin/articles/${id}`),
  // Variant management
  createVariant: (articleId: number, data: any) => api.post<any>(`/admin/articles/${articleId}/variants`, data),
  updateVariant: (variantId: number, data: any) => api.put<any>(`/admin/articles/variants/${variantId}`, data),
  deleteVariant: (variantId: number) => api.delete<void>(`/admin/articles/variants/${variantId}`),
};

// Mug API endpoints
export const mugsApi = {
  getAll: () => api.get<Mug[]>('/admin/articles/mugs'),
  getById: (id: number) => api.get<Mug>(`/admin/articles/mugs/${id}`),
  create: (data: CreateMugRequest) => api.post<Mug>('/admin/articles/mugs', data),
  update: (id: number, data: UpdateMugRequest) => api.put<Mug>(`/admin/articles/mugs/${id}`, data),
  delete: (id: number) => api.delete<void>(`/admin/articles/mugs/${id}`),
  search: (name: string) => api.get<Mug[]>(`/admin/articles/mugs/search?name=${encodeURIComponent(name)}`),
  getByPriceRange: (minPrice: number, maxPrice: number) =>
    api.get<Mug[]>(`/admin/articles/mugs/price-range?minPrice=${minPrice}&maxPrice=${maxPrice}`),
};

// Article Category API endpoints
export const articleCategoriesApi = {
  getAll: () => api.get<ArticleCategory[]>('/admin/articles/categories'),
  getById: (id: number) => api.get<ArticleCategory>(`/admin/articles/categories/${id}`),
  create: (data: CreateArticleCategoryRequest) => api.post<ArticleCategory>('/admin/articles/categories', data),
  update: (id: number, data: UpdateArticleCategoryRequest) => api.put<ArticleCategory>(`/admin/articles/categories/${id}`, data),
  delete: (id: number) => api.delete<void>(`/admin/articles/categories/${id}`),
  search: (name: string) => api.get<ArticleCategory[]>(`/admin/articles/categories/search?name=${encodeURIComponent(name)}`),
};

// Article SubCategory API endpoints
export const articleSubCategoriesApi = {
  getAll: () => api.get<ArticleSubCategory[]>('/admin/articles/subcategories'),
  getById: (id: number) => api.get<ArticleSubCategory>(`/admin/articles/subcategories/${id}`),
  getByCategoryId: (categoryId: number) => api.get<ArticleSubCategory[]>(`/admin/articles/subcategories/category/${categoryId}`),
  create: (data: CreateArticleSubCategoryRequest) => api.post<ArticleSubCategory>('/admin/articles/subcategories', data),
  update: (id: number, data: UpdateArticleSubCategoryRequest) => api.put<ArticleSubCategory>(`/admin/articles/subcategories/${id}`, data),
  delete: (id: number) => api.delete<void>(`/admin/articles/subcategories/${id}`),
  search: (name: string) => api.get<ArticleSubCategory[]>(`/admin/articles/subcategories/search?name=${encodeURIComponent(name)}`),
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

export interface CreateArticleCategoryRequest {
  name: string;
  description?: string;
}

export interface UpdateArticleCategoryRequest {
  name?: string;
  description?: string;
}

export interface CreateArticleSubCategoryRequest {
  name: string;
  articleCategoryId: number;
  description?: string;
}

export interface UpdateArticleSubCategoryRequest {
  name?: string;
  articleCategoryId?: number;
  description?: string;
}

// Mug Variant API endpoints
export const mugVariantsApi = {
  getAll: () => api.get<MugVariant[]>('/admin/articles/mugs/variants'),
  getById: (id: number) => api.get<MugVariant>(`/admin/articles/mugs/variants/${id}`),
  getByMugId: (mugId: number) => api.get<MugVariant[]>(`/admin/articles/mugs/variants/mug/${mugId}`),
  create: (data: CreateMugVariantRequest) => api.post<MugVariant>('/admin/articles/mugs/variants', data),
  update: (id: number, data: UpdateMugVariantRequest) => api.put<MugVariant>(`/admin/articles/mugs/variants/${id}`, data),
  delete: (id: number) => api.delete<void>(`/admin/articles/mugs/variants/${id}`),
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
