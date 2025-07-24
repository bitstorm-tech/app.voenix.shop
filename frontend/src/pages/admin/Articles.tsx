import { Badge } from '@/components/ui/Badge';
import { Button } from '@/components/ui/Button';
import ConfirmationDialog from '@/components/ui/ConfirmationDialog';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/Select';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/Table';
import { useArticles, useDeleteArticle } from '@/hooks/queries/useArticles';
import type { ArticleType } from '@/types/article';
import { Edit, Plus, Trash2 } from 'lucide-react';
import { useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';

const articleTypeLabels: Record<ArticleType, string> = {
  MUG: 'Mug',
  SHIRT: 'T-Shirt',
  PILLOW: 'Pillow',
};

const articleTypeColors: Record<ArticleType, string> = {
  MUG: 'bg-blue-100 text-blue-800',
  SHIRT: 'bg-green-100 text-green-800',
  PILLOW: 'bg-purple-100 text-purple-800',
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
    <div className="container mx-auto p-6">
      <div className="mb-6 flex items-center justify-between">
        <h1 className="text-2xl font-bold">Articles</h1>
        <div className="flex items-center gap-4">
          <Select value={typeFilter || 'all'} onValueChange={handleTypeFilterChange}>
            <SelectTrigger className="w-40">
              <SelectValue placeholder="Filter by type" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">All Types</SelectItem>
              <SelectItem value="MUG">Mugs</SelectItem>
              <SelectItem value="SHIRT">T-Shirts</SelectItem>
              <SelectItem value="PILLOW">Pillows</SelectItem>
            </SelectContent>
          </Select>
          <Button onClick={() => navigate('/admin/articles/new')}>
            <Plus className="mr-2 h-4 w-4" />
            New Article
          </Button>
        </div>
      </div>

      <div className="rounded-md border">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Name</TableHead>
              <TableHead>Type</TableHead>
              <TableHead>Category</TableHead>
              <TableHead>Supplier</TableHead>
              <TableHead>Price</TableHead>
              <TableHead>Status</TableHead>
              <TableHead className="text-right">Actions</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {loading ? (
              <TableRow>
                <TableCell colSpan={7} className="text-center text-gray-500">
                  Loading...
                </TableCell>
              </TableRow>
            ) : articles.length === 0 ? (
              <TableRow>
                <TableCell colSpan={7} className="text-center text-gray-500">
                  No articles found
                </TableCell>
              </TableRow>
            ) : (
              articles.map((article) => (
                <TableRow key={article.id}>
                  <TableCell className="font-medium">{article.name}</TableCell>
                  <TableCell>
                    <Badge className={articleTypeColors[article.articleType]}>{articleTypeLabels[article.articleType]}</Badge>
                  </TableCell>
                  <TableCell>
                    {article.categoryName || '-'}
                    {article.subcategoryName && <span className="text-sm text-gray-500"> / {article.subcategoryName}</span>}
                  </TableCell>
                  <TableCell>{article.supplierName || '-'}</TableCell>
                  <TableCell>${(article.price / 100).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}</TableCell>
                  <TableCell>
                    <Badge variant={article.active ? 'default' : 'secondary'}>{article.active ? 'Active' : 'Inactive'}</Badge>
                  </TableCell>
                  <TableCell className="text-right">
                    <div className="flex justify-end gap-2">
                      <Button variant="outline" size="sm" onClick={() => navigate(`/admin/articles/${article.id}/edit`)}>
                        <Edit className="h-4 w-4" />
                      </Button>
                      <Button variant="outline" size="sm" onClick={() => handleDelete(article.id)}>
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

      <ConfirmationDialog
        isOpen={isDeleting}
        onConfirm={confirmDelete}
        onCancel={cancelDelete}
        description="Are you sure you want to delete this article? This action cannot be undone."
      />
    </div>
  );
}
