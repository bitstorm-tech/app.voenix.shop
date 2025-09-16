import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';

import deVat from './locales/de/vat.json';
import enVat from './locales/en/vat.json';

export const defaultNS = 'vat';
export const LANGUAGE_STORAGE_KEY = 'voenix.shop.language';
export const resources = {
  en: { vat: enVat },
  de: { vat: deVat },
} as const;

const detectInitialLanguage = (): string => {
  const storedLanguage = window.localStorage.getItem(LANGUAGE_STORAGE_KEY);
  if (storedLanguage) {
    return storedLanguage;
  }

  if (typeof window === 'undefined') {
    return 'en';
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
  ns: [defaultNS],
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
