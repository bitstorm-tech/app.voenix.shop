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
import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { toast } from 'sonner';

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

  // Redirect to login if not authenticated
  useEffect(() => {
    if (!sessionLoading && !session?.authenticated) {
      navigate('/login?redirect=' + encodeURIComponent('/checkout'));
    }
  }, [session, sessionLoading, navigate]);

  // Handle empty cart - redirect to cart page
  useEffect(() => {
    if (!cartLoading && cartData && cartData.isEmpty) {
      navigate('/cart');
    }
  }, [cartData, cartLoading, navigate]);

  // Convert cart data for display
  const items = cartData?.items || [];
  const totalItems = cartData?.totalItemCount || 0;
  const subtotal = (cartData?.totalPrice || 0) / 100; // Convert cents to dollars

  const handleInputChange = (field: string, value: string) => {
    if (field.startsWith('shipping_address.')) {
      const addressField = field.replace('shipping_address.', '');
      setFormData((prev) => ({
        ...prev,
        shipping_address: {
          ...prev.shipping_address,
          [addressField]: value,
        },
      }));
    } else {
      setFormData((prev) => ({
        ...prev,
        [field]: value,
      }));
    }
  };


  // Auto-download PDF after successful order creation
  const triggerPDFDownload = async (orderId: string, orderNumber: string) => {
    try {
      console.log('Attempting to auto-download PDF for order:', orderNumber);

      const result = await downloadOrderPDF({
        orderId,
        orderNumber,
        onProgress: (progress) => {
          // Could show progress in toast if needed
          console.log(`PDF download progress: ${progress}%`);
        },
        onError: (error) => {
          console.error('PDF download error:', error);
          toast.error('PDF download failed', {
            description: 'You can download your receipt from the order details page.',
          });
        },
      });

      if (result.success) {
        toast.success('Receipt downloaded!', {
          description: 'Your order receipt has been downloaded to your device.',
        });
      }
    } catch (error) {
      console.error('Error during PDF download:', error);
      // Don't show error toast here as the onError callback handles it
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!cartData || cartData.isEmpty) {
      console.error('Cart is empty');
      return;
    }

    try {
      const orderRequest: CreateOrderRequest = {
        customerEmail: formData.email,
        customerFirstName: formData.firstName,
        customerLastName: formData.lastName,
        customerPhone: formData.phone || undefined,
        shippingAddress: {
          streetAddress1: formData.shipping_address.streetAddress1,
          streetAddress2: formData.shipping_address.streetAddress2 || undefined,
          city: formData.shipping_address.city,
          state: formData.shipping_address.state,
          postalCode: formData.shipping_address.postalCode,
          country: formData.shipping_address.country,
        },
        useShippingAsBilling: formData.sameAsBilling,
        billingAddress: formData.sameAsBilling
          ? undefined
          : {
              streetAddress1: formData.shipping_address.streetAddress1,
              streetAddress2: formData.shipping_address.streetAddress2 || undefined,
              city: formData.shipping_address.city,
              state: formData.shipping_address.state,
              postalCode: formData.shipping_address.postalCode,
              country: formData.shipping_address.country,
            },
      };

      const order = await createOrderMutation.mutateAsync(orderRequest);
      console.log('Order created successfully:', order);

      toast.success('Order placed successfully!', {
        description: `Order #${order.orderNumber} has been created.`,
      });

      // Trigger automatic PDF download
      // Use setTimeout to ensure navigation doesn't interfere with download
      setTimeout(() => {
        triggerPDFDownload(order.id, order.orderNumber);
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

  // Loading state for cart data
  if (cartLoading) {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <div className="text-center">
          <LoadingSpinner />
          <p className="mt-2 text-sm text-gray-600">Loading checkout...</p>
        </div>
      </div>
    );
  }

  // Error state for cart
  if (cartError) {
    return (
      <div className="flex min-h-screen items-center justify-center px-4">
        <div className="text-center">
          <div className="mx-auto h-12 w-12 text-red-500">
            <AlertTriangle className="h-12 w-12" />
          </div>
          <h2 className="mt-4 text-lg font-medium text-gray-900">Error loading cart</h2>
          <p className="mt-2 text-sm text-gray-600">{cartError instanceof Error ? cartError.message : 'Unable to load your cart'}</p>
          <Button onClick={() => window.location.reload()} className="mt-6">
            Try Again
          </Button>
        </div>
      </div>
    );
  }

  // This case should be handled by the redirect effect, but keeping as fallback
  if (!cartData || cartData.isEmpty) {
    return (
      <div className="flex min-h-screen items-center justify-center px-4">
        <div className="text-center">
          <ShoppingBag className="mx-auto h-12 w-12 text-gray-400" />
          <h2 className="mt-4 text-lg font-medium text-gray-900">Your cart is empty</h2>
          <p className="mt-2 text-sm text-gray-600">Add items to your cart before checking out</p>
          <Button onClick={() => navigate('/cart')} className="mt-6">
            View Cart
          </Button>
        </div>
      </div>
    );
  }

  const shipping = 4.99; // TODO: Make configurable
  const tax = subtotal * 0.08; // TODO: Make configurable - currently 8%
  const total = subtotal + shipping + tax;

  // Check if any items have price changes
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
                  <div className="sm:col-span-2">
                    <Label htmlFor="email">Email Address</Label>
                    <Input
                      id="email"
                      type="email"
                      value={formData.email}
                      onChange={(e) => handleInputChange('email', e.target.value)}
                      placeholder="john.doe@example.com"
                      className="mt-1"
                      required
                    />
                  </div>
                  <div>
                    <Label htmlFor="firstName">First Name</Label>
                    <Input
                      id="firstName"
                      value={formData.firstName}
                      onChange={(e) => handleInputChange('firstName', e.target.value)}
                      placeholder="John"
                      className="mt-1"
                      required
                    />
                  </div>
                  <div>
                    <Label htmlFor="lastName">Last Name</Label>
                    <Input
                      id="lastName"
                      value={formData.lastName}
                      onChange={(e) => handleInputChange('lastName', e.target.value)}
                      placeholder="Doe"
                      className="mt-1"
                      required
                    />
                  </div>
                  <div className="sm:col-span-2">
                    <Label htmlFor="phone">Phone Number (optional)</Label>
                    <Input
                      id="phone"
                      type="tel"
                      value={formData.phone}
                      onChange={(e) => handleInputChange('phone', e.target.value)}
                      placeholder="+1 (555) 123-4567"
                      className="mt-1"
                    />
                  </div>
                </div>
              </div>

              <div className="rounded-lg bg-white p-6 shadow-sm">
                <h2 className="mb-4 flex items-center gap-2 text-lg font-semibold">
                  <Truck className="h-5 w-5" />
                  Shipping Information
                </h2>
                <div className="grid gap-4 sm:grid-cols-2">
                  <div className="sm:col-span-2">
                    <Label htmlFor="address">Street Address</Label>
                    <Input
                      id="address"
                      value={formData.shipping_address.streetAddress1}
                      onChange={(e) => handleInputChange('shipping_address.streetAddress1', e.target.value)}
                      placeholder="123 Main Street"
                      className="mt-1"
                      required
                    />
                  </div>
                  <div className="sm:col-span-2">
                    <Label htmlFor="address2">Apartment, suite, etc. (optional)</Label>
                    <Input
                      id="address2"
                      value={formData.shipping_address.streetAddress2}
                      onChange={(e) => handleInputChange('shipping_address.streetAddress2', e.target.value)}
                      placeholder="Apt 4B"
                      className="mt-1"
                    />
                  </div>
                  <div>
                    <Label htmlFor="city">City</Label>
                    <Input
                      id="city"
                      value={formData.shipping_address.city}
                      onChange={(e) => handleInputChange('shipping_address.city', e.target.value)}
                      placeholder="New York"
                      className="mt-1"
                      required
                    />
                  </div>
                  <div>
                    <Label htmlFor="state">State</Label>
                    <Input
                      id="state"
                      value={formData.shipping_address.state}
                      onChange={(e) => handleInputChange('shipping_address.state', e.target.value)}
                      placeholder="NY"
                      className="mt-1"
                      required
                    />
                  </div>
                  <div>
                    <Label htmlFor="zip">ZIP Code</Label>
                    <Input
                      id="zip"
                      value={formData.shipping_address.postalCode}
                      onChange={(e) => handleInputChange('shipping_address.postalCode', e.target.value)}
                      placeholder="10001"
                      className="mt-1"
                      required
                    />
                  </div>
                  <div>
                    <Label htmlFor="country">Country</Label>
                    <Input
                      id="country"
                      value={formData.shipping_address.country}
                      onChange={(e) => handleInputChange('shipping_address.country', e.target.value)}
                      placeholder="US"
                      className="mt-1"
                      required
                    />
                  </div>
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
                          {imageUrl ? (
                            <img
                              src={imageUrl}
                              alt="Custom mug design"
                              className="h-full w-full object-cover"
                              onError={(e) => {
                                // Hide broken image and show placeholder
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
                            <ShoppingBag className="h-6 w-6 text-gray-400" />
                          </div>
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
