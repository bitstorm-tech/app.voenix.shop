import ArticleCategoryFormDialog from '@/components/admin/categories/ArticleCategoryFormDialog';
import ArticleSubCategoryFormDialog from '@/components/admin/categories/ArticleSubCategoryFormDialog';
import { Accordion, AccordionContent, AccordionItem, AccordionTrigger } from '@/components/ui/Accordion';
import { Badge } from '@/components/ui/Badge';
import { Button } from '@/components/ui/Button';
import ConfirmationDialog from '@/components/ui/ConfirmationDialog';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/Table';
import { articleCategoriesApi, articleSubCategoriesApi } from '@/lib/api';
import { ArticleCategory, ArticleSubCategory } from '@/types/mug';
import { Edit, Plus, Trash2 } from 'lucide-react';
import { useEffect, useState } from 'react';

interface CategoryWithSubcategories extends ArticleCategory {
  subcategories: ArticleSubCategory[];
}

export default function ArticleCategories() {
  const [categories, setCategories] = useState<ArticleCategory[]>([]);
  const [subcategories, setSubcategories] = useState<ArticleSubCategory[]>([]);
  const [categoriesWithSubs, setCategoriesWithSubs] = useState<CategoryWithSubcategories[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [isCategoryDialogOpen, setIsCategoryDialogOpen] = useState(false);
  const [isSubcategoryDialogOpen, setIsSubcategoryDialogOpen] = useState(false);
  const [editingCategory, setEditingCategory] = useState<ArticleCategory | null>(null);
  const [editingSubcategory, setEditingSubcategory] = useState<ArticleSubCategory | null>(null);
  const [selectedCategoryId, setSelectedCategoryId] = useState<number | null>(null);

  const [deleteDialog, setDeleteDialog] = useState<{
    isOpen: boolean;
    type: 'category' | 'subcategory';
    id: number | null;
    name: string;
  }>({ isOpen: false, type: 'category', id: null, name: '' });

  useEffect(() => {
    fetchData();
  }, []);

  useEffect(() => {
    // Combine categories and subcategories
    const combined = categories.map((category) => ({
      ...category,
      subcategories: subcategories.filter((sub) => sub.articleCategoryId === category.id),
    }));
    setCategoriesWithSubs(combined);
  }, [categories, subcategories]);

  const fetchData = async () => {
    try {
      setIsLoading(true);
      setError(null);
      const [categoriesData, subcategoriesData] = await Promise.all([articleCategoriesApi.getAll(), articleSubCategoriesApi.getAll()]);
      setCategories(categoriesData);
      setSubcategories(subcategoriesData);
    } catch (error) {
      console.error('Error fetching data:', error);
      setError('Failed to load categories. Please try again.');
    } finally {
      setIsLoading(false);
    }
  };

  const handleAddCategory = () => {
    setEditingCategory(null);
    setIsCategoryDialogOpen(true);
  };

  const handleEditCategory = (category: ArticleCategory) => {
    setEditingCategory(category);
    setIsCategoryDialogOpen(true);
  };

  const handleAddSubcategory = (categoryId: number) => {
    setEditingSubcategory(null);
    setSelectedCategoryId(categoryId);
    setIsSubcategoryDialogOpen(true);
  };

  const handleEditSubcategory = (subcategory: ArticleSubCategory) => {
    setEditingSubcategory(subcategory);
    setSelectedCategoryId(subcategory.articleCategoryId);
    setIsSubcategoryDialogOpen(true);
  };

  const handleDeleteCategory = (category: ArticleCategory) => {
    const hasSubcategories = categoriesWithSubs.find((c) => c.id === category.id)?.subcategories.length || 0;
    if (hasSubcategories > 0) {
      alert('Cannot delete category with subcategories. Please delete all subcategories first.');
      return;
    }
    setDeleteDialog({
      isOpen: true,
      type: 'category',
      id: category.id,
      name: category.name,
    });
  };

  const handleDeleteSubcategory = (subcategory: ArticleSubCategory) => {
    if (subcategory.articlesCount && subcategory.articlesCount > 0) {
      alert('Cannot delete subcategory with associated articles.');
      return;
    }
    setDeleteDialog({
      isOpen: true,
      type: 'subcategory',
      id: subcategory.id,
      name: subcategory.name,
    });
  };

  const confirmDelete = async () => {
    if (!deleteDialog.id) return;

    try {
      if (deleteDialog.type === 'category') {
        await articleCategoriesApi.delete(deleteDialog.id);
        setCategories(categories.filter((c) => c.id !== deleteDialog.id));
      } else {
        await articleSubCategoriesApi.delete(deleteDialog.id);
        setSubcategories(subcategories.filter((s) => s.id !== deleteDialog.id));
      }
      setDeleteDialog({ isOpen: false, type: 'category', id: null, name: '' });
    } catch (error) {
      console.error('Error deleting:', error);
      alert(`Failed to delete ${deleteDialog.type}. Please try again.`);
    }
  };

  const handleCategorySaved = async () => {
    await fetchData();
    setIsCategoryDialogOpen(false);
  };

  const handleSubcategorySaved = async () => {
    await fetchData();
    setIsSubcategoryDialogOpen(false);
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
        <h1 className="text-2xl font-bold">Article Categories</h1>
        <Button onClick={handleAddCategory}>
          <Plus className="mr-2 h-4 w-4" />
          New Category
        </Button>
      </div>

      {categoriesWithSubs.length === 0 ? (
        <div className="rounded-md border p-8 text-center">
          <p className="text-gray-500">No categories found. Create your first category to get started.</p>
        </div>
      ) : (
        <Accordion type="multiple" className="w-full space-y-2">
          {categoriesWithSubs.map((category) => (
            <AccordionItem key={category.id} value={`category-${category.id}`} className="rounded-lg border !border-b">
              <div className="flex items-center justify-between px-4">
                <AccordionTrigger className="flex-1 py-4 hover:no-underline">
                  <div className="flex items-center gap-3">
                    <span className="font-medium">{category.name}</span>
                    <Badge variant="secondary">{category.subcategories.length} subcategories</Badge>
                    {category.articles_count && category.articles_count > 0 && <Badge variant="outline">{category.articles_count} articles</Badge>}
                  </div>
                </AccordionTrigger>
                <div className="flex items-center gap-2 py-4">
                  <Button variant="ghost" size="sm" onClick={() => handleEditCategory(category)}>
                    <Edit className="h-4 w-4" />
                  </Button>
                  <Button variant="ghost" size="sm" onClick={() => handleDeleteCategory(category)} disabled={category.subcategories.length > 0}>
                    <Trash2 className="h-4 w-4" />
                  </Button>
                </div>
              </div>
              <AccordionContent className="px-4 pb-4">
                {category.subcategories.length === 0 ? (
                  <div className="mb-4 rounded-md bg-gray-50 p-4 text-center">
                    <p className="mb-2 text-sm text-gray-500">No subcategories yet</p>
                    <Button size="sm" variant="outline" onClick={() => handleAddSubcategory(category.id)}>
                      <Plus className="mr-2 h-3 w-3" />
                      Add Subcategory
                    </Button>
                  </div>
                ) : (
                  <>
                    <Table>
                      <TableHeader>
                        <TableRow>
                          <TableHead>Name</TableHead>
                          <TableHead>Description</TableHead>
                          <TableHead>Articles</TableHead>
                          <TableHead>Created</TableHead>
                          <TableHead className="text-right">Actions</TableHead>
                        </TableRow>
                      </TableHeader>
                      <TableBody>
                        {category.subcategories.map((subcategory) => (
                          <TableRow key={subcategory.id}>
                            <TableCell className="font-medium">{subcategory.name}</TableCell>
                            <TableCell className="max-w-xs truncate">{subcategory.description || '-'}</TableCell>
                            <TableCell>{subcategory.articlesCount || 0}</TableCell>
                            <TableCell>{subcategory.createdAt ? new Date(subcategory.createdAt).toLocaleDateString() : '-'}</TableCell>
                            <TableCell className="text-right">
                              <Button variant="ghost" size="sm" onClick={() => handleEditSubcategory(subcategory)} className="mr-1">
                                <Edit className="h-4 w-4" />
                              </Button>
                              <Button
                                variant="ghost"
                                size="sm"
                                onClick={() => handleDeleteSubcategory(subcategory)}
                                disabled={!!subcategory.articlesCount && subcategory.articlesCount > 0}
                              >
                                <Trash2 className="h-4 w-4" />
                              </Button>
                            </TableCell>
                          </TableRow>
                        ))}
                      </TableBody>
                    </Table>
                    <div className="mt-4 text-center">
                      <Button size="sm" variant="outline" onClick={() => handleAddSubcategory(category.id)}>
                        <Plus className="mr-2 h-3 w-3" />
                        Add Subcategory
                      </Button>
                    </div>
                  </>
                )}
              </AccordionContent>
            </AccordionItem>
          ))}
        </Accordion>
      )}

      <ArticleCategoryFormDialog
        open={isCategoryDialogOpen}
        onOpenChange={setIsCategoryDialogOpen}
        category={editingCategory}
        onSaved={handleCategorySaved}
      />

      <ArticleSubCategoryFormDialog
        open={isSubcategoryDialogOpen}
        onOpenChange={setIsSubcategoryDialogOpen}
        subcategory={editingSubcategory}
        categoryId={selectedCategoryId}
        categories={categories}
        onSaved={handleSubcategorySaved}
      />

      <ConfirmationDialog
        isOpen={deleteDialog.isOpen}
        onConfirm={confirmDelete}
        onCancel={() => setDeleteDialog({ ...deleteDialog, isOpen: false })}
        description={`Are you sure you want to delete ${deleteDialog.type === 'category' ? 'the category' : 'the subcategory'} "${deleteDialog.name}"? This action cannot be undone.`}
      />
    </div>
  );
}
