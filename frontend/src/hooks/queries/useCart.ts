import { cartApi } from '@/lib/api';
import type { AddToCartRequest, CartDto, CartSummaryDto, UpdateCartItemRequest } from '@/types/cart';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useSession } from './useAuth';

// Query keys
export const cartKeys = {
  all: ['cart'] as const,
  cart: () => [...cartKeys.all, 'data'] as const,
  summary: () => [...cartKeys.all, 'summary'] as const,
};

// Get cart query
export function useCart() {
  const { data: session, isLoading: sessionLoading } = useSession();

  return useQuery({
    queryKey: cartKeys.cart(),
    queryFn: cartApi.getCart,
    enabled: !sessionLoading && session?.authenticated === true,
    retry: (failureCount, error: any) => {
      // Don't retry on auth errors
      if (error?.status === 401 || error?.status === 403) {
        return false;
      }
      return failureCount < 3;
    },
    staleTime: 30 * 1000, // 30 seconds
    // Return empty cart structure when user is not authenticated
    placeholderData:
      session?.authenticated === false
        ? {
            id: 0,
            userId: 0,
            status: 'ACTIVE' as const,
            version: 0,
            expiresAt: null,
            items: [],
            totalItemCount: 0,
            totalPrice: 0,
            isEmpty: true,
            createdAt: new Date().toISOString(),
            updatedAt: new Date().toISOString(),
          }
        : undefined,
  });
}

// Get cart summary query (for badges)
export function useCartSummary() {
  const { data: session, isLoading: sessionLoading } = useSession();

  return useQuery({
    queryKey: cartKeys.summary(),
    queryFn: cartApi.getSummary,
    enabled: !sessionLoading && session?.authenticated === true,
    retry: (failureCount, error: any) => {
      // Don't retry on auth errors
      if (error?.status === 401 || error?.status === 403) {
        return false;
      }
      return failureCount < 3;
    },
    staleTime: 30 * 1000, // 30 seconds
    // Return empty summary when user is not authenticated
    placeholderData:
      session?.authenticated === false
        ? {
            itemCount: 0,
            totalPrice: 0,
            hasItems: false,
          }
        : undefined,
  });
}

// Add item to cart mutation
export function useAddToCart() {
  const queryClient = useQueryClient();
  const { data: session } = useSession();

  return useMutation({
    mutationFn: (data: AddToCartRequest) => {
      // Ensure user is authenticated before making API call
      if (!session?.authenticated) {
        throw new Error('Authentication required to add items to cart');
      }
      return cartApi.addItem(data);
    },
    onMutate: async (newItem) => {
      // Cancel any outgoing refetches
      await queryClient.cancelQueries({ queryKey: cartKeys.cart() });
      await queryClient.cancelQueries({ queryKey: cartKeys.summary() });

      // Snapshot the previous value
      const previousCart = queryClient.getQueryData<CartDto>(cartKeys.cart());
      const previousSummary = queryClient.getQueryData<CartSummaryDto>(cartKeys.summary());

      // Optimistically update cart summary
      if (previousSummary) {
        queryClient.setQueryData<CartSummaryDto>(cartKeys.summary(), {
          ...previousSummary,
          itemCount: previousSummary.itemCount + (newItem.quantity || 1),
          hasItems: true,
        });
      }

      // Return a context object with the snapshotted value
      return { previousCart, previousSummary };
    },
    onError: (_err, _newItem, context) => {
      // If the mutation fails, use the context returned from onMutate to roll back
      if (context?.previousCart) {
        queryClient.setQueryData(cartKeys.cart(), context.previousCart);
      }
      if (context?.previousSummary) {
        queryClient.setQueryData(cartKeys.summary(), context.previousSummary);
      }
    },
    onSuccess: (data) => {
      // Update cart data
      queryClient.setQueryData(cartKeys.cart(), data);

      // Update summary from cart data
      queryClient.setQueryData<CartSummaryDto>(cartKeys.summary(), {
        itemCount: data.totalItemCount,
        totalPrice: data.totalPrice,
        hasItems: !data.isEmpty,
      });
    },
  });
}

// Update cart item mutation
export function useUpdateCartItem() {
  const queryClient = useQueryClient();
  const { data: session } = useSession();

  return useMutation({
    mutationFn: ({ itemId, data }: { itemId: number; data: UpdateCartItemRequest }) => {
      // Ensure user is authenticated before making API call
      if (!session?.authenticated) {
        throw new Error('Authentication required to update cart items');
      }
      return cartApi.updateItem(itemId, data);
    },
    onMutate: async ({ itemId, data }) => {
      // Cancel any outgoing refetches
      await queryClient.cancelQueries({ queryKey: cartKeys.cart() });
      await queryClient.cancelQueries({ queryKey: cartKeys.summary() });

      // Snapshot the previous value
      const previousCart = queryClient.getQueryData<CartDto>(cartKeys.cart());
      const previousSummary = queryClient.getQueryData<CartSummaryDto>(cartKeys.summary());

      // Optimistically update cart if we have the data
      if (previousCart) {
        const updatedCart = {
          ...previousCart,
          items: previousCart.items.map((item) =>
            item.id === itemId ? { ...item, quantity: data.quantity, totalPrice: item.priceAtTime * data.quantity } : item,
          ),
        };

        // Recalculate totals
        const newTotalItemCount = updatedCart.items.reduce((sum, item) => sum + item.quantity, 0);
        const newTotalPrice = updatedCart.items.reduce((sum, item) => sum + item.totalPrice, 0);

        updatedCart.totalItemCount = newTotalItemCount;
        updatedCart.totalPrice = newTotalPrice;
        updatedCart.isEmpty = newTotalItemCount === 0;

        queryClient.setQueryData(cartKeys.cart(), updatedCart);

        // Update summary
        queryClient.setQueryData<CartSummaryDto>(cartKeys.summary(), {
          itemCount: newTotalItemCount,
          totalPrice: newTotalPrice,
          hasItems: newTotalItemCount > 0,
        });
      }

      return { previousCart, previousSummary };
    },
    onError: (_err, _variables, context) => {
      // If the mutation fails, use the context returned from onMutate to roll back
      if (context?.previousCart) {
        queryClient.setQueryData(cartKeys.cart(), context.previousCart);
      }
      if (context?.previousSummary) {
        queryClient.setQueryData(cartKeys.summary(), context.previousSummary);
      }
    },
    onSuccess: (data) => {
      // Update cart data
      queryClient.setQueryData(cartKeys.cart(), data);

      // Update summary from cart data
      queryClient.setQueryData<CartSummaryDto>(cartKeys.summary(), {
        itemCount: data.totalItemCount,
        totalPrice: data.totalPrice,
        hasItems: !data.isEmpty,
      });
    },
  });
}

// Remove cart item mutation
export function useRemoveCartItem() {
  const queryClient = useQueryClient();
  const { data: session } = useSession();

  return useMutation({
    mutationFn: (itemId: number) => {
      // Ensure user is authenticated before making API call
      if (!session?.authenticated) {
        throw new Error('Authentication required to remove cart items');
      }
      return cartApi.removeItem(itemId);
    },
    onMutate: async (itemId) => {
      // Cancel any outgoing refetches
      await queryClient.cancelQueries({ queryKey: cartKeys.cart() });
      await queryClient.cancelQueries({ queryKey: cartKeys.summary() });

      // Snapshot the previous value
      const previousCart = queryClient.getQueryData<CartDto>(cartKeys.cart());
      const previousSummary = queryClient.getQueryData<CartSummaryDto>(cartKeys.summary());

      // Optimistically update cart if we have the data
      if (previousCart) {
        const updatedCart = {
          ...previousCart,
          items: previousCart.items.filter((item) => item.id !== itemId),
        };

        // Recalculate totals
        const newTotalItemCount = updatedCart.items.reduce((sum, item) => sum + item.quantity, 0);
        const newTotalPrice = updatedCart.items.reduce((sum, item) => sum + item.totalPrice, 0);

        updatedCart.totalItemCount = newTotalItemCount;
        updatedCart.totalPrice = newTotalPrice;
        updatedCart.isEmpty = newTotalItemCount === 0;

        queryClient.setQueryData(cartKeys.cart(), updatedCart);

        // Update summary
        queryClient.setQueryData<CartSummaryDto>(cartKeys.summary(), {
          itemCount: newTotalItemCount,
          totalPrice: newTotalPrice,
          hasItems: newTotalItemCount > 0,
        });
      }

      return { previousCart, previousSummary };
    },
    onError: (_err, _itemId, context) => {
      // If the mutation fails, use the context returned from onMutate to roll back
      if (context?.previousCart) {
        queryClient.setQueryData(cartKeys.cart(), context.previousCart);
      }
      if (context?.previousSummary) {
        queryClient.setQueryData(cartKeys.summary(), context.previousSummary);
      }
    },
    onSuccess: (data) => {
      // Update cart data
      queryClient.setQueryData(cartKeys.cart(), data);

      // Update summary from cart data
      queryClient.setQueryData<CartSummaryDto>(cartKeys.summary(), {
        itemCount: data.totalItemCount,
        totalPrice: data.totalPrice,
        hasItems: !data.isEmpty,
      });
    },
  });
}

// Clear cart mutation
export function useClearCart() {
  const queryClient = useQueryClient();
  const { data: session } = useSession();

  return useMutation({
    mutationFn: () => {
      // Ensure user is authenticated before making API call
      if (!session?.authenticated) {
        throw new Error('Authentication required to clear cart');
      }
      return cartApi.clearCart();
    },
    onMutate: async () => {
      // Cancel any outgoing refetches
      await queryClient.cancelQueries({ queryKey: cartKeys.cart() });
      await queryClient.cancelQueries({ queryKey: cartKeys.summary() });

      // Snapshot the previous value
      const previousCart = queryClient.getQueryData<CartDto>(cartKeys.cart());
      const previousSummary = queryClient.getQueryData<CartSummaryDto>(cartKeys.summary());

      // Optimistically update to empty cart
      queryClient.setQueryData<CartSummaryDto>(cartKeys.summary(), {
        itemCount: 0,
        totalPrice: 0,
        hasItems: false,
      });

      return { previousCart, previousSummary };
    },
    onError: (_err, _variables, context) => {
      // If the mutation fails, use the context returned from onMutate to roll back
      if (context?.previousCart) {
        queryClient.setQueryData(cartKeys.cart(), context.previousCart);
      }
      if (context?.previousSummary) {
        queryClient.setQueryData(cartKeys.summary(), context.previousSummary);
      }
    },
    onSuccess: (data) => {
      // Update cart data
      queryClient.setQueryData(cartKeys.cart(), data);

      // Update summary from cart data
      queryClient.setQueryData<CartSummaryDto>(cartKeys.summary(), {
        itemCount: data.totalItemCount,
        totalPrice: data.totalPrice,
        hasItems: !data.isEmpty,
      });
    },
  });
}

// Refresh cart prices mutation
export function useRefreshCartPrices() {
  const queryClient = useQueryClient();
  const { data: session } = useSession();

  return useMutation({
    mutationFn: () => {
      // Ensure user is authenticated before making API call
      if (!session?.authenticated) {
        throw new Error('Authentication required to refresh cart prices');
      }
      return cartApi.refreshPrices();
    },
    onSuccess: (data) => {
      // Update cart data
      queryClient.setQueryData(cartKeys.cart(), data);

      // Update summary from cart data
      queryClient.setQueryData<CartSummaryDto>(cartKeys.summary(), {
        itemCount: data.totalItemCount,
        totalPrice: data.totalPrice,
        hasItems: !data.isEmpty,
      });
    },
  });
}
