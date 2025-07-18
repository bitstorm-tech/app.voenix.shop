import { Button } from '@/components/ui/Button';
import { ColorPicker } from '@/components/ui/ColorPicker';
import { Input } from '@/components/ui/Input';
import { Label } from '@/components/ui/Label';
import type { CreateMugVariantRequest, UpdateMugVariantRequest } from '@/lib/api';
import { imagesApi, mugVariantsApi } from '@/lib/api';
import type { MugVariant } from '@/types/mug';
import { useEffect, useRef, useState } from 'react';

interface VariantsTabProps {
  mugId?: number;
}

export default function VariantsTab({ mugId }: VariantsTabProps) {
  const [variants, setVariants] = useState<MugVariant[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [isAddingVariant, setIsAddingVariant] = useState(false);
  const [editingVariantId, setEditingVariantId] = useState<number | null>(null);

  // New variant form state
  const [newVariant, setNewVariant] = useState<Omit<CreateMugVariantRequest, 'mugId'>>({
    colorCode: '#000000',
    exampleImageFilename: '',
  });
  const [newVariantImageFile, setNewVariantImageFile] = useState<File | null>(null);
  const [newVariantImageUrl, setNewVariantImageUrl] = useState<string | null>(null);
  const newVariantFileInputRef = useRef<HTMLInputElement>(null);

  // Edit variant state
  const [editVariant, setEditVariant] = useState<UpdateMugVariantRequest>({});
  const [editVariantImageFile, setEditVariantImageFile] = useState<File | null>(null);
  const [editVariantImageUrl, setEditVariantImageUrl] = useState<string | null>(null);
  const editVariantFileInputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    if (mugId) {
      fetchVariants();
    }
  }, [mugId]);

  // Cleanup blob URLs on unmount
  useEffect(() => {
    return () => {
      if (newVariantImageUrl && newVariantImageUrl.startsWith('blob:')) {
        URL.revokeObjectURL(newVariantImageUrl);
      }
      if (editVariantImageUrl && editVariantImageUrl.startsWith('blob:')) {
        URL.revokeObjectURL(editVariantImageUrl);
      }
    };
  }, [newVariantImageUrl, editVariantImageUrl]);

  const fetchVariants = async () => {
    if (!mugId) return;

    try {
      setLoading(true);
      const data = await mugVariantsApi.getByMugId(mugId);
      setVariants(data);
    } catch (error) {
      console.error('Error fetching variants:', error);
      setError('Failed to load variants');
    } finally {
      setLoading(false);
    }
  };

  const handleAddVariant = async () => {
    if (!mugId) return;

    if (!newVariantImageFile) {
      setError('Please select an image for the variant');
      return;
    }

    try {
      setLoading(true);
      setError(null);

      // Upload image first
      const uploadResult = await imagesApi.upload(newVariantImageFile, 'PUBLIC');

      // Create variant
      const createRequest: CreateMugVariantRequest = {
        mugId,
        colorCode: newVariant.colorCode,
        exampleImageFilename: uploadResult.filename,
      };

      const createdVariant = await mugVariantsApi.create(createRequest);
      setVariants([...variants, createdVariant]);

      // Reset form
      setIsAddingVariant(false);
      setNewVariant({ colorCode: '#000000', exampleImageFilename: '' });
      setNewVariantImageFile(null);
      if (newVariantImageUrl && newVariantImageUrl.startsWith('blob:')) {
        URL.revokeObjectURL(newVariantImageUrl);
      }
      setNewVariantImageUrl(null);
    } catch (error) {
      console.error('Error creating variant:', error);
      setError('Failed to create variant');
    } finally {
      setLoading(false);
    }
  };

  const handleUpdateVariant = async (variantId: number) => {
    try {
      setLoading(true);
      setError(null);

      let updateData: UpdateMugVariantRequest = { ...editVariant };

      // Upload new image if selected
      if (editVariantImageFile) {
        const uploadResult = await imagesApi.upload(editVariantImageFile, 'PUBLIC');
        updateData.exampleImageFilename = uploadResult.filename;
      }

      const updatedVariant = await mugVariantsApi.update(variantId, updateData);
      setVariants(variants.map((v) => (v.id === variantId ? updatedVariant : v)));

      // Reset edit state
      setEditingVariantId(null);
      setEditVariant({});
      setEditVariantImageFile(null);
      if (editVariantImageUrl && editVariantImageUrl.startsWith('blob:')) {
        URL.revokeObjectURL(editVariantImageUrl);
      }
      setEditVariantImageUrl(null);
    } catch (error) {
      console.error('Error updating variant:', error);
      setError('Failed to update variant');
    } finally {
      setLoading(false);
    }
  };

  const handleDeleteVariant = async (variantId: number) => {
    if (!confirm('Are you sure you want to delete this variant?')) {
      return;
    }

    try {
      setLoading(true);
      setError(null);
      await mugVariantsApi.delete(variantId);
      setVariants(variants.filter((v) => v.id !== variantId));
    } catch (error) {
      console.error('Error deleting variant:', error);
      setError('Failed to delete variant');
    } finally {
      setLoading(false);
    }
  };

  const handleImageUpload = (e: React.ChangeEvent<HTMLInputElement>, isEdit: boolean = false) => {
    const file = e.target.files?.[0];
    if (!file || !file.type.startsWith('image/')) {
      setError('Please select a valid image file');
      return;
    }

    // Check file size (10MB limit)
    const maxSize = 10 * 1024 * 1024;
    if (file.size > maxSize) {
      setError('File size exceeds maximum allowed size of 10MB');
      return;
    }

    if (isEdit) {
      // Clean up previous blob URL if exists
      if (editVariantImageUrl && editVariantImageUrl.startsWith('blob:')) {
        URL.revokeObjectURL(editVariantImageUrl);
      }

      const blobUrl = URL.createObjectURL(file);
      setEditVariantImageFile(file);
      setEditVariantImageUrl(blobUrl);
    } else {
      // Clean up previous blob URL if exists
      if (newVariantImageUrl && newVariantImageUrl.startsWith('blob:')) {
        URL.revokeObjectURL(newVariantImageUrl);
      }

      const blobUrl = URL.createObjectURL(file);
      setNewVariantImageFile(file);
      setNewVariantImageUrl(blobUrl);
    }

    setError(null);
  };

  const startEditingVariant = (variant: MugVariant) => {
    setEditingVariantId(variant.id);
    setEditVariant({ colorCode: variant.colorCode });
    setEditVariantImageUrl(variant.exampleImageUrl);
  };

  const cancelEditing = () => {
    setEditingVariantId(null);
    setEditVariant({});
    setEditVariantImageFile(null);
    if (editVariantImageUrl && editVariantImageUrl.startsWith('blob:')) {
      URL.revokeObjectURL(editVariantImageUrl);
    }
    setEditVariantImageUrl(null);
  };

  const cancelAddingVariant = () => {
    setIsAddingVariant(false);
    setNewVariant({ colorCode: '#000000', exampleImageFilename: '' });
    setNewVariantImageFile(null);
    if (newVariantImageUrl && newVariantImageUrl.startsWith('blob:')) {
      URL.revokeObjectURL(newVariantImageUrl);
    }
    setNewVariantImageUrl(null);
  };

  if (!mugId) {
    return (
      <div className="text-center text-gray-500">
        <p>Please save the mug first to manage variants.</p>
      </div>
    );
  }

  if (loading && variants.length === 0 && !isAddingVariant) {
    return (
      <div className="text-center text-gray-500">
        <p>Loading variants...</p>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {error && <div className="rounded border border-red-200 bg-red-50 px-4 py-3 text-red-700">{error}</div>}

      {/* Variants List */}
      <div className="space-y-4">
        {variants.map((variant) => (
          <div key={variant.id} className="overflow-hidden rounded-lg border bg-white">
            {editingVariantId === variant.id ? (
              // Edit Mode
              <div className="p-4">
                <div className="flex flex-col gap-4 sm:flex-row sm:items-center">
                  <div className="flex items-center gap-4">
                    <div className="flex-shrink-0">
                      <Label htmlFor={`edit-color-${variant.id}`} className="text-xs text-gray-500">Color</Label>
                      <ColorPicker
                        id={`edit-color-${variant.id}`}
                        value={editVariant.colorCode || variant.colorCode}
                        onChange={(e) => setEditVariant({ ...editVariant, colorCode: e.target.value })}
                      />
                    </div>
                    <div className="flex-shrink-0">
                      {editVariantImageUrl && (
                        <img src={editVariantImageUrl} alt="Variant preview" className="h-20 w-20 rounded-md object-cover" />
                      )}
                    </div>
                  </div>

                  <div className="flex-grow">
                    <Label htmlFor={`edit-image-${variant.id}`} className="text-xs text-gray-500">Example Image</Label>
                    <Input
                      id={`edit-image-${variant.id}`}
                      type="file"
                      accept="image/*"
                      ref={editVariantFileInputRef}
                      onChange={(e) => handleImageUpload(e, true)}
                      className="mt-1"
                    />
                  </div>

                  <div className="flex gap-2 sm:flex-shrink-0">
                    <Button size="sm" onClick={() => handleUpdateVariant(variant.id)} disabled={loading}>
                      Save
                    </Button>
                    <Button size="sm" variant="outline" onClick={cancelEditing} disabled={loading}>
                      Cancel
                    </Button>
                  </div>
                </div>
              </div>
            ) : (
              // View Mode
              <div className="flex flex-col items-center gap-4 p-4 sm:flex-row">
                <img 
                  src={variant.exampleImageUrl} 
                  alt={`Variant ${variant.colorCode}`} 
                  className="h-20 w-20 flex-shrink-0 rounded-md object-cover" 
                />
                <div className="flex flex-grow items-center gap-3">
                  <div className="h-10 w-10 flex-shrink-0 rounded-full border-2 border-gray-300" style={{ backgroundColor: variant.colorCode }} />
                  <span className="font-medium">{variant.colorCode}</span>
                </div>
                <div className="flex gap-2 sm:flex-shrink-0">
                  <Button size="sm" variant="outline" onClick={() => startEditingVariant(variant)} disabled={loading}>
                    Edit
                  </Button>
                  <Button size="sm" variant="destructive" onClick={() => handleDeleteVariant(variant.id)} disabled={loading}>
                    Delete
                  </Button>
                </div>
              </div>
            )}
          </div>
        ))}

        {/* Add New Variant */}
        {isAddingVariant ? (
          <div className="overflow-hidden rounded-lg border border-dashed bg-gray-50">
            <div className="p-4">
              <div className="flex flex-col gap-4 sm:flex-row sm:items-center">
                <div className="flex items-center gap-4">
                  <div className="flex-shrink-0">
                    <Label htmlFor="new-color" className="text-xs text-gray-500">Color</Label>
                    <ColorPicker
                      id="new-color"
                      value={newVariant.colorCode}
                      onChange={(e) => setNewVariant({ ...newVariant, colorCode: e.target.value })}
                    />
                  </div>
                  <div className="flex-shrink-0">
                    {newVariantImageUrl && (
                      <img src={newVariantImageUrl} alt="New variant preview" className="h-20 w-20 rounded-md object-cover" />
                    )}
                  </div>
                </div>

                <div className="flex-grow">
                  <Label htmlFor="new-image" className="text-xs text-gray-500">Example Image</Label>
                  <Input 
                    id="new-image" 
                    type="file" 
                    accept="image/*" 
                    ref={newVariantFileInputRef} 
                    onChange={(e) => handleImageUpload(e, false)} 
                    className="mt-1"
                  />
                </div>

                <div className="flex gap-2 sm:flex-shrink-0">
                  <Button size="sm" onClick={handleAddVariant} disabled={loading}>
                    Add Variant
                  </Button>
                  <Button size="sm" variant="outline" onClick={cancelAddingVariant} disabled={loading}>
                    Cancel
                  </Button>
                </div>
              </div>
            </div>
          </div>
        ) : (
          <button
            className="flex w-full items-center justify-center rounded-lg border border-dashed border-gray-300 bg-gray-50 p-4 transition-colors hover:border-gray-400 hover:bg-gray-100"
            onClick={() => setIsAddingVariant(true)}
          >
            <div className="flex items-center gap-2 text-gray-500">
              <span className="text-2xl">+</span>
              <span className="text-sm font-medium">Add Variant</span>
            </div>
          </button>
        )}
      </div>
    </div>
  );
}
