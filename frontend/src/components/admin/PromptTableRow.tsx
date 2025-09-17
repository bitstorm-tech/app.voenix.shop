import { Button } from '@/components/ui/Button';
import { TableCell, TableRow } from '@/components/ui/Table';
import { Prompt } from '@/types/prompt';
import { Edit, Play, Trash2 } from 'lucide-react';
import { useTranslation } from 'react-i18next';

interface PromptTableRowProps {
  prompt: Prompt;
  onEdit: (prompt: Prompt) => void;
  onDelete: (promptId: number) => void;
  onTest: (promptId: number) => void;
}

export default function PromptTableRow({ prompt, onEdit, onDelete, onTest }: PromptTableRowProps) {
  const { t } = useTranslation('adminPrompts');

  return (
    <TableRow>
      <TableCell>
        <span className={prompt.active ? 'text-green-600' : 'text-red-600'}>
          {prompt.active ? t('table.status.active') : t('table.status.inactive')}
        </span>
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
          <Button variant="outline" size="sm" onClick={() => onEdit(prompt)} aria-label={t('table.actions.edit', { title: prompt.title })}>
            <Edit className="h-4 w-4" />
          </Button>
          <Button variant="outline" size="sm" onClick={() => onTest(prompt.id)} aria-label={t('table.actions.test', { title: prompt.title })}>
            <Play className="h-4 w-4" />
          </Button>
          <Button variant="outline" size="sm" onClick={() => onDelete(prompt.id)} aria-label={t('table.actions.delete', { title: prompt.title })}>
            <Trash2 className="h-4 w-4" />
          </Button>
        </div>
      </TableCell>
    </TableRow>
  );
}
