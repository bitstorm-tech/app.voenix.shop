import { Button } from '@/components/ui/Button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/Card';
import { Checkbox } from '@/components/ui/Checkbox';
import { ColorPicker } from '@/components/ui/ColorPicker';
import ConfirmationDialog from '@/components/ui/ConfirmationDialog';
import { Input } from '@/components/ui/Input';
import { Label } from '@/components/ui/Label';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/Table';
import { articlesApi } from '@/lib/api';
import { cn } from '@/lib/utils';
import type { ArticleMugVariant, CreateArticleMugVariantRequest } from '@/types/article';
import { Copy, Edit, Image as ImageIcon, Plus, Trash2, Upload, X } from 'lucide-react';
import { useCallback, useEffect, useRef, useState } from 'react';
import Cropper from 'react-easy-crop';
import { useNavigate } from 'react-router-dom';
import { toast } from 'sonner';

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
  const [newVariant, setNewVariant] = useState<CreateArticleMugVariantRequest>({
    insideColorCode: '#ffffff',
    outsideColorCode: '#ffffff',
    name: '',
    articleVariantNumber: '',
    isDefault: false,
    active: true,
  });
  const [imageFile, setImageFile] = useState<File | null>(null);
  const [imagePreviewUrl, setImagePreviewUrl] = useState<string | null>(null);
  const [originalImageUrl, setOriginalImageUrl] = useState<string | null>(null);
  const [showCropper, setShowCropper] = useState(false);
  const [crop, setCrop] = useState({ x: 0, y: 0 });
  const [zoom, setZoom] = useState(1);
  const [croppedAreaPixels, setCroppedAreaPixels] = useState<{
    x: number;
    y: number;
    width: number;
    height: number;
  } | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);
  const [isDeleting, setIsDeleting] = useState(false);
  const [deleteVariantId, setDeleteVariantId] = useState<number | null>(null);
  const [deleteVariantIndex, setDeleteVariantIndex] = useState<number | null>(null);
  const [isTemporaryVariant, setIsTemporaryVariant] = useState(false);
  const [editingVariantId, setEditingVariantId] = useState<number | null>(null);
  const [editingTemporaryIndex, setEditingTemporaryIndex] = useState<number | null>(null);
  const [existingImageUrl, setExistingImageUrl] = useState<string | null>(null);
  const [imageRemoved, setImageRemoved] = useState(false);

  // Sync local state with prop changes (important for when variants are copied or refetched)
  useEffect(() => {
    setVariants(initialVariants);
  }, [initialVariants]);

  const createCroppedImage = async (imageUrl: string, cropArea: { x: number; y: number; width: number; height: number }): Promise<string> => {
    return new Promise((resolve, reject) => {
      const image = new Image();
      image.crossOrigin = 'anonymous';

      image.onload = () => {
        const canvas = document.createElement('canvas');
        const ctx = canvas.getContext('2d');

        if (!ctx) {
          reject(new Error('Failed to get canvas context'));
          return;
        }

        canvas.width = cropArea.width;
        canvas.height = cropArea.height;

        ctx.drawImage(image, cropArea.x, cropArea.y, cropArea.width, cropArea.height, 0, 0, cropArea.width, cropArea.height);
        canvas.toBlob(
          (blob) => {
            if (blob) {
              const croppedUrl = URL.createObjectURL(blob);
              resolve(croppedUrl);
            } else {
              reject(new Error('Failed to create blob from canvas'));
            }
          },
          'image/jpeg',
          0.9,
        );
      };

      image.onerror = () => {
        reject(new Error('Failed to load image'));
      };

      image.src = imageUrl;
    });
  };

  const handleImageUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    if (!file.type.startsWith('image/')) {
      toast.error('Please upload an image file');
      return;
    }
    const maxSize = 10 * 1024 * 1024;
    if (file.size > maxSize) {
      toast.error('File size exceeds maximum allowed size of 10MB');
      return;
    }

    // Clean up previous blob URLs if exist
    if (imagePreviewUrl) {
      URL.revokeObjectURL(imagePreviewUrl);
    }
    if (originalImageUrl) {
      URL.revokeObjectURL(originalImageUrl);
    }

    const blobUrl = URL.createObjectURL(file);
    setImageFile(file);
    setOriginalImageUrl(blobUrl);
    setImagePreviewUrl(blobUrl);
    setCrop({ x: 0, y: 0 });
    setZoom(1);
    setCroppedAreaPixels(null);
    setShowCropper(true);

    if (fileInputRef.current) {
      fileInputRef.current.value = '';
    }
  };

  const handleCropComplete = useCallback(
    (
      _croppedArea: { x: number; y: number; width: number; height: number },
      croppedAreaPixels: { x: number; y: number; width: number; height: number },
    ) => {
      setCroppedAreaPixels(croppedAreaPixels);
    },
    [],
  );

  const handleCropCancel = () => {
    setShowCropper(false);
    setCrop({ x: 0, y: 0 });
    setZoom(1);
    setCroppedAreaPixels(null);
    if (imagePreviewUrl) {
      URL.revokeObjectURL(imagePreviewUrl);
    }
    if (originalImageUrl) {
      URL.revokeObjectURL(originalImageUrl);
    }
    setImagePreviewUrl(null);
    setOriginalImageUrl(null);
    setImageFile(null);
  };

  const handleCropConfirm = async () => {
    if (croppedAreaPixels && originalImageUrl) {
      try {
        // Create cropped image preview
        const croppedUrl = await createCroppedImage(originalImageUrl, croppedAreaPixels);

        // Update the preview URL to show the cropped version
        if (imagePreviewUrl && imagePreviewUrl !== originalImageUrl) {
          URL.revokeObjectURL(imagePreviewUrl);
        }
        setImagePreviewUrl(croppedUrl);
      } catch (error) {
        console.error('Failed to create cropped preview:', error);
        toast.error('Failed to create image preview');
      }
    }
    setShowCropper(false);
  };

  const handleRemoveImage = () => {
    if (imagePreviewUrl && imagePreviewUrl !== existingImageUrl) {
      URL.revokeObjectURL(imagePreviewUrl);
    }
    if (originalImageUrl && originalImageUrl !== imagePreviewUrl) {
      URL.revokeObjectURL(originalImageUrl);
    }
    setImagePreviewUrl(null);
    setOriginalImageUrl(null);
    setImageFile(null);
    setCrop({ x: 0, y: 0 });
    setZoom(1);
    setCroppedAreaPixels(null);
    // Mark the existing image for removal but keep the URL reference
    if (existingImageUrl) {
      setImageRemoved(true);
      // Don't clear existingImageUrl here - we need it to know which image to remove
    }
  };

  const handleEditVariant = (variant: ArticleMugVariant) => {
    setEditingVariantId(variant.id);
    setEditingTemporaryIndex(null);
    setNewVariant({
      insideColorCode: variant.insideColorCode,
      outsideColorCode: variant.outsideColorCode,
      name: variant.name,
      articleVariantNumber: variant.articleVariantNumber || '',
      isDefault: variant.isDefault || false,
      active: variant.active ?? true,
    });
    setImageRemoved(false);

    // Handle existing image preview
    if (variant.exampleImageUrl) {
      setImagePreviewUrl(variant.exampleImageUrl);
      setExistingImageUrl(variant.exampleImageUrl);
    } else {
      setImagePreviewUrl(null);
      setExistingImageUrl(null);
    }
  };

  const handleEditTemporaryVariant = (index: number, variant: CreateArticleMugVariantRequest) => {
    setEditingTemporaryIndex(index);
    setEditingVariantId(null);
    setNewVariant({
      insideColorCode: variant.insideColorCode,
      outsideColorCode: variant.outsideColorCode,
      name: variant.name,
      articleVariantNumber: variant.articleVariantNumber || '',
      isDefault: variant.isDefault || false,
      active: variant.active ?? true,
    });
    setImageRemoved(false);
  };

  const handleCancelEdit = () => {
    setEditingVariantId(null);
    setEditingTemporaryIndex(null);
    setExistingImageUrl(null);
    setImageRemoved(false);
    setNewVariant({
      insideColorCode: '#ffffff',
      outsideColorCode: '#ffffff',
      name: '',
      articleVariantNumber: '',
      isDefault: false,
      active: true,
    });
    handleRemoveImage();
  };

  const handleAddOrUpdateVariant = async () => {
    if (!newVariant.name) {
      toast.error('Please enter a variant name');
      return;
    }

    const isEditing = editingVariantId !== null || editingTemporaryIndex !== null;

    // Check for duplicate variants (excluding the one being edited)
    const isDuplicate = articleId
      ? variants.some((v) => v.name === newVariant.name && v.id !== editingVariantId)
      : temporaryVariants.some((v, index) => v.name === newVariant.name && index !== editingTemporaryIndex);

    if (isDuplicate) {
      toast.error('A variant with this name already exists');
      return;
    }

    // Backend will handle ensuring only one variant is default

    if (!articleId) {
      // Handle temporary variant for unsaved article
      const variantToSave = { ...newVariant };

      // Store image file reference separately to be handled when article is saved
      // NOTE: Image files for temporary variants need to be stored in the parent component
      // and uploaded after the article and variant are created

      if (editingTemporaryIndex !== null && onUpdateTemporaryVariant) {
        // Update existing temporary variant
        onUpdateTemporaryVariant(editingTemporaryIndex, variantToSave);
        toast.success('Variant updated (will be saved with article)');
      } else if (onAddTemporaryVariant) {
        // Add new temporary variant
        onAddTemporaryVariant(variantToSave);
        toast.success('Variant added (will be saved with article)');
      }

      handleCancelEdit();
      return;
    }

    // Handle variant for saved article
    try {
      let response: ArticleMugVariant;

      if (isEditing && editingVariantId) {
        // Update existing variant without image data
        const updateRequest: CreateArticleMugVariantRequest = {
          insideColorCode: newVariant.insideColorCode,
          outsideColorCode: newVariant.outsideColorCode,
          name: newVariant.name,
          articleVariantNumber: newVariant.articleVariantNumber,
          isDefault: newVariant.isDefault,
          active: newVariant.active,
        };

        response = await articlesApi.updateMugVariant(editingVariantId, updateRequest);

        // Handle image removal if user removed the existing image
        if (imageRemoved && existingImageUrl && !imageFile) {
          try {
            // The removeMugVariantImage API returns the updated variant
            response = await articlesApi.removeMugVariantImage(editingVariantId);
            toast.success('Image removed successfully');
          } catch (error) {
            console.error('Error removing variant image:', error);
            toast.error('Failed to remove image');
          }
        }
      } else {
        // Create new variant without image data
        response = await articlesApi.createMugVariant(articleId, newVariant);
      }

      // Upload image if a new file was selected
      if (imageFile && response.id && croppedAreaPixels) {
        try {
          const imageResponse = await articlesApi.uploadMugVariantImage(response.id, imageFile, {
            x: croppedAreaPixels.x,
            y: croppedAreaPixels.y,
            width: croppedAreaPixels.width,
            height: croppedAreaPixels.height,
          });
          // Update the response with the data returned from the backend
          response.exampleImageFilename = imageResponse.exampleImageFilename;
          response.exampleImageUrl = imageResponse.exampleImageUrl;
        } catch (imageError) {
          console.error('Error uploading variant image:', imageError);
          toast.error(isEditing ? 'Variant updated but image upload failed' : 'Variant created but image upload failed');
        }
      }

      // Update the variant in the list
      if (isEditing) {
        // If we updated the default status, we need to refetch all variants
        // because the backend might have updated other variants' default status
        if (newVariant.isDefault && articleId) {
          try {
            // Refetch all variants for this article to get the updated default states
            const article = await articlesApi.getById(articleId);
            if (article.mugVariants) {
              setVariants(article.mugVariants);
            }
          } catch (error) {
            console.error('Error refetching variants:', error);
            // Fallback to just updating the single variant
            setVariants(variants.map((v) => (v.id === editingVariantId ? response : v)));
          }
        } else {
          // For non-default updates, just update the single variant
          setVariants(variants.map((v) => (v.id === editingVariantId ? response : v)));
        }
        toast.success('Variant updated successfully');
      } else {
        setVariants([...variants, response]);
        toast.success('Variant added successfully');
      }

      handleCancelEdit();
    } catch (error) {
      console.error(isEditing ? 'Error updating variant:' : 'Error adding variant:', error);
      toast.error(isEditing ? 'Failed to update variant' : 'Failed to add variant');
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
          {editingVariantId || editingTemporaryIndex !== null
            ? 'Edit the variant details below'
            : 'Add different color combinations for this mug. Each variant can have different inside and outside colors.'}
        </CardDescription>
      </CardHeader>
      <CardContent className="space-y-4">
        {!articleId && temporaryVariants.length > 0 && (
          <div className="rounded-lg border border-blue-200 bg-blue-50 p-4 text-sm text-blue-800">
            These variants will be saved when you save the article.
          </div>
        )}

        {/* Image Cropper Modal */}
        {showCropper && originalImageUrl && (
          <div className="bg-opacity-50 fixed inset-0 z-50 flex items-center justify-center bg-black">
            <div className="w-full max-w-2xl rounded-lg bg-white p-6">
              <div className="mb-4 flex items-center justify-between">
                <h3 className="text-lg font-semibold">Crop Image</h3>
                <Button variant="ghost" size="sm" onClick={handleCropCancel}>
                  <X className="h-4 w-4" />
                </Button>
              </div>
              <div className="mb-4">
                <div className="mb-4 text-center">
                  <h3 className="text-lg font-semibold">Crop your variant image</h3>
                  <p className="text-sm text-gray-600">Select the area you want to use for the variant thumbnail</p>
                </div>
                <div className="relative h-96">
                  <Cropper
                    image={originalImageUrl}
                    crop={crop}
                    zoom={zoom}
                    aspect={1}
                    minZoom={0.5}
                    restrictPosition={false}
                    onCropChange={setCrop}
                    onCropComplete={handleCropComplete}
                    onZoomChange={setZoom}
                    showGrid={true}
                    cropShape="rect"
                  />
                </div>
              </div>
              <div className="flex justify-end gap-2">
                <Button variant="outline" onClick={handleCropCancel}>
                  Cancel
                </Button>
                <Button onClick={handleCropConfirm}>Confirm Crop</Button>
              </div>
            </div>
          </div>
        )}

        <div className="space-y-4">
          <div className="grid grid-cols-1 gap-4 md:grid-cols-2 lg:grid-cols-3">
            <div className="space-y-2">
              <Label>Name *</Label>
              <Input
                value={newVariant.name}
                onChange={(e) => setNewVariant({ ...newVariant, name: e.target.value })}
                placeholder="e.g., Classic White"
                required
              />
            </div>

            <div className="space-y-2">
              <Label>Article Variant Number</Label>
              <Input
                value={newVariant.articleVariantNumber || ''}
                onChange={(e) => setNewVariant({ ...newVariant, articleVariantNumber: e.target.value })}
                placeholder="e.g., MUG-001"
                maxLength={100}
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
              <Label>Default Variant</Label>
              <div className="flex items-center space-x-2">
                <Checkbox
                  id="default-variant"
                  checked={newVariant.isDefault}
                  onCheckedChange={(checked) => setNewVariant({ ...newVariant, isDefault: checked === true })}
                />
                <Label htmlFor="default-variant" className="cursor-pointer text-sm font-normal">
                  Set as default variant
                </Label>
              </div>
            </div>

            <div className="space-y-2">
              <Label>Active Status</Label>
              <div className="flex items-center space-x-2">
                <Checkbox
                  id="active-variant"
                  checked={newVariant.active ?? true}
                  onCheckedChange={(checked) => setNewVariant({ ...newVariant, active: checked === true })}
                />
                <Label htmlFor="active-variant" className="cursor-pointer text-sm font-normal">
                  Variant is active and visible to customers
                </Label>
              </div>
            </div>

            <div className="space-y-2">
              <Label>Example Image</Label>
              <div className="space-y-2">
                {imagePreviewUrl && !showCropper ? (
                  <div className="relative inline-block">
                    <img src={imagePreviewUrl} alt="Preview" className="h-20 w-20 rounded border object-cover" />
                    <Button
                      type="button"
                      variant="ghost"
                      size="sm"
                      className="absolute -top-2 -right-2 h-6 w-6 rounded-full bg-red-500 p-0 text-white hover:bg-red-600"
                      onClick={handleRemoveImage}
                    >
                      <X className="h-4 w-4" />
                    </Button>
                  </div>
                ) : (
                  <div>
                    <input ref={fileInputRef} type="file" accept="image/*" onChange={handleImageUpload} className="hidden" />
                    <Button type="button" variant="outline" size="sm" onClick={() => fileInputRef.current?.click()} className="w-full">
                      <Upload className="mr-2 h-4 w-4" />
                      Upload Image
                    </Button>
                  </div>
                )}
              </div>
            </div>
          </div>

          <div className="flex items-center justify-end gap-4">
            {articleId && !(editingVariantId || editingTemporaryIndex !== null) && (
              <Button onClick={handleCopyVariants} variant="outline" className="min-w-[140px]">
                <Copy className="mr-2 h-4 w-4" />
                Copy Variants
              </Button>
            )}
            <div className={cn('flex gap-2', !articleId || editingVariantId || editingTemporaryIndex !== null ? 'w-full justify-end' : '')}>
              {(editingVariantId || editingTemporaryIndex !== null) && (
                <Button onClick={handleCancelEdit} variant="outline" className="min-w-[120px]">
                  Cancel
                </Button>
              )}
              <Button onClick={handleAddOrUpdateVariant} className="min-w-[120px]">
                {editingVariantId || editingTemporaryIndex !== null ? (
                  <>Update Variant</>
                ) : (
                  <>
                    <Plus className="mr-2 h-4 w-4" />
                    Add Variant
                  </>
                )}
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
                    className={cn(editingVariantId === variant.id ? 'bg-blue-50' : '', !variant.active ? 'bg-gray-50 opacity-60' : '')}
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
