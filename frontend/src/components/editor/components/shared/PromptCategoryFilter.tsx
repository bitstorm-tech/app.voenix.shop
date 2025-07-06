import { Button } from '@/components/ui/Button';
import { cn } from '@/lib/utils';

interface PromptCategoryFilterProps {
  categories: string[];
  selectedCategory: string;
  onCategoryChange: (category: string) => void;
}

export default function PromptCategoryFilter({ categories, selectedCategory, onCategoryChange }: PromptCategoryFilterProps) {
  return (
    <div className="flex flex-wrap gap-2">
      {categories.map((category) => (
        <Button
          key={category}
          variant={selectedCategory === category ? 'default' : 'outline'}
          size="sm"
          onClick={() => onCategoryChange(category)}
          className={cn('capitalize transition-all', selectedCategory === category && 'shadow-md')}
        >
          {category === 'all' ? 'All Styles' : category}
        </Button>
      ))}
    </div>
  );
}
