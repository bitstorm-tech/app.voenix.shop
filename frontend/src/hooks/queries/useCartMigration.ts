import { useCartStore } from '@/stores/cartStore';
import { useEffect } from 'react';
import { useAddToCart } from './useCart';

/**
 * Hook to migrate local cart items to the backend when user logs in
 * This ensures items added before authentication are preserved
 */
export function useCartMigration(isAuthenticated: boolean) {
  const localItems = useCartStore((state) => state.items);
  const clearLocalCart = useCartStore((state) => state.clearCart);
  const addToCartMutation = useAddToCart();

  useEffect(() => {
    // Only migrate if user just became authenticated and has local items
    if (isAuthenticated && localItems.length > 0) {
      const migrateItems = async () => {
        try {
          // Migrate each local item to the backend
          for (const item of localItems) {
            await addToCartMutation.mutateAsync({
              articleId: item.mug.id,
              variantId: item.variant?.id || 0,
              quantity: item.quantity,
              customData: {
                imageUrl: item.image,
                cropData: item.cropData,
                promptInfo: item.prompt
                  ? {
                      promptId: item.prompt.id,
                      promptText: item.prompt.promptText || item.prompt.title,
                    }
                  : undefined,
              },
            });
          }

          // Clear local cart after successful migration
          clearLocalCart();
        } catch (error) {
          console.error('Failed to migrate cart items:', error);
          // Don't clear local cart on error so items aren't lost
        }
      };

      migrateItems();
    }
  }, [isAuthenticated, localItems.length]);

  return {
    isMigrating: addToCartMutation.isPending && localItems.length > 0,
    migrationError: addToCartMutation.error,
  };
}
