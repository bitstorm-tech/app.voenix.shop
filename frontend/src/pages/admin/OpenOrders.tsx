import { Badge } from '@/components/ui/Badge';
import { Button } from '@/components/ui/Button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/Card';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/Table';
import { Clock, Eye, Package } from 'lucide-react';
import { useEffect, useState } from 'react';

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
  status: 'pending' | 'processing';
  created_at: string;
  updated_at: string;
}

export default function OpenOrders() {
  const [orders, setOrders] = useState<Order[]>([]);
  const [selectedOrder, setSelectedOrder] = useState<Order | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    // Mock data for now - replace with actual API call when orders endpoint is available
    const fetchOrders = async () => {
      setIsLoading(true);
      // Simulate API delay
      await new Promise(resolve => setTimeout(resolve, 500));
      
      // Mock open orders data
      const mockOrders: Order[] = [
        {
          id: 'ORD-004',
          customer_name: 'Alice Johnson',
          customer_email: 'alice@example.com',
          items: [
            { name: 'Custom Mug - Wedding Photo', quantity: 4, price: 19.99 }
          ],
          total: 79.96,
          status: 'processing',
          created_at: '2024-01-16T08:00:00Z',
          updated_at: '2024-01-16T09:30:00Z'
        },
        {
          id: 'ORD-005',
          customer_name: 'Mike Brown',
          customer_email: 'mike@example.com',
          items: [
            { name: 'Custom Mug - Sports Team', quantity: 2, price: 22.99 },
            { name: 'Custom Mug - Vacation Memory', quantity: 2, price: 19.99 }
          ],
          total: 85.96,
          status: 'pending',
          created_at: '2024-01-16T10:15:00Z',
          updated_at: '2024-01-16T10:15:00Z'
        },
        {
          id: 'ORD-006',
          customer_name: 'Sarah Davis',
          customer_email: 'sarah@example.com',
          items: [
            { name: 'Custom Mug - Baby Photo', quantity: 1, price: 24.99 }
          ],
          total: 24.99,
          status: 'pending',
          created_at: '2024-01-16T11:45:00Z',
          updated_at: '2024-01-16T11:45:00Z'
        }
      ];
      
      setOrders(mockOrders);
      setIsLoading(false);
    };

    fetchOrders();
  }, []);

  const getStatusBadge = (status: string) => {
    if (status === 'pending') {
      return <Badge variant="secondary">Pending</Badge>;
    }
    return <Badge className="bg-blue-100 text-blue-800">Processing</Badge>;
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      month: 'short',
      day: 'numeric',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  return (
    <div className="container mx-auto p-6">
      <div className="mb-6 flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold">Open Orders</h1>
          <p className="text-gray-600">Manage pending and processing orders</p>
        </div>
        <div className="flex items-center gap-2">
          <Package className="h-5 w-5 text-gray-500" />
          <span className="text-lg font-semibold">{orders.length} Open Orders</span>
        </div>
      </div>

      <div className="grid gap-6 lg:grid-cols-3">
        <div className="lg:col-span-2">
          <Card>
            <CardHeader>
              <CardTitle>Order List</CardTitle>
              <CardDescription>Click on an order to view details</CardDescription>
            </CardHeader>
            <CardContent>
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Order ID</TableHead>
                    <TableHead>Customer</TableHead>
                    <TableHead>Total</TableHead>
                    <TableHead>Status</TableHead>
                    <TableHead>Created</TableHead>
                    <TableHead className="text-right">Actions</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {isLoading ? (
                    <TableRow>
                      <TableCell colSpan={6} className="text-center text-gray-500">
                        Loading orders...
                      </TableCell>
                    </TableRow>
                  ) : orders.length === 0 ? (
                    <TableRow>
                      <TableCell colSpan={6} className="text-center text-gray-500">
                        No open orders
                      </TableCell>
                    </TableRow>
                  ) : (
                    orders.map((order) => (
                      <TableRow key={order.id} className="cursor-pointer hover:bg-gray-50" onClick={() => setSelectedOrder(order)}>
                        <TableCell className="font-medium">{order.id}</TableCell>
                        <TableCell>
                          <div>
                            <div className="font-medium">{order.customer_name}</div>
                            <div className="text-sm text-gray-500">{order.customer_email}</div>
                          </div>
                        </TableCell>
                        <TableCell>${order.total.toFixed(2)}</TableCell>
                        <TableCell>{getStatusBadge(order.status)}</TableCell>
                        <TableCell className="text-sm text-gray-500">{formatDate(order.created_at)}</TableCell>
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
            </CardContent>
          </Card>
        </div>

        <div className="lg:col-span-1">
          {selectedOrder ? (
            <Card>
              <CardHeader>
                <CardTitle>Order Details</CardTitle>
                <CardDescription>{selectedOrder.id}</CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div>
                  <h3 className="font-semibold">Customer Information</h3>
                  <p className="text-sm">{selectedOrder.customer_name}</p>
                  <p className="text-sm text-gray-500">{selectedOrder.customer_email}</p>
                </div>

                <div>
                  <h3 className="mb-2 font-semibold">Items</h3>
                  <div className="space-y-2">
                    {selectedOrder.items.map((item, index) => (
                      <div key={index} className="flex justify-between rounded bg-gray-50 p-2 text-sm">
                        <div>
                          <div className="font-medium">{item.name}</div>
                          <div className="text-gray-500">Qty: {item.quantity}</div>
                        </div>
                        <div className="text-right">
                          <div>${(item.price * item.quantity).toFixed(2)}</div>
                          <div className="text-xs text-gray-500">${item.price.toFixed(2)} each</div>
                        </div>
                      </div>
                    ))}
                  </div>
                </div>

                <div className="border-t pt-4">
                  <div className="flex justify-between font-semibold">
                    <span>Total</span>
                    <span>${selectedOrder.total.toFixed(2)}</span>
                  </div>
                </div>

                <div className="space-y-2 border-t pt-4">
                  <div className="flex items-center gap-2 text-sm">
                    <Clock className="h-4 w-4 text-gray-400" />
                    <span className="text-gray-600">Created: {formatDate(selectedOrder.created_at)}</span>
                  </div>
                  <div className="flex items-center gap-2 text-sm">
                    <Clock className="h-4 w-4 text-gray-400" />
                    <span className="text-gray-600">Updated: {formatDate(selectedOrder.updated_at)}</span>
                  </div>
                </div>

                <div className="flex gap-2 border-t pt-4">
                  <Button className="flex-1" variant="outline">
                    Mark as Completed
                  </Button>
                  <Button className="flex-1">Process Order</Button>
                </div>
              </CardContent>
            </Card>
          ) : (
            <Card>
              <CardHeader>
                <CardTitle>Order Details</CardTitle>
                <CardDescription>Select an order to view details</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="flex h-40 items-center justify-center text-gray-400">
                  <p>No order selected</p>
                </div>
              </CardContent>
            </Card>
          )}
        </div>
      </div>
    </div>
  );
}
