import { CropData, GeneratedImageCropData, MugOption, MugVariant } from '@/components/editor/types';
import { Prompt } from '@/types/prompt';
import { create } from 'zustand';

export interface CartItem {
  id: string; // Unique identifier for the cart item
  mug: MugOption;
  variant: MugVariant | null;
  image: string; // Generated or uploaded image URL
  cropData: CropData | GeneratedImageCropData | null;
  prompt: Prompt | null;
  quantity: number;
  price: number; // Price per item
}

interface CartState {
  items: CartItem[];

  // Actions - Note: These are now compatibility methods for local cart before authentication
  addItem: (item: Omit<CartItem, 'id' | 'quantity'>) => void;
  updateQuantity: (itemId: string, quantity: number) => void;
  removeItem: (itemId: string) => void;
  clearCart: () => void;

  // Computed values
  getTotalItems: () => number;
  getTotalPrice: () => number;
  getItem: (itemId: string) => CartItem | undefined;
}

// Helper function to generate unique ID
function generateCartItemId(): string {
  return `cart-item-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;
}

// Note: This store is now primarily used for temporary local storage before authentication
// For authenticated users, use the useCart hooks which integrate with the backend API
export const useCartStore = create<CartState>()((set, get) => ({
  items: [],

  addItem: (itemData) => {
    set((state) => {
      const newItem: CartItem = {
        ...itemData,
        id: generateCartItemId(),
        quantity: 1,
      };

      // Check if similar item already exists (same mug, variant, image)
      const existingItemIndex = state.items.findIndex(
        (item) => item.mug.id === newItem.mug.id && item.variant?.id === newItem.variant?.id && item.image === newItem.image,
      );

      if (existingItemIndex >= 0) {
        // Increment quantity of existing item
        const updatedItems = [...state.items];
        updatedItems[existingItemIndex] = {
          ...updatedItems[existingItemIndex],
          quantity: updatedItems[existingItemIndex].quantity + 1,
        };
        return { ...state, items: updatedItems };
      } else {
        // Add new item
        return { ...state, items: [...state.items, newItem] };
      }
    });
  },

  updateQuantity: (itemId, quantity) => {
    set((state) => {
      if (quantity <= 0) {
        // Remove item if quantity is 0 or less
        return { ...state, items: state.items.filter((item) => item.id !== itemId) };
      } else {
        // Update quantity
        const updatedItems = state.items.map((item) => (item.id === itemId ? { ...item, quantity } : item));
        return { ...state, items: updatedItems };
      }
    });
  },

  removeItem: (itemId) => {
    set((state) => ({
      ...state,
      items: state.items.filter((item) => item.id !== itemId),
    }));
  },

  clearCart: () => {
    set({ items: [] });
  },

  getTotalItems: () => {
    const state = get();
    return state.items.reduce((total, item) => total + item.quantity, 0);
  },

  getTotalPrice: () => {
    const state = get();
    return state.items.reduce((total, item) => total + item.price * item.quantity, 0);
  },

  getItem: (itemId) => {
    const state = get();
    return state.items.find((item) => item.id === itemId);
  },
}));
