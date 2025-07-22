import {
  promptSlotTypesApi,
  promptSlotVariantsApi,
  type CreatePromptSlotTypeRequest,
  type CreatePromptSlotVariantRequest,
  type UpdatePromptSlotTypeRequest,
  type UpdatePromptSlotVariantRequest,
} from '@/lib/api';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';

// Query keys for slot types
export const slotTypeKeys = {
  all: ['slotTypes'] as const,
  lists: () => [...slotTypeKeys.all, 'list'] as const,
  details: () => [...slotTypeKeys.all, 'detail'] as const,
  detail: (id: number) => [...slotTypeKeys.details(), id] as const,
};

// Query keys for slot variants
export const slotVariantKeys = {
  all: ['slotVariants'] as const,
  lists: () => [...slotVariantKeys.all, 'list'] as const,
  byType: (typeId: number) => [...slotVariantKeys.all, 'byType', typeId] as const,
  details: () => [...slotVariantKeys.all, 'detail'] as const,
  detail: (id: number) => [...slotVariantKeys.details(), id] as const,
};

// Slot Types
export function useSlotTypes() {
  return useQuery({
    queryKey: slotTypeKeys.lists(),
    queryFn: promptSlotTypesApi.getAll,
  });
}

export function useSlotType(id: number | undefined) {
  return useQuery({
    queryKey: slotTypeKeys.detail(id!),
    queryFn: () => promptSlotTypesApi.getById(id!),
    enabled: !!id,
  });
}

export function useCreateSlotType() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (data: CreatePromptSlotTypeRequest) => promptSlotTypesApi.create(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: slotTypeKeys.lists() });
      toast.success('Slot type created successfully');
    },
  });
}

export function useUpdateSlotType() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: UpdatePromptSlotTypeRequest }) => promptSlotTypesApi.update(id, data),
    onSuccess: (_, { id }) => {
      queryClient.invalidateQueries({ queryKey: slotTypeKeys.lists() });
      queryClient.invalidateQueries({ queryKey: slotTypeKeys.detail(id) });
      toast.success('Slot type updated successfully');
    },
  });
}

export function useDeleteSlotType() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (id: number) => promptSlotTypesApi.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: slotTypeKeys.lists() });
      queryClient.invalidateQueries({ queryKey: slotVariantKeys.all });
      toast.success('Slot type deleted successfully');
    },
  });
}

export function useUpdateSlotTypePositions() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (positions: { id: number; position: number }[]) => promptSlotTypesApi.updatePositions(positions),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: slotTypeKeys.lists() });
      toast.success('Positions updated successfully');
    },
  });
}

// Slot Variants
export function useSlotVariants() {
  return useQuery({
    queryKey: slotVariantKeys.lists(),
    queryFn: promptSlotVariantsApi.getAll,
  });
}

export function useSlotVariantsByType(typeId: number | undefined) {
  return useQuery({
    queryKey: slotVariantKeys.byType(typeId!),
    queryFn: () => promptSlotVariantsApi.getByTypeId(typeId!),
    enabled: !!typeId,
  });
}

export function useSlotVariant(id: number | undefined) {
  return useQuery({
    queryKey: slotVariantKeys.detail(id!),
    queryFn: () => promptSlotVariantsApi.getById(id!),
    enabled: !!id,
  });
}

export function useCreateSlotVariant() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (data: CreatePromptSlotVariantRequest) => promptSlotVariantsApi.create(data),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: slotVariantKeys.lists() });
      queryClient.invalidateQueries({
        queryKey: slotVariantKeys.byType(variables.promptSlotTypeId),
      });
      toast.success('Slot variant created successfully');
    },
  });
}

export function useUpdateSlotVariant() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: UpdatePromptSlotVariantRequest }) => promptSlotVariantsApi.update(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: slotVariantKeys.all });
      toast.success('Slot variant updated successfully');
    },
  });
}

export function useDeleteSlotVariant() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (id: number) => promptSlotVariantsApi.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: slotVariantKeys.all });
      toast.success('Slot variant deleted successfully');
    },
  });
}
