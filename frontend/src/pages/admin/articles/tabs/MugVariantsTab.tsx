import { Button } from '@/components/ui/Button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/Card';
import { ColorPicker } from '@/components/ui/ColorPicker';
import { Input } from '@/components/ui/Input';
import { Label } from '@/components/ui/Label';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/Table';
import { articlesApi } from '@/lib/api';
import type { ArticleMugVariant, CreateArticleMugVariantRequest } from '@/types/article';
import { Plus, Trash2 } from 'lucide-react';
import { useState } from 'react';
import { toast } from 'sonner';

interface MugVariantsTabProps {
  articleId?: number;
  variants: ArticleMugVariant[];
  temporaryVariants?: CreateArticleMugVariantRequest[];
  onAddTemporaryVariant?: (variant: CreateArticleMugVariantRequest) => void;
  onDeleteTemporaryVariant?: (index: number) => void;
}

export default function MugVariantsTab({
  articleId,
  variants: initialVariants,
  temporaryVariants = [],
  onAddTemporaryVariant,
  onDeleteTemporaryVariant,
}: MugVariantsTabProps) {
  const [variants, setVariants] = useState<ArticleMugVariant[]>(initialVariants);
  const [newVariant, setNewVariant] = useState<CreateArticleMugVariantRequest>({
    insideColorCode: '#ffffff',
    outsideColorCode: '#ffffff',
    name: '',
    sku: '',
    exampleImageFilename: '',
  });

  const handleAddVariant = async () => {
    if (!newVariant.name) {
      toast.error('Please enter a variant name');
      return;
    }

    // Check for duplicate variants
    const isDuplicate = articleId ? variants.some((v) => v.name === newVariant.name) : temporaryVariants.some((v) => v.name === newVariant.name);

    if (isDuplicate) {
      toast.error('A variant with this name already exists');
      return;
    }

    if (!articleId) {
      // Handle temporary variant for unsaved article
      if (onAddTemporaryVariant) {
        onAddTemporaryVariant(newVariant);
        setNewVariant({
          insideColorCode: '#ffffff',
          outsideColorCode: '#ffffff',
          name: '',
          sku: '',
          exampleImageFilename: '',
        });
        toast.success('Variant added (will be saved with article)');
      }
      return;
    }

    // Handle variant for saved article
    try {
      const response = await articlesApi.createMugVariant(articleId, newVariant);
      setVariants([...variants, response]);
      setNewVariant({
        insideColorCode: '#ffffff',
        outsideColorCode: '#ffffff',
        name: '',
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
      await articlesApi.deleteMugVariant(variantId);
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

  return (
    <Card>
      <CardHeader>
        <CardTitle>Mug Variants</CardTitle>
        <CardDescription>Add different color combinations for this mug. Each variant can have different inside and outside colors.</CardDescription>
      </CardHeader>
      <CardContent className="space-y-4">
        {!articleId && temporaryVariants.length > 0 && (
          <div className="rounded-lg border border-blue-200 bg-blue-50 p-4 text-sm text-blue-800">
            These variants will be saved when you save the article.
          </div>
        )}

        <div className="space-y-4">
          <div className="grid grid-cols-6 gap-4">
            <div className="space-y-2">
              <Label>Name</Label>
              <Input
                value={newVariant.name}
                onChange={(e) => setNewVariant({ ...newVariant, name: e.target.value })}
                placeholder="e.g., Classic White"
              />
            </div>

            <div className="space-y-2">
              <Label>Inside Color</Label>
              <ColorPicker value={newVariant.insideColorCode} onChange={(color) => setNewVariant({ ...newVariant, insideColorCode: color })} />
            </div>

            <div className="space-y-2">
              <Label>Outside Color</Label>
              <ColorPicker value={newVariant.outsideColorCode} onChange={(color) => setNewVariant({ ...newVariant, outsideColorCode: color })} />
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
                  <TableHead>Name</TableHead>
                  <TableHead>Inside Color</TableHead>
                  <TableHead>Outside Color</TableHead>
                  <TableHead>SKU</TableHead>
                  <TableHead>Example Image</TableHead>
                  <TableHead className="text-right">Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {variants.map((variant) => (
                  <TableRow key={variant.id}>
                    <TableCell>{variant.name}</TableCell>
                    <TableCell>
                      <div className="flex items-center gap-2">
                        <div className="h-6 w-6 rounded border border-gray-300" style={{ backgroundColor: variant.insideColorCode }} />
                        <span className="text-sm text-gray-600">{variant.insideColorCode}</span>
                      </div>
                    </TableCell>
                    <TableCell>
                      <div className="flex items-center gap-2">
                        <div className="h-6 w-6 rounded border border-gray-300" style={{ backgroundColor: variant.outsideColorCode }} />
                        <span className="text-sm text-gray-600">{variant.outsideColorCode}</span>
                      </div>
                    </TableCell>
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
                  <TableHead>Name</TableHead>
                  <TableHead>Inside Color</TableHead>
                  <TableHead>Outside Color</TableHead>
                  <TableHead>SKU</TableHead>
                  <TableHead>Example Image</TableHead>
                  <TableHead className="text-right">Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {temporaryVariants.map((variant, index) => (
                  <TableRow key={index}>
                    <TableCell>{variant.name}</TableCell>
                    <TableCell>
                      <div className="flex items-center gap-2">
                        <div className="h-6 w-6 rounded border border-gray-300" style={{ backgroundColor: variant.insideColorCode }} />
                        <span className="text-sm text-gray-600">{variant.insideColorCode}</span>
                      </div>
                    </TableCell>
                    <TableCell>
                      <div className="flex items-center gap-2">
                        <div className="h-6 w-6 rounded border border-gray-300" style={{ backgroundColor: variant.outsideColorCode }} />
                        <span className="text-sm text-gray-600">{variant.outsideColorCode}</span>
                      </div>
                    </TableCell>
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
