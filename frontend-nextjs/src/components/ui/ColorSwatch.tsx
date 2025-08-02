import { cn } from '@/lib/utils';

interface ColorSwatchProps {
  color: string;
  label?: string;
  size?: 'sm' | 'md' | 'lg';
  className?: string;
}

export function ColorSwatch({ color, label, size = 'md', className }: ColorSwatchProps) {
  const sizeClasses = {
    sm: 'h-4 w-4',
    md: 'h-6 w-6',
    lg: 'h-8 w-8',
  };

  return (
    <div className={cn('flex items-center gap-2', className)}>
      <div
        className={cn('rounded-md border border-gray-300 shadow-sm', sizeClasses[size])}
        style={{ backgroundColor: color }}
        title={label || color}
      />
      {label && <span className="text-muted-foreground text-sm">{label}</span>}
    </div>
  );
}
