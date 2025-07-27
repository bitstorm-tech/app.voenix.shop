import { Input } from '@/components/ui/Input';
import { cn } from '@/lib/utils';
import { Check, Copy } from 'lucide-react';
import { useState } from 'react';

interface InputWithCopyProps {
  value: string;
  placeholder?: string;
  disabled?: boolean;
  className?: string;
  id?: string;
  showCopyButton?: boolean;
}

export function InputWithCopy({ value, placeholder, disabled = true, className, id, showCopyButton = true }: InputWithCopyProps) {
  const [copied, setCopied] = useState(false);

  const handleCopy = async () => {
    if (!value || value === placeholder) return;

    try {
      await navigator.clipboard.writeText(value);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    } catch (err) {
      console.error('Failed to copy text: ', err);
    }
  };

  const shouldShowCopyButton = showCopyButton && value && value !== placeholder;

  return (
    <div className={cn('relative inline-block w-full', className)}>
      <Input
        id={id}
        value={value}
        disabled={disabled}
        className={cn(shouldShowCopyButton ? 'pr-10' : '', 'w-full')}
        placeholder={placeholder}
        readOnly
      />
      {shouldShowCopyButton && (
        <button
          type="button"
          className="text-muted-foreground hover:text-foreground absolute inset-y-0 right-0 flex items-center px-2"
          onClick={handleCopy}
          title={copied ? 'Copied!' : 'Copy to clipboard'}
        >
          {copied ? <Check className="h-4 w-4 text-green-600" /> : <Copy className="h-4 w-4" />}
        </button>
      )}
    </div>
  );
}
