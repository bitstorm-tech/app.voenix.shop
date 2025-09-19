import { SlotTypeSelector } from '@/components/admin/prompt-slots/SlotTypeSelector';
import { Button } from '@/components/ui/Button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/Card';
import { Checkbox } from '@/components/ui/Checkbox';
import { Input } from '@/components/ui/Input';
import { InputWithCopy } from '@/components/ui/InputWithCopy';
import { Label } from '@/components/ui/Label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/Select';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/Tabs';
import { Textarea } from '@/components/ui/Textarea';
import type { CreatePromptRequest, PromptSlotUpdate, UpdatePromptRequest } from '@/lib/api';
import { imagesApi, promptCategoriesApi, promptLlmsApi, promptsApi, promptSubCategoriesApi } from '@/lib/api';
import { generatePromptNumber, getArticleNumberPlaceholder } from '@/lib/articleNumberUtils';
import { convertCostCalculationToCents, convertCostCalculationToEuros } from '@/lib/currency';
import { usePromptPriceStore } from '@/stores/admin/prompts/usePromptPriceStore';
import type { PromptCategory, PromptSubCategory } from '@/types/prompt';
import type { ProviderLLM } from '@/types/promptSlotVariant';
import { Calculator, FileText, Upload, X } from 'lucide-react';
import { useCallback, useEffect, useRef, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useNavigate, useParams } from 'react-router-dom';
import PriceCalculationTab from './prompts/components/PriceCalculationTab';
// no extra API needed; prompt includes costCalculation

export default function NewOrEditPrompt() {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const isEditing = !!id;
  const { t } = useTranslation('admin');

  const [formData, setFormData] = useState({
    title: '',
    promptText: '',
    categoryId: 0,
    subcategoryId: 0,
    active: true,
  });
  const [promptId, setPromptId] = useState<number | null>(null);
  const [priceId, setPriceId] = useState<number | null>(null);
  const [selectedSlotIds, setSelectedSlotIds] = useState<number[]>([]);
  const [categories, setCategories] = useState<PromptCategory[]>([]);
  const [subcategories, setSubcategories] = useState<PromptSubCategory[]>([]);
  const [llmOptions, setLlmOptions] = useState<ProviderLLM[]>([]);
  const [selectedLlm, setSelectedLlm] = useState<string>('');
  const [loading, setLoading] = useState(false);
  const [initialLoading, setInitialLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [llmError, setLlmError] = useState<string | null>(null);
  const [exampleImageFilename, setExampleImageFilename] = useState<string | null>(null);
  const [exampleImageUrl, setExampleImageUrl] = useState<string | null>(null);
  const [exampleImageFile, setExampleImageFile] = useState<File | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);
  const [activeTab, setActiveTab] = useState<'prompt' | 'cost-calculation'>('prompt');
  const setCostCalculation = usePromptPriceStore((state) => state.setCostCalculation);

  const fetchCategories = useCallback(async () => {
    try {
      const data = await promptCategoriesApi.getAll();
      setCategories(data);
    } catch (error) {
      console.error('Error fetching categories:', error);
      setError(t('prompt.errors.loadCategories'));
    }
  }, [t]);

  const fetchSubcategories = useCallback(async (categoryId: number) => {
    if (!categoryId) {
      setSubcategories([]);
      return;
    }

    try {
      const data = await promptSubCategoriesApi.getByCategory(categoryId);
      setSubcategories(data);
    } catch (error) {
      console.error('Error fetching subcategories:', error);
      // Don't show error to user as subcategory is optional
      setSubcategories([]);
    }
  }, []);

  const fetchLlms = useCallback(async () => {
    try {
      const response = await promptLlmsApi.getAll();
      setLlmOptions(response.llms);
      setLlmError(null);
    } catch (error) {
      console.error('Error fetching LLM options:', error);
      setLlmError(t('prompt.errors.loadLLMs'));
    }
  }, [t]);

  const fetchPrompt = useCallback(async () => {
    if (!id) return;

    try {
      const prompt = await promptsApi.getById(parseInt(id));
      setPromptId(prompt.id);
      if (typeof prompt.priceId === 'number') {
        setPriceId(prompt.priceId);
      }
      setFormData({
        title: prompt.title || '',
        promptText: prompt.promptText || '',
        categoryId: prompt.categoryId || 0,
        subcategoryId: prompt.subcategoryId || 0,
        active: prompt.active ?? true,
      });

      // Set selected slot IDs
      if (prompt.slots) {
        setSelectedSlotIds(prompt.slots.map((slot) => slot.id));
        if (prompt.slots.length > 0 && prompt.slots[0].llm) {
          const slotLlm = prompt.slots[0].llm;
          setSelectedLlm(slotLlm);
          setLlmOptions((current) => {
            if (current.some((option) => option.llm === slotLlm)) {
              return current;
            }
            return [
              ...current,
              {
                llm: slotLlm,
                provider: 'Unknown',
                friendlyName: slotLlm,
              },
            ];
          });
        }
      }

      // Set example image if exists
      if (prompt.exampleImageUrl) {
        setExampleImageUrl(prompt.exampleImageUrl);
        // Extract filename from URL
        const filename = prompt.exampleImageUrl.split('/').pop() || null;
        setExampleImageFilename(filename);
      }

      // Fetch subcategories for the prompt's category
      if (prompt.categoryId) {
        await fetchSubcategories(prompt.categoryId);
      }

      // If prompt has a priceId, fetch and initialize the price store
      if (prompt.costCalculation) {
        const converted = convertCostCalculationToEuros(prompt.costCalculation);
        if (converted) {
          setCostCalculation(converted);
        }
      }
    } catch (error) {
      console.error('Error fetching prompt:', error);
      setError(t('prompt.errors.load'));
      throw error;
    }
  }, [fetchSubcategories, id, setCostCalculation, t]);

  useEffect(() => {
    let isMounted = true;

    const load = async () => {
      try {
        setInitialLoading(true);
        if (isMounted) {
          setError(null);
        }
        await Promise.all([fetchCategories(), fetchLlms()]);
        if (isEditing) {
          await fetchPrompt();
        }
      } catch (error) {
        console.error('Error initializing prompt form:', error);
        if (isMounted) {
          setError(t('prompt.errors.load'));
        }
      } finally {
        if (isMounted) {
          setInitialLoading(false);
        }
      }
    };

    load();

    return () => {
      isMounted = false;
    };
  }, [fetchCategories, fetchLlms, fetchPrompt, isEditing, t]);

  // Cleanup blob URLs on unmount
  useEffect(() => {
    return () => {
      if (exampleImageUrl && exampleImageUrl.startsWith('blob:')) {
        URL.revokeObjectURL(exampleImageUrl);
      }
    };
  }, [exampleImageUrl]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!formData.title.trim()) {
      setError(t('prompt.errors.titleRequired'));
      setActiveTab('prompt');
      return;
    }

    if (!formData.categoryId) {
      setError(t('prompt.errors.categoryRequired'));
      setActiveTab('prompt');
      return;
    }

    try {
      setLoading(true);
      setError(null);

      let imageFilename = exampleImageFilename;

      // Upload new image if there is one
      if (exampleImageFile) {
        try {
          const uploadResult = await imagesApi.upload(exampleImageFile, 'PROMPT_EXAMPLE');
          imageFilename = uploadResult.filename;
        } catch (uploadError: unknown) {
          console.error('Error uploading image:', uploadError);
          const errorMessage = uploadError instanceof Error ? uploadError.message : '';
          setError(
            t('prompt.errors.uploadImage', {
              message: errorMessage || t('common.errors.generic'),
            }),
          );
          setLoading(false);
          return;
        }
      }

      const slots: PromptSlotUpdate[] = selectedSlotIds.map((slotId) => ({
        slotId: slotId,
      }));

      // Gather price calculation from store and convert to cents for API
      const { costCalculation } = usePromptPriceStore.getState();
      const costCalcCents = convertCostCalculationToCents(costCalculation);

      if (isEditing) {
        const promptPart: UpdatePromptRequest = {
          title: formData.title,
          promptText: formData.promptText || undefined,
          categoryId: formData.categoryId,
          subcategoryId: formData.subcategoryId || undefined,
          active: formData.active,
          slots,
          exampleImageFilename: imageFilename || undefined,
          priceId: priceId ?? undefined,
          costCalculation: costCalcCents || undefined,
        };
        await promptsApi.update(parseInt(id), promptPart);
      } else {
        const promptPart: CreatePromptRequest = {
          title: formData.title,
          promptText: formData.promptText || undefined,
          categoryId: formData.categoryId,
          subcategoryId: formData.subcategoryId || undefined,
          active: formData.active,
          slots,
          exampleImageFilename: imageFilename || undefined,
          costCalculation: costCalcCents || undefined,
        };
        await promptsApi.create(promptPart);
      }

      navigate('/admin/prompts');
    } catch (error) {
      console.error('Error saving prompt:', error);
      setError(t('prompt.errors.save'));
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = () => {
    navigate('/admin/prompts');
  };

  const handleImageUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file || !file.type.startsWith('image/')) {
      setError(t('prompt.errors.invalidImage'));
      return;
    }

    // Check file size (10MB limit)
    const maxSize = 10 * 1024 * 1024;
    if (file.size > maxSize) {
      setError(t('prompt.errors.imageTooLarge'));
      return;
    }

    // Clean up previous blob URL if exists
    if (exampleImageUrl && exampleImageUrl.startsWith('blob:')) {
      URL.revokeObjectURL(exampleImageUrl);
    }

    // Create blob URL for preview
    const blobUrl = URL.createObjectURL(file);
    setExampleImageFile(file);
    setExampleImageUrl(blobUrl);
    setError(null);

    // Reset the input
    if (fileInputRef.current) {
      fileInputRef.current.value = '';
    }
  };

  const handleRemoveImage = () => {
    // Clean up blob URL if exists
    if (exampleImageUrl && exampleImageUrl.startsWith('blob:')) {
      URL.revokeObjectURL(exampleImageUrl);
    }
    setExampleImageFilename(null);
    setExampleImageUrl(null);
    setExampleImageFile(null);
  };

  if (initialLoading) {
    return (
      <div className="container mx-auto p-6">
        <div className="flex h-64 items-center justify-center">
          <p className="text-gray-500">{t('common.loading')}</p>
        </div>
      </div>
    );
  }

  return (
    <div className="container mx-auto p-6">
      <div className="mx-auto max-w-4xl">
        <Tabs value={activeTab} onValueChange={(v) => setActiveTab(v as 'prompt' | 'cost-calculation')} className="space-y-6">
          <TabsList className="grid w-full grid-cols-2 lg:w-auto lg:grid-flow-col">
            <TabsTrigger value="prompt" className="gap-2">
              <FileText className="h-4 w-4" />
              <span className="hidden sm:inline">{t('prompt.tabs.prompt')}</span>
            </TabsTrigger>
            <TabsTrigger value="cost-calculation" className="gap-2">
              <Calculator className="h-4 w-4" />
              <span className="hidden sm:inline">{t('prompt.tabs.costCalculation')}</span>
            </TabsTrigger>
          </TabsList>

          <TabsContent value="prompt" forceMount>
            <Card>
              <CardHeader>
                <CardTitle>{isEditing ? t('prompt.title.edit') : t('prompt.title.new')}</CardTitle>
                <CardDescription>{isEditing ? t('prompt.description.edit') : t('prompt.description.new')}</CardDescription>
              </CardHeader>
              <CardContent>
                <form id="prompt-form" onSubmit={handleSubmit} className="space-y-6">
                  {error && <div className="rounded border border-red-200 bg-red-50 px-4 py-3 text-red-700">{error}</div>}

                  <div className="space-y-2">
                    <Label htmlFor="title">{t('prompt.form.title')}</Label>
                    <Input
                      id="title"
                      value={formData.title}
                      onChange={(e) => setFormData({ ...formData, title: e.target.value })}
                      placeholder={t('prompt.form.titlePlaceholder')}
                      required
                    />
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="promptNumber">{t('prompt.form.promptNumber')}</Label>
                    <InputWithCopy
                      id="promptNumber"
                      value={
                        generatePromptNumber(formData.categoryId || null, formData.subcategoryId || null, promptId) || getArticleNumberPlaceholder()
                      }
                      placeholder={getArticleNumberPlaceholder()}
                      className="[&_input]:bg-muted"
                    />
                  </div>

                  <div className="flex gap-8">
                    <div className="space-y-2">
                      <Label htmlFor="category">{t('prompt.form.category')}</Label>
                      <Select
                        value={formData.categoryId.toString()}
                        onValueChange={(value) => {
                          const newCategoryId = parseInt(value);
                          setFormData({ ...formData, categoryId: newCategoryId, subcategoryId: 0 });
                          fetchSubcategories(newCategoryId);
                        }}
                      >
                        <SelectTrigger id="category">
                          <SelectValue placeholder={t('prompt.form.categoryPlaceholder')} />
                        </SelectTrigger>
                        <SelectContent>
                          {categories.map((category) => (
                            <SelectItem key={category.id} value={category.id.toString()}>
                              {category.name}
                            </SelectItem>
                          ))}
                        </SelectContent>
                      </Select>
                    </div>

                    {formData.categoryId > 0 && subcategories.length > 0 && (
                      <div className="space-y-2">
                        <Label htmlFor="subcategory">{t('prompt.form.subcategory')}</Label>
                        <Select
                          value={formData.subcategoryId.toString()}
                          onValueChange={(value) => setFormData({ ...formData, subcategoryId: parseInt(value) })}
                        >
                          <SelectTrigger id="subcategory">
                            <SelectValue placeholder={t('prompt.form.subcategoryPlaceholder')} />
                          </SelectTrigger>
                          <SelectContent>
                            <SelectItem value="0">{t('prompt.form.noSubcategory')}</SelectItem>
                            {subcategories.map((subcategory) => (
                              <SelectItem key={subcategory.id} value={subcategory.id.toString()}>
                                {subcategory.name}
                              </SelectItem>
                            ))}
                          </SelectContent>
                        </Select>
                      </div>
                    )}
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="promptText">{t('prompt.form.promptStyle')}</Label>
                    <Textarea
                      id="promptText"
                      value={formData.promptText}
                      onChange={(e) => setFormData({ ...formData, promptText: e.target.value })}
                      placeholder={t('prompt.form.promptStylePlaceholder')}
                      rows={4}
                    />
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="llm">{t('prompt.form.llm')}</Label>
                    <Select value={selectedLlm} onValueChange={(value) => setSelectedLlm(value)} disabled={llmOptions.length === 0}>
                      <SelectTrigger id="llm">
                        <SelectValue placeholder={t('prompt.form.llmPlaceholder')} />
                      </SelectTrigger>
                      <SelectContent>
                        {llmOptions.map((option) => (
                          <SelectItem key={option.llm} value={option.llm}>
                            {option.friendlyName}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                    {llmError ? (
                      <p className="text-sm text-red-600">{llmError}</p>
                    ) : llmOptions.length === 0 ? (
                      <p className="text-muted-foreground text-sm">{t('prompt.form.llmEmpty')}</p>
                    ) : null}
                  </div>

                  <div className="space-y-2">
                    <SlotTypeSelector selectedSlotIds={selectedSlotIds} onSelectionChange={setSelectedSlotIds} llmFilter={selectedLlm || undefined} />
                  </div>

                  <div className="space-y-2">
                    <Label>{t('prompt.exampleImage.label')}</Label>
                    <div className="space-y-3">
                      {exampleImageUrl ? (
                        <div className="relative w-full max-w-md">
                          <img
                            src={exampleImageUrl}
                            alt={t('prompt.exampleImage.alt')}
                            className="w-full rounded-lg border border-gray-200 object-contain"
                          />
                          <Button
                            type="button"
                            variant="outline"
                            size="sm"
                            className="absolute top-2 right-2 bg-white shadow-sm"
                            onClick={handleRemoveImage}
                          >
                            <X className="h-4 w-4" />
                          </Button>
                        </div>
                      ) : (
                        <div className="flex items-center gap-2">
                          <Button type="button" variant="outline" onClick={() => fileInputRef.current?.click()}>
                            <Upload className="mr-2 h-4 w-4" />
                            {t('common.actions.uploadImage')}
                          </Button>
                          <p className="text-sm text-gray-500">{t('prompt.exampleImage.hint')}</p>
                        </div>
                      )}
                      <input ref={fileInputRef} type="file" accept="image/*" onChange={handleImageUpload} className="hidden" />
                    </div>
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="active">{t('prompt.form.status')}</Label>
                    <div className="flex items-center space-x-2">
                      <Checkbox
                        id="active"
                        checked={formData.active}
                        onCheckedChange={(checked) => setFormData({ ...formData, active: checked as boolean })}
                      />
                      <Label htmlFor="active" className="font-normal">
                        {t('prompt.form.statusHint')}
                      </Label>
                    </div>
                  </div>
                </form>
              </CardContent>
            </Card>
          </TabsContent>

          <TabsContent value="cost-calculation">
            <PriceCalculationTab />
          </TabsContent>
        </Tabs>

        {/* Global action bar visible for both tabs */}
        <div className="mt-6 flex gap-4">
          <Button type="submit" form="prompt-form" disabled={loading}>
            {loading ? t('common.status.saving') : isEditing ? t('prompt.actions.update') : t('prompt.actions.create')}
          </Button>
          <Button type="button" variant="outline" onClick={handleCancel}>
            {t('common.actions.cancel')}
          </Button>
        </div>
      </div>
    </div>
  );
}
