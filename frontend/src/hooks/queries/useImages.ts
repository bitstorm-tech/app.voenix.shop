import { imagesApi } from '@/lib/api';
import { useMutation } from '@tanstack/react-query';
import { toast } from 'sonner';

// Upload image mutation
export function useUploadImage() {
  return useMutation({
    mutationFn: ({
      file,
      imageType,
      cropArea,
    }: {
      file: File;
      imageType: 'PUBLIC' | 'PRIVATE' | 'PROMPT_EXAMPLE' | 'PROMPT_SLOT_VARIANT_EXAMPLE';
      cropArea?: { x: number; y: number; width: number; height: number };
    }) => imagesApi.upload(file, imageType, cropArea),
    onSuccess: () => {
      toast.success('Image uploaded successfully');
    },
    onError: (error: any) => {
      toast.error(error?.message || 'Failed to upload image');
    },
  });
}

// Delete image mutation
export function useDeleteImage() {
  return useMutation({
    mutationFn: (filename: string) => imagesApi.delete(filename),
    onSuccess: () => {
      toast.success('Image deleted successfully');
    },
    onError: (error: any) => {
      toast.error(error?.message || 'Failed to delete image');
    },
  });
}
