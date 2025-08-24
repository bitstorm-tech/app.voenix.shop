import MugVariantDialog from '@/components/admin/articles/MugVariantDialog';
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

const ColorDisplay = ({ color }: { color: string }) => (
  <div className="flex items-center gap-2">
    <div className="h-6 w-6 rounded border border-gray-300" style={{ backgroundColor: color }} />
    <span className="text-sm text-gray-600">{color}</span>
  </div>
);

const isActive = (variant: ArticleMugVariant | CreateArticleMugVariantRequest) => variant.active !== false;

interface VariantTableProps<T> {
  variants: T[];
  onEdit: (variant: T, index?: number) => void;
  onDelete: (variant: T, index?: number) => void;
  isTemporary?: boolean;
}

function VariantTable<T extends ArticleMugVariant | CreateArticleMugVariantRequest>({
  variants,
  onEdit,
  onDelete,
  isTemporary = false,
}: VariantTableProps<T>) {
  return (
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
          {variants.map((variant, index) => (
            <TableRow key={isTemporary ? index : (variant as ArticleMugVariant).id} className={cn(!isActive(variant) ? 'bg-gray-50 opacity-60' : '')}>
              <TableCell className="font-medium">
                <div className="flex items-center gap-2">
                  {variant.name}
                  {!isActive(variant) && (
                    <span className="inline-flex items-center rounded-full bg-gray-100 px-2 py-1 text-xs font-medium text-gray-600">Inactive</span>
                  )}
                </div>
              </TableCell>
              <TableCell>{variant.articleVariantNumber || '-'}</TableCell>
              <TableCell>
                <ColorDisplay color={variant.insideColorCode} />
              </TableCell>
              <TableCell>
                <ColorDisplay color={variant.outsideColorCode} />
              </TableCell>
              <TableCell>
                {variant.isDefault && (
                  <span className="inline-flex items-center rounded-full bg-green-100 px-2 py-1 text-xs font-medium text-green-700">Default</span>
                )}
              </TableCell>
              <TableCell>
                {isActive(variant) ? (
                  <span className="inline-flex items-center rounded-full bg-green-100 px-2 py-1 text-xs font-medium text-green-700">Active</span>
                ) : (
                  <span className="inline-flex items-center rounded-full bg-red-100 px-2 py-1 text-xs font-medium text-red-700">Inactive</span>
                )}
              </TableCell>
              <TableCell>
                {!isTemporary && (variant as ArticleMugVariant).exampleImageUrl ? (
                  <img
                    src={(variant as ArticleMugVariant).exampleImageUrl!}
                    alt={`${variant.name} example`}
                    className="h-10 w-10 rounded border object-cover"
                  />
                ) : (
                  <div className="flex h-10 w-10 items-center justify-center rounded border bg-gray-100">
                    <ImageIcon className="h-4 w-4 text-gray-400" />
                  </div>
                )}
              </TableCell>
              <TableCell className="text-right">
                <div className="flex justify-end gap-1">
                  <Button variant="ghost" size="sm" onClick={() => onEdit(variant, index)}>
                    <Edit className="h-4 w-4" />
                  </Button>
                  <Button variant="ghost" size="sm" onClick={() => onDelete(variant, index)}>
                    <Trash2 className="h-4 w-4" />
                  </Button>
                </div>
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </div>
  );
}

type DialogState =
  | { type: 'closed' }
  | { type: 'add' }
  | { type: 'edit'; variant: ArticleMugVariant }
  | { type: 'editTemp'; variant: CreateArticleMugVariantRequest; index: number };

type DeleteState = { type: 'none' } | { type: 'saved'; id: number } | { type: 'temporary'; index: number };

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
  const [dialogState, setDialogState] = useState<DialogState>({ type: 'closed' });
  const [deleteState, setDeleteState] = useState<DeleteState>({ type: 'none' });

  // Sync local state with prop changes (important for when variants are copied or refetched)
  useEffect(() => {
    setVariants(initialVariants);
  }, [initialVariants]);

  const handleAddVariant = () => setDialogState({ type: 'add' });

  const handleEditVariant = (variant: ArticleMugVariant) => setDialogState({ type: 'edit', variant });

  const handleEditTemporaryVariant = (index: number, variant: CreateArticleMugVariantRequest) => setDialogState({ type: 'editTemp', variant, index });

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

  const handleDeleteVariant = (variant: ArticleMugVariant) => setDeleteState({ type: 'saved', id: variant.id });

  const handleDeleteTemporaryVariant = (index: number) => setDeleteState({ type: 'temporary', index });

  const confirmDelete = async () => {
    switch (deleteState.type) {
      case 'temporary':
        onDeleteTemporaryVariant?.(deleteState.index);
        toast.success('Variant removed');
        break;

      case 'saved':
        try {
          await articlesApi.deleteMugVariant(deleteState.id);
          setVariants((prev) => prev.filter((v) => v.id !== deleteState.id));
          toast.success('Variant deleted successfully');
        } catch (error) {
          console.error('Error deleting variant:', error);
          toast.error('Failed to delete variant');
        }
        break;
    }
    setDeleteState({ type: 'none' });
  };

  const cancelDelete = () => setDeleteState({ type: 'none' });

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
        <CardDescription>Add different color combinations for this mug. Each variant can have different inside and outside colors.</CardDescription>
      </CardHeader>
      <CardContent className="space-y-4">
        {!articleId && temporaryVariants.length > 0 && (
          <div className="rounded-lg border border-blue-200 bg-blue-50 p-4 text-sm text-blue-800">
            These variants will be saved when you save the article.
          </div>
        )}

        <MugVariantDialog
          open={dialogState.type !== 'closed'}
          onOpenChange={(open) => setDialogState(open ? dialogState : { type: 'closed' })}
          articleId={articleId}
          variant={dialogState.type === 'edit' ? dialogState.variant : undefined}
          temporaryVariant={dialogState.type === 'editTemp' ? dialogState.variant : undefined}
          temporaryVariantIndex={dialogState.type === 'editTemp' ? dialogState.index : undefined}
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
          <VariantTable variants={variants} onEdit={(variant) => handleEditVariant(variant)} onDelete={(variant) => handleDeleteVariant(variant)} />
        )}

        {/* Show temporary variants for unsaved articles */}
        {!articleId && temporaryVariants.length > 0 && (
          <VariantTable
            variants={temporaryVariants}
            onEdit={(variant, index) => handleEditTemporaryVariant(index!, variant)}
            onDelete={(_, index) => handleDeleteTemporaryVariant(index!)}
            isTemporary
          />
        )}
      </CardContent>

      <ConfirmationDialog
        isOpen={deleteState.type !== 'none'}
        onConfirm={confirmDelete}
        onCancel={cancelDelete}
        title="Delete Mug Variant"
        description="Are you sure you want to delete this mug variant? This action cannot be undone."
        confirmText="Delete Variant"
      />
    </Card>
  );
}
