import { useSession } from '@/hooks/queries/useAuth';
import { useCartSummary } from '@/hooks/queries/useCart';
import { useCartStore } from '@/stores/cartStore';
import { ShoppingCart } from 'lucide-react';
import { Link } from 'react-router-dom';

export function CartBadge() {
  const { data: session } = useSession();

  // API-backed cart summary for authenticated users
  const { data: cartSummary } = useCartSummary();

  // Local cart store for non-authenticated users
  const getTotalLocalItems = useCartStore((state) => state.getTotalItems);

  // Determine which data to use based on authentication status
  const isAuthenticated = session?.authenticated === true;
  const itemCount = isAuthenticated ? cartSummary?.itemCount || 0 : getTotalLocalItems();

  return (
    <Link
      to="/cart"
      className="relative inline-flex items-center rounded-full bg-white p-2 text-gray-600 shadow-sm hover:bg-gray-50 hover:text-gray-900 focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 focus:outline-none"
    >
      <ShoppingCart className="h-6 w-6" />
      {itemCount > 0 && (
        <span className="absolute -top-1 -right-1 flex h-5 w-5 items-center justify-center rounded-full bg-red-500 text-xs font-medium text-white">
          {itemCount > 99 ? '99+' : itemCount}
        </span>
      )}
      <span className="sr-only">
        Shopping cart with {itemCount} {itemCount === 1 ? 'item' : 'items'}
      </span>
    </Link>
  );
}

// Floating cart badge for pages without navigation
export function FloatingCartBadge() {
  const { data: session } = useSession();

  // API-backed cart summary for authenticated users
  const { data: cartSummary } = useCartSummary();

  // Local cart store for non-authenticated users
  const getTotalLocalItems = useCartStore((state) => state.getTotalItems);

  // Determine which data to use based on authentication status
  const isAuthenticated = session?.authenticated === true;
  const itemCount = isAuthenticated ? cartSummary?.itemCount || 0 : getTotalLocalItems();

  // Don't show if cart is empty
  if (itemCount === 0) {
    return null;
  }

  return (
    <div className="fixed top-4 right-4 z-50">
      <Link
        to="/cart"
        className="relative inline-flex items-center rounded-full bg-blue-600 p-3 text-white shadow-lg hover:bg-blue-700 focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 focus:outline-none"
      >
        <ShoppingCart className="h-6 w-6" />
        <span className="absolute -top-1 -right-1 flex h-6 w-6 items-center justify-center rounded-full bg-red-500 text-xs font-medium text-white">
          {itemCount > 99 ? '99+' : itemCount}
        </span>
        <span className="sr-only">
          Shopping cart with {itemCount} {itemCount === 1 ? 'item' : 'items'}
        </span>
      </Link>
    </div>
  );
}
