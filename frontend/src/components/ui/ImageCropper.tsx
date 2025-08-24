import { type CropArea } from '@/lib/image-utils';
import { useCallback, useRef, useState } from 'react';
import ReactCrop, { type Crop } from 'react-image-crop';
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

  const calculateCropArea = useCallback(
    (percentCrop: Crop) => {
      const image = imageRef.current;
      if (!image) return;

      const scale = {
        x: image.naturalWidth / image.width,
        y: image.naturalHeight / image.height,
      };

      const pixelCrop: CropArea = {
        x: (percentCrop.x / 100) * image.width * scale.x,
        y: (percentCrop.y / 100) * image.height * scale.y,
        width: (percentCrop.width / 100) * image.width * scale.x,
        height: (percentCrop.height / 100) * image.height * scale.y,
      };

      onCropComplete(percentCrop as CropArea, pixelCrop);
    },
    [onCropComplete],
  );

  const handleImageLoad = useCallback(() => {
    // Calculate initial crop area when image loads
    calculateCropArea(crop);
  }, [calculateCropArea, crop]);

  return (
    <div className={`relative flex max-h-[60vh] items-center justify-center ${className}`.trim()}>
      <ReactCrop
        crop={crop}
        onChange={(_, percentCrop) => setCrop(percentCrop)}
        onComplete={(_, percentCrop) => calculateCropArea(percentCrop)}
        aspect={aspectRatio}
        keepSelection
        className="max-h-[60vh] max-w-full"
      >
        <img ref={imageRef} src={srcImage} alt="Crop" className="h-auto max-h-[60vh] w-auto max-w-full object-contain" onLoad={handleImageLoad} />
      </ReactCrop>
    </div>
  );
}
