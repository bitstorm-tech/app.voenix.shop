import PromptForm from '@/components/admin/PromptForm';
import { promptCategoriesApi, promptsApi } from '@/lib/api';
import { Prompt, PromptCategory } from '@/types/prompt';
import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';

export default function NewOrEditPrompt() {
  const { id } = useParams<{ id: string }>();
  const isEditMode = !!id;
  const [categories, setCategories] = useState<PromptCategory[]>([]);
  const [prompt, setPrompt] = useState<Prompt | undefined>(undefined);
  const [loading, setLoading] = useState(true);
  const [loadingPrompt, setLoadingPrompt] = useState(false);
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

  useEffect(() => {
    if (isEditMode && id) {
      const fetchPrompt = async () => {
        setLoadingPrompt(true);
        try {
          const data = await promptsApi.getById(Number(id));
          setPrompt(data);
        } catch (err) {
          console.error('Failed to fetch prompt:', err);
          setError('Failed to load prompt');
        } finally {
          setLoadingPrompt(false);
        }
      };

      fetchPrompt();
    }
  }, [id, isEditMode]);

  if (loading || loadingPrompt) {
    return (
      <div className="container mx-auto p-6">
        <div className="mb-6">
          <h1 className="text-2xl font-bold">{isEditMode ? 'Edit Prompt' : 'New Prompt'}</h1>
          <p className="text-gray-600">Loading...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="container mx-auto p-6">
        <div className="mb-6">
          <h1 className="text-2xl font-bold">{isEditMode ? 'Edit Prompt' : 'New Prompt'}</h1>
          <p className="text-red-600">{error}</p>
        </div>
      </div>
    );
  }

  return (
    <div className="container mx-auto p-6">
      <div className="mb-6">
        <h1 className="text-2xl font-bold">{isEditMode ? 'Edit Prompt' : 'New Prompt'}</h1>
        <p className="text-gray-600">{isEditMode ? 'Update the prompt details.' : 'Create a new prompt for your collection.'}</p>
      </div>

      <div className="mx-auto max-w-4xl">
        <PromptForm categories={categories} prompt={prompt} />
      </div>
    </div>
  );
}
