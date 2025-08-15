import { CartItemImage } from '@/components/CartItemImage';
import { LoadingSpinner } from '@/components/LoadingSpinner';
import { Button } from '@/components/ui/Button';
import { useSession } from '@/hooks/queries/useAuth';
import { useCart, useRemoveCartItem, useUpdateCartItem } from '@/hooks/queries/useCart';
import { imagePreloader } from '@/lib/imagePreloader';
import { Minus, Plus, RefreshCw, ShoppingBag, Trash2 } from 'lucide-react';
import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';

export default function CartPage() {
  const navigate = useNavigate();
  const { data: session, isLoading: sessionLoading } = useSession();

  // Redirect to login if not authenticated
  useEffect(() => {
    if (!sessionLoading && !session?.authenticated) {
      const returnUrl = encodeURIComponent(window.location.pathname);
      navigate(`/login?returnUrl=${returnUrl}`);
    }
  }, [session, sessionLoading, navigate]);

  // API-backed cart for authenticated users
  const { data: cartData, isLoading: cartLoading, error: cartError } = useCart();
  const updateCartItemMutation = useUpdateCartItem();
  const removeCartItemMutation = useRemoveCartItem();

  const [updatingItems, setUpdatingItems] = useState<Set<number>>(new Set());

  // Only show cart for authenticated users
  const items = cartData?.items || [];
  const totalItems = cartData?.totalItemCount || 0;
  const totalPrice = (cartData?.totalPrice || 0) / 100; // Convert cents to dollars

  // Preload cart images for better performance
  useEffect(() => {
    if (items.length > 0) {
      const imagesToPreload = items
        .filter((item) => item.generatedImageFilename)
        .map((item, index) => ({
          src: `/api/user/images/${item.generatedImageFilename}`,
          priority: (index < 3 ? 'high' : 'medium') as 'high' | 'medium' | 'low', // First 3 images get high priority
        }));

      if (imagesToPreload.length > 0) {
        imagePreloader.preloadImages(imagesToPreload).catch((error) => {
          console.log('Some images failed to preload:', error);
          // Non-critical error, don't show to user
        });
      }
    }
  }, [items]);

  const handleUpdateQuantity = async (itemId: number, newQuantity: number) => {
    setUpdatingItems((prev) => new Set(prev).add(itemId));

    try {
      await updateCartItemMutation.mutateAsync({
        itemId,
        data: { quantity: newQuantity },
      });
    } catch (error) {
      console.error('Error updating quantity:', error);
    } finally {
      setUpdatingItems((prev) => {
        const newSet = new Set(prev);
        newSet.delete(itemId);
        return newSet;
      });
    }
  };

  const handleRemoveItem = async (itemId: number) => {
    setUpdatingItems((prev) => new Set(prev).add(itemId));

    try {
      await removeCartItemMutation.mutateAsync(itemId);
    } catch (error) {
      console.error('Error removing item:', error);
    } finally {
      setUpdatingItems((prev) => {
        const newSet = new Set(prev);
        newSet.delete(itemId);
        return newSet;
      });
    }
  };

  const handleCheckout = () => {
    navigate('/checkout');
  };

  const handleContinueShopping = () => {
    window.location.href = '/editor';
  };

  // Don't render anything while checking authentication
  if (sessionLoading) {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <LoadingSpinner />
      </div>
    );
  }

  // Don't render if not authenticated (redirect will happen)
  if (!session?.authenticated) {
    return null;
  }

  // Loading state for cart data
  if (cartLoading) {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <LoadingSpinner />
      </div>
    );
  }

  // Error state for cart
  if (cartError) {
    return (
      <div className="flex min-h-screen items-center justify-center px-4">
        <div className="text-center">
          <div className="mx-auto h-12 w-12 text-red-500">
            <RefreshCw className="h-12 w-12" />
          </div>
          <h2 className="mt-4 text-lg font-medium text-gray-900">Error loading cart</h2>
          <p className="mt-2 text-sm text-gray-600">{cartError instanceof Error ? cartError.message : 'Unable to load your cart'}</p>
          <Button onClick={() => window.location.reload()} className="mt-6">
            Try Again
          </Button>
        </div>
      </div>
    );
  }

  if (items.length === 0) {
    return (
      <div className="flex min-h-screen items-center justify-center px-4">
        <div className="text-center">
          <ShoppingBag className="mx-auto h-12 w-12 text-gray-400" />
          <h2 className="mt-4 text-lg font-medium text-gray-900">Your cart is empty</h2>
          <p className="mt-2 text-sm text-gray-600">Start by designing your custom mug</p>
          <Button onClick={handleContinueShopping} className="mt-6">
            Design a Mug
          </Button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <h1 className="mb-8 text-3xl font-bold">Shopping Cart</h1>

        <div className="lg:grid lg:grid-cols-12 lg:gap-x-12">
          <div className="lg:col-span-7">
            <div className="space-y-4">
              {items.map((item) => {
                const itemId = item.id;
                const isUpdating = updatingItems.has(itemId);

                // API cart item structure
                const displayItem = {
                  name: item.article.name,
                  variant: item.variant,
                  quantity: item.quantity,
                  price: item.priceAtTime / 100, // Convert cents to dollars
                  hasPriceChanged: item.hasPriceChanged,
                  originalPrice: item.originalPrice / 100,
                };

                // Construct image URL from generatedImageFilename
                const imageUrl = item.generatedImageFilename ? `/api/user/images/${item.generatedImageFilename}` : undefined;

                const itemPrice = displayItem.price;
                const subtotal = displayItem.price * displayItem.quantity;

                return (
                  <div key={itemId} className="rounded-lg bg-white p-6 shadow-sm">
                    <div className="sm:flex sm:items-start">
                      <CartItemImage
                        src={imageUrl}
                        alt={`${displayItem.name}${displayItem.variant ? ` - ${displayItem.variant.colorCode}` : ''} design preview`}
                        className="h-24 w-24 rounded-md object-cover sm:h-32 sm:w-32"
                        onLoad={() => {
                          // Optional: Analytics tracking for successful image loads
                          console.log(`Image loaded successfully for item ${itemId}`);
                        }}
                        onError={(error) => {
                          // Optional: Error tracking for failed image loads
                          console.error(`Image failed to load for item ${itemId}:`, error);
                        }}
                      />
                      <div className="mt-4 sm:mt-0 sm:ml-6 sm:flex-1">
                        <div className="sm:flex sm:items-start sm:justify-between">
                          <div>
                            <h3 className="text-lg font-medium text-gray-900">{displayItem.name}</h3>
                            {displayItem.variant && <p className="mt-1 text-sm text-gray-500">Variant: {displayItem.variant.colorCode}</p>}
                            <div className="mt-1 flex items-center gap-2">
                              <p className="text-lg font-medium text-gray-900">${itemPrice.toFixed(2)}</p>
                              {displayItem.hasPriceChanged && (
                                <span className="text-sm text-orange-600">(was ${displayItem.originalPrice?.toFixed(2)})</span>
                              )}
                            </div>
                          </div>
                          <div className="mt-4 sm:mt-0">
                            <button
                              onClick={() => handleRemoveItem(itemId)}
                              disabled={isUpdating}
                              className="text-red-600 hover:text-red-500 disabled:opacity-50"
                            >
                              <Trash2 className="h-5 w-5" />
                            </button>
                          </div>
                        </div>
                        <div className="mt-4 flex items-center">
                          <button
                            onClick={() => handleUpdateQuantity(itemId, Math.max(1, displayItem.quantity - 1))}
                            disabled={isUpdating || displayItem.quantity <= 1}
                            className="rounded-md bg-gray-100 p-1 hover:bg-gray-200 disabled:opacity-50"
                          >
                            <Minus className="h-4 w-4" />
                          </button>
                          <span className="mx-4 text-gray-900">{displayItem.quantity}</span>
                          <button
                            onClick={() => handleUpdateQuantity(itemId, displayItem.quantity + 1)}
                            disabled={isUpdating}
                            className="rounded-md bg-gray-100 p-1 hover:bg-gray-200 disabled:opacity-50"
                          >
                            <Plus className="h-4 w-4" />
                          </button>
                          <span className="ml-6 text-lg font-medium text-gray-900">Subtotal: ${subtotal.toFixed(2)}</span>
                        </div>
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>

            <div className="mt-6">
              <Button variant="outline" onClick={handleContinueShopping} className="w-full sm:w-auto">
                Design Another Mug
              </Button>
            </div>
          </div>

          <div className="mt-8 lg:col-span-5 lg:mt-0">
            <div className="rounded-lg bg-white p-6 shadow-sm">
              <h2 className="text-lg font-medium text-gray-900">Order Summary</h2>
              <div className="mt-4 space-y-3">
                <div className="flex items-center justify-between">
                  <span className="text-gray-600">Subtotal ({totalItems} items)</span>
                  <span className="font-medium text-gray-900">${totalPrice.toFixed(2)}</span>
                </div>
                <div className="border-t pt-3">
                  <div className="flex items-center justify-between">
                    <span className="text-lg font-medium text-gray-900">Total</span>
                    <span className="text-lg font-medium text-gray-900">${totalPrice.toFixed(2)}</span>
                  </div>
                </div>
              </div>
              <Button onClick={handleCheckout} className="mt-6 w-full">
                Checkout • ${totalPrice.toFixed(2)}
              </Button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
