import { Button } from '@/components/ui/Button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/Card';
import { Input } from '@/components/ui/Input';
import { Label } from '@/components/ui/Label';
import type { CreateSlotTypeRequest, UpdateSlotTypeRequest } from '@/lib/api';
import { slotTypesApi } from '@/lib/api';
import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';

export default function NewOrEditSlotType() {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const isEditing = !!id;

  const [formData, setFormData] = useState<CreateSlotTypeRequest>({
    name: '',
  });
  const [loading, setLoading] = useState(false);
  const [initialLoading, setInitialLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (isEditing) {
      fetchSlotType();
    } else {
      setInitialLoading(false);
    }
  }, [id]);

  const fetchSlotType = async () => {
    if (!id) return;

    try {
      setInitialLoading(true);
      const slotType = await slotTypesApi.getById(parseInt(id));
      setFormData({
        name: slotType.name,
      });
    } catch (error) {
      console.error('Error fetching slot type:', error);
      setError('Failed to load slot type');
    } finally {
      setInitialLoading(false);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!formData.name.trim()) {
      setError('Name is required');
      return;
    }

    try {
      setLoading(true);
      setError(null);

      if (isEditing) {
        const updateData: UpdateSlotTypeRequest = {
          name: formData.name,
        };
        await slotTypesApi.update(parseInt(id), updateData);
      } else {
        await slotTypesApi.create(formData);
      }

      navigate('/admin/slot-types');
    } catch (error) {
      console.error('Error saving slot type:', error);
      setError('Failed to save slot type. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = () => {
    navigate('/admin/slot-types');
  };

  if (initialLoading) {
    return (
      <div className="container mx-auto p-6">
        <div className="flex h-64 items-center justify-center">
          <p className="text-gray-500">Loading...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="container mx-auto p-6">
      <Card className="mx-auto max-w-2xl">
        <CardHeader>
          <CardTitle>{isEditing ? 'Edit Slot Type' : 'New Slot Type'}</CardTitle>
          <CardDescription>{isEditing ? 'Update the slot type details below' : 'Create a new slot type with the form below'}</CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-6">
            {error && <div className="rounded border border-red-200 bg-red-50 px-4 py-3 text-red-700">{error}</div>}

            <div className="space-y-2">
              <Label htmlFor="name">Name</Label>
              <Input
                id="name"
                value={formData.name}
                onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                placeholder="Enter slot type name"
                required
              />
            </div>

            <div className="flex gap-4">
              <Button type="submit" disabled={loading}>
                {loading ? 'Saving...' : isEditing ? 'Update Slot Type' : 'Create Slot Type'}
              </Button>
              <Button type="button" variant="outline" onClick={handleCancel}>
                Cancel
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}
