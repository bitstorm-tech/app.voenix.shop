import { authApi, RegisterRequest } from '@/lib/api';
import { queryClient } from '@/lib/queryClient';
import { useWizardStore } from '@/stores/editor/useWizardStore';
import type { LoginResponse, SessionInfo } from '@/types/auth';
import { useMutation } from '@tanstack/react-query';
import { authKeys } from './useAuth';

// Register mutation for wizard flow
export function useRegister() {
  const setAuthenticated = useWizardStore((state) => state.setAuthenticated);

  return useMutation({
    mutationFn: (data: RegisterRequest) => authApi.register(data),
    onSuccess: async (data: LoginResponse) => {
      // Update session in cache
      queryClient.setQueryData<SessionInfo>(authKeys.session(), {
        authenticated: true,
        user: { ...data.user, roles: data.roles },
        roles: data.roles,
      });

      // Invalidate queries to refetch with new auth state
      await queryClient.invalidateQueries();

      // Update wizard store with authentication state
      setAuthenticated(true, {
        id: data.user.id,
        email: data.user.email,
        firstName: data.user.firstName,
        lastName: data.user.lastName,
        phoneNumber: data.user.phoneNumber,
        roles: data.roles,
      });
    },
  });
}
