import { vatApi, type CreateValueAddedTaxRequest, type UpdateValueAddedTaxRequest } from '@/lib/api';
import type { ValueAddedTax } from '@/types/vat';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';

// Type for VAT list filters
interface VatListFilters {
  [key: string]: unknown;
}

// Query keys
export const vatKeys = {
  all: ['vat'] as const,
  lists: () => [...vatKeys.all, 'list'] as const,
  list: (filters?: VatListFilters) => [...vatKeys.lists(), filters] as const,
  details: () => [...vatKeys.all, 'detail'] as const,
  detail: (id: number) => [...vatKeys.details(), id] as const,
};

// Get all VATs
export function useVats() {
  return useQuery({
    queryKey: vatKeys.lists(),
    queryFn: vatApi.getAll,
  });
}

// Get VAT by ID
export function useVat(id: number | undefined) {
  return useQuery({
    queryKey: vatKeys.detail(id!),
    queryFn: () => vatApi.getById(id!),
    enabled: !!id,
  });
}

// Create VAT mutation
export function useCreateVat() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (data: CreateValueAddedTaxRequest) => vatApi.create(data),
    onSuccess: (newVat) => {
      // Invalidate and refetch VAT list
      queryClient.invalidateQueries({ queryKey: vatKeys.lists() });

      // Optionally add the new VAT to cache immediately
      queryClient.setQueryData<ValueAddedTax[]>(vatKeys.lists(), (old) => {
        return old ? [...old, newVat] : [newVat];
      });

      toast.success('VAT created successfully');
    },
  });
}

// Update VAT mutation
export function useUpdateVat() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: UpdateValueAddedTaxRequest }) => vatApi.update(id, data),
    onSuccess: (updatedVat, { id }) => {
      // Update the specific VAT in cache
      queryClient.setQueryData(vatKeys.detail(id), updatedVat);

      // Invalidate list to ensure consistency
      queryClient.invalidateQueries({ queryKey: vatKeys.lists() });

      toast.success('VAT updated successfully');
    },
  });
}

// Delete VAT mutation
export function useDeleteVat() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (id: number) => vatApi.delete(id),
    onSuccess: (_, id) => {
      // Remove from cache
      queryClient.invalidateQueries({ queryKey: vatKeys.lists() });
      queryClient.removeQueries({ queryKey: vatKeys.detail(id) });

      toast.success('VAT deleted successfully');
    },
  });
}
