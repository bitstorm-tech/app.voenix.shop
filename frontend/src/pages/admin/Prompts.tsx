import PromptTable from '@/components/admin/PromptTable';
import PromptTableHeader from '@/components/admin/PromptTableHeader';
import TestPromptDialog from '@/components/admin/TestPromptDialog';
import ConfirmationDialog from '@/components/ui/ConfirmationDialog';
import { usePromptCategories } from '@/hooks/queries/useCategories';
import { useDeletePrompt, usePrompts } from '@/hooks/queries/usePrompts';
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';

export default function Prompts() {
  const navigate = useNavigate();
  const { data: prompts = [], isLoading: isLoadingPrompts, error: promptsError } = usePrompts();
  const { data: categories = [], isLoading: isLoadingCategories } = usePromptCategories();
  const deletePromptMutation = useDeletePrompt();

  const [selectedCategory, setSelectedCategory] = useState<string>('all');
  const [isModalOpen, setIsModalOpen] = useState<boolean>(false);
  const [testingPromptId, setTestingPromptId] = useState<number | undefined>(undefined);
  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState<boolean>(false);
  const [promptToDelete, setPromptToDelete] = useState<number | undefined>(undefined);

  const isLoading = isLoadingPrompts || isLoadingCategories;
  const error = promptsError;

  const filteredPrompts = selectedCategory === 'all' ? prompts : prompts.filter((prompt) => prompt.categoryId === parseInt(selectedCategory));

  const handleEdit = (prompt: any) => {
    navigate(`/admin/prompts/${prompt.id}/edit`);
  };

  const handleDelete = (promptId: number) => {
    setPromptToDelete(promptId);
    setIsDeleteDialogOpen(true);
  };

  const confirmDelete = async () => {
    if (!promptToDelete) return;

    deletePromptMutation.mutate(promptToDelete, {
      onSuccess: () => {
        setIsDeleteDialogOpen(false);
        setPromptToDelete(undefined);
      },
    });
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
        <div className="flex h-64 items-center justify-center">
          <p className="text-gray-500">Loading prompts...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="container mx-auto p-6">
        <div className="flex h-64 items-center justify-center">
          <div className="text-center">
            <p className="mb-4 text-red-500">Failed to load prompts. Please try again.</p>
            <button onClick={() => window.location.reload()} className="rounded bg-blue-500 px-4 py-2 text-white hover:bg-blue-600">
              Retry
            </button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="container mx-auto p-6">
      <PromptTableHeader
        categories={categories}
        selectedCategory={selectedCategory}
        onCategoryChange={setSelectedCategory}
        onNewPrompt={handleNewPrompt}
      />

      <PromptTable prompts={filteredPrompts} onEdit={handleEdit} onDelete={handleDelete} onTest={handleTest} />

      <TestPromptDialog isOpen={isModalOpen} testingPromptId={testingPromptId} onClose={closeModal} />

      <ConfirmationDialog
        isOpen={isDeleteDialogOpen}
        onConfirm={confirmDelete}
        onCancel={cancelDelete}
        description="This will permanently delete the prompt. This action cannot be undone."
      />
    </div>
  );
}
