import { Button } from '@/components/ui/Button';
import { Checkbox } from '@/components/ui/Checkbox';
import { ColorPicker } from '@/components/ui/ColorPicker';
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from '@/components/ui/Dialog';
import ImageCropperFixedBoxDialog from '@/components/ui/ImageCropperFixedBoxDialog';
import { Input } from '@/components/ui/Input';
import { Label } from '@/components/ui/Label';
import { articlesApi } from '@/lib/api';
import { blobUrlToFile } from '@/lib/image-utils';
import type { ArticleMugVariant, CreateArticleMugVariantRequest } from '@/types/article';
import { Upload, X } from 'lucide-react';
import { useEffect, useRef, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { toast } from 'sonner';

interface MugVariantDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  articleId?: number;
  variant?: ArticleMugVariant;
  temporaryVariant?: CreateArticleMugVariantRequest;
  temporaryVariantIndex?: number;
  existingVariants: ArticleMugVariant[];
  existingTemporaryVariants: CreateArticleMugVariantRequest[];
  onVariantSaved?: (variant: ArticleMugVariant) => void;
  onTemporaryVariantSaved?: (variant: CreateArticleMugVariantRequest, index?: number) => void;
  onRefetchVariants?: () => void;
}

export default function MugVariantDialog({
  open,
  onOpenChange,
  articleId,
  variant,
  temporaryVariant,
  temporaryVariantIndex,
  existingVariants,
  existingTemporaryVariants,
  onVariantSaved,
  onTemporaryVariantSaved,
  onRefetchVariants,
}: MugVariantDialogProps) {
  const isEditing = !!(variant || temporaryVariant);
  const isTemporary = !articleId || !!temporaryVariant;
  const fileInputRef = useRef<HTMLInputElement>(null);
  const { t } = useTranslation('adminArticles');

  const DEFAULT_FORM_DATA: CreateArticleMugVariantRequest = {
    insideColorCode: '#ffffff',
    outsideColorCode: '#ffffff',
    name: '',
    articleVariantNumber: '',
    isDefault: false,
    active: true,
  };

  const [formData, setFormData] = useState<CreateArticleMugVariantRequest>(DEFAULT_FORM_DATA);

  const [imageState, setImageState] = useState({
    file: null as File | null,
    previewUrl: null as string | null,
    originalUrl: null as string | null,
    existingUrl: null as string | null,
    showCropper: false,
    isRemoved: false,
  });
  const [loading, setLoading] = useState(false);

  const MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

  const cleanupBlobUrls = () => {
    if (imageState.previewUrl && imageState.previewUrl !== imageState.existingUrl && imageState.previewUrl.startsWith('blob:')) {
      URL.revokeObjectURL(imageState.previewUrl);
    }
    if (imageState.originalUrl && imageState.originalUrl.startsWith('blob:')) {
      URL.revokeObjectURL(imageState.originalUrl);
    }
  };

  const resetImageState = () => {
    cleanupBlobUrls();
    setImageState({
      file: null,
      previewUrl: null,
      originalUrl: null,
      existingUrl: null,
      showCropper: false,
      isRemoved: false,
    });
  };

  const validateImageFile = (file: File): string | null => {
    if (!file.type.startsWith('image/')) return t('form.mugVariants.dialog.errors.invalidImageType');
    if (file.size > MAX_FILE_SIZE) return t('form.mugVariants.dialog.errors.imageTooLarge');
    return null;
  };

  const validateForm = (): string | null => {
    if (!formData.name) return t('form.mugVariants.dialog.errors.nameRequired');

    const isDuplicate = isTemporary
      ? existingTemporaryVariants.some((v, index) => v.name === formData.name && index !== temporaryVariantIndex)
      : existingVariants.some((v) => v.name === formData.name && v.id !== variant?.id);

    if (isDuplicate) return t('form.mugVariants.dialog.errors.duplicateName');
    return null;
  };

  useEffect(() => {
    if (!open) return;

    // Initialize form data
    const initialData = variant || temporaryVariant || DEFAULT_FORM_DATA;
    setFormData({
      insideColorCode: initialData.insideColorCode,
      outsideColorCode: initialData.outsideColorCode,
      name: initialData.name,
      articleVariantNumber: initialData.articleVariantNumber || '',
      isDefault: initialData.isDefault || false,
      active: initialData.active ?? true,
    });

    // Initialize image state
    resetImageState();
    if (variant?.exampleImageUrl) {
      setImageState((prev) => ({
        ...prev,
        previewUrl: variant.exampleImageUrl!,
        existingUrl: variant.exampleImageUrl!,
      }));
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [open, variant, temporaryVariant]);

  // Cleanup effect
  useEffect(() => {
    return () => {
      cleanupBlobUrls();
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handleImageUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    const error = validateImageFile(file);
    if (error) {
      toast.error(error);
      return;
    }

    // Clean up previous blob URLs
    cleanupBlobUrls();

    const blobUrl = URL.createObjectURL(file);
    setImageState({
      file,
      previewUrl: blobUrl,
      originalUrl: blobUrl,
      existingUrl: imageState.existingUrl,
      showCropper: true,
      isRemoved: false,
    });

    if (fileInputRef.current) {
      fileInputRef.current.value = '';
    }
  };

  const handleCropCancel = () => {
    cleanupBlobUrls();
    setImageState({
      file: null,
      previewUrl: imageState.existingUrl,
      originalUrl: null,
      existingUrl: imageState.existingUrl,
      showCropper: false,
      isRemoved: false,
    });
  };

  const handleCropConfirm = async (croppedImageUrl: string) => {
    try {
      // Update the preview URL to show the cropped version
      if (imageState.previewUrl && imageState.previewUrl !== imageState.originalUrl && imageState.previewUrl !== imageState.existingUrl) {
        URL.revokeObjectURL(imageState.previewUrl);
      }

      // Convert the cropped blob URL to a File object to replace the original
      if (imageState.file) {
        const croppedFile = await blobUrlToFile(croppedImageUrl, imageState.file.name, imageState.file.type);
        setImageState((prev) => ({
          ...prev,
          file: croppedFile,
          previewUrl: croppedImageUrl,
          showCropper: false,
        }));
      }
    } catch (error) {
      console.error('Failed to process cropped image:', error);
      toast.error(t('form.mugVariants.dialog.errors.processCropped'));
    }
  };

  const handleRemoveImage = () => {
    cleanupBlobUrls();

    // For newly uploaded images (no existing image), clear everything
    if (!imageState.existingUrl) {
      resetImageState();
    } else {
      // For existing images, keep the existing URL but mark for removal
      setImageState((prev) => ({
        ...prev,
        file: null,
        previewUrl: prev.existingUrl,
        originalUrl: null,
        isRemoved: true,
      }));
    }
  };

  const handleTemporarySubmit = () => {
    const variantToSave = { ...formData };
    if (onTemporaryVariantSaved) {
      onTemporaryVariantSaved(variantToSave, temporaryVariantIndex);
      toast.success(isEditing ? t('form.mugVariants.dialog.toasts.temporary.updated') : t('form.mugVariants.dialog.toasts.temporary.added'));
    }
    onOpenChange(false);
  };

  const updateVariant = async (variant: ArticleMugVariant): Promise<ArticleMugVariant> => {
    let response = await articlesApi.updateMugVariant(variant.id, formData);

    // Handle image removal if user removed the existing image
    if (imageState.isRemoved && imageState.existingUrl && !imageState.file) {
      try {
        response = await articlesApi.removeMugVariantImage(variant.id);
        toast.success(t('form.mugVariants.dialog.toasts.image.removed'));
      } catch (error) {
        console.error('Error removing variant image:', error);
        toast.error(t('form.mugVariants.dialog.errors.removeImage'));
      }
    }

    return response;
  };

  const createVariant = async (articleId: number): Promise<ArticleMugVariant> => {
    return await articlesApi.createMugVariant(articleId, formData);
  };

  const uploadImage = async (variantId: number): Promise<ArticleMugVariant> => {
    if (!imageState.file) throw new Error('No file to upload');

    try {
      return await articlesApi.uploadMugVariantImage(variantId, imageState.file);
    } catch (imageError) {
      console.error('Error uploading variant image:', imageError);
      toast.error(isEditing ? t('form.mugVariants.dialog.errors.uploadFailedUpdate') : t('form.mugVariants.dialog.errors.uploadFailedCreate'));
      throw imageError;
    }
  };

  const handleSuccess = (response: ArticleMugVariant) => {
    if (formData.isDefault && onRefetchVariants) {
      onRefetchVariants();
    } else if (onVariantSaved) {
      onVariantSaved(response);
    }

    toast.success(isEditing ? t('form.mugVariants.dialog.toasts.success.update') : t('form.mugVariants.dialog.toasts.success.create'));
    onOpenChange(false);
  };

  const handlePersistentSubmit = async () => {
    if (!articleId) return;

    setLoading(true);
    try {
      let response: ArticleMugVariant;

      if (isEditing && variant) {
        response = await updateVariant(variant);
      } else {
        response = await createVariant(articleId);
      }

      // Upload image if a new file was selected
      if (imageState.file && response.id) {
        if (isEditing && variant?.exampleImageFilename) {
          try {
            await articlesApi.removeMugVariantImage(variant.id);
          } catch (error) {
            console.error('Failed to remove previous mug variant image:', error);
          }
        }

        const imageResponse = await uploadImage(response.id);
        // Update the response with the data returned from the backend
        response.exampleImageFilename = imageResponse.exampleImageFilename;
        response.exampleImageUrl = imageResponse.exampleImageUrl;
      }

      handleSuccess(response);
    } catch (error) {
      console.error(isEditing ? 'Error updating variant:' : 'Error adding variant:', error);
      toast.error(isEditing ? t('form.mugVariants.dialog.errors.updateFailed') : t('form.mugVariants.dialog.errors.createFailed'));
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    const error = validateForm();
    if (error) {
      toast.error(error);
      return;
    }

    if (isTemporary) {
      handleTemporarySubmit();
    } else {
      await handlePersistentSubmit();
    }
  };

  const handleCancel = () => {
    onOpenChange(false);
  };

  return (
    <>
      <ImageCropperFixedBoxDialog
        open={imageState.showCropper && !!imageState.originalUrl}
        onOpenChange={(open) => !open && handleCropCancel()}
        srcImage={imageState.originalUrl || ''}
        aspectRatio={1}
        onConfirm={handleCropConfirm}
        title={t('form.mugVariants.dialog.cropper.title')}
        description={t('form.mugVariants.dialog.cropper.description')}
      />

      <Dialog open={open} onOpenChange={onOpenChange}>
        <DialogContent className="max-h-[90vh] overflow-y-auto sm:max-w-[600px]">
          <form onSubmit={handleSubmit}>
            <DialogHeader>
              <DialogTitle>{isEditing ? t('form.mugVariants.dialog.title.edit') : t('form.mugVariants.dialog.title.add')}</DialogTitle>
              <DialogDescription>
                {isEditing ? t('form.mugVariants.dialog.description.edit') : t('form.mugVariants.dialog.description.add')}
              </DialogDescription>
            </DialogHeader>

            <div className="grid gap-6 py-6">
              <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
                <div className="space-y-2">
                  <Label htmlFor="name">{t('form.mugVariants.dialog.fields.name.label')}</Label>
                  <Input
                    id="name"
                    value={formData.name}
                    onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                    placeholder={t('form.mugVariants.dialog.fields.name.placeholder')}
                    required
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="articleVariantNumber">{t('form.mugVariants.dialog.fields.articleVariantNumber.label')}</Label>
                  <Input
                    id="articleVariantNumber"
                    value={formData.articleVariantNumber || ''}
                    onChange={(e) => setFormData({ ...formData, articleVariantNumber: e.target.value })}
                    placeholder={t('form.mugVariants.dialog.fields.articleVariantNumber.placeholder')}
                    maxLength={100}
                  />
                </div>

                <div className="space-y-2">
                  <Label>{t('form.mugVariants.table.insideColor')}</Label>
                  <ColorPicker value={formData.insideColorCode} onChange={(color) => setFormData({ ...formData, insideColorCode: color })} />
                </div>

                <div className="space-y-2">
                  <Label>{t('form.mugVariants.table.outsideColor')}</Label>
                  <ColorPicker value={formData.outsideColorCode} onChange={(color) => setFormData({ ...formData, outsideColorCode: color })} />
                </div>
              </div>

              <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
                <div className="space-y-2">
                  <Label>{t('form.mugVariants.dialog.defaultVariant')}</Label>
                  <div className="flex items-center space-x-2">
                    <Checkbox
                      id="default-variant"
                      checked={formData.isDefault}
                      onCheckedChange={(checked) => setFormData({ ...formData, isDefault: checked === true })}
                    />
                    <Label htmlFor="default-variant" className="cursor-pointer text-sm font-normal">
                      {t('form.mugVariants.dialog.setAsDefault')}
                    </Label>
                  </div>
                </div>

                <div className="space-y-2">
                  <Label>{t('form.mugVariants.dialog.activeStatus')}</Label>
                  <div className="flex items-center space-x-2">
                    <Checkbox
                      id="active-variant"
                      checked={formData.active ?? true}
                      onCheckedChange={(checked) => setFormData({ ...formData, active: checked === true })}
                    />
                    <Label htmlFor="active-variant" className="cursor-pointer text-sm font-normal">
                      {t('form.mugVariants.dialog.activeDescription')}
                    </Label>
                  </div>
                </div>
              </div>

              <div className="space-y-2">
                <Label>{t('form.mugVariants.dialog.exampleImage')}</Label>
                <div className="space-y-2">
                  {(() => {
                    const showImagePreview = imageState.previewUrl && !imageState.isRemoved && !imageState.showCropper;

                    return showImagePreview ? (
                      <div className="relative inline-block">
                        <img
                          src={imageState.previewUrl!}
                          alt={t('form.mugVariants.dialog.previewAlt')}
                          className="h-20 w-20 rounded border object-cover"
                        />
                        <Button
                          type="button"
                          variant="ghost"
                          size="sm"
                          className="absolute -top-2 -right-2 h-6 w-6 rounded-full bg-red-500 p-0 text-white hover:bg-red-600"
                          onClick={handleRemoveImage}
                        >
                          <X className="h-4 w-4" />
                        </Button>
                      </div>
                    ) : (
                      <div>
                        <input ref={fileInputRef} type="file" accept="image/*" onChange={handleImageUpload} className="hidden" />
                        <Button
                          type="button"
                          variant="outline"
                          size="sm"
                          onClick={() => fileInputRef.current?.click()}
                          className="w-full max-w-[200px]"
                        >
                          <Upload className="mr-2 h-4 w-4" />
                          {t('form.mugVariants.dialog.actions.uploadImage')}
                        </Button>
                      </div>
                    );
                  })()}
                </div>
              </div>
            </div>

            <DialogFooter>
              <Button type="button" variant="outline" onClick={handleCancel}>
                {t('form.mugVariants.dialog.actions.cancel')}
              </Button>
              <Button type="submit" disabled={loading}>
                {loading
                  ? t('form.mugVariants.dialog.actions.saving')
                  : isEditing
                    ? t('form.mugVariants.dialog.actions.update')
                    : t('form.mugVariants.dialog.actions.add')}
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>
    </>
  );
}
