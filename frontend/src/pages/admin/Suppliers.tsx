import { Button } from '@/components/ui/Button';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/Table';
import { useDeleteSupplier, useSuppliers } from '@/hooks/queries/useSuppliers';
import { Edit, Plus, Trash2 } from 'lucide-react';
import { useNavigate } from 'react-router-dom';

export default function Suppliers() {
  const navigate = useNavigate();
  const { data: suppliers, isLoading } = useSuppliers();
  const deleteSupplierMutation = useDeleteSupplier();

  const handleDelete = (id: number) => {
    if (window.confirm('Are you sure you want to delete this supplier?')) {
      deleteSupplierMutation.mutate(id);
    }
  };

  const getContactName = (supplier: any) => {
    const parts = [supplier.title, supplier.firstName, supplier.lastName].filter(Boolean);
    return parts.length > 0 ? parts.join(' ') : '-';
  };

  const getPrimaryPhone = (supplier: any) => {
    return supplier.phoneNumber1 || supplier.phoneNumber2 || supplier.phoneNumber3 || '-';
  };

  if (isLoading) {
    return (
      <div className="container mx-auto p-6">
        <div className="flex h-64 items-center justify-center">
          <p className="text-gray-500">Loading suppliers...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="container mx-auto p-6">
      <div className="mb-6 flex items-center justify-between">
        <h1 className="text-2xl font-bold">Suppliers</h1>
        <Button onClick={() => navigate('/admin/suppliers/new')}>
          <Plus className="mr-2 h-4 w-4" />
          New Supplier
        </Button>
      </div>

      <div className="rounded-md border">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Name</TableHead>
              <TableHead>Contact</TableHead>
              <TableHead>Email</TableHead>
              <TableHead>Phone</TableHead>
              <TableHead>City</TableHead>
              <TableHead className="text-right">Actions</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {suppliers && suppliers.length > 0 ? (
              suppliers.map((supplier) => (
                <TableRow key={supplier.id}>
                  <TableCell className="font-medium">{supplier.name || '-'}</TableCell>
                  <TableCell>{getContactName(supplier)}</TableCell>
                  <TableCell>{supplier.email || '-'}</TableCell>
                  <TableCell>{getPrimaryPhone(supplier)}</TableCell>
                  <TableCell>{supplier.city || '-'}</TableCell>
                  <TableCell className="text-right">
                    <div className="flex justify-end gap-2">
                      <Button variant="outline" size="sm" onClick={() => navigate(`/admin/suppliers/${supplier.id}/edit`)}>
                        <Edit className="h-4 w-4" />
                      </Button>
                      <Button variant="outline" size="sm" onClick={() => handleDelete(supplier.id)} disabled={deleteSupplierMutation.isPending}>
                        <Trash2 className="h-4 w-4" />
                      </Button>
                    </div>
                  </TableCell>
                </TableRow>
              ))
            ) : (
              <TableRow>
                <TableCell colSpan={6} className="h-24 text-center">
                  <p className="text-gray-500">No suppliers found. Click "New Supplier" to add one.</p>
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </div>
    </div>
  );
}
