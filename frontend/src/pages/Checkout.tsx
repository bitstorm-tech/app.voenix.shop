import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import { Label } from '@/components/ui/Label';
import { CreditCard, Lock, ShoppingBag, Truck } from 'lucide-react';
import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';

// Removed CheckoutPageProps as we're not using Inertia.js props

interface CartItem {
  id: number;
  mug: {
    id: number;
    name: string;
    image: string;
  };
  generated_image_path: string;
  quantity: number;
  price: number;
  subtotal: number;
}

interface Cart {
  items: CartItem[];
  subtotal: number;
  total_items: number;
}

interface FormData {
  email: string;
  first_name: string;
  last_name: string;
  phone: string;
  shipping_address: {
    line1: string;
    line2: string;
    city: string;
    state: string;
    postal_code: string;
    country: string;
  };
  sameAsBilling: boolean;
}

export default function CheckoutPage() {
  const navigate = useNavigate();
  const [cart, setCart] = useState<Cart | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isProcessing, setIsProcessing] = useState(false);
  const [formData, setFormData] = useState<FormData>({
    email: '',
    first_name: '',
    last_name: '',
    phone: '',
    shipping_address: {
      line1: '',
      line2: '',
      city: '',
      state: '',
      postal_code: '',
      country: 'US',
    },
    sameAsBilling: true,
  });

  const fetchCart = async () => {
    try {
      // Simulate API call with dummy data
      await new Promise((resolve) => setTimeout(resolve, 500));

      const dummyCart: Cart = {
        items: [
          {
            id: 1,
            mug: {
              id: 1,
              name: 'Classic White Mug',
              image: '/placeholder-mug.jpg',
            },
            generated_image_path: 'dummy-image-1.png',
            quantity: 2,
            price: 15.99,
            subtotal: 31.98,
          },
          {
            id: 2,
            mug: {
              id: 2,
              name: 'Premium Black Mug',
              image: '/placeholder-mug-black.jpg',
            },
            generated_image_path: 'dummy-image-2.png',
            quantity: 1,
            price: 19.99,
            subtotal: 19.99,
          },
        ],
        subtotal: 51.97,
        total_items: 3,
      };

      setCart(dummyCart);
    } catch (error) {
      console.error('Error fetching cart:', error);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchCart();
  }, []);

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

  const generateOrderReceiptHtml = (order: { order_number: string; total: number }, cart: Cart) => {
    const date = new Date().toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    });

    const itemsHtml = cart.items
      .map(
        (item) => `
        <tr>
          <td style="padding: 8px; border-bottom: 1px solid #eee;">
            <strong>${item.mug.name}</strong><br>
            <small style="color: #666;">Custom Design</small>
          </td>
          <td style="padding: 8px; border-bottom: 1px solid #eee; text-align: center;">${item.quantity}</td>
          <td style="padding: 8px; border-bottom: 1px solid #eee; text-align: right;">$${item.price.toFixed(2)}</td>
          <td style="padding: 8px; border-bottom: 1px solid #eee; text-align: right;">$${item.subtotal.toFixed(2)}</td>
        </tr>
      `,
      )
      .join('');

    const shipping = 4.99;
    const tax = cart.subtotal * 0.08;
    const total = cart.subtotal + shipping + tax;

    return `
      <div style="font-family: Arial, sans-serif; font-size: 14px; color: #333;">
        <div style="text-align: center; margin-bottom: 20px;">
          <h2 style="margin: 0; font-size: 24px;">ORDER RECEIPT</h2>
          <p style="margin: 5px 0; color: #666;">Order #${order.order_number}</p>
          <p style="margin: 5px 0; color: #666;">${date}</p>
        </div>
        
        <div style="margin-bottom: 20px;">
          <p style="margin: 5px 0;"><strong>${formData.first_name} ${formData.last_name}</strong></p>
          <p style="margin: 5px 0;">${formData.email}</p>
        </div>
        
        <table style="width: 100%; border-collapse: collapse; margin-bottom: 20px;">
          <thead>
            <tr style="background-color: #f5f5f5;">
              <th style="padding: 8px; text-align: left;">Item</th>
              <th style="padding: 8px; text-align: center; width: 60px;">Qty</th>
              <th style="padding: 8px; text-align: right; width: 80px;">Price</th>
              <th style="padding: 8px; text-align: right; width: 80px;">Total</th>
            </tr>
          </thead>
          <tbody>
            ${itemsHtml}
          </tbody>
        </table>
        
        <div style="text-align: right; margin-top: 20px;">
          <div style="margin-bottom: 5px;">
            <span>Subtotal:</span>
            <span style="display: inline-block; width: 100px; text-align: right;">$${cart.subtotal.toFixed(2)}</span>
          </div>
          <div style="margin-bottom: 5px;">
            <span>Shipping:</span>
            <span style="display: inline-block; width: 100px; text-align: right;">$${shipping.toFixed(2)}</span>
          </div>
          <div style="margin-bottom: 5px;">
            <span>Tax:</span>
            <span style="display: inline-block; width: 100px; text-align: right;">$${tax.toFixed(2)}</span>
          </div>
          <div style="border-top: 2px solid #333; padding-top: 5px; margin-top: 10px; font-weight: bold; font-size: 16px;">
            <span>Total:</span>
            <span style="display: inline-block; width: 100px; text-align: right;">$${total.toFixed(2)}</span>
          </div>
        </div>
        
        <div style="text-align: center; margin-top: 30px; font-size: 12px; color: #666;">
          <p>Thank you for your order!</p>
        </div>
      </div>
    `;
  };

  const downloadOrderPdf = async (order: { id: number; order_number: string; total: number }) => {
    if (!cart) return;

    try {
      const receiptHtml = generateOrderReceiptHtml(order, cart);

      // Simulate PDF download by creating an HTML file instead
      const blob = new Blob([receiptHtml], { type: 'text/html' });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `receipt-${order.order_number}.html`;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);

      console.log('Receipt downloaded (HTML format for demo)');
    } catch (error) {
      console.error('Error generating receipt:', error);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!cart || cart.items.length === 0) {
      alert('Your cart is empty');
      return;
    }

    setIsProcessing(true);

    try {
      const shipping = 4.99;
      const tax = cart.subtotal * 0.08;

      // Simulate API call with dummy data
      await new Promise((resolve) => setTimeout(resolve, 1500));

      // Create dummy order
      const dummyOrder = {
        id: Math.floor(Math.random() * 1000) + 1,
        order_number: `ORD-${Date.now()}`,
        total: cart.subtotal + shipping + tax,
        email: formData.email,
        first_name: formData.first_name,
        last_name: formData.last_name,
        phone: formData.phone || null,
        shipping_address: formData.shipping_address,
        billing_address: formData.sameAsBilling ? null : formData.shipping_address,
        shipping: shipping,
        tax: tax,
      };

      console.log('Order created:', dummyOrder);

      await downloadOrderPdf({
        id: dummyOrder.id,
        order_number: dummyOrder.order_number,
        total: dummyOrder.total,
      });

      // Navigate to home page after successful order
      navigate('/');
    } catch (error) {
      console.error('Error creating order:', error);
      alert('Failed to create order. Please try again.');
    } finally {
      setIsProcessing(false);
    }
  };

  if (isLoading) {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <div className="text-center">
          <div className="inline-block h-8 w-8 animate-spin rounded-full border-4 border-solid border-current border-r-transparent"></div>
          <p className="mt-2 text-sm text-gray-600">Loading checkout...</p>
        </div>
      </div>
    );
  }

  if (!cart || cart.items.length === 0) {
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

  const shipping = 4.99;
  const tax = cart.subtotal * 0.08;
  const total = cart.subtotal + shipping + tax;

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
                      value={formData.first_name}
                      onChange={(e) => handleInputChange('first_name', e.target.value)}
                      placeholder="John"
                      className="mt-1"
                      required
                    />
                  </div>
                  <div>
                    <Label htmlFor="lastName">Last Name</Label>
                    <Input
                      id="lastName"
                      value={formData.last_name}
                      onChange={(e) => handleInputChange('last_name', e.target.value)}
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
                      value={formData.shipping_address.line1}
                      onChange={(e) => handleInputChange('shipping_address.line1', e.target.value)}
                      placeholder="123 Main Street"
                      className="mt-1"
                      required
                    />
                  </div>
                  <div className="sm:col-span-2">
                    <Label htmlFor="address2">Apartment, suite, etc. (optional)</Label>
                    <Input
                      id="address2"
                      value={formData.shipping_address.line2}
                      onChange={(e) => handleInputChange('shipping_address.line2', e.target.value)}
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
                      value={formData.shipping_address.postal_code}
                      onChange={(e) => handleInputChange('shipping_address.postal_code', e.target.value)}
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

                <div className="space-y-3">
                  {cart.items.map((item) => (
                    <div key={item.id} className="flex gap-3">
                      <div className="relative h-16 w-16 overflow-hidden rounded-lg bg-gray-100">
                        <img
                          src={`https://via.placeholder.com/150?text=Mug+${item.id}`}
                          alt="Custom mug design"
                          className="h-full w-full object-cover"
                        />
                      </div>
                      <div className="flex-1">
                        <h3 className="text-sm font-medium">{item.mug.name}</h3>
                        <p className="text-sm text-gray-600">Qty: {item.quantity}</p>
                      </div>
                      <div className="text-sm font-medium">${item.subtotal.toFixed(2)}</div>
                    </div>
                  ))}
                </div>

                <div className="mt-4 space-y-2 border-t pt-4">
                  <div className="flex justify-between text-sm">
                    <span>Subtotal ({cart.total_items} items)</span>
                    <span>${cart.subtotal.toFixed(2)}</span>
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

              <Button type="submit" className="w-full gap-2" size="lg" disabled={isProcessing}>
                <Lock className="h-4 w-4" />
                {isProcessing ? 'Processing...' : `Buy Now • $${total.toFixed(2)}`}
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
