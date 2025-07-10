import { Button } from '@/components/ui/Button';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/Select';
import { PromptCategory } from '@/types/prompt';
import { Plus } from 'lucide-react';

interface PromptTableHeaderProps {
  categories: PromptCategory[];
  selectedCategory: string;
  onCategoryChange: (category: string) => void;
  onNewPrompt: () => void;
}

export default function PromptTableHeader({ categories, selectedCategory, onCategoryChange, onNewPrompt }: PromptTableHeaderProps) {
  return (
    <>
      <div className="mb-6 flex items-center justify-between">
        <h1 className="text-2xl font-bold">Prompts</h1>
        <Button onClick={onNewPrompt}>
          <Plus className="mr-2 h-4 w-4" />
          New Prompt
        </Button>
      </div>
      <div className="mb-4 flex items-center gap-4">
        <label className="text-sm font-medium">Filter by Category:</label>
        <Select value={selectedCategory} onValueChange={onCategoryChange}>
          <SelectTrigger className="w-[200px]">
            <SelectValue />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="all">All Categories</SelectItem>
            {categories.map((category) => (
              <SelectItem key={category.id} value={category.id.toString()}>
                {category.name}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>
    </>
  );
}
