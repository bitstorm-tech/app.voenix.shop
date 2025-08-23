import { Button } from '@/components/ui/Button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/Card';
import ConfirmationDialog from '@/components/ui/ConfirmationDialog';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/Table';
import { articlesApi } from '@/lib/api';
import { cn } from '@/lib/utils';
import type { ArticleMugVariant, CreateArticleMugVariantRequest } from '@/types/article';
import { Copy, Edit, Image as ImageIcon, Plus, Trash2 } from 'lucide-react';
import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { toast } from 'sonner';
import MugVariantDialog from '@/components/admin/articles/MugVariantDialog';

interface MugVariantsTabProps {
  articleId?: number;
  variants: ArticleMugVariant[];
  temporaryVariants?: CreateArticleMugVariantRequest[];
  onAddTemporaryVariant?: (variant: CreateArticleMugVariantRequest) => void;
  onDeleteTemporaryVariant?: (index: number) => void;
  onUpdateTemporaryVariant?: (index: number, variant: CreateArticleMugVariantRequest) => void;
}

export default function MugVariantsTab({
  articleId,
  variants: initialVariants,
  temporaryVariants = [],
  onAddTemporaryVariant,
  onDeleteTemporaryVariant,
  onUpdateTemporaryVariant,
}: MugVariantsTabProps) {
  const navigate = useNavigate();
  const [variants, setVariants] = useState<ArticleMugVariant[]>(initialVariants);
  const [showDialog, setShowDialog] = useState(false);
  const [editingVariant, setEditingVariant] = useState<ArticleMugVariant | null>(null);
  const [editingTemporaryVariant, setEditingTemporaryVariant] = useState<CreateArticleMugVariantRequest | null>(null);
  const [editingTemporaryIndex, setEditingTemporaryIndex] = useState<number | null>(null);
  const [isDeleting, setIsDeleting] = useState(false);
  const [deleteVariantId, setDeleteVariantId] = useState<number | null>(null);
  const [deleteVariantIndex, setDeleteVariantIndex] = useState<number | null>(null);
  const [isTemporaryVariant, setIsTemporaryVariant] = useState(false);

  // Sync local state with prop changes (important for when variants are copied or refetched)
  useEffect(() => {
    setVariants(initialVariants);
  }, [initialVariants]);

  const handleAddVariant = () => {
    setEditingVariant(null);
    setEditingTemporaryVariant(null);
    setEditingTemporaryIndex(null);
    setShowDialog(true);
  };

  const handleEditVariant = (variant: ArticleMugVariant) => {
    setEditingVariant(variant);
    setEditingTemporaryVariant(null);
    setEditingTemporaryIndex(null);
    setShowDialog(true);
  };

  const handleEditTemporaryVariant = (index: number, variant: CreateArticleMugVariantRequest) => {
    setEditingTemporaryVariant(variant);
    setEditingTemporaryIndex(index);
    setEditingVariant(null);
    setShowDialog(true);
  };


  const handleVariantSaved = (variant: ArticleMugVariant) => {
    const existingIndex = variants.findIndex((v) => v.id === variant.id);
    if (existingIndex >= 0) {
      // Update existing variant
      setVariants(variants.map((v) => (v.id === variant.id ? variant : v)));
    } else {
      // Add new variant
      setVariants([...variants, variant]);
    }
  };

  const handleTemporaryVariantSaved = (variant: CreateArticleMugVariantRequest, index?: number) => {
    if (typeof index === 'number' && onUpdateTemporaryVariant) {
      // Update existing temporary variant
      onUpdateTemporaryVariant(index, variant);
    } else if (onAddTemporaryVariant) {
      // Add new temporary variant
      onAddTemporaryVariant(variant);
    }
  };

  const handleRefetchVariants = async () => {
    if (articleId) {
      try {
        const article = await articlesApi.getById(articleId);
        if (article.mugVariants) {
          setVariants(article.mugVariants);
        }
      } catch (error) {
        console.error('Error refetching variants:', error);
      }
    }
  };

  const handleDeleteVariant = (variantId: number) => {
    setDeleteVariantId(variantId);
    setIsTemporaryVariant(false);
    setIsDeleting(true);
  };

  const handleDeleteTemporaryVariant = (index: number) => {
    setDeleteVariantIndex(index);
    setIsTemporaryVariant(true);
    setIsDeleting(true);
  };

  const confirmDelete = async () => {
    if (isTemporaryVariant && deleteVariantIndex !== null) {
      if (onDeleteTemporaryVariant) {
        onDeleteTemporaryVariant(deleteVariantIndex);
        toast.success('Variant removed');
      }
    } else if (!isTemporaryVariant && deleteVariantId !== null) {
      try {
        await articlesApi.deleteMugVariant(deleteVariantId);
        setVariants(variants.filter((v) => v.id !== deleteVariantId));
        toast.success('Variant deleted successfully');
      } catch (error) {
        console.error('Error deleting variant:', error);
        toast.error('Failed to delete variant');
      }
    }
    cancelDelete();
  };

  const cancelDelete = () => {
    setIsDeleting(false);
    setDeleteVariantId(null);
    setDeleteVariantIndex(null);
    setIsTemporaryVariant(false);
  };

  const handleCopyVariants = () => {
    if (!articleId) {
      toast.error('Please save the article first before copying variants');
      return;
    }
    navigate(`/admin/articles/${articleId}/copy-variants`);
  };

  return (
    <Card>
      <CardHeader>
        <CardTitle>Mug Variants</CardTitle>
        <CardDescription>
          Add different color combinations for this mug. Each variant can have different inside and outside colors.
        </CardDescription>
      </CardHeader>
      <CardContent className="space-y-4">
        {!articleId && temporaryVariants.length > 0 && (
          <div className="rounded-lg border border-blue-200 bg-blue-50 p-4 text-sm text-blue-800">
            These variants will be saved when you save the article.
          </div>
        )}

        <MugVariantDialog
          open={showDialog}
          onOpenChange={setShowDialog}
          articleId={articleId}
          variant={editingVariant || undefined}
          temporaryVariant={editingTemporaryVariant || undefined}
          temporaryVariantIndex={editingTemporaryIndex || undefined}
          existingVariants={variants}
          existingTemporaryVariants={temporaryVariants}
          onVariantSaved={handleVariantSaved}
          onTemporaryVariantSaved={handleTemporaryVariantSaved}
          onRefetchVariants={handleRefetchVariants}
        />

        <div className="flex items-center justify-end gap-4">
          {articleId && (
            <Button onClick={handleCopyVariants} variant="outline" className="min-w-[140px]">
              <Copy className="mr-2 h-4 w-4" />
              Copy Variants
            </Button>
          )}
          <Button onClick={handleAddVariant} className="min-w-[140px]">
            <Plus className="mr-2 h-4 w-4" />
            Add Variant
          </Button>
        </div>

        {/* Show saved variants */}
        {variants.length > 0 && (
          <div className="rounded-md border">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Name</TableHead>
                  <TableHead>Article Variant Number</TableHead>
                  <TableHead>Inside Color</TableHead>
                  <TableHead>Outside Color</TableHead>
                  <TableHead>Default</TableHead>
                  <TableHead>Active</TableHead>
                  <TableHead>Example Image</TableHead>
                  <TableHead className="text-right">Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {variants.map((variant) => (
                  <TableRow
                    key={variant.id}
                    className={cn(!variant.active ? 'bg-gray-50 opacity-60' : '')}
                  >
                    <TableCell className="font-medium">
                      <div className="flex items-center gap-2">
                        {variant.name}
                        {!variant.active && (
                          <span className="inline-flex items-center rounded-full bg-gray-100 px-2 py-1 text-xs font-medium text-gray-600">
                            Inactive
                          </span>
                        )}
                      </div>
                    </TableCell>
                    <TableCell>{variant.articleVariantNumber || '-'}</TableCell>
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
                    <TableCell>
                      {variant.isDefault && (
                        <span className="inline-flex items-center rounded-full bg-green-100 px-2 py-1 text-xs font-medium text-green-700">
                          Default
                        </span>
                      )}
                    </TableCell>
                    <TableCell>
                      {variant.active ? (
                        <span className="inline-flex items-center rounded-full bg-green-100 px-2 py-1 text-xs font-medium text-green-700">
                          Active
                        </span>
                      ) : (
                        <span className="inline-flex items-center rounded-full bg-red-100 px-2 py-1 text-xs font-medium text-red-700">Inactive</span>
                      )}
                    </TableCell>
                    <TableCell>
                      {variant.exampleImageUrl ? (
                        <img src={variant.exampleImageUrl} alt={`${variant.name} example`} className="h-10 w-10 rounded border object-cover" />
                      ) : (
                        <div className="flex h-10 w-10 items-center justify-center rounded border bg-gray-100">
                          <ImageIcon className="h-4 w-4 text-gray-400" />
                        </div>
                      )}
                    </TableCell>
                    <TableCell className="text-right">
                      <div className="flex justify-end gap-1">
                        <Button variant="ghost" size="sm" onClick={() => handleEditVariant(variant)}>
                          <Edit className="h-4 w-4" />
                        </Button>
                        <Button variant="ghost" size="sm" onClick={() => handleDeleteVariant(variant.id)}>
                          <Trash2 className="h-4 w-4" />
                        </Button>
                      </div>
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
                  <TableHead>Article Variant Number</TableHead>
                  <TableHead>Inside Color</TableHead>
                  <TableHead>Outside Color</TableHead>
                  <TableHead>Default</TableHead>
                  <TableHead>Active</TableHead>
                  <TableHead>Example Image</TableHead>
                  <TableHead className="text-right">Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {temporaryVariants.map((variant, index) => (
                  <TableRow
                    key={index}
                    className={cn(editingTemporaryIndex === index ? 'bg-blue-50' : '', variant.active === false ? 'bg-gray-50 opacity-60' : '')}
                  >
                    <TableCell className="font-medium">
                      <div className="flex items-center gap-2">
                        {variant.name}
                        {variant.active === false && (
                          <span className="inline-flex items-center rounded-full bg-gray-100 px-2 py-1 text-xs font-medium text-gray-600">
                            Inactive
                          </span>
                        )}
                      </div>
                    </TableCell>
                    <TableCell>{variant.articleVariantNumber || '-'}</TableCell>
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
                    <TableCell>
                      {variant.isDefault && (
                        <span className="inline-flex items-center rounded-full bg-green-100 px-2 py-1 text-xs font-medium text-green-700">
                          Default
                        </span>
                      )}
                    </TableCell>
                    <TableCell>
                      {variant.active !== false ? (
                        <span className="inline-flex items-center rounded-full bg-green-100 px-2 py-1 text-xs font-medium text-green-700">
                          Active
                        </span>
                      ) : (
                        <span className="inline-flex items-center rounded-full bg-red-100 px-2 py-1 text-xs font-medium text-red-700">Inactive</span>
                      )}
                    </TableCell>
                    <TableCell>
                      <div className="flex h-10 w-10 items-center justify-center rounded border bg-gray-100">
                        <ImageIcon className="h-4 w-4 text-gray-400" />
                      </div>
                    </TableCell>
                    <TableCell className="text-right">
                      <div className="flex justify-end gap-1">
                        <Button variant="ghost" size="sm" onClick={() => handleEditTemporaryVariant(index, variant)}>
                          <Edit className="h-4 w-4" />
                        </Button>
                        <Button variant="ghost" size="sm" onClick={() => handleDeleteTemporaryVariant(index)}>
                          <Trash2 className="h-4 w-4" />
                        </Button>
                      </div>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </div>
        )}
      </CardContent>

      <ConfirmationDialog
        isOpen={isDeleting}
        onConfirm={confirmDelete}
        onCancel={cancelDelete}
        title="Delete Mug Variant"
        description="Are you sure you want to delete this mug variant? This action cannot be undone."
        confirmText="Delete Variant"
      />
    </Card>
  );
}
