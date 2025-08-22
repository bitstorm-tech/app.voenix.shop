import { type CropArea } from '@/lib/image-utils';
import { useCallback, useRef, useState } from 'react';
import ReactCrop, { type Crop, type PixelCrop } from 'react-image-crop';
import 'react-image-crop/dist/ReactCrop.css';

interface ImageCropperProps {
  srcImage: string;
  aspectRatio?: number;
  onCropComplete: (croppedArea: CropArea, croppedAreaPixels: CropArea) => void;
  className?: string;
}

export default function ImageCropper({ srcImage, aspectRatio, onCropComplete, className = '' }: ImageCropperProps) {
  const imageRef = useRef<HTMLImageElement>(null);
  const [crop, setCrop] = useState<Crop>({
    unit: '%',
    width: 80,
    height: 80,
    x: 10,
    y: 10,
  });

  const handleCropComplete = useCallback(
    (crop: PixelCrop, percentCrop: Crop) => {
      if (!imageRef.current) return;

      const image = imageRef.current;
      const scaleX = image.naturalWidth / image.width;
      const scaleY = image.naturalHeight / image.height;

      // Scale the pixel crop coordinates to match the natural image size
      const croppedAreaPixels: CropArea = {
        x: crop.x * scaleX,
        y: crop.y * scaleY,
        width: crop.width * scaleX,
        height: crop.height * scaleY,
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
    [onCropComplete],
  );

  return (
    <div className={`space-y-4 ${className}`}>
      <div className="relative max-h-[60vh] overflow-auto">
        <ReactCrop crop={crop} onChange={(_, percentCrop) => setCrop(percentCrop)} onComplete={handleCropComplete} aspect={aspectRatio} keepSelection>
          <img ref={imageRef} src={srcImage} alt="Crop" className="h-auto max-w-full object-contain" />
        </ReactCrop>
      </div>
    </div>
  );
}
