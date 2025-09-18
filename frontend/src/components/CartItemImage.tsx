import { useImageWithFallback } from '@/hooks/useImageWithFallback';
import { AlertCircle, RefreshCw, ShoppingBag } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { ImageSkeleton } from './ImageSkeleton';
import { Button } from './ui/Button';

interface CartItemImageProps {
  src: string | null | undefined;
  alt: string;
  className?: string;
  onLoad?: () => void;
  onError?: (error: Error) => void;
}

export function CartItemImage({ src, alt, className = 'h-24 w-24 rounded-md object-cover sm:h-32 sm:w-32', onLoad, onError }: CartItemImageProps) {
  const { t } = useTranslation('cart');
  const { retryCount, retry, isLoading, isLoaded, isError, isRetrying } = useImageWithFallback(src, {
    maxRetries: 3,
    retryDelay: 1000,
    preload: true,
    onLoad,
    onError,
  });

  const containerClasses = `relative flex-shrink-0 ${className.includes('h-') ? '' : 'h-24 w-24 sm:h-32 sm:w-32'} ${className.includes('rounded') ? '' : 'rounded-md'} overflow-hidden`;

  // Loading state with skeleton
  if (isLoading && !isRetrying) {
    return (
      <div className={containerClasses}>
        <ImageSkeleton className={className} />
      </div>
    );
  }

  // Retrying state
  if (isRetrying) {
    return (
      <div className={containerClasses}>
        <div className={`flex items-center justify-center bg-gray-100 ${className}`}>
          <div className="text-center">
            <RefreshCw className="mx-auto h-6 w-6 animate-spin text-gray-400" />
            <p className="mt-1 text-xs text-gray-500">{t('image.retrying')}</p>
          </div>
        </div>
      </div>
    );
  }

  // Error state with retry option
  if (isError) {
    return (
      <div className={containerClasses}>
        <div className={`flex flex-col items-center justify-center bg-gray-100 ${className} p-2`}>
          <AlertCircle className="mb-1 h-6 w-6 text-red-400" />
          <p className="mb-2 text-center text-xs text-gray-600">{retryCount > 0 ? t('image.failed') : t('image.error')}</p>
          <Button variant="outline" size="sm" onClick={retry} className="h-auto px-2 py-1 text-xs">
            <RefreshCw className="mr-1 h-3 w-3" />
            {t('image.retry')}
          </Button>
        </div>
      </div>
    );
  }

  // Success state - show actual image
  if (isLoaded && src) {
    return (
      <div className={containerClasses}>
        <img
          src={src}
          alt={alt}
          className={`${className} transition-opacity duration-300`}
          loading="lazy"
          onLoad={onLoad}
          onError={(e) => {
            console.error('Image load error:', e);
            onError?.(new Error(`Failed to display image: ${src}`));
          }}
        />
      </div>
    );
  }

  // Fallback state (no image)
  return (
    <div className={containerClasses}>
      <div className={`flex items-center justify-center bg-gray-200 ${className}`}>
        <ShoppingBag className="h-8 w-8 text-gray-400" />
      </div>
    </div>
  );
}
