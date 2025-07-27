import { LoadingSpinner } from './LoadingSpinner';

interface LazyLoadingFallbackProps {
  message?: string;
}

export function LazyLoadingFallback({ message = 'Loading...' }: LazyLoadingFallbackProps) {
  return (
    <div className="flex h-screen items-center justify-center">
      <div className="text-center">
        <LoadingSpinner className="mx-auto mb-4 h-8 w-8" />
        <p className="text-sm text-gray-600">{message}</p>
      </div>
    </div>
  );
}
