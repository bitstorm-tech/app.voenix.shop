import { orderApi } from '@/api/orderApi';
import type { CreateOrderRequest, OrderDto } from '@/types/order';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';

// Query keys
export const orderKeys = {
  all: ['orders'] as const,
  lists: () => [...orderKeys.all, 'list'] as const,
  list: (filters?: Record<string, unknown>) => [...orderKeys.lists(), { filters }] as const,
  details: () => [...orderKeys.all, 'detail'] as const,
  detail: (id: string) => [...orderKeys.details(), id] as const,
};

// Hook to get user's orders
export function useOrders() {
  return useQuery({
    queryKey: orderKeys.list(),
    queryFn: orderApi.getUserOrders,
    staleTime: 5 * 60 * 1000, // 5 minutes
  });
}

// Hook to get a specific order
export function useOrder(orderId: string) {
  return useQuery({
    queryKey: orderKeys.detail(orderId),
    queryFn: () => orderApi.getOrder(orderId),
    enabled: !!orderId,
    staleTime: 5 * 60 * 1000, // 5 minutes
  });
}

// Hook to create an order (checkout)
export function useCreateOrder() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (data: CreateOrderRequest) => orderApi.createOrder(data),
    onSuccess: (newOrder: OrderDto) => {
      // Invalidate and refetch orders list
      queryClient.invalidateQueries({ queryKey: orderKeys.lists() });

      // Add the new order to the cache
      queryClient.setQueryData(orderKeys.detail(newOrder.id), newOrder);

      // Invalidate cart since order creation converts the cart
      queryClient.invalidateQueries({ queryKey: ['cart'] });
    },
  });
}
