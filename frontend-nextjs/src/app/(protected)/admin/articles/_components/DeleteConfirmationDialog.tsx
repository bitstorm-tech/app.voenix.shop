import {
  AlertDialog,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from '@/components/ui/AlertDialog';
import { Button } from '@/components/ui/Button';
import { updateUrlSearchParams } from '@/lib/urls';
import Link from 'next/link';
import { deleteArticleWithFormAction } from '../_actions/deleteArticleWithForm';

interface DeleteConfirmationDialogProps {
  isOpen: boolean;
  articleId: number;
  articleName: string;
  currentPath: string;
}

export function DeleteConfirmationDialog({
  isOpen,
  articleId,
  articleName,
  currentPath,
}: DeleteConfirmationDialogProps) {
  if (!isOpen) {
    return null;
  }

  // Create URL without delete confirmation parameters for cancel action
  const cancelUrl = updateUrlSearchParams(currentPath, {
    confirmDelete: null,
    deleteId: null,
  });

  return (
    <AlertDialog open={isOpen}>
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>Confirm Deletion</AlertDialogTitle>
          <AlertDialogDescription>
            Are you sure you want to delete the article &ldquo;{articleName}&rdquo;? This action cannot be undone.
          </AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter>
          <Button variant="outline" asChild>
            <Link href={cancelUrl}>
              Cancel
            </Link>
          </Button>
          <form action={deleteArticleWithFormAction}>
            <input type="hidden" name="articleId" value={articleId} />
            <input type="hidden" name="redirectPath" value={currentPath} />
            <Button type="submit" variant="destructive">
              Delete
            </Button>
          </form>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  );
}