import { SortableSlotTypeList } from '@/components/admin/slot-types/SortableSlotTypeList';
import ConfirmationDialog from '@/components/ui/ConfirmationDialog';
import { promptSlotTypesApi } from '@/lib/api';
import type { PromptSlotType } from '@/types/promptSlotVariant';
import { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';

export default function PromptSlotTypes() {
  const [promptSlotTypes, setPromptSlotTypes] = useState<PromptSlotType[]>([]);
  const [loading, setLoading] = useState(true);
  const [isDeleting, setIsDeleting] = useState(false);
  const [deleteId, setDeleteId] = useState<number | null>(null);
  const [isSaving, setIsSaving] = useState(false);
  const { t } = useTranslation('adminPromptSlotTypes');

  useEffect(() => {
    fetchPromptSlotTypes();
  }, []);

  const fetchPromptSlotTypes = async () => {
    try {
      setLoading(true);
      const data = await promptSlotTypesApi.getAll();
      // Sort by position
      const sortedData = data.sort((a, b) => a.position - b.position);
      setPromptSlotTypes(sortedData);
    } catch (error) {
      console.error('Error fetching prompt slot types:', error);
    } finally {
      setLoading(false);
    }
  };

  const handlePromptSlotTypesChange = async (updatedPromptSlotTypes: PromptSlotType[]) => {
    // Optimistically update the UI
    setPromptSlotTypes(updatedPromptSlotTypes);

    // Update all positions to match the new order (1-based)
    const positionUpdates = updatedPromptSlotTypes.map((promptSlotType, index) => ({
      id: promptSlotType.id,
      position: index + 1,
    }));

    try {
      setIsSaving(true);
      await promptSlotTypesApi.updatePositions(positionUpdates);
      // Refresh the list to ensure consistency
      await fetchPromptSlotTypes();
    } catch (error) {
      console.error('Error updating prompt slot type positions:', error);
      // Revert on error
      await fetchPromptSlotTypes();
    } finally {
      setIsSaving(false);
    }
  };

  const handleDelete = async (id: number) => {
    setDeleteId(id);
    setIsDeleting(true);
  };

  const confirmDelete = async () => {
    if (deleteId) {
      try {
        await promptSlotTypesApi.delete(deleteId);
        setIsDeleting(false);
        setDeleteId(null);
        fetchPromptSlotTypes();
      } catch (error) {
        console.error('Error deleting prompt slot type:', error);
        setIsDeleting(false);
        setDeleteId(null);
      }
    }
  };

  const cancelDelete = () => {
    setIsDeleting(false);
    setDeleteId(null);
  };

  return (
    <div className="container mx-auto p-6">
      <div className="mb-6">
        <h1 className="text-2xl font-bold">{t('page.title')}</h1>
        <p className="mt-1 text-gray-600">{t('page.subtitle')}</p>
      </div>

      {loading ? (
        <div className="flex h-64 items-center justify-center">
          <p className="text-gray-500">{t('page.loading')}</p>
        </div>
      ) : (
        <>
          <SortableSlotTypeList slotTypes={promptSlotTypes} onSlotTypesChange={handlePromptSlotTypesChange} onDelete={handleDelete} />
          {isSaving && <div className="mt-2 text-sm text-gray-500">{t('page.saving')}</div>}
        </>
      )}

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
