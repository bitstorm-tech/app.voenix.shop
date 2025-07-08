import { Prompt } from '@/types/prompt';
import { useState } from 'react';

interface UsePromptSelectionReturn {
  selectedCategory: string;
  setSelectedCategory: (category: string) => void;
  filteredPrompts: Prompt[];
  categories: string[];
}

export function usePromptSelection(prompts: Prompt[]): UsePromptSelectionReturn {
  const [selectedCategory, setSelectedCategory] = useState<string>('all');

  const categories = (() => {
    const uniqueCategories = new Set<string>();
    prompts.forEach((prompt) => {
      // TODO: Add subcategory support when backend provides it
      if (prompt.category) {
        uniqueCategories.add(prompt.category.name);
      }
    });

    return ['all', ...Array.from(uniqueCategories).sort()];
  })();

  const filteredPrompts = (() => {
    if (selectedCategory === 'all') {
      return prompts;
    }
    return prompts.filter((prompt) => {
      // TODO: Add subcategory support when backend provides it
      const promptCategory = prompt.category?.name;
      return promptCategory === selectedCategory;
    });
  })();

  return {
    selectedCategory,
    setSelectedCategory,
    filteredPrompts,
    categories,
  };
}
