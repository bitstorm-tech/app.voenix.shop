import { Badge } from '@/components/ui/Badge';
import { Button } from '@/components/ui/Button';
import ConfirmationDialog from '@/components/ui/ConfirmationDialog';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/Table';
import { useDeleteVat, useVats } from '@/hooks/queries/useVat';
import { Edit, Plus, Trash2 } from 'lucide-react';
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';

export default function Vat() {
  const navigate = useNavigate();
  const { data: vats = [], isLoading, error } = useVats();
  const deleteVatMutation = useDeleteVat();
  const { t, i18n } = useTranslation();

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
          <p className="text-gray-500">{t('loading')}</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="container mx-auto p-6">
        <div className="flex h-64 items-center justify-center">
          <div className="text-center">
            <p className="mb-4 text-red-500">{t('loadError')}</p>
            <button onClick={() => window.location.reload()} className="rounded bg-blue-500 px-4 py-2 text-white hover:bg-blue-600">
              {t('retry')}
            </button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="container mx-auto p-6">
      <div className="mb-6 flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold">{t('title')}</h1>
        </div>
        <div className="flex items-center gap-4">
          <label className="flex items-center gap-2 text-sm text-gray-600" htmlFor="vat-language">
            <span>{t('language.label')}</span>
            <select
              id="vat-language"
              aria-label={t('language.label')}
              className="rounded border px-2 py-1 text-sm"
              value={(i18n.resolvedLanguage ?? i18n.language ?? 'en').startsWith('de') ? 'de' : 'en'}
              onChange={(event) => i18n.changeLanguage(event.target.value)}
            >
              <option value="en">{t('language.en')}</option>
              <option value="de">{t('language.de')}</option>
            </select>
          </label>
          <Button onClick={handleNewVat}>
            <Plus className="mr-2 h-4 w-4" />
            {t('add')}
          </Button>
        </div>
      </div>

      <div className="rounded-md border">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>{t('table.name')}</TableHead>
              <TableHead>{t('table.percent')}</TableHead>
              <TableHead>{t('table.description')}</TableHead>
              <TableHead className="text-right">{t('table.actions')}</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {vats.length === 0 ? (
              <TableRow>
                <TableCell colSpan={4} className="text-center text-gray-500">
                  {t('table.empty')}
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
                          {t('badge.default')}
                        </Badge>
                      )}
                    </div>
                  </TableCell>
                  <TableCell>{t('percent', { value: vat.percent })}</TableCell>
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
        title={t('confirmation.title')}
        description={
          vatToDelete?.isDefault
            ? t('confirmation.defaultDescription')
            : t('confirmation.description')
        }
        confirmText={t('confirmation.confirm')}
        cancelText={t('confirmation.cancel')}
      />
    </div>
  );
}
