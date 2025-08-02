import { LoadingSpinner } from '@/components/LoadingSpinner';
import { Button } from '@/components/ui/Button';
import { useSession } from '@/hooks/queries/useAuth';
import { useCart, useRemoveCartItem, useUpdateCartItem } from '@/hooks/queries/useCart';
import { useCartStore } from '@/stores/cartStore';
import { Minus, Plus, RefreshCw, ShoppingBag, Trash2 } from 'lucide-react';
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';

export default function CartPage() {
  const navigate = useNavigate();
  const { data: session } = useSession();

  // API-backed cart for authenticated users
  const { data: cartData, isLoading: cartLoading, error: cartError } = useCart();
  const updateCartItemMutation = useUpdateCartItem();
  const removeCartItemMutation = useRemoveCartItem();

  // Local cart store for non-authenticated users
  const localItems = useCartStore((state) => state.items);
  const updateLocalQuantity = useCartStore((state) => state.updateQuantity);
  const removeLocalItem = useCartStore((state) => state.removeItem);
  const getTotalLocalItems = useCartStore((state) => state.getTotalItems);
  const getTotalLocalPrice = useCartStore((state) => state.getTotalPrice);

  const [updatingItems, setUpdatingItems] = useState<Set<number | string>>(new Set());

  // Determine which data to use based on authentication status
  const isAuthenticated = session?.authenticated === true;
  const items = isAuthenticated ? cartData?.items || [] : localItems;
  const totalItems = isAuthenticated ? cartData?.totalItemCount || 0 : getTotalLocalItems();
  const totalPrice = isAuthenticated
    ? (cartData?.totalPrice || 0) / 100 // Convert cents to dollars
    : getTotalLocalPrice();
  const isLoading = isAuthenticated ? cartLoading : false;

  const handleUpdateQuantity = async (itemId: number | string, newQuantity: number) => {
    setUpdatingItems((prev) => new Set(prev).add(itemId));

    try {
      if (isAuthenticated && typeof itemId === 'number') {
        await updateCartItemMutation.mutateAsync({
          itemId,
          data: { quantity: newQuantity },
        });
      } else if (!isAuthenticated && typeof itemId === 'string') {
        updateLocalQuantity(itemId, newQuantity);
      }
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

  const handleRemoveItem = async (itemId: number | string) => {
    setUpdatingItems((prev) => new Set(prev).add(itemId));

    try {
      if (isAuthenticated && typeof itemId === 'number') {
        await removeCartItemMutation.mutateAsync(itemId);
      } else if (!isAuthenticated && typeof itemId === 'string') {
        removeLocalItem(itemId);
      }
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
    navigate('/editor');
  };

  // Loading state for authenticated users
  if (isLoading) {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <LoadingSpinner />
      </div>
    );
  }

  // Error state for authenticated users
  if (isAuthenticated && cartError) {
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

        {/* Authentication notice for non-authenticated users with items */}
        {!isAuthenticated && items.length > 0 && (
          <div className="mb-6 rounded-lg bg-blue-50 p-4">
            <div className="flex">
              <div className="flex-shrink-0">
                <svg className="h-5 w-5 text-blue-400" viewBox="0 0 20 20" fill="currentColor">
                  <path
                    fillRule="evenodd"
                    d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z"
                    clipRule="evenodd"
                  />
                </svg>
              </div>
              <div className="ml-3">
                <h3 className="text-sm font-medium text-blue-800">Sign in to save your cart</h3>
                <div className="mt-2 text-sm text-blue-700">
                  <p>Your items are currently stored locally. Sign in or create an account to save your cart across devices and sessions.</p>
                </div>
                <div className="mt-4">
                  <div className="-mx-2 -my-1.5 flex">
                    <button
                      type="button"
                      onClick={() => navigate('/login')}
                      className="rounded-md bg-blue-50 px-2 py-1.5 text-sm font-medium text-blue-800 hover:bg-blue-100 focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 focus:ring-offset-blue-50 focus:outline-none"
                    >
                      Sign In
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        )}

        <div className="lg:grid lg:grid-cols-12 lg:gap-x-12">
          <div className="lg:col-span-7">
            <div className="space-y-4">
              {items.map((item) => {
                // Handle different data structures for authenticated vs non-authenticated
                const itemId = isAuthenticated ? (item as any).id : item.id;
                const isUpdating = updatingItems.has(itemId);

                let displayItem, imageUrl, itemPrice, subtotal;

                if (isAuthenticated) {
                  // API cart item structure
                  const apiItem = item as any;
                  displayItem = {
                    name: apiItem.article.name,
                    variant: apiItem.variant,
                    quantity: apiItem.quantity,
                    price: apiItem.priceAtTime / 100, // Convert cents to dollars
                    hasPriceChanged: apiItem.hasPriceChanged,
                    originalPrice: apiItem.originalPrice / 100,
                  };
                  imageUrl = apiItem.customData?.imageUrl || apiItem.article.image;
                  itemPrice = displayItem.price;
                  subtotal = displayItem.price * displayItem.quantity;
                } else {
                  // Local cart item structure
                  const localItem = item as any;
                  displayItem = {
                    name: localItem.mug.name,
                    variant: localItem.variant,
                    quantity: localItem.quantity,
                    price: localItem.price,
                    prompt: localItem.prompt,
                  };
                  imageUrl = localItem.image;
                  itemPrice = displayItem.price;
                  subtotal = displayItem.price * displayItem.quantity;
                }

                // Handle different image URL formats
                if (imageUrl && !imageUrl.startsWith('http') && !imageUrl.startsWith('data:') && !imageUrl.startsWith('/api/')) {
                  imageUrl = `/api/images/${imageUrl}`;
                }

                return (
                  <div key={itemId} className="rounded-lg bg-white p-6 shadow-sm">
                    <div className="sm:flex sm:items-start">
                      <div className="flex-shrink-0">
                        <img
                          src={imageUrl || '/placeholder-mug.png'}
                          alt="Custom mug design"
                          className="h-24 w-24 rounded-md object-cover sm:h-32 sm:w-32"
                          onError={(e) => {
                            e.currentTarget.src = '/placeholder-mug.png';
                          }}
                        />
                      </div>
                      <div className="mt-4 sm:mt-0 sm:ml-6 sm:flex-1">
                        <div className="sm:flex sm:items-start sm:justify-between">
                          <div>
                            <h3 className="text-lg font-medium text-gray-900">{displayItem.name}</h3>
                            {displayItem.variant && <p className="mt-1 text-sm text-gray-500">Variant: {displayItem.variant.colorCode}</p>}
                            {!isAuthenticated && displayItem.prompt && (
                              <p className="mt-1 text-sm text-gray-500">Prompt: {displayItem.prompt.promptText || displayItem.prompt.title}</p>
                            )}
                            <div className="mt-1 flex items-center gap-2">
                              <p className="text-lg font-medium text-gray-900">${itemPrice.toFixed(2)}</p>
                              {isAuthenticated && displayItem.hasPriceChanged && (
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
