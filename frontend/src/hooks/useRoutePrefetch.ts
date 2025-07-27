import { prefetchRoutes } from '@/lib/routePrefetch';
import { useEffect } from 'react';
import { useLocation } from 'react-router-dom';

export function useRoutePrefetch() {
  const location = useLocation();

  useEffect(() => {
    // Prefetch routes based on current location
    if (location.pathname === '/login') {
      // User might go to admin after login
      prefetchRoutes.admin();
    } else if (location.pathname.startsWith('/editor') || location.pathname === '/') {
      // User might go to cart/checkout from editor
      prefetchRoutes.checkout();
    }
  }, [location.pathname]);

  return {
    prefetchAdmin: prefetchRoutes.admin,
    prefetchEditor: prefetchRoutes.editor,
    prefetchCheckout: prefetchRoutes.checkout,
  };
}
