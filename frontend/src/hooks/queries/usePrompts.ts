import { promptsApi, type CreatePromptRequest, type PromptSlotUpdate, type UpdatePromptRequest } from '@/lib/api';
import type { Prompt } from '@/types/prompt';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';

// Type for prompt list filters
interface PromptListFilters {
  [key: string]: unknown;
}

// Query keys
export const promptKeys = {
  all: ['prompts'] as const,
  lists: () => [...promptKeys.all, 'list'] as const,
  list: (filters?: PromptListFilters) => [...promptKeys.lists(), filters] as const,
  details: () => [...promptKeys.all, 'detail'] as const,
  detail: (id: number) => [...promptKeys.details(), id] as const,
  search: (title: string) => [...promptKeys.all, 'search', title] as const,
};

export function usePrompts() {
  return useQuery({
    queryKey: promptKeys.lists(),
    queryFn: promptsApi.getAll,
  });
}

export function usePrompt(id: number | undefined) {
  return useQuery({
    queryKey: promptKeys.detail(id!),
    queryFn: () => promptsApi.getById(id!),
    enabled: !!id,
  });
}

export function useSearchPrompts(title: string) {
  return useQuery({
    queryKey: promptKeys.search(title),
    queryFn: () => promptsApi.search(title),
    enabled: title.length > 0,
  });
}

export function useCreatePrompt() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (data: CreatePromptRequest) => promptsApi.create(data),
    onSuccess: (newPrompt) => {
      // Invalidate and refetch prompts list
      queryClient.invalidateQueries({ queryKey: promptKeys.lists() });

      // Optionally add the new prompt to cache immediately
      queryClient.setQueryData<Prompt[]>(promptKeys.lists(), (old) => {
        return old ? [...old, newPrompt] : [newPrompt];
      });

      toast.success('Prompt created successfully');
    },
  });
}

export function useUpdatePrompt() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: UpdatePromptRequest }) => promptsApi.update(id, data),
    onSuccess: (updatedPrompt, { id }) => {
      queryClient.setQueryData(promptKeys.detail(id), updatedPrompt);

      // Invalidate list to ensure consistency
      queryClient.invalidateQueries({ queryKey: promptKeys.lists() });

      toast.success('Prompt updated successfully');
    },
  });
}

export function useDeletePrompt() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (id: number) => promptsApi.delete(id),
    onSuccess: (_, id) => {
      queryClient.removeQueries({ queryKey: promptKeys.detail(id) });

      // Invalidate list
      queryClient.invalidateQueries({ queryKey: promptKeys.lists() });

      toast.success('Prompt deleted successfully');
    },
  });
}

export function useAddPromptSlots() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ id, slotIds }: { id: number; slotIds: number[] }) => promptsApi.addSlots(id, slotIds),
    onSuccess: (updatedPrompt, { id }) => {
      queryClient.setQueryData(promptKeys.detail(id), updatedPrompt);

      // Invalidate list
      queryClient.invalidateQueries({ queryKey: promptKeys.lists() });

      toast.success('Slots added successfully');
    },
  });
}

export function useUpdatePromptSlots() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ id, slots }: { id: number; slots: PromptSlotUpdate[] }) => promptsApi.updateSlots(id, slots),
    onSuccess: (updatedPrompt, { id }) => {
      queryClient.setQueryData(promptKeys.detail(id), updatedPrompt);

      // Invalidate list
      queryClient.invalidateQueries({ queryKey: promptKeys.lists() });

      toast.success('Slots updated successfully');
    },
  });
}
