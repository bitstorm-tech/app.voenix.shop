import { useRef, useState } from 'react';
import ReactCrop, { centerCrop, makeAspectCrop, type Crop, type PixelCrop } from 'react-image-crop';
import 'react-image-crop/dist/ReactCrop.css';
import { MugOption } from '../../types';

interface ImageCropperProps {
  imageUrl: string;
  onCropComplete: (pixelCrop: PixelCrop) => void;
  aspect?: number;
  mug?: MugOption;
  showGrid?: boolean;
  cropShape?: 'rect' | 'round';
  height?: string;
  title?: string;
  description?: string;
  containerStyle?: React.CSSProperties;
}

export default function ImageCropper({
  imageUrl,
  onCropComplete,
  aspect,
  mug,
  showGrid = true,
  cropShape = 'rect',
  title,
  description,
}: ImageCropperProps) {
  const imgRef = useRef<HTMLImageElement>(null);
  const [crop, setCrop] = useState<Crop>();

  // Calculate aspect ratio from mug dimensions if provided
  const aspectRatio =
    mug?.print_template_width_mm && mug?.print_template_height_mm ? mug.print_template_width_mm / mug.print_template_height_mm : aspect;

  const onImageLoad = (e: React.SyntheticEvent<HTMLImageElement>) => {
    const { naturalWidth: width, naturalHeight: height } = e.currentTarget;

    // Calculate the maximum crop size that fits within the image
    // while maintaining the desired aspect ratio
    const imageAspectRatio = width / height;

    if (aspectRatio === undefined) {
      setCrop({
        x: 0,
        y: 0,
        width: 100,
        height: 100,
        unit: '%',
      });
      return;
    }

    let cropWidth: number;
    let cropHeight: number;

    if (imageAspectRatio > aspectRatio) {
      // Image is wider than the crop aspect ratio
      // Fit by height and calculate width
      cropHeight = 100; // 100% of image height
      cropWidth = (aspectRatio * height * 100) / width;
    } else {
      // Image is taller than the crop aspect ratio
      // Fit by width and calculate height
      cropWidth = 100; // 100% of image width
      cropHeight = (width * 100) / (aspectRatio * height);
    }

    // Ensure the crop doesn't exceed 100% in any dimension
    if (cropWidth > 100) {
      const scale = 100 / cropWidth;
      cropWidth = 100;
      cropHeight = cropHeight * scale;
    }
    if (cropHeight > 100) {
      const scale = 100 / cropHeight;
      cropHeight = 100;
      cropWidth = cropWidth * scale;
    }

    const crop = centerCrop(
      makeAspectCrop(
        {
          unit: '%',
          width: cropWidth,
          height: cropHeight,
        },
        aspectRatio,
        width,
        height,
      ),
      width,
      height,
    );

    setCrop(crop);
  };

  const handleCropChange = (_: Crop, percentCrop: Crop) => {
    setCrop(percentCrop);
  };

  const handleCropComplete = (pixelCrop: PixelCrop) => {
    onCropComplete(pixelCrop);
  };

  return (
    <>
      {(title || description || mug) && (
        <div className="mb-4 text-center">
          {title && <h3 className="text-lg font-semibold">{title}</h3>}
          {description && <p className="text-sm text-gray-600">{description}</p>}
          {mug && (
            <p className="mt-1 text-xs text-gray-500">
              The crop area matches your {mug.name}'s printable area ({mug.print_template_width_mm}mm × {mug.print_template_height_mm}mm)
            </p>
          )}
        </div>
      )}

      <ReactCrop
        crop={crop}
        onChange={handleCropChange}
        onComplete={handleCropComplete}
        aspect={aspectRatio}
        ruleOfThirds={showGrid}
        circularCrop={cropShape === 'round'}
        keepSelection={true}
      >
        <img ref={imgRef} onLoad={onImageLoad} src={imageUrl} alt="Crop preview" />
      </ReactCrop>
    </>
  );
}
