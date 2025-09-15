import { usePublicPrompts } from '@/hooks/queries/usePublicPrompts';
import { cn } from '@/lib/utils';
import { useWizardStore } from '@/stores/editor/useWizardStore';
import { Check } from 'lucide-react';
import { usePromptSelection } from '../../hooks/usePromptSelection';
import PromptCategoryFilter from '../shared/PromptCategoryFilter';

export default function PromptSelectionStep() {
  const { data: prompts = [] } = usePublicPrompts();
  const selectedPrompt = useWizardStore((state) => state.selectedPrompt);
  const selectPrompt = useWizardStore((state) => state.selectPrompt);
  const { selectedCategory, setSelectedCategory, filteredPrompts, categories } = usePromptSelection(prompts);

  return (
    <div className="space-y-6">
      <div>
        <h3 className="mb-2 text-lg font-semibold">Choose Your Style</h3>
        <p className="text-sm text-gray-600">Select a style that will transform your image into something magical</p>
      </div>

      <PromptCategoryFilter categories={categories} selectedCategory={selectedCategory} onCategoryChange={setSelectedCategory} />

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {filteredPrompts.map((prompt) => {
          const isSelected = selectedPrompt?.id === prompt.id;

          return (
            <div
              key={prompt.id}
              onClick={() => selectPrompt(prompt)}
              className={cn(
                'group relative cursor-pointer overflow-hidden rounded-lg border-2 transition-all duration-300',
                isSelected ? 'border-primary shadow-md' : 'border-gray-200 hover:-translate-y-1 hover:border-gray-300 hover:shadow-xl',
              )}
            >
              {isSelected && (
                <div className="absolute top-2 right-2 z-10">
                  <div className="bg-primary flex h-6 w-6 items-center justify-center rounded-full text-white">
                    <Check className="h-4 w-4" />
                  </div>
                </div>
              )}

              {prompt.exampleImageUrl ? (
                <div className="relative">
                  <img
                    src={prompt.exampleImageUrl}
                    alt={prompt.title}
                    className="h-48 w-full object-cover transition-transform duration-300 group-hover:scale-110"
                    loading="lazy"
                  />
                  {typeof prompt.price === 'number' && prompt.price > 0 && (
                    <div className="absolute top-2 left-2 z-10">
                      <span className="bg-primary/90 text-primary-foreground rounded-full px-2 py-1 text-xs font-semibold shadow">
                        ${(prompt.price / 100).toFixed(2)}
                      </span>
                    </div>
                  )}
                  <div className="absolute inset-x-0 bottom-0 bg-gradient-to-t from-black/70 to-transparent p-3 transition-all duration-300 group-hover:from-black/80">
                    <h4 className="font-medium text-white">{prompt.title}</h4>
                  </div>
                  <div className="absolute inset-0 bg-black/0 transition-colors duration-300 group-hover:bg-black/10" />
                </div>
              ) : (
                <div className="flex h-48 items-center justify-center bg-gray-100 p-4 transition-colors duration-300 group-hover:bg-gray-200">
                  <div className="text-center">
                    <h4 className="mb-2 font-medium">{prompt.title}</h4>
                    <p className="text-xs text-gray-500">No preview available</p>
                    {typeof prompt.price === 'number' && prompt.price > 0 && (
                      <div className="mt-2">
                        <span className="bg-primary/10 text-primary rounded-full px-2 py-1 text-xs font-semibold">
                          ${(prompt.price / 100).toFixed(2)}
                        </span>
                      </div>
                    )}
                  </div>
                </div>
              )}
            </div>
          );
        })}
      </div>

      {filteredPrompts.length === 0 && (
        <div className="py-12 text-center">
          <p className="text-gray-500">No styles found in this category</p>
        </div>
      )}
    </div>
  );
}
