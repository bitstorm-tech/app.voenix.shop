import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import { Label } from '@/components/ui/Label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/Select';
import { ToggleGroup, ToggleGroupItem } from '@/components/ui/ToggleGroup';
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
  const selectedMug = useWizardStore((state) => state.selectedMug);
  const selectedVariant = useWizardStore((state) => state.selectedVariant);
  const selectedGeneratedImage = useWizardStore((state) => state.selectedGeneratedImage);
  const generatedImageCropData = useWizardStore((state) => state.generatedImageCropData);
  const updateGeneratedImageCropData = useWizardStore((state) => state.updateGeneratedImageCropData);

  const { locale, currency } = getLocaleCurrency(i18n.language);
  const currencyFormatter = useMemo(() => new Intl.NumberFormat(locale, { style: 'currency', currency }), [locale, currency]);

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
  const [isBold, setIsBold] = useState(false);
  const [isItalic, setIsItalic] = useState(false);
  const [isUnderline, setIsUnderline] = useState(false);

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
      setIsBold(textbox.fontWeight === 'bold');
      setIsItalic(textbox.fontStyle === 'italic');
      setIsUnderline(textbox.underline === true);
      setIsTextSelected(true);
    } else {
      setIsTextSelected(false);
    }
  }, []);

  const handleSelectionCleared = useCallback(() => {
    setIsTextSelected(false);
    setTextValue('');
    setIsBold(false);
    setIsItalic(false);
    setIsUnderline(false);
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

        // Always scale image to fill container width (max-w-4xl = 896px)
        const containerWidth = 896;
        const scale = containerWidth / imageWidth;
        const scaledWidth = containerWidth;
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

  useEffect(() => {
    if (typeof window === 'undefined') {
      return;
    }

    const canvas = fabricCanvasRef.current;
    if (!canvas || !isFabricReady) {
      return;
    }

    // Keep Fabric's hit testing in sync after toggling visibility or resizing the container.
    const recalcOffsets = () => {
      canvas.calcOffset();
      canvas.requestRenderAll();
    };

    if (previewUrl) {
      recalcOffsets();
    }

    window.addEventListener('resize', recalcOffsets);
    return () => {
      window.removeEventListener('resize', recalcOffsets);
    };
  }, [isFabricReady, previewUrl]);

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

  const handleFontChange = useCallback((value: string) => {
    const selectedFont = resolveFontFamily(value);
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

  const handleFormattingChange = useCallback((values: string[]) => {
    const newBoldState = values.includes('bold');
    const newItalicState = values.includes('italic');
    const newUnderlineState = values.includes('underline');

    setIsBold(newBoldState);
    setIsItalic(newItalicState);
    setIsUnderline(newUnderlineState);

    const canvas = fabricCanvasRef.current;
    if (!canvas) {
      return;
    }

    const activeObject = canvas.getActiveObject();
    if (activeObject && activeObject.type === 'textbox') {
      const textbox = activeObject as FabricTextboxInstance;
      textbox.set('fontWeight', newBoldState ? 'bold' : 'normal');
      textbox.set('fontStyle', newItalicState ? 'italic' : 'normal');
      textbox.set('underline', newUnderlineState);
      canvas.requestRenderAll();
    }
  }, []);

  const handleAddText = useCallback(() => {
    const canvas = fabricCanvasRef.current;
    const fabricModule = fabricModuleRef.current;

    if (!canvas || !fabricModule || !previewUrl) {
      return;
    }

    const content = textValue.trim() || t('steps.preview.text.defaultLabel');

    const width = canvas.getWidth() || 400;
    const height = canvas.getHeight() || 400;
    const baseSize = Math.max(Math.min(Math.round(Math.min(width, height) / 6), 72), 24);

    const textbox = new fabricModule.Textbox(content, {
      fill: textColor,
      fontFamily,
      fontSize: baseSize,
      fontWeight: isBold ? 'bold' : 'normal',
      fontStyle: isItalic ? 'italic' : 'normal',
      underline: isUnderline,
      originX: 'center',
      originY: 'center',
      left: width / 2,
      top: height / 2,
      editable: true,
      centeredScaling: true,
      borderColor: '#4f46e5',
      cornerColor: '#4f46e5',
      transparentCorners: false,
      snapAngle: 90,
      snapThreshold: 5,
    });

    canvas.add(textbox);
    canvas.setActiveObject(textbox);
    canvas.bringObjectToFront(textbox);
    canvas.requestRenderAll();
    syncActiveTextboxSettings();
  }, [fontFamily, isBold, isItalic, isUnderline, previewUrl, syncActiveTextboxSettings, t, textColor, textValue]);

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
        <h3 className="mb-1 text-xl font-bold">{t('steps.preview.title')}</h3>
        <p className="text-sm text-gray-600">{t('steps.preview.subtitle')}</p>
      </div>

      <div className="space-y-8">
        <div className="mx-auto flex w-full max-w-6xl flex-col gap-6 md:flex-row">
          <div className="flex-1">
            <ImageCropper
              imageUrl={imageUrl}
              onCropComplete={handleCropComplete}
              mug={selectedMug}
              title={t('steps.preview.cropper.title')}
              description={t('steps.preview.cropper.description')}
              showGrid={false}
            />
          </div>

          <div className="flex flex-col gap-4 md:w-1/3">
            <div className="overflow-hidden rounded-lg border border-gray-200 shadow-md">
              <img src={selectedMug.image} alt={selectedMug.name} className="h-auto w-full object-contain" style={{ maxHeight: '500px' }} />
            </div>

            <div className="rounded-lg border border-gray-200 bg-white p-4 shadow-sm">
              <h4 className="mb-3 text-lg font-bold text-gray-900">{selectedMug.name}</h4>
              <div className="space-y-2 text-sm">
                <div className="flex items-center justify-between">
                  <span className="text-gray-600">{t('steps.preview.mugInfo.price')}:</span>
                  <span className="font-semibold text-gray-900">{currencyFormatter.format(selectedMug.price)}</span>
                </div>
                {selectedMug.filling_quantity && (
                  <div className="flex items-center justify-between">
                    <span className="text-gray-600">{t('steps.preview.mugInfo.capacity')}:</span>
                    <span className="text-gray-900">{selectedMug.filling_quantity}</span>
                  </div>
                )}
                {selectedVariant && (
                  <div className="flex items-center justify-between border-t border-gray-100 pt-2">
                    <span className="text-gray-600">{t('steps.preview.mugInfo.color')}:</span>
                    <div className="flex items-center gap-2">
                      <span className="text-gray-900">{selectedVariant.name}</span>
                      <div
                        className="h-5 w-5 rounded-full border border-gray-300 shadow-sm"
                        style={{ backgroundColor: selectedVariant.colorCode }}
                        title={selectedVariant.colorCode}
                      />
                    </div>
                  </div>
                )}
              </div>
            </div>
          </div>
        </div>

        <div className="flex flex-col items-center">
          <div className="w-full max-w-4xl space-y-6">
            <div className="relative min-h-[280px] w-full overflow-hidden border border-gray-200 bg-gray-50 shadow-inner">
              <canvas ref={canvasElementRef} className={`h-auto w-full ${hasPreview && isFabricReady ? 'block' : 'hidden'}`} />

              {!hasPreview && !isGeneratingPreview && (
                <div className="flex h-full min-h-[280px] items-center justify-center px-4 text-center text-sm text-gray-500">
                  {t('steps.preview.previewUnavailable')}
                </div>
              )}

              {shouldShowLoader && (
                <div className="absolute inset-0 flex items-center justify-center bg-white/70">
                  <div
                    className="border-t-primary h-12 w-12 animate-spin rounded-full border-4 border-gray-300"
                    aria-label={t('steps.preview.generatingPreview')}
                  ></div>
                </div>
              )}
            </div>

            <div className="rounded-lg border border-gray-200 bg-white p-4 shadow-sm">
              <h5 className="text-base font-semibold">{t('steps.preview.text.title')}</h5>
              <p className="mt-1 text-sm text-gray-600">{isTextSelected ? t('steps.preview.text.editHint') : t('steps.preview.text.addHint')}</p>

              <div className="mt-4 flex flex-col gap-4 sm:flex-row">
                <div className="flex flex-1 flex-col gap-1.5">
                  <Label htmlFor="text-input">{t('steps.preview.text.label')}</Label>
                  <Input
                    id="text-input"
                    type="text"
                    value={textValue}
                    onChange={handleTextInputChange}
                    placeholder={t('steps.preview.text.placeholder')}
                  />
                </div>

                <div className="flex flex-col gap-1.5">
                  <Label htmlFor="color-input">{t('steps.preview.text.color')}</Label>
                  <input
                    id="color-input"
                    type="color"
                    value={textColor}
                    onChange={handleColorChange}
                    className="h-9 w-16 cursor-pointer rounded-md border border-gray-300 bg-white p-1"
                  />
                </div>

                <div className="flex flex-col gap-1.5">
                  <Label htmlFor="font-select">{t('steps.preview.text.font')}</Label>
                  <Select value={fontFamily} onValueChange={handleFontChange}>
                    <SelectTrigger id="font-select" className="w-[180px]">
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      {TEXT_FONTS.map((fontOption) => (
                        <SelectItem key={fontOption} value={fontOption} style={{ fontFamily: fontOption }}>
                          {fontOption}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>

                <div className="flex flex-col gap-1.5">
                  <Label htmlFor="formatting-buttons">{t('steps.preview.text.formatting')}</Label>
                  <ToggleGroup
                    id="formatting-buttons"
                    type="multiple"
                    value={[...(isBold ? ['bold'] : []), ...(isItalic ? ['italic'] : []), ...(isUnderline ? ['underline'] : [])]}
                    onValueChange={handleFormattingChange}
                    variant="outline"
                  >
                    <ToggleGroupItem
                      value="bold"
                      aria-label={t('steps.preview.text.bold')}
                      className="font-bold"
                      disabled={!canAddText && !isTextSelected}
                    >
                      B
                    </ToggleGroupItem>
                    <ToggleGroupItem
                      value="italic"
                      aria-label={t('steps.preview.text.italic')}
                      className="font-serif italic"
                      disabled={!canAddText && !isTextSelected}
                    >
                      I
                    </ToggleGroupItem>
                    <ToggleGroupItem
                      value="underline"
                      aria-label={t('steps.preview.text.underline')}
                      className="underline"
                      disabled={!canAddText && !isTextSelected}
                    >
                      U
                    </ToggleGroupItem>
                  </ToggleGroup>
                </div>

                <div className="flex flex-col gap-1.5">
                  <Label className="opacity-0">Action</Label>
                  <Button type="button" onClick={handleAddText} disabled={!canAddText} size="lg">
                    {isTextSelected ? t('steps.preview.text.addAnother') : t('steps.preview.text.add')}
                  </Button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      <p className="text-center text-sm text-gray-500">{t('steps.preview.summary.saved')}</p>
    </div>
  );
}
