export type ArticleType = 'MUG' | 'SHIRT';

export type FitType = 'REGULAR' | 'SLIM' | 'LOOSE';

export interface ArticleMugVariant {
  id: number;
  articleId: number;
  insideColorCode: string;
  outsideColorCode: string;
  name: string;
  exampleImageUrl?: string | null;
  articleVariantNumber?: string;
  isDefault: boolean;
  exampleImageFilename?: string | null;
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

export interface Article {
  id: number;
  name: string;
  descriptionShort: string;
  descriptionLong: string;
  active: boolean;
  articleType: ArticleType;
  categoryId: number;
  categoryName: string;
  subcategoryId?: number;
  subcategoryName?: string;
  supplierId?: number;
  supplierName?: string;
  supplierArticleName?: string;
  supplierArticleNumber?: string;
  mugVariants?: ArticleMugVariant[];
  shirtVariants?: ArticleShirtVariant[];
  mugDetails?: ArticleMugDetails;
  shirtDetails?: ArticleShirtDetails;
  costCalculation?: CostCalculation;
  createdAt?: string;
  updatedAt?: string;
}

export interface CreateArticleRequest {
  name: string;
  descriptionShort: string;
  descriptionLong: string;
  active: boolean;
  articleType: ArticleType;
  categoryId: number;
  subcategoryId?: number;
  supplierId?: number;
  supplierArticleName?: string;
  supplierArticleNumber?: string;
  mugVariants?: CreateArticleMugVariantRequest[];
  shirtVariants?: CreateArticleShirtVariantRequest[];
  mugDetails?: CreateMugDetailsRequest;
  shirtDetails?: CreateShirtDetailsRequest;
  costCalculation?: CreateCostCalculationRequest;
}

export interface CreateArticleMugVariantRequest {
  insideColorCode: string;
  outsideColorCode: string;
  name: string;
  articleVariantNumber?: string;
  isDefault?: boolean;
}

export interface CreateArticleShirtVariantRequest {
  color: string;
  size: string;
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

export interface UpdateArticleRequest extends Omit<CreateArticleRequest, 'articleType' | 'mugVariants' | 'shirtVariants'> {
  mugDetails?: UpdateMugDetailsRequest;
  shirtDetails?: UpdateShirtDetailsRequest;
  costCalculation?: UpdateCostCalculationRequest;
}

export interface UpdateMugDetailsRequest extends CreateMugDetailsRequest {}

export interface UpdateShirtDetailsRequest extends CreateShirtDetailsRequest {}

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
  salesMarginNet: number;
  salesMarginTax: number;
  salesMarginGross: number;
  salesMarginPercent: number;
  salesTotalNet: number;
  salesTotalTax: number;
  salesTotalGross: number;
  salesPriceUnit?: string;

  // Calculation mode
  purchaseCalculationMode: 'NET' | 'GROSS';
  salesCalculationMode: 'NET' | 'GROSS';

  // UI state
  purchasePriceCorresponds: boolean;
  salesPriceCorresponds: boolean;
  purchaseActiveRow: 'cost' | 'costPercent';
  salesActiveRow: 'margin' | 'marginPercent' | 'total';
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
