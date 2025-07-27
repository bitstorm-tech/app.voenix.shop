import { Badge } from '@/components/ui/Badge';
import { Button } from '@/components/ui/Button';
import ConfirmationDialog from '@/components/ui/ConfirmationDialog';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/Table';
import { useDeleteVat, useVats } from '@/hooks/queries/useVat';
import { Edit, Plus, Trash2 } from 'lucide-react';
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';

export default function Vat() {
  const navigate = useNavigate();
  const { data: vats = [], isLoading, error } = useVats();
  const deleteVatMutation = useDeleteVat();

  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState<boolean>(false);
  const [vatToDelete, setVatToDelete] = useState<{ id: number; isDefault: boolean } | undefined>(undefined);

  const handleEdit = (vatId: number) => {
    navigate(`/admin/vat/${vatId}/edit`);
  };

  const handleDelete = (vatId: number, isDefault: boolean) => {
    setVatToDelete({ id: vatId, isDefault });
    setIsDeleteDialogOpen(true);
  };

  const confirmDelete = async () => {
    if (!vatToDelete) return;

    deleteVatMutation.mutate(vatToDelete.id, {
      onSuccess: () => {
        setIsDeleteDialogOpen(false);
        setVatToDelete(undefined);
      },
    });
  };

  const cancelDelete = () => {
    setIsDeleteDialogOpen(false);
    setVatToDelete(undefined);
  };

  const handleNewVat = () => {
    navigate('/admin/vat/new');
  };

  if (isLoading) {
    return (
      <div className="container mx-auto p-6">
        <div className="flex h-64 items-center justify-center">
          <p className="text-gray-500">Loading VAT entries...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="container mx-auto p-6">
        <div className="flex h-64 items-center justify-center">
          <div className="text-center">
            <p className="mb-4 text-red-500">Failed to load VAT entries. Please try again.</p>
            <button onClick={() => window.location.reload()} className="rounded bg-blue-500 px-4 py-2 text-white hover:bg-blue-600">
              Retry
            </button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="container mx-auto p-6">
      <div className="mb-6 flex items-center justify-between">
        <h1 className="text-2xl font-bold">VAT Management</h1>
        <Button onClick={handleNewVat}>
          <Plus className="mr-2 h-4 w-4" />
          Add New VAT
        </Button>
      </div>

      <div className="rounded-md border">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Name</TableHead>
              <TableHead>Percent</TableHead>
              <TableHead>Description</TableHead>
              <TableHead className="text-right">Actions</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {vats.length === 0 ? (
              <TableRow>
                <TableCell colSpan={4} className="text-center text-gray-500">
                  No VAT entries found
                </TableCell>
              </TableRow>
            ) : (
              vats.map((vat) => (
                <TableRow key={vat.id}>
                  <TableCell className="font-medium">
                    <div className="flex items-center gap-2">
                      {vat.name}
                      {vat.isDefault && (
                        <Badge variant="default" className="text-xs">
                          Default
                        </Badge>
                      )}
                    </div>
                  </TableCell>
                  <TableCell>{vat.percent}%</TableCell>
                  <TableCell>{vat.description || '-'}</TableCell>
                  <TableCell className="text-right">
                    <div className="flex justify-end gap-2">
                      <Button variant="outline" size="sm" onClick={() => handleEdit(vat.id)}>
                        <Edit className="h-4 w-4" />
                      </Button>
                      <Button variant="outline" size="sm" onClick={() => handleDelete(vat.id, vat.isDefault)}>
                        <Trash2 className="h-4 w-4" />
                      </Button>
                    </div>
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </div>

      <ConfirmationDialog
        isOpen={isDeleteDialogOpen}
        onConfirm={confirmDelete}
        onCancel={cancelDelete}
        description={
          vatToDelete?.isDefault
            ? 'This is the default VAT. After deletion, you will need to set a new default VAT. This action cannot be undone.'
            : 'This will permanently delete the VAT entry. This action cannot be undone.'
        }
      />
    </div>
  );
}
