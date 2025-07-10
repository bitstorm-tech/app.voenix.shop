import { Button } from '@/components/ui/Button';
import { Card } from '@/components/ui/Card';
import type { PromptSlot } from '@/types/prompt';
import { useSortable } from '@dnd-kit/sortable';
import { CSS } from '@dnd-kit/utilities';
import { GripVertical, X } from 'lucide-react';

interface SortableSlotItemProps {
  slot: PromptSlot;
  onRemove: (slotId: number) => void;
}

export function SortableSlotItem({ slot, onRemove }: SortableSlotItemProps) {
  const { attributes, listeners, setNodeRef, transform, transition, isDragging } = useSortable({
    id: slot.id,
  });

  const style = {
    transform: CSS.Transform.toString(transform),
    transition,
    opacity: isDragging ? 0.5 : 1,
  };

  return (
    <Card ref={setNodeRef} style={style} className={`p-4 ${isDragging ? 'shadow-lg' : ''}`}>
      <div className="flex items-start gap-3">
        <button
          className="cursor-grab touch-none text-gray-400 hover:text-gray-600 focus:outline-none"
          {...attributes}
          {...listeners}
          type="button"
          aria-label="Drag to reorder"
        >
          <GripVertical className="h-5 w-5" />
        </button>

        <div className="flex-1">
          <div className="flex items-center justify-between">
            <div>
              <h4 className="font-medium text-gray-900">{slot.name}</h4>
              <p className="text-sm text-gray-500">{slot.slotType?.name}</p>
            </div>
            <Button
              type="button"
              variant="ghost"
              size="sm"
              onClick={() => onRemove(slot.id)}
              className="text-red-600 hover:text-red-700"
              aria-label={`Remove ${slot.name}`}
            >
              <X className="h-4 w-4" />
            </Button>
          </div>
          {slot.prompt && (
            <p className="mt-2 text-sm text-gray-600" title={slot.prompt}>
              {slot.prompt.length > 100 ? `${slot.prompt.substring(0, 100)}...` : slot.prompt}
            </p>
          )}
        </div>
      </div>
    </Card>
  );
}
