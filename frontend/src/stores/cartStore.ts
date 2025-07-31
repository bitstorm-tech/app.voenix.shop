import { CropData, GeneratedImageCropData, MugOption, MugVariant } from '@/components/editor/types';
import { Prompt } from '@/types/prompt';
import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import { immer } from 'zustand/middleware/immer';

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

  // Actions
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

export const useCartStore = create<CartState>()(
  persist(
    immer((set, get) => ({
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
            state.items[existingItemIndex].quantity += 1;
          } else {
            // Add new item
            state.items.push(newItem);
          }
        });
      },

      updateQuantity: (itemId, quantity) => {
        set((state) => {
          const itemIndex = state.items.findIndex((item) => item.id === itemId);
          if (itemIndex >= 0) {
            if (quantity <= 0) {
              // Remove item if quantity is 0 or less
              state.items.splice(itemIndex, 1);
            } else {
              state.items[itemIndex].quantity = quantity;
            }
          }
        });
      },

      removeItem: (itemId) => {
        set((state) => {
          const itemIndex = state.items.findIndex((item) => item.id === itemId);
          if (itemIndex >= 0) {
            state.items.splice(itemIndex, 1);
          }
        });
      },

      clearCart: () => {
        set((state) => {
          state.items = [];
        });
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
    })),
    {
      name: 'voenix-cart-storage', // localStorage key
      partialize: (state) => ({ items: state.items }), // Only persist items
    },
  ),
);
