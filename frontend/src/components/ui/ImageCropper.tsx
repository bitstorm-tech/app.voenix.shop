import { Label } from '@/components/ui/Label';
import { Slider } from '@/components/ui/Slider';
import { type CropArea } from '@/lib/image-utils';
import { useCallback, useState } from 'react';
import Cropper from 'react-easy-crop';

interface CropState {
  x: number;
  y: number;
}

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
  const [crop, setCrop] = useState<CropState>({ x: 0, y: 0 });
  const [zoom, setZoom] = useState(1);

  const handleCropComplete = useCallback(
    (croppedArea: CropArea, croppedAreaPixels: CropArea) => {
      onCropComplete(croppedArea, croppedAreaPixels);
    },
    [onCropComplete]
  );

  return (
    <div className={`space-y-4 ${className}`}>
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
  );
}