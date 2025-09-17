import { LoadingSpinner } from '@/components/LoadingSpinner';
import { AppHeader } from '@/components/layout/AppHeader';
import { Button } from '@/components/ui/Button';
import { useOrder } from '@/hooks/queries/useOrders';
import { createManualDownloadUrl, downloadOrderPDF } from '@/lib/pdfDownload';
import { AlertTriangle, CheckCircle, Download, Package, ShoppingBag, Truck } from 'lucide-react';
import { useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { toast } from 'sonner';
import { useTranslation } from 'react-i18next';

export default function OrderSuccessPage() {
  const { orderId } = useParams<{ orderId: string }>();
  const navigate = useNavigate();
  const { data: order, isLoading, error } = useOrder(orderId!);
  const { t, i18n } = useTranslation('orderSuccess');
  const locale = i18n.language === 'de' ? 'de-DE' : 'en-US';

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
        onError: (downloadError) => {
          setPdfDownloadError(downloadError.message || t('receipt.errorDefault'));
        },
      });

      if (result.success) {
        setPdfDownloadStatus('success');
        toast.success(t('toast.downloadSuccess.title'), {
          description: t('toast.downloadSuccess.description'),
        });
      } else {
        setPdfDownloadStatus('error');
        setPdfDownloadError(result.error || t('receipt.errorDefault'));
        toast.error(t('toast.downloadError.title'), {
          description: t('toast.downloadError.description'),
        });
      }
    } catch (err) {
      setPdfDownloadStatus('error');
      const errorMessage = err instanceof Error && err.message ? err.message : t('receipt.errorDefault');
      setPdfDownloadError(errorMessage);
      toast.error(t('toast.downloadError.title'), {
        description: t('toast.downloadError.description'),
      });
    }
  };

  if (isLoading) {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <div className="text-center">
          <LoadingSpinner />
          <p className="mt-2 text-sm text-gray-600">{t('loading.indicator')}</p>
        </div>
      </div>
    );
  }

  if (error || !order) {
    const errorMessage = error instanceof Error && error.message ? error.message : t('error.defaultMessage');
    return (
      <div className="flex min-h-screen items-center justify-center px-4">
        <div className="text-center">
          <div className="mx-auto h-12 w-12 text-red-500">
            <AlertTriangle className="h-12 w-12" />
          </div>
          <h2 className="mt-4 text-lg font-medium text-gray-900">{t('error.title')}</h2>
          <p className="mt-2 text-sm text-gray-600">{errorMessage}</p>
          <Button onClick={() => navigate('/')} className="mt-6">
            {t('error.cta')}
          </Button>
        </div>
      </div>
    );
  }

  const currencyFormatter = new Intl.NumberFormat(locale, { style: 'currency', currency: 'USD' });

  const formatPrice = (priceInCents: number) => {
    return currencyFormatter.format(priceInCents / 100);
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString(locale, {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    });
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
          <h1 className="mt-4 text-2xl font-bold text-gray-900 sm:text-3xl">{t('header.title')}</h1>
          <p className="mt-2 text-gray-600">{t('header.description')}</p>
        </div>

        {/* Order Details Card */}
        <div className="mt-8 rounded-lg bg-white p-6 shadow-sm">
          <div className="flex items-center justify-between border-b pb-4">
            <h2 className="text-lg font-semibold text-gray-900">{t('detailsCard.title')}</h2>
            <span className="inline-flex items-center rounded-full bg-green-100 px-2.5 py-0.5 text-xs font-medium text-green-800">
              {t(`detailsCard.status.${order.status.toLowerCase()}`, { defaultValue: order.status })}
            </span>
          </div>

          <div className="mt-4 grid gap-4 sm:grid-cols-2">
            <div>
              <h3 className="text-sm font-medium text-gray-500">{t('detailsCard.orderNumberLabel')}</h3>
              <p className="mt-1 font-mono text-sm text-gray-900">{order.orderNumber}</p>
            </div>
            <div>
              <h3 className="text-sm font-medium text-gray-500">{t('detailsCard.orderDateLabel')}</h3>
              <p className="mt-1 text-sm text-gray-900">{formatDate(order.createdAt)}</p>
            </div>
            <div>
              <h3 className="text-sm font-medium text-gray-500">{t('detailsCard.customerLabel')}</h3>
              <p className="mt-1 text-sm text-gray-900">
                {order.customerFirstName} {order.customerLastName}
              </p>
              <p className="text-sm text-gray-600">{order.customerEmail}</p>
            </div>
            <div>
              <h3 className="text-sm font-medium text-gray-500">{t('detailsCard.totalLabel')}</h3>
              <p className="mt-1 text-lg font-semibold text-gray-900">{formatPrice(order.totalAmount)}</p>
            </div>
          </div>
        </div>

        {/* Order Items */}
        <div className="mt-8 rounded-lg bg-white p-6 shadow-sm">
          <h2 className="mb-4 text-lg font-semibold text-gray-900">{t('items.title')}</h2>
          <div className="space-y-4">
            {order.items.map((item) => {
              const imageUrl = item.generatedImageFilename ? `/api/user/images/${item.generatedImageFilename}` : undefined;

              return (
                <div key={item.id} className="flex gap-4 border-b pb-4 last:border-0 last:pb-0">
                  <div className="relative h-16 w-16 overflow-hidden rounded-lg bg-gray-100">
                    {imageUrl ? (
                      <img
                        src={imageUrl}
                        alt={t('items.imageAlt')}
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
                    <p className="text-sm text-gray-600">{t('items.variant', { color: item.variant.colorCode, quantity: item.quantity })}</p>
                    <p className="text-sm text-gray-600">{t('items.priceEach', { price: formatPrice(item.pricePerItem) })}</p>
                  </div>
                  <div className="text-right">
                    <p className="font-medium text-gray-900">{formatPrice(item.totalPrice)}</p>
                  </div>
                </div>
              );
            })}
          </div>

          {/* Order Summary */}
          <div className="mt-6 border-t pt-4">
            <div className="space-y-2">
              <div className="flex justify-between text-sm">
                <span>{t('summary.subtotal')}</span>
                <span>{formatPrice(order.subtotal)}</span>
              </div>
              <div className="flex justify-between text-sm">
                <span>{t('summary.shipping')}</span>
                <span>{formatPrice(order.shippingAmount)}</span>
              </div>
              <div className="flex justify-between text-sm">
                <span>{t('summary.tax')}</span>
                <span>{formatPrice(order.taxAmount)}</span>
              </div>
              <div className="flex justify-between border-t pt-2 text-base font-semibold">
                <span>{t('summary.total')}</span>
                <span>{formatPrice(order.totalAmount)}</span>
              </div>
            </div>
          </div>
        </div>

        {/* Shipping Address */}
        <div className="mt-8 rounded-lg bg-white p-6 shadow-sm">
          <h2 className="mb-4 flex items-center gap-2 text-lg font-semibold text-gray-900">
            <Truck className="h-5 w-5" />
            {t('shipping.title')}
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
            {t('receipt.title')}
          </h2>

          <div className="space-y-4">
            <p className="text-sm text-gray-600">{t('receipt.description')}</p>

            {/* Download Progress */}
            {pdfDownloadStatus === 'downloading' && (
              <div className="space-y-2">
                <div className="flex items-center justify-between text-sm">
                  <span>{t('receipt.progressLabel')}</span>
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
                <span className="text-sm font-medium text-green-800">{t('receipt.success')}</span>
              </div>
            )}

            {/* Error Message */}
            {pdfDownloadStatus === 'error' && (
              <div className="flex items-center gap-2 rounded-lg bg-red-50 p-3">
                <AlertTriangle className="h-5 w-5 text-red-600" />
                <div className="text-sm text-red-800">
                  <div className="font-medium">{t('receipt.errorTitle')}</div>
                  <div>{pdfDownloadError ?? t('receipt.errorDefault')}</div>
                  <div>{t('receipt.errorHelp')}</div>
                </div>
              </div>
            )}

            {/* Download Buttons */}
            <div className="flex flex-col gap-2 sm:flex-row">
              <Button onClick={handleDownloadPDF} disabled={pdfDownloadStatus === 'downloading'} className="gap-2">
                <Download className="h-4 w-4" />
                {pdfDownloadStatus === 'downloading' ? t('receipt.primary.downloading') : t('receipt.primary.idle')}
              </Button>

              {/* Manual Download Link - Always visible as fallback */}
              <Button asChild variant="outline" className="gap-2">
                <a href={createManualDownloadUrl(order.id)} download target="_blank" rel="noopener noreferrer">
                  <Download className="h-4 w-4" />
                  {t('receipt.secondary')}
                </a>
              </Button>
            </div>
          </div>
        </div>

        {/* What's Next */}
        <div className="mt-8 rounded-lg bg-blue-50 p-6">
          <h2 className="mb-2 text-lg font-semibold text-blue-900">{t('nextSteps.title')}</h2>
          <p className="text-sm text-blue-800">{t('nextSteps.description')}</p>
        </div>

        {/* Action Buttons */}
        <div className="mt-8 flex flex-col gap-3 sm:flex-row sm:justify-center">
          <Button onClick={() => navigate('/orders')} variant="outline" className="gap-2">
            <ShoppingBag className="h-4 w-4" />
            {t('actions.viewOrders')}
          </Button>
          <Button asChild>
            <Link to="/" reloadDocument>
              {t('actions.continueShopping')}
            </Link>
          </Button>
        </div>
      </div>
    </div>
  );
}
