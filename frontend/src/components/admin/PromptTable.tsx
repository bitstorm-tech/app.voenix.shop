import { Table, TableBody, TableHead, TableHeader, TableRow } from '@/components/ui/Table';
import { Prompt } from '@/types/prompt';
import { useTranslation } from 'react-i18next';
import PromptTableRow from './PromptTableRow';

interface PromptTableProps {
  prompts: Prompt[];
  onEdit: (prompt: Prompt) => void;
  onDelete: (promptId: number) => void;
  onTest: (promptId: number) => void;
}

export default function PromptTable({ prompts, onEdit, onDelete, onTest }: PromptTableProps) {
  const { t } = useTranslation('adminPrompts');

  return (
    <div className="rounded-md border">
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>{t('table.headers.active')}</TableHead>
            <TableHead>{t('table.headers.name')}</TableHead>
            <TableHead>{t('table.headers.category')}</TableHead>
            <TableHead>{t('table.headers.prompt')}</TableHead>
            <TableHead className="text-right">{t('table.headers.actions')}</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {prompts.map((prompt) => (
            <PromptTableRow key={prompt.id} prompt={prompt} onEdit={onEdit} onDelete={onDelete} onTest={onTest} />
          ))}
        </TableBody>
      </Table>
    </div>
  );
}
