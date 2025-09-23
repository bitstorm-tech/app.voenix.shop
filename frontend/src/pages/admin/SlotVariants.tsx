import { Button } from '@/components/ui/Button';
import ConfirmationDialog from '@/components/ui/ConfirmationDialog';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/Table';
import { promptSlotVariantsApi } from '@/lib/api';
import type { PromptSlotVariant } from '@/types/promptSlotVariant';
import { Edit, Image, Plus, Trash2 } from 'lucide-react';
import { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useNavigate } from 'react-router-dom';

export default function SlotVariants() {
  const navigate = useNavigate();
  const [slotVariants, setSlotVariants] = useState<PromptSlotVariant[]>([]);
  const [loading, setLoading] = useState(true);
  const [isDeleting, setIsDeleting] = useState(false);
  const [deleteId, setDeleteId] = useState<number | null>(null);
  const [error, setError] = useState<'load' | null>(null);
  const { t, i18n } = useTranslation('adminSlotVariants');
  const locale = i18n.language || 'en';

  useEffect(() => {
    fetchSlotVariants();
  }, []);

  const fetchSlotVariants = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await promptSlotVariantsApi.getAll();
      setSlotVariants(data);
    } catch (error) {
      console.error('Error fetching slot variants:', error);
      setError('load');
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id: number) => {
    setDeleteId(id);
    setIsDeleting(true);
  };

  const confirmDelete = async () => {
    if (deleteId) {
      try {
        await promptSlotVariantsApi.delete(deleteId);
        setIsDeleting(false);
        setDeleteId(null);
        fetchSlotVariants();
      } catch (error) {
        console.error('Error deleting slot variant:', error);
        setIsDeleting(false);
        setDeleteId(null);
        window.alert(t('alerts.deleteError'));
      }
    }
  };

  const cancelDelete = () => {
    setIsDeleting(false);
    setDeleteId(null);
  };

  const truncatePrompt = (prompt: string, maxLength: number = 50) => {
    if (prompt.length <= maxLength) return prompt;
    return prompt.substring(0, maxLength) + '...';
  };

  if (loading) {
    return (
      <div className="container mx-auto p-6">
        <div className="flex h-64 items-center justify-center">
          <p className="text-gray-500">{t('page.loading')}</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="container mx-auto p-6">
        <div className="flex h-64 items-center justify-center">
          <div className="text-center">
            <p className="mb-4 text-red-500">{t('page.error.loadFailed')}</p>
            <Button onClick={fetchSlotVariants}>{t('page.retry')}</Button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="container mx-auto p-6">
      <div className="mb-6 flex items-center justify-between">
        <h1 className="text-2xl font-bold">{t('page.title')}</h1>
        <Button onClick={() => navigate('/admin/slot-variants/new')}>
          <Plus className="mr-2 h-4 w-4" />
          {t('page.new')}
        </Button>
      </div>

      <div className="rounded-md border">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>{t('table.headers.id')}</TableHead>
              <TableHead>{t('table.headers.name')}</TableHead>
              <TableHead>{t('table.headers.slotType')}</TableHead>
              <TableHead>{t('table.headers.llm')}</TableHead>
              <TableHead>{t('table.headers.prompt')}</TableHead>
              <TableHead>{t('table.headers.description')}</TableHead>
              <TableHead>{t('table.headers.example')}</TableHead>
              <TableHead>{t('table.headers.createdAt')}</TableHead>
              <TableHead className="text-right">{t('table.headers.actions')}</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {loading ? (
              <TableRow>
                <TableCell colSpan={9} className="text-center text-gray-500">
                  {t('table.loading')}
                </TableCell>
              </TableRow>
            ) : slotVariants.length === 0 ? (
              <TableRow>
                <TableCell colSpan={9} className="text-center text-gray-500">
                  {t('table.empty')}
                </TableCell>
              </TableRow>
            ) : (
              slotVariants.map((slot) => (
                <TableRow key={slot.id}>
                  <TableCell className="font-medium">{slot.id}</TableCell>
                  <TableCell>{slot.name}</TableCell>
                  <TableCell>{slot.promptSlotType?.name || '-'}</TableCell>
                  <TableCell>{slot.llm || '-'}</TableCell>
                  <TableCell className="max-w-xs">
                    <span className="text-sm text-gray-600" title={slot.prompt}>
                      {truncatePrompt(slot.prompt)}
                    </span>
                  </TableCell>
                  <TableCell className="max-w-xs">
                    <span className="text-sm text-gray-600" title={slot.description || ''}>
                      {slot.description ? truncatePrompt(slot.description, 30) : '-'}
                    </span>
                  </TableCell>
                  <TableCell>
                    {slot.exampleImageUrl ? (
                      <div className="flex items-center justify-center">
                        <Image className="h-4 w-4 text-green-600" />
                      </div>
                    ) : (
                      <div className="text-center text-gray-400">-</div>
                    )}
                  </TableCell>
                  <TableCell>{slot.createdAt ? new Date(slot.createdAt).toLocaleDateString(locale) : '-'}</TableCell>
                  <TableCell className="text-right">
                    <div className="flex justify-end gap-2">
                      <Button variant="outline" size="sm" onClick={() => navigate(`/admin/slot-variants/${slot.id}/edit`)}>
                        <Edit className="h-4 w-4" />
                      </Button>
                      <Button variant="outline" size="sm" onClick={() => handleDelete(slot.id)}>
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
        isOpen={isDeleting}
        onConfirm={confirmDelete}
        onCancel={cancelDelete}
        title={t('confirmation.title')}
        description={t('confirmation.description')}
        confirmText={t('confirmation.confirm')}
        cancelText={t('confirmation.cancel')}
      />
    </div>
  );
}
