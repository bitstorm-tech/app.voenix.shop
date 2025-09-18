import { Button } from '@/components/ui/Button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/Card';
import { Input } from '@/components/ui/Input';
import { Label } from '@/components/ui/Label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/Select';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/Table';
import { articlesApi } from '@/lib/api';
import type { ArticleShirtVariant, CreateArticleShirtVariantRequest } from '@/types/article';
import { Plus, Trash2 } from 'lucide-react';
import { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { toast } from 'sonner';

interface ShirtVariantsTabProps {
  articleId?: number;
  variants: ArticleShirtVariant[];
  temporaryVariants?: CreateArticleShirtVariantRequest[];
  onAddTemporaryVariant?: (variant: CreateArticleShirtVariantRequest) => void;
  onDeleteTemporaryVariant?: (index: number) => void;
  onUpdateTemporaryVariant?: (index: number, variant: CreateArticleShirtVariantRequest) => void;
}

const SHIRT_SIZES = ['XS', 'S', 'M', 'L', 'XL', 'XXL', 'XXXL'];

export default function ShirtVariantsTab({
  articleId,
  variants: initialVariants,
  temporaryVariants = [],
  onAddTemporaryVariant,
  onDeleteTemporaryVariant,
}: ShirtVariantsTabProps) {
  const [variants, setVariants] = useState<ArticleShirtVariant[]>(initialVariants);
  const [newVariant, setNewVariant] = useState<CreateArticleShirtVariantRequest>({
    color: '',
    size: 'M',
    exampleImageFilename: '',
  });
  const { t } = useTranslation('adminArticles');

  // Sync local state with prop changes (important for when variants are copied or refetched)
  useEffect(() => {
    setVariants(initialVariants);
  }, [initialVariants]);

  const handleAddVariant = async () => {
    if (!newVariant.color) {
      toast.error(t('form.shirtVariants.toasts.colorRequired'));
      return;
    }

    // Check for duplicate variants
    const isDuplicate = articleId
      ? variants.some((v) => v.color === newVariant.color && v.size === newVariant.size)
      : temporaryVariants.some((v) => v.color === newVariant.color && v.size === newVariant.size);

    if (isDuplicate) {
      toast.error(t('form.shirtVariants.toasts.duplicate'));
      return;
    }

    if (!articleId) {
      // Handle temporary variant for unsaved article
      if (onAddTemporaryVariant) {
        onAddTemporaryVariant(newVariant);
        setNewVariant({
          color: '',
          size: 'M',
          exampleImageFilename: '',
        });
        toast.success(t('form.shirtVariants.toasts.addedTemporary'));
      }
      return;
    }

    // Handle variant for saved article
    try {
      const response = await articlesApi.createShirtVariant(articleId, newVariant);
      setVariants([...variants, response]);
      setNewVariant({
        color: '',
        size: 'M',
        exampleImageFilename: '',
      });
      toast.success(t('form.shirtVariants.toasts.added'));
    } catch (error) {
      console.error('Error adding variant:', error);
      toast.error(t('form.shirtVariants.toasts.addError'));
    }
  };

  const handleDeleteVariant = async (variantId: number) => {
    try {
      await articlesApi.deleteShirtVariant(variantId);
      setVariants(variants.filter((v) => v.id !== variantId));
      toast.success(t('form.shirtVariants.toasts.deleted'));
    } catch (error) {
      console.error('Error deleting variant:', error);
      toast.error(t('form.shirtVariants.toasts.deleteError'));
    }
  };

  const handleDeleteTemporaryVariant = (index: number) => {
    if (onDeleteTemporaryVariant) {
      onDeleteTemporaryVariant(index);
      toast.success(t('form.shirtVariants.toasts.removed'));
    }
  };

  return (
    <Card>
      <CardHeader>
        <CardTitle>{t('form.shirtVariants.title')}</CardTitle>
        <CardDescription>{t('form.shirtVariants.description')}</CardDescription>
      </CardHeader>
      <CardContent className="space-y-4">
        {!articleId && temporaryVariants.length > 0 && (
          <div className="rounded-lg border border-blue-200 bg-blue-50 p-4 text-sm text-blue-800">{t('form.shirtVariants.info.unsavedNotice')}</div>
        )}

        <div className="space-y-4">
          <div className="grid grid-cols-5 gap-4">
            <div className="space-y-2">
              <Label>{t('form.shirtVariants.fields.color.label')}</Label>
              <Input
                value={newVariant.color}
                onChange={(e) => setNewVariant({ ...newVariant, color: e.target.value })}
                placeholder={t('form.shirtVariants.fields.color.placeholder')}
              />
            </div>

            <div className="space-y-2">
              <Label>{t('form.shirtVariants.fields.size.label')}</Label>
              <Select value={newVariant.size} onValueChange={(value) => setNewVariant({ ...newVariant, size: value })}>
                <SelectTrigger>
                  <SelectValue placeholder={t('form.common.select')} />
                </SelectTrigger>
                <SelectContent>
                  {SHIRT_SIZES.map((size) => (
                    <SelectItem key={size} value={size}>
                      {size}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            <div className="space-y-2">
              <Label>{t('form.shirtVariants.fields.exampleImage.label')}</Label>
              <Input
                value={newVariant.exampleImageFilename}
                onChange={(e) => setNewVariant({ ...newVariant, exampleImageFilename: e.target.value })}
                placeholder={t('form.shirtVariants.fields.exampleImage.placeholder')}
              />
            </div>

            <div className="flex items-end">
              <Button onClick={handleAddVariant} className="w-full">
                <Plus className="mr-2 h-4 w-4" />
                {t('form.common.add')}
              </Button>
            </div>
          </div>
        </div>

        {/* Show saved variants */}
        {variants.length > 0 && (
          <div className="rounded-md border">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>{t('form.shirtVariants.table.color')}</TableHead>
                  <TableHead>{t('form.shirtVariants.table.size')}</TableHead>

                  <TableHead>{t('form.shirtVariants.table.exampleImage')}</TableHead>
                  <TableHead className="text-right">{t('form.common.actions')}</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {variants.map((variant) => (
                  <TableRow key={variant.id}>
                    <TableCell>{variant.color}</TableCell>
                    <TableCell>{variant.size}</TableCell>

                    <TableCell>{variant.exampleImageUrl ? t('form.common.yes') : '-'}</TableCell>
                    <TableCell className="text-right">
                      <Button variant="ghost" size="sm" onClick={() => handleDeleteVariant(variant.id)}>
                        <Trash2 className="h-4 w-4" />
                      </Button>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </div>
        )}

        {/* Show temporary variants for unsaved articles */}
        {!articleId && temporaryVariants.length > 0 && (
          <div className="rounded-md border">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>{t('form.shirtVariants.table.color')}</TableHead>
                  <TableHead>{t('form.shirtVariants.table.size')}</TableHead>

                  <TableHead>{t('form.shirtVariants.table.exampleImage')}</TableHead>
                  <TableHead className="text-right">{t('form.common.actions')}</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {temporaryVariants.map((variant, index) => (
                  <TableRow key={index}>
                    <TableCell>{variant.color}</TableCell>
                    <TableCell>{variant.size}</TableCell>

                    <TableCell>{variant.exampleImageFilename || '-'}</TableCell>
                    <TableCell className="text-right">
                      <Button variant="ghost" size="sm" onClick={() => handleDeleteTemporaryVariant(index)}>
                        <Trash2 className="h-4 w-4" />
                      </Button>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </div>
        )}
      </CardContent>
    </Card>
  );
}
