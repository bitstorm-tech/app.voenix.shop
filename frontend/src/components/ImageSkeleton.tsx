import { ShoppingBag } from 'lucide-react';

interface ImageSkeletonProps {
  className?: string;
  showIcon?: boolean;
}

export function ImageSkeleton({ className = '', showIcon = true }: ImageSkeletonProps) {
  return (
    <div className={`relative overflow-hidden bg-gray-200 ${className}`}>
      <div className="flex h-full w-full items-center justify-center">
        {showIcon && <ShoppingBag className="h-8 w-8 text-gray-400" />}
      </div>
      {/* Shimmer effect using standard Tailwind animations */}
      <div className="absolute inset-0 animate-pulse bg-gradient-to-r from-transparent via-white/20 to-transparent" />
    </div>
  );
}