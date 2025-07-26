import { Button } from '@/components/ui/Button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/Card';
import { Input } from '@/components/ui/Input';
import { Label } from '@/components/ui/Label';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/Table';
import { articlesApi } from '@/lib/api';
import type { ArticlePillowVariant, CreateArticlePillowVariantRequest } from '@/types/article';
import { Plus, Trash2 } from 'lucide-react';
import { useState } from 'react';
import { toast } from 'sonner';

interface PillowVariantsTabProps {
  articleId?: number;
  variants: ArticlePillowVariant[];
  temporaryVariants?: CreateArticlePillowVariantRequest[];
  onAddTemporaryVariant?: (variant: CreateArticlePillowVariantRequest) => void;
  onDeleteTemporaryVariant?: (index: number) => void;
  onUpdateTemporaryVariant?: (index: number, variant: CreateArticlePillowVariantRequest) => void;
}

export default function PillowVariantsTab({
  articleId,
  variants: initialVariants,
  temporaryVariants = [],
  onAddTemporaryVariant,
  onDeleteTemporaryVariant,
}: PillowVariantsTabProps) {
  const [variants, setVariants] = useState<ArticlePillowVariant[]>(initialVariants);
  const [newVariant, setNewVariant] = useState<CreateArticlePillowVariantRequest>({
    color: '',
    material: '',
    exampleImageFilename: '',
  });

  const handleAddVariant = async () => {
    if (!newVariant.color || !newVariant.material) {
      toast.error('Please enter both color and material');
      return;
    }

    // Check for duplicate variants
    const isDuplicate = articleId
      ? variants.some((v) => v.color === newVariant.color && v.material === newVariant.material)
      : temporaryVariants.some((v) => v.color === newVariant.color && v.material === newVariant.material);

    if (isDuplicate) {
      toast.error('This color and material combination already exists');
      return;
    }

    if (!articleId) {
      // Handle temporary variant for unsaved article
      if (onAddTemporaryVariant) {
        onAddTemporaryVariant(newVariant);
        setNewVariant({
          color: '',
          material: '',
          exampleImageFilename: '',
        });
        toast.success('Variant added (will be saved with article)');
      }
      return;
    }

    // Handle variant for saved article
    try {
      const response = await articlesApi.createPillowVariant(articleId, newVariant);
      setVariants([...variants, response]);
      setNewVariant({
        color: '',
        material: '',
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
      await articlesApi.deletePillowVariant(variantId);
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
        <CardTitle>Pillow Variants</CardTitle>
        <CardDescription>Add color and material combinations for this pillow. Each combination represents a unique product variant.</CardDescription>
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
              <Label>Color</Label>
              <Input
                value={newVariant.color}
                onChange={(e) => setNewVariant({ ...newVariant, color: e.target.value })}
                placeholder="e.g., Navy Blue"
              />
            </div>

            <div className="space-y-2">
              <Label>Material</Label>
              <Input
                value={newVariant.material}
                onChange={(e) => setNewVariant({ ...newVariant, material: e.target.value })}
                placeholder="e.g., Cotton"
              />
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
                  <TableHead>Color</TableHead>
                  <TableHead>Material</TableHead>

                  <TableHead>Example Image</TableHead>
                  <TableHead className="text-right">Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {variants.map((variant) => (
                  <TableRow key={variant.id}>
                    <TableCell>{variant.color}</TableCell>
                    <TableCell>{variant.material}</TableCell>

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
                  <TableHead>Color</TableHead>
                  <TableHead>Material</TableHead>

                  <TableHead>Example Image</TableHead>
                  <TableHead className="text-right">Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {temporaryVariants.map((variant, index) => (
                  <TableRow key={index}>
                    <TableCell>{variant.color}</TableCell>
                    <TableCell>{variant.material}</TableCell>

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
