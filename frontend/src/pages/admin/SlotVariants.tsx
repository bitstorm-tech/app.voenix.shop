import { Button } from '@/components/ui/Button';
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from '@/components/ui/Dialog';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/Table';
import { promptSlotVariantsApi } from '@/lib/api';
import type { PromptSlotVariant } from '@/types/promptSlotVariant';
import { Edit, Image, Plus, Trash2 } from 'lucide-react';
import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';

export default function SlotVariants() {
  const navigate = useNavigate();
  const [slotVariants, setSlotVariants] = useState<PromptSlotVariant[]>([]);
  const [loading, setLoading] = useState(true);
  const [isDeleting, setIsDeleting] = useState(false);
  const [deleteId, setDeleteId] = useState<number | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchSlotVariants();
  }, []);

  const fetchSlotVariants = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await promptSlotVariantsApi.getAll();
      setSlotVariants(data);
    } catch (error) {
      console.error('Error fetching slot variants:', error);
      setError('Failed to load slot variants. Please try again.');
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
        await promptSlotVariantsApi.delete(deleteId);
        setIsDeleting(false);
        setDeleteId(null);
        fetchSlotVariants();
      } catch (error) {
        console.error('Error deleting slot variant:', error);
        setIsDeleting(false);
        setDeleteId(null);
        alert('Failed to delete slot variant. Please try again.');
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
          <p className="text-gray-500">Loading slot variants...</p>
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
            <Button onClick={fetchSlotVariants}>Retry</Button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="container mx-auto p-6">
      <div className="mb-6 flex items-center justify-between">
        <h1 className="text-2xl font-bold">Slot Variants</h1>
        <Button onClick={() => navigate('/admin/slot-variants/new')}>
          <Plus className="mr-2 h-4 w-4" />
          New Slot Variant
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
              <TableHead>Description</TableHead>
              <TableHead>Example</TableHead>
              <TableHead>Created At</TableHead>
              <TableHead className="text-right">Actions</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {loading ? (
              <TableRow>
                <TableCell colSpan={8} className="text-center text-gray-500">
                  Loading...
                </TableCell>
              </TableRow>
            ) : slotVariants.length === 0 ? (
              <TableRow>
                <TableCell colSpan={8} className="text-center text-gray-500">
                  No slot variants found
                </TableCell>
              </TableRow>
            ) : (
              slotVariants.map((slot) => (
                <TableRow key={slot.id}>
                  <TableCell className="font-medium">{slot.id}</TableCell>
                  <TableCell>{slot.name}</TableCell>
                  <TableCell>{slot.promptSlotType?.name || '-'}</TableCell>
                  <TableCell className="max-w-xs">
                    <span className="text-sm text-gray-600" title={slot.prompt}>
                      {truncatePrompt(slot.prompt)}
                    </span>
                  </TableCell>
                  <TableCell className="max-w-xs">
                    <span className="text-sm text-gray-600" title={slot.description || ''}>
                      {slot.description ? truncatePrompt(slot.description, 30) : '-'}
                    </span>
                  </TableCell>
                  <TableCell>
                    {slot.exampleImageUrl ? (
                      <div className="flex items-center justify-center">
                        <Image className="h-4 w-4 text-green-600" />
                      </div>
                    ) : (
                      <div className="text-center text-gray-400">-</div>
                    )}
                  </TableCell>
                  <TableCell>{slot.createdAt ? new Date(slot.createdAt).toLocaleDateString() : '-'}</TableCell>
                  <TableCell className="text-right">
                    <Button variant="ghost" size="sm" onClick={() => navigate(`/admin/slot-variants/${slot.id}/edit`)} className="mr-2">
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
            <DialogDescription>Are you sure you want to delete this slot variant? This action cannot be undone.</DialogDescription>
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
