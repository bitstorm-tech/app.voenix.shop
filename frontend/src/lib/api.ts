import type { CropData } from '@/components/editor/types';
import type {
  Article,
  ArticleMugVariant,
  ArticleShirtVariant,
  CostCalculation,
  CreateArticleMugVariantRequest,
  CreateArticleRequest,
  CreateArticleShirtVariantRequest,
  CreateCostCalculationRequest,
  PaginatedResponse,
  UpdateArticleRequest,
} from '@/types/article';
import type { LoginRequest, LoginResponse, SessionInfo } from '@/types/auth';
import type { AddToCartRequest, CartDto, CartSummaryDto, UpdateCartItemRequest } from '@/types/cart';
import type { MugWithVariantsSummary } from '@/types/copyVariants';
import type { Country } from '@/types/country';
import type { ArticleCategory, ArticleSubCategory, Mug, MugVariant } from '@/types/mug';
import type { Prompt, PromptCategory, PromptSubCategory } from '@/types/prompt';
import type { PromptSlotType, PromptSlotVariant, ProviderLLM } from '@/types/promptSlotVariant';
import type { CreateSupplierRequest, Supplier, UpdateSupplierRequest } from '@/types/supplier';
import type { CreateValueAddedTaxRequest, UpdateValueAddedTaxRequest, ValueAddedTax } from '@/types/vat';

export type { CreateValueAddedTaxRequest, UpdateValueAddedTaxRequest } from '@/types/vat';

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
    if (response.status === 401 || response.status === 403) {
      // Clear auth state - just redirect, React Query will handle the rest
      // The session query will fail and update the auth state automatically
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

  post: async <T>(endpoint: string, data: unknown): Promise<T> => {
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

  put: async <T>(endpoint: string, data: unknown): Promise<T> => {
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
  updateSlots: (id: number, slots: PromptSlotUpdate[]) =>
    api.put<Prompt>(`/admin/prompts/${id}/slots`, { slotVariants: slots.map((s) => ({ slotId: s.slotId })) }),
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
  llm: string;
  categoryId: number;
  subcategoryId?: number;
  active: boolean;
  slots?: PromptSlotUpdate[];
  exampleImageFilename?: string;
  priceId?: number;
  costCalculation?: CreateCostCalculationRequest;
}

export interface UpdatePromptRequest {
  title?: string;
  promptText?: string;
  llm?: string;
  categoryId?: number;
  subcategoryId?: number;
  active?: boolean;
  slots?: PromptSlotUpdate[];
  exampleImageFilename?: string;
  priceId?: number;
  costCalculation?: CreateCostCalculationRequest;
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

// Re-export types for convenience
export type { CreateArticleRequest, UpdateArticleRequest } from '@/types/article';

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
  // Mug variant management
  createMugVariant: (articleId: number, data: CreateArticleMugVariantRequest) =>
    api.post<ArticleMugVariant>(`/admin/articles/mugs/${articleId}/variants`, data),
  updateMugVariant: (variantId: number, data: CreateArticleMugVariantRequest) =>
    api.put<ArticleMugVariant>(`/admin/articles/mugs/variants/${variantId}`, data),
  deleteMugVariant: (variantId: number) => api.delete<void>(`/admin/articles/mugs/variants/${variantId}`),
  uploadMugVariantImage: async (variantId: number, file: File, cropArea?: { x: number; y: number; width: number; height: number }) => {
    const formData = new FormData();
    formData.append('image', file); // Changed from 'file' to 'image' to match backend
    if (cropArea) {
      formData.append('cropX', cropArea.x.toString());
      formData.append('cropY', cropArea.y.toString());
      formData.append('cropWidth', cropArea.width.toString());
      formData.append('cropHeight', cropArea.height.toString());
    }
    const response = await fetch(`/api/admin/articles/mugs/variants/${variantId}/image`, {
      method: 'POST',
      credentials: 'include',
      body: formData,
    });
    return handleResponse<ArticleMugVariant>(response); // Return proper type matching backend response
  },
  removeMugVariantImage: (variantId: number) => api.delete<ArticleMugVariant>(`/admin/articles/mugs/variants/${variantId}/image`),
  // Copy variant functionality
  getVariantsCatalog: (excludeMugId?: number): Promise<MugWithVariantsSummary[]> => {
    const queryParams = excludeMugId ? `?excludeMugId=${excludeMugId}` : '';
    return api.get<MugWithVariantsSummary[]>(`/admin/articles/mugs/variants-catalog${queryParams}`);
  },
  copyVariants: (mugId: number, variantIds: number[]): Promise<ArticleMugVariant[]> =>
    api.post<ArticleMugVariant[]>(`/admin/articles/mugs/${mugId}/copy-variants`, { variantIds }),
  // Shirt variant management
  createShirtVariant: (articleId: number, data: CreateArticleShirtVariantRequest) =>
    api.post<ArticleShirtVariant>(`/admin/articles/shirts/${articleId}/variants`, data),
  updateShirtVariant: (variantId: number, data: CreateArticleShirtVariantRequest) =>
    api.put<ArticleShirtVariant>(`/admin/articles/shirts/variants/${variantId}`, data),
  deleteShirtVariant: (variantId: number) => api.delete<void>(`/admin/articles/shirts/variants/${variantId}`),
};

// Mug API endpoints
export const mugsApi = {
  getAll: () => api.get<Mug[]>('/mugs'),
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

// Prompt Slot Type API endpoints
export const promptSlotTypesApi = {
  getAll: () => api.get<PromptSlotType[]>('/admin/prompts/prompt-slot-types'),
  getById: (id: number) => api.get<PromptSlotType>(`/admin/prompts/prompt-slot-types/${id}`),
  create: (data: CreatePromptSlotTypeRequest) => api.post<PromptSlotType>('/admin/prompts/prompt-slot-types', data),
  update: (id: number, data: UpdatePromptSlotTypeRequest) => api.put<PromptSlotType>(`/admin/prompts/prompt-slot-types/${id}`, data),
  delete: (id: number) => api.delete<void>(`/admin/prompts/prompt-slot-types/${id}`),
  updatePositions: async (positions: { id: number; position: number }[]) => {
    const allPromptSlotTypes = await api.get<PromptSlotType[]>('/admin/prompts/prompt-slot-types');
    const maxPosition = Math.max(...allPromptSlotTypes.map((st) => st.position), ...positions.map((p) => p.position));

    // Phase 1: Move all items to temporary positions (beyond max)
    const tempOffset = maxPosition + 10;
    for (const update of positions) {
      await api.put<PromptSlotType>(`/admin/prompts/prompt-slot-types/${update.id}`, { position: update.position + tempOffset });
    }

    // Phase 2: Move items to their final positions
    for (const update of positions) {
      await api.put<PromptSlotType>(`/admin/prompts/prompt-slot-types/${update.id}`, { position: update.position });
    }

    return positions;
  },
};

// Type definitions for Prompt Slot Type API requests
export interface CreatePromptSlotTypeRequest {
  name: string;
  position: number;
}

export interface UpdatePromptSlotTypeRequest {
  name?: string;
  position?: number;
}

export interface UpdateSlotTypePositionsRequest {
  positions: { id: number; position: number }[];
}

// Prompt Slot Variant API endpoints
export const promptSlotVariantsApi = {
  getAll: () => api.get<PromptSlotVariant[]>('/admin/prompts/slot-variants'),
  getById: (id: number) => api.get<PromptSlotVariant>(`/admin/prompts/slot-variants/${id}`),
  getByTypeId: (typeId: number) => api.get<PromptSlotVariant[]>(`/admin/prompts/slot-variants/type/${typeId}`),
  create: (data: CreatePromptSlotVariantRequest) => api.post<PromptSlotVariant>('/admin/prompts/slot-variants', data),
  update: (id: number, data: UpdatePromptSlotVariantRequest) => api.put<PromptSlotVariant>(`/admin/prompts/slot-variants/${id}`, data),
  delete: (id: number) => api.delete<void>(`/admin/prompts/slot-variants/${id}`),
};

export const promptLlmsApi = {
  getAll: () => api.get<{ llms: ProviderLLM[] }>('/admin/ai/llms'),
};

// Type definitions for Prompt Slot Variant API requests
export interface CreatePromptSlotVariantRequest {
  promptSlotTypeId: number;
  name: string;
  prompt: string;
  description?: string;
  exampleImageFilename?: string | null;
  llm: string;
}

export interface UpdatePromptSlotVariantRequest {
  promptSlotTypeId?: number;
  name?: string;
  prompt?: string;
  description?: string;
  exampleImageFilename?: string | null;
  llm?: string;
}

// Image API endpoints
export const imagesApi = {
  upload: async (
    file: File,
    imageType: 'PUBLIC' | 'PRIVATE' | 'PROMPT_EXAMPLE' | 'PROMPT_SLOT_VARIANT_EXAMPLE',
    cropArea?: { x: number; y: number; width: number; height: number },
  ) => {
    const formData = new FormData();
    formData.append('file', file);

    // Backend expects these as regular form fields, not a JSON 'request' part
    formData.append('imageType', imageType);
    if (cropArea) {
      formData.append('cropX', String(cropArea.x));
      formData.append('cropY', String(cropArea.y));
      formData.append('cropWidth', String(cropArea.width));
      formData.append('cropHeight', String(cropArea.height));
    }

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
  register: (data: RegisterRequest) => api.post<LoginResponse>('/auth/register', data),
  registerGuest: (data: RegisterGuestRequest) => api.post<LoginResponse>('/auth/register-guest', data),
};

// Type definition for RegisterRequest
export interface RegisterRequest {
  email: string;
  password: string;
}

// Type definition for RegisterGuestRequest
export interface RegisterGuestRequest {
  email: string;
  firstName?: string;
  lastName?: string;
  phoneNumber?: string;
}

// User API endpoints
export const userApi = {
  // Generate images using authenticated endpoint
  generateImage: async (image: File, promptId: number, cropData?: CropData): Promise<PublicImageGenerationResponse> => {
    const formData = new FormData();
    formData.append('image', image);
    formData.append('promptId', promptId.toString());

    // Add crop data if provided
    if (cropData) {
      formData.append('cropX', cropData.croppedAreaPixels.x.toString());
      formData.append('cropY', cropData.croppedAreaPixels.y.toString());
      formData.append('cropWidth', cropData.croppedAreaPixels.width.toString());
      formData.append('cropHeight', cropData.croppedAreaPixels.height.toString());
    }

    const url = '/api/user/ai/images/generate';
    const response = await fetch(url, {
      method: 'POST',
      body: formData,
      credentials: 'include',
    });
    return handleResponse<PublicImageGenerationResponse>(response);
  },
};

// Admin User API endpoints - Removed (not used)

// VAT API endpoints
export const vatApi = {
  getAll: () => api.get<ValueAddedTax[]>('/admin/vat'),
  getById: (id: number) => api.get<ValueAddedTax>(`/admin/vat/${id}`),
  create: (data: CreateValueAddedTaxRequest) => api.post<ValueAddedTax>('/admin/vat', data),
  update: (id: number, data: UpdateValueAddedTaxRequest) => api.put<ValueAddedTax>(`/admin/vat/${id}`, data),
  delete: (id: number) => api.delete<void>(`/admin/vat/${id}`),
};

// Service API endpoints
export const servicesApi = {
  // OpenAI Image Edit
  editImage: async (image: File, mask: File, prompt: string, size: string, quality: string, background: string) => {
    const formData = new FormData();
    formData.append('image', image);

    const request = {
      mask,
      prompt,
      size,
      quality,
      background,
    };

    formData.append('request', new Blob([JSON.stringify(request)], { type: 'application/json' }));

    const response = await fetch('/api/admin/openai/image-edit', {
      method: 'POST',
      credentials: 'include',
      body: formData,
    });
    return handleResponse<ImageEditResponse>(response);
  },
  // Test Prompt
  testPrompt: async (image: File, masterPrompt: string, specificPrompt: string, background: string, quality: string, size: string) => {
    const formData = new FormData();
    formData.append('image', image);
    formData.append('masterPrompt', masterPrompt);
    formData.append('specificPrompt', specificPrompt);
    formData.append('background', background);
    formData.append('quality', quality);
    formData.append('size', size);

    const response = await fetch('/api/admin/openai/test-prompt', {
      method: 'POST',
      credentials: 'include',
      body: formData,
    });
    return handleResponse<TestPromptResponse>(response);
  },
  // PDF Generation
  generatePdf: (data: GeneratePdfRequest) => api.post<PdfResponse>('/admin/pdf/generate', data),
};

// Supplier API
export const supplierApi = {
  getAll: () => api.get<Supplier[]>('/admin/suppliers'),
  getById: (id: number) => api.get<Supplier>(`/admin/suppliers/${id}`),
  create: (data: CreateSupplierRequest) => api.post<Supplier>('/admin/suppliers', data),
  update: (id: number, data: UpdateSupplierRequest) => api.put<Supplier>(`/admin/suppliers/${id}`, data),
  delete: (id: number) => api.delete(`/admin/suppliers/${id}`),
};

// Country API
export const countryApi = {
  getAll: () => api.get<Country[]>('/public/countries'),
};

// Prices API (admin)
export const pricesApi = {
  getById: (id: number) => api.get<CostCalculation>(`/admin/prices/${id}`),
};

// Public API endpoints (no authentication required)
export const publicApi = {
  fetchPrompts: async (): Promise<Prompt[]> => {
    const response = await fetch('/api/prompts', {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      },
      // No credentials needed for public endpoints
    });
    return handleResponse<Prompt[]>(response);
  },
  generateImage: async (image: File, promptId: number, cropData?: CropData): Promise<PublicImageGenerationResponse> => {
    const formData = new FormData();
    formData.append('image', image);
    formData.append('promptId', promptId.toString());

    // Add crop data if provided
    if (cropData) {
      formData.append('cropX', cropData.croppedAreaPixels.x.toString());
      formData.append('cropY', cropData.croppedAreaPixels.y.toString());
      formData.append('cropWidth', cropData.croppedAreaPixels.width.toString());
      formData.append('cropHeight', cropData.croppedAreaPixels.height.toString());
    }

    const url = '/api/public/images/generate';
    const response = await fetch(url, {
      method: 'POST',
      body: formData,
      // No credentials needed for public endpoints
    });
    return handleResponse<PublicImageGenerationResponse>(response);
  },
  // Generate PDF using public endpoint
  generatePdf: async (mugId: number, imageUrl: string): Promise<Blob> => {
    const response = await fetch('/api/public/pdf/generate', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        mugId,
        imageUrl,
      }),
      // No credentials needed for public endpoints
    });

    if (!response.ok) {
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

    return response.blob();
  },
  fetchMugs: async (): Promise<Mug[]> => {
    const response = await fetch('/api/mugs', {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      },
      // No credentials needed for public endpoints
    });
    return handleResponse<Mug[]>(response);
  },
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

export interface TestPromptResponse {
  imageUrl: string;
  filename: string;
  finalPrompt: string;
}

export interface GeneratePdfRequest {
  content: string;
  size: 'A4' | 'A3' | 'LETTER' | 'LEGAL';
}

export interface PdfResponse {
  pdfContent: string; // Base64 encoded
}

export interface PublicImageGenerationResponse {
  imageUrls: string[];
  generatedImageIds: number[];
  prompt?: string;
}

// Cart API endpoints
export const cartApi = {
  // Get user's active cart
  getCart: () => api.get<CartDto>('/user/cart'),

  // Get cart summary (for badges)
  getSummary: () => api.get<CartSummaryDto>('/user/cart/summary'),

  // Add item to cart
  addItem: (data: AddToCartRequest) => api.post<CartDto>('/user/cart/items', data),

  // Update cart item quantity
  updateItem: (itemId: number, data: UpdateCartItemRequest) => api.put<CartDto>(`/user/cart/items/${itemId}`, data),

  // Remove cart item
  removeItem: (itemId: number) => api.delete<CartDto>(`/user/cart/items/${itemId}`),

  // Clear entire cart
  clearCart: () => api.delete<CartDto>('/user/cart'),

  // Refresh cart prices to current values
  refreshPrices: () => api.post<CartDto>('/user/cart/refresh-prices', {}),
};

// Re-export order API for consistency
export { orderApi } from '@/api/orderApi';
