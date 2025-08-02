import { Article } from './article';

export interface CartItemCustomData {
  imageUrl?: string;
  cropData?: {
    crop: { x: number; y: number };
    zoom: number;
    croppedAreaPixels: {
      x: number;
      y: number;
      width: number;
      height: number;
    };
  };
  generatedImageId?: number;
  promptInfo?: {
    promptId: number;
    promptText: string;
  };
}

export interface CartItemDto {
  id: number;
  article: Article;
  variant: MugVariantDto;
  quantity: number;
  priceAtTime: number; // Price in cents when added to cart
  originalPrice: number; // Current price for comparison
  hasPriceChanged: boolean;
  totalPrice: number; // priceAtTime * quantity in cents
  customData: CartItemCustomData;
  position: number;
  createdAt: string;
  updatedAt: string;
}

export interface MugVariantDto {
  id: number;
  articleId: number;
  colorCode: string;
  exampleImageUrl: string | null;
  supplierArticleNumber: string | null;
  isDefault: boolean;
  exampleImageFilename: string | null;
}

export interface CartDto {
  id: number;
  userId: number;
  status: 'ACTIVE' | 'EXPIRED' | 'CONVERTED';
  version: number;
  expiresAt: string | null;
  items: CartItemDto[];
  totalItemCount: number;
  totalPrice: number; // Total price in cents
  isEmpty: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CartSummaryDto {
  itemCount: number;
  totalPrice: number; // Total price in cents
  hasItems: boolean;
}

export interface AddToCartRequest {
  articleId: number;
  variantId: number;
  quantity?: number;
  customData?: CartItemCustomData;
}

export interface UpdateCartItemRequest {
  quantity: number;
}
