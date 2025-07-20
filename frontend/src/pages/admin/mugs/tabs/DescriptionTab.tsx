import { Checkbox } from '@/components/ui/Checkbox';
import { Input } from '@/components/ui/Input';
import { Label } from '@/components/ui/Label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/Select';
import { Textarea } from '@/components/ui/Textarea';
import type { CreateMugRequest } from '@/lib/api';
import type { ArticleCategory, ArticleSubCategory } from '@/types/mug';

interface DescriptionTabProps {
  formData: CreateMugRequest;
  setFormData: (data: CreateMugRequest) => void;
  categories: ArticleCategory[];
  subCategories: ArticleSubCategory[];
}

export default function DescriptionTab({ formData, setFormData, categories, subCategories }: DescriptionTabProps) {
  return (
    <div className="space-y-6">
      <div className="flex items-center space-x-2">
        <Checkbox id="active" checked={formData.active} onCheckedChange={(checked) => setFormData({ ...formData, active: checked as boolean })} />
        <Label htmlFor="active" className="text-sm font-normal">
          Active
        </Label>
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
    </div>
  );
}
