'use client';

import { cn } from '@/lib/utils';
import { ImageIcon } from 'lucide-react';
import { useState } from 'react';

interface ArticleImageProps {
  src: string | null;
  alt: string;
  className?: string;
  size?: 'xs' | 'sm' | 'md' | 'lg';
}

const sizeClasses = {
  xs: 'h-8 w-8',
  sm: 'h-12 w-12',
  md: 'h-16 w-16',
  lg: 'h-20 w-20',
};

export function ArticleImage({ src, alt, className, size = 'md' }: ArticleImageProps) {
  const [hasError, setHasError] = useState(false);
  const [isLoading, setIsLoading] = useState(true);

  if (!src || hasError) {
    return (
      <div className={cn('bg-muted flex items-center justify-center rounded-md border', sizeClasses[size], className)}>
        <ImageIcon className="text-muted-foreground h-4 w-4" />
      </div>
    );
  }

  return (
    <div className={cn('relative overflow-hidden rounded-md border', sizeClasses[size], className)}>
      {isLoading && (
        <div className="bg-muted absolute inset-0 flex items-center justify-center">
          <div className="h-3 w-3 animate-spin rounded-full border-2 border-current border-t-transparent" />
        </div>
      )}
      <img
        src={src}
        alt={alt}
        className="h-full w-full object-cover"
        onLoad={() => setIsLoading(false)}
        onError={() => {
          setHasError(true);
          setIsLoading(false);
        }}
      />
    </div>
  );
}
