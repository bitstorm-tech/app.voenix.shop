import { Button } from '@/components/ui/Button';
import { TableCell, TableRow } from '@/components/ui/Table';
import { Prompt } from '@/types/prompt';
import { Edit, Play, Trash2 } from 'lucide-react';

interface PromptTableRowProps {
  prompt: Prompt;
  onEdit: (prompt: Prompt) => void;
  onDelete: (promptId: number) => void;
  onTest: (promptId: number) => void;
}

export default function PromptTableRow({ prompt, onEdit, onDelete, onTest }: PromptTableRowProps) {
  return (
    <TableRow>
      <TableCell>
        {prompt.exampleImageUrl ? (
          <img src={prompt.exampleImageUrl} alt={`Example for ${prompt.title}`} className="h-16 w-16 rounded object-cover" />
        ) : (
          <span className="text-sm text-gray-500">-</span>
        )}
      </TableCell>
      <TableCell>
        <span className={prompt.active ? 'text-green-600' : 'text-red-600'}>{prompt.active ? 'Yes' : 'No'}</span>
      </TableCell>
      <TableCell>
        <span>{prompt.title}</span>
      </TableCell>
      <TableCell>
        <span>{prompt.category?.name || '-'}</span>
      </TableCell>
      <TableCell>
        <span className="block max-w-md truncate" title={prompt.promptText || ''}>
          {prompt.promptText || '-'}
        </span>
      </TableCell>
      <TableCell className="text-right">
        <div className="flex justify-end gap-2">
          <Button variant="outline" size="sm" onClick={() => onEdit(prompt)}>
            <Edit className="h-4 w-4" />
          </Button>
          <Button variant="outline" size="sm" onClick={() => onTest(prompt.id)}>
            <Play className="h-4 w-4" />
          </Button>
          <Button variant="outline" size="sm" onClick={() => onDelete(prompt.id)}>
            <Trash2 className="h-4 w-4" />
          </Button>
        </div>
      </TableCell>
    </TableRow>
  );
}
