import { authApi } from '@/lib/api';
import type { LoginRequest, LoginResponse, SessionInfo } from '@/types/auth';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';

// Query keys
export const authKeys = {
  all: ['auth'] as const,
  session: () => [...authKeys.all, 'session'] as const,
};

// Check session query
export function useSession() {
  return useQuery({
    queryKey: authKeys.session(),
    queryFn: authApi.checkSession,
    retry: false,
    staleTime: 30 * 1000, // 30 seconds
  });
}

// Login mutation
export function useLogin() {
  const queryClient = useQueryClient();
  const navigate = useNavigate();

  return useMutation({
    mutationFn: (data: LoginRequest) => authApi.login(data),
    onSuccess: (data: LoginResponse) => {
      // Update session in cache
      queryClient.setQueryData<SessionInfo>(authKeys.session(), {
        authenticated: true,
        user: { ...data.user, roles: data.roles },
        roles: data.roles,
      });

      // Invalidate all queries to refetch with new auth state
      queryClient.invalidateQueries();

      // Navigate to admin
      navigate('/admin');
    },
  });
}

// Logout mutation
export function useLogout() {
  const queryClient = useQueryClient();
  const navigate = useNavigate();

  return useMutation({
    mutationFn: authApi.logout,
    onSuccess: () => {
      // Clear all cache
      queryClient.clear();

      // Navigate to login
      navigate('/login');
    },
    onError: () => {
      // Even on error, clear cache and redirect
      queryClient.clear();
      navigate('/login');
    },
  });
}
