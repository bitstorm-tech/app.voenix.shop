import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';

import enVat from './locales/en/vat.json';
import deVat from './locales/de/vat.json';

export const defaultNS = 'vat';
export const resources = {
  en: { vat: enVat },
  de: { vat: deVat },
} as const;

i18n.use(initReactI18next).init({
  resources,
  fallbackLng: 'en',
  ns: [defaultNS],
  defaultNS,
  interpolation: { escapeValue: false },
  returnNull: false,
});

export default i18n;
