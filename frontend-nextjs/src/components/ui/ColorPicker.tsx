import { Input } from '@/components/ui/Input';
import { Label } from '@/components/ui/Label';
import { Popover, PopoverContent, PopoverTrigger } from '@/components/ui/Popover';
import { cn } from '@/lib/utils';
import { Paintbrush } from 'lucide-react';
import { forwardRef, useState } from 'react';

export interface ColorPickerProps extends Omit<React.InputHTMLAttributes<HTMLInputElement>, 'value' | 'onChange'> {
  label?: string;
  value?: string;
  onChange?: (color: string) => void;
}

const PRESET_COLORS = [
  '#ffffff', // White
  '#000000', // Black
  '#ff0000', // Red
  '#00ff00', // Green
  '#0000ff', // Blue
  '#ffff00', // Yellow
  '#ff00ff', // Magenta
  '#00ffff', // Cyan
  '#ff8800', // Orange
  '#8800ff', // Purple
  '#88ff00', // Lime
  '#0088ff', // Sky Blue
  '#ff0088', // Pink
  '#888888', // Gray
  '#444444', // Dark Gray
];

const ColorPicker = forwardRef<HTMLInputElement, ColorPickerProps>(({ className, label, id, value = '#ffffff', onChange, ...props }, ref) => {
  const [open, setOpen] = useState(false);
  const [inputValue, setInputValue] = useState(value);

  const handleColorChange = (color: string) => {
    setInputValue(color);
    onChange?.(color);
    setOpen(false);
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const newValue = e.target.value;
    setInputValue(newValue);
    if (/^#[0-9A-Fa-f]{6}$/.test(newValue)) {
      onChange?.(newValue);
    }
  };

  // Simple version - just a color input
  if (!onChange) {
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
          value={value}
          {...props}
        />
        {label && (
          <label htmlFor={id} className="text-sm leading-none font-medium peer-disabled:cursor-not-allowed peer-disabled:opacity-70">
            {label}
          </label>
        )}
      </div>
    );
  }

  // Advanced version with popover
  return (
    <Popover open={open} onOpenChange={setOpen}>
      <PopoverTrigger asChild>
        <button
          className={cn(
            'border-input bg-background ring-offset-background hover:bg-accent hover:text-accent-foreground focus:ring-ring flex h-10 w-full items-center justify-between rounded-md border px-3 py-2 text-sm focus:ring-2 focus:ring-offset-2 focus:outline-none',
            className,
          )}
          type="button"
        >
          <div className="flex items-center gap-2">
            <div className="h-5 w-5 rounded border border-gray-300" style={{ backgroundColor: value }} />
            <span>{value}</span>
          </div>
          <Paintbrush className="h-4 w-4 opacity-50" />
        </button>
      </PopoverTrigger>
      <PopoverContent className="w-64">
        <div className="space-y-3">
          <div>
            <Label htmlFor="color-input" className="text-sm font-medium">
              Hex Color
            </Label>
            <Input id="color-input" value={inputValue} onChange={handleInputChange} placeholder="#ffffff" className="mt-1" />
          </div>
          <div>
            <Label className="text-sm font-medium">Preset Colors</Label>
            <div className="mt-2 grid grid-cols-5 gap-2">
              {PRESET_COLORS.map((color) => (
                <button
                  key={color}
                  className={cn('h-8 w-8 rounded border-2 transition-all hover:scale-110', value === color ? 'border-primary' : 'border-gray-300')}
                  style={{ backgroundColor: color }}
                  onClick={() => handleColorChange(color)}
                  type="button"
                />
              ))}
            </div>
          </div>
          <div>
            <Label htmlFor="native-color" className="text-sm font-medium">
              Custom Color
            </Label>
            <input
              id="native-color"
              type="color"
              value={value}
              onChange={(e) => handleColorChange(e.target.value)}
              className="border-input mt-1 h-10 w-full cursor-pointer rounded border"
            />
          </div>
        </div>
      </PopoverContent>
    </Popover>
  );
});

ColorPicker.displayName = 'ColorPicker';

export { ColorPicker };
