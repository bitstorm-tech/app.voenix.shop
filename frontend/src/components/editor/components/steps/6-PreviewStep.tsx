import { useDebounce } from '@/hooks/useDebounce';
import { getCroppedImgFromArea } from '@/lib/imageCropUtils';
import { getLocaleCurrency } from '@/lib/locale';
import { useWizardStore } from '@/stores/editor/useWizardStore';
import { CheckCircle } from 'lucide-react';
import { useEffect, useMemo, useState } from 'react';
import { useTranslation } from 'react-i18next';
import type { PixelCrop } from 'react-image-crop';
import { GeneratedImageCropData } from '../../types';
import ImageCropper from '../shared/ImageCropper';

export default function PreviewStep() {
  const { t, i18n } = useTranslation('editor');
  const { locale, currency } = getLocaleCurrency(i18n.language);
  const currencyFormatter = useMemo(() => new Intl.NumberFormat(locale, { style: 'currency', currency }), [locale, currency]);
  const selectedMug = useWizardStore((state) => state.selectedMug);
  const selectedGeneratedImage = useWizardStore((state) => state.selectedGeneratedImage);
  const generatedImageCropData = useWizardStore((state) => state.generatedImageCropData);
  const userData = useWizardStore((state) => state.userData);
  const updateGeneratedImageCropData = useWizardStore((state) => state.updateGeneratedImageCropData);

  const [localCropData, setLocalCropData] = useState<GeneratedImageCropData | null>(generatedImageCropData);
  const debouncedCropData = useDebounce(localCropData, 200);
  const [previewUrl, setPreviewUrl] = useState<string | null>(null);
  const [isGeneratingPreview, setIsGeneratingPreview] = useState(false);

  const handleCropComplete = (pixelCrop: PixelCrop) => {
    const newCropData = {
      crop: { x: 0, y: 0 }, // Not used, but kept for type compatibility
      zoom: 1, // Not used, but kept for type compatibility
      croppedAreaPixels: pixelCrop,
    };
    setLocalCropData(newCropData);
    updateGeneratedImageCropData(newCropData);
  };

  // Handle different image URL formats:
  // - data: URLs are used as-is
  // - URLs starting with /api/ are already complete (e.g., /api/public/images/...)
  // - Otherwise, assume it's just a filename and construct the full URL
  const imageUrl = selectedGeneratedImage
    ? selectedGeneratedImage.startsWith('data:') || selectedGeneratedImage.startsWith('/api/')
      ? selectedGeneratedImage
      : `/api/images/${selectedGeneratedImage}`
    : null;

  const formatPrice = (value: number) => currencyFormatter.format(value);

  useEffect(() => {
    if (!imageUrl) {
      setPreviewUrl(null);
      setIsGeneratingPreview(false);
      return;
    }

    let isCancelled = false;

    const generatePreview = async () => {
      setIsGeneratingPreview(true);
      try {
        const crop = debouncedCropData?.croppedAreaPixels || generatedImageCropData?.croppedAreaPixels;

        if (crop) {
          const croppedImage = await getCroppedImgFromArea(imageUrl, crop);
          if (!isCancelled) {
            setPreviewUrl(croppedImage);
          }
        } else if (!isCancelled) {
          setPreviewUrl(imageUrl);
        }
      } catch (error) {
        console.error('Error generating preview image:', error);
        if (!isCancelled) {
          setPreviewUrl(imageUrl);
        }
      } finally {
        if (!isCancelled) {
          setIsGeneratingPreview(false);
        }
      }
    };

    generatePreview();

    return () => {
      isCancelled = true;
    };
  }, [imageUrl, debouncedCropData, generatedImageCropData]);

  if (!selectedMug || !selectedGeneratedImage || !imageUrl) {
    return (
      <div className="py-8 text-center text-gray-500">
        <p>{t('steps.preview.missing.title')}</p>
        <p className="mt-2 text-sm">
          {!selectedMug && `${t('steps.preview.missing.mug')} `}
          {!selectedGeneratedImage && `${t('steps.preview.missing.image')} `}
        </p>
      </div>
    );
  }

  return (
    <div className="space-y-8">
      <div className="text-center">
        <div className="mb-4 flex justify-center">
          <CheckCircle className="h-12 w-12 text-green-500" />
        </div>
        <h3 className="mb-2 text-2xl font-bold">{t('steps.preview.title')}</h3>
        <p className="text-gray-600">{t('steps.preview.subtitle')}</p>
      </div>

      {/* Side-by-side layout for desktop, stacked for mobile */}
      <div className="grid gap-8 lg:grid-cols-2">
        {/* Cropper on the left */}
        <div>
          <ImageCropper
            imageUrl={imageUrl}
            onCropComplete={handleCropComplete}
            mug={selectedMug}
            title={t('steps.preview.cropper.title')}
            description={t('steps.preview.cropper.description')}
            showGrid={false}
          />
        </div>

        {/* Preview on the right */}
        <div className="flex flex-col items-center justify-center">
          <div className="w-full max-w-md">
            <div className="aspect-square w-full overflow-hidden rounded-lg border border-gray-200 bg-gray-50 shadow-inner">
              {isGeneratingPreview ? (
                <div className="flex h-full items-center justify-center">
                  <div
                    className="border-t-primary h-12 w-12 animate-spin rounded-full border-4 border-gray-300"
                    aria-label={t('steps.preview.generatingPreview', { defaultValue: 'Generating preview…' })}
                  ></div>
                </div>
              ) : previewUrl ? (
                <img
                  src={previewUrl}
                  alt={t('steps.preview.previewAlt', { product: selectedMug.name, defaultValue: `${selectedMug.name} preview` })}
                  className="h-full w-full object-contain"
                />
              ) : (
                <div className="flex h-full items-center justify-center px-4 text-center text-sm text-gray-500">
                  {t('steps.preview.previewUnavailable', {
                    defaultValue: 'Preview will appear here once an image is selected.',
                  })}
                </div>
              )}
            </div>

            <div className="mt-6 text-center">
              <h4 className="text-lg font-semibold">{selectedMug.name}</h4>
              <p className="mt-1 text-sm text-gray-600">{selectedMug.capacity}</p>
              {selectedMug.special && (
                <span className="mt-2 inline-block rounded-full bg-yellow-100 px-3 py-1 text-sm font-medium text-yellow-800">
                  {selectedMug.special}
                </span>
              )}
            </div>
          </div>
        </div>
      </div>

      <div className="mx-auto max-w-md space-y-4 rounded-lg bg-gray-50 p-6">
        <h4 className="font-semibold">{t('steps.preview.summary.title')}</h4>

        <div className="space-y-2 text-sm">
          <div className="flex justify-between">
            <span className="text-gray-600">{t('steps.preview.summary.product')}</span>
            <span className="font-medium">{selectedMug.name}</span>
          </div>
          <div className="flex justify-between">
            <span className="text-gray-600">{t('steps.preview.summary.capacity')}</span>
            <span className="font-medium">{selectedMug.capacity}</span>
          </div>
          {selectedMug.special && (
            <div className="flex justify-between">
              <span className="text-gray-600">{t('steps.preview.summary.special')}</span>
              <span className="font-medium">{selectedMug.special}</span>
            </div>
          )}
          {userData && (
            <div className="flex justify-between">
              <span className="text-gray-600">{t('steps.preview.summary.customer')}</span>
              <span className="font-medium">
                {userData.firstName || userData.lastName ? `${userData.firstName || ''} ${userData.lastName || ''}`.trim() : userData.email}
              </span>
            </div>
          )}
        </div>

        <div className="border-t pt-4">
          <div className="flex justify-between text-lg font-semibold">
            <span>{t('steps.preview.summary.total')}</span>
            <span className="text-primary">{formatPrice(selectedMug.price)}</span>
          </div>
        </div>
      </div>

      <p className="text-center text-sm text-gray-500">{t('steps.preview.summary.saved')}</p>
    </div>
  );
}
