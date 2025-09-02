import { api } from '@/lib/api';
import type { GetUserImagesResponse, PromptSummary, UserImagesParams } from '@/types/userImage';

export const userImagesApi = {
  // Get all user images with pagination and filtering
  getUserImages: (params?: UserImagesParams): Promise<GetUserImagesResponse> => {
    const queryParams = new URLSearchParams({
      page: String(params?.page ?? 0),
      size: String(params?.size ?? 20),
      type: params?.type ?? 'all',
      sortBy: params?.sortBy ?? 'createdAt',
      sortDirection: params?.sortDirection ?? 'DESC',
    });
    return api.get<GetUserImagesResponse>(`/user/images?${queryParams.toString()}`);
  },

  // Get prompt summaries by IDs
  getPromptsByIds: (ids: number[]): Promise<PromptSummary[]> => {
    if (ids.length === 0) return Promise.resolve([]);
    const queryParams = new URLSearchParams();
    ids.forEach((id) => queryParams.append('ids', String(id)));
    return api.get<PromptSummary[]>(`/prompts/batch?${queryParams.toString()}`);
  },
};
