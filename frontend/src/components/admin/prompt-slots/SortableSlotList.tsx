import { Button } from '@/components/ui/Button';
import { Card } from '@/components/ui/Card';
import type { PromptSlot } from '@/types/prompt';
import { DndContext, DragEndEvent, DragOverlay, DragStartEvent, PointerSensor, closestCenter, useSensor, useSensors } from '@dnd-kit/core';
import { SortableContext, arrayMove, verticalListSortingStrategy } from '@dnd-kit/sortable';
import { Plus } from 'lucide-react';
import { useState } from 'react';
import { SortableSlotItem } from './SortableSlotItem';

interface SortableSlotListProps {
  slots: PromptSlot[];
  onSlotsChange: (slots: PromptSlot[]) => void;
  onAddSlot: () => void;
}

export function SortableSlotList({ slots, onSlotsChange, onAddSlot }: SortableSlotListProps) {
  const [activeId, setActiveId] = useState<number | null>(null);

  const sensors = useSensors(
    useSensor(PointerSensor, {
      activationConstraint: {
        distance: 8,
      },
    }),
  );

  const handleDragStart = (event: DragStartEvent) => {
    setActiveId(event.active.id as number);
  };

  const handleDragEnd = (event: DragEndEvent) => {
    const { active, over } = event;

    if (over && active.id !== over.id) {
      const oldIndex = slots.findIndex((slot) => slot.id === active.id);
      const newIndex = slots.findIndex((slot) => slot.id === over.id);

      const newSlots = arrayMove(slots, oldIndex, newIndex);
      // Update positions based on new order
      const updatedSlots = newSlots.map((slot, index) => ({
        ...slot,
        position: index,
      }));

      onSlotsChange(updatedSlots);
    }

    setActiveId(null);
  };

  const handleRemoveSlot = (slotId: number) => {
    const newSlots = slots.filter((slot) => slot.id !== slotId);
    // Update positions after removal
    const updatedSlots = newSlots.map((slot, index) => ({
      ...slot,
      position: index,
    }));
    onSlotsChange(updatedSlots);
  };

  const activeSlot = activeId ? slots.find((slot) => slot.id === activeId) : null;

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h3 className="text-sm font-medium text-gray-700">Prompt Slots</h3>
        <Button type="button" variant="outline" size="sm" onClick={onAddSlot}>
          <Plus className="mr-2 h-4 w-4" />
          Add Slot
        </Button>
      </div>

      {slots.length === 0 ? (
        <Card className="border-dashed p-8 text-center">
          <p className="text-sm text-gray-500">No slots added yet. Click "Add Slot" to get started.</p>
        </Card>
      ) : (
        <DndContext sensors={sensors} collisionDetection={closestCenter} onDragStart={handleDragStart} onDragEnd={handleDragEnd}>
          <SortableContext items={slots.map((s) => s.id)} strategy={verticalListSortingStrategy}>
            <div className="space-y-2">
              {slots.map((slot) => (
                <SortableSlotItem key={slot.id} slot={slot} onRemove={handleRemoveSlot} />
              ))}
            </div>
          </SortableContext>
          <DragOverlay>
            {activeSlot ? (
              <div className="rounded-lg border bg-white p-4 shadow-lg">
                <div className="font-medium">{activeSlot.name}</div>
                <div className="text-sm text-gray-500">{activeSlot.slotType?.name}</div>
              </div>
            ) : null}
          </DragOverlay>
        </DndContext>
      )}
    </div>
  );
}
