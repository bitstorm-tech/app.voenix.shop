import type { PaginatedResponse } from './article';

export interface UserImage {
  id: number;
  uuid: string;
  filename: string;
  originalFilename?: string;
  type: 'uploaded' | 'generated';
  contentType?: string;
  fileSize?: number;
  promptId?: number;
  promptTitle?: string | null;
  uploadedImageId?: number;
  userId: number;
  createdAt: string;
  imageUrl: string;
  thumbnailUrl?: string;
}

export interface UserImagesParams {
  page?: number;
  size?: number;
  type?: 'all' | 'uploaded' | 'generated';
  sortBy?: 'createdAt' | 'type';
  sortDirection?: 'ASC' | 'DESC';
}

export type GetUserImagesResponse = PaginatedResponse<UserImage>;

export interface PromptSummary {
  id: number;
  title: string;
}
