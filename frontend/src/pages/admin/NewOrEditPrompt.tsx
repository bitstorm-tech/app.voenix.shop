import { SlotTypeSelector } from '@/components/admin/prompt-slots/SlotTypeSelector';
import { Button } from '@/components/ui/Button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/Card';
import { Checkbox } from '@/components/ui/Checkbox';
import { Input } from '@/components/ui/Input';
import { InputWithCopy } from '@/components/ui/InputWithCopy';
import { Label } from '@/components/ui/Label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/Select';
import { Textarea } from '@/components/ui/Textarea';
import type { CreatePromptRequest, PromptSlotUpdate, UpdatePromptRequest } from '@/lib/api';
import { imagesApi, promptCategoriesApi, promptsApi, promptSubCategoriesApi } from '@/lib/api';
import { generatePromptNumber, getArticleNumberPlaceholder } from '@/lib/articleNumberUtils';
import type { PromptCategory, PromptSubCategory } from '@/types/prompt';
import { Upload, X } from 'lucide-react';
import { useEffect, useRef, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';

export default function NewOrEditPrompt() {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const isEditing = !!id;

  const [formData, setFormData] = useState({
    title: '',
    promptText: '',
    categoryId: 0,
    subcategoryId: 0,
    active: true,
  });
  const [promptId, setPromptId] = useState<number | null>(null);
  const [selectedSlotIds, setSelectedSlotIds] = useState<number[]>([]);
  const [categories, setCategories] = useState<PromptCategory[]>([]);
  const [subcategories, setSubcategories] = useState<PromptSubCategory[]>([]);
  const [loading, setLoading] = useState(false);
  const [initialLoading, setInitialLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [exampleImageFilename, setExampleImageFilename] = useState<string | null>(null);
  const [exampleImageUrl, setExampleImageUrl] = useState<string | null>(null);
  const [exampleImageFile, setExampleImageFile] = useState<File | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    fetchCategories();
    if (isEditing) {
      fetchPrompt();
    } else {
      setInitialLoading(false);
    }
  }, [id]);

  // Cleanup blob URLs on unmount
  useEffect(() => {
    return () => {
      if (exampleImageUrl && exampleImageUrl.startsWith('blob:')) {
        URL.revokeObjectURL(exampleImageUrl);
      }
    };
  }, [exampleImageUrl]);

  const fetchCategories = async () => {
    try {
      const data = await promptCategoriesApi.getAll();
      setCategories(data);
    } catch (error) {
      console.error('Error fetching categories:', error);
      setError('Failed to load categories');
    }
  };

  const fetchPrompt = async () => {
    if (!id) return;

    try {
      setInitialLoading(true);
      const prompt = await promptsApi.getById(parseInt(id));
      setPromptId(prompt.id);
      setFormData({
        title: prompt.title || '',
        promptText: prompt.promptText || '',
        categoryId: prompt.categoryId || 0,
        subcategoryId: prompt.subcategoryId || 0,
        active: prompt.active ?? true,
      });

      // Set selected slot IDs
      if (prompt.slots) {
        setSelectedSlotIds(prompt.slots.map((slot) => slot.id));
      }

      // Set example image if exists
      if (prompt.exampleImageUrl) {
        setExampleImageUrl(prompt.exampleImageUrl);
        // Extract filename from URL
        const filename = prompt.exampleImageUrl.split('/').pop() || null;
        setExampleImageFilename(filename);
      }

      // Fetch subcategories for the prompt's category
      if (prompt.categoryId) {
        await fetchSubcategories(prompt.categoryId);
      }
    } catch (error) {
      console.error('Error fetching prompt:', error);
      setError('Failed to load prompt');
    } finally {
      setInitialLoading(false);
    }
  };

  const fetchSubcategories = async (categoryId: number) => {
    if (!categoryId) {
      setSubcategories([]);
      return;
    }

    try {
      const data = await promptSubCategoriesApi.getByCategory(categoryId);
      setSubcategories(data);
    } catch (error) {
      console.error('Error fetching subcategories:', error);
      // Don't show error to user as subcategory is optional
      setSubcategories([]);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!formData.title.trim()) {
      setError('Title is required');
      return;
    }

    if (!formData.categoryId) {
      setError('Category is required');
      return;
    }

    try {
      setLoading(true);
      setError(null);

      let imageFilename = exampleImageFilename;

      // Upload new image if there is one
      if (exampleImageFile) {
        try {
          const uploadResult = await imagesApi.upload(exampleImageFile, 'PROMPT_EXAMPLE');
          imageFilename = uploadResult.filename;
        } catch (uploadError: any) {
          console.error('Error uploading image:', uploadError);
          setError(`Failed to upload image: ${uploadError.message || 'Please try again.'}`);
          setLoading(false);
          return;
        }
      }

      const slots: PromptSlotUpdate[] = selectedSlotIds.map((slotId) => ({
        slotId: slotId,
      }));

      if (isEditing) {
        const updateData: UpdatePromptRequest = {
          title: formData.title,
          promptText: formData.promptText || undefined,
          categoryId: formData.categoryId,
          subcategoryId: formData.subcategoryId || undefined,
          active: formData.active,
          slots,
          exampleImageFilename: imageFilename || undefined,
        };
        await promptsApi.update(parseInt(id), updateData);
      } else {
        const createData: CreatePromptRequest = {
          title: formData.title,
          promptText: formData.promptText || undefined,
          categoryId: formData.categoryId,
          subcategoryId: formData.subcategoryId || undefined,
          active: formData.active,
          slots,
          exampleImageFilename: imageFilename || undefined,
        };
        await promptsApi.create(createData);
      }

      navigate('/admin/prompts');
    } catch (error) {
      console.error('Error saving prompt:', error);
      setError('Failed to save prompt. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = () => {
    navigate('/admin/prompts');
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
    if (exampleImageUrl && exampleImageUrl.startsWith('blob:')) {
      URL.revokeObjectURL(exampleImageUrl);
    }

    // Create blob URL for preview
    const blobUrl = URL.createObjectURL(file);
    setExampleImageFile(file);
    setExampleImageUrl(blobUrl);
    setError(null);

    // Reset the input
    if (fileInputRef.current) {
      fileInputRef.current.value = '';
    }
  };

  const handleRemoveImage = () => {
    // Clean up blob URL if exists
    if (exampleImageUrl && exampleImageUrl.startsWith('blob:')) {
      URL.revokeObjectURL(exampleImageUrl);
    }
    setExampleImageFilename(null);
    setExampleImageUrl(null);
    setExampleImageFile(null);
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
          <CardTitle>{isEditing ? 'Edit Prompt' : 'New Prompt'}</CardTitle>
          <CardDescription>{isEditing ? 'Update the prompt details below' : 'Create a new prompt with the form below'}</CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-6">
            {error && <div className="rounded border border-red-200 bg-red-50 px-4 py-3 text-red-700">{error}</div>}

            <div className="space-y-2">
              <Label htmlFor="title">Title</Label>
              <Input
                id="title"
                value={formData.title}
                onChange={(e) => setFormData({ ...formData, title: e.target.value })}
                placeholder="Enter prompt title"
                required
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="promptNumber">Prompt Number</Label>
              <InputWithCopy
                id="promptNumber"
                value={generatePromptNumber(formData.categoryId || null, formData.subcategoryId || null, promptId) || getArticleNumberPlaceholder()}
                placeholder={getArticleNumberPlaceholder()}
                className="[&_input]:bg-muted"
              />
            </div>

            <div className="flex gap-8">
              <div className="space-y-2">
                <Label htmlFor="category">Category</Label>
                <Select
                  value={formData.categoryId.toString()}
                  onValueChange={(value) => {
                    const newCategoryId = parseInt(value);
                    setFormData({ ...formData, categoryId: newCategoryId, subcategoryId: 0 });
                    fetchSubcategories(newCategoryId);
                  }}
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

              {formData.categoryId > 0 && subcategories.length > 0 && (
                <div className="space-y-2">
                  <Label htmlFor="subcategory">Subcategory (optional)</Label>
                  <Select
                    value={formData.subcategoryId.toString()}
                    onValueChange={(value) => setFormData({ ...formData, subcategoryId: parseInt(value) })}
                  >
                    <SelectTrigger id="subcategory">
                      <SelectValue placeholder="Select a subcategory (optional)" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="0">No subcategory</SelectItem>
                      {subcategories.map((subcategory) => (
                        <SelectItem key={subcategory.id} value={subcategory.id.toString()}>
                          {subcategory.name}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>
              )}
            </div>

            <div className="space-y-2">
              <Label htmlFor="promptText">Prompt Style (optional)</Label>
              <Textarea
                id="promptText"
                value={formData.promptText}
                onChange={(e) => setFormData({ ...formData, promptText: e.target.value })}
                placeholder="Enter the prompt text content..."
                rows={4}
              />
            </div>

            <div className="space-y-2">
              <SlotTypeSelector selectedSlotIds={selectedSlotIds} onSelectionChange={setSelectedSlotIds} />
            </div>

            <div className="space-y-2">
              <Label>Example Image (optional)</Label>
              <div className="space-y-3">
                {exampleImageUrl ? (
                  <div className="relative w-full max-w-md">
                    <img src={exampleImageUrl} alt="Prompt example" className="w-full rounded-lg border border-gray-200 object-contain" />
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

            <div className="space-y-2">
              <Label htmlFor="active">Status</Label>
              <div className="flex items-center space-x-2">
                <Checkbox
                  id="active"
                  checked={formData.active}
                  onCheckedChange={(checked) => setFormData({ ...formData, active: checked as boolean })}
                />
                <Label htmlFor="active" className="font-normal">
                  Make this prompt available for use
                </Label>
              </div>
            </div>

            <div className="flex gap-4">
              <Button type="submit" disabled={loading}>
                {loading ? 'Saving...' : isEditing ? 'Update Prompt' : 'Create Prompt'}
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
