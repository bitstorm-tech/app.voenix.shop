import { AlertCircle, RefreshCw, ShoppingBag } from 'lucide-react';
import { useImageWithFallback } from '@/hooks/useImageWithFallback';
import { ImageSkeleton } from './ImageSkeleton';
import { Button } from './ui/Button';

interface CartItemImageProps {
  src: string | null | undefined;
  alt: string;
  className?: string;
  onLoad?: () => void;
  onError?: (error: Error) => void;
}

export function CartItemImage({ 
  src, 
  alt, 
  className = 'h-24 w-24 rounded-md object-cover sm:h-32 sm:w-32',
  onLoad,
  onError 
}: CartItemImageProps) {
  const { 
    retryCount, 
    retry, 
    isLoading, 
    isLoaded, 
    isError, 
    isRetrying 
  } = useImageWithFallback(src, {
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
            <RefreshCw className="h-6 w-6 text-gray-400 animate-spin mx-auto" />
            <p className="text-xs text-gray-500 mt-1">Retrying...</p>
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
          <AlertCircle className="h-6 w-6 text-red-400 mb-1" />
          <p className="text-xs text-gray-600 text-center mb-2">
            {retryCount > 0 ? 'Failed to load' : 'Image error'}
          </p>
          <Button
            variant="outline"
            size="sm"
            onClick={retry}
            className="text-xs px-2 py-1 h-auto"
          >
            <RefreshCw className="h-3 w-3 mr-1" />
            Retry
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