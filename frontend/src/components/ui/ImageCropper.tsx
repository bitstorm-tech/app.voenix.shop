import { type CropArea } from '@/lib/image-utils';
import { useCallback, useState } from 'react';
import ReactCrop, { type Crop, type PixelCrop } from 'react-image-crop';
import 'react-image-crop/dist/ReactCrop.css';

interface ImageCropperProps {
  srcImage: string;
  aspectRatio?: number;
  onCropComplete: (croppedArea: CropArea, croppedAreaPixels: CropArea) => void;
  className?: string;
}

export default function ImageCropper({
  srcImage,
  aspectRatio,
  onCropComplete,
  className = '',
}: ImageCropperProps) {
  const [crop, setCrop] = useState<Crop>({
    unit: '%',
    width: 50,
    height: 50,
    x: 25,
    y: 25,
  });

  const handleCropComplete = useCallback(
    (crop: PixelCrop, percentCrop: Crop) => {
      // Convert PixelCrop to CropArea format for compatibility
      const croppedAreaPixels: CropArea = {
        x: crop.x,
        y: crop.y,
        width: crop.width,
        height: crop.height,
      };

      // Convert percentage crop to CropArea format
      const croppedArea: CropArea = {
        x: percentCrop.x,
        y: percentCrop.y,
        width: percentCrop.width,
        height: percentCrop.height,
      };

      onCropComplete(croppedArea, croppedAreaPixels);
    },
    [onCropComplete]
  );

  return (
    <div className={`space-y-4 ${className}`}>
      <div className="relative h-96">
        <ReactCrop
          crop={crop}
          onChange={(_, percentCrop) => setCrop(percentCrop)}
          onComplete={handleCropComplete}
          aspect={aspectRatio}
          keepSelection
        >
          <img src={srcImage} alt="Crop" className="max-w-full max-h-full" />
        </ReactCrop>
      </div>
    </div>
  );
}