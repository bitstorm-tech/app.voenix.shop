import { Badge } from '@/components/ui/Badge';
import { Button } from '@/components/ui/Button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/Card';
import { Input } from '@/components/ui/Input';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/Table';
import { getLocaleCurrency } from '@/lib/locale';
import { CheckCircle2, Download, Eye, Package2, Search, Truck } from 'lucide-react';
import { useEffect, useMemo, useState } from 'react';
import { useTranslation } from 'react-i18next';

interface OrderItem {
  name: string;
  quantity: number;
  price: number;
}

interface Order {
  id: string;
  customer_name: string;
  customer_email: string;
  items: OrderItem[];
  total: number;
  status: 'completed' | 'shipped';
  created_at: string;
  updated_at: string;
  completed_at: string;
}

export default function CompletedOrders() {
  const [orders, setOrders] = useState<Order[]>([]);
  const [selectedOrder, setSelectedOrder] = useState<Order | null>(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const { t, i18n } = useTranslation('adminCompletedOrders');
  const { locale, currency } = getLocaleCurrency(i18n.language);
  const currencyFormatter = useMemo(() => new Intl.NumberFormat(locale, { style: 'currency', currency }), [locale, currency]);
  const currencySymbol = useMemo(
    () => currencyFormatter.formatToParts(0).find((part) => part.type === 'currency')?.value ?? currency,
    [currencyFormatter, currency],
  );

  useEffect(() => {
    // Mock data for now - replace with actual API call when orders endpoint is available
    const fetchOrders = async () => {
      setIsLoading(true);
      // Simulate API delay
      await new Promise((resolve) => setTimeout(resolve, 500));

      // Mock completed orders data
      const mockOrders: Order[] = [
        {
          id: 'ORD-001',
          customer_name: 'John Doe',
          customer_email: 'john@example.com',
          items: [
            { name: 'Custom Mug - Beach Sunset', quantity: 2, price: 19.99 },
            { name: 'Custom Mug - Mountain View', quantity: 1, price: 19.99 },
          ],
          total: 59.97,
          status: 'completed',
          created_at: '2024-01-15T10:30:00Z',
          updated_at: '2024-01-15T14:45:00Z',
          completed_at: '2024-01-15T14:45:00Z',
        },
        {
          id: 'ORD-002',
          customer_name: 'Jane Smith',
          customer_email: 'jane@example.com',
          items: [{ name: 'Custom Mug - Pet Portrait', quantity: 3, price: 24.99 }],
          total: 74.97,
          status: 'shipped',
          created_at: '2024-01-14T09:15:00Z',
          updated_at: '2024-01-14T16:30:00Z',
          completed_at: '2024-01-14T16:30:00Z',
        },
        {
          id: 'ORD-003',
          customer_name: 'Bob Wilson',
          customer_email: 'bob@example.com',
          items: [
            { name: 'Custom Mug - Family Photo', quantity: 1, price: 19.99 },
            { name: 'Custom Mug - Logo Design', quantity: 5, price: 22.99 },
          ],
          total: 134.94,
          status: 'completed',
          created_at: '2024-01-13T11:20:00Z',
          updated_at: '2024-01-13T15:00:00Z',
          completed_at: '2024-01-13T15:00:00Z',
        },
      ];

      setOrders(mockOrders);
      setIsLoading(false);
    };

    fetchOrders();
  }, []);

  const getStatusBadge = (status: string) => {
    if (status === 'completed') {
      return (
        <Badge className="bg-green-100 text-green-800">
          <CheckCircle2 className="mr-1 h-3 w-3" />
          {t('status.completed')}
        </Badge>
      );
    }
    return (
      <Badge className="bg-purple-100 text-purple-800">
        <Truck className="mr-1 h-3 w-3" />
        {t('status.shipped')}
      </Badge>
    );
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString(locale, {
      month: 'short',
      day: 'numeric',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const filteredOrders = orders.filter(
    (order) =>
      order.id.toLowerCase().includes(searchTerm.toLowerCase()) ||
      order.customer_name.toLowerCase().includes(searchTerm.toLowerCase()) ||
      order.customer_email.toLowerCase().includes(searchTerm.toLowerCase()),
  );

  const totalRevenue = orders.reduce((sum, order) => sum + order.total, 0);
  const formatCurrency = (value: number) => currencyFormatter.format(value);

  return (
    <div className="container mx-auto p-6">
      <div className="mb-6">
        <h1 className="text-3xl font-bold">{t('page.title')}</h1>
        <p className="text-gray-600">{t('page.subtitle')}</p>
      </div>

      <div className="mb-6 grid gap-4 md:grid-cols-3">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">{t('cards.totalOrders.title')}</CardTitle>
            <Package2 className="text-muted-foreground h-4 w-4" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{orders.length}</div>
            <p className="text-muted-foreground text-xs">{t('cards.totalOrders.description')}</p>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">{t('cards.totalRevenue.title')}</CardTitle>
            <span className="text-muted-foreground text-sm">{currencySymbol}</span>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{formatCurrency(totalRevenue)}</div>
            <p className="text-muted-foreground text-xs">{t('cards.totalRevenue.description')}</p>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">{t('cards.averageOrder.title')}</CardTitle>
            <span className="text-muted-foreground text-sm">$</span>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{formatCurrency(orders.length > 0 ? totalRevenue / orders.length : 0)}</div>
            <p className="text-muted-foreground text-xs">{t('cards.averageOrder.description')}</p>
          </CardContent>
        </Card>
      </div>

      <div className="grid gap-6 lg:grid-cols-3">
        <div className="lg:col-span-2">
          <Card>
            <CardHeader>
              <div className="flex items-center justify-between">
                <div>
                  <CardTitle>{t('tableCard.title')}</CardTitle>
                  <CardDescription>{t('tableCard.description')}</CardDescription>
                </div>
                <Button variant="outline" size="sm">
                  <Download className="mr-2 h-4 w-4" />
                  {t('page.export')}
                </Button>
              </div>
            </CardHeader>
            <CardContent>
              <div className="mb-4 flex items-center gap-2">
                <Search className="h-4 w-4 text-gray-400" />
                <Input
                  type="text"
                  placeholder={t('page.searchPlaceholder')}
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  className="max-w-sm"
                />
              </div>
              <div className="overflow-x-auto">
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>{t('table.headers.id')}</TableHead>
                      <TableHead>{t('table.headers.customer')}</TableHead>
                      <TableHead>{t('table.headers.total')}</TableHead>
                      <TableHead>{t('table.headers.status')}</TableHead>
                      <TableHead>{t('table.headers.completed')}</TableHead>
                      <TableHead className="text-right">{t('table.headers.actions')}</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {isLoading ? (
                      <TableRow>
                        <TableCell colSpan={6} className="text-center text-gray-500">
                          {t('page.loading')}
                        </TableCell>
                      </TableRow>
                    ) : filteredOrders.length === 0 ? (
                      <TableRow>
                        <TableCell colSpan={6} className="text-center text-gray-500">
                          {t('page.empty')}
                        </TableCell>
                      </TableRow>
                    ) : (
                      filteredOrders.map((order) => (
                        <TableRow key={order.id} className="cursor-pointer hover:bg-gray-50" onClick={() => setSelectedOrder(order)}>
                          <TableCell className="font-medium">{order.id}</TableCell>
                          <TableCell>
                            <div>
                              <div className="font-medium">{order.customer_name}</div>
                              <div className="text-sm text-gray-500">{t('table.customer.email', { email: order.customer_email })}</div>
                            </div>
                          </TableCell>
                          <TableCell>{formatCurrency(order.total)}</TableCell>
                          <TableCell>{getStatusBadge(order.status)}</TableCell>
                          <TableCell className="text-sm text-gray-500">{formatDate(order.completed_at)}</TableCell>
                          <TableCell className="text-right">
                            <Button
                              variant="ghost"
                              size="sm"
                              onClick={(e) => {
                                e.stopPropagation();
                                setSelectedOrder(order);
                              }}
                            >
                              <Eye className="h-4 w-4" />
                            </Button>
                          </TableCell>
                        </TableRow>
                      ))
                    )}
                  </TableBody>
                </Table>
              </div>
            </CardContent>
          </Card>
        </div>

        <div className="lg:col-span-1">
          {selectedOrder ? (
            <Card>
              <CardHeader>
                <CardTitle>{t('details.title')}</CardTitle>
                <CardDescription>{selectedOrder.id}</CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div>
                  <h3 className="font-semibold">{t('details.customer')}</h3>
                  <p className="text-sm">{selectedOrder.customer_name}</p>
                  <p className="text-sm text-gray-500">{t('table.customer.email', { email: selectedOrder.customer_email })}</p>
                </div>

                <div>
                  <h3 className="mb-2 font-semibold">{t('details.items')}</h3>
                  <div className="space-y-2">
                    {selectedOrder.items.map((item, index) => (
                      <div key={index} className="flex justify-between rounded bg-gray-50 p-2 text-sm">
                        <div>
                          <div className="font-medium">{item.name}</div>
                          <div className="text-gray-500">{t('items.quantity', { count: item.quantity })}</div>
                        </div>
                        <div className="text-right">
                          <div>{formatCurrency(item.price * item.quantity)}</div>
                          <div className="text-xs text-gray-500">{t('items.priceEach', { price: formatCurrency(item.price) })}</div>
                        </div>
                      </div>
                    ))}
                  </div>
                </div>

                <div className="border-t pt-4">
                  <div className="flex justify-between font-semibold">
                    <span>{t('details.total')}</span>
                    <span>{formatCurrency(selectedOrder.total)}</span>
                  </div>
                </div>

                <div className="space-y-2 border-t pt-4">
                  <div className="text-sm">
                    <span className="text-gray-600">{t('details.created')}: </span>
                    <span>{formatDate(selectedOrder.created_at)}</span>
                  </div>
                  <div className="text-sm">
                    <span className="text-gray-600">{t('details.completed')}: </span>
                    <span>{formatDate(selectedOrder.completed_at)}</span>
                  </div>
                  <div className="mt-2">{getStatusBadge(selectedOrder.status)}</div>
                </div>

                <div className="flex gap-2 border-t pt-4">
                  <Button className="flex-1" variant="outline">
                    {t('page.viewInvoice')}
                  </Button>
                  <Button className="flex-1" variant="outline">
                    {t('page.resendReceipt')}
                  </Button>
                </div>
              </CardContent>
            </Card>
          ) : (
            <Card>
              <CardHeader>
                <CardTitle>{t('details.title')}</CardTitle>
                <CardDescription>{t('page.selectionDescription')}</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="flex h-40 items-center justify-center text-gray-400">
                  <p>{t('page.noSelection')}</p>
                </div>
              </CardContent>
            </Card>
          )}
        </div>
      </div>
    </div>
  );
}
