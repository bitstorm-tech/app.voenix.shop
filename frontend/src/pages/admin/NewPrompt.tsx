import PromptForm from '@/components/admin/PromptForm';
import { promptCategoriesApi } from '@/lib/api';
import { PromptCategory } from '@/types/prompt';
import { useEffect, useState } from 'react';

export default function NewPrompt() {
  const [categories, setCategories] = useState<PromptCategory[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchCategories = async () => {
      try {
        const data = await promptCategoriesApi.getAll();
        setCategories(data);
      } catch (err) {
        console.error('Failed to fetch categories:', err);
        setError('Failed to load categories');
      } finally {
        setLoading(false);
      }
    };

    fetchCategories();
  }, []);

  if (loading) {
    return (
      <div className="container mx-auto p-6">
        <div className="mb-6">
          <h1 className="text-2xl font-bold">New Prompt</h1>
          <p className="text-gray-600">Loading...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="container mx-auto p-6">
        <div className="mb-6">
          <h1 className="text-2xl font-bold">New Prompt</h1>
          <p className="text-red-600">{error}</p>
        </div>
      </div>
    );
  }

  return (
    <div className="container mx-auto p-6">
      <div className="mb-6">
        <h1 className="text-2xl font-bold">New Prompt</h1>
        <p className="text-gray-600">Create a new prompt for your collection.</p>
      </div>

      <div className="mx-auto max-w-4xl">
        <PromptForm categories={categories} />
      </div>
    </div>
  );
}