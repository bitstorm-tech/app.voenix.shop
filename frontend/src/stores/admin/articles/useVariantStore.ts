import type { ArticleMugVariant, ArticleShirtVariant, CreateArticleMugVariantRequest, CreateArticleShirtVariantRequest } from '@/types/article';
import { create } from 'zustand';
import { immer } from 'zustand/middleware/immer';

interface VariantStoreState {
  // Existing variants (from database)
  mugVariants: ArticleMugVariant[];
  shirtVariants: ArticleShirtVariant[];

  // Temporary variants (for new articles)
  temporaryMugVariants: CreateArticleMugVariantRequest[];
  temporaryShirtVariants: CreateArticleShirtVariantRequest[];

  // Actions for mug variants
  setMugVariants: (variants: ArticleMugVariant[]) => void;
  addTemporaryMugVariant: (variant: CreateArticleMugVariantRequest) => void;
  updateTemporaryMugVariant: (index: number, variant: CreateArticleMugVariantRequest) => void;
  deleteTemporaryMugVariant: (index: number) => void;
  clearTemporaryMugVariants: () => void;

  // Actions for shirt variants
  setShirtVariants: (variants: ArticleShirtVariant[]) => void;
  addTemporaryShirtVariant: (variant: CreateArticleShirtVariantRequest) => void;
  updateTemporaryShirtVariant: (index: number, variant: CreateArticleShirtVariantRequest) => void;
  deleteTemporaryShirtVariant: (index: number) => void;
  clearTemporaryShirtVariants: () => void;

  // Reset all variants
  resetVariants: () => void;
}

export const useVariantStore = create<VariantStoreState>()(
  immer((set) => ({
    // Initial state
    mugVariants: [],
    shirtVariants: [],
    temporaryMugVariants: [],
    temporaryShirtVariants: [],

    // Mug variant actions
    setMugVariants: (variants) => {
      set((state) => {
        state.mugVariants = variants;
      });
    },

    addTemporaryMugVariant: (variant) => {
      set((state) => {
        state.temporaryMugVariants.push(variant);
      });
    },

    updateTemporaryMugVariant: (index, variant) => {
      set((state) => {
        if (index >= 0 && index < state.temporaryMugVariants.length) {
          state.temporaryMugVariants[index] = variant;
        }
      });
    },

    deleteTemporaryMugVariant: (index) => {
      set((state) => {
        if (index >= 0 && index < state.temporaryMugVariants.length) {
          state.temporaryMugVariants.splice(index, 1);
        }
      });
    },

    clearTemporaryMugVariants: () => {
      set((state) => {
        state.temporaryMugVariants = [];
      });
    },

    // Shirt variant actions
    setShirtVariants: (variants) => {
      set((state) => {
        state.shirtVariants = variants;
      });
    },

    addTemporaryShirtVariant: (variant) => {
      set((state) => {
        state.temporaryShirtVariants.push(variant);
      });
    },

    updateTemporaryShirtVariant: (index, variant) => {
      set((state) => {
        if (index >= 0 && index < state.temporaryShirtVariants.length) {
          state.temporaryShirtVariants[index] = variant;
        }
      });
    },

    deleteTemporaryShirtVariant: (index) => {
      set((state) => {
        if (index >= 0 && index < state.temporaryShirtVariants.length) {
          state.temporaryShirtVariants.splice(index, 1);
        }
      });
    },

    clearTemporaryShirtVariants: () => {
      set((state) => {
        state.temporaryShirtVariants = [];
      });
    },

    // Reset all variants
    resetVariants: () => {
      set((state) => {
        state.mugVariants = [];
        state.shirtVariants = [];
        state.temporaryMugVariants = [];
        state.temporaryShirtVariants = [];
      });
    },
  })),
);
