import { useTranslation } from 'react-i18next';
import { LoadingSpinner } from './LoadingSpinner';

interface LazyLoadingFallbackProps {
  message?: string;
}

export function LazyLoadingFallback({ message }: LazyLoadingFallbackProps) {
  const { t } = useTranslation('common');
  const displayMessage = message ?? t('loading');
  return (
    <div className="flex h-screen items-center justify-center">
      <div className="text-center">
        <LoadingSpinner className="mx-auto mb-4 h-8 w-8" />
        <p className="text-sm text-gray-600">{displayMessage}</p>
      </div>
    </div>
  );
}
