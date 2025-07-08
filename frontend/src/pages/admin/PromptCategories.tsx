import { Button } from '@/components/ui/Button';
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from '@/components/ui/Dialog';
import { Input } from '@/components/ui/Input';
import { Label } from '@/components/ui/Label';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/Table';
import { promptCategoriesApi } from '@/lib/api';
import { PromptCategory } from '@/types/prompt';
import { Edit, Plus, Trash2 } from 'lucide-react';
import { useEffect, useState } from 'react';

export default function PromptCategories() {
  const [categories, setCategories] = useState<PromptCategory[]>([]);
  const [isCategoryDialogOpen, setIsCategoryDialogOpen] = useState(false);
  const [editingCategory, setEditingCategory] = useState<PromptCategory | null>(null);
  const [categoryName, setCategoryName] = useState('');
  const [isDeleting, setIsDeleting] = useState(false);
  const [deleteId, setDeleteId] = useState<number | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchCategories();
  }, []);

  const fetchCategories = async () => {
    try {
      setIsLoading(true);
      setError(null);
      const data = await promptCategoriesApi.getAll();
      setCategories(data);
    } catch (error) {
      console.error('Error fetching categories:', error);
      setError('Failed to load categories. Please try again.');
    } finally {
      setIsLoading(false);
    }
  };

  const handleOpenCategoryDialog = (category?: PromptCategory) => {
    if (category) {
      setEditingCategory(category);
      setCategoryName(category.name);
    } else {
      setEditingCategory(null);
      setCategoryName('');
    }
    setIsCategoryDialogOpen(true);
  };

  const handleCloseCategoryDialog = () => {
    setIsCategoryDialogOpen(false);
    setEditingCategory(null);
    setCategoryName('');
  };

  const handleSubmitCategory = async (e: React.FormEvent) => {
    e.preventDefault();

    try {
      if (editingCategory) {
        const updated = await promptCategoriesApi.update(editingCategory.id, { name: categoryName });
        setCategories(categories.map((c) => (c.id === updated.id ? updated : c)));
      } else {
        const created = await promptCategoriesApi.create({ name: categoryName });
        setCategories([...categories, created]);
      }
      handleCloseCategoryDialog();
    } catch (error) {
      console.error('Error saving category:', error);
      alert('Failed to save category. Please try again.');
    }
  };

  const handleDelete = async (id: number) => {
    setDeleteId(id);
    setIsDeleting(true);
  };

  const confirmDelete = async () => {
    if (deleteId) {
      try {
        await promptCategoriesApi.delete(deleteId);
        setCategories(categories.filter((c) => c.id !== deleteId));
        setIsDeleting(false);
        setDeleteId(null);
      } catch (error) {
        console.error('Error deleting category:', error);
        alert('Failed to delete category. It may have associated prompts.');
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
          <p className="text-gray-500">Loading categories...</p>
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
            <button onClick={fetchCategories} className="rounded bg-blue-500 px-4 py-2 text-white hover:bg-blue-600">
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
        <h1 className="text-2xl font-bold">Prompt Categories</h1>
        <Button onClick={() => handleOpenCategoryDialog()}>
          <Plus className="mr-2 h-4 w-4" />
          New Category
        </Button>
      </div>

      <div className="rounded-md border">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Name</TableHead>
              <TableHead>Prompts Count</TableHead>
              <TableHead>Created At</TableHead>
              <TableHead className="text-right">Actions</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {categories.length === 0 ? (
              <TableRow>
                <TableCell colSpan={4} className="text-center text-gray-500">
                  No categories found
                </TableCell>
              </TableRow>
            ) : (
              categories.map((category) => (
                <TableRow key={category.id}>
                  <TableCell className="font-medium">{category.name}</TableCell>
                  <TableCell>{category.prompts_count || 0}</TableCell>
                  <TableCell>{category.createdAt ? new Date(category.createdAt).toLocaleDateString() : '-'}</TableCell>
                  <TableCell className="text-right">
                    <Button variant="ghost" size="sm" onClick={() => handleOpenCategoryDialog(category)} className="mr-1">
                      <Edit className="h-4 w-4" />
                    </Button>
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => handleDelete(category.id)}
                      disabled={!!category.prompts_count && category.prompts_count > 0}
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

      <Dialog open={isCategoryDialogOpen} onOpenChange={setIsCategoryDialogOpen}>
        <DialogContent>
          <form onSubmit={handleSubmitCategory}>
            <DialogHeader>
              <DialogTitle>{editingCategory ? 'Edit Category' : 'New Category'}</DialogTitle>
              <DialogDescription>{editingCategory ? 'Update the category details below.' : 'Create a new prompt category.'}</DialogDescription>
            </DialogHeader>
            <div className="grid gap-4 py-4">
              <div className="grid grid-cols-4 items-center gap-4">
                <Label htmlFor="name" className="text-right">
                  Name
                </Label>
                <Input id="name" value={categoryName} onChange={(e) => setCategoryName(e.target.value)} className="col-span-3" required />
              </div>
            </div>
            <DialogFooter>
              <Button type="button" variant="outline" onClick={handleCloseCategoryDialog}>
                Cancel
              </Button>
              <Button type="submit">{editingCategory ? 'Update' : 'Create'}</Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>

      <Dialog open={isDeleting} onOpenChange={setIsDeleting}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Confirm Deletion</DialogTitle>
            <DialogDescription>Are you sure you want to delete this category? This action cannot be undone.</DialogDescription>
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
