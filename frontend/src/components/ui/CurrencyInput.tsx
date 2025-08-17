import { cn } from '@/lib/utils';
import { forwardRef, useCallback, useEffect, useRef, useState } from 'react';

interface CurrencyInputProps extends Omit<React.InputHTMLAttributes<HTMLInputElement>, 'onChange' | 'value' | 'type'> {
  value: number | undefined;
  onChange: (value: number) => void;
  currency?: string;
  decimalPlaces?: number;
}

export const CurrencyInput = forwardRef<HTMLInputElement, CurrencyInputProps>(
  ({ value, onChange, currency = 'EUR', decimalPlaces = 2, className, disabled, ...props }, ref) => {
    const inputRef = useRef<HTMLInputElement>(null);
    const [displayValue, setDisplayValue] = useState('');
    const [isFocused, setIsFocused] = useState(false);

    // Detect browser locale decimal separator
    const getLocaleSeparator = (): '.' | ',' => {
      const num = 1.1;
      const formatted = num.toLocaleString();
      return formatted.includes(',') ? ',' : '.';
    };

    const [userSeparator, setUserSeparator] = useState<'.' | ','>(getLocaleSeparator());

    // Format number for display
    const formatNumber = useCallback(
      (num: number | undefined, separator?: '.' | ','): string => {
        // Handle undefined or null values
        if (num === undefined || num === null) {
          return '0' + (separator === ',' ? ',' : '.') + '0'.repeat(decimalPlaces);
        }
        const formatted = num.toFixed(decimalPlaces);
        if (separator === ',') {
          return formatted.replace('.', ',');
        }
        return formatted;
      },
      [decimalPlaces],
    );

    // Parse user input to number
    const parseInput = (input: string): number | null => {
      // Replace comma with dot for parsing
      const normalized = input.replace(',', '.');

      // Remove all non-numeric characters except decimal point and minus
      const cleaned = normalized.replace(/[^0-9.-]/g, '');

      // Handle empty or invalid input
      if (cleaned === '' || cleaned === '-' || cleaned === '.') {
        return 0;
      }

      // Parse the number
      const parsed = parseFloat(cleaned);
      return isNaN(parsed) ? null : parsed;
    };

    // Update display value when prop value changes (only when not focused)
    useEffect(() => {
      if (!isFocused) {
        setDisplayValue(formatNumber(value ?? 0, userSeparator));
      }
    }, [value, isFocused, decimalPlaces, userSeparator, formatNumber]);

    const handleFocus = (e: React.FocusEvent<HTMLInputElement>) => {
      setIsFocused(true);
      // Select all text on focus for easy replacement
      e.target.select();
      props.onFocus?.(e);
    };

    const handleBlur = (e: React.FocusEvent<HTMLInputElement>) => {
      setIsFocused(false);
      // Format the value on blur
      const parsed = parseInput(displayValue);
      if (parsed !== null) {
        onChange(parsed);
        setDisplayValue(formatNumber(parsed, userSeparator));
      } else {
        // Reset to previous value if parsing failed
        setDisplayValue(formatNumber(value ?? 0, userSeparator));
      }
      props.onBlur?.(e);
    };

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
      const input = e.target.value;
      const cursorPosition = e.target.selectionStart || 0;

      // Track which separator the user is using
      if (input.includes(',')) {
        setUserSeparator(',');
      } else if (input.includes('.')) {
        setUserSeparator('.');
      }

      // Check if input has too many decimal places
      const decimalSeparatorIndex = Math.max(input.indexOf('.'), input.indexOf(','));
      if (decimalSeparatorIndex !== -1) {
        const decimalPart = input.substring(decimalSeparatorIndex + 1);
        // If trying to enter more decimal places than allowed, prevent it
        if (decimalPart.length > decimalPlaces) {
          return;
        }
      }

      // Allow user to type freely while focused
      setDisplayValue(input);

      // Parse and update the numeric value
      const parsed = parseInput(input);
      if (parsed !== null) {
        onChange(parsed);
      }

      // Restore cursor position after React re-render
      requestAnimationFrame(() => {
        if (inputRef.current) {
          inputRef.current.setSelectionRange(cursorPosition, cursorPosition);
        }
      });
    };

    const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
      // Allow: backspace, delete, tab, escape, enter
      if (
        [8, 9, 27, 13, 46].indexOf(e.keyCode) !== -1 ||
        // Allow: Ctrl+A, Ctrl+C, Ctrl+V, Ctrl+X
        (e.keyCode === 65 && e.ctrlKey === true) ||
        (e.keyCode === 67 && e.ctrlKey === true) ||
        (e.keyCode === 86 && e.ctrlKey === true) ||
        (e.keyCode === 88 && e.ctrlKey === true) ||
        // Allow: home, end, left, right, up, down
        (e.keyCode >= 35 && e.keyCode <= 40)
      ) {
        // Let it happen
        return;
      }

      // Allow decimal point or comma (but not if one already exists)
      if ((e.key === '.' || e.key === ',') && !displayValue.includes('.') && !displayValue.includes(',')) {
        return;
      }

      // Allow minus at the beginning
      if (e.key === '-' && e.currentTarget.selectionStart === 0 && displayValue.indexOf('-') === -1) {
        return;
      }

      // Check if typing a number would exceed decimal places limit
      if (e.key >= '0' && e.key <= '9') {
        const currentValue = e.currentTarget.value;
        const selectionStart = e.currentTarget.selectionStart || 0;
        const selectionEnd = e.currentTarget.selectionEnd || 0;

        // Simulate what the value would be after this keypress
        const newValue = currentValue.substring(0, selectionStart) + e.key + currentValue.substring(selectionEnd);

        const decimalSeparatorIndex = Math.max(newValue.indexOf('.'), newValue.indexOf(','));
        if (decimalSeparatorIndex !== -1) {
          const decimalPart = newValue.substring(decimalSeparatorIndex + 1);
          if (decimalPart.length > decimalPlaces) {
            e.preventDefault();
            return;
          }
        }
      }

      // Ensure that it is a number and stop other keys (but allow Ctrl/Cmd combinations)
      if (!e.ctrlKey && !e.metaKey && (e.shiftKey || e.keyCode < 48 || e.keyCode > 57) && (e.keyCode < 96 || e.keyCode > 105)) {
        e.preventDefault();
      }

      props.onKeyDown?.(e);
    };

    // Combine refs
    const setRefs = (el: HTMLInputElement | null) => {
      inputRef.current = el;
      if (typeof ref === 'function') {
        ref(el);
      } else if (ref) {
        ref.current = el;
      }
    };

    return (
      <div className="relative">
        <input
          ref={setRefs}
          type="text"
          inputMode="decimal"
          value={displayValue}
          onChange={handleChange}
          onFocus={handleFocus}
          onBlur={handleBlur}
          onKeyDown={handleKeyDown}
          disabled={disabled}
          className={cn(
            'border-input file:text-foreground placeholder:text-muted-foreground focus-visible:ring-ring flex h-9 w-full rounded-md border bg-transparent px-3 py-1 text-base shadow-sm transition-colors file:border-0 file:bg-transparent file:text-sm file:font-medium focus-visible:ring-1 focus-visible:outline-none disabled:cursor-not-allowed disabled:opacity-50 md:text-sm',
            className,
          )}
          {...props}
        />
        {currency && !isFocused && (
          <span className="text-muted-foreground pointer-events-none absolute top-1/2 right-3 -translate-y-1/2 text-sm">{currency}</span>
        )}
      </div>
    );
  },
);

CurrencyInput.displayName = 'CurrencyInput';
