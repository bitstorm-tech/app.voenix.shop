import { Button } from '@/components/ui/Button';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/Table';
import { useDeleteSupplier, useSuppliers } from '@/hooks/queries/useSuppliers';
import type { Supplier } from '@/types/supplier';
import { Edit, Plus, Trash2 } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { useNavigate } from 'react-router-dom';

export default function Suppliers() {
  const navigate = useNavigate();
  const { data: suppliers, isLoading } = useSuppliers();
  const deleteSupplierMutation = useDeleteSupplier();
  const { t } = useTranslation('adminSuppliers');

  const handleDelete = (id: number) => {
    if (window.confirm(t('confirmDelete'))) {
      deleteSupplierMutation.mutate(id);
    }
  };

  const getContactName = (supplier: Pick<Supplier, 'title' | 'firstName' | 'lastName'>) => {
    const parts = [supplier.title, supplier.firstName, supplier.lastName].filter(Boolean);
    return parts.length > 0 ? parts.join(' ') : '-';
  };

  const getPrimaryPhone = (supplier: Pick<Supplier, 'phoneNumber1' | 'phoneNumber2' | 'phoneNumber3'>) => {
    return supplier.phoneNumber1 || supplier.phoneNumber2 || supplier.phoneNumber3 || '-';
  };

  if (isLoading) {
    return (
      <div className="container mx-auto p-6">
        <div className="flex h-64 items-center justify-center">
          <p className="text-gray-500">{t('loading')}</p>
        </div>
      </div>
    );
  }

  return (
    <div className="container mx-auto p-6">
      <div className="mb-6 flex items-center justify-between">
        <h1 className="text-2xl font-bold">{t('title')}</h1>
        <Button onClick={() => navigate('/admin/suppliers/new')}>
          <Plus className="mr-2 h-4 w-4" />
          {t('buttons.new')}
        </Button>
      </div>

      <div className="rounded-md border">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>{t('table.headers.name')}</TableHead>
              <TableHead>{t('table.headers.contact')}</TableHead>
              <TableHead>{t('table.headers.email')}</TableHead>
              <TableHead>{t('table.headers.phone')}</TableHead>
              <TableHead>{t('table.headers.city')}</TableHead>
              <TableHead className="text-right">{t('table.headers.actions')}</TableHead>
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
                  <p className="text-gray-500">{t('table.empty', { action: t('buttons.new') })}</p>
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </div>
    </div>
  );
}
