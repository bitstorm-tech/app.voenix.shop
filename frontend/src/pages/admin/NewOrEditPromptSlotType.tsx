import { Button } from '@/components/ui/Button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/Card';
import { Input } from '@/components/ui/Input';
import { Label } from '@/components/ui/Label';
import type { CreatePromptSlotTypeRequest, UpdatePromptSlotTypeRequest } from '@/lib/api';
import { promptSlotTypesApi } from '@/lib/api';
import { useCallback, useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';

export default function NewOrEditPromptSlotType() {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const isEditing = !!id;

  const [formData, setFormData] = useState<CreatePromptSlotTypeRequest>({
    name: '',
    position: 0,
  });
  const [loading, setLoading] = useState(false);
  const [initialLoading, setInitialLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [nextPosition, setNextPosition] = useState<number>(0);

  const fetchNextPosition = useCallback(async () => {
    try {
      setInitialLoading(true);
      const allPromptSlotTypes = await promptSlotTypesApi.getAll();
      // Find the maximum position and add 1
      const maxPosition = allPromptSlotTypes.length > 0 ? Math.max(...allPromptSlotTypes.map((st) => st.position)) : 0;
      setNextPosition(maxPosition + 1);
    } catch (error) {
      console.error('Error fetching prompt slot types:', error);
      // Default to 1 if error (1-based indexing)
      setNextPosition(1);
    } finally {
      setInitialLoading(false);
    }
  }, []);

  const fetchPromptSlotType = useCallback(async () => {
    if (!id) return;

    try {
      setInitialLoading(true);
      const promptSlotType = await promptSlotTypesApi.getById(parseInt(id));
      setFormData({
        name: promptSlotType.name,
        position: promptSlotType.position,
      });
    } catch (error) {
      console.error('Error fetching prompt slot type:', error);
      setError('Failed to load prompt slot type');
    } finally {
      setInitialLoading(false);
    }
  }, [id]);

  useEffect(() => {
    if (isEditing) {
      fetchPromptSlotType();
    } else {
      // For new prompt slot types, fetch all to determine next position
      fetchNextPosition();
    }
  }, [fetchPromptSlotType, fetchNextPosition, isEditing]);

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
        const updateData: UpdatePromptSlotTypeRequest = {
          name: formData.name,
        };
        await promptSlotTypesApi.update(parseInt(id), updateData);
      } else {
        const createData: CreatePromptSlotTypeRequest = {
          name: formData.name,
          position: nextPosition,
        };
        await promptSlotTypesApi.create(createData);
      }

      navigate('/admin/prompt-slot-types');
    } catch (error) {
      console.error('Error saving prompt slot type:', error);
      setError('Failed to save prompt slot type. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = () => {
    navigate('/admin/prompt-slot-types');
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
