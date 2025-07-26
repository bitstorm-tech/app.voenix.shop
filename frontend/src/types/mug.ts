export interface Mug {
  id: number;
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
  createdAt?: string;
  updatedAt?: string;
  category?: ArticleCategory;
  subCategory?: ArticleSubCategory;
  variants?: MugVariant[];
}

export interface ArticleCategory {
  id: number;
  name: string;
  description?: string;
  createdAt?: string;
  updatedAt?: string;
  articles_count?: number;
}

export interface ArticleSubCategory {
  id: number;
  name: string;
  articleCategoryId: number;
  category?: ArticleCategory;
  description?: string;
  articlesCount?: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface MugVariant {
  id: number;
  mugId: number;
  colorCode: string;
  exampleImageUrl: string;
  supplierArticleNumber?: string;
  isDefault: boolean;
  exampleImageFilename?: string;
  createdAt?: string;
  updatedAt?: string;
}
