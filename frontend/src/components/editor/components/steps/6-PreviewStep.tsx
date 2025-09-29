import { useDebounce } from '@/hooks/useDebounce';
import { getCroppedImgFromArea } from '@/lib/imageCropUtils';
import { getLocaleCurrency } from '@/lib/locale';
import { useWizardStore } from '@/stores/editor/useWizardStore';
import { useCallback, useEffect, useMemo, useRef, useState, type ChangeEvent } from 'react';
import { useTranslation } from 'react-i18next';
import type { PixelCrop } from 'react-image-crop';
import { GeneratedImageCropData } from '../../types';
import ImageCropper from '../shared/ImageCropper';

type FabricModule = typeof import('fabric');
type FabricCanvasInstance = InstanceType<FabricModule['Canvas']>;
type FabricTextboxInstance = InstanceType<FabricModule['Textbox']>;

const TEXT_FONTS = ['Arial', 'Helvetica', 'Times New Roman', 'Courier New', 'Georgia', 'Comic Sans MS'] as const;
const DEFAULT_FONT = TEXT_FONTS[0];
const DEFAULT_TEXT_COLOR = '#000000';

const isHexColor = (value: string) => /^#(?:[0-9a-fA-F]{3}){1,2}$/.test(value);

const resolveFontFamily = (font?: string): (typeof TEXT_FONTS)[number] => {
  if (!font) {
    return DEFAULT_FONT;
  }

  return (TEXT_FONTS as readonly string[]).includes(font) ? (font as (typeof TEXT_FONTS)[number]) : DEFAULT_FONT;
};

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

  const canvasElementRef = useRef<HTMLCanvasElement | null>(null);
  const fabricModuleRef = useRef<FabricModule | null>(null);
  const fabricCanvasRef = useRef<FabricCanvasInstance | null>(null);

  const [isFabricReady, setIsFabricReady] = useState(false);
  const [textValue, setTextValue] = useState('');
  const [textColor, setTextColor] = useState<string>(DEFAULT_TEXT_COLOR);
  const [fontFamily, setFontFamily] = useState<(typeof TEXT_FONTS)[number]>(DEFAULT_FONT);
  const [isTextSelected, setIsTextSelected] = useState(false);

  const handleCropComplete = (pixelCrop: PixelCrop) => {
    const newCropData = {
      crop: { x: 0, y: 0 }, // Not used, but kept for type compatibility
      zoom: 1, // Not used, but kept for type compatibility
      croppedAreaPixels: pixelCrop,
    };
    setLocalCropData(newCropData);
    updateGeneratedImageCropData(newCropData);
  };

  const imageUrl = selectedGeneratedImage
    ? selectedGeneratedImage.startsWith('data:') || selectedGeneratedImage.startsWith('/api/')
      ? selectedGeneratedImage
      : `/api/images/${selectedGeneratedImage}`
    : null;

  const formatPrice = (value: number) => currencyFormatter.format(value);

  const syncActiveTextboxSettings = useCallback(() => {
    const canvas = fabricCanvasRef.current;
    if (!canvas) {
      return;
    }

    const activeObject = canvas.getActiveObject();
    if (activeObject && activeObject.type === 'textbox') {
      const textbox = activeObject as FabricTextboxInstance;
      setTextValue(textbox.text ?? '');

      const fill = typeof textbox.fill === 'string' && isHexColor(textbox.fill) ? textbox.fill : DEFAULT_TEXT_COLOR;
      setTextColor(fill);
      setFontFamily(resolveFontFamily(textbox.fontFamily as string | undefined));
      setIsTextSelected(true);
    } else {
      setIsTextSelected(false);
    }
  }, []);

  const handleSelectionCleared = useCallback(() => {
    setIsTextSelected(false);
    setTextValue('');
  }, []);

  useEffect(() => {
    let isMounted = true;

    const setupFabric = async () => {
      try {
        const module: FabricModule = await import('fabric');
        if (!isMounted || !canvasElementRef.current) {
          return;
        }

        fabricModuleRef.current = module;

        const canvas = new module.Canvas(canvasElementRef.current, {
          preserveObjectStacking: true,
          selection: true,
        });

        canvas.selectionColor = 'rgba(79, 70, 229, 0.08)';
        canvas.selectionBorderColor = '#4f46e5';
        canvas.hoverCursor = 'move';

        fabricCanvasRef.current = canvas;

        canvas.on('selection:created', syncActiveTextboxSettings);
        canvas.on('selection:updated', syncActiveTextboxSettings);
        canvas.on('selection:cleared', handleSelectionCleared);
        canvas.on('object:modified', syncActiveTextboxSettings);

        setIsFabricReady(true);
      } catch (error) {
        console.error('Failed to initialize fabric.js', error);
      }
    };

    setupFabric();

    return () => {
      isMounted = false;
      const canvas = fabricCanvasRef.current;
      if (canvas) {
        canvas.off('selection:created', syncActiveTextboxSettings);
        canvas.off('selection:updated', syncActiveTextboxSettings);
        canvas.off('selection:cleared', handleSelectionCleared);
        canvas.off('object:modified', syncActiveTextboxSettings);
        canvas.dispose();
        fabricCanvasRef.current = null;
      }
      fabricModuleRef.current = null;
    };
  }, [handleSelectionCleared, syncActiveTextboxSettings]);

  useEffect(() => {
    const canvas = fabricCanvasRef.current;
    const fabricModule = fabricModuleRef.current;

    if (!canvas || !fabricModule) {
      return;
    }

    if (!previewUrl) {
      canvas.discardActiveObject();
      canvas.getObjects().forEach((object) => canvas.remove(object));
      canvas.backgroundImage = undefined;
      canvas.requestRenderAll();
      setIsTextSelected(false);
      setTextValue('');
      return;
    }

    let isCancelled = false;

    const loadBackgroundImage = async () => {
      try {
        const image = await fabricModule.Image.fromURL(previewUrl, {
          crossOrigin: 'anonymous',
        });

        if (isCancelled) {
          return;
        }

        const imageWidth = image.width ?? 400;
        const imageHeight = image.height ?? 400;

        // Scale image to fit within container (max-w-2xl = 672px)
        const maxWidth = 672;
        const scale = imageWidth > maxWidth ? maxWidth / imageWidth : 1;
        const scaledWidth = Math.round(imageWidth * scale);
        const scaledHeight = Math.round(imageHeight * scale);

        canvas.setDimensions({ width: scaledWidth, height: scaledHeight });

        image.scaleToWidth(scaledWidth);
        image.top = 0;
        image.left = 0;
        image.originX = 'left';
        image.originY = 'top';

        canvas.backgroundImage = image;
        canvas.requestRenderAll();
      } catch (error) {
        console.error('Failed to load preview image', error);
      }
    };

    loadBackgroundImage();

    return () => {
      isCancelled = true;
    };
  }, [previewUrl]);

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

  const handleTextInputChange = useCallback((event: ChangeEvent<HTMLInputElement>) => {
    const value = event.target.value;
    setTextValue(value);

    const canvas = fabricCanvasRef.current;
    if (!canvas) {
      return;
    }

    const activeObject = canvas.getActiveObject();
    if (activeObject && activeObject.type === 'textbox') {
      (activeObject as FabricTextboxInstance).set('text', value);
      canvas.requestRenderAll();
    }
  }, []);

  const handleColorChange = useCallback((event: ChangeEvent<HTMLInputElement>) => {
    const value = event.target.value;
    setTextColor(value);

    const canvas = fabricCanvasRef.current;
    if (!canvas) {
      return;
    }

    const activeObject = canvas.getActiveObject();
    if (activeObject && activeObject.type === 'textbox') {
      (activeObject as FabricTextboxInstance).set('fill', value);
      canvas.requestRenderAll();
    }
  }, []);

  const handleFontChange = useCallback((event: ChangeEvent<HTMLSelectElement>) => {
    const selectedFont = resolveFontFamily(event.target.value);
    setFontFamily(selectedFont);

    const canvas = fabricCanvasRef.current;
    if (!canvas) {
      return;
    }

    const activeObject = canvas.getActiveObject();
    if (activeObject && activeObject.type === 'textbox') {
      (activeObject as FabricTextboxInstance).set('fontFamily', selectedFont);
      canvas.requestRenderAll();
    }
  }, []);

  const handleAddText = useCallback(() => {
    const canvas = fabricCanvasRef.current;
    const fabricModule = fabricModuleRef.current;

    if (!canvas || !fabricModule || !previewUrl) {
      return;
    }

    const content =
      textValue.trim() ||
      t('steps.preview.text.defaultLabel', {
        defaultValue: 'Your text',
      });

    const width = canvas.getWidth() || 400;
    const height = canvas.getHeight() || 400;
    const baseSize = Math.max(Math.min(Math.round(Math.min(width, height) / 6), 72), 24);

    const textbox = new fabricModule.Textbox(content, {
      fill: textColor,
      fontFamily,
      fontSize: baseSize,
      originX: 'center',
      originY: 'center',
      left: width / 2,
      top: height / 2,
      editable: true,
      centeredScaling: true,
      borderColor: '#4f46e5',
      cornerColor: '#4f46e5',
      transparentCorners: false,
    });

    canvas.add(textbox);
    canvas.setActiveObject(textbox);
    canvas.bringObjectToFront(textbox);
    canvas.requestRenderAll();
    syncActiveTextboxSettings();
  }, [fontFamily, previewUrl, syncActiveTextboxSettings, t, textColor, textValue]);

  const hasPreview = Boolean(previewUrl);
  const shouldShowLoader = (hasPreview && !isFabricReady) || isGeneratingPreview;
  const canAddText = Boolean(previewUrl && isFabricReady && !isGeneratingPreview);

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
        <h3 className="mb-2 text-2xl font-bold">{t('steps.preview.title')}</h3>
        <p className="text-gray-600">{t('steps.preview.subtitle')}</p>
      </div>

      <div className="space-y-8">
        <div className="mx-auto w-full max-w-4xl">
          <ImageCropper
            imageUrl={imageUrl}
            onCropComplete={handleCropComplete}
            mug={selectedMug}
            title={t('steps.preview.cropper.title')}
            description={t('steps.preview.cropper.description')}
            showGrid={false}
          />
        </div>

        <div className="flex flex-col items-center">
          <div className="w-full max-w-2xl space-y-6">
            <div className="relative min-h-[280px] w-full overflow-hidden rounded-lg border border-gray-200 bg-gray-50 shadow-inner">
              <canvas ref={canvasElementRef} className={`h-auto w-full ${hasPreview && isFabricReady ? 'block' : 'hidden'}`} />

              {!hasPreview && !isGeneratingPreview && (
                <div className="flex h-full min-h-[280px] items-center justify-center px-4 text-center text-sm text-gray-500">
                  {t('steps.preview.previewUnavailable', {
                    defaultValue: 'Preview will appear here once an image is selected.',
                  })}
                </div>
              )}

              {shouldShowLoader && (
                <div className="absolute inset-0 flex items-center justify-center bg-white/70">
                  <div
                    className="border-t-primary h-12 w-12 animate-spin rounded-full border-4 border-gray-300"
                    aria-label={t('steps.preview.generatingPreview', { defaultValue: 'Generating preview…' })}
                  ></div>
                </div>
              )}
            </div>

            <div className="rounded-lg border border-gray-200 bg-white p-4 shadow-sm">
              <h5 className="text-base font-semibold">{t('steps.preview.text.title', { defaultValue: 'Add custom text' })}</h5>
              <p className="mt-1 text-sm text-gray-600">
                {isTextSelected
                  ? t('steps.preview.text.editHint', {
                      defaultValue: 'You are editing the selected text. Drag the handles to move, resize, or rotate it.',
                    })
                  : t('steps.preview.text.addHint', {
                      defaultValue: 'Set the style and add text to the preview. You can place multiple text boxes.',
                    })}
              </p>

              <div className="mt-4 flex flex-col gap-4 sm:flex-row sm:items-end">
                <label className="flex-1 text-sm font-medium text-gray-700">
                  {t('steps.preview.text.label', { defaultValue: 'Text' })}
                  <input
                    type="text"
                    value={textValue}
                    onChange={handleTextInputChange}
                    placeholder={t('steps.preview.text.placeholder', { defaultValue: 'Enter custom text' })}
                    className="focus:border-primary focus:ring-primary/50 mt-1 w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:ring-2 focus:outline-none"
                  />
                </label>

                <label className="text-sm font-medium text-gray-700">
                  {t('steps.preview.text.color', { defaultValue: 'Color' })}
                  <input
                    type="color"
                    value={textColor}
                    onChange={handleColorChange}
                    className="mt-1 h-10 w-16 cursor-pointer rounded-md border border-gray-300 bg-white p-1"
                  />
                </label>

                <label className="text-sm font-medium text-gray-700">
                  {t('steps.preview.text.font', { defaultValue: 'Font' })}
                  <select
                    value={fontFamily}
                    onChange={handleFontChange}
                    className="focus:border-primary focus:ring-primary/50 mt-1 w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:ring-2 focus:outline-none"
                  >
                    {TEXT_FONTS.map((fontOption) => (
                      <option key={fontOption} value={fontOption}>
                        {fontOption}
                      </option>
                    ))}
                  </select>
                </label>

                <button
                  type="button"
                  onClick={handleAddText}
                  disabled={!canAddText}
                  className="bg-primary hover:bg-primary/90 inline-flex h-10 items-center justify-center rounded-md px-4 text-sm font-semibold text-white transition disabled:cursor-not-allowed disabled:bg-gray-300"
                >
                  {isTextSelected
                    ? t('steps.preview.text.addAnother', { defaultValue: 'Add another text' })
                    : t('steps.preview.text.add', { defaultValue: 'Add text' })}
                </button>
              </div>
            </div>

            <div className="text-center">
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
