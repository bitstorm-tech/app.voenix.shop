import { Button } from '@/components/ui/Button';
import { Card } from '@/components/ui/Card';
import type { SlotType } from '@/types/slot';
import { useSortable } from '@dnd-kit/sortable';
import { CSS } from '@dnd-kit/utilities';
import { Edit, GripVertical, Trash2 } from 'lucide-react';

interface SortableSlotTypeItemProps {
  slotType: SlotType;
  onEdit: (id: number) => void;
  onDelete: (id: number) => void;
}

export function SortableSlotTypeItem({ slotType, onEdit, onDelete }: SortableSlotTypeItemProps) {
  const { attributes, listeners, setNodeRef, transform, transition, isDragging } = useSortable({
    id: slotType.id,
  });

  const style = {
    transform: CSS.Transform.toString(transform),
    transition,
    opacity: isDragging ? 0.5 : 1,
  };

  return (
    <Card ref={setNodeRef} style={style} className={`p-4 ${isDragging ? 'shadow-lg' : ''}`}>
      <div className="flex items-center gap-3">
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
              <h4 className="font-medium text-gray-900">{slotType.name}</h4>
              <p className="text-sm text-gray-500">Position: {slotType.position}</p>
            </div>
            <div className="flex items-center gap-2">
              <Button variant="ghost" size="sm" onClick={() => onEdit(slotType.id)} aria-label={`Edit ${slotType.name}`}>
                <Edit className="h-4 w-4" />
              </Button>
              <Button
                variant="ghost"
                size="sm"
                onClick={() => onDelete(slotType.id)}
                className="text-red-600 hover:text-red-700"
                aria-label={`Delete ${slotType.name}`}
              >
                <Trash2 className="h-4 w-4" />
              </Button>
            </div>
          </div>
          {slotType.createdAt && <p className="mt-1 text-xs text-gray-400">Created: {new Date(slotType.createdAt).toLocaleDateString()}</p>}
        </div>
      </div>
    </Card>
  );
}
