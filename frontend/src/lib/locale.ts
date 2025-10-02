const DEFAULT_LOCALE = 'en-US';
const DEFAULT_CURRENCY = 'EUR';

interface LocaleCurrencyConfig {
  prefix: string;
  locale: string;
  currency: string;
}

const LANGUAGE_CONFIGS: LocaleCurrencyConfig[] = [{ prefix: 'de', locale: 'de-DE', currency: 'EUR' }];

const normalizeLocale = (locale: string): string => {
  try {
    const [canonical] = Intl.getCanonicalLocales(locale);
    return canonical ?? locale;
  } catch {
    return locale;
  }
};

export function getLocaleCurrency(language: string | undefined): { locale: string; currency: string } {
  if (!language) {
    return { locale: DEFAULT_LOCALE, currency: DEFAULT_CURRENCY };
  }

  const normalizedLanguage = language.toLowerCase();
  const config = LANGUAGE_CONFIGS.find(({ prefix }) => normalizedLanguage.startsWith(prefix));

  if (config) {
    return { locale: config.locale, currency: config.currency };
  }

  const locale = normalizedLanguage.includes('-') ? normalizeLocale(language) : DEFAULT_LOCALE;
  return { locale, currency: DEFAULT_CURRENCY };
}
