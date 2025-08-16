import type { Article, PaginatedResponse } from './article';
import type { MugVariantDto } from './cart';

export enum OrderStatus {
  PENDING = 'PENDING',
  PROCESSING = 'PROCESSING',
  SHIPPED = 'SHIPPED',
  DELIVERED = 'DELIVERED',
  CANCELLED = 'CANCELLED',
}

export interface AddressDto {
  streetAddress1: string;
  streetAddress2?: string;
  city: string;
  state: string;
  postalCode: string;
  country: string;
}

export interface CreateOrderRequest {
  customerEmail: string;
  customerFirstName: string;
  customerLastName: string;
  customerPhone?: string;
  shippingAddress: AddressDto;
  billingAddress?: AddressDto;
  useShippingAsBilling?: boolean;
  notes?: string;
}

export interface OrderItemDto {
  id: string;
  article: Article;
  variant: MugVariantDto;
  quantity: number;
  pricePerItem: number; // In cents
  totalPrice: number; // In cents
  generatedImageId?: number;
  generatedImageFilename?: string;
  promptId?: number;
  customData: Record<string, unknown>;
  createdAt: string;
}

export interface OrderDto {
  id: string;
  orderNumber: string;
  customerEmail: string;
  customerFirstName: string;
  customerLastName: string;
  customerPhone?: string;
  shippingAddress: AddressDto;
  billingAddress?: AddressDto;
  subtotal: number; // In cents
  taxAmount: number; // In cents
  shippingAmount: number; // In cents
  totalAmount: number; // In cents
  status: OrderStatus;
  cartId: number;
  notes?: string;
  items: OrderItemDto[];
  pdfUrl: string; // URL to download the PDF receipt
  createdAt: string;
  updatedAt: string;
}

// API response types
export type CreateOrderResponse = OrderDto;

export type GetOrdersResponse = PaginatedResponse<OrderDto>;
