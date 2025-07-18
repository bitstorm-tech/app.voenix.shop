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
  category?: MugCategory;
  subCategory?: MugSubCategory;
  variants?: MugVariant[];
}

export interface MugCategory {
  id: number;
  name: string;
  description?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface MugSubCategory {
  id: number;
  name: string;
  mugCategoryId: number;
  category?: MugCategory;
  description?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface MugVariant {
  id: number;
  mugId: number;
  colorCode: string;
  exampleImageUrl: string;
  createdAt?: string;
  updatedAt?: string;
}
