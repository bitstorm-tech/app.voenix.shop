import { supplierApi } from '@/lib/api';
import type { CreateSupplierRequest, UpdateSupplierRequest } from '@/types/supplier';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';

const QUERY_KEYS = {
  all: ['suppliers'] as const,
  detail: (id: number) => ['suppliers', id] as const,
};

export function useSuppliers() {
  return useQuery({
    queryKey: QUERY_KEYS.all,
    queryFn: supplierApi.getAll,
  });
}

export function useSupplier(id: number | undefined) {
  return useQuery({
    queryKey: QUERY_KEYS.detail(id!),
    queryFn: () => supplierApi.getById(id!),
    enabled: !!id,
  });
}

export function useCreateSupplier() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (data: CreateSupplierRequest) => supplierApi.create(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: QUERY_KEYS.all });
      toast.success('Supplier created successfully');
    },
    onError: (error: any) => {
      toast.error(error.message || 'Failed to create supplier');
    },
  });
}

export function useUpdateSupplier() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: UpdateSupplierRequest }) => supplierApi.update(id, data),
    onSuccess: (_, { id }) => {
      queryClient.invalidateQueries({ queryKey: QUERY_KEYS.all });
      queryClient.invalidateQueries({ queryKey: QUERY_KEYS.detail(id) });
      toast.success('Supplier updated successfully');
    },
    onError: (error: any) => {
      toast.error(error.message || 'Failed to update supplier');
    },
  });
}

export function useDeleteSupplier() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (id: number) => supplierApi.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: QUERY_KEYS.all });
      toast.success('Supplier deleted successfully');
    },
    onError: (error: any) => {
      toast.error(error.message || 'Failed to delete supplier');
    },
  });
}
