import ImageCropper from '@/components/editor/components/shared/ImageCropper';
import { Button } from '@/components/ui/Button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/Card';
import { Checkbox } from '@/components/ui/Checkbox';
import { ColorPicker } from '@/components/ui/ColorPicker';
import ConfirmationDialog from '@/components/ui/ConfirmationDialog';
import { Input } from '@/components/ui/Input';
import { Label } from '@/components/ui/Label';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/Table';
import { articlesApi } from '@/lib/api';
import type { ArticleMugVariant, CreateArticleMugVariantRequest } from '@/types/article';
import { Image as ImageIcon, Plus, Trash2, Upload, X } from 'lucide-react';
import { useRef, useState } from 'react';
import { type PixelCrop } from 'react-image-crop';
import 'react-image-crop/dist/ReactCrop.css';
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
    exampleImageFilename: '',
    supplierArticleNumber: '',
    isDefault: false,
  });
  const [imageFile, setImageFile] = useState<File | null>(null);
  const [imagePreviewUrl, setImagePreviewUrl] = useState<string | null>(null);
  const [showCropper, setShowCropper] = useState(false);
  const [cropData, setCropData] = useState<PixelCrop | null>(null);
  const [imageDimensions, setImageDimensions] = useState<{
    natural: { width: number; height: number };
    displayed: { width: number; height: number };
  } | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);
  const [isDeleting, setIsDeleting] = useState(false);
  const [deleteVariantId, setDeleteVariantId] = useState<number | null>(null);
  const [deleteVariantIndex, setDeleteVariantIndex] = useState<number | null>(null);
  const [isTemporaryVariant, setIsTemporaryVariant] = useState(false);

  const handleImageUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    // Check file type
    if (!file.type.startsWith('image/')) {
      toast.error('Please upload an image file');
      return;
    }

    // Check file size (10MB limit)
    const maxSize = 10 * 1024 * 1024;
    if (file.size > maxSize) {
      toast.error('File size exceeds maximum allowed size of 10MB');
      return;
    }

    // Clean up previous blob URL if exists
    if (imagePreviewUrl) {
      URL.revokeObjectURL(imagePreviewUrl);
    }

    // Create blob URL for preview
    const blobUrl = URL.createObjectURL(file);
    setImageFile(file);
    setImagePreviewUrl(blobUrl);
    setShowCropper(true);

    // Reset the input
    if (fileInputRef.current) {
      fileInputRef.current.value = '';
    }
  };

  const handleCropComplete = (
    crop: PixelCrop,
    dimensions: { natural: { width: number; height: number }; displayed: { width: number; height: number } },
  ) => {
    setCropData(crop);
    setImageDimensions(dimensions);
  };

  const handleCropCancel = () => {
    setShowCropper(false);
    setCropData(null);
    setImageDimensions(null);
    if (imagePreviewUrl) {
      URL.revokeObjectURL(imagePreviewUrl);
    }
    setImagePreviewUrl(null);
    setImageFile(null);
  };

  const handleCropConfirm = () => {
    setShowCropper(false);
  };

  const handleRemoveImage = () => {
    if (imagePreviewUrl) {
      URL.revokeObjectURL(imagePreviewUrl);
    }
    setImagePreviewUrl(null);
    setImageFile(null);
    setCropData(null);
    setImageDimensions(null);
    setNewVariant({ ...newVariant, exampleImageFilename: '' });
  };

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

    // Check if setting as default when another default exists
    if (newVariant.isDefault) {
      const hasDefault = articleId ? variants.some((v) => v.isDefault) : temporaryVariants.some((v) => v.isDefault);
      if (hasDefault) {
        toast.error('Another variant is already set as default. Please unset it first.');
        return;
      }
    }

    if (!articleId) {
      // Handle temporary variant for unsaved article
      if (onAddTemporaryVariant) {
        // Store image data if present
        const variantToAdd = { ...newVariant };
        if (imageFile) {
          // For temporary variants, we'll store the filename and handle upload when article is saved
          variantToAdd.exampleImageFilename = imageFile.name;
        }
        onAddTemporaryVariant(variantToAdd);
        setNewVariant({
          insideColorCode: '#ffffff',
          outsideColorCode: '#ffffff',
          name: '',
          exampleImageFilename: '',
          supplierArticleNumber: '',
          isDefault: false,
        });
        handleRemoveImage();
        toast.success('Variant added (will be saved with article)');
      }
      return;
    }

    // Handle variant for saved article
    try {
      const response = await articlesApi.createMugVariant(articleId, newVariant);

      // Upload image if present
      if (imageFile && response.id && cropData) {
        try {
          // Calculate the scaling factor if we have image dimensions
          let scaledCropData = cropData;
          if (imageDimensions) {
            // Calculate scale factors from stored dimensions
            const scaleX = imageDimensions.natural.width / imageDimensions.displayed.width;
            const scaleY = imageDimensions.natural.height / imageDimensions.displayed.height;

            // Scale the crop coordinates to match the original image dimensions
            scaledCropData = {
              x: cropData.x * scaleX,
              y: cropData.y * scaleY,
              width: cropData.width * scaleX,
              height: cropData.height * scaleY,
              unit: 'px' as const,
            };

            console.log('Image crop scaling:', {
              cropData,
              scaledCropData,
              natural: imageDimensions.natural,
              displayed: imageDimensions.displayed,
              scaleX,
              scaleY,
            });
          }

          const imageResponse = await articlesApi.uploadMugVariantImage(response.id, imageFile, {
            x: scaledCropData.x,
            y: scaledCropData.y,
            width: scaledCropData.width,
            height: scaledCropData.height,
          });
          // Update the response with the data returned from the backend
          response.exampleImageFilename = imageResponse.exampleImageFilename;
          response.exampleImageUrl = imageResponse.exampleImageUrl;
        } catch (imageError) {
          console.error('Error uploading variant image:', imageError);
          toast.error('Variant created but image upload failed');
        }
      }

      setVariants([...variants, response]);
      setNewVariant({
        insideColorCode: '#ffffff',
        outsideColorCode: '#ffffff',
        name: '',
        exampleImageFilename: '',
        supplierArticleNumber: '',
        isDefault: false,
      });
      handleRemoveImage();
      setImageDimensions(null);
      toast.success('Variant added successfully');
    } catch (error) {
      console.error('Error adding variant:', error);
      toast.error('Failed to add variant');
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

        {/* Image Cropper Modal */}
        {showCropper && imagePreviewUrl && (
          <div className="bg-opacity-50 fixed inset-0 z-50 flex items-center justify-center bg-black">
            <div className="w-full max-w-2xl rounded-lg bg-white p-6">
              <div className="mb-4 flex items-center justify-between">
                <h3 className="text-lg font-semibold">Crop Image</h3>
                <Button variant="ghost" size="sm" onClick={handleCropCancel}>
                  <X className="h-4 w-4" />
                </Button>
              </div>
              <div className="mb-4">
                <ImageCropper
                  imageUrl={imagePreviewUrl}
                  onCropComplete={handleCropComplete}
                  aspect={1}
                  showGrid={true}
                  title="Crop your variant image"
                  description="Select the area you want to use for the variant thumbnail"
                />
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
              <Label>Supplier Article Number</Label>
              <Input
                value={newVariant.supplierArticleNumber || ''}
                onChange={(e) => setNewVariant({ ...newVariant, supplierArticleNumber: e.target.value })}
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

          <div className="flex justify-end">
            <Button onClick={handleAddVariant} className="min-w-[120px]">
              <Plus className="mr-2 h-4 w-4" />
              Add Variant
            </Button>
          </div>
        </div>

        {/* Show saved variants */}
        {variants.length > 0 && (
          <div className="rounded-md border">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Name</TableHead>
                  <TableHead>Supplier Article Number</TableHead>
                  <TableHead>Inside Color</TableHead>
                  <TableHead>Outside Color</TableHead>
                  <TableHead>Default</TableHead>
                  <TableHead>Example Image</TableHead>
                  <TableHead className="text-right">Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {variants.map((variant) => (
                  <TableRow key={variant.id}>
                    <TableCell className="font-medium">{variant.name}</TableCell>
                    <TableCell>{variant.supplierArticleNumber || '-'}</TableCell>
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
                      {variant.exampleImageUrl ? (
                        <img src={variant.exampleImageUrl} alt={`${variant.name} example`} className="h-10 w-10 rounded border object-cover" />
                      ) : (
                        <div className="flex h-10 w-10 items-center justify-center rounded border bg-gray-100">
                          <ImageIcon className="h-4 w-4 text-gray-400" />
                        </div>
                      )}
                    </TableCell>
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
                  <TableHead>Supplier Article Number</TableHead>
                  <TableHead>Inside Color</TableHead>
                  <TableHead>Outside Color</TableHead>
                  <TableHead>Default</TableHead>
                  <TableHead>Example Image</TableHead>
                  <TableHead className="text-right">Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {temporaryVariants.map((variant, index) => (
                  <TableRow key={index}>
                    <TableCell className="font-medium">{variant.name}</TableCell>
                    <TableCell>{variant.supplierArticleNumber || '-'}</TableCell>
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
                      {variant.exampleImageFilename ? (
                        <div className="flex h-10 w-10 items-center justify-center rounded border bg-blue-100">
                          <ImageIcon className="h-4 w-4 text-blue-600" />
                        </div>
                      ) : (
                        <div className="flex h-10 w-10 items-center justify-center rounded border bg-gray-100">
                          <ImageIcon className="h-4 w-4 text-gray-400" />
                        </div>
                      )}
                    </TableCell>
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
