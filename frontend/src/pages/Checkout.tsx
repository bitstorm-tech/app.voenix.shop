import { LoadingSpinner } from '@/components/LoadingSpinner';
import { Alert, AlertDescription } from '@/components/ui/Alert';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import { Label } from '@/components/ui/Label';
import { useSession } from '@/hooks/queries/useAuth';
import { useCart } from '@/hooks/queries/useCart';
import { useCreateOrder } from '@/hooks/queries/useOrders';
import { downloadOrderPDF } from '@/lib/pdfDownload';
import type { CreateOrderRequest } from '@/types/order';
import { AlertTriangle, CreditCard, Lock, ShoppingBag, Truck } from 'lucide-react';
import { ReactNode, useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useNavigate } from 'react-router-dom';
import { toast } from 'sonner';

// Pricing configuration
const CHECKOUT_CONFIG = {
  shipping: 4.99,
  taxRate: 0.08,
};

// Form field configurations
interface FieldConfig {
  name: string;
  type?: string;
  required?: boolean;
  span?: number;
  labelKey: string;
  placeholderKey?: string;
}

const contactFields: FieldConfig[] = [
  {
    name: 'email',
    type: 'email',
    required: true,
    span: 2,
    labelKey: 'contact.fields.email.label',
    placeholderKey: 'contact.fields.email.placeholder',
  },
  { name: 'firstName', required: true, labelKey: 'contact.fields.firstName.label', placeholderKey: 'contact.fields.firstName.placeholder' },
  { name: 'lastName', required: true, labelKey: 'contact.fields.lastName.label', placeholderKey: 'contact.fields.lastName.placeholder' },
  { name: 'phone', type: 'tel', span: 2, labelKey: 'contact.fields.phone.label', placeholderKey: 'contact.fields.phone.placeholder' },
];

const addressFields: FieldConfig[] = [
  {
    name: 'streetAddress1',
    required: true,
    span: 2,
    labelKey: 'shipping.fields.streetAddress1.label',
    placeholderKey: 'shipping.fields.streetAddress1.placeholder',
  },
  { name: 'streetAddress2', span: 2, labelKey: 'shipping.fields.streetAddress2.label', placeholderKey: 'shipping.fields.streetAddress2.placeholder' },
  { name: 'city', required: true, labelKey: 'shipping.fields.city.label', placeholderKey: 'shipping.fields.city.placeholder' },
  { name: 'state', required: true, labelKey: 'shipping.fields.state.label', placeholderKey: 'shipping.fields.state.placeholder' },
  { name: 'postalCode', required: true, labelKey: 'shipping.fields.postalCode.label', placeholderKey: 'shipping.fields.postalCode.placeholder' },
  { name: 'country', required: true, labelKey: 'shipping.fields.country.label', placeholderKey: 'shipping.fields.country.placeholder' },
];

// Reusable state component
interface CheckoutStateProps {
  icon?: React.ComponentType<{ className?: string }>;
  title?: string;
  message?: string;
  action?: ReactNode;
}

const CheckoutState = ({ icon: Icon, title, message, action }: CheckoutStateProps) => (
  <div className="flex min-h-screen items-center justify-center px-4">
    <div className="text-center">
      {Icon && <Icon className="mx-auto h-12 w-12 text-gray-400" />}
      {title && <h2 className="mt-4 text-lg font-medium text-gray-900">{title}</h2>}
      {message && <p className="mt-2 text-sm text-gray-600">{message}</p>}
      {action}
    </div>
  </div>
);

// Using the real CartDto and CartItemDto types from the API

interface FormData {
  email: string;
  firstName: string;
  lastName: string;
  phone: string;
  shipping_address: {
    streetAddress1: string;
    streetAddress2: string;
    city: string;
    state: string;
    postalCode: string;
    country: string;
  };
  sameAsBilling: boolean;
}

export default function CheckoutPage() {
  const navigate = useNavigate();
  const { t } = useTranslation('checkout');
  const { data: session, isLoading: sessionLoading } = useSession();
  const { data: cartData, isLoading: cartLoading, error: cartError } = useCart();
  const createOrderMutation = useCreateOrder();
  const [imageErrors, setImageErrors] = useState<Record<string, boolean>>({});
  const [formData, setFormData] = useState<FormData>({
    email: '',
    firstName: '',
    lastName: '',
    phone: '',
    shipping_address: {
      streetAddress1: '',
      streetAddress2: '',
      city: '',
      state: '',
      postalCode: '',
      country: 'US',
    },
    sameAsBilling: true,
  });

  // Handle redirects
  useEffect(() => {
    if (sessionLoading || cartLoading) return;

    if (!session?.authenticated) {
      navigate('/login?redirect=' + encodeURIComponent('/checkout'));
    } else if (cartData?.isEmpty) {
      navigate('/cart');
    }
  }, [session, sessionLoading, cartData, cartLoading, navigate]);

  // Convert cart data for display
  const items = cartData?.items || [];
  const totalItems = cartData?.totalItemCount || 0;
  const subtotal = (cartData?.totalPrice || 0) / 100; // Convert cents to base currency
  const formatCurrency = (value: number) => t('currency', { value: value.toFixed(2) });

  const updateField = (field: string, value: string) => {
    setFormData((prev) => ({ ...prev, [field]: value }));
  };

  const updateAddress = (field: string, value: string) => {
    setFormData((prev) => ({
      ...prev,
      shipping_address: { ...prev.shipping_address, [field]: value },
    }));
  };

  const formatAddress = (addr: FormData['shipping_address']) => ({
    streetAddress1: addr.streetAddress1,
    streetAddress2: addr.streetAddress2 || undefined,
    city: addr.city,
    state: addr.state,
    postalCode: addr.postalCode,
    country: addr.country,
  });

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!cartData || cartData.isEmpty) {
      console.error('Cart is empty');
      return;
    }

    try {
      const shippingAddr = formatAddress(formData.shipping_address);
      const orderRequest: CreateOrderRequest = {
        customerEmail: formData.email,
        customerFirstName: formData.firstName,
        customerLastName: formData.lastName,
        customerPhone: formData.phone || undefined,
        shippingAddress: shippingAddr,
        useShippingAsBilling: formData.sameAsBilling,
        billingAddress: formData.sameAsBilling ? undefined : shippingAddr,
      };

      const order = await createOrderMutation.mutateAsync(orderRequest);
      console.log('Order created successfully:', order);

      toast.success(t('toast.orderSuccess.title'), {
        description: t('toast.orderSuccess.description', { orderNumber: order.orderNumber }),
      });

      // Auto-download PDF
      setTimeout(async () => {
        const result = await downloadOrderPDF({
          orderId: order.id,
          orderNumber: order.orderNumber,
        });

        if (result.success) {
          toast.success(t('toast.receiptSuccess.title'), {
            description: t('toast.receiptSuccess.description'),
          });
        } else {
          toast.error(t('toast.receiptError.title'), {
            description: t('toast.receiptError.description'),
          });
        }
      }, 500);

      // Navigate to order success page
      navigate(`/order-success/${order.id}`);
    } catch (error) {
      console.error('Error creating order:', error);
      toast.error(t('toast.orderError.title'), {
        description: error instanceof Error ? error.message : t('toast.orderError.description'),
      });
    }
  };

  // Handle loading and error states
  if (sessionLoading || cartLoading) {
    return <CheckoutState icon={LoadingSpinner} message={cartLoading ? t('loading') : undefined} />;
  }

  if (!session?.authenticated) return null;

  if (cartError) {
    const errorMessage = typeof cartError === 'object' && cartError && 'message' in cartError ? (cartError as Error).message : t('error.description');
    return (
      <CheckoutState
        icon={AlertTriangle}
        title={t('error.heading')}
        message={errorMessage}
        action={
          <Button onClick={() => window.location.reload()} className="mt-6">
            {t('actions.tryAgain')}
          </Button>
        }
      />
    );
  }

  if (!cartData?.items?.length) {
    return (
      <CheckoutState
        icon={ShoppingBag}
        title={t('empty.heading')}
        message={t('empty.message')}
        action={
          <Button onClick={() => navigate('/cart')} className="mt-6">
            {t('actions.viewCart')}
          </Button>
        }
      />
    );
  }

  const { shipping, taxRate } = CHECKOUT_CONFIG;
  const tax = subtotal * taxRate;
  const total = subtotal + shipping + tax;

  const hasPriceChanges = items.some((item) => item.hasPriceChanged);

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
        <h1 className="mb-8 text-2xl font-bold text-gray-900 sm:text-3xl">{t('title')}</h1>

        <form onSubmit={handleSubmit} className="grid gap-8 lg:grid-cols-3">
          <div className="lg:col-span-2">
            <div className="space-y-6">
              <div className="rounded-lg bg-white p-6 shadow-sm">
                <h2 className="mb-4 text-lg font-semibold">{t('contact.heading')}</h2>
                <div className="grid gap-4 sm:grid-cols-2">
                  {contactFields.map((field) => (
                    <div key={field.name} className={field.span === 2 ? 'sm:col-span-2' : ''}>
                      <Label htmlFor={field.name}>{t(field.labelKey)}</Label>
                      <Input
                        id={field.name}
                        type={field.type || 'text'}
                        value={formData[field.name as keyof FormData] as string}
                        onChange={(e) => updateField(field.name, e.target.value)}
                        placeholder={field.placeholderKey ? t(field.placeholderKey) : undefined}
                        className="mt-1"
                        required={field.required}
                      />
                    </div>
                  ))}
                </div>
              </div>

              <div className="rounded-lg bg-white p-6 shadow-sm">
                <h2 className="mb-4 flex items-center gap-2 text-lg font-semibold">
                  <Truck className="h-5 w-5" />
                  {t('shipping.heading')}
                </h2>
                <div className="grid gap-4 sm:grid-cols-2">
                  {addressFields.map((field) => (
                    <div key={field.name} className={field.span === 2 ? 'sm:col-span-2' : ''}>
                      <Label htmlFor={field.name}>{t(field.labelKey)}</Label>
                      <Input
                        id={field.name}
                        value={formData.shipping_address[field.name as keyof FormData['shipping_address']]}
                        onChange={(e) => updateAddress(field.name, e.target.value)}
                        placeholder={field.placeholderKey ? t(field.placeholderKey) : undefined}
                        className="mt-1"
                        required={field.required}
                      />
                    </div>
                  ))}
                </div>
                <div className="mt-4">
                  <label className="flex items-center gap-2">
                    <input
                      type="checkbox"
                      checked={formData.sameAsBilling}
                      onChange={(e) => setFormData((prev) => ({ ...prev, sameAsBilling: e.target.checked }))}
                      className="rounded border-gray-300"
                    />
                    <span className="text-sm">{t('shipping.checkbox')}</span>
                  </label>
                </div>
              </div>

              <div className="rounded-lg bg-white p-6 shadow-sm">
                <h2 className="mb-4 flex items-center gap-2 text-lg font-semibold">
                  <CreditCard className="h-5 w-5" />
                  {t('payment.heading')}
                </h2>
                <p className="text-sm text-gray-600">{t('payment.description')}</p>
              </div>
            </div>
          </div>

          <div className="lg:col-span-1">
            <div className="sticky top-8 space-y-4">
              <div className="rounded-lg bg-white p-6 shadow-sm">
                <h2 className="mb-4 text-lg font-semibold">{t('summary.heading')}</h2>

                {hasPriceChanges && (
                  <Alert variant="info" className="mb-4">
                    <AlertTriangle className="h-4 w-4" />
                    <AlertDescription>{t('summary.alert')}</AlertDescription>
                  </Alert>
                )}

                <div className="space-y-3">
                  {items.map((item) => {
                    const itemTotal = item.totalPrice / 100; // Convert cents to dollars
                    const imageUrl = item.generatedImageFilename ? `/api/user/images/${item.generatedImageFilename}` : undefined;

                    return (
                      <div key={item.id} className="flex gap-3">
                        <div className="relative h-16 w-16 overflow-hidden rounded-lg bg-gray-100">
                          {imageUrl && !imageErrors[item.id] ? (
                            <img
                              src={imageUrl}
                              alt={t('items.imageAlt')}
                              className="h-full w-full object-cover"
                              onError={() => setImageErrors((prev) => ({ ...prev, [item.id]: true }))}
                            />
                          ) : (
                            <div className="flex h-full w-full items-center justify-center bg-gray-200">
                              <ShoppingBag className="h-6 w-6 text-gray-400" />
                            </div>
                          )}
                        </div>
                        <div className="flex-1">
                          <h3 className="text-sm font-medium">{item.article.name}</h3>
                          <p className="text-sm text-gray-600">
                            {item.variant?.colorCode
                              ? t('items.variantDetails', { color: item.variant.colorCode, quantity: item.quantity })
                              : t('items.quantityOnly', { quantity: item.quantity })}
                          </p>
                          {item.hasPriceChanged && item.originalPrice ? (
                            <p className="text-xs text-orange-600">{t('items.priceUpdated', { amount: formatCurrency(item.originalPrice / 100) })}</p>
                          ) : null}
                        </div>
                        <div className="text-sm font-medium">{formatCurrency(itemTotal)}</div>
                      </div>
                    );
                  })}
                </div>

                <div className="mt-4 space-y-2 border-t pt-4">
                  <div className="flex justify-between text-sm">
                    <span>{t('summary.subtotal', { count: totalItems })}</span>
                    <span>{formatCurrency(subtotal)}</span>
                  </div>
                  <div className="flex justify-between text-sm">
                    <span>{t('summary.shipping')}</span>
                    <span>{formatCurrency(shipping)}</span>
                  </div>
                  <div className="flex justify-between text-sm">
                    <span>{t('summary.tax')}</span>
                    <span>{formatCurrency(tax)}</span>
                  </div>
                  <div className="flex justify-between border-t pt-2 text-base font-semibold">
                    <span>{t('summary.total')}</span>
                    <span>{formatCurrency(total)}</span>
                  </div>
                </div>
              </div>

              <Button type="submit" className="w-full gap-2" size="lg" disabled={createOrderMutation.isPending}>
                <Lock className="h-4 w-4" />
                {createOrderMutation.isPending ? t('summary.processing') : t('summary.checkout', { amount: formatCurrency(total) })}
              </Button>

              <div className="text-center text-xs text-gray-500">
                <Lock className="mx-auto mb-1 h-4 w-4" />
                {t('security')}
              </div>
            </div>
          </div>
        </form>
      </div>
    </div>
  );
}
