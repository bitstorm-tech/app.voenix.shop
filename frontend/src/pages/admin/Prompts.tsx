import DeleteConfirmationDialog from '@/components/admin/DeleteConfirmationDialog';
import PromptTable from '@/components/admin/PromptTable';
import PromptTableHeader from '@/components/admin/PromptTableHeader';
import TestPromptDialog from '@/components/admin/TestPromptDialog';
import { promptCategoriesApi, promptsApi } from '@/lib/api';
import { Prompt, PromptCategory } from '@/types/prompt';
import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';

export default function Prompts() {
  const navigate = useNavigate();
  const [prompts, setPrompts] = useState<Prompt[]>([]);
  const [categories, setCategories] = useState<PromptCategory[]>([]);
  const [selectedCategory, setSelectedCategory] = useState<string>('all');
  const [isModalOpen, setIsModalOpen] = useState<boolean>(false);
  const [testingPromptId, setTestingPromptId] = useState<number | undefined>(undefined);
  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState<boolean>(false);
  const [promptToDelete, setPromptToDelete] = useState<number | undefined>(undefined);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    try {
      setIsLoading(true);
      setError(null);
      const [promptsData, categoriesData] = await Promise.all([
        promptsApi.getAll(),
        promptCategoriesApi.getAll(),
      ]);
      setPrompts(promptsData);
      setCategories(categoriesData);
    } catch (error) {
      console.error('Error fetching data:', error);
      setError('Failed to load prompts. Please try again.');
    } finally {
      setIsLoading(false);
    }
  };

  const filteredPrompts = selectedCategory === 'all' 
    ? prompts 
    : prompts.filter((prompt) => prompt.categoryId === parseInt(selectedCategory));

  const handleEdit = (prompt: Prompt) => {
    navigate(`/admin/prompts/${prompt.id}/edit`);
  };

  const handleDelete = (promptId: number) => {
    setPromptToDelete(promptId);
    setIsDeleteDialogOpen(true);
  };

  const confirmDelete = async () => {
    if (!promptToDelete) return;

    try {
      await promptsApi.delete(promptToDelete);
      setPrompts(prompts.filter(p => p.id !== promptToDelete));
      setIsDeleteDialogOpen(false);
      setPromptToDelete(undefined);
    } catch (error) {
      console.error('Error deleting prompt:', error);
      alert('Failed to delete prompt. Please try again.');
    }
  };

  const cancelDelete = () => {
    setIsDeleteDialogOpen(false);
    setPromptToDelete(undefined);
  };

  const handleTest = (promptId: number) => {
    setTestingPromptId(promptId);
    setIsModalOpen(true);
  };

  const closeModal = () => {
    setIsModalOpen(false);
    setTestingPromptId(undefined);
  };

  const handleNewPrompt = () => {
    navigate('/admin/prompts/new');
  };

  if (isLoading) {
    return (
      <div className="container mx-auto p-6">
        <div className="flex items-center justify-center h-64">
          <p className="text-gray-500">Loading prompts...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="container mx-auto p-6">
        <div className="flex items-center justify-center h-64">
          <div className="text-center">
            <p className="text-red-500 mb-4">{error}</p>
            <button 
              onClick={fetchData} 
              className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600"
            >
              Retry
            </button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <>
      <div className="container mx-auto p-6">
        <PromptTableHeader
          categories={categories}
          selectedCategory={selectedCategory}
          onCategoryChange={setSelectedCategory}
          onNewPrompt={handleNewPrompt}
        />

        <PromptTable prompts={filteredPrompts} onEdit={handleEdit} onDelete={handleDelete} onTest={handleTest} />

        <TestPromptDialog isOpen={isModalOpen} testingPromptId={testingPromptId} onClose={closeModal} />

        <DeleteConfirmationDialog isOpen={isDeleteDialogOpen} onConfirm={confirmDelete} onCancel={cancelDelete} />
      </div>
    </>
  );
}