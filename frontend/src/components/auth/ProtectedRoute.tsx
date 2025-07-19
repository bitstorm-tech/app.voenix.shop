import { useAuthStore } from '@/stores/authStore';
import { useEffect } from 'react';
import { Navigate, Outlet, useLocation } from 'react-router-dom';

interface ProtectedRouteProps {
  requiredRoles?: string[];
}

export default function ProtectedRoute({ requiredRoles = [] }: ProtectedRouteProps) {
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
  const isLoading = useAuthStore((state) => state.isLoading);
  const roles = useAuthStore((state) => state.roles);
  const location = useLocation();

  useEffect(() => {
    // Use getState to avoid subscribing to the store in useEffect
    useAuthStore.getState().checkSession();
  }, []);

  if (isLoading) {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <div className="text-lg">Loading...</div>
      </div>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  if (requiredRoles.length > 0) {
    const hasRequiredRole = requiredRoles.some((role) => roles.includes(role));
    if (!hasRequiredRole) {
      return <Navigate to="/" replace />;
    }
  }

  return <Outlet />;
}
