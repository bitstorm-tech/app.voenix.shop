import { Button } from '@/components/ui/Button';
import ImageCropperDialog from '@/components/ui/ImageCropperDialog';
import { cleanupBlobUrls, type CropArea, isValidImageFile, isValidImageSize } from '@/lib/image-utils';
import { cn } from '@/lib/utils';
import { useWizardStore } from '@/stores/editor/useWizardStore';
import { Upload, X } from 'lucide-react';
import React, { useEffect, useRef, useState } from 'react';
import { toast } from 'sonner';

export default function ImageUploadStep() {
  const uploadImage = useWizardStore((state) => state.uploadImage);
  const cropImage = useWizardStore((state) => state.cropImage);
  const removeImage = useWizardStore((state) => state.removeImage);
  const uploadedImageUrl = useWizardStore((state) => state.uploadedImageUrl);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const [showCropper, setShowCropper] = useState(false);
  const [originalImageUrl, setOriginalImageUrl] = useState<string | null>(null);
  const [imageFile, setImageFile] = useState<File | null>(null);

  const handleFileUpload = (file: File) => {
    if (!isValidImageFile(file)) {
      toast.error('Please upload an image file');
      return;
    }

    if (!isValidImageSize(file, 4)) {
      toast.error('File size exceeds maximum allowed size of 4MB');
      return;
    }

    // Clean up previous blob URLs
    cleanupBlobUrls([originalImageUrl]);

    const blobUrl = URL.createObjectURL(file);
    setImageFile(file);
    setOriginalImageUrl(blobUrl);
    setShowCropper(true);
  };

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    handleFileUpload(file);

    if (fileInputRef.current) {
      fileInputRef.current.value = '';
    }
  };

  const handleDragOver = (e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
  };

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();

    const file = e.dataTransfer.files[0];
    if (!file) return;

    handleFileUpload(file);
  };

  const handleCropCancel = () => {
    setShowCropper(false);
    cleanupBlobUrls([originalImageUrl]);
    setOriginalImageUrl(null);
    setImageFile(null);
  };

  const handleCropConfirm = async (croppedImageUrl: string, croppedAreaPixels: CropArea) => {
    if (imageFile) {
      try {
        // Upload image to the store
        uploadImage(imageFile, croppedImageUrl);

        // Pass the crop data to the store
        cropImage({
          crop: { x: 0, y: 0 }, // Reset crop position since we have the final cropped image
          zoom: 1,
          croppedAreaPixels,
        });

        // Clean up original image URL
        if (originalImageUrl) {
          URL.revokeObjectURL(originalImageUrl);
        }
        setOriginalImageUrl(null);
        setImageFile(null);
      } catch (error) {
        console.error('Failed to process cropped image:', error);
        toast.error('Failed to process image');
      }
    }
  };

  const handleRemoveImage = () => {
    cleanupBlobUrls([originalImageUrl, uploadedImageUrl]);
    setOriginalImageUrl(null);
    setImageFile(null);
    removeImage();
  };

  // Cleanup on unmount
  useEffect(() => {
    return () => {
      cleanupBlobUrls([originalImageUrl, uploadedImageUrl]);
    };
  }, [originalImageUrl, uploadedImageUrl]);

  return (
    <>
      <ImageCropperDialog
        open={showCropper && !!originalImageUrl}
        onOpenChange={(open) => !open && handleCropCancel()}
        srcImage={originalImageUrl || ''}
        onConfirm={handleCropConfirm}
        title="Crop your image"
        description="Select the area you want to use for your mug design"
      />

      {!uploadedImageUrl ? (
        <div className="flex flex-col items-center justify-center space-y-4">
          <div
            onDragOver={handleDragOver}
            onDrop={handleDrop}
            className={cn(
              'relative h-64 w-full max-w-md cursor-pointer rounded-lg border-2 border-dashed border-gray-300',
              'flex flex-col items-center justify-center bg-gray-50 transition-colors hover:bg-gray-100',
            )}
            onClick={() => fileInputRef.current?.click()}
          >
            <Upload className="mb-4 h-12 w-12 text-gray-400" />
            <p className="mb-2 text-sm font-medium text-gray-700">Click to upload or drag and drop</p>
            <p className="text-xs text-gray-500">PNG, JPG, GIF, WEBP up to 4MB</p>

            <input ref={fileInputRef} type="file" accept="image/*" onChange={handleFileChange} className="hidden" />
          </div>

          <p className="text-center text-sm text-gray-600">Upload an image to get started with your personalized mug design</p>
        </div>
      ) : (
        <div className="space-y-4">
          <div className="flex items-center justify-between">
            <h3 className="text-lg font-semibold">Your Cropped Image</h3>
            <Button variant="ghost" size="sm" onClick={handleRemoveImage} className="text-red-600 hover:text-red-700">
              <X className="mr-2 h-4 w-4" />
              Remove Image
            </Button>
          </div>

          <div className="flex justify-center">
            <img src={uploadedImageUrl} alt="Cropped preview" className="h-auto max-h-96 w-auto max-w-full rounded border" />
          </div>

          <p className="text-center text-sm text-gray-600">Your image is ready! You can continue to the next step or upload a different image.</p>
        </div>
      )}
    </>
  );
}
