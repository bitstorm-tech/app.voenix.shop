import { Button } from '@/components/ui/Button';
import { Card } from '@/components/ui/Card';
import type { PromptSlotType } from '@/types/promptSlotVariant';
import { DndContext, DragEndEvent, DragOverlay, DragStartEvent, PointerSensor, closestCenter, useSensor, useSensors } from '@dnd-kit/core';
import { SortableContext, arrayMove, verticalListSortingStrategy } from '@dnd-kit/sortable';
import { Plus } from 'lucide-react';
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { SortableSlotTypeItem } from './SortableSlotTypeItem';

interface SortableSlotTypeListProps {
  slotTypes: PromptSlotType[];
  onSlotTypesChange: (slotTypes: PromptSlotType[]) => void;
  onDelete: (id: number) => void;
}

export function SortableSlotTypeList({ slotTypes, onSlotTypesChange, onDelete }: SortableSlotTypeListProps) {
  const navigate = useNavigate();
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
      const oldIndex = slotTypes.findIndex((slotType) => slotType.id === active.id);
      const newIndex = slotTypes.findIndex((slotType) => slotType.id === over.id);

      const newSlotTypes = arrayMove(slotTypes, oldIndex, newIndex);
      // Update positions based on new order
      const updatedSlotTypes = newSlotTypes.map((slotType, index) => ({
        ...slotType,
        position: index,
      }));

      onSlotTypesChange(updatedSlotTypes);
    }

    setActiveId(null);
  };

  const handleEdit = (id: number) => {
    navigate(`/admin/prompt-slot-types/${id}/edit`);
  };

  const activeSlotType = activeId ? slotTypes.find((slotType) => slotType.id === activeId) : null;

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h3 className="text-sm font-medium text-gray-700">Drag to reorder slot types</h3>
        <Button onClick={() => navigate('/admin/prompt-slot-types/new')}>
          <Plus className="mr-2 h-4 w-4" />
          New Slot Type
        </Button>
      </div>

      {slotTypes.length === 0 ? (
        <Card className="border-dashed p-8 text-center">
          <p className="text-sm text-gray-500">No slot types found. Click &quot;New Slot Type&quot; to get started.</p>
        </Card>
      ) : (
        <DndContext sensors={sensors} collisionDetection={closestCenter} onDragStart={handleDragStart} onDragEnd={handleDragEnd}>
          <SortableContext items={slotTypes.map((s) => s.id)} strategy={verticalListSortingStrategy}>
            <div className="space-y-2">
              {slotTypes.map((slotType) => (
                <SortableSlotTypeItem key={slotType.id} slotType={slotType} onEdit={handleEdit} onDelete={onDelete} />
              ))}
            </div>
          </SortableContext>
          <DragOverlay>
            {activeSlotType ? (
              <div className="rounded-lg border bg-white p-4 shadow-lg">
                <div className="font-medium">{activeSlotType.name}</div>
              </div>
            ) : null}
          </DragOverlay>
        </DndContext>
      )}
    </div>
  );
}
