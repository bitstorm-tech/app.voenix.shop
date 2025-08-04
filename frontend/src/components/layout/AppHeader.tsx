import { Button } from '@/components/ui/Button';
import { CartBadge } from '@/components/ui/CartBadge';
import { useSession } from '@/hooks/queries/useAuth';
import { Package, Palette } from 'lucide-react';
import { Link } from 'react-router-dom';

export function AppHeader() {
  const { data: session } = useSession();
  const isAuthenticated = session?.authenticated === true;

  return (
    <header className="border-b bg-white shadow-sm">
      <div className="mx-auto max-w-5xl px-4 py-3">
        <div className="flex items-center justify-between">
          {/* Left side - Design Mug button */}
          <div>
            <Button variant="ghost" size="sm" asChild>
              <Link to="/editor">
                <Palette className="h-4 w-4" />
                <span className="ml-2 hidden sm:inline">Design Mug</span>
              </Link>
            </Button>
          </div>

          {/* Right side - Orders and Cart */}
          <nav className="flex items-center gap-3">
            {isAuthenticated && (
              <Button variant="ghost" size="sm" asChild>
                <Link to="/orders">
                  <Package className="h-4 w-4" />
                  <span className="ml-2 hidden sm:inline">My Orders</span>
                </Link>
              </Button>
            )}
            <CartBadge />
          </nav>
        </div>
      </div>
    </header>
  );
}
