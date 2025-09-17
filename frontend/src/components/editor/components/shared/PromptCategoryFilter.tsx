import { Button } from '@/components/ui/Button';
import { cn } from '@/lib/utils';
import { useTranslation } from 'react-i18next';

interface PromptCategoryFilterProps {
  categories: string[];
  selectedCategory: string;
  onCategoryChange: (category: string) => void;
}

export default function PromptCategoryFilter({ categories, selectedCategory, onCategoryChange }: PromptCategoryFilterProps) {
  const { t } = useTranslation('editor');
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
          {category === 'all' ? t('categoryFilter.all') : category}
        </Button>
      ))}
    </div>
  );
}
