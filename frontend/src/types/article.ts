export type ArticleType = 'MUG' | 'SHIRT' | 'PILLOW';

export type FitType = 'REGULAR' | 'SLIM' | 'LOOSE';

export interface ArticleMugVariant {
  id: number;
  articleId: number;
  insideColorCode: string;
  outsideColorCode: string;
  name: string;
  sku?: string;
  exampleImageUrl?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface ArticleShirtVariant {
  id: number;
  articleId: number;
  color: string;
  size: string;
  sku?: string;
  exampleImageUrl?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface ArticlePillowVariant {
  id: number;
  articleId: number;
  color: string;
  material: string;
  sku?: string;
  exampleImageUrl?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface ArticleMugDetails {
  articleId: number;
  heightMm: number;
  diameterMm: number;
  printTemplateWidthMm: number;
  printTemplateHeightMm: number;
  fillingQuantity?: string;
  dishwasherSafe: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface ArticleShirtDetails {
  articleId: number;
  material: string;
  careInstructions?: string;
  fitType: FitType;
  availableSizes: string[];
  createdAt?: string;
  updatedAt?: string;
}

export interface ArticlePillowDetails {
  articleId: number;
  widthCm: number;
  heightCm: number;
  depthCm: number;
  material: string;
  fillingType: string;
  coverRemovable: boolean;
  washable: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface Article {
  id: number;
  name: string;
  descriptionShort: string;
  descriptionLong: string;
  exampleImageFilename: string;
  price: number;
  active: boolean;
  articleType: ArticleType;
  categoryId: number;
  categoryName: string;
  subcategoryId?: number;
  subcategoryName?: string;
  supplierId?: number;
  supplierName?: string;
  mugVariants?: ArticleMugVariant[];
  shirtVariants?: ArticleShirtVariant[];
  pillowVariants?: ArticlePillowVariant[];
  mugDetails?: ArticleMugDetails;
  shirtDetails?: ArticleShirtDetails;
  pillowDetails?: ArticlePillowDetails;
  createdAt?: string;
  updatedAt?: string;
}

export interface CreateArticleRequest {
  name: string;
  descriptionShort: string;
  descriptionLong: string;
  exampleImageFilename: string;
  price: number;
  active: boolean;
  articleType: ArticleType;
  categoryId: number;
  subcategoryId?: number;
  supplierId?: number;
  mugVariants?: CreateArticleMugVariantRequest[];
  shirtVariants?: CreateArticleShirtVariantRequest[];
  pillowVariants?: CreateArticlePillowVariantRequest[];
  mugDetails?: CreateMugDetailsRequest;
  shirtDetails?: CreateShirtDetailsRequest;
  pillowDetails?: CreatePillowDetailsRequest;
}

export interface CreateArticleMugVariantRequest {
  insideColorCode: string;
  outsideColorCode: string;
  name: string;
  sku?: string;
  exampleImageFilename?: string;
}

export interface CreateArticleShirtVariantRequest {
  color: string;
  size: string;
  sku?: string;
  exampleImageFilename?: string;
}

export interface CreateArticlePillowVariantRequest {
  color: string;
  material: string;
  sku?: string;
  exampleImageFilename?: string;
}

export interface CreateMugDetailsRequest {
  heightMm: number;
  diameterMm: number;
  printTemplateWidthMm: number;
  printTemplateHeightMm: number;
  fillingQuantity?: string;
  dishwasherSafe: boolean;
}

export interface CreateShirtDetailsRequest {
  material: string;
  careInstructions?: string;
  fitType: FitType;
  availableSizes: string[];
}

export interface CreatePillowDetailsRequest {
  widthCm: number;
  heightCm: number;
  depthCm: number;
  material: string;
  fillingType: string;
  coverRemovable: boolean;
  washable: boolean;
}

export interface UpdateArticleRequest extends Omit<CreateArticleRequest, 'articleType' | 'mugVariants' | 'shirtVariants' | 'pillowVariants'> {
  mugDetails?: UpdateMugDetailsRequest;
  shirtDetails?: UpdateShirtDetailsRequest;
  pillowDetails?: UpdatePillowDetailsRequest;
}

export interface UpdateMugDetailsRequest extends CreateMugDetailsRequest {}

export interface UpdateShirtDetailsRequest extends CreateShirtDetailsRequest {}

export interface UpdatePillowDetailsRequest extends CreatePillowDetailsRequest {}

export interface PaginatedResponse<T> {
  content: T[];
  currentPage: number;
  totalPages: number;
  totalElements: number;
  size: number;
}
