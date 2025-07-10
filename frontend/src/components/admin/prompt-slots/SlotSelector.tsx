import { Button } from '@/components/ui/Button';
import { Checkbox } from '@/components/ui/Checkbox';
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from '@/components/ui/Dialog';
import { Input } from '@/components/ui/Input';
import { Label } from '@/components/ui/Label';
import { slotsApi } from '@/lib/api';
import type { Slot } from '@/types/slot';
import { useEffect, useState } from 'react';

interface SlotSelectorProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  existingSlotIds: number[];
  existingSlotTypeIds: number[];
  onSelectSlots: (slots: Slot[]) => void;
}

export function SlotSelector({ open, onOpenChange, existingSlotIds, existingSlotTypeIds, onSelectSlots }: SlotSelectorProps) {
  const [availableSlots, setAvailableSlots] = useState<Slot[]>([]);
  const [selectedSlots, setSelectedSlots] = useState<Set<number>>(new Set());
  const [searchQuery, setSearchQuery] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (open) {
      fetchSlots();
    }
  }, [open]);

  const fetchSlots = async () => {
    try {
      setLoading(true);
      setError(null);
      const slots = await slotsApi.getAll();
      setAvailableSlots(slots);
    } catch (error) {
      console.error('Error fetching slots:', error);
      setError('Failed to load slots');
    } finally {
      setLoading(false);
    }
  };

  const handleSlotToggle = (slotId: number, slotTypeId: number) => {
    const newSelected = new Set(selectedSlots);

    if (newSelected.has(slotId)) {
      newSelected.delete(slotId);
    } else {
      // Check if a slot of the same type is already selected
      const slotsOfSameType = availableSlots.filter((s) => s.slotTypeId === slotTypeId && selectedSlots.has(s.id)).map((s) => s.id);

      // Remove any existing slots of the same type
      slotsOfSameType.forEach((id) => newSelected.delete(id));

      // Add the new slot
      newSelected.add(slotId);
    }

    setSelectedSlots(newSelected);
  };

  const handleConfirm = () => {
    const slotsToAdd = availableSlots.filter((slot) => selectedSlots.has(slot.id));
    onSelectSlots(slotsToAdd);
    setSelectedSlots(new Set());
    onOpenChange(false);
  };

  const filteredSlots = availableSlots.filter((slot) => {
    // Filter out already existing slots
    if (existingSlotIds.includes(slot.id)) return false;

    // Apply search filter
    if (searchQuery) {
      const query = searchQuery.toLowerCase();
      return (
        slot.name.toLowerCase().includes(query) || slot.prompt.toLowerCase().includes(query) || slot.slotType?.name.toLowerCase().includes(query)
      );
    }

    return true;
  });

  // Group slots by type
  const slotsByType = filteredSlots.reduce(
    (acc, slot) => {
      const typeName = slot.slotType?.name || 'Unknown';
      if (!acc[typeName]) {
        acc[typeName] = [];
      }
      acc[typeName].push(slot);
      return acc;
    },
    {} as Record<string, Slot[]>,
  );

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-h-[80vh] max-w-2xl overflow-hidden">
        <DialogHeader>
          <DialogTitle>Add Slots</DialogTitle>
          <DialogDescription>Select slots to add to your prompt. Only one slot per type is allowed.</DialogDescription>
        </DialogHeader>

        <div className="space-y-4 overflow-y-auto px-1">
          <div>
            <Label htmlFor="search">Search slots</Label>
            <Input
              id="search"
              placeholder="Search by name, prompt, or type..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="mt-1"
            />
          </div>

          {loading && <div className="py-8 text-center text-gray-500">Loading slots...</div>}

          {error && <div className="rounded border border-red-200 bg-red-50 px-4 py-3 text-red-700">{error}</div>}

          {!loading && !error && Object.keys(slotsByType).length === 0 && (
            <div className="py-8 text-center text-gray-500">No available slots found.</div>
          )}

          {!loading &&
            !error &&
            Object.entries(slotsByType).map(([typeName, slots]) => (
              <div key={typeName} className="space-y-2">
                <h3 className="font-medium text-gray-900">{typeName}</h3>
                <div className="space-y-2">
                  {slots.map((slot) => {
                    const isDisabled = existingSlotTypeIds.includes(slot.slotTypeId) && !selectedSlots.has(slot.id);
                    return (
                      <label
                        key={slot.id}
                        className={`flex cursor-pointer space-x-3 rounded-lg border p-3 hover:bg-gray-50 ${isDisabled ? 'opacity-50' : ''}`}
                      >
                        <Checkbox
                          checked={selectedSlots.has(slot.id)}
                          onCheckedChange={() => handleSlotToggle(slot.id, slot.slotTypeId)}
                          disabled={isDisabled}
                        />
                        <div className="flex-1">
                          <div className="font-medium">{slot.name}</div>
                          <div className="text-sm text-gray-600">{slot.prompt}</div>
                          {isDisabled && <div className="mt-1 text-xs text-orange-600">A slot of this type is already added</div>}
                        </div>
                      </label>
                    );
                  })}
                </div>
              </div>
            ))}
        </div>

        <DialogFooter>
          <Button type="button" variant="outline" onClick={() => onOpenChange(false)}>
            Cancel
          </Button>
          <Button type="button" onClick={handleConfirm} disabled={selectedSlots.size === 0}>
            Add {selectedSlots.size} Slot{selectedSlots.size !== 1 ? 's' : ''}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
