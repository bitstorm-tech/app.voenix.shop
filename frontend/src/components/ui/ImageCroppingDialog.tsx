import { Button } from '@/components/ui/Button';
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from '@/components/ui/Dialog';
import { Label } from '@/components/ui/Label';
import { Slider } from '@/components/ui/Slider';
import { createCroppedImage, type CropArea } from '@/lib/image-utils';
import { useCallback, useEffect, useState } from 'react';
import Cropper from 'react-easy-crop';
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

interface CropState {
  x: number;
  y: number;
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
  const [crop, setCrop] = useState<CropState>({ x: 0, y: 0 });
  const [zoom, setZoom] = useState(1);
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
      setCrop({ x: 0, y: 0 });
      setZoom(1);
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

        <div className="space-y-4">
          <div className="relative h-96">
            <Cropper
              image={srcImage}
              crop={crop}
              cropSize={{ width: 300, height: 300 }}
              zoom={zoom}
              aspect={aspectRatio}
              minZoom={0.5}
              restrictPosition={false}
              onCropChange={setCrop}
              onCropComplete={handleCropComplete}
              onZoomChange={setZoom}
              showGrid={true}
              cropShape="rect"
            />
          </div>

          <div className="space-y-2">
            <Label className="text-sm font-medium text-gray-700">Zoom: {Math.round(zoom * 100)}%</Label>
            <Slider value={[zoom]} onValueChange={(values) => setZoom(values[0])} min={0.5} max={3} step={0.01} className="w-full" />
            <div className="flex justify-between text-xs text-gray-500">
              <span>50%</span>
              <span>300%</span>
            </div>
          </div>
        </div>

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
