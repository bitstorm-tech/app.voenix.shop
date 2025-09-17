import { Accordion, AccordionContent, AccordionItem, AccordionTrigger } from '@/components/ui/Accordion';
import { Button } from '@/components/ui/Button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/Card';
import { Label } from '@/components/ui/Label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/Select';
import { Textarea } from '@/components/ui/Textarea';
import { Code, Loader2, Upload } from 'lucide-react';
import { useCallback, useEffect, useMemo, useState } from 'react';
import { useTranslation } from 'react-i18next';

type Background = 'auto' | 'transparent' | 'opaque';
type Quality = 'low' | 'medium' | 'high';
type Provider = 'OPENAI' | 'GOOGLE' | 'FLUX';

interface OpenAiRequestParams {
  model: string;
  size: string;
  n: number;
  responseFormat: string;
  masterPrompt: string;
  specificPrompt: string;
  combinedPrompt: string;
  quality?: string;
  background?: string;
}

const SIZE_VALUES = ['1024x1024', '1536x1024', '1024x1536'] as const;
const BACKGROUND_VALUES: Background[] = ['auto', 'transparent', 'opaque'];
const QUALITY_VALUES: Quality[] = ['low', 'medium', 'high'];
const PROVIDER_VALUES: Provider[] = ['OPENAI', 'GOOGLE', 'FLUX'];

const MASTER_PROMPT_STORAGE_KEY = 'promptTester.masterPrompt';

export default function PromptTester() {
  const { t } = useTranslation('adminPromptTester');
  const [uploadedImage, setUploadedImage] = useState<File | null>(null);
  const [previewUrl, setPreviewUrl] = useState<string>('');
  const [isGenerating, setIsGenerating] = useState(false);
  const [generatedImage, setGeneratedImage] = useState<string>('');
  const [error, setError] = useState<string>('');
  const [validationErrors, setValidationErrors] = useState<Record<string, string>>({});
  const [requestParams, setRequestParams] = useState<OpenAiRequestParams | null>(null);

  const [data, setDataState] = useState({
    masterPrompt: '',
    specificPrompt: '',
    background: 'auto' as Background,
    quality: 'low' as Quality,
    size: '1024x1024',
    provider: 'OPENAI' as Provider,
  });

  const setData = useCallback(<K extends keyof typeof data>(key: K, value: (typeof data)[K]) => {
    setDataState((prev) => ({ ...prev, [key]: value }));
  }, []);

  // Load master prompt from localStorage on mount
  useEffect(() => {
    const storedMasterPrompt = localStorage.getItem(MASTER_PROMPT_STORAGE_KEY);
    if (storedMasterPrompt) {
      setData('masterPrompt', storedMasterPrompt);
    }
  }, [setData]);

  // Save master prompt to localStorage whenever it changes
  useEffect(() => {
    if (data.masterPrompt) {
      localStorage.setItem(MASTER_PROMPT_STORAGE_KEY, data.masterPrompt);
    }
  }, [data.masterPrompt]);

  const handleImageUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      setUploadedImage(file);
      const url = URL.createObjectURL(file);
      setPreviewUrl(url);
      setGeneratedImage('');
      setError('');
      setValidationErrors({});
      setRequestParams(null);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!uploadedImage) {
      setError(t('errors.uploadRequired'));
      return;
    }

    setIsGenerating(true);
    setError('');
    setGeneratedImage('');
    setValidationErrors({});

    const formData = new FormData();
    if (uploadedImage) {
      formData.append('image', uploadedImage);
    }
    formData.append('masterPrompt', data.masterPrompt);
    formData.append('specificPrompt', data.specificPrompt);
    formData.append('background', data.background);
    formData.append('quality', data.quality);
    formData.append('size', data.size);

    try {
      const url = new URL('/api/admin/ai/test-prompt', window.location.origin);
      url.searchParams.set('provider', data.provider);

      const response = await fetch(url.toString(), {
        method: 'POST',
        credentials: 'include',
        body: formData,
      });

      // Clone the response so we can read it as text if JSON parsing fails
      const responseClone = response.clone();

      let result;
      try {
        result = await response.json();
      } catch {
        // If JSON parsing fails, it's likely an HTML error page
        const text = await responseClone.text();
        if (text.includes('Maximum execution time')) {
          throw new Error(t('errors.timeout'));
        }
        throw new Error(t('errors.invalidResponse'));
      }

      if (!response.ok) {
        if (response.status === 422 && result.errors) {
          setValidationErrors(result.errors);
          return;
        }
        throw new Error(result.message || t('errors.generateFailed'));
      }

      setGeneratedImage(result.imageUrl);
      setRequestParams(result.requestParams);
    } catch (err) {
      setError(err instanceof Error ? err.message : t('errors.generic'));
    } finally {
      setIsGenerating(false);
    }
  };

  const providerOptions = useMemo(
    () => PROVIDER_VALUES.map((value) => ({ value, label: t(`form.provider.options.${value.toLowerCase() as Lowercase<Provider>}`) })),
    [t],
  );

  const backgroundOptions = useMemo(() => BACKGROUND_VALUES.map((value) => ({ value, label: t(`form.background.options.${value}`) })), [t]);

  const qualityOptions = useMemo(() => QUALITY_VALUES.map((value) => ({ value, label: t(`form.quality.options.${value}`) })), [t]);

  const sizeOptions = useMemo(() => SIZE_VALUES.map((value) => ({ value, label: t(`form.size.options.${value}`) })), [t]);

  return (
    <>
      <main className="p-8">
        <h1 className="mb-8 text-3xl font-bold">{t('page.title')}</h1>

        <form onSubmit={handleSubmit} className="flex max-w-4xl flex-col gap-6">
          <div className="flex flex-wrap gap-8">
            <div>
              <Label htmlFor="provider">{t('form.provider.label')}</Label>
              <Select value={data.provider} onValueChange={(value) => setData('provider', value as Provider)}>
                <SelectTrigger id="provider" className="mt-1">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  {providerOptions.map((option) => (
                    <SelectItem key={option.value} value={option.value}>
                      {option.label}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            <div>
              <Label htmlFor="background">{t('form.background.label')}</Label>
              <Select value={data.background} onValueChange={(value) => setData('background', value as Background)}>
                <SelectTrigger id="background" className={`mt-1 ${validationErrors.background ? 'border-red-500' : ''}`}>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  {backgroundOptions.map((option) => (
                    <SelectItem key={option.value} value={option.value}>
                      {option.label}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              {validationErrors.background && <p className="mt-1 text-sm text-red-600">{validationErrors.background}</p>}
            </div>

            <div>
              <Label htmlFor="quality">{t('form.quality.label')}</Label>
              <Select value={data.quality} onValueChange={(value) => setData('quality', value as Quality)}>
                <SelectTrigger id="quality" className={`mt-1 ${validationErrors.quality ? 'border-red-500' : ''}`}>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  {qualityOptions.map((option) => (
                    <SelectItem key={option.value} value={option.value}>
                      {option.label}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              {validationErrors.quality && <p className="mt-1 text-sm text-red-600">{validationErrors.quality}</p>}
            </div>

            <div>
              <Label htmlFor="size">{t('form.size.label')}</Label>
              <Select value={data.size} onValueChange={(value) => setData('size', value)}>
                <SelectTrigger id="size" className={`mt-1 ${validationErrors.size ? 'border-red-500' : ''}`}>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  {sizeOptions.map((option) => (
                    <SelectItem key={option.value} value={option.value}>
                      {option.label}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              {validationErrors.size && <p className="mt-1 text-sm text-red-600">{validationErrors.size}</p>}
            </div>
          </div>

          <div className="flex flex-col gap-4">
            <div>
              <Label htmlFor="masterPrompt">{t('form.masterPrompt.label')}</Label>
              <Textarea
                id="masterPrompt"
                placeholder={t('form.masterPrompt.placeholder')}
                value={data.masterPrompt}
                onChange={(e) => setData('masterPrompt', e.target.value)}
                className={`mt-1 min-h-[120px] ${validationErrors.masterPrompt ? 'border-red-500' : ''}`}
              />
              {validationErrors.masterPrompt && <p className="mt-1 text-sm text-red-600">{validationErrors.masterPrompt}</p>}
            </div>

            <div>
              <Label htmlFor="specificPrompt">{t('form.specificPrompt.label')}</Label>
              <Textarea
                id="specificPrompt"
                placeholder={t('form.specificPrompt.placeholder')}
                value={data.specificPrompt}
                onChange={(e) => setData('specificPrompt', e.target.value)}
                className={`mt-1 min-h-[120px] ${validationErrors.specificPrompt ? 'border-red-500' : ''}`}
              />
              {validationErrors.specificPrompt && <p className="mt-1 text-sm text-red-600">{validationErrors.specificPrompt}</p>}
            </div>
          </div>

          <div>
            <Label htmlFor="image">{t('form.image.label')}</Label>
            <div className="mt-1">
              <label
                htmlFor="image"
                className={`flex cursor-pointer flex-col items-center justify-center rounded-lg border-2 border-dashed p-6 hover:border-gray-400 ${
                  validationErrors.image ? 'border-red-500' : 'border-gray-300'
                }`}
              >
                {previewUrl ? (
                  <img src={previewUrl} alt={t('form.image.previewAlt')} className="max-h-48 rounded" />
                ) : (
                  <>
                    <Upload className="mb-2 h-8 w-8 text-gray-400" />
                    <span className="text-sm text-gray-600">{t('form.image.helper')}</span>
                  </>
                )}
                <input id="image" type="file" accept="image/*" onChange={handleImageUpload} className="hidden" />
              </label>
              {validationErrors.image && <p className="mt-1 text-sm text-red-600">{validationErrors.image}</p>}
            </div>
          </div>

          {error && <div className="rounded-md bg-red-50 p-4 text-sm text-red-600">{error}</div>}

          <Button type="submit" disabled={isGenerating} className="w-full md:w-auto">
            {isGenerating ? (
              <>
                <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                {t('actions.generating')}
              </>
            ) : (
              t('actions.generate')
            )}
          </Button>
        </form>

        {generatedImage && (
          <div className="mt-8 space-y-6">
            <div>
              <h2 className="mb-4 text-xl font-semibold">{t('results.title')}</h2>
              <img src={generatedImage} alt={t('results.imageAlt')} className="max-w-full rounded-lg shadow-lg" />
            </div>

            {requestParams && (
              <Card>
                <CardHeader>
                  <CardTitle className="flex items-center gap-2">
                    <Code className="h-5 w-5" />
                    {t('results.request.title')}
                  </CardTitle>
                  <CardDescription>{t('results.request.description')}</CardDescription>
                </CardHeader>
                <CardContent>
                  <Accordion type="single" collapsible className="w-full">
                    <AccordionItem value="prompts">
                      <AccordionTrigger>{t('results.request.sections.prompts.title')}</AccordionTrigger>
                      <AccordionContent>
                        <div className="space-y-4">
                          <div>
                            <Label className="text-sm font-medium">{t('results.request.sections.prompts.master')}</Label>
                            <div className="mt-1 rounded-md bg-gray-50 p-3 text-sm">{requestParams.masterPrompt}</div>
                          </div>
                          <div>
                            <Label className="text-sm font-medium">{t('results.request.sections.prompts.specific')}</Label>
                            <div className="mt-1 rounded-md bg-gray-50 p-3 text-sm">{requestParams.specificPrompt}</div>
                          </div>
                          <div>
                            <Label className="text-sm font-medium">{t('results.request.sections.prompts.combined')}</Label>
                            <div className="mt-1 rounded-md bg-gray-50 p-3 text-sm">{requestParams.combinedPrompt}</div>
                          </div>
                        </div>
                      </AccordionContent>
                    </AccordionItem>
                    <AccordionItem value="parameters">
                      <AccordionTrigger>{t('results.request.sections.parameters.title')}</AccordionTrigger>
                      <AccordionContent>
                        <div className="grid grid-cols-2 gap-4 text-sm">
                          <div>
                            <Label className="font-medium">{t('results.request.sections.parameters.model')}</Label>
                            <p className="mt-1 text-gray-600">{requestParams.model}</p>
                          </div>
                          <div>
                            <Label className="font-medium">{t('results.request.sections.parameters.size')}</Label>
                            <p className="mt-1 text-gray-600">{requestParams.size}</p>
                          </div>
                          {requestParams.quality && (
                            <div>
                              <Label className="font-medium">{t('results.request.sections.parameters.quality')}</Label>
                              <p className="mt-1 text-gray-600">{requestParams.quality}</p>
                            </div>
                          )}
                          {requestParams.background && (
                            <div>
                              <Label className="font-medium">{t('results.request.sections.parameters.background')}</Label>
                              <p className="mt-1 text-gray-600">{requestParams.background}</p>
                            </div>
                          )}
                          <div>
                            <Label className="font-medium">{t('results.request.sections.parameters.count')}</Label>
                            <p className="mt-1 text-gray-600">{requestParams.n}</p>
                          </div>
                          <div>
                            <Label className="font-medium">{t('results.request.sections.parameters.responseFormat')}</Label>
                            <p className="mt-1 text-gray-600">{requestParams.responseFormat}</p>
                          </div>
                        </div>
                      </AccordionContent>
                    </AccordionItem>
                  </Accordion>
                </CardContent>
              </Card>
            )}
          </div>
        )}
      </main>
    </>
  );
}
