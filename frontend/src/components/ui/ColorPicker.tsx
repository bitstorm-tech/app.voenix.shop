import { cn } from '@/lib/utils';
import { forwardRef } from 'react';

export interface ColorPickerProps extends React.InputHTMLAttributes<HTMLInputElement> {
  label?: string;
}

const ColorPicker = forwardRef<HTMLInputElement, ColorPickerProps>(({ className, label, id, ...props }, ref) => {
  return (
    <div className="flex items-center gap-3">
      <input
        type="color"
        id={id}
        className={cn(
          'border-input bg-background h-10 w-20 cursor-pointer rounded-md border shadow-sm transition-colors',
          'focus:ring-ring hover:border-gray-400 focus:ring-2 focus:ring-offset-2 focus:outline-none',
          'disabled:cursor-not-allowed disabled:opacity-50',
          className,
        )}
        ref={ref}
        {...props}
      />
      {label && (
        <label htmlFor={id} className="text-sm leading-none font-medium peer-disabled:cursor-not-allowed peer-disabled:opacity-70">
          {label}
        </label>
      )}
    </div>
  );
});

ColorPicker.displayName = 'ColorPicker';

export { ColorPicker };
