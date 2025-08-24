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
  label: string;
  type?: string;
  placeholder: string;
  required?: boolean;
  span?: number;
}

const contactFields: FieldConfig[] = [
  { name: 'email', label: 'Email Address', type: 'email', placeholder: 'john.doe@example.com', required: true, span: 2 },
  { name: 'firstName', label: 'First Name', placeholder: 'John', required: true },
  { name: 'lastName', label: 'Last Name', placeholder: 'Doe', required: true },
  { name: 'phone', label: 'Phone Number (optional)', type: 'tel', placeholder: '+1 (555) 123-4567', span: 2 },
];

const addressFields: FieldConfig[] = [
  { name: 'streetAddress1', label: 'Street Address', placeholder: '123 Main Street', required: true, span: 2 },
  { name: 'streetAddress2', label: 'Apartment, suite, etc. (optional)', placeholder: 'Apt 4B', span: 2 },
  { name: 'city', label: 'City', placeholder: 'New York', required: true },
  { name: 'state', label: 'State', placeholder: 'NY', required: true },
  { name: 'postalCode', label: 'ZIP Code', placeholder: '10001', required: true },
  { name: 'country', label: 'Country', placeholder: 'US', required: true },
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
  const subtotal = (cartData?.totalPrice || 0) / 100; // Convert cents to dollars

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

      toast.success('Order placed successfully!', {
        description: `Order #${order.orderNumber} has been created.`,
      });

      // Auto-download PDF
      setTimeout(async () => {
        const result = await downloadOrderPDF({
          orderId: order.id,
          orderNumber: order.orderNumber,
        });

        if (result.success) {
          toast.success('Receipt downloaded!', {
            description: 'Your order receipt has been downloaded to your device.',
          });
        } else {
          toast.error('PDF download failed', {
            description: 'You can download your receipt from the order details page.',
          });
        }
      }, 500);

      // Navigate to order success page
      navigate(`/order-success/${order.id}`);
    } catch (error) {
      console.error('Error creating order:', error);
      toast.error('Failed to place order', {
        description: error instanceof Error ? error.message : 'Please try again or contact support.',
      });
    }
  };

  // Handle loading and error states
  if (sessionLoading || cartLoading) {
    return <CheckoutState icon={LoadingSpinner} message={cartLoading ? 'Loading checkout...' : undefined} />;
  }

  if (!session?.authenticated) return null;

  if (cartError) {
    const errorMessage =
      typeof cartError === 'object' && cartError && 'message' in cartError ? (cartError as Error).message : 'Unable to load your cart';
    return (
      <CheckoutState
        icon={AlertTriangle}
        title="Error loading cart"
        message={errorMessage}
        action={
          <Button onClick={() => window.location.reload()} className="mt-6">
            Try Again
          </Button>
        }
      />
    );
  }

  if (!cartData?.items?.length) {
    return (
      <CheckoutState
        icon={ShoppingBag}
        title="Your cart is empty"
        message="Add items to your cart before checking out"
        action={
          <Button onClick={() => navigate('/cart')} className="mt-6">
            View Cart
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
        <h1 className="mb-8 text-2xl font-bold text-gray-900 sm:text-3xl">Checkout</h1>

        <form onSubmit={handleSubmit} className="grid gap-8 lg:grid-cols-3">
          <div className="lg:col-span-2">
            <div className="space-y-6">
              <div className="rounded-lg bg-white p-6 shadow-sm">
                <h2 className="mb-4 text-lg font-semibold">Contact Information</h2>
                <div className="grid gap-4 sm:grid-cols-2">
                  {contactFields.map((field) => (
                    <div key={field.name} className={field.span === 2 ? 'sm:col-span-2' : ''}>
                      <Label htmlFor={field.name}>{field.label}</Label>
                      <Input
                        id={field.name}
                        type={field.type || 'text'}
                        value={formData[field.name as keyof FormData] as string}
                        onChange={(e) => updateField(field.name, e.target.value)}
                        placeholder={field.placeholder}
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
                  Shipping Information
                </h2>
                <div className="grid gap-4 sm:grid-cols-2">
                  {addressFields.map((field) => (
                    <div key={field.name} className={field.span === 2 ? 'sm:col-span-2' : ''}>
                      <Label htmlFor={field.name}>{field.label}</Label>
                      <Input
                        id={field.name}
                        value={formData.shipping_address[field.name as keyof FormData['shipping_address']]}
                        onChange={(e) => updateAddress(field.name, e.target.value)}
                        placeholder={field.placeholder}
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
                    <span className="text-sm">Billing address same as shipping</span>
                  </label>
                </div>
              </div>

              <div className="rounded-lg bg-white p-6 shadow-sm">
                <h2 className="mb-4 flex items-center gap-2 text-lg font-semibold">
                  <CreditCard className="h-5 w-5" />
                  Payment Information
                </h2>
                <p className="text-sm text-gray-600">
                  Payment processing will be implemented in the next phase. For now, orders will be created without payment.
                </p>
              </div>
            </div>
          </div>

          <div className="lg:col-span-1">
            <div className="sticky top-8 space-y-4">
              <div className="rounded-lg bg-white p-6 shadow-sm">
                <h2 className="mb-4 text-lg font-semibold">Order Summary</h2>

                {hasPriceChanges && (
                  <Alert variant="info" className="mb-4">
                    <AlertTriangle className="h-4 w-4" />
                    <AlertDescription>
                      Some items in your cart have had price changes since they were added. The updated prices are shown below.
                    </AlertDescription>
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
                              alt="Custom mug design"
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
                            {item.variant.colorCode} • Qty: {item.quantity}
                          </p>
                          {item.hasPriceChanged && (
                            <p className="text-xs text-orange-600">Price updated: was ${(item.originalPrice / 100).toFixed(2)}</p>
                          )}
                        </div>
                        <div className="text-sm font-medium">${itemTotal.toFixed(2)}</div>
                      </div>
                    );
                  })}
                </div>

                <div className="mt-4 space-y-2 border-t pt-4">
                  <div className="flex justify-between text-sm">
                    <span>Subtotal ({totalItems} items)</span>
                    <span>${subtotal.toFixed(2)}</span>
                  </div>
                  <div className="flex justify-between text-sm">
                    <span>Shipping</span>
                    <span>${shipping.toFixed(2)}</span>
                  </div>
                  <div className="flex justify-between text-sm">
                    <span>Tax</span>
                    <span>${tax.toFixed(2)}</span>
                  </div>
                  <div className="flex justify-between border-t pt-2 text-base font-semibold">
                    <span>Total</span>
                    <span>${total.toFixed(2)}</span>
                  </div>
                </div>
              </div>

              <Button type="submit" className="w-full gap-2" size="lg" disabled={createOrderMutation.isPending}>
                <Lock className="h-4 w-4" />
                {createOrderMutation.isPending ? 'Processing...' : `Buy Now • $${total.toFixed(2)}`}
              </Button>

              <div className="text-center text-xs text-gray-500">
                <Lock className="mx-auto mb-1 h-4 w-4" />
                Your payment information is secure and encrypted
              </div>
            </div>
          </div>
        </form>
      </div>
    </div>
  );
}
