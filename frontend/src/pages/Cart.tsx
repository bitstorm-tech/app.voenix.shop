import { Button } from '@/components/ui/Button';
import { useCartStore } from '@/stores/cartStore';
import { Minus, Plus, ShoppingBag, Trash2 } from 'lucide-react';
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';

export default function CartPage() {
  const navigate = useNavigate();
  const items = useCartStore((state) => state.items);
  const updateQuantity = useCartStore((state) => state.updateQuantity);
  const removeItem = useCartStore((state) => state.removeItem);
  const getTotalItems = useCartStore((state) => state.getTotalItems);
  const getTotalPrice = useCartStore((state) => state.getTotalPrice);
  const [updatingItems, setUpdatingItems] = useState<Set<string>>(new Set());

  const handleUpdateQuantity = async (itemId: string, newQuantity: number) => {
    setUpdatingItems((prev) => new Set(prev).add(itemId));

    try {
      updateQuantity(itemId, newQuantity);
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

  const handleRemoveItem = async (itemId: string) => {
    setUpdatingItems((prev) => new Set(prev).add(itemId));

    try {
      removeItem(itemId);
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

  const totalItems = getTotalItems();
  const totalPrice = getTotalPrice();

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
                const isUpdating = updatingItems.has(item.id);
                const subtotal = item.price * item.quantity;

                // Handle different image URL formats
                let imageUrl = item.image;
                if (!item.image.startsWith('http') && !item.image.startsWith('data:') && !item.image.startsWith('/api/')) {
                  imageUrl = `/api/images/${item.image}`;
                }

                return (
                  <div key={item.id} className="rounded-lg bg-white p-6 shadow-sm">
                    <div className="sm:flex sm:items-start">
                      <div className="flex-shrink-0">
                        <img src={imageUrl} alt="Custom mug design" className="h-24 w-24 rounded-md object-cover sm:h-32 sm:w-32" />
                      </div>
                      <div className="mt-4 sm:mt-0 sm:ml-6 sm:flex-1">
                        <div className="sm:flex sm:items-start sm:justify-between">
                          <div>
                            <h3 className="text-lg font-medium text-gray-900">{item.mug.name}</h3>
                            {item.variant && <p className="mt-1 text-sm text-gray-500">Variant: {item.variant.colorCode}</p>}
                            {item.prompt && <p className="mt-1 text-sm text-gray-500">Prompt: {item.prompt.promptText}</p>}
                            <p className="mt-1 text-lg font-medium text-gray-900">${item.price.toFixed(2)}</p>
                          </div>
                          <div className="mt-4 sm:mt-0">
                            <button
                              onClick={() => handleRemoveItem(item.id)}
                              disabled={isUpdating}
                              className="text-red-600 hover:text-red-500 disabled:opacity-50"
                            >
                              <Trash2 className="h-5 w-5" />
                            </button>
                          </div>
                        </div>
                        <div className="mt-4 flex items-center">
                          <button
                            onClick={() => handleUpdateQuantity(item.id, Math.max(1, item.quantity - 1))}
                            disabled={isUpdating || item.quantity <= 1}
                            className="rounded-md bg-gray-100 p-1 hover:bg-gray-200 disabled:opacity-50"
                          >
                            <Minus className="h-4 w-4" />
                          </button>
                          <span className="mx-4 text-gray-900">{item.quantity}</span>
                          <button
                            onClick={() => handleUpdateQuantity(item.id, item.quantity + 1)}
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
