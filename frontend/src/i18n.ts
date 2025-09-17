import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';

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
import deVat from './locales/de/vat.json';
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
import enVat from './locales/en/vat.json';

const deAdmin = {
  common: deAdminCommon,
  articleCategory: deAdminArticleCategory,
  articleSubCategory: deAdminArticleSubCategory,
  prompt: deAdminPrompt,
  promptSlotType: deAdminPromptSlotType,
  promptSlotVariant: deAdminPromptSlotVariant,
  supplier: deAdminSupplier,
  vatForm: deAdminVat,
} as const;

const enAdmin = {
  common: enAdminCommon,
  articleCategory: enAdminArticleCategory,
  articleSubCategory: enAdminArticleSubCategory,
  prompt: enAdminPrompt,
  promptSlotType: enAdminPromptSlotType,
  promptSlotVariant: enAdminPromptSlotVariant,
  supplier: enAdminSupplier,
  vatForm: enAdminVat,
} as const;

export const defaultNS = 'vat';
export const LANGUAGE_STORAGE_KEY = 'voenix.shop.language';
export const resources = {
  en: { vat: enVat, cart: enCart, checkout: enCheckout, editor: enEditor, admin: enAdmin },
  de: { vat: deVat, cart: deCart, checkout: deCheckout, editor: deEditor, admin: deAdmin },
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
