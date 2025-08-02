import { Label } from '@/components/ui/Label';
import type { ComponentProps } from 'react';

interface FieldLabelProps extends ComponentProps<typeof Label> {
  required?: boolean;
  optional?: boolean;
  children: React.ReactNode;
}

export function FieldLabel({ required, optional, children, ...props }: FieldLabelProps) {
  return (
    <Label {...props}>
      {children}
      {required && <span className="text-destructive ml-0.5">*</span>}
      {optional && <span className="text-muted-foreground ml-1 text-sm">(optional)</span>}
    </Label>
  );
}
