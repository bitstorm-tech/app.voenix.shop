import { LazyLoadingFallback } from '@/components/LazyLoadingFallback';
import ProtectedRoute from '@/components/auth/ProtectedRoute';
import { queryClient } from '@/lib/queryClient';
import { QueryClientProvider } from '@tanstack/react-query';
import { ReactQueryDevtools } from '@tanstack/react-query-devtools';
import { lazy, Suspense } from 'react';
import { Route, BrowserRouter as Router, Routes } from 'react-router-dom';
import { Toaster } from 'sonner';

// Lazy load main pages
const Editor = lazy(() => import('@/pages/Editor'));
const CartPage = lazy(() => import('@/pages/Cart'));
const CheckoutPage = lazy(() => import('@/pages/Checkout'));
const OrderSuccessPage = lazy(() => import('@/pages/OrderSuccess'));
const OrdersPage = lazy(() => import('@/pages/Orders'));
const Login = lazy(() => import('@/pages/Login'));

// Lazy load admin routes
const AdminRoutes = lazy(() => import('@/routes/AdminRoutes').then((module) => ({ default: module.AdminRoutes })));

export default function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <Router>
        <Suspense fallback={<LazyLoadingFallback />}>
          <Routes>
            <Route path="/" element={<Editor />} />
            <Route path="/editor" element={<Editor />} />
            <Route path="/cart" element={<CartPage />} />
            <Route path="/checkout" element={<CheckoutPage />} />
            <Route path="/order-success/:orderId" element={<OrderSuccessPage />} />
            <Route path="/orders" element={<OrdersPage />} />
            <Route path="/login" element={<Login />} />

            <Route element={<ProtectedRoute />}>
              <Route path="/admin/*" element={<AdminRoutes />} />
            </Route>
          </Routes>
        </Suspense>
        <Toaster position="top-center" richColors />
        <ReactQueryDevtools initialIsOpen={false} buttonPosition="top-left" />
      </Router>
    </QueryClientProvider>
  );
}
