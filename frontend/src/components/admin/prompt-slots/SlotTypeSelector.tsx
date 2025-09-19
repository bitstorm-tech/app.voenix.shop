import { Accordion, AccordionContent, AccordionItem, AccordionTrigger } from '@/components/ui/Accordion';
import { Badge } from '@/components/ui/Badge';
import { Label } from '@/components/ui/Label';
import { promptSlotVariantsApi } from '@/lib/api';
import type { PromptSlotType, PromptSlotVariant } from '@/types/promptSlotVariant';
import { useCallback, useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';

interface SlotTypeSelectorProps {
  selectedSlotIds: number[];
  onSelectionChange: (slotIds: number[]) => void;
  llmFilter?: string;
}

export function SlotTypeSelector({ selectedSlotIds, onSelectionChange, llmFilter }: SlotTypeSelectorProps) {
  const { t } = useTranslation('admin');
  const [slotVariants, setSlotVariants] = useState<PromptSlotVariant[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchSlotVariants = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await promptSlotVariantsApi.getAll();
      setSlotVariants(data);
    } catch (error) {
      console.error('Error fetching slot variants:', error);
      setError(t('prompt.slotSelector.errors.load'));
    } finally {
      setLoading(false);
    }
  }, [t]);

  useEffect(() => {
    fetchSlotVariants();
  }, [fetchSlotVariants]);

  const filteredSlotVariants = llmFilter ? slotVariants.filter((slot) => slot.llm === llmFilter) : slotVariants;

  useEffect(() => {
    if (loading || !llmFilter) {
      return;
    }

    const validSlotIds = new Set(filteredSlotVariants.map((slot) => slot.id));
    const filteredSelection = selectedSlotIds.filter((id) => validSlotIds.has(id));

    if (filteredSelection.length !== selectedSlotIds.length) {
      onSelectionChange(filteredSelection);
    }
  }, [filteredSlotVariants, llmFilter, loading, onSelectionChange, selectedSlotIds]);

  // Group slot variants by type
  const slotVariantsByType = filteredSlotVariants.reduce(
    (acc, slot) => {
      const typeId = slot.promptSlotType?.id || 0;

      if (!acc[typeId]) {
        acc[typeId] = {
          type: slot.promptSlotType || { id: 0, name: t('prompt.slotSelector.fallbackType'), position: 999 },
          slotVariants: [],
        };
      }
      acc[typeId].slotVariants.push(slot);
      return acc;
    },
    {} as Record<number, { type: PromptSlotType; slotVariants: PromptSlotVariant[] }>,
  );

  // Sort slot types by position
  const sortedSlotTypes = Object.values(slotVariantsByType).sort((a, b) => a.type.position - b.type.position);

  const handleSlotChange = (slotTypeId: number, slotId: number, isChecked: boolean) => {
    // Get all currently selected slot variants except those of the same type
    const otherSelectedSlots = selectedSlotIds.filter((id) => {
      const slot = slotVariants.find((s) => s.id === id);
      return slot && slot.promptSlotTypeId !== slotTypeId;
    });

    // If checked, add the new selection; if unchecked, just keep other selections
    const newSelection = isChecked ? [...otherSelectedSlots, slotId] : otherSelectedSlots;
    onSelectionChange(newSelection);
  };

  if (loading) {
    return <div className="py-8 text-center text-gray-500">{t('prompt.slotSelector.loading')}</div>;
  }

  if (error) {
    return <div className="rounded border border-red-200 bg-red-50 px-4 py-3 text-red-700">{error}</div>;
  }

  if (sortedSlotTypes.length === 0) {
    if (llmFilter) {
      return <div className="rounded border border-gray-200 bg-gray-50 px-4 py-3 text-gray-600">{t('prompt.slotSelector.filteredEmpty')}</div>;
    }

    return <div className="rounded border border-gray-200 bg-gray-50 px-4 py-3 text-gray-600">{t('prompt.slotSelector.empty')}</div>;
  }

  return (
    <div className="space-y-2">
      <div className="flex items-center justify-between">
        <Label>{t('prompt.slotSelector.label')}</Label>
        <Badge variant="secondary">{t('prompt.slotSelector.badge', { selected: selectedSlotIds.length, total: sortedSlotTypes.length })}</Badge>
      </div>

      <Accordion type="multiple" className="w-full">
        {sortedSlotTypes.map(({ type, slotVariants: typeSlotVariants }) => {
          const isSelected = selectedSlotIds.some((id) => slotVariants.find((s) => s.id === id)?.promptSlotTypeId === type.id);

          return (
            <AccordionItem key={type.id} value={`type-${type.id}`}>
              <AccordionTrigger className={isSelected ? 'font-semibold' : ''}>
                <div className="flex items-center gap-2">
                  <span>{type.name}</span>
                  {isSelected && (
                    <Badge variant="default" className="h-5 text-xs">
                      {t('prompt.slotSelector.selectedBadge')}
                    </Badge>
                  )}
                </div>
              </AccordionTrigger>
              <AccordionContent>
                <div className="space-y-3">
                  {typeSlotVariants.map((slot) => (
                    <label key={slot.id} className="flex cursor-pointer space-x-3 rounded-lg border p-4 hover:bg-gray-50">
                      <input
                        type="checkbox"
                        checked={selectedSlotIds.includes(slot.id)}
                        onChange={(e) => handleSlotChange(type.id, slot.id, e.target.checked)}
                        className="mt-1 h-4 w-4 rounded border-gray-300 text-blue-600 focus:ring-blue-500"
                      />
                      <div className="flex-1 space-y-1">
                        <div className="font-medium">{slot.name}</div>
                        <div className="text-sm text-gray-600">{slot.prompt}</div>
                        {slot.description && <div className="text-sm text-gray-500">{slot.description}</div>}
                        {slot.exampleImageUrl && (
                          <img
                            src={slot.exampleImageUrl}
                            alt={t('prompt.slotSelector.exampleAlt', { name: slot.name })}
                            className="mt-2 h-20 w-20 rounded object-cover"
                          />
                        )}
                      </div>
                    </label>
                  ))}
                </div>
              </AccordionContent>
            </AccordionItem>
          );
        })}
      </Accordion>
    </div>
  );
}
