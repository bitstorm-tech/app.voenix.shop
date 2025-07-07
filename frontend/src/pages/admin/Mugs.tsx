import { Button } from '@/components/ui/Button';
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from '@/components/ui/Dialog';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/Table';
import { mugsApi } from '@/lib/api';
import type { Mug } from '@/types/mug';
import { Edit, Image, Plus, Trash2 } from 'lucide-react';
import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';

export default function Mugs() {
  const navigate = useNavigate();
  const [mugs, setMugs] = useState<Mug[]>([]);
  const [loading, setLoading] = useState(true);
  const [isDeleting, setIsDeleting] = useState(false);
  const [deleteId, setDeleteId] = useState<number | null>(null);

  useEffect(() => {
    fetchMugs();
  }, []);

  const fetchMugs = async () => {
    try {
      setLoading(true);
      const data = await mugsApi.getAll();
      setMugs(data);
    } catch (error) {
      console.error('Error fetching mugs:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id: number) => {
    setDeleteId(id);
    setIsDeleting(true);
  };

  const confirmDelete = async () => {
    if (deleteId) {
      try {
        await mugsApi.delete(deleteId);
        setIsDeleting(false);
        setDeleteId(null);
        // Refresh the list after deletion
        fetchMugs();
      } catch (error) {
        console.error('Error deleting mug:', error);
        setIsDeleting(false);
        setDeleteId(null);
      }
    }
  };

  const cancelDelete = () => {
    setIsDeleting(false);
    setDeleteId(null);
  };

  return (
    <div className="container mx-auto p-6">
      <div className="mb-6 flex items-center justify-between">
        <h1 className="text-2xl font-bold">Mugs</h1>
        <Button onClick={() => navigate('/admin/mugs/new')}>
          <Plus className="mr-2 h-4 w-4" />
          New Mug
        </Button>
      </div>

      <div className="rounded-md border">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead className="w-16">Image</TableHead>
              <TableHead>Name</TableHead>
              <TableHead>Category</TableHead>
              <TableHead>Dimensions</TableHead>
              <TableHead>Price</TableHead>
              <TableHead>Created At</TableHead>
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
            ) : mugs.length === 0 ? (
              <TableRow>
                <TableCell colSpan={7} className="text-center text-gray-500">
                  No mugs found
                </TableCell>
              </TableRow>
            ) : (
              mugs.map((mug) => (
                <TableRow key={mug.id}>
                  <TableCell>
                    {mug.image ? (
                      <img src={mug.image} alt={mug.name} className="h-12 w-12 rounded object-cover" />
                    ) : (
                      <div className="flex h-12 w-12 items-center justify-center rounded bg-gray-100">
                        <Image className="h-6 w-6 text-gray-400" />
                      </div>
                    )}
                  </TableCell>
                  <TableCell className="font-medium">{mug.name}</TableCell>
                  <TableCell>
                    {mug.category?.name || '-'}
                    {mug.subCategory && <span className="text-sm text-gray-500"> / {mug.subCategory.name}</span>}
                  </TableCell>
                  <TableCell>{mug.heightMm && mug.diameterMm ? `${mug.heightMm}×${mug.diameterMm}mm` : '-'}</TableCell>
                  <TableCell>${mug.price}</TableCell>
                  <TableCell>{mug.createdAt ? new Date(mug.createdAt).toLocaleDateString() : '-'}</TableCell>
                  <TableCell className="text-right">
                    <Button variant="ghost" size="sm" onClick={() => navigate(`/admin/mugs/${mug.id}/edit`)} className="mr-2">
                      <Edit className="h-4 w-4" />
                    </Button>
                    <Button variant="ghost" size="sm" onClick={() => handleDelete(mug.id)}>
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
            <DialogDescription>Are you sure you want to delete this mug? This action cannot be undone.</DialogDescription>
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
