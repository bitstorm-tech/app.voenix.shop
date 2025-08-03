import type { Article, ArticleType, PaginatedResponse } from '@/types/article';
import { cookies } from 'next/headers';

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

async function serverApiRequest<T>(
  endpoint: string,
  options: RequestInit = {},
): Promise<T> {
  const cookieStore = await cookies();
  const sessionCookie = cookieStore.get('SESSION');

  const defaultOptions: RequestInit = {
    headers: {
      'Content-Type': 'application/json',
      ...(sessionCookie && {
        Cookie: `SESSION=${sessionCookie.value}`,
      }),
    },
    ...options,
  };

  // For server-side requests, we need to use absolute URLs
  // In development, this will be the backend server directly
  // In production, you might want to use an environment variable
  const baseUrl = process.env.BACKEND_URL;
  const response = await fetch(`${baseUrl}/api${endpoint}`, defaultOptions);
  return handleResponse<T>(response);
}

export const serverApi = {
  get: async <T>(endpoint: string): Promise<T> => {
    return serverApiRequest<T>(endpoint, { method: 'GET' });
  },

  post: async <T>(endpoint: string, data: unknown): Promise<T> => {
    return serverApiRequest<T>(endpoint, {
      method: 'POST',
      body: JSON.stringify(data),
    });
  },

  put: async <T>(endpoint: string, data: unknown): Promise<T> => {
    return serverApiRequest<T>(endpoint, {
      method: 'PUT',
      body: JSON.stringify(data),
    });
  },

  delete: async <T>(endpoint: string): Promise<T> => {
    return serverApiRequest<T>(endpoint, { method: 'DELETE' });
  },
};

export interface GetArticlesParams {
  page?: number;
  size?: number;
  type?: ArticleType;
  categoryId?: number;
  subcategoryId?: number;
  active?: boolean;
  search?: string;
}

// Server-side Articles API
export const serverArticlesApi = {
  getAll: async (params?: GetArticlesParams): Promise<PaginatedResponse<Article>> => {
    const queryParams = new URLSearchParams();
    if (params) {
      Object.entries(params).forEach(([key, value]) => {
        if (value !== undefined && value !== null) {
          queryParams.append(key, value.toString());
        }
      });
    }
    const queryString = queryParams.toString();
    return serverApi.get<PaginatedResponse<Article>>(`/admin/articles${queryString ? `?${queryString}` : ''}`);
  },

  getById: async (id: number): Promise<Article> => {
    return serverApi.get<Article>(`/admin/articles/${id}`);
  },

  delete: async (id: number): Promise<void> => {
    return serverApi.delete<void>(`/admin/articles/${id}`);
  },
};