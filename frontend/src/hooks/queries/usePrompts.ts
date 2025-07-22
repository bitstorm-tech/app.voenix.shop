import { promptsApi, type CreatePromptRequest, type UpdatePromptRequest } from '@/lib/api';
import type { Prompt } from '@/types/prompt';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';

// Query keys
export const promptKeys = {
  all: ['prompts'] as const,
  lists: () => [...promptKeys.all, 'list'] as const,
  list: (filters?: any) => [...promptKeys.lists(), filters] as const,
  details: () => [...promptKeys.all, 'detail'] as const,
  detail: (id: number) => [...promptKeys.details(), id] as const,
  search: (title: string) => [...promptKeys.all, 'search', title] as const,
};

// Get all prompts
export function usePrompts() {
  return useQuery({
    queryKey: promptKeys.lists(),
    queryFn: promptsApi.getAll,
  });
}

// Get prompt by ID
export function usePrompt(id: number | undefined) {
  return useQuery({
    queryKey: promptKeys.detail(id!),
    queryFn: () => promptsApi.getById(id!),
    enabled: !!id,
  });
}

// Search prompts
export function useSearchPrompts(title: string) {
  return useQuery({
    queryKey: promptKeys.search(title),
    queryFn: () => promptsApi.search(title),
    enabled: title.length > 0,
  });
}

// Create prompt mutation
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

// Update prompt mutation
export function useUpdatePrompt() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: UpdatePromptRequest }) => promptsApi.update(id, data),
    onSuccess: (updatedPrompt, { id }) => {
      // Update the specific prompt in cache
      queryClient.setQueryData(promptKeys.detail(id), updatedPrompt);

      // Invalidate list to ensure consistency
      queryClient.invalidateQueries({ queryKey: promptKeys.lists() });

      toast.success('Prompt updated successfully');
    },
  });
}

// Delete prompt mutation
export function useDeletePrompt() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (id: number) => promptsApi.delete(id),
    onSuccess: (_, id) => {
      // Remove from cache
      queryClient.removeQueries({ queryKey: promptKeys.detail(id) });

      // Invalidate list
      queryClient.invalidateQueries({ queryKey: promptKeys.lists() });

      toast.success('Prompt deleted successfully');
    },
  });
}

// Add slots to prompt
export function useAddPromptSlots() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ id, slotIds }: { id: number; slotIds: number[] }) => promptsApi.addSlots(id, slotIds),
    onSuccess: (updatedPrompt, { id }) => {
      // Update the specific prompt in cache
      queryClient.setQueryData(promptKeys.detail(id), updatedPrompt);

      // Invalidate list
      queryClient.invalidateQueries({ queryKey: promptKeys.lists() });

      toast.success('Slots added successfully');
    },
  });
}

// Update prompt slots
export function useUpdatePromptSlots() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ id, slots }: { id: number; slots: any[] }) => promptsApi.updateSlots(id, slots),
    onSuccess: (updatedPrompt, { id }) => {
      // Update the specific prompt in cache
      queryClient.setQueryData(promptKeys.detail(id), updatedPrompt);

      // Invalidate list
      queryClient.invalidateQueries({ queryKey: promptKeys.lists() });

      toast.success('Slots updated successfully');
    },
  });
}
