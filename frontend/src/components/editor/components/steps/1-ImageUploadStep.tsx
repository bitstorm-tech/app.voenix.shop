import { Button } from '@/components/ui/Button';
import { Label } from '@/components/ui/Label';
import { Slider } from '@/components/ui/Slider';
import { cleanupBlobUrls, createCroppedImage, isValidImageFile, isValidImageSize } from '@/lib/image-utils';
import { cn } from '@/lib/utils';
import { useWizardStore } from '@/stores/editor/useWizardStore';
import { Upload, X } from 'lucide-react';
import React, { useCallback, useEffect, useRef, useState } from 'react';
import Cropper from 'react-easy-crop';
import { toast } from 'sonner';

export default function ImageUploadStep() {
  const uploadImage = useWizardStore((state) => state.uploadImage);
  const cropImage = useWizardStore((state) => state.cropImage);
  const removeImage = useWizardStore((state) => state.removeImage);
  const uploadedImageUrl = useWizardStore((state) => state.uploadedImageUrl);
  const fileInputRef = useRef<HTMLInputElement>(null);

  // New state for react-easy-crop
  const [showCropper, setShowCropper] = useState(false);
  const [crop, setCrop] = useState({ x: 0, y: 0 });
  const [zoom, setZoom] = useState(1);
  const [croppedAreaPixels, setCroppedAreaPixels] = useState<{
    x: number;
    y: number;
    width: number;
    height: number;
  } | null>(null);
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
    setCrop({ x: 0, y: 0 });
    setZoom(1);
    setCroppedAreaPixels(null);
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

  const handleCropComplete = useCallback(
    (
      _croppedArea: { x: number; y: number; width: number; height: number },
      croppedAreaPixels: { x: number; y: number; width: number; height: number },
    ) => {
      setCroppedAreaPixels(croppedAreaPixels);
    },
    [],
  );

  const handleCropCancel = () => {
    setShowCropper(false);
    setCrop({ x: 0, y: 0 });
    setZoom(1);
    setCroppedAreaPixels(null);
    cleanupBlobUrls([originalImageUrl]);
    setOriginalImageUrl(null);
    setImageFile(null);
  };

  const handleCropConfirm = async () => {
    if (croppedAreaPixels && originalImageUrl && imageFile) {
      try {
        // Create cropped image preview
        const croppedUrl = await createCroppedImage(originalImageUrl, croppedAreaPixels);

        // Upload image to the store
        uploadImage(imageFile, croppedUrl);

        // Pass the crop data to the store
        cropImage({
          crop: { x: crop.x, y: crop.y },
          zoom,
          croppedAreaPixels,
        });

        // Clean up original image URL but keep the cropped one
        if (originalImageUrl !== croppedUrl) {
          URL.revokeObjectURL(originalImageUrl);
        }
        setOriginalImageUrl(null);
        setImageFile(null);
      } catch (error) {
        console.error('Failed to create cropped preview:', error);
        toast.error('Failed to create image preview');
      }
    }
    setShowCropper(false);
  };

  const handleRemoveImage = () => {
    cleanupBlobUrls([originalImageUrl, uploadedImageUrl]);
    setOriginalImageUrl(null);
    setImageFile(null);
    setCrop({ x: 0, y: 0 });
    setZoom(1);
    setCroppedAreaPixels(null);
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
      {/* Image Cropper Modal */}
      {showCropper && originalImageUrl && (
        <div className="bg-opacity-50 fixed inset-0 z-50 flex items-center justify-center bg-black">
          <div className="w-full max-w-2xl rounded-lg bg-white p-6">
            <div className="mb-4 flex items-center justify-between">
              <h3 className="text-lg font-semibold">Crop Image</h3>
              <Button variant="ghost" size="sm" onClick={handleCropCancel}>
                <X className="h-4 w-4" />
              </Button>
            </div>
            <div className="mb-4">
              <div className="mb-4 text-center">
                <h3 className="text-lg font-semibold">Crop your image</h3>
                <p className="text-sm text-gray-600">Select the area you want to use for your mug design</p>
              </div>
              <div className="relative h-96">
                <Cropper
                  image={originalImageUrl}
                  crop={crop}
                  cropSize={{ width: 300, height: 300 }}
                  zoom={zoom}
                  aspect={1}
                  minZoom={0.5}
                  restrictPosition={false}
                  onCropChange={setCrop}
                  onCropComplete={handleCropComplete}
                  onZoomChange={setZoom}
                  showGrid={true}
                  cropShape="rect"
                />
              </div>
              <div className="mt-4 space-y-2">
                <Label className="text-sm font-medium text-gray-700">Zoom: {Math.round(zoom * 100)}%</Label>
                <Slider value={[zoom]} onValueChange={(values) => setZoom(values[0])} min={0.5} max={3} step={0.1} className="w-full" />
                <div className="flex justify-between text-xs text-gray-500">
                  <span>50%</span>
                  <span>300%</span>
                </div>
              </div>
            </div>
            <div className="flex justify-end gap-2">
              <Button variant="outline" onClick={handleCropCancel}>
                Cancel
              </Button>
              <Button onClick={handleCropConfirm}>Confirm Crop</Button>
            </div>
          </div>
        </div>
      )}

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
            <img src={uploadedImageUrl} alt="Cropped preview" className="h-64 w-64 rounded border object-cover" />
          </div>

          <p className="text-center text-sm text-gray-600">Your image is ready! You can continue to the next step or upload a different image.</p>
        </div>
      )}
    </>
  );
}
