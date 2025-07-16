import { Button } from '@/components/ui/Button';
import { Minus, Plus, ShoppingBag, Trash2 } from 'lucide-react';
import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';

interface CartItem {
  id: number;
  mug: {
    id: number;
    name: string;
    image: string;
  };
  generated_image_path: string;
  original_image_path: string | null;
  quantity: number;
  price: number;
  subtotal: number;
  prompt: {
    id: number;
    text: string;
  } | null;
}

interface Cart {
  id: number;
  items: CartItem[];
  subtotal: number;
  total_items: number;
}

// Dummy cart data
const DUMMY_CART: Cart = {
  id: 1,
  items: [
    {
      id: 1,
      mug: {
        id: 1,
        name: 'Classic White Mug',
        image: 'mug-white.jpg',
      },
      generated_image_path: 'generated-1.jpg',
      original_image_path: null,
      quantity: 2,
      price: 19.99,
      subtotal: 39.98,
      prompt: {
        id: 1,
        text: 'A cute cat playing with yarn',
      },
    },
    {
      id: 2,
      mug: {
        id: 2,
        name: 'Black Coffee Mug',
        image: 'mug-black.jpg',
      },
      generated_image_path: 'generated-2.jpg',
      original_image_path: 'upload-1.jpg',
      quantity: 1,
      price: 24.99,
      subtotal: 24.99,
      prompt: null,
    },
  ],
  subtotal: 64.97,
  total_items: 3,
};

export default function CartPage() {
  const navigate = useNavigate();
  const [cart, setCart] = useState<Cart | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [updatingItems, setUpdatingItems] = useState<Set<number>>(new Set());

  const fetchCart = async () => {
    try {
      // Simulate API delay
      await new Promise((resolve) => setTimeout(resolve, 500));

      // Set dummy cart data
      setCart(DUMMY_CART);
    } catch (error) {
      console.error('Error fetching cart:', error);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchCart();
  }, []);

  const updateQuantity = async (itemId: number, newQuantity: number) => {
    setUpdatingItems((prev) => new Set(prev).add(itemId));

    try {
      // Simulate API delay
      await new Promise((resolve) => setTimeout(resolve, 300));

      // Update cart state locally
      setCart((prevCart) => {
        if (!prevCart) return null;

        const updatedItems = prevCart.items.map((item) => {
          if (item.id === itemId) {
            const updatedItem = { ...item, quantity: newQuantity };
            updatedItem.subtotal = updatedItem.price * newQuantity;
            return updatedItem;
          }
          return item;
        });

        const newSubtotal = updatedItems.reduce((sum, item) => sum + item.subtotal, 0);
        const newTotalItems = updatedItems.reduce((sum, item) => sum + item.quantity, 0);

        return {
          ...prevCart,
          items: updatedItems,
          subtotal: newSubtotal,
          total_items: newTotalItems,
        };
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

  const removeItem = async (itemId: number) => {
    setUpdatingItems((prev) => new Set(prev).add(itemId));

    try {
      // Simulate API delay
      await new Promise((resolve) => setTimeout(resolve, 300));

      // Remove item from cart state
      setCart((prevCart) => {
        if (!prevCart) return null;

        const updatedItems = prevCart.items.filter((item) => item.id !== itemId);

        if (updatedItems.length === 0) {
          return null;
        }

        const newSubtotal = updatedItems.reduce((sum, item) => sum + item.subtotal, 0);
        const newTotalItems = updatedItems.reduce((sum, item) => sum + item.quantity, 0);

        return {
          ...prevCart,
          items: updatedItems,
          subtotal: newSubtotal,
          total_items: newTotalItems,
        };
      });
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

  if (isLoading) {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <div className="text-center">
          <div className="inline-block h-8 w-8 animate-spin rounded-full border-4 border-solid border-current border-r-transparent"></div>
          <p className="mt-2 text-sm text-gray-600">Loading cart...</p>
        </div>
      </div>
    );
  }

  if (!cart || cart.items.length === 0) {
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
              {cart.items.map((item) => {
                const isUpdating = updatingItems.has(item.id);
                return (
                  <div key={item.id} className="rounded-lg bg-white p-6 shadow-sm">
                    <div className="sm:flex sm:items-start">
                      <div className="flex-shrink-0">
                        <img
                          src={`/api/images/${item.generated_image_path}`}
                          alt="Custom mug design"
                          className="h-24 w-24 rounded-md object-cover sm:h-32 sm:w-32"
                        />
                      </div>
                      <div className="mt-4 sm:mt-0 sm:ml-6 sm:flex-1">
                        <div className="sm:flex sm:items-start sm:justify-between">
                          <div>
                            <h3 className="text-lg font-medium text-gray-900">{item.mug.name}</h3>
                            {item.prompt && <p className="mt-1 text-sm text-gray-500">Prompt: {item.prompt.text}</p>}
                            <p className="mt-1 text-lg font-medium text-gray-900">${item.price.toFixed(2)}</p>
                          </div>
                          <div className="mt-4 sm:mt-0">
                            <button
                              onClick={() => removeItem(item.id)}
                              disabled={isUpdating}
                              className="text-red-600 hover:text-red-500 disabled:opacity-50"
                            >
                              <Trash2 className="h-5 w-5" />
                            </button>
                          </div>
                        </div>
                        <div className="mt-4 flex items-center">
                          <button
                            onClick={() => updateQuantity(item.id, Math.max(1, item.quantity - 1))}
                            disabled={isUpdating || item.quantity <= 1}
                            className="rounded-md bg-gray-100 p-1 hover:bg-gray-200 disabled:opacity-50"
                          >
                            <Minus className="h-4 w-4" />
                          </button>
                          <span className="mx-4 text-gray-900">{item.quantity}</span>
                          <button
                            onClick={() => updateQuantity(item.id, item.quantity + 1)}
                            disabled={isUpdating}
                            className="rounded-md bg-gray-100 p-1 hover:bg-gray-200 disabled:opacity-50"
                          >
                            <Plus className="h-4 w-4" />
                          </button>
                          <span className="ml-6 text-lg font-medium text-gray-900">Subtotal: ${item.subtotal.toFixed(2)}</span>
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
                  <span className="text-gray-600">Subtotal ({cart.total_items} items)</span>
                  <span className="font-medium text-gray-900">${cart.subtotal.toFixed(2)}</span>
                </div>
                <div className="border-t pt-3">
                  <div className="flex items-center justify-between">
                    <span className="text-lg font-medium text-gray-900">Total</span>
                    <span className="text-lg font-medium text-gray-900">${cart.subtotal.toFixed(2)}</span>
                  </div>
                </div>
              </div>
              <Button onClick={handleCheckout} className="mt-6 w-full">
                Checkout • ${cart.subtotal.toFixed(2)}
              </Button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
