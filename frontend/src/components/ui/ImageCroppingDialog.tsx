import { Button } from '@/components/ui/Button';
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from '@/components/ui/Dialog';
import ImageCropper from '@/components/ui/ImageCropper';
import { createCroppedImage, type CropArea } from '@/lib/image-utils';
import { useCallback, useEffect, useState } from 'react';
import { toast } from 'sonner';

interface ImageCroppingDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  srcImage: string;
  aspectRatio?: number;
  onConfirm: (croppedImageUrl: string, croppingArea: CropArea) => void;
  title?: string;
  description?: string;
}

export default function ImageCroppingDialog({
  open,
  onOpenChange,
  srcImage,
  aspectRatio,
  onConfirm,
  title = 'Crop Image',
  description = 'Select the area you want to use',
}: ImageCroppingDialogProps) {
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
      toast.error('Please select a crop area');
      return;
    }

    try {
      setIsLoading(true);
      const croppedImageUrl = await createCroppedImage(srcImage, croppedAreaPixels);
      onConfirm(croppedImageUrl, croppedAreaPixels);
      onOpenChange(false);
    } catch (error) {
      console.error('Failed to create cropped image:', error);
      toast.error('Failed to create cropped image');
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
          <DialogTitle>{title}</DialogTitle>
          <DialogDescription>{description}</DialogDescription>
        </DialogHeader>

        <ImageCropper
          srcImage={srcImage}
          aspectRatio={aspectRatio}
          onCropComplete={handleCropComplete}
        />

        <DialogFooter>
          <Button variant="outline" onClick={handleCancel} disabled={isLoading}>
            Cancel
          </Button>
          <Button onClick={handleConfirm} disabled={isLoading}>
            {isLoading ? 'Processing...' : 'Confirm Crop'}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
