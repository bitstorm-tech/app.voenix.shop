import { Table, TableBody, TableHead, TableHeader, TableRow } from '@/components/ui/Table';
import { Prompt } from '@/types/prompt';
import PromptTableRow from './PromptTableRow';

interface PromptTableProps {
  prompts: Prompt[];
  onEdit: (prompt: Prompt) => void;
  onDelete: (promptId: number) => void;
  onTest: (promptId: number) => void;
}

export default function PromptTable({ prompts, onEdit, onDelete, onTest }: PromptTableProps) {
  return (
    <div className="rounded-md border">
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>Example</TableHead>
            <TableHead>Active</TableHead>
            <TableHead>Name</TableHead>
            <TableHead>Category</TableHead>
            <TableHead>Prompt</TableHead>
            <TableHead>Actions</TableHead>
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
