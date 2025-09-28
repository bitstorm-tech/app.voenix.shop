import { Button } from '@/components/ui/Button';
import { CartBadge } from '@/components/ui/CartBadge';
import { useSession } from '@/hooks/queries/useAuth';
import { Images, Package, Palette } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { Link } from 'react-router-dom';

export function AppHeader() {
  const { data: session } = useSession();
  const isAuthenticated = session?.authenticated === true;
  const { t, i18n } = useTranslation('appHeader');
  const resolvedLanguage = (i18n.resolvedLanguage ?? i18n.language ?? 'en').toLowerCase();
  const languageValue = resolvedLanguage.startsWith('de') ? 'de' : 'en';

  const handleLanguageChange = (newLanguage: string) => {
    if (newLanguage === languageValue) return;
    i18n.changeLanguage(newLanguage);
  };

  return (
    <header className="border-b bg-white shadow-sm">
      <div className="mx-auto max-w-5xl px-4 py-3">
        <div className="flex items-center justify-between">
          {/* Left side - Design Mug button */}
          <div>
            <Button variant="ghost" size="sm" asChild>
              <Link to="/editor">
                <Palette className="h-4 w-4" />
                <span className="ml-2 hidden sm:inline">{t('links.designMug')}</span>
              </Link>
            </Button>
          </div>

          {/* Right side - Orders and Cart */}
          <nav className="flex items-center gap-3">
            {isAuthenticated && (
              <>
                <Button variant="ghost" size="sm" asChild>
                  <Link to="/my-images">
                    <Images className="h-4 w-4" />
                    <span className="ml-2 hidden sm:inline">{t('links.myImages')}</span>
                  </Link>
                </Button>
                <Button variant="ghost" size="sm" asChild>
                  <Link to="/orders">
                    <Package className="h-4 w-4" />
                    <span className="ml-2 hidden sm:inline">{t('links.myOrders')}</span>
                  </Link>
                </Button>
              </>
            )}
            <label className="flex items-center" htmlFor="app-language">
              <span className="sr-only">{t('language.label')}</span>
              <select
                id="app-language"
                aria-label={t('language.aria')}
                className="rounded border px-2 py-1 text-sm"
                value={languageValue}
                onChange={(event) => handleLanguageChange(event.target.value)}
              >
                <option value="en">🇬🇧</option>
                <option value="de">🇩🇪</option>
              </select>
            </label>
            <CartBadge />
          </nav>
        </div>
      </div>
    </header>
  );
}
