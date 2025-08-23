import { Button } from '@/components/ui/Button';
import { Checkbox } from '@/components/ui/Checkbox';
import { ColorPicker } from '@/components/ui/ColorPicker';
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from '@/components/ui/Dialog';
import ImageCropperFixedBoxDialog from '@/components/ui/ImageCropperFixedBoxDialog';
import { Input } from '@/components/ui/Input';
import { Label } from '@/components/ui/Label';
import { articlesApi } from '@/lib/api';
import { blobUrlToFile } from '@/lib/image-utils';
import type { ArticleMugVariant, CreateArticleMugVariantRequest } from '@/types/article';
import { Upload, X } from 'lucide-react';
import { useEffect, useRef, useState } from 'react';
import { toast } from 'sonner';

interface MugVariantDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  articleId?: number;
  variant?: ArticleMugVariant;
  temporaryVariant?: CreateArticleMugVariantRequest;
  temporaryVariantIndex?: number;
  existingVariants: ArticleMugVariant[];
  existingTemporaryVariants: CreateArticleMugVariantRequest[];
  onVariantSaved?: (variant: ArticleMugVariant) => void;
  onTemporaryVariantSaved?: (variant: CreateArticleMugVariantRequest, index?: number) => void;
  onRefetchVariants?: () => void;
}

export default function MugVariantDialog({
  open,
  onOpenChange,
  articleId,
  variant,
  temporaryVariant,
  temporaryVariantIndex,
  existingVariants,
  existingTemporaryVariants,
  onVariantSaved,
  onTemporaryVariantSaved,
  onRefetchVariants,
}: MugVariantDialogProps) {
  const isEditing = !!(variant || temporaryVariant);
  const isTemporary = !articleId || !!temporaryVariant;
  const fileInputRef = useRef<HTMLInputElement>(null);

  const [formData, setFormData] = useState<CreateArticleMugVariantRequest>({
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
  const [existingImageUrl, setExistingImageUrl] = useState<string | null>(null);
  const [imageRemoved, setImageRemoved] = useState(false);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (open) {
      if (variant) {
        setFormData({
          insideColorCode: variant.insideColorCode,
          outsideColorCode: variant.outsideColorCode,
          name: variant.name,
          articleVariantNumber: variant.articleVariantNumber || '',
          isDefault: variant.isDefault || false,
          active: variant.active ?? true,
        });
        if (variant.exampleImageUrl) {
          setImagePreviewUrl(variant.exampleImageUrl);
          setExistingImageUrl(variant.exampleImageUrl);
        } else {
          setImagePreviewUrl(null);
          setExistingImageUrl(null);
        }
      } else if (temporaryVariant) {
        setFormData({
          insideColorCode: temporaryVariant.insideColorCode,
          outsideColorCode: temporaryVariant.outsideColorCode,
          name: temporaryVariant.name,
          articleVariantNumber: temporaryVariant.articleVariantNumber || '',
          isDefault: temporaryVariant.isDefault || false,
          active: temporaryVariant.active ?? true,
        });
      } else {
        setFormData({
          insideColorCode: '#ffffff',
          outsideColorCode: '#ffffff',
          name: '',
          articleVariantNumber: '',
          isDefault: false,
          active: true,
        });
        setImagePreviewUrl(null);
        setExistingImageUrl(null);
      }
      setImageRemoved(false);
      setImageFile(null);
      setOriginalImageUrl(null);
    } else {
      // Clean up when dialog closes
      if (imagePreviewUrl && imagePreviewUrl !== existingImageUrl) {
        URL.revokeObjectURL(imagePreviewUrl);
      }
      if (originalImageUrl && originalImageUrl !== imagePreviewUrl) {
        URL.revokeObjectURL(originalImageUrl);
      }
    }
  }, [open, variant, temporaryVariant, imagePreviewUrl, existingImageUrl, originalImageUrl]);

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
    if (imagePreviewUrl && imagePreviewUrl !== existingImageUrl) {
      URL.revokeObjectURL(imagePreviewUrl);
    }
    if (originalImageUrl) {
      URL.revokeObjectURL(originalImageUrl);
    }

    const blobUrl = URL.createObjectURL(file);
    setImageFile(file);
    setOriginalImageUrl(blobUrl);
    setImagePreviewUrl(blobUrl);
    setShowCropper(true);

    if (fileInputRef.current) {
      fileInputRef.current.value = '';
    }
  };

  const handleCropCancel = () => {
    setShowCropper(false);
    if (imagePreviewUrl && imagePreviewUrl !== existingImageUrl) {
      URL.revokeObjectURL(imagePreviewUrl);
    }
    if (originalImageUrl) {
      URL.revokeObjectURL(originalImageUrl);
    }
    setImagePreviewUrl(existingImageUrl);
    setOriginalImageUrl(null);
    setImageFile(null);
  };

  const handleCropConfirm = async (croppedImageUrl: string) => {
    try {
      // Update the preview URL to show the cropped version
      if (imagePreviewUrl && imagePreviewUrl !== originalImageUrl && imagePreviewUrl !== existingImageUrl) {
        URL.revokeObjectURL(imagePreviewUrl);
      }
      setImagePreviewUrl(croppedImageUrl);
      
      // Convert the cropped blob URL to a File object to replace the original
      if (imageFile) {
        const croppedFile = await blobUrlToFile(croppedImageUrl, imageFile.name, imageFile.type);
        setImageFile(croppedFile);
      }
    } catch (error) {
      console.error('Failed to process cropped image:', error);
      toast.error('Failed to process cropped image');
    }
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
    // Mark the existing image for removal but keep the URL reference
    if (existingImageUrl) {
      setImageRemoved(true);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!formData.name) {
      toast.error('Please enter a variant name');
      return;
    }

    // Check for duplicate variants (excluding the one being edited)
    const isDuplicate = isTemporary
      ? existingTemporaryVariants.some((v, index) => v.name === formData.name && index !== temporaryVariantIndex)
      : existingVariants.some((v) => v.name === formData.name && v.id !== variant?.id);

    if (isDuplicate) {
      toast.error('A variant with this name already exists');
      return;
    }

    if (isTemporary) {
      // Handle temporary variant for unsaved article
      const variantToSave = { ...formData };
      if (onTemporaryVariantSaved) {
        onTemporaryVariantSaved(variantToSave, temporaryVariantIndex);
        toast.success(isEditing ? 'Variant updated (will be saved with article)' : 'Variant added (will be saved with article)');
      }
      onOpenChange(false);
      return;
    }

    if (!articleId) return;

    // Handle variant for saved article
    try {
      setLoading(true);
      let response: ArticleMugVariant;

      if (isEditing && variant) {
        // Update existing variant without image data
        const updateRequest: CreateArticleMugVariantRequest = {
          insideColorCode: formData.insideColorCode,
          outsideColorCode: formData.outsideColorCode,
          name: formData.name,
          articleVariantNumber: formData.articleVariantNumber,
          isDefault: formData.isDefault,
          active: formData.active,
        };

        response = await articlesApi.updateMugVariant(variant.id, updateRequest);

        // Handle image removal if user removed the existing image
        if (imageRemoved && existingImageUrl && !imageFile) {
          try {
            // The removeMugVariantImage API returns the updated variant
            response = await articlesApi.removeMugVariantImage(variant.id);
            toast.success('Image removed successfully');
          } catch (error) {
            console.error('Error removing variant image:', error);
            toast.error('Failed to remove image');
          }
        }
      } else {
        // Create new variant without image data
        response = await articlesApi.createMugVariant(articleId, formData);
      }

      // Upload image if a new file was selected
      if (imageFile && response.id) {
        try {
          const imageResponse = await articlesApi.uploadMugVariantImage(response.id, imageFile);
          // Update the response with the data returned from the backend
          response.exampleImageFilename = imageResponse.exampleImageFilename;
          response.exampleImageUrl = imageResponse.exampleImageUrl;
        } catch (imageError) {
          console.error('Error uploading variant image:', imageError);
          toast.error(isEditing ? 'Variant updated but image upload failed' : 'Variant created but image upload failed');
        }
      }

      // If we updated the default status, we need to refetch all variants
      if (formData.isDefault && onRefetchVariants) {
        onRefetchVariants();
      } else if (onVariantSaved) {
        onVariantSaved(response);
      }

      toast.success(isEditing ? 'Variant updated successfully' : 'Variant added successfully');
      onOpenChange(false);
    } catch (error) {
      console.error(isEditing ? 'Error updating variant:' : 'Error adding variant:', error);
      toast.error(isEditing ? 'Failed to update variant' : 'Failed to add variant');
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = () => {
    onOpenChange(false);
  };

  return (
    <>
      <ImageCropperFixedBoxDialog
        open={showCropper && !!originalImageUrl}
        onOpenChange={(open) => !open && handleCropCancel()}
        srcImage={originalImageUrl || ''}
        aspectRatio={1}
        onConfirm={handleCropConfirm}
        title="Crop your variant image"
        description="Select the area you want to use for the variant thumbnail"
      />

      <Dialog open={open} onOpenChange={onOpenChange}>
        <DialogContent className="sm:max-w-[600px] max-h-[90vh] overflow-y-auto">
          <form onSubmit={handleSubmit}>
            <DialogHeader>
              <DialogTitle>
                {isEditing ? 'Edit Mug Variant' : 'Add New Mug Variant'}
              </DialogTitle>
              <DialogDescription>
                {isEditing 
                  ? 'Update the variant details below' 
                  : 'Create a new mug variant with different colors and settings'
                }
              </DialogDescription>
            </DialogHeader>
            
            <div className="grid gap-6 py-6">
              <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
                <div className="space-y-2">
                  <Label htmlFor="name">Name *</Label>
                  <Input
                    id="name"
                    value={formData.name}
                    onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                    placeholder="e.g., Classic White"
                    required
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="articleVariantNumber">Article Variant Number</Label>
                  <Input
                    id="articleVariantNumber"
                    value={formData.articleVariantNumber || ''}
                    onChange={(e) => setFormData({ ...formData, articleVariantNumber: e.target.value })}
                    placeholder="e.g., MUG-001"
                    maxLength={100}
                  />
                </div>

                <div className="space-y-2">
                  <Label>Inside Color</Label>
                  <ColorPicker 
                    value={formData.insideColorCode} 
                    onChange={(color) => setFormData({ ...formData, insideColorCode: color })} 
                  />
                </div>

                <div className="space-y-2">
                  <Label>Outside Color</Label>
                  <ColorPicker 
                    value={formData.outsideColorCode} 
                    onChange={(color) => setFormData({ ...formData, outsideColorCode: color })} 
                  />
                </div>
              </div>

              <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
                <div className="space-y-2">
                  <Label>Default Variant</Label>
                  <div className="flex items-center space-x-2">
                    <Checkbox
                      id="default-variant"
                      checked={formData.isDefault}
                      onCheckedChange={(checked) => setFormData({ ...formData, isDefault: checked === true })}
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
                      checked={formData.active ?? true}
                      onCheckedChange={(checked) => setFormData({ ...formData, active: checked === true })}
                    />
                    <Label htmlFor="active-variant" className="cursor-pointer text-sm font-normal">
                      Variant is active and visible to customers
                    </Label>
                  </div>
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
                      <input 
                        ref={fileInputRef} 
                        type="file" 
                        accept="image/*" 
                        onChange={handleImageUpload} 
                        className="hidden" 
                      />
                      <Button 
                        type="button" 
                        variant="outline" 
                        size="sm" 
                        onClick={() => fileInputRef.current?.click()} 
                        className="w-full max-w-[200px]"
                      >
                        <Upload className="mr-2 h-4 w-4" />
                        Upload Image
                      </Button>
                    </div>
                  )}
                </div>
              </div>
            </div>

            <DialogFooter>
              <Button type="button" variant="outline" onClick={handleCancel}>
                Cancel
              </Button>
              <Button type="submit" disabled={loading}>
                {loading ? 'Saving...' : isEditing ? 'Update Variant' : 'Add Variant'}
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>
    </>
  );
}