import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';

import deCart from './locales/de/cart.json';
import deCheckout from './locales/de/checkout.json';
import deEditor from './locales/de/editor.json';
import deLogin from './locales/de/login.json';
import deMyImages from './locales/de/my-images.json';
import deOrders from './locales/de/orders.json';
import deVat from './locales/de/vat.json';
import enCart from './locales/en/cart.json';
import enCheckout from './locales/en/checkout.json';
import enEditor from './locales/en/editor.json';
import enLogin from './locales/en/login.json';
import enMyImages from './locales/en/my-images.json';
import enOrders from './locales/en/orders.json';
import enVat from './locales/en/vat.json';

export const defaultNS = 'vat';
export const LANGUAGE_STORAGE_KEY = 'voenix.shop.language';
export const resources = {
  en: { vat: enVat, cart: enCart, checkout: enCheckout, editor: enEditor, login: enLogin, myImages: enMyImages, orders: enOrders },
  de: { vat: deVat, cart: deCart, checkout: deCheckout, editor: deEditor, login: deLogin, myImages: deMyImages, orders: deOrders },
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
