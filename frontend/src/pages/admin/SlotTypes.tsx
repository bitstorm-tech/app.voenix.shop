import { SortableSlotTypeList } from '@/components/admin/slot-types/SortableSlotTypeList';
import { Button } from '@/components/ui/Button';
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from '@/components/ui/Dialog';
import { slotTypesApi } from '@/lib/api';
import type { SlotType } from '@/types/slot';
import { useEffect, useState } from 'react';

export default function SlotTypes() {
  const [slotTypes, setSlotTypes] = useState<SlotType[]>([]);
  const [loading, setLoading] = useState(true);
  const [isDeleting, setIsDeleting] = useState(false);
  const [deleteId, setDeleteId] = useState<number | null>(null);
  const [isSaving, setIsSaving] = useState(false);

  useEffect(() => {
    fetchSlotTypes();
  }, []);

  const fetchSlotTypes = async () => {
    try {
      setLoading(true);
      const data = await slotTypesApi.getAll();
      // Sort by position
      const sortedData = data.sort((a, b) => a.position - b.position);
      setSlotTypes(sortedData);
    } catch (error) {
      console.error('Error fetching slot types:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleSlotTypesChange = async (updatedSlotTypes: SlotType[]) => {
    // Optimistically update the UI
    setSlotTypes(updatedSlotTypes);

    // Update all positions to match the new order (1-based)
    const positionUpdates = updatedSlotTypes.map((slotType, index) => ({
      id: slotType.id,
      position: index + 1,
    }));

    try {
      setIsSaving(true);
      await slotTypesApi.updatePositions(positionUpdates);
      // Refresh the list to ensure consistency
      await fetchSlotTypes();
    } catch (error) {
      console.error('Error updating slot type positions:', error);
      // Revert on error
      await fetchSlotTypes();
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
        await slotTypesApi.delete(deleteId);
        setIsDeleting(false);
        setDeleteId(null);
        fetchSlotTypes();
      } catch (error) {
        console.error('Error deleting slot type:', error);
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
        <h1 className="text-2xl font-bold">Slot Types</h1>
        <p className="mt-1 text-gray-600">Manage slot types and their display order</p>
      </div>

      {loading ? (
        <div className="flex h-64 items-center justify-center">
          <p className="text-gray-500">Loading...</p>
        </div>
      ) : (
        <>
          <SortableSlotTypeList slotTypes={slotTypes} onSlotTypesChange={handleSlotTypesChange} onDelete={handleDelete} />
          {isSaving && <div className="mt-2 text-sm text-gray-500">Saving position changes...</div>}
        </>
      )}

      <Dialog open={isDeleting} onOpenChange={setIsDeleting}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Confirm Deletion</DialogTitle>
            <DialogDescription>Are you sure you want to delete this slot type? This action cannot be undone.</DialogDescription>
          </DialogHeader>
          <DialogFooter>
            <Button variant="outline" onClick={cancelDelete}>
              Cancel
            </Button>
            <Button variant="destructive" onClick={confirmDelete}>
              Delete
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
