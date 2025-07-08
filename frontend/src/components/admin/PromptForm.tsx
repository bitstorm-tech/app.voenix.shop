import { Button } from '@/components/ui/Button';
import { Checkbox } from '@/components/ui/Checkbox';
import { Input } from '@/components/ui/Input';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/Select';
import { Textarea } from '@/components/ui/Textarea';
import { promptsApi, type CreatePromptRequest, type UpdatePromptRequest } from '@/lib/api';
import { Prompt, PromptCategory } from '@/types/prompt';
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';

interface PromptFormProps {
  prompt?: Prompt | null;
  categories: PromptCategory[];
  onSuccess?: () => void;
  onCancel?: () => void;
}

export default function PromptForm({ prompt, categories, onSuccess, onCancel }: PromptFormProps) {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    title: prompt?.title || '',
    categoryId: prompt?.categoryId?.toString() || '',
    content: prompt?.content || '',
    active: prompt?.active ?? true,
  });
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrors({});

    if (!formData.title || !formData.categoryId || !formData.content) {
      return;
    }

    setIsSubmitting(true);

    try {
      const data: CreatePromptRequest | UpdatePromptRequest = {
        title: formData.title,
        categoryId: parseInt(formData.categoryId),
        content: formData.content,
        active: formData.active,
      };

      if (prompt) {
        await promptsApi.update(prompt.id, data);
      } else {
        await promptsApi.create(data);
      }

      if (onSuccess) {
        onSuccess();
      } else {
        navigate('/admin/prompts');
      }
    } catch (error: any) {
      console.error('Form submission error:', error);
      if (error.status === 400 && error.message) {
        setErrors({ general: error.message });
      } else {
        setErrors({ general: 'An error occurred while saving the prompt' });
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleCancel = () => {
    if (onCancel) {
      onCancel();
    } else {
      navigate('/admin/prompts');
    }
  };

  const isFormValid = formData.title && formData.categoryId && formData.content;

  return (
    <form onSubmit={handleSubmit} className="space-y-6">
      {errors.general && <div className="rounded border border-red-200 bg-red-50 px-4 py-2 text-red-600">{errors.general}</div>}

      <div className="grid gap-4">
        <div className="grid grid-cols-1 items-start gap-2 sm:grid-cols-4 sm:items-center sm:gap-4">
          <label htmlFor="title" className="text-sm font-medium sm:text-right">
            Title
          </label>
          <div className="sm:col-span-3">
            <Input
              id="title"
              type="text"
              value={formData.title}
              onChange={(e) => setFormData({ ...formData, title: e.target.value })}
              placeholder="Enter prompt title"
              required
            />
            {errors.title && <p className="mt-1 text-sm text-red-500">{errors.title}</p>}
          </div>
        </div>

        <div className="grid grid-cols-1 items-start gap-2 sm:grid-cols-4 sm:items-center sm:gap-4">
          <label htmlFor="category" className="text-sm font-medium sm:text-right">
            Category
          </label>
          <div className="sm:col-span-3">
            <Select value={formData.categoryId} onValueChange={(value) => setFormData({ ...formData, categoryId: value })}>
              <SelectTrigger>
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
            {errors.categoryId && <p className="mt-1 text-sm text-red-500">{errors.categoryId}</p>}
          </div>
        </div>

        <div className="grid grid-cols-1 items-start gap-2 sm:grid-cols-4 sm:gap-4">
          <label htmlFor="content" className="text-sm font-medium sm:mt-2 sm:text-right">
            Content
          </label>
          <div className="sm:col-span-3">
            <Textarea
              id="content"
              rows={4}
              value={formData.content}
              onChange={(e) => setFormData({ ...formData, content: e.target.value })}
              placeholder="Enter the prompt content"
              required
            />
            {errors.content && <p className="mt-1 text-sm text-red-500">{errors.content}</p>}
          </div>
        </div>

        <div className="grid grid-cols-1 items-start gap-2 sm:grid-cols-4 sm:items-center sm:gap-4">
          <label htmlFor="active" className="text-sm font-medium sm:text-right">
            Active
          </label>
          <div className="flex items-center">
            <Checkbox id="active" checked={formData.active} onCheckedChange={(checked) => setFormData({ ...formData, active: checked as boolean })} />
            <label htmlFor="active" className="ml-2 text-sm">
              Make this prompt available for use
            </label>
          </div>
        </div>
      </div>

      <div className="flex justify-end gap-2">
        <Button type="button" variant="outline" onClick={handleCancel}>
          Cancel
        </Button>
        <Button type="submit" disabled={!isFormValid || isSubmitting}>
          {isSubmitting ? 'Saving...' : prompt ? 'Update' : 'Create'}
        </Button>
      </div>
    </form>
  );
}
