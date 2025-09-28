import { Button } from '@/components/ui/Button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/Card';
import { Input } from '@/components/ui/Input';
import { Label } from '@/components/ui/Label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/Select';
import { Textarea } from '@/components/ui/Textarea';
import type { CreatePromptSlotVariantRequest, UpdatePromptSlotVariantRequest } from '@/lib/api';
import { promptLlmsApi, promptSlotTypesApi, promptSlotVariantsApi } from '@/lib/api';
import type { PromptSlotType, ProviderLLM } from '@/types/promptSlotVariant';
import { useCallback, useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useNavigate, useParams } from 'react-router-dom';

export default function NewOrEditPromptSlotVariant() {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const isEditing = !!id;
  const { t } = useTranslation('admin');

  const [formData, setFormData] = useState<CreatePromptSlotVariantRequest>({
    name: '',
    promptSlotTypeId: 0,
    prompt: '',
    description: '',
    llm: '',
  });
  const [promptSlotTypes, setPromptSlotTypes] = useState<PromptSlotType[]>([]);
  const [llmOptions, setLlmOptions] = useState<ProviderLLM[]>([]);
  const [loading, setLoading] = useState(false);
  const [initialLoading, setInitialLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [llmError, setLlmError] = useState<string | null>(null);

  const fetchPromptSlotTypes = useCallback(async () => {
    try {
      const data = await promptSlotTypesApi.getAll();
      setPromptSlotTypes(data);
    } catch (error) {
      console.error('Error fetching prompt slot types:', error);
      setError(t('promptSlotVariant.errors.loadTypes'));
    }
  }, [t]);

  const fetchLlmOptions = useCallback(async () => {
    try {
      const response = await promptLlmsApi.getAll();
      setLlmOptions(response.llms);
      setLlmError(null);
    } catch (error) {
      console.error('Error fetching llm options:', error);
      setLlmError(t('promptSlotVariant.errors.loadLLMs'));
    }
  }, [t]);

  const fetchSlot = useCallback(async () => {
    if (!id) return;

    try {
      const slot = await promptSlotVariantsApi.getById(parseInt(id));
      setFormData({
        name: slot.name,
        promptSlotTypeId: slot.promptSlotTypeId,
        prompt: slot.prompt,
        description: slot.description || '',
        llm: slot.llm || '',
      });
      if (slot.llm) {
        setLlmOptions((current) => {
          if (current.some((option) => option.llm === slot.llm)) {
            return current;
          }
          return [
            ...current,
            {
              llm: slot.llm,
              provider: 'Unknown',
              friendlyName: slot.llm,
            },
          ];
        });
      }
    } catch (error) {
      console.error('Error fetching slot:', error);
      setError(t('promptSlotVariant.errors.load'));
    }
  }, [id, t]);

  useEffect(() => {
    let isActive = true;
    const load = async () => {
      try {
        setInitialLoading(true);
        await Promise.all([fetchPromptSlotTypes(), fetchLlmOptions()]);
        if (isEditing) {
          await fetchSlot();
        }
      } finally {
        if (isActive) {
          setInitialLoading(false);
        }
      }
    };

    load().catch(() => {
      if (isActive) {
        setInitialLoading(false);
      }
    });

    return () => {
      isActive = false;
    };
  }, [fetchPromptSlotTypes, fetchLlmOptions, fetchSlot, isEditing]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!formData.name.trim()) {
      setError(t('promptSlotVariant.errors.nameRequired'));
      return;
    }

    if (!formData.promptSlotTypeId) {
      setError(t('promptSlotVariant.errors.typeRequired'));
      return;
    }

    if (!formData.prompt.trim()) {
      setError(t('promptSlotVariant.errors.promptRequired'));
      return;
    }

    if (!formData.llm.trim()) {
      setError(t('promptSlotVariant.errors.llmRequired'));
      return;
    }

    try {
      setLoading(true);
      setError(null);

      if (isEditing) {
        const updateData: UpdatePromptSlotVariantRequest = {
          name: formData.name,
          promptSlotTypeId: formData.promptSlotTypeId,
          prompt: formData.prompt,
          description: formData.description,
          llm: formData.llm,
        };
        await promptSlotVariantsApi.update(parseInt(id), updateData);
      } else {
        const createData: CreatePromptSlotVariantRequest = {
          name: formData.name,
          promptSlotTypeId: formData.promptSlotTypeId,
          prompt: formData.prompt,
          description: formData.description,
          llm: formData.llm,
        };
        await promptSlotVariantsApi.create(createData);
      }

      navigate('/admin/slot-variants');
    } catch (error) {
      console.error('Error saving slot:', error);
      setError(t('promptSlotVariant.errors.save'));
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = () => {
    navigate('/admin/slot-variants');
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
      <Card className="mx-auto max-w-2xl">
        <CardHeader>
          <CardTitle>{isEditing ? t('promptSlotVariant.title.edit') : t('promptSlotVariant.title.new')}</CardTitle>
          <CardDescription>{isEditing ? t('promptSlotVariant.description.edit') : t('promptSlotVariant.description.new')}</CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-6">
            {error && <div className="rounded border border-red-200 bg-red-50 px-4 py-3 text-red-700">{error}</div>}

            <div className="space-y-2">
              <Label htmlFor="name">{t('promptSlotVariant.form.name')}</Label>
              <Input
                id="name"
                value={formData.name}
                onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                placeholder={t('promptSlotVariant.form.namePlaceholder')}
                required
              />
            </div>

            <div className="grid gap-6 md:grid-cols-2">
              <div className="space-y-2">
                <Label htmlFor="promptSlotType">{t('promptSlotVariant.form.type')}</Label>
                <Select
                  value={formData.promptSlotTypeId.toString()}
                  onValueChange={(value) => setFormData({ ...formData, promptSlotTypeId: parseInt(value) })}
                >
                  <SelectTrigger id="promptSlotType">
                    <SelectValue placeholder={t('promptSlotVariant.form.typePlaceholder')} />
                  </SelectTrigger>
                  <SelectContent>
                    {promptSlotTypes.map((type) => (
                      <SelectItem key={type.id} value={type.id.toString()}>
                        {type.name}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              <div className="space-y-2">
                <Label htmlFor="llm">{t('promptSlotVariant.form.llm')}</Label>
                <Select value={formData.llm} onValueChange={(value) => setFormData({ ...formData, llm: value })} disabled={llmOptions.length === 0}>
                  <SelectTrigger id="llm">
                    <SelectValue placeholder={t('promptSlotVariant.form.llmPlaceholder')} />
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
                  <p className="text-muted-foreground text-sm">{t('promptSlotVariant.form.llmEmpty')}</p>
                ) : null}
              </div>
            </div>

            <div className="space-y-2">
              <Label htmlFor="prompt">{t('promptSlotVariant.form.prompt')}</Label>
              <Textarea
                id="prompt"
                value={formData.prompt}
                onChange={(e) => setFormData({ ...formData, prompt: e.target.value })}
                placeholder={t('promptSlotVariant.form.promptPlaceholder')}
                rows={6}
                required
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="description">{t('promptSlotVariant.form.description')}</Label>
              <Textarea
                id="description"
                value={formData.description || ''}
                onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                placeholder={t('promptSlotVariant.form.descriptionPlaceholder')}
                rows={3}
              />
            </div>

            <div className="flex gap-4">
              <Button type="submit" disabled={loading}>
                {loading ? t('common.status.saving') : isEditing ? t('promptSlotVariant.actions.update') : t('promptSlotVariant.actions.create')}
              </Button>
              <Button type="button" variant="outline" onClick={handleCancel}>
                {t('common.actions.cancel')}
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}
