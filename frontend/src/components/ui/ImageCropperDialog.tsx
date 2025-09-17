import { Button } from '@/components/ui/Button';
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from '@/components/ui/Dialog';
import ImageCropper from '@/components/ui/ImageCropper';
import { createCroppedImage, type CropArea } from '@/lib/image-utils';
import { useCallback, useEffect, useState } from 'react';
import { toast } from 'sonner';
import { useTranslation } from 'react-i18next';

interface ImageCropperDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  srcImage: string;
  aspectRatio?: number;
  onConfirm: (croppedImageUrl: string, croppingArea: CropArea) => void;
  title?: string;
  description?: string;
}

export default function ImageCropperDialog({
  open,
  onOpenChange,
  srcImage,
  aspectRatio,
  onConfirm,
  title,
  description,
}: ImageCropperDialogProps) {
  const { t } = useTranslation('editor');
  const [croppedAreaPixels, setCroppedAreaPixels] = useState<CropArea | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  const handleCropComplete = useCallback((_croppedArea: CropArea, croppedAreaPixels: CropArea) => {
    setCroppedAreaPixels(croppedAreaPixels);
  }, []);

  const handleCancel = () => {
    onOpenChange(false);
  };

  const handleConfirm = async () => {
    if (!croppedAreaPixels) {
      toast.error(t('steps.imageUpload.cropper.dialog.toasts.noArea'));
      return;
    }

    try {
      setIsLoading(true);
      const croppedImageUrl = await createCroppedImage(srcImage, croppedAreaPixels);
      onConfirm(croppedImageUrl, croppedAreaPixels);
      onOpenChange(false);
    } catch (error) {
      console.error('Failed to create cropped image:', error);
      toast.error(t('steps.imageUpload.cropper.dialog.toasts.fail'));
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    if (open) {
      setCroppedAreaPixels(null);
    }
  }, [open]);

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-2xl" showCloseButton={false}>
        <DialogHeader>
          <DialogTitle>{title ?? t('steps.imageUpload.cropper.dialog.title')}</DialogTitle>
          <DialogDescription>{description ?? t('steps.imageUpload.cropper.dialog.description')}</DialogDescription>
        </DialogHeader>

        <ImageCropper srcImage={srcImage} aspectRatio={aspectRatio} onCropComplete={handleCropComplete} />

        <DialogFooter>
          <Button variant="outline" onClick={handleCancel} disabled={isLoading}>
            {t('steps.imageUpload.cropper.dialog.buttons.cancel')}
          </Button>
          <Button onClick={handleConfirm} disabled={isLoading}>
            {isLoading
              ? t('steps.imageUpload.cropper.dialog.buttons.processing')
              : t('steps.imageUpload.cropper.dialog.buttons.confirm')}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
