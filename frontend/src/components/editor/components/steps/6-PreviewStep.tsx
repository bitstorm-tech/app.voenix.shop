import { useDebounce } from '@/hooks/useDebounce';
import { useWizardStore } from '@/stores/editor/useWizardStore';
import { CheckCircle } from 'lucide-react';
import { useState } from 'react';
import type { PixelCrop } from 'react-image-crop';
import { GeneratedImageCropData } from '../../types';
import ImageCropper from '../shared/ImageCropper';
import MugPreview from '../shared/MugPreview';

export default function PreviewStep() {
  const selectedMug = useWizardStore((state) => state.selectedMug);
  const selectedGeneratedImage = useWizardStore((state) => state.selectedGeneratedImage);
  const generatedImageCropData = useWizardStore((state) => state.generatedImageCropData);
  const userData = useWizardStore((state) => state.userData);
  const updateGeneratedImageCropData = useWizardStore((state) => state.updateGeneratedImageCropData);

  const [localCropData, setLocalCropData] = useState<GeneratedImageCropData | null>(generatedImageCropData);
  const debouncedCropData = useDebounce(localCropData, 200);

  if (!selectedMug || !selectedGeneratedImage) {
    return (
      <div className="py-8 text-center text-gray-500">
        <p>Missing required data. Please complete all previous steps.</p>
        <p className="mt-2 text-sm">
          {!selectedMug && 'Mug not selected. '}
          {!selectedGeneratedImage && 'Generated image not selected. '}
        </p>
      </div>
    );
  }

  const handleCropComplete = (pixelCrop: PixelCrop) => {
    const newCropData = {
      crop: { x: 0, y: 0 }, // Not used, but kept for type compatibility
      zoom: 1, // Not used, but kept for type compatibility
      croppedAreaPixels: pixelCrop,
    };
    setLocalCropData(newCropData);
    updateGeneratedImageCropData(newCropData);
  };

  const imageUrl = selectedGeneratedImage.startsWith('data:') ? selectedGeneratedImage : `/api/images/${selectedGeneratedImage}`;

  return (
    <div className="space-y-8">
      <div className="text-center">
        <div className="mb-4 flex justify-center">
          <CheckCircle className="h-12 w-12 text-green-500" />
        </div>
        <h3 className="mb-2 text-2xl font-bold">Perfect Your Design</h3>
        <p className="text-gray-600">Position and scale your design on the left, and see the live preview on the right</p>
      </div>

      {/* Side-by-side layout for desktop, stacked for mobile */}
      <div className="grid gap-8 lg:grid-cols-2">
        {/* Cropper on the left */}
        <div>
          <ImageCropper
            imageUrl={imageUrl}
            onCropComplete={handleCropComplete}
            mug={selectedMug}
            title="Position and scale your design"
            description="Drag to move your image and use the slider to zoom in or out"
            showGrid={false}
          />
        </div>

        {/* Preview on the right */}
        <div className="flex flex-col items-center justify-center">
          <MugPreview mug={selectedMug} imageUrl={imageUrl} cropData={debouncedCropData || generatedImageCropData} />
        </div>
      </div>

      <div className="mx-auto max-w-md space-y-4 rounded-lg bg-gray-50 p-6">
        <h4 className="font-semibold">Order Summary</h4>

        <div className="space-y-2 text-sm">
          <div className="flex justify-between">
            <span className="text-gray-600">Product:</span>
            <span className="font-medium">{selectedMug.name}</span>
          </div>
          <div className="flex justify-between">
            <span className="text-gray-600">Capacity:</span>
            <span className="font-medium">{selectedMug.capacity}</span>
          </div>
          {selectedMug.special && (
            <div className="flex justify-between">
              <span className="text-gray-600">Special Feature:</span>
              <span className="font-medium">{selectedMug.special}</span>
            </div>
          )}
          {userData && (
            <div className="flex justify-between">
              <span className="text-gray-600">Customer:</span>
              <span className="font-medium">
                {userData.firstName || userData.lastName ? `${userData.firstName || ''} ${userData.lastName || ''}`.trim() : userData.email}
              </span>
            </div>
          )}
        </div>

        <div className="border-t pt-4">
          <div className="flex justify-between text-lg font-semibold">
            <span>Total:</span>
            <span className="text-primary">${selectedMug.price}</span>
          </div>
        </div>
      </div>

      <p className="text-center text-sm text-gray-500">Your design has been saved and will be available in your account</p>
    </div>
  );
}
