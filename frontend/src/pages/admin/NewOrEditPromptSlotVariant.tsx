import { Button } from '@/components/ui/Button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/Card';
import { Input } from '@/components/ui/Input';
import { Label } from '@/components/ui/Label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/Select';
import { Textarea } from '@/components/ui/Textarea';
import type { CreatePromptSlotVariantRequest, UpdatePromptSlotVariantRequest } from '@/lib/api';
import { imagesApi, promptLlmsApi, promptSlotTypesApi, promptSlotVariantsApi } from '@/lib/api';
import type { PromptLLMOption, PromptSlotType } from '@/types/promptSlotVariant';
import { Trash2, Upload } from 'lucide-react';
import { useCallback, useEffect, useRef, useState } from 'react';
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
    exampleImageFilename: undefined,
    llm: '',
  });
  const [promptSlotTypes, setPromptSlotTypes] = useState<PromptSlotType[]>([]);
  const [llmOptions, setLlmOptions] = useState<PromptLLMOption[]>([]);
  const [loading, setLoading] = useState(false);
  const [initialLoading, setInitialLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [llmError, setLlmError] = useState<string | null>(null);
  const [uploadingImage, setUploadingImage] = useState(false);
  const [currentImageUrl, setCurrentImageUrl] = useState<string | null>(null);
  const [imageFile, setImageFile] = useState<File | null>(null);
  const blobUrlRef = useRef<string | null>(null);

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
        exampleImageFilename: slot.exampleImageUrl ? slot.exampleImageUrl.split('/').pop() : undefined,
        llm: slot.llm || '',
      });
      if (slot.llm) {
        setLlmOptions((current) => {
          if (current.some((option) => option.id === slot.llm)) {
            return current;
          }
          return [...current, { id: slot.llm, label: slot.llm }];
        });
      }
      if (slot.exampleImageUrl) {
        setCurrentImageUrl(slot.exampleImageUrl);
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

  // Cleanup blob URLs
  useEffect(() => {
    return () => {
      if (blobUrlRef.current) {
        URL.revokeObjectURL(blobUrlRef.current);
      }
    };
  }, []);

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

      let finalImageFilename = formData.exampleImageFilename;

      // Upload image if there's a new file selected
      if (imageFile) {
        try {
          setUploadingImage(true);
          const response = await imagesApi.upload(imageFile, 'PROMPT_SLOT_VARIANT_EXAMPLE');
          finalImageFilename = response.filename;
        } catch (error) {
          console.error('Error uploading image:', error);
          setError(t('promptSlotVariant.errors.uploadImage'));
          return;
        } finally {
          setUploadingImage(false);
        }
      }

      if (isEditing) {
        const updateData: UpdatePromptSlotVariantRequest = {
          name: formData.name,
          promptSlotTypeId: formData.promptSlotTypeId,
          prompt: formData.prompt,
          description: formData.description,
          llm: formData.llm,
          // Send null to explicitly remove image, undefined to not change it
          exampleImageFilename: finalImageFilename === 'pending' ? undefined : (finalImageFilename ?? null),
        };
        await promptSlotVariantsApi.update(parseInt(id), updateData);
      } else {
        const createData: CreatePromptSlotVariantRequest = {
          name: formData.name,
          promptSlotTypeId: formData.promptSlotTypeId,
          prompt: formData.prompt,
          description: formData.description,
          llm: formData.llm,
          // Send null to explicitly have no image, undefined to not set it
          exampleImageFilename: finalImageFilename === 'pending' ? undefined : (finalImageFilename ?? null),
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

  const handleImageUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    // Validate file type
    if (!file.type.startsWith('image/')) {
      setError(t('promptSlotVariant.errors.invalidImage'));
      return;
    }

    // Validate file size (max 5MB)
    if (file.size > 5 * 1024 * 1024) {
      setError(t('promptSlotVariant.errors.imageTooLarge'));
      return;
    }

    setError(null);

    // Store the file for later upload
    setImageFile(file);

    // Create a blob URL for preview
    if (blobUrlRef.current) {
      URL.revokeObjectURL(blobUrlRef.current);
    }
    const blobUrl = URL.createObjectURL(file);
    blobUrlRef.current = blobUrl;
    setCurrentImageUrl(blobUrl);

    // Set a placeholder filename (will be replaced with actual filename after upload)
    setFormData({ ...formData, exampleImageFilename: 'pending' });
  };

  const handleRemoveImage = () => {
    // Clean up blob URL if it exists
    if (blobUrlRef.current) {
      URL.revokeObjectURL(blobUrlRef.current);
      blobUrlRef.current = null;
    }

    // Reset image-related state
    setImageFile(null);
    setFormData({ ...formData, exampleImageFilename: undefined });
    setCurrentImageUrl(null);
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
                      <SelectItem key={option.id} value={option.id}>
                        {option.label}
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

            <div className="space-y-2">
              <Label htmlFor="exampleImage">{t('promptSlotVariant.form.exampleImage')}</Label>
              <div className="space-y-4">
                {currentImageUrl ? (
                  <div className="relative inline-block">
                    <img src={currentImageUrl} alt={t('promptSlotVariant.form.exampleAlt')} className="h-32 w-32 rounded-lg border object-cover" />
                    <Button type="button" variant="destructive" size="icon" className="absolute -top-2 -right-2 h-8 w-8" onClick={handleRemoveImage}>
                      <Trash2 className="h-4 w-4" />
                    </Button>
                  </div>
                ) : (
                  <div className="flex items-center gap-4">
                    <Input id="exampleImage" type="file" accept="image/*" onChange={handleImageUpload} className="hidden" />
                    <Label
                      htmlFor="exampleImage"
                      className="flex cursor-pointer items-center gap-2 rounded-lg border-2 border-dashed px-4 py-2 hover:border-gray-400"
                    >
                      <Upload className="h-4 w-4" />
                      {t('common.actions.uploadImage')}
                    </Label>
                    <span className="text-sm text-gray-500">{t('promptSlotVariant.form.exampleHint')}</span>
                  </div>
                )}
              </div>
            </div>

            <div className="flex gap-4">
              <Button type="submit" disabled={loading || uploadingImage}>
                {uploadingImage
                  ? t('common.status.uploadingImage')
                  : loading
                    ? t('common.status.saving')
                    : isEditing
                      ? t('promptSlotVariant.actions.update')
                      : t('promptSlotVariant.actions.create')}
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
