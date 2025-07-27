import { cn } from '@/lib/utils';
import { Loader2 } from 'lucide-react';

interface LoadingSpinnerProps {
  className?: string;
}

export function LoadingSpinner({ className }: LoadingSpinnerProps) {
  return (
    <div className="flex h-screen w-full items-center justify-center">
      <Loader2 className={cn('text-primary h-8 w-8 animate-spin', className)} />
    </div>
  );
}
