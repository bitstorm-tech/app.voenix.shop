import { api } from '@/lib/api';
import type { CreateOrderRequest, CreateOrderResponse, GetOrdersResponse, OrderDto } from '@/types/order';

export const orderApi = {
  // Create order from cart (checkout)
  createOrder: (data: CreateOrderRequest): Promise<CreateOrderResponse> => api.post<CreateOrderResponse>('/user/checkout', data),

  // Get user's orders
  getUserOrders: (): Promise<GetOrdersResponse> => api.get<GetOrdersResponse>('/user/orders'),

  // Get specific order by ID
  getOrder: (orderId: string): Promise<OrderDto> => api.get<OrderDto>(`/user/orders/${orderId}`),
};
