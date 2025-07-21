import { Accordion, AccordionContent, AccordionItem, AccordionTrigger } from '@/components/ui/Accordion';
import { Badge } from '@/components/ui/Badge';
import { Label } from '@/components/ui/Label';
import { slotsApi } from '@/lib/api';
import type { Slot, SlotType } from '@/types/slot';
import { useEffect, useState } from 'react';

interface SlotTypeSelectorProps {
  selectedSlotIds: number[];
  onSelectionChange: (slotIds: number[]) => void;
}

export function SlotTypeSelector({ selectedSlotIds, onSelectionChange }: SlotTypeSelectorProps) {
  const [slots, setSlots] = useState<Slot[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchSlots();
  }, []);

  const fetchSlots = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await slotsApi.getAll();
      setSlots(data);
    } catch (error) {
      console.error('Error fetching slots:', error);
      setError('Failed to load slots');
    } finally {
      setLoading(false);
    }
  };

  // Group slots by type
  const slotsByType = slots.reduce(
    (acc, slot) => {
      const typeId = slot.slotType?.id || 0;

      if (!acc[typeId]) {
        acc[typeId] = {
          type: slot.slotType || { id: 0, name: 'Other', position: 999 },
          slots: [],
        };
      }
      acc[typeId].slots.push(slot);
      return acc;
    },
    {} as Record<number, { type: SlotType; slots: Slot[] }>,
  );

  // Sort slot types by position
  const sortedSlotTypes = Object.values(slotsByType).sort((a, b) => a.type.position - b.type.position);

  const handleSlotChange = (slotTypeId: number, slotId: number, isChecked: boolean) => {
    // Get all currently selected slots except those of the same type
    const otherSelectedSlots = selectedSlotIds.filter((id) => {
      const slot = slots.find((s) => s.id === id);
      return slot && slot.slotTypeId !== slotTypeId;
    });

    // If checked, add the new selection; if unchecked, just keep other selections
    const newSelection = isChecked ? [...otherSelectedSlots, slotId] : otherSelectedSlots;
    onSelectionChange(newSelection);
  };

  if (loading) {
    return <div className="py-8 text-center text-gray-500">Loading slots...</div>;
  }

  if (error) {
    return <div className="rounded border border-red-200 bg-red-50 px-4 py-3 text-red-700">{error}</div>;
  }

  if (sortedSlotTypes.length === 0) {
    return <div className="rounded border border-gray-200 bg-gray-50 px-4 py-3 text-gray-600">No slots available. Please create slots first.</div>;
  }

  return (
    <div className="space-y-2">
      <div className="flex items-center justify-between">
        <Label>Prompt Slots</Label>
        <Badge variant="secondary">
          {selectedSlotIds.length} of {sortedSlotTypes.length} types selected
        </Badge>
      </div>

      <Accordion type="multiple" className="w-full">
        {sortedSlotTypes.map(({ type, slots: typeSlots }) => {
          const isSelected = selectedSlotIds.some((id) => slots.find((s) => s.id === id)?.slotTypeId === type.id);

          return (
            <AccordionItem key={type.id} value={`type-${type.id}`}>
              <AccordionTrigger className={isSelected ? 'font-semibold' : ''}>
                <div className="flex items-center gap-2">
                  <span>{type.name}</span>
                  {isSelected && (
                    <Badge variant="default" className="h-5 text-xs">
                      Selected
                    </Badge>
                  )}
                </div>
              </AccordionTrigger>
              <AccordionContent>
                <div className="space-y-3">
                  {typeSlots.map((slot) => (
                    <label key={slot.id} className="flex cursor-pointer space-x-3 rounded-lg border p-4 hover:bg-gray-50">
                      <input
                        type="checkbox"
                        checked={selectedSlotIds.includes(slot.id)}
                        onChange={(e) => handleSlotChange(type.id, slot.id, e.target.checked)}
                        className="mt-1 h-4 w-4 rounded border-gray-300 text-blue-600 focus:ring-blue-500"
                      />
                      <div className="flex-1 space-y-1">
                        <div className="font-medium">{slot.name}</div>
                        <div className="text-sm text-gray-600">{slot.prompt}</div>
                        {slot.description && <div className="text-sm text-gray-500">{slot.description}</div>}
                        {slot.exampleImageUrl && (
                          <img src={slot.exampleImageUrl} alt={`Example for ${slot.name}`} className="mt-2 h-20 w-20 rounded object-cover" />
                        )}
                      </div>
                    </label>
                  ))}
                </div>
              </AccordionContent>
            </AccordionItem>
          );
        })}
      </Accordion>
    </div>
  );
}
