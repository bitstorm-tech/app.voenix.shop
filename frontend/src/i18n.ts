import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';

import deAdminArticleCategories from './locales/de/admin-article-categories.json';
import deAdminArticles from './locales/de/admin-articles.json';
import deAdminCompletedOrders from './locales/de/admin-completed-orders.json';
import deAdminLogistics from './locales/de/admin-logistics.json';
import deAdminOpenOrders from './locales/de/admin-open-orders.json';
import deAdminPromptCategories from './locales/de/admin-prompt-categories.json';
import deAdminPromptSlotTypes from './locales/de/admin-prompt-slot-types.json';
import deAdminPromptTester from './locales/de/admin-prompt-tester.json';
import deAdminPrompts from './locales/de/admin-prompts.json';
import deAdminSlotVariants from './locales/de/admin-slot-variants.json';
import deAdminSuppliers from './locales/de/admin-suppliers.json';
import deAdminCommon from './locales/de/admin/common.json';
import deAdminArticleCategory from './locales/de/admin/newOrEditArticleCategory.json';
import deAdminArticleSubCategory from './locales/de/admin/newOrEditArticleSubCategory.json';
import deAdminPrompt from './locales/de/admin/newOrEditPrompt.json';
import deAdminPromptSlotType from './locales/de/admin/newOrEditPromptSlotType.json';
import deAdminPromptSlotVariant from './locales/de/admin/newOrEditPromptSlotVariant.json';
import deAdminSupplier from './locales/de/admin/newOrEditSupplier.json';
import deAdminVat from './locales/de/admin/newOrEditVat.json';
import deCart from './locales/de/cart.json';
import deCheckout from './locales/de/checkout.json';
import deEditor from './locales/de/editor.json';
import deLogin from './locales/de/login.json';
import deMyImages from './locales/de/my-images.json';
import deOrderSuccess from './locales/de/order-success.json';
import deOrders from './locales/de/orders.json';
import deVat from './locales/de/vat.json';
import enAdminArticleCategories from './locales/en/admin-article-categories.json';
import enAdminArticles from './locales/en/admin-articles.json';
import enAdminCompletedOrders from './locales/en/admin-completed-orders.json';
import enAdminLogistics from './locales/en/admin-logistics.json';
import enAdminOpenOrders from './locales/en/admin-open-orders.json';
import enAdminPromptCategories from './locales/en/admin-prompt-categories.json';
import enAdminPromptSlotTypes from './locales/en/admin-prompt-slot-types.json';
import enAdminPromptTester from './locales/en/admin-prompt-tester.json';
import enAdminPrompts from './locales/en/admin-prompts.json';
import enAdminSlotVariants from './locales/en/admin-slot-variants.json';
import enAdminSuppliers from './locales/en/admin-suppliers.json';
import enAdminCommon from './locales/en/admin/common.json';
import enAdminArticleCategory from './locales/en/admin/newOrEditArticleCategory.json';
import enAdminArticleSubCategory from './locales/en/admin/newOrEditArticleSubCategory.json';
import enAdminPrompt from './locales/en/admin/newOrEditPrompt.json';
import enAdminPromptSlotType from './locales/en/admin/newOrEditPromptSlotType.json';
import enAdminPromptSlotVariant from './locales/en/admin/newOrEditPromptSlotVariant.json';
import enAdminSupplier from './locales/en/admin/newOrEditSupplier.json';
import enAdminVat from './locales/en/admin/newOrEditVat.json';
import enCart from './locales/en/cart.json';
import enCheckout from './locales/en/checkout.json';
import enEditor from './locales/en/editor.json';
import enLogin from './locales/en/login.json';
import enMyImages from './locales/en/my-images.json';
import enOrderSuccess from './locales/en/order-success.json';
import enOrders from './locales/en/orders.json';
import enVat from './locales/en/vat.json';

const enAdminNamespace = {
  articleCategory: enAdminArticleCategory,
  articleSubCategory: enAdminArticleSubCategory,
  prompt: enAdminPrompt,
  promptSlotType: enAdminPromptSlotType,
  promptSlotVariant: enAdminPromptSlotVariant,
  supplier: enAdminSupplier,
  vatForm: enAdminVat,
  common: enAdminCommon,
};

const deAdminNamespace = {
  articleCategory: deAdminArticleCategory,
  articleSubCategory: deAdminArticleSubCategory,
  prompt: deAdminPrompt,
  promptSlotType: deAdminPromptSlotType,
  promptSlotVariant: deAdminPromptSlotVariant,
  supplier: deAdminSupplier,
  vatForm: deAdminVat,
  common: deAdminCommon,
};

export const defaultNS = 'vat';
export const LANGUAGE_STORAGE_KEY = 'voenix.shop.language';
export const resources = {
  en: {
    admin: enAdminNamespace,
    adminArticleCategories: enAdminArticleCategories,
    adminArticles: enAdminArticles,
    adminOpenOrders: enAdminOpenOrders,
    adminPromptCategories: enAdminPromptCategories,
    adminPromptSlotTypes: enAdminPromptSlotTypes,
    adminPromptTester: enAdminPromptTester,
    adminPrompts: enAdminPrompts,
    adminSlotVariants: enAdminSlotVariants,
    adminSuppliers: enAdminSuppliers,
    adminCompletedOrders: enAdminCompletedOrders,
    adminLogistics: enAdminLogistics,
    articleCategory: enAdminArticleCategory,
    articleSubCategory: enAdminArticleSubCategory,
    cart: enCart,
    checkout: enCheckout,
    common: enAdminCommon,
    editor: enEditor,
    login: enLogin,
    myImages: enMyImages,
    orderSuccess: enOrderSuccess,
    orders: enOrders,
    prompt: enAdminPrompt,
    promptSlotType: enAdminPromptSlotType,
    promptSlotVariant: enAdminPromptSlotVariant,
    supplier: enAdminSupplier,
    vat: enVat,
    vatForm: enAdminVat,
  },
  de: {
    admin: deAdminNamespace,
    adminArticleCategories: deAdminArticleCategories,
    adminArticles: deAdminArticles,
    adminOpenOrders: deAdminOpenOrders,
    adminPromptCategories: deAdminPromptCategories,
    adminPromptSlotTypes: deAdminPromptSlotTypes,
    adminPromptTester: deAdminPromptTester,
    adminPrompts: deAdminPrompts,
    adminSlotVariants: deAdminSlotVariants,
    adminSuppliers: deAdminSuppliers,
    adminCompletedOrders: deAdminCompletedOrders,
    adminLogistics: deAdminLogistics,
    articleCategory: deAdminArticleCategory,
    articleSubCategory: deAdminArticleSubCategory,
    cart: deCart,
    checkout: deCheckout,
    common: deAdminCommon,
    editor: deEditor,
    login: deLogin,
    myImages: deMyImages,
    orderSuccess: deOrderSuccess,
    orders: deOrders,
    prompt: deAdminPrompt,
    promptSlotType: deAdminPromptSlotType,
    promptSlotVariant: deAdminPromptSlotVariant,
    supplier: deAdminSupplier,
    vat: deVat,
    vatForm: deAdminVat,
  },
} as const;
export const namespaces = Object.freeze(Object.keys(resources.en)) as readonly (keyof typeof resources.en)[];

const detectInitialLanguage = (): string => {
  if (typeof window === 'undefined') {
    return 'en';
  }

  const storedLanguage = window.localStorage.getItem(LANGUAGE_STORAGE_KEY);
  if (storedLanguage) {
    return storedLanguage;
  }

  const browserLanguage = (window.navigator.languages && window.navigator.languages[0]) || window.navigator.language || 'en';
  const normalized = browserLanguage.toLowerCase();

  if (normalized.startsWith('de')) {
    return 'de';
  }

  return 'en';
};

const initialLanguage = detectInitialLanguage();

i18n.use(initReactI18next).init({
  resources,
  fallbackLng: 'en',
  ns: namespaces as string[],
  defaultNS,
  interpolation: { escapeValue: false },
  returnNull: false,
  lng: initialLanguage,
});

if (typeof window !== 'undefined') {
  const persistLanguage = (lng: string) => {
    window.localStorage.setItem(LANGUAGE_STORAGE_KEY, lng);
  };

  persistLanguage(i18n.language || initialLanguage);
  i18n.off('languageChanged', persistLanguage);
  i18n.on('languageChanged', persistLanguage);
}
