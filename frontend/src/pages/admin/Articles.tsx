import { Badge } from '@/components/ui/Badge';
import { Button } from '@/components/ui/Button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/Card';
import ConfirmationDialog from '@/components/ui/ConfirmationDialog';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/Select';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/Table';
import { useArticles, useDeleteArticle } from '@/hooks/queries/useArticles';
import type { ArticleType } from '@/types/article';
import { Edit, Filter, Package, Plus, Trash2 } from 'lucide-react';
import { useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';

const articleTypeLabels: Record<ArticleType, string> = {
  MUG: 'Mug',
  SHIRT: 'T-Shirt',
  PILLOW: 'Pillow',
};

const articleTypeColors: Record<ArticleType, string> = {
  MUG: 'bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200',
  SHIRT: 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200',
  PILLOW: 'bg-purple-100 text-purple-800 dark:bg-purple-900 dark:text-purple-200',
};

export default function Articles() {
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();
  const [isDeleting, setIsDeleting] = useState(false);
  const [deleteId, setDeleteId] = useState<number | null>(null);

  const typeFilter = searchParams.get('type') as ArticleType | null;
  const page = parseInt(searchParams.get('page') || '0');
  const size = parseInt(searchParams.get('size') || '50');

  const { data, isLoading: loading } = useArticles({
    page,
    size,
    type: typeFilter || undefined,
  });

  const deleteArticleMutation = useDeleteArticle();
  const articles = data?.content || [];

  const handleTypeFilterChange = (value: string) => {
    if (value === 'all') {
      searchParams.delete('type');
    } else {
      searchParams.set('type', value);
    }
    setSearchParams(searchParams);
  };

  const handleDelete = async (id: number) => {
    setDeleteId(id);
    setIsDeleting(true);
  };

  const confirmDelete = async () => {
    if (deleteId) {
      deleteArticleMutation.mutate(deleteId, {
        onSuccess: () => {
          setIsDeleting(false);
          setDeleteId(null);
        },
        onError: () => {
          setIsDeleting(false);
          setDeleteId(null);
        },
      });
    }
  };

  const cancelDelete = () => {
    setIsDeleting(false);
    setDeleteId(null);
  };

  return (
    <div className="container mx-auto max-w-7xl p-4 md:p-6">
      {/* Page Header */}
      <div className="mb-8">
        <div className="flex flex-col gap-6 md:flex-row md:items-center md:justify-between">
          <div>
            <h1 className="text-3xl font-bold tracking-tight">Articles</h1>
            <p className="text-muted-foreground mt-2">Manage your product catalog and inventory</p>
          </div>

          <div className="flex flex-col gap-3 sm:flex-row sm:items-center">
            {/* Filter */}
            <div className="flex items-center gap-2">
              <Filter className="text-muted-foreground h-4 w-4" />
              <Select value={typeFilter || 'all'} onValueChange={handleTypeFilterChange}>
                <SelectTrigger className="w-[180px]">
                  <SelectValue placeholder="Filter by type" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="all">All Types</SelectItem>
                  <SelectItem value="MUG">Mugs</SelectItem>
                  <SelectItem value="SHIRT">T-Shirts</SelectItem>
                  <SelectItem value="PILLOW">Pillows</SelectItem>
                </SelectContent>
              </Select>
            </div>

            {/* Add Button */}
            <Button onClick={() => navigate('/admin/articles/new')} className="w-full sm:w-auto">
              <Plus className="mr-2 h-4 w-4" />
              New Article
            </Button>
          </div>
        </div>
      </div>

      {/* Table Card */}
      <Card>
        <CardHeader className="px-6 py-4">
          <div className="flex items-center justify-between">
            <CardTitle className="text-base font-medium">{typeFilter ? `${articleTypeLabels[typeFilter]}s` : 'All Articles'}</CardTitle>
            <CardDescription>
              {articles.length} {articles.length === 1 ? 'item' : 'items'}
            </CardDescription>
          </div>
        </CardHeader>

        <CardContent className="p-0">
          <div className="overflow-x-auto">
            <Table>
              <TableHeader>
                <TableRow className="hover:bg-transparent">
                  <TableHead className="w-[250px]">Name</TableHead>
                  <TableHead className="w-[120px]">Type</TableHead>
                  <TableHead className="w-[200px]">Category</TableHead>
                  <TableHead className="w-[150px]">Supplier</TableHead>
                  <TableHead className="w-[100px]">Status</TableHead>
                  <TableHead className="w-[120px] text-right">Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {loading ? (
                  <TableRow>
                    <TableCell colSpan={6} className="h-32">
                      <div className="text-muted-foreground flex flex-col items-center justify-center gap-3">
                        <Package className="h-8 w-8 animate-pulse" />
                        <p>Loading articles...</p>
                      </div>
                    </TableCell>
                  </TableRow>
                ) : articles.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={6} className="h-32">
                      <div className="text-muted-foreground flex flex-col items-center justify-center gap-3">
                        <Package className="h-8 w-8" />
                        <p>No articles found</p>
                        <Button variant="outline" size="sm" onClick={() => navigate('/admin/articles/new')}>
                          <Plus className="mr-2 h-4 w-4" />
                          Add your first article
                        </Button>
                      </div>
                    </TableCell>
                  </TableRow>
                ) : (
                  articles.map((article) => (
                    <TableRow key={article.id} className="group hover:bg-muted/50 transition-colors">
                      <TableCell className="font-medium">{article.name}</TableCell>
                      <TableCell>
                        <Badge variant="secondary" className={articleTypeColors[article.articleType]}>
                          {articleTypeLabels[article.articleType]}
                        </Badge>
                      </TableCell>
                      <TableCell>
                        <div className="flex">
                          <span className="font-medium">{article.categoryName || '-'}</span>
                          {article.subcategoryName && (
                            <>
                              <span className="px-1">&gt;</span> <span className="text-muted-foreground text-sm">{article.subcategoryName}</span>
                            </>
                          )}
                        </div>
                      </TableCell>
                      <TableCell className="text-muted-foreground">{article.supplierName || '-'}</TableCell>
                      <TableCell>
                        <Badge
                          variant={article.active ? 'default' : 'secondary'}
                          className={article.active ? 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200' : ''}
                        >
                          {article.active ? 'Active' : 'Inactive'}
                        </Badge>
                      </TableCell>
                      <TableCell>
                        <div className="flex justify-end gap-1 opacity-0 transition-opacity group-hover:opacity-100">
                          <Button variant="ghost" size="sm" onClick={() => navigate(`/admin/articles/${article.id}/edit`)} title="Edit article">
                            <Edit className="h-4 w-4" />
                          </Button>
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => handleDelete(article.id)}
                            title="Delete article"
                            className="text-destructive hover:text-destructive"
                          >
                            <Trash2 className="h-4 w-4" />
                          </Button>
                        </div>
                      </TableCell>
                    </TableRow>
                  ))
                )}
              </TableBody>
            </Table>
          </div>
        </CardContent>
      </Card>

      <ConfirmationDialog
        isOpen={isDeleting}
        onConfirm={confirmDelete}
        onCancel={cancelDelete}
        description="Are you sure you want to delete this article? This action cannot be undone."
      />
    </div>
  );
}
