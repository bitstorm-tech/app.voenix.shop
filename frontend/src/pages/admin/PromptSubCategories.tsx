import { Button } from '@/components/ui/Button';
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from '@/components/ui/Dialog';
import { Input } from '@/components/ui/Input';
import { Label } from '@/components/ui/Label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/Select';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/Table';
import { Textarea } from '@/components/ui/Textarea';
import { promptCategoriesApi, promptSubCategoriesApi } from '@/lib/api';
import { PromptCategory, PromptSubCategory } from '@/types/prompt';
import { Edit, Plus, Trash2 } from 'lucide-react';
import { useEffect, useState } from 'react';

export default function PromptSubCategories() {
  const [subCategories, setSubCategories] = useState<PromptSubCategory[]>([]);
  const [categories, setCategories] = useState<PromptCategory[]>([]);
  const [isSubCategoryDialogOpen, setIsSubCategoryDialogOpen] = useState(false);
  const [editingSubCategory, setEditingSubCategory] = useState<PromptSubCategory | null>(null);
  const [subCategoryName, setSubCategoryName] = useState('');
  const [subCategoryDescription, setSubCategoryDescription] = useState('');
  const [selectedCategoryId, setSelectedCategoryId] = useState<string>('');
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

  const handleOpenSubCategoryDialog = (subCategory?: PromptSubCategory) => {
    if (subCategory) {
      setEditingSubCategory(subCategory);
      setSubCategoryName(subCategory.name);
      setSubCategoryDescription(subCategory.description || '');
      setSelectedCategoryId(subCategory.promptCategoryId.toString());
    } else {
      setEditingSubCategory(null);
      setSubCategoryName('');
      setSubCategoryDescription('');
      setSelectedCategoryId('');
    }
    setIsSubCategoryDialogOpen(true);
  };

  const handleCloseSubCategoryDialog = () => {
    setIsSubCategoryDialogOpen(false);
    setEditingSubCategory(null);
    setSubCategoryName('');
    setSubCategoryDescription('');
    setSelectedCategoryId('');
  };

  const handleSubmitSubCategory = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!selectedCategoryId) {
      alert('Please select a category');
      return;
    }

    try {
      if (editingSubCategory) {
        const updated = await promptSubCategoriesApi.update(editingSubCategory.id, {
          name: subCategoryName,
          description: subCategoryDescription || undefined,
          promptCategoryId: parseInt(selectedCategoryId),
        });
        setSubCategories(subCategories.map((sc) => (sc.id === updated.id ? updated : sc)));
      } else {
        const created = await promptSubCategoriesApi.create({
          name: subCategoryName,
          description: subCategoryDescription || undefined,
          promptCategoryId: parseInt(selectedCategoryId),
        });
        setSubCategories([...subCategories, created]);
      }
      handleCloseSubCategoryDialog();
    } catch (error) {
      console.error('Error saving subcategory:', error);
      alert('Failed to save subcategory. Please try again.');
    }
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
        <Button onClick={() => handleOpenSubCategoryDialog()}>
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
                    <Button variant="ghost" size="sm" onClick={() => handleOpenSubCategoryDialog(subCategory)} className="mr-1">
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

      <Dialog open={isSubCategoryDialogOpen} onOpenChange={setIsSubCategoryDialogOpen}>
        <DialogContent>
          <form onSubmit={handleSubmitSubCategory}>
            <DialogHeader>
              <DialogTitle>{editingSubCategory ? 'Edit Subcategory' : 'New Subcategory'}</DialogTitle>
              <DialogDescription>
                {editingSubCategory ? 'Update the subcategory details below.' : 'Create a new prompt subcategory.'}
              </DialogDescription>
            </DialogHeader>
            <div className="grid gap-4 py-4">
              <div className="grid grid-cols-4 items-center gap-4">
                <Label htmlFor="category" className="text-right">
                  Category
                </Label>
                <Select value={selectedCategoryId} onValueChange={setSelectedCategoryId} required>
                  <SelectTrigger className="col-span-3">
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
              <div className="grid grid-cols-4 items-center gap-4">
                <Label htmlFor="name" className="text-right">
                  Name
                </Label>
                <Input id="name" value={subCategoryName} onChange={(e) => setSubCategoryName(e.target.value)} className="col-span-3" required />
              </div>
              <div className="grid grid-cols-4 items-start gap-4">
                <Label htmlFor="description" className="pt-2 text-right">
                  Description
                </Label>
                <Textarea
                  id="description"
                  value={subCategoryDescription}
                  onChange={(e) => setSubCategoryDescription(e.target.value)}
                  className="col-span-3"
                  rows={3}
                />
              </div>
            </div>
            <DialogFooter>
              <Button type="button" variant="outline" onClick={handleCloseSubCategoryDialog}>
                Cancel
              </Button>
              <Button type="submit">{editingSubCategory ? 'Update' : 'Create'}</Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>

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
