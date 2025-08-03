import { LoadingSpinner } from '@/components/LoadingSpinner';
import { Button } from '@/components/ui/Button';
import { useSession } from '@/hooks/queries/useAuth';
import { useOrders } from '@/hooks/queries/useOrders';
import type { OrderDto } from '@/types/order';
import { AlertTriangle, Package, ShoppingBag } from 'lucide-react';
import { useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';

export default function OrdersPage() {
  const navigate = useNavigate();
  const { data: session, isLoading: sessionLoading } = useSession();
  const { data: ordersData, isLoading: ordersLoading, error: ordersError } = useOrders();

  // Redirect to login if not authenticated
  useEffect(() => {
    if (!sessionLoading && !session?.authenticated) {
      navigate('/login?redirect=' + encodeURIComponent('/orders'));
    }
  }, [session, sessionLoading, navigate]);

  // Don't render anything while checking authentication
  if (sessionLoading) {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <LoadingSpinner />
      </div>
    );
  }

  // Don't render if not authenticated (redirect will happen)
  if (!session?.authenticated) {
    return null;
  }

  const formatPrice = (priceInCents: number) => {
    return (priceInCents / 100).toFixed(2);
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
    });
  };

  const getStatusColor = (status: string) => {
    switch (status.toLowerCase()) {
      case 'pending':
        return 'bg-yellow-100 text-yellow-800';
      case 'processing':
        return 'bg-blue-100 text-blue-800';
      case 'shipped':
        return 'bg-purple-100 text-purple-800';
      case 'delivered':
        return 'bg-green-100 text-green-800';
      case 'cancelled':
        return 'bg-red-100 text-red-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  };

  const OrderCard = ({ order }: { order: OrderDto }) => {
    const firstItem = order.items[0];
    const remainingItemsCount = order.items.length - 1;
    const imageUrl = firstItem?.generatedImageFilename ? `/api/user/images/${firstItem.generatedImageFilename}` : undefined;

    return (
      <div className="rounded-lg bg-white p-6 shadow-sm">
        <div className="flex items-start justify-between">
          <div className="flex gap-4">
            <div className="relative h-16 w-16 overflow-hidden rounded-lg bg-gray-100">
              {imageUrl ? (
                <img
                  src={imageUrl}
                  alt="Order item"
                  className="h-full w-full object-cover"
                  onError={(e) => {
                    e.currentTarget.style.display = 'none';
                    const placeholder = e.currentTarget.nextElementSibling;
                    if (placeholder) {
                      (placeholder as HTMLElement).style.display = 'flex';
                    }
                  }}
                />
              ) : null}
              <div className="flex h-16 w-16 items-center justify-center rounded-lg bg-gray-200" style={{ display: imageUrl ? 'none' : 'flex' }}>
                <Package className="h-6 w-6 text-gray-400" />
              </div>
            </div>
            <div>
              <h3 className="font-semibold text-gray-900">Order #{order.orderNumber}</h3>
              <p className="text-sm text-gray-600">Placed on {formatDate(order.createdAt)}</p>
              <p className="text-sm text-gray-600">
                {order.items.length} item{order.items.length > 1 ? 's' : ''}
                {firstItem && (
                  <>
                    {' • '}
                    {firstItem.article.name}
                    {remainingItemsCount > 0 && ` and ${remainingItemsCount} more`}
                  </>
                )}
              </p>
            </div>
          </div>
          <div className="text-right">
            <span className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium ${getStatusColor(order.status)}`}>
              {order.status}
            </span>
            <p className="mt-2 text-lg font-semibold text-gray-900">${formatPrice(order.totalAmount)}</p>
          </div>
        </div>
        <div className="mt-4 flex gap-3">
          <Button asChild variant="outline" size="sm">
            <Link to={`/order-success/${order.id}`}>View Details</Link>
          </Button>
          {order.status === 'PENDING' && (
            <Button variant="outline" size="sm" className="text-red-600 hover:text-red-700">
              Cancel Order
            </Button>
          )}
        </div>
      </div>
    );
  };

  // Loading state
  if (ordersLoading) {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <div className="text-center">
          <LoadingSpinner />
          <p className="mt-2 text-sm text-gray-600">Loading your orders...</p>
        </div>
      </div>
    );
  }

  // Error state
  if (ordersError) {
    return (
      <div className="flex min-h-screen items-center justify-center px-4">
        <div className="text-center">
          <div className="mx-auto h-12 w-12 text-red-500">
            <AlertTriangle className="h-12 w-12" />
          </div>
          <h2 className="mt-4 text-lg font-medium text-gray-900">Error loading orders</h2>
          <p className="mt-2 text-sm text-gray-600">{ordersError instanceof Error ? ordersError.message : 'Unable to load your orders'}</p>
          <Button onClick={() => window.location.reload()} className="mt-6">
            Try Again
          </Button>
        </div>
      </div>
    );
  }

  const orders = ordersData?.content || [];

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="mx-auto max-w-4xl px-4 py-8 sm:px-6 lg:px-8">
        <div className="mb-8">
          <h1 className="text-2xl font-bold text-gray-900 sm:text-3xl">Your Orders</h1>
          <p className="mt-2 text-gray-600">Track and manage your recent orders</p>
        </div>

        {orders.length === 0 ? (
          <div className="py-12 text-center">
            <ShoppingBag className="mx-auto h-12 w-12 text-gray-400" />
            <h3 className="mt-4 text-lg font-medium text-gray-900">No orders yet</h3>
            <p className="mt-2 text-gray-600">When you place orders, they'll appear here.</p>
            <Button asChild className="mt-6">
              <Link to="/">Start Shopping</Link>
            </Button>
          </div>
        ) : (
          <div className="space-y-6">
            <div className="text-sm text-gray-600">
              Showing {orders.length} order{orders.length > 1 ? 's' : ''}
            </div>
            {orders.map((order) => (
              <OrderCard key={order.id} order={order} />
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
