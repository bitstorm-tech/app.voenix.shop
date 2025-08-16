import { LoadingSpinner } from '@/components/LoadingSpinner';
import { AppHeader } from '@/components/layout/AppHeader';
import { Button } from '@/components/ui/Button';
import { useOrder } from '@/hooks/queries/useOrders';
import { createManualDownloadUrl, downloadOrderPDF } from '@/lib/pdfDownload';
import { AlertTriangle, CheckCircle, Download, Package, ShoppingBag, Truck } from 'lucide-react';
import { useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { toast } from 'sonner';

export default function OrderSuccessPage() {
  const { orderId } = useParams<{ orderId: string }>();
  const navigate = useNavigate();
  const { data: order, isLoading, error } = useOrder(orderId!);

  // PDF download states
  const [pdfDownloadStatus, setPdfDownloadStatus] = useState<'idle' | 'downloading' | 'success' | 'error'>('idle');
  const [pdfDownloadProgress, setPdfDownloadProgress] = useState(0);
  const [pdfDownloadError, setPdfDownloadError] = useState<string | null>(null);

  // Handle manual PDF download
  const handleDownloadPDF = async () => {
    if (!order) return;

    setPdfDownloadStatus('downloading');
    setPdfDownloadProgress(0);
    setPdfDownloadError(null);

    try {
      const result = await downloadOrderPDF({
        orderId: order.id,
        orderNumber: order.orderNumber,
        onProgress: (progress) => {
          setPdfDownloadProgress(progress);
        },
        onError: (error) => {
          setPdfDownloadError(error.message);
        },
      });

      if (result.success) {
        setPdfDownloadStatus('success');
        toast.success('Receipt downloaded!', {
          description: 'Your order receipt has been downloaded to your device.',
        });
      } else {
        setPdfDownloadStatus('error');
        setPdfDownloadError(result.error || 'Download failed');
        toast.error('Download failed', {
          description: 'You can try the manual download link below.',
        });
      }
    } catch (error) {
      setPdfDownloadStatus('error');
      const errorMessage = error instanceof Error ? error.message : 'Unknown error';
      setPdfDownloadError(errorMessage);
    }
  };

  if (isLoading) {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <div className="text-center">
          <LoadingSpinner />
          <p className="mt-2 text-sm text-gray-600">Loading order details...</p>
        </div>
      </div>
    );
  }

  if (error || !order) {
    return (
      <div className="flex min-h-screen items-center justify-center px-4">
        <div className="text-center">
          <div className="mx-auto h-12 w-12 text-red-500">
            <AlertTriangle className="h-12 w-12" />
          </div>
          <h2 className="mt-4 text-lg font-medium text-gray-900">Order not found</h2>
          <p className="mt-2 text-sm text-gray-600">{error instanceof Error ? error.message : 'Unable to load order details'}</p>
          <Button onClick={() => navigate('/')} className="mt-6">
            Continue shopping
          </Button>
        </div>
      </div>
    );
  }

  const formatPrice = (priceInCents: number) => {
    return (priceInCents / 100).toFixed(2);
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <AppHeader />
      <div className="mx-auto max-w-3xl px-4 pt-8 pb-8 sm:px-6 lg:px-8">
        {/* Success Header */}
        <div className="text-center">
          <div className="mx-auto flex h-16 w-16 items-center justify-center rounded-full bg-green-100">
            <CheckCircle className="h-8 w-8 text-green-600" />
          </div>
          <h1 className="mt-4 text-2xl font-bold text-gray-900 sm:text-3xl">Order confirmed!</h1>
          <p className="mt-2 text-gray-600">Thank you for your order. We&apos;ve received your order and will begin processing it shortly.</p>
        </div>

        {/* Order Details Card */}
        <div className="mt-8 rounded-lg bg-white p-6 shadow-sm">
          <div className="flex items-center justify-between border-b pb-4">
            <h2 className="text-lg font-semibold text-gray-900">Order Details</h2>
            <span className="inline-flex items-center rounded-full bg-green-100 px-2.5 py-0.5 text-xs font-medium text-green-800">
              {order.status}
            </span>
          </div>

          <div className="mt-4 grid gap-4 sm:grid-cols-2">
            <div>
              <h3 className="text-sm font-medium text-gray-500">Order Number</h3>
              <p className="mt-1 font-mono text-sm text-gray-900">{order.orderNumber}</p>
            </div>
            <div>
              <h3 className="text-sm font-medium text-gray-500">Order Date</h3>
              <p className="mt-1 text-sm text-gray-900">
                {new Date(order.createdAt).toLocaleDateString('en-US', {
                  year: 'numeric',
                  month: 'long',
                  day: 'numeric',
                })}
              </p>
            </div>
            <div>
              <h3 className="text-sm font-medium text-gray-500">Customer</h3>
              <p className="mt-1 text-sm text-gray-900">
                {order.customerFirstName} {order.customerLastName}
              </p>
              <p className="text-sm text-gray-600">{order.customerEmail}</p>
            </div>
            <div>
              <h3 className="text-sm font-medium text-gray-500">Total</h3>
              <p className="mt-1 text-lg font-semibold text-gray-900">${formatPrice(order.totalAmount)}</p>
            </div>
          </div>
        </div>

        {/* Order Items */}
        <div className="mt-8 rounded-lg bg-white p-6 shadow-sm">
          <h2 className="mb-4 text-lg font-semibold text-gray-900">Order Items</h2>
          <div className="space-y-4">
            {order.items.map((item) => {
              const imageUrl = item.generatedImageFilename ? `/api/user/images/${item.generatedImageFilename}` : undefined;

              return (
                <div key={item.id} className="flex gap-4 border-b pb-4 last:border-0 last:pb-0">
                  <div className="relative h-16 w-16 overflow-hidden rounded-lg bg-gray-100">
                    {imageUrl ? (
                      <img
                        src={imageUrl}
                        alt="Custom mug design"
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
                    <div
                      className="flex h-16 w-16 items-center justify-center rounded-lg bg-gray-200"
                      style={{ display: imageUrl ? 'none' : 'flex' }}
                    >
                      <Package className="h-6 w-6 text-gray-400" />
                    </div>
                  </div>
                  <div className="flex-1">
                    <h3 className="font-medium text-gray-900">{item.article.name}</h3>
                    <p className="text-sm text-gray-600">
                      {item.variant.colorCode} • Qty: {item.quantity}
                    </p>
                    <p className="text-sm text-gray-600">${formatPrice(item.pricePerItem)} each</p>
                  </div>
                  <div className="text-right">
                    <p className="font-medium text-gray-900">${formatPrice(item.totalPrice)}</p>
                  </div>
                </div>
              );
            })}
          </div>

          {/* Order Summary */}
          <div className="mt-6 border-t pt-4">
            <div className="space-y-2">
              <div className="flex justify-between text-sm">
                <span>Subtotal</span>
                <span>${formatPrice(order.subtotal)}</span>
              </div>
              <div className="flex justify-between text-sm">
                <span>Shipping</span>
                <span>${formatPrice(order.shippingAmount)}</span>
              </div>
              <div className="flex justify-between text-sm">
                <span>Tax</span>
                <span>${formatPrice(order.taxAmount)}</span>
              </div>
              <div className="flex justify-between border-t pt-2 text-base font-semibold">
                <span>Total</span>
                <span>${formatPrice(order.totalAmount)}</span>
              </div>
            </div>
          </div>
        </div>

        {/* Shipping Address */}
        <div className="mt-8 rounded-lg bg-white p-6 shadow-sm">
          <h2 className="mb-4 flex items-center gap-2 text-lg font-semibold text-gray-900">
            <Truck className="h-5 w-5" />
            Shipping Address
          </h2>
          <div className="text-sm text-gray-600">
            <p className="font-medium text-gray-900">
              {order.customerFirstName} {order.customerLastName}
            </p>
            <p>{order.shippingAddress.streetAddress1}</p>
            {order.shippingAddress.streetAddress2 && <p>{order.shippingAddress.streetAddress2}</p>}
            <p>
              {order.shippingAddress.city}, {order.shippingAddress.state} {order.shippingAddress.postalCode}
            </p>
            <p>{order.shippingAddress.country}</p>
          </div>
        </div>

        {/* PDF Download Section */}
        <div className="mt-8 rounded-lg bg-white p-6 shadow-sm">
          <h2 className="mb-4 flex items-center gap-2 text-lg font-semibold text-gray-900">
            <Download className="h-5 w-5" />
            Order Receipt
          </h2>

          <div className="space-y-4">
            <p className="text-sm text-gray-600">Download your order receipt as a PDF for your records.</p>

            {/* Download Progress */}
            {pdfDownloadStatus === 'downloading' && (
              <div className="space-y-2">
                <div className="flex items-center justify-between text-sm">
                  <span>Preparing download...</span>
                  <span>{pdfDownloadProgress}%</span>
                </div>
                <div className="h-2 w-full overflow-hidden rounded-full bg-gray-200">
                  <div className="h-full bg-blue-600 transition-all duration-300" style={{ width: `${pdfDownloadProgress}%` }} />
                </div>
              </div>
            )}

            {/* Success Message */}
            {pdfDownloadStatus === 'success' && (
              <div className="flex items-center gap-2 rounded-lg bg-green-50 p-3">
                <CheckCircle className="h-5 w-5 text-green-600" />
                <span className="text-sm font-medium text-green-800">Receipt downloaded successfully!</span>
              </div>
            )}

            {/* Error Message */}
            {pdfDownloadStatus === 'error' && pdfDownloadError && (
              <div className="flex items-center gap-2 rounded-lg bg-red-50 p-3">
                <AlertTriangle className="h-5 w-5 text-red-600" />
                <div className="text-sm text-red-800">
                  <div className="font-medium">Download failed</div>
                  <div>{pdfDownloadError}</div>
                </div>
              </div>
            )}

            {/* Download Buttons */}
            <div className="flex flex-col gap-2 sm:flex-row">
              <Button onClick={handleDownloadPDF} disabled={pdfDownloadStatus === 'downloading'} className="gap-2">
                <Download className="h-4 w-4" />
                {pdfDownloadStatus === 'downloading' ? 'Downloading...' : 'Download Receipt'}
              </Button>

              {/* Manual Download Link - Always visible as fallback */}
              <Button asChild variant="outline" className="gap-2">
                <a href={createManualDownloadUrl(order.id)} download target="_blank" rel="noopener noreferrer">
                  <Download className="h-4 w-4" />
                  Download PDF
                </a>
              </Button>
            </div>
          </div>
        </div>

        {/* What's Next */}
        <div className="mt-8 rounded-lg bg-blue-50 p-6">
          <h2 className="mb-2 text-lg font-semibold text-blue-900">What&apos;s next?</h2>
          <p className="text-sm text-blue-800">
            We&apos;ll send you an email confirmation with your order details and tracking information once your order ships. You can check your order
            status anytime by visiting your order history.
          </p>
        </div>

        {/* Action Buttons */}
        <div className="mt-8 flex flex-col gap-3 sm:flex-row sm:justify-center">
          <Button onClick={() => navigate('/orders')} variant="outline" className="gap-2">
            <ShoppingBag className="h-4 w-4" />
            View Order History
          </Button>
          <Button asChild>
            <Link to="/" reloadDocument>
              Continue Shopping
            </Link>
          </Button>
        </div>
      </div>
    </div>
  );
}
