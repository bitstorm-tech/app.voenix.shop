import SlotTypeForm from '@/components/admin/SlotTypeForm';
import { slotTypesApi } from '@/lib/api';
import { SlotType } from '@/types/slot';
import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';

export default function NewOrEditSlotType() {
  const { id } = useParams<{ id: string }>();
  const isEditMode = !!id;
  const [slotType, setSlotType] = useState<SlotType | undefined>(undefined);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (isEditMode && id) {
      const fetchSlotType = async () => {
        setLoading(true);
        try {
          const data = await slotTypesApi.getById(Number(id));
          setSlotType(data);
        } catch (err) {
          console.error('Failed to fetch slot type:', err);
          setError('Failed to load slot type');
        } finally {
          setLoading(false);
        }
      };

      fetchSlotType();
    }
  }, [id, isEditMode]);

  if (loading) {
    return (
      <div className="container mx-auto p-6">
        <div className="mb-6">
          <h1 className="text-2xl font-bold">{isEditMode ? 'Edit Slot Type' : 'New Slot Type'}</h1>
          <p className="text-gray-600">Loading...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="container mx-auto p-6">
        <div className="mb-6">
          <h1 className="text-2xl font-bold">{isEditMode ? 'Edit Slot Type' : 'New Slot Type'}</h1>
          <p className="text-red-600">{error}</p>
        </div>
      </div>
    );
  }

  return (
    <div className="container mx-auto p-6">
      <div className="mb-6">
        <h1 className="text-2xl font-bold">{isEditMode ? 'Edit Slot Type' : 'New Slot Type'}</h1>
        <p className="text-gray-600">
          {isEditMode ? 'Update the slot type details.' : 'Create a new slot type for your collection.'}
        </p>
      </div>

      <div className="mx-auto max-w-4xl">
        <SlotTypeForm slotType={slotType} />
      </div>
    </div>
  );
}