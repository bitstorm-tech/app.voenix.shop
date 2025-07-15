import { Button } from '@/components/ui/Button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/Card';
import { Checkbox } from '@/components/ui/Checkbox';
import { Input } from '@/components/ui/Input';
import { Label } from '@/components/ui/Label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/Select';
import { Textarea } from '@/components/ui/Textarea';
import type { CreateMugRequest, UpdateMugRequest } from '@/lib/api';
import { imagesApi, mugCategoriesApi, mugSubCategoriesApi, mugsApi } from '@/lib/api';
import type { MugCategory, MugSubCategory } from '@/types/mug';
import { Upload, X } from 'lucide-react';
import { useEffect, useRef, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';

export default function NewOrEditMug() {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const isEditing = !!id;

  const [formData, setFormData] = useState<CreateMugRequest>({
    name: '',
    descriptionLong: '',
    descriptionShort: '',
    image: '',
    price: 0,
    heightMm: 0,
    diameterMm: 0,
    printTemplateWidthMm: 0,
    printTemplateHeightMm: 0,
    fillingQuantity: '',
    dishwasherSafe: true,
    active: true,
    categoryId: undefined,
    subCategoryId: undefined,
  });
  const [categories, setCategories] = useState<MugCategory[]>([]);
  const [subCategories, setSubCategories] = useState<MugSubCategory[]>([]);
  const [loading, setLoading] = useState(false);
  const [initialLoading, setInitialLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [imageUrl, setImageUrl] = useState<string | null>(null);
  const [imageFile, setImageFile] = useState<File | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    fetchCategories();
    if (isEditing) {
      fetchMug();
    } else {
      setInitialLoading(false);
    }
  }, [id]);

  // Cleanup blob URLs on unmount
  useEffect(() => {
    return () => {
      if (imageUrl && imageUrl.startsWith('blob:')) {
        URL.revokeObjectURL(imageUrl);
      }
    };
  }, [imageUrl]);

  useEffect(() => {
    if (formData.categoryId) {
      fetchSubCategories(formData.categoryId);
    } else {
      setSubCategories([]);
      setFormData((prev) => ({ ...prev, subCategoryId: undefined }));
    }
  }, [formData.categoryId]);

  const fetchCategories = async () => {
    try {
      const data = await mugCategoriesApi.getAll();
      setCategories(data);
    } catch (error) {
      console.error('Error fetching mug categories:', error);
      setError('Failed to load mug categories');
    }
  };

  const fetchSubCategories = async (categoryId: number) => {
    try {
      const data = await mugSubCategoriesApi.getByCategoryId(categoryId);
      setSubCategories(data);
    } catch (error) {
      console.error('Error fetching mug subcategories:', error);
    }
  };

  const fetchMug = async () => {
    if (!id) return;

    try {
      setInitialLoading(true);
      const mug = await mugsApi.getById(parseInt(id));
      setFormData({
        name: mug.name,
        descriptionLong: mug.descriptionLong,
        descriptionShort: mug.descriptionShort,
        image: mug.image,
        price: mug.price,
        heightMm: mug.heightMm,
        diameterMm: mug.diameterMm,
        printTemplateWidthMm: mug.printTemplateWidthMm,
        printTemplateHeightMm: mug.printTemplateHeightMm,
        fillingQuantity: mug.fillingQuantity || '',
        dishwasherSafe: mug.dishwasherSafe,
        active: mug.active,
        categoryId: mug.category?.id,
        subCategoryId: mug.subCategory?.id,
      });

      // Set image if exists
      if (mug.image) {
        setImageUrl(mug.image);
      }
    } catch (error) {
      console.error('Error fetching mug:', error);
      setError('Failed to load mug');
    } finally {
      setInitialLoading(false);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!formData.name.trim()) {
      setError('Name is required');
      return;
    }

    if (!formData.descriptionShort.trim()) {
      setError('Short description is required');
      return;
    }

    if (!formData.descriptionLong.trim()) {
      setError('Long description is required');
      return;
    }

    if (formData.price <= 0) {
      setError('Price must be greater than 0');
      return;
    }

    if (formData.heightMm <= 0 || formData.diameterMm <= 0) {
      setError('Height and diameter must be greater than 0');
      return;
    }

    if (formData.printTemplateWidthMm <= 0 || formData.printTemplateHeightMm <= 0) {
      setError('Print template dimensions must be greater than 0');
      return;
    }

    try {
      setLoading(true);
      setError(null);

      let finalImageUrl = formData.image;

      // Upload new image if there is one
      if (imageFile) {
        try {
          const uploadResult = await imagesApi.upload(imageFile, 'PUBLIC');
          // Construct the full URL for the uploaded image
          finalImageUrl = `/api/images/${uploadResult.filename}`;
        } catch (uploadError: any) {
          console.error('Error uploading image:', uploadError);
          setError(`Failed to upload image: ${uploadError.message || 'Please try again.'}`);
          setLoading(false);
          return;
        }
      } else if (imageUrl && !imageUrl.startsWith('blob:')) {
        // Keep existing image URL if not uploading a new one
        finalImageUrl = imageUrl;
      }

      if (isEditing) {
        const updateData: UpdateMugRequest = {
          ...formData,
          image: finalImageUrl || '',
          fillingQuantity: formData.fillingQuantity || undefined,
        };
        await mugsApi.update(parseInt(id), updateData);
      } else {
        await mugsApi.create({
          ...formData,
          image: finalImageUrl || '',
        });
      }

      navigate('/admin/mugs');
    } catch (error) {
      console.error('Error saving mug:', error);
      setError('Failed to save mug. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = () => {
    navigate('/admin/mugs');
  };

  const handleImageUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
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

    // Clean up previous blob URL if exists
    if (imageUrl && imageUrl.startsWith('blob:')) {
      URL.revokeObjectURL(imageUrl);
    }

    // Create blob URL for preview
    const blobUrl = URL.createObjectURL(file);
    setImageFile(file);
    setImageUrl(blobUrl);
    setError(null);

    // Reset the input
    if (fileInputRef.current) {
      fileInputRef.current.value = '';
    }
  };

  const handleRemoveImage = () => {
    // Clean up blob URL if exists
    if (imageUrl && imageUrl.startsWith('blob:')) {
      URL.revokeObjectURL(imageUrl);
    }
    setImageUrl(null);
    setImageFile(null);
  };

  if (initialLoading) {
    return (
      <div className="container mx-auto p-6">
        <div className="flex h-64 items-center justify-center">
          <p className="text-gray-500">Loading...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="container mx-auto p-6">
      <Card className="mx-auto max-w-2xl">
        <CardHeader>
          <CardTitle>{isEditing ? 'Edit Mug' : 'New Mug'}</CardTitle>
          <CardDescription>{isEditing ? 'Update the mug details below' : 'Create a new mug with the form below'}</CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-6">
            {error && <div className="rounded border border-red-200 bg-red-50 px-4 py-3 text-red-700">{error}</div>}

            <div className="space-y-2">
              <Label htmlFor="name">Name</Label>
              <Input
                id="name"
                value={formData.name}
                onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                placeholder="Enter mug name"
                required
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="descriptionShort">Short Description</Label>
              <Input
                id="descriptionShort"
                value={formData.descriptionShort}
                onChange={(e) => setFormData({ ...formData, descriptionShort: e.target.value })}
                placeholder="Brief description for listings"
                required
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="descriptionLong">Long Description</Label>
              <Textarea
                id="descriptionLong"
                value={formData.descriptionLong}
                onChange={(e) => setFormData({ ...formData, descriptionLong: e.target.value })}
                placeholder="Detailed description"
                rows={4}
                required
              />
            </div>

            <div className="space-y-2">
              <Label>Image</Label>
              <div className="space-y-3">
                {imageUrl ? (
                  <div className="relative w-full max-w-md">
                    <img src={imageUrl} alt="Mug" className="w-full rounded-lg border border-gray-200 object-contain" />
                    <Button
                      type="button"
                      variant="outline"
                      size="sm"
                      className="absolute top-2 right-2 bg-white shadow-sm"
                      onClick={handleRemoveImage}
                    >
                      <X className="h-4 w-4" />
                    </Button>
                  </div>
                ) : (
                  <div className="flex items-center gap-2">
                    <Button type="button" variant="outline" onClick={() => fileInputRef.current?.click()}>
                      <Upload className="mr-2 h-4 w-4" />
                      Upload Image
                    </Button>
                    <p className="text-sm text-gray-500">PNG, JPG, GIF, or WEBP (automatically converted to WebP)</p>
                  </div>
                )}
                <input ref={fileInputRef} type="file" accept="image/*" onChange={handleImageUpload} className="hidden" />
              </div>
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="category">Category</Label>
                <Select
                  value={formData.categoryId?.toString() || ''}
                  onValueChange={(value) => setFormData({ ...formData, categoryId: value ? parseInt(value) : undefined })}
                >
                  <SelectTrigger id="category">
                    <SelectValue placeholder="Select a category" />
                  </SelectTrigger>
                  <SelectContent>
                    {categories.map((category) => (
                      <SelectItem key={category.id} value={category.id.toString()}>
                        {category.name}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>

              <div className="space-y-2">
                <Label htmlFor="subCategory">Subcategory</Label>
                <Select
                  value={formData.subCategoryId?.toString() || ''}
                  onValueChange={(value) => setFormData({ ...formData, subCategoryId: value ? parseInt(value) : undefined })}
                  disabled={!formData.categoryId}
                >
                  <SelectTrigger id="subCategory">
                    <SelectValue placeholder="Select a subcategory" />
                  </SelectTrigger>
                  <SelectContent>
                    {subCategories.map((subCategory) => (
                      <SelectItem key={subCategory.id} value={subCategory.id.toString()}>
                        {subCategory.name}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="price">Price ($)</Label>
                <Input
                  id="price"
                  type="number"
                  step="0.01"
                  value={formData.price}
                  onChange={(e) => setFormData({ ...formData, price: parseFloat(e.target.value) || 0 })}
                  placeholder="0.00"
                  required
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="fillingQuantity">Filling Quantity</Label>
                <Input
                  id="fillingQuantity"
                  value={formData.fillingQuantity}
                  onChange={(e) => setFormData({ ...formData, fillingQuantity: e.target.value })}
                  placeholder="e.g., 330ml"
                />
              </div>
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="heightMm">Height (mm)</Label>
                <Input
                  id="heightMm"
                  type="number"
                  value={formData.heightMm}
                  onChange={(e) => setFormData({ ...formData, heightMm: parseInt(e.target.value) || 0 })}
                  placeholder="0"
                  required
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="diameterMm">Diameter (mm)</Label>
                <Input
                  id="diameterMm"
                  type="number"
                  value={formData.diameterMm}
                  onChange={(e) => setFormData({ ...formData, diameterMm: parseInt(e.target.value) || 0 })}
                  placeholder="0"
                  required
                />
              </div>
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="printTemplateWidthMm">Print Template Width (mm)</Label>
                <Input
                  id="printTemplateWidthMm"
                  type="number"
                  value={formData.printTemplateWidthMm}
                  onChange={(e) => setFormData({ ...formData, printTemplateWidthMm: parseInt(e.target.value) || 0 })}
                  placeholder="0"
                  required
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="printTemplateHeightMm">Print Template Height (mm)</Label>
                <Input
                  id="printTemplateHeightMm"
                  type="number"
                  value={formData.printTemplateHeightMm}
                  onChange={(e) => setFormData({ ...formData, printTemplateHeightMm: parseInt(e.target.value) || 0 })}
                  placeholder="0"
                  required
                />
              </div>
            </div>

            <div className="space-y-4">
              <div className="flex items-center space-x-2">
                <Checkbox
                  id="dishwasherSafe"
                  checked={formData.dishwasherSafe}
                  onCheckedChange={(checked) => setFormData({ ...formData, dishwasherSafe: checked as boolean })}
                />
                <Label htmlFor="dishwasherSafe" className="text-sm font-normal">
                  Dishwasher Safe
                </Label>
              </div>

              <div className="flex items-center space-x-2">
                <Checkbox
                  id="active"
                  checked={formData.active}
                  onCheckedChange={(checked) => setFormData({ ...formData, active: checked as boolean })}
                />
                <Label htmlFor="active" className="text-sm font-normal">
                  Active
                </Label>
              </div>
            </div>

            <div className="flex gap-4">
              <Button type="submit" disabled={loading}>
                {loading ? 'Saving...' : isEditing ? 'Update Mug' : 'Create Mug'}
              </Button>
              <Button type="button" variant="outline" onClick={handleCancel}>
                Cancel
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}
