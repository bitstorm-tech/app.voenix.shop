import { Button } from '@/components/ui/Button';
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from '@/components/ui/Dialog';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/Table';
import { promptCategoriesApi, promptSubCategoriesApi } from '@/lib/api';
import { PromptCategory, PromptSubCategory } from '@/types/prompt';
import { Edit, Plus, Trash2 } from 'lucide-react';
import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';

export default function PromptSubCategories() {
  const navigate = useNavigate();
  const [subCategories, setSubCategories] = useState<PromptSubCategory[]>([]);
  const [categories, setCategories] = useState<PromptCategory[]>([]);
  const [isDeleting, setIsDeleting] = useState(false);
  const [deleteId, setDeleteId] = useState<number | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    try {
      setIsLoading(true);
      setError(null);
      const [subCategoriesData, categoriesData] = await Promise.all([promptSubCategoriesApi.getAll(), promptCategoriesApi.getAll()]);
      setSubCategories(subCategoriesData);
      setCategories(categoriesData);
    } catch (error) {
      console.error('Error fetching data:', error);
      setError('Failed to load subcategories. Please try again.');
    } finally {
      setIsLoading(false);
    }
  };

  const getCategoryName = (categoryId: number) => {
    const category = categories.find((c) => c.id === categoryId);
    return category?.name || 'Unknown';
  };

  const handleDelete = async (id: number) => {
    setDeleteId(id);
    setIsDeleting(true);
  };

  const confirmDelete = async () => {
    if (deleteId) {
      try {
        await promptSubCategoriesApi.delete(deleteId);
        setSubCategories(subCategories.filter((sc) => sc.id !== deleteId));
        setIsDeleting(false);
        setDeleteId(null);
      } catch (error) {
        console.error('Error deleting subcategory:', error);
        alert('Failed to delete subcategory. It may have associated prompts.');
      }
    }
  };

  const cancelDelete = () => {
    setIsDeleting(false);
    setDeleteId(null);
  };

  if (isLoading) {
    return (
      <div className="container mx-auto p-6">
        <div className="flex h-64 items-center justify-center">
          <p className="text-gray-500">Loading subcategories...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="container mx-auto p-6">
        <div className="flex h-64 items-center justify-center">
          <div className="text-center">
            <p className="mb-4 text-red-500">{error}</p>
            <button onClick={fetchData} className="rounded bg-blue-500 px-4 py-2 text-white hover:bg-blue-600">
              Retry
            </button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="container mx-auto p-6">
      <div className="mb-6 flex items-center justify-between">
        <h1 className="text-2xl font-bold">Prompt Subcategories</h1>
        <Button onClick={() => navigate('/admin/prompt-subcategories/new')}>
          <Plus className="mr-2 h-4 w-4" />
          New Subcategory
        </Button>
      </div>

      <div className="rounded-md border">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Name</TableHead>
              <TableHead>Category</TableHead>
              <TableHead>Description</TableHead>
              <TableHead>Prompts Count</TableHead>
              <TableHead>Created At</TableHead>
              <TableHead className="text-right">Actions</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {subCategories.length === 0 ? (
              <TableRow>
                <TableCell colSpan={6} className="text-center text-gray-500">
                  No subcategories found
                </TableCell>
              </TableRow>
            ) : (
              subCategories.map((subCategory) => (
                <TableRow key={subCategory.id}>
                  <TableCell className="font-medium">{subCategory.name}</TableCell>
                  <TableCell>{getCategoryName(subCategory.promptCategoryId)}</TableCell>
                  <TableCell className="max-w-xs truncate">{subCategory.description || '-'}</TableCell>
                  <TableCell>{subCategory.promptsCount || 0}</TableCell>
                  <TableCell>{subCategory.createdAt ? new Date(subCategory.createdAt).toLocaleDateString() : '-'}</TableCell>
                  <TableCell className="text-right">
                    <Button variant="ghost" size="sm" onClick={() => navigate(`/admin/prompt-subcategories/${subCategory.id}/edit`)} className="mr-1">
                      <Edit className="h-4 w-4" />
                    </Button>
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => handleDelete(subCategory.id)}
                      disabled={!!subCategory.promptsCount && subCategory.promptsCount > 0}
                    >
                      <Trash2 className="h-4 w-4" />
                    </Button>
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </div>

      <Dialog open={isDeleting} onOpenChange={setIsDeleting}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Confirm Deletion</DialogTitle>
            <DialogDescription>Are you sure you want to delete this subcategory? This action cannot be undone.</DialogDescription>
          </DialogHeader>
          <DialogFooter>
            <Button variant="outline" onClick={cancelDelete}>
              Cancel
            </Button>
            <Button variant="destructive" onClick={confirmDelete}>
              Delete
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
