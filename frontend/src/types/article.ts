export type ArticleType = 'MUG' | 'SHIRT' | 'PILLOW';

export type FitType = 'REGULAR' | 'SLIM' | 'LOOSE';

export interface ArticleMugVariant {
  id: number;
  articleId: number;
  insideColorCode: string;
  outsideColorCode: string;
  name: string;
  exampleImageUrl?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface ArticleShirtVariant {
  id: number;
  articleId: number;
  color: string;
  size: string;
  exampleImageUrl?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface ArticlePillowVariant {
  id: number;
  articleId: number;
  color: string;
  material: string;
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
  costCalculation?: CostCalculation;
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
  costCalculation?: CreateCostCalculationRequest;
}

export interface CreateArticleMugVariantRequest {
  insideColorCode: string;
  outsideColorCode: string;
  name: string;
  exampleImageFilename?: string;
}

export interface CreateArticleShirtVariantRequest {
  color: string;
  size: string;
  exampleImageFilename?: string;
}

export interface CreateArticlePillowVariantRequest {
  color: string;
  material: string;
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
  costCalculation?: UpdateCostCalculationRequest;
}

export interface UpdateMugDetailsRequest extends CreateMugDetailsRequest {}

export interface UpdateShirtDetailsRequest extends CreateShirtDetailsRequest {}

export interface UpdatePillowDetailsRequest extends CreatePillowDetailsRequest {}

export interface CostCalculation {
  // Purchase section
  purchasePriceNet: number;
  purchasePriceTax: number;
  purchasePriceGross: number;
  purchaseCostNet: number;
  purchaseCostTax: number;
  purchaseCostGross: number;
  purchaseCostPercent: number;
  purchaseTotalNet: number;
  purchaseTotalTax: number;
  purchaseTotalGross: number;
  purchasePriceUnit?: string;
  purchaseVatRateId?: number;
  purchaseVatRatePercent: number;

  // Sales section
  salesVatRateId?: number;
  salesVatRatePercent: number;
  marginNet: number;
  marginTax: number;
  marginGross: number;
  marginPercent: number;
  salesTotalNet: number;
  salesTotalTax: number;
  salesTotalGross: number;
  salesPriceUnit?: string;

  // Calculation mode
  purchaseCalculationMode: 'NET' | 'GROSS';
  salesCalculationMode: 'NET' | 'GROSS';
}

export interface CreateCostCalculationRequest extends Partial<CostCalculation> {}

export interface UpdateCostCalculationRequest extends Partial<CostCalculation> {}

export interface PaginatedResponse<T> {
  content: T[];
  currentPage: number;
  totalPages: number;
  totalElements: number;
  size: number;
}
