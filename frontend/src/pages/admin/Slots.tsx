import { Button } from '@/components/ui/Button';
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from '@/components/ui/Dialog';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/Table';
import { slotsApi } from '@/lib/api';
import type { Slot } from '@/types/slot';
import { Edit, Plus, Trash2 } from 'lucide-react';
import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';

export default function Slots() {
  const navigate = useNavigate();
  const [slots, setSlots] = useState<Slot[]>([]);
  const [loading, setLoading] = useState(true);
  const [isDeleting, setIsDeleting] = useState(false);
  const [deleteId, setDeleteId] = useState<number | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchSlots();
  }, []);

  const fetchSlots = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await slotsApi.getAll();
      setSlots(data);
    } catch (error) {
      console.error('Error fetching slots:', error);
      setError('Failed to load slots. Please try again.');
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
        await slotsApi.delete(deleteId);
        setIsDeleting(false);
        setDeleteId(null);
        fetchSlots();
      } catch (error) {
        console.error('Error deleting slot:', error);
        setIsDeleting(false);
        setDeleteId(null);
        alert('Failed to delete slot. Please try again.');
      }
    }
  };

  const cancelDelete = () => {
    setIsDeleting(false);
    setDeleteId(null);
  };

  const truncatePrompt = (prompt: string, maxLength: number = 50) => {
    if (prompt.length <= maxLength) return prompt;
    return prompt.substring(0, maxLength) + '...';
  };

  if (loading) {
    return (
      <div className="container mx-auto p-6">
        <div className="flex h-64 items-center justify-center">
          <p className="text-gray-500">Loading slots...</p>
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
            <Button onClick={fetchSlots}>Retry</Button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="container mx-auto p-6">
      <div className="mb-6 flex items-center justify-between">
        <h1 className="text-2xl font-bold">Slots</h1>
        <Button onClick={() => navigate('/admin/slots/new')}>
          <Plus className="mr-2 h-4 w-4" />
          New Slot
        </Button>
      </div>

      <div className="rounded-md border">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>ID</TableHead>
              <TableHead>Name</TableHead>
              <TableHead>Slot Type</TableHead>
              <TableHead>Prompt</TableHead>
              <TableHead>Created At</TableHead>
              <TableHead className="text-right">Actions</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {loading ? (
              <TableRow>
                <TableCell colSpan={6} className="text-center text-gray-500">
                  Loading...
                </TableCell>
              </TableRow>
            ) : slots.length === 0 ? (
              <TableRow>
                <TableCell colSpan={6} className="text-center text-gray-500">
                  No slots found
                </TableCell>
              </TableRow>
            ) : (
              slots.map((slot) => (
                <TableRow key={slot.id}>
                  <TableCell className="font-medium">{slot.id}</TableCell>
                  <TableCell>{slot.name}</TableCell>
                  <TableCell>{slot.slotType?.name || '-'}</TableCell>
                  <TableCell className="max-w-xs">
                    <span className="text-sm text-gray-600" title={slot.prompt}>
                      {truncatePrompt(slot.prompt)}
                    </span>
                  </TableCell>
                  <TableCell>{slot.createdAt ? new Date(slot.createdAt).toLocaleDateString() : '-'}</TableCell>
                  <TableCell className="text-right">
                    <Button variant="ghost" size="sm" onClick={() => navigate(`/admin/slots/${slot.id}/edit`)} className="mr-2">
                      <Edit className="h-4 w-4" />
                    </Button>
                    <Button variant="ghost" size="sm" onClick={() => handleDelete(slot.id)}>
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
            <DialogDescription>Are you sure you want to delete this slot? This action cannot be undone.</DialogDescription>
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
