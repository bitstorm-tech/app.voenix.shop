import { SlotSelector } from '@/components/admin/prompt-slots/SlotSelector';
import { SortableSlotList } from '@/components/admin/prompt-slots/SortableSlotList';
import { Button } from '@/components/ui/Button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/Card';
import { Checkbox } from '@/components/ui/Checkbox';
import { Input } from '@/components/ui/Input';
import { Label } from '@/components/ui/Label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/Select';
import type { CreatePromptRequest, PromptSlotUpdate, UpdatePromptRequest } from '@/lib/api';
import { promptCategoriesApi, promptsApi } from '@/lib/api';
import type { PromptCategory, PromptSlot } from '@/types/prompt';
import type { Slot } from '@/types/slot';
import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';

export default function NewOrEditPrompt() {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const isEditing = !!id;

  const [formData, setFormData] = useState({
    title: '',
    categoryId: 0,
    active: true,
  });
  const [promptSlots, setPromptSlots] = useState<PromptSlot[]>([]);
  const [categories, setCategories] = useState<PromptCategory[]>([]);
  const [showSlotSelector, setShowSlotSelector] = useState(false);
  const [loading, setLoading] = useState(false);
  const [initialLoading, setInitialLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchCategories();
    if (isEditing) {
      fetchPrompt();
    } else {
      setInitialLoading(false);
    }
  }, [id]);

  const fetchCategories = async () => {
    try {
      const data = await promptCategoriesApi.getAll();
      setCategories(data);
    } catch (error) {
      console.error('Error fetching categories:', error);
      setError('Failed to load categories');
    }
  };

  const fetchPrompt = async () => {
    if (!id) return;

    try {
      setInitialLoading(true);
      const prompt = await promptsApi.getById(parseInt(id));
      setFormData({
        title: prompt.title || '',
        categoryId: prompt.categoryId || 0,
        active: prompt.active ?? true,
      });

      // Set prompt slots with proper PromptSlot type
      if (prompt.slots) {
        setPromptSlots(prompt.slots);
      }
    } catch (error) {
      console.error('Error fetching prompt:', error);
      setError('Failed to load prompt');
    } finally {
      setInitialLoading(false);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!formData.title.trim()) {
      setError('Title is required');
      return;
    }

    if (!formData.categoryId) {
      setError('Category is required');
      return;
    }

    if (promptSlots.length === 0) {
      setError('At least one slot is required');
      return;
    }

    try {
      setLoading(true);
      setError(null);

      const slots: PromptSlotUpdate[] = promptSlots.map((slot, index) => ({
        slotId: slot.id,
        position: index,
      }));

      if (isEditing) {
        const updateData: UpdatePromptRequest = {
          title: formData.title,
          categoryId: formData.categoryId,
          active: formData.active,
          slots,
        };
        await promptsApi.update(parseInt(id), updateData);
      } else {
        const createData: CreatePromptRequest = {
          title: formData.title,
          categoryId: formData.categoryId,
          active: formData.active,
          slots,
        };
        await promptsApi.create(createData);
      }

      navigate('/admin/prompts');
    } catch (error) {
      console.error('Error saving prompt:', error);
      setError('Failed to save prompt. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = () => {
    navigate('/admin/prompts');
  };

  const handleSlotsChange = (newSlots: PromptSlot[]) => {
    setPromptSlots(newSlots);
  };

  const handleAddSlots = (slotsToAdd: Slot[]) => {
    const newPromptSlots: PromptSlot[] = slotsToAdd.map((slot, index) => ({
      ...slot,
      position: promptSlots.length + index,
    }));
    setPromptSlots([...promptSlots, ...newPromptSlots]);
  };

  const existingSlotIds = promptSlots.map((s) => s.id);
  const existingSlotTypeIds = promptSlots.map((s) => s.slotTypeId);

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
          <CardTitle>{isEditing ? 'Edit Prompt' : 'New Prompt'}</CardTitle>
          <CardDescription>{isEditing ? 'Update the prompt details below' : 'Create a new prompt with the form below'}</CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-6">
            {error && <div className="rounded border border-red-200 bg-red-50 px-4 py-3 text-red-700">{error}</div>}

            <div className="space-y-2">
              <Label htmlFor="title">Title</Label>
              <Input
                id="title"
                value={formData.title}
                onChange={(e) => setFormData({ ...formData, title: e.target.value })}
                placeholder="Enter prompt title"
                required
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="category">Category</Label>
              <Select value={formData.categoryId.toString()} onValueChange={(value) => setFormData({ ...formData, categoryId: parseInt(value) })}>
                <SelectTrigger id="category">
                  <SelectValue placeholder="Select a category" />
                </SelectTrigger>
                <SelectContent>
                  {categories.map((category) => (
                    <SelectItem key={category.id} value={category.id.toString()}>
                      {category.name}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            <div className="space-y-2">
              <SortableSlotList slots={promptSlots} onSlotsChange={handleSlotsChange} onAddSlot={() => setShowSlotSelector(true)} />
            </div>

            <div className="space-y-2">
              <Label htmlFor="active">Status</Label>
              <div className="flex items-center space-x-2">
                <Checkbox
                  id="active"
                  checked={formData.active}
                  onCheckedChange={(checked) => setFormData({ ...formData, active: checked as boolean })}
                />
                <Label htmlFor="active" className="font-normal">
                  Make this prompt available for use
                </Label>
              </div>
            </div>

            <div className="flex gap-4">
              <Button type="submit" disabled={loading}>
                {loading ? 'Saving...' : isEditing ? 'Update Prompt' : 'Create Prompt'}
              </Button>
              <Button type="button" variant="outline" onClick={handleCancel}>
                Cancel
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>

      <SlotSelector
        open={showSlotSelector}
        onOpenChange={setShowSlotSelector}
        existingSlotIds={existingSlotIds}
        existingSlotTypeIds={existingSlotTypeIds}
        onSelectSlots={handleAddSlots}
      />
    </div>
  );
}
