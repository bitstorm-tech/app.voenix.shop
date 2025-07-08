import { Button } from '@/components/ui/Button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/Card';
import { Input } from '@/components/ui/Input';
import { Label } from '@/components/ui/Label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/Select';
import { Textarea } from '@/components/ui/Textarea';
import type { CreateSlotRequest, UpdateSlotRequest } from '@/lib/api';
import { slotsApi, slotTypesApi } from '@/lib/api';
import type { SlotType } from '@/types/slot';
import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';

export default function NewOrEditSlot() {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const isEditing = !!id;

  const [formData, setFormData] = useState<CreateSlotRequest>({
    name: '',
    slotTypeId: 0,
    prompt: '',
  });
  const [slotTypes, setSlotTypes] = useState<SlotType[]>([]);
  const [loading, setLoading] = useState(false);
  const [initialLoading, setInitialLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchSlotTypes();
    if (isEditing) {
      fetchSlot();
    } else {
      setInitialLoading(false);
    }
  }, [id]);

  const fetchSlotTypes = async () => {
    try {
      const data = await slotTypesApi.getAll();
      setSlotTypes(data);
    } catch (error) {
      console.error('Error fetching slot types:', error);
      setError('Failed to load slot types');
    }
  };

  const fetchSlot = async () => {
    if (!id) return;

    try {
      setInitialLoading(true);
      const slot = await slotsApi.getById(parseInt(id));
      setFormData({
        name: slot.name,
        slotTypeId: slot.slotTypeId,
        prompt: slot.prompt,
      });
    } catch (error) {
      console.error('Error fetching slot:', error);
      setError('Failed to load slot');
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

    if (!formData.slotTypeId) {
      setError('Slot type is required');
      return;
    }

    if (!formData.prompt.trim()) {
      setError('Prompt is required');
      return;
    }

    try {
      setLoading(true);
      setError(null);

      if (isEditing) {
        const updateData: UpdateSlotRequest = {
          name: formData.name,
          slotTypeId: formData.slotTypeId,
          prompt: formData.prompt,
        };
        await slotsApi.update(parseInt(id), updateData);
      } else {
        await slotsApi.create(formData);
      }

      navigate('/admin/slots');
    } catch (error) {
      console.error('Error saving slot:', error);
      setError('Failed to save slot. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = () => {
    navigate('/admin/slots');
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
          <CardTitle>{isEditing ? 'Edit Slot' : 'New Slot'}</CardTitle>
          <CardDescription>{isEditing ? 'Update the slot details below' : 'Create a new slot with the form below'}</CardDescription>
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
                placeholder="Enter slot name"
                required
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="slotType">Slot Type</Label>
              <Select value={formData.slotTypeId.toString()} onValueChange={(value) => setFormData({ ...formData, slotTypeId: parseInt(value) })}>
                <SelectTrigger id="slotType">
                  <SelectValue placeholder="Select a slot type" />
                </SelectTrigger>
                <SelectContent>
                  {slotTypes.map((type) => (
                    <SelectItem key={type.id} value={type.id.toString()}>
                      {type.name}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            <div className="space-y-2">
              <Label htmlFor="prompt">Prompt</Label>
              <Textarea
                id="prompt"
                value={formData.prompt}
                onChange={(e) => setFormData({ ...formData, prompt: e.target.value })}
                placeholder="Enter the prompt text"
                rows={6}
                required
              />
            </div>

            <div className="flex gap-4">
              <Button type="submit" disabled={loading}>
                {loading ? 'Saving...' : isEditing ? 'Update Slot' : 'Create Slot'}
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
