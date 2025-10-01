import { cn } from '$lib/utils/cn';

export type ButtonVariant = 'default' | 'secondary' | 'outline' | 'ghost' | 'link';
export type ButtonSize = 'default' | 'sm' | 'lg' | 'icon';

type ButtonVariantOptions = {
	variant?: ButtonVariant;
	size?: ButtonSize;
	class?: string;
};

const baseClasses =
	'inline-flex items-center justify-center gap-2 whitespace-nowrap rounded-md text-sm font-medium transition-colors focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-blue-600 disabled:pointer-events-none disabled:opacity-50';

const variantClasses: Record<ButtonVariant, string> = {
	default: 'bg-slate-900 text-white shadow-sm hover:bg-slate-900/90',
	secondary: 'bg-gray-100 text-gray-900 hover:bg-gray-100/80',
	outline: 'border border-gray-300 bg-white text-gray-700 hover:bg-gray-100',
	ghost: 'text-gray-700 hover:bg-gray-100',
	link: 'text-blue-600 underline-offset-4 hover:underline'
};

const sizeClasses: Record<ButtonSize, string> = {
	default: 'h-10 px-4 py-2',
	sm: 'h-9 rounded-md px-3',
	lg: 'h-11 rounded-md px-5 text-base',
	icon: 'size-10 p-0'
};

export function buttonVariants({
	variant = 'default',
	size = 'default',
	class: className = ''
}: ButtonVariantOptions = {}) {
	return cn(baseClasses, variantClasses[variant], sizeClasses[size], className);
}
