import { Button } from '@/components/ui/Button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/Card';
import { Input } from '@/components/ui/Input';
import { Label } from '@/components/ui/Label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/Select';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/Table';
import { articlesApi } from '@/lib/api';
import type { ArticleType, ArticleVariant, CreateArticleVariantRequest, VariantType } from '@/types/article';
import { Plus, Trash2 } from 'lucide-react';
import { useState } from 'react';
import { toast } from 'sonner';

interface VariantsTabProps {
  articleId?: number;
  articleType: ArticleType;
  variants: ArticleVariant[];
  temporaryVariants?: CreateArticleVariantRequest[];
  onAddTemporaryVariant?: (variant: CreateArticleVariantRequest) => void;
  onDeleteTemporaryVariant?: (index: number) => void;
}

export default function VariantsTab({
  articleId,
  articleType,
  variants: initialVariants,
  temporaryVariants = [],
  onAddTemporaryVariant,
  onDeleteTemporaryVariant,
}: VariantsTabProps) {
  const [variants, setVariants] = useState<ArticleVariant[]>(initialVariants);
  const [newVariant, setNewVariant] = useState({
    variantType: 'COLOR' as VariantType,
    variantValue: '',
    sku: '',
    exampleImageFilename: '',
  });

  const getAvailableVariantTypes = (): VariantType[] => {
    switch (articleType) {
      case 'MUG':
        return ['COLOR'];
      case 'SHIRT':
        return ['COLOR', 'SIZE'];
      case 'PILLOW':
        return ['COLOR', 'MATERIAL'];
      default:
        return ['COLOR'];
    }
  };

  const handleAddVariant = async () => {
    if (!newVariant.variantValue) {
      toast.error('Please enter a variant value');
      return;
    }

    // Check for duplicate variants
    const isDuplicate = articleId
      ? variants.some((v) => v.variantType === newVariant.variantType && v.variantValue === newVariant.variantValue)
      : temporaryVariants.some((v) => v.variantType === newVariant.variantType && v.variantValue === newVariant.variantValue);

    if (isDuplicate) {
      toast.error('This variant already exists');
      return;
    }

    if (!articleId) {
      // Handle temporary variant for unsaved article
      if (onAddTemporaryVariant) {
        onAddTemporaryVariant(newVariant);
        setNewVariant({
          variantType: 'COLOR',
          variantValue: '',
          sku: '',
          exampleImageFilename: '',
        });
        toast.success('Variant added (will be saved with article)');
      }
      return;
    }

    // Handle variant for saved article
    try {
      const response = await articlesApi.createVariant(articleId, newVariant);
      setVariants([...variants, response]);
      setNewVariant({
        variantType: 'COLOR',
        variantValue: '',
        sku: '',
        exampleImageFilename: '',
      });
      toast.success('Variant added successfully');
    } catch (error) {
      console.error('Error adding variant:', error);
      toast.error('Failed to add variant');
    }
  };

  const handleDeleteVariant = async (variantId: number) => {
    try {
      await articlesApi.deleteVariant(variantId);
      setVariants(variants.filter((v) => v.id !== variantId));
      toast.success('Variant deleted successfully');
    } catch (error) {
      console.error('Error deleting variant:', error);
      toast.error('Failed to delete variant');
    }
  };

  const handleDeleteTemporaryVariant = (index: number) => {
    if (onDeleteTemporaryVariant) {
      onDeleteTemporaryVariant(index);
      toast.success('Variant removed');
    }
  };

  const getVariantTypeLabel = (type: VariantType): string => {
    const labels: Record<VariantType, string> = {
      COLOR: 'Color',
      SIZE: 'Size',
      MATERIAL: 'Material',
    };
    return labels[type] || type;
  };

  return (
    <Card>
      <CardHeader>
        <CardTitle>Variants</CardTitle>
        <CardDescription>
          {articleType === 'MUG' && 'Add color variants for this mug'}
          {articleType === 'SHIRT' && 'Add color and size combinations for this shirt'}
          {articleType === 'PILLOW' && 'Add color and material combinations for this pillow'}
        </CardDescription>
      </CardHeader>
      <CardContent className="space-y-4">
        {!articleId && temporaryVariants.length > 0 && (
          <div className="rounded-lg border border-blue-200 bg-blue-50 p-4 text-sm text-blue-800">
            These variants will be saved when you save the article.
          </div>
        )}

        <div className="space-y-4">
          <div className="grid grid-cols-5 gap-4">
            <div className="space-y-2">
              <Label>Type</Label>
              <Select value={newVariant.variantType} onValueChange={(value) => setNewVariant({ ...newVariant, variantType: value as VariantType })}>
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  {getAvailableVariantTypes().map((type) => (
                    <SelectItem key={type} value={type}>
                      {getVariantTypeLabel(type)}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            <div className="space-y-2">
              <Label>Value</Label>
              <Input
                value={newVariant.variantValue}
                onChange={(e) => setNewVariant({ ...newVariant, variantValue: e.target.value })}
                placeholder={newVariant.variantType === 'COLOR' ? 'Red' : newVariant.variantType === 'SIZE' ? 'L' : 'Cotton'}
              />
            </div>

            <div className="space-y-2">
              <Label>SKU</Label>
              <Input value={newVariant.sku} onChange={(e) => setNewVariant({ ...newVariant, sku: e.target.value })} placeholder="Optional" />
            </div>

            <div className="space-y-2">
              <Label>Example Image</Label>
              <Input
                value={newVariant.exampleImageFilename}
                onChange={(e) => setNewVariant({ ...newVariant, exampleImageFilename: e.target.value })}
                placeholder="image.jpg"
              />
            </div>

            <div className="flex items-end">
              <Button onClick={handleAddVariant} className="w-full">
                <Plus className="mr-2 h-4 w-4" />
                Add
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
                  <TableHead>Type</TableHead>
                  <TableHead>Value</TableHead>
                  <TableHead>SKU</TableHead>
                  <TableHead>Example Image</TableHead>
                  <TableHead className="text-right">Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {variants.map((variant) => (
                  <TableRow key={variant.id}>
                    <TableCell>{getVariantTypeLabel(variant.variantType)}</TableCell>
                    <TableCell>{variant.variantValue}</TableCell>
                    <TableCell>{variant.sku || '-'}</TableCell>
                    <TableCell>{variant.exampleImageUrl ? 'Yes' : '-'}</TableCell>
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
                  <TableHead>Type</TableHead>
                  <TableHead>Value</TableHead>
                  <TableHead>SKU</TableHead>
                  <TableHead>Example Image</TableHead>
                  <TableHead className="text-right">Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {temporaryVariants.map((variant, index) => (
                  <TableRow key={index}>
                    <TableCell>{getVariantTypeLabel(variant.variantType as VariantType)}</TableCell>
                    <TableCell>{variant.variantValue}</TableCell>
                    <TableCell>{variant.sku || '-'}</TableCell>
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
