import { CartItemImage } from '@/components/CartItemImage';
import { LoadingSpinner } from '@/components/LoadingSpinner';
import { Button } from '@/components/ui/Button';
import { useSession } from '@/hooks/queries/useAuth';
import { useCart, useRemoveCartItem, useUpdateCartItem } from '@/hooks/queries/useCart';
import { imagePreloader } from '@/lib/imagePreloader';
import { Minus, Plus, RefreshCw, ShoppingBag, Trash2 } from 'lucide-react';
import { useEffect, useMemo, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useNavigate } from 'react-router-dom';

export default function CartPage() {
  const navigate = useNavigate();
  const { data: session, isLoading: sessionLoading } = useSession();
  const { t } = useTranslation('cart');

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
  const items = useMemo(() => cartData?.items || [], [cartData?.items]);
  const totalItems = cartData?.totalItemCount || 0;
  const totalPriceCents = cartData?.totalPrice || 0;
  const formatCents = (value: number) => (value / 100).toFixed(2);
  const formatCurrency = (value: number) => t('currency', { value: formatCents(value) });

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
          <h2 className="mt-4 text-lg font-medium text-gray-900">{t('error.heading')}</h2>
          <p className="mt-2 text-sm text-gray-600">{cartError instanceof Error ? cartError.message : t('error.description')}</p>
          <Button onClick={() => window.location.reload()} className="mt-6">
            {t('actions.tryAgain')}
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
          <h2 className="mt-4 text-lg font-medium text-gray-900">{t('empty.heading')}</h2>
          <p className="mt-2 text-sm text-gray-600">{t('empty.body')}</p>
          <Button onClick={handleContinueShopping} className="mt-6">
            {t('empty.cta')}
          </Button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <h1 className="mb-8 text-3xl font-bold">{t('title')}</h1>

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
                };

                // Construct image URL from generatedImageFilename
                const imageUrl = item.generatedImageFilename ? `/api/user/images/${item.generatedImageFilename}` : undefined;

                const articlePriceCents = item.articlePriceAtTime ?? item.priceAtTime;
                const promptPriceCents = item.promptPriceAtTime ?? 0;
                const articleOriginalCents = item.articleOriginalPrice ?? item.originalPrice;
                const promptOriginalCents = item.promptOriginalPrice ?? 0;
                const combinedUnitPriceCents = articlePriceCents + promptPriceCents;
                const subtotalCents = combinedUnitPriceCents * item.quantity;
                const showPromptLine = (item.promptId ?? null) !== null || promptPriceCents > 0 || promptOriginalCents > 0;
                const promptLineLabel = item.promptTitle ?? t('item.promptDefaultLabel');
                const articlePriceChanged = articleOriginalCents > 0 && articleOriginalCents !== articlePriceCents;
                const promptPriceChanged = item.hasPromptPriceChanged && promptOriginalCents > 0 && promptOriginalCents !== promptPriceCents;

                return (
                  <div key={itemId} className="rounded-lg bg-white p-6 shadow-sm">
                    <div className="sm:flex sm:items-start">
                      <CartItemImage
                        src={imageUrl}
                        alt={
                          displayItem.variant
                            ? t('item.imageAltWithColor', { name: displayItem.name, color: displayItem.variant.colorCode })
                            : t('item.imageAlt', { name: displayItem.name })
                        }
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
                            {displayItem.variant && (
                              <p className="mt-1 text-sm text-gray-500">{t('item.variantLabel', { color: displayItem.variant.colorCode })}</p>
                            )}
                            <div className="mt-1 flex items-center gap-2">
                              <p className="text-lg font-medium text-gray-900">{formatCurrency(articlePriceCents)}</p>
                              {articlePriceChanged && (
                                <span className="text-sm text-orange-600">
                                  {t('item.priceWas', { amount: formatCurrency(articleOriginalCents) })}
                                </span>
                              )}
                            </div>
                            {showPromptLine && (
                              <div className="mt-2 text-sm text-gray-700" aria-live="polite">
                                <span className="font-medium">{t('item.promptPriceLabel', { label: promptLineLabel })}</span>{' '}
                                <span>{formatCurrency(promptPriceCents)}</span>
                                {promptPriceChanged && (
                                  <span className="ml-2 text-orange-600">{t('item.priceWas', { amount: formatCurrency(promptOriginalCents) })}</span>
                                )}
                              </div>
                            )}
                          </div>
                          <div className="mt-4 sm:mt-0">
                            <button
                              onClick={() => handleRemoveItem(itemId)}
                              disabled={isUpdating}
                              className="text-red-600 hover:text-red-500 disabled:opacity-50"
                            >
                              <Trash2 className="h-5 w-5" />
                              <span className="sr-only">{t('item.remove')}</span>
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
                          <span className="ml-6 text-lg font-medium text-gray-900">
                            {t('item.subtotal', { amount: formatCurrency(subtotalCents) })}
                          </span>
                        </div>
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>

            <div className="mt-6">
              <Button variant="outline" onClick={handleContinueShopping} className="w-full sm:w-auto">
                {t('continueShopping')}
              </Button>
            </div>
          </div>

          <div className="mt-8 lg:col-span-5 lg:mt-0">
            <div className="rounded-lg bg-white p-6 shadow-sm">
              <h2 className="text-lg font-medium text-gray-900">{t('summary.heading')}</h2>
              <div className="mt-4 space-y-3">
                <div className="flex items-center justify-between">
                  <span className="text-gray-600">{t('summary.subtotal', { count: totalItems })}</span>
                  <span className="font-medium text-gray-900">{formatCurrency(totalPriceCents)}</span>
                </div>
                <div className="border-t pt-3">
                  <div className="flex items-center justify-between">
                    <span className="text-lg font-medium text-gray-900">{t('summary.total')}</span>
                    <span className="text-lg font-medium text-gray-900">{formatCurrency(totalPriceCents)}</span>
                  </div>
                </div>
              </div>
              <Button onClick={handleCheckout} className="mt-6 w-full">
                {t('summary.checkout', { amount: formatCurrency(totalPriceCents) })}
              </Button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
