import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import { slotTypesApi, type CreateSlotTypeRequest, type UpdateSlotTypeRequest } from '@/lib/api';
import { SlotType } from '@/types/slot';
import { useNavigate } from 'react-router-dom';
import { useState } from 'react';

interface SlotTypeFormProps {
  slotType?: SlotType | null;
  onSuccess?: () => void;
  onCancel?: () => void;
}

export default function SlotTypeForm({ slotType, onSuccess, onCancel }: SlotTypeFormProps) {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    name: slotType?.name || '',
  });
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrors({});

    if (!formData.name) {
      return;
    }

    setIsSubmitting(true);

    try {
      if (slotType) {
        const updateData: UpdateSlotTypeRequest = {
          name: formData.name,
        };
        await slotTypesApi.update(slotType.id, updateData);
      } else {
        const createData: CreateSlotTypeRequest = {
          name: formData.name,
        };
        await slotTypesApi.create(createData);
      }

      if (onSuccess) {
        onSuccess();
      } else {
        navigate('/admin/slot-types');
      }
    } catch (error: any) {
      console.error('Form submission error:', error);
      if (error.status === 400 && error.message) {
        setErrors({ general: error.message });
      } else {
        setErrors({ general: 'An error occurred while saving the slot type' });
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleCancel = () => {
    if (onCancel) {
      onCancel();
    } else {
      navigate('/admin/slot-types');
    }
  };

  const isFormValid = formData.name;

  return (
    <form onSubmit={handleSubmit} className="space-y-6">
      {errors.general && (
        <div className="bg-red-50 border border-red-200 text-red-600 px-4 py-2 rounded">
          {errors.general}
        </div>
      )}

      <div className="grid gap-4">
        <div className="grid grid-cols-1 items-start gap-2 sm:grid-cols-4 sm:items-center sm:gap-4">
          <label htmlFor="name" className="text-sm font-medium sm:text-right">
            Name
          </label>
          <div className="sm:col-span-3">
            <Input
              id="name"
              type="text"
              value={formData.name}
              onChange={(e) => setFormData({ ...formData, name: e.target.value })}
              placeholder="Enter slot type name"
              required
            />
            {errors.name && <p className="mt-1 text-sm text-red-500">{errors.name}</p>}
          </div>
        </div>
      </div>

      <div className="flex justify-end gap-2">
        <Button type="button" variant="outline" onClick={handleCancel}>
          Cancel
        </Button>
        <Button type="submit" disabled={!isFormValid || isSubmitting}>
          {isSubmitting ? 'Saving...' : slotType ? 'Update' : 'Create'}
        </Button>
      </div>
    </form>
  );
}