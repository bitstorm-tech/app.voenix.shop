import { articleCategoriesApi, articlesApi, articleSubCategoriesApi } from '@/lib/api';
import type { Article, ArticleType, CreateMugDetailsRequest, CreateShirtDetailsRequest } from '@/types/article';
import type { ArticleCategory, ArticleSubCategory } from '@/types/mug';
import { toast } from 'sonner';
import { create } from 'zustand';
import { immer } from 'zustand/middleware/immer';

interface ArticleFormState {
  // Form data
  article: Partial<Article>;
  categories: ArticleCategory[];
  subcategories: ArticleSubCategory[];

  // UI state
  isLoading: boolean;
  isSaving: boolean;
  isEdit: boolean;

  // Actions
  initializeForm: (articleId?: number) => Promise<void>;
  updateArticle: <K extends keyof Article>(field: K, value: Article[K]) => void;
  updateMugDetails: (details: Partial<CreateMugDetailsRequest>) => void;
  updateShirtDetails: (details: Partial<CreateShirtDetailsRequest>) => void;
  setArticleType: (type: ArticleType) => void;
  setCategory: (categoryId: number) => void;
  setSubcategory: (subcategoryId?: number) => void;
  loadCategories: () => Promise<void>;
  loadSubcategories: (categoryId: number) => Promise<void>;
  resetForm: () => void;
}

const initialArticle: Partial<Article> = {
  name: '',
  descriptionShort: '',
  descriptionLong: '',
  active: true,
  articleType: 'MUG',
  categoryId: 0,
  supplierId: undefined,
  mugVariants: [],
  shirtVariants: [],
  mugDetails: {
    articleId: 0,
    heightMm: 0,
    diameterMm: 0,
    printTemplateWidthMm: 0,
    printTemplateHeightMm: 0,
    fillingQuantity: '',
    dishwasherSafe: true,
  },
};

export const useArticleFormStore = create<ArticleFormState>()(
  immer((set, get) => ({
    // Initial state
    article: { ...initialArticle },
    categories: [],
    subcategories: [],
    isLoading: false,
    isSaving: false,
    isEdit: false,

    // Initialize form (load article if editing)
    initializeForm: async (articleId?: number) => {
      set((state) => {
        state.isLoading = true;
        state.isEdit = !!articleId;
      });

      try {
        // Load categories
        await get().loadCategories();

        if (articleId) {
          // Load existing article
          const data = await articlesApi.getById(articleId);
          set((state) => {
            state.article = data;
          });

          // Load subcategories for the article's category
          if (data.categoryId) {
            await get().loadSubcategories(data.categoryId);
          }
        } else {
          // Reset to initial state for new article
          set((state) => {
            state.article = { ...initialArticle };
          });
        }
      } catch (error) {
        console.error('Error initializing form:', error);
        toast.error('Failed to load article data');
      } finally {
        set((state) => {
          state.isLoading = false;
        });
      }
    },

    // Update article field
    updateArticle: (field, value) => {
      set((state) => {
        state.article[field] = value;
      });
    },

    // Update mug details
    updateMugDetails: (details) => {
      set((state) => {
        if (!state.article.mugDetails) {
          state.article.mugDetails = {
            articleId: 0,
            heightMm: 0,
            diameterMm: 0,
            printTemplateWidthMm: 0,
            printTemplateHeightMm: 0,
            fillingQuantity: '',
            dishwasherSafe: true,
          };
        }
        Object.assign(state.article.mugDetails, details);
      });
    },

    // Update shirt details
    updateShirtDetails: (details) => {
      set((state) => {
        if (!state.article.shirtDetails) {
          state.article.shirtDetails = {
            articleId: 0,
            material: '',
            careInstructions: '',
            fitType: 'REGULAR',
            availableSizes: [],
          };
        }
        Object.assign(state.article.shirtDetails, details);
      });
    },

    // Set article type and initialize type-specific details
    setArticleType: (type) => {
      set((state) => {
        state.article.articleType = type;

        switch (type) {
          case 'MUG':
            state.article.mugDetails = {
              articleId: 0,
              heightMm: 0,
              diameterMm: 0,
              printTemplateWidthMm: 0,
              printTemplateHeightMm: 0,
              fillingQuantity: '',
              dishwasherSafe: true,
            };
            delete state.article.shirtDetails;
            break;
          case 'SHIRT':
            state.article.shirtDetails = {
              articleId: 0,
              material: '',
              careInstructions: '',
              fitType: 'REGULAR',
              availableSizes: [],
            };
            delete state.article.mugDetails;
            break;
        }
      });
    },

    // Set category and reset subcategory
    setCategory: (categoryId) => {
      set((state) => {
        state.article.categoryId = categoryId;
        state.article.subcategoryId = undefined;
        state.subcategories = [];
      });

      // Load subcategories for the new category
      get().loadSubcategories(categoryId);
    },

    // Set subcategory
    setSubcategory: (subcategoryId) => {
      set((state) => {
        state.article.subcategoryId = subcategoryId;
      });
    },

    // Load categories
    loadCategories: async () => {
      try {
        const data = await articleCategoriesApi.getAll();
        set((state) => {
          state.categories = data;
        });
      } catch (error) {
        console.error('Error loading categories:', error);
        toast.error('Failed to load categories');
      }
    },

    // Load subcategories
    loadSubcategories: async (categoryId) => {
      if (!categoryId) {
        set((state) => {
          state.subcategories = [];
        });
        return;
      }

      try {
        const data = await articleSubCategoriesApi.getByCategoryId(categoryId);
        set((state) => {
          state.subcategories = data;
        });
      } catch (error) {
        console.error('Error loading subcategories:', error);
        toast.error('Failed to load subcategories');
      }
    },

    // Reset form
    resetForm: () => {
      set((state) => {
        state.article = { ...initialArticle };
        state.subcategories = [];
        state.isEdit = false;
        state.isLoading = false;
        state.isSaving = false;
      });
    },
  })),
);
