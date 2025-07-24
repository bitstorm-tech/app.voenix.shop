import { Card } from '@/components/ui/Card';
import ConfirmationDialog from '@/components/ui/ConfirmationDialog';
import { useDeleteVat, useVats } from '@/hooks/queries/useVat';
import { Pencil, Plus, Trash2 } from 'lucide-react';
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';

export default function Vat() {
  const navigate = useNavigate();
  const { data: vats = [], isLoading, error } = useVats();
  const deleteVatMutation = useDeleteVat();

  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState<boolean>(false);
  const [vatToDelete, setVatToDelete] = useState<number | undefined>(undefined);

  const handleEdit = (vatId: number) => {
    navigate(`/admin/vat/${vatId}/edit`);
  };

  const handleDelete = (vatId: number) => {
    setVatToDelete(vatId);
    setIsDeleteDialogOpen(true);
  };

  const confirmDelete = async () => {
    if (!vatToDelete) return;

    deleteVatMutation.mutate(vatToDelete, {
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
        <button onClick={handleNewVat} className="flex items-center gap-2 rounded-md bg-blue-600 px-4 py-2 text-white hover:bg-blue-700">
          <Plus className="h-4 w-4" />
          Add New VAT
        </button>
      </div>

      <Card>
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead className="border-b">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium tracking-wider text-gray-500 uppercase">Name</th>
                <th className="px-6 py-3 text-left text-xs font-medium tracking-wider text-gray-500 uppercase">Percent</th>
                <th className="px-6 py-3 text-left text-xs font-medium tracking-wider text-gray-500 uppercase">Description</th>
                <th className="px-6 py-3 text-left text-xs font-medium tracking-wider text-gray-500 uppercase">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y">
              {vats.length === 0 ? (
                <tr>
                  <td colSpan={4} className="px-6 py-4 text-center text-gray-500">
                    No VAT entries found
                  </td>
                </tr>
              ) : (
                vats.map((vat) => (
                  <tr key={vat.id} className="hover:bg-gray-50">
                    <td className="px-6 py-4 text-sm font-medium text-gray-900">{vat.name}</td>
                    <td className="px-6 py-4 text-sm text-gray-500">{vat.percent}%</td>
                    <td className="px-6 py-4 text-sm text-gray-500">{vat.description || '-'}</td>
                    <td className="px-6 py-4 text-sm">
                      <div className="flex gap-2">
                        <button onClick={() => handleEdit(vat.id)} className="rounded p-1 text-blue-600 hover:bg-blue-100" title="Edit VAT">
                          <Pencil className="h-4 w-4" />
                        </button>
                        <button onClick={() => handleDelete(vat.id)} className="rounded p-1 text-red-600 hover:bg-red-100" title="Delete VAT">
                          <Trash2 className="h-4 w-4" />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </Card>

      <ConfirmationDialog
        isOpen={isDeleteDialogOpen}
        onConfirm={confirmDelete}
        onCancel={cancelDelete}
        description="This will permanently delete the VAT entry. This action cannot be undone."
      />
    </div>
  );
}
