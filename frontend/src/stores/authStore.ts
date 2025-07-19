import { authApi } from '@/lib/api';
import type { LoginRequest, User } from '@/types/auth';
import { create } from 'zustand';

interface AuthState {
  user: User | null;
  roles: string[];
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (credentials: LoginRequest) => Promise<void>;
  logout: () => Promise<void>;
  checkSession: () => Promise<void>;
}

export const useAuthStore = create<AuthState>((set) => ({
  user: null,
  roles: [],
  isAuthenticated: false,
  isLoading: true,

  login: async (credentials: LoginRequest) => {
    const response = await authApi.login(credentials);
    set({
      user: response.user,
      roles: response.roles,
      isAuthenticated: true,
    });
  },

  logout: async () => {
    try {
      await authApi.logout();
    } finally {
      set({
        user: null,
        roles: [],
        isAuthenticated: false,
      });
    }
  },

  checkSession: async () => {
    try {
      const sessionInfo = await authApi.checkSession();
      if (sessionInfo.authenticated && sessionInfo.user) {
        set({
          user: sessionInfo.user,
          roles: sessionInfo.roles,
          isAuthenticated: true,
          isLoading: false,
        });
      } else {
        set({
          user: null,
          roles: [],
          isAuthenticated: false,
          isLoading: false,
        });
      }
    } catch (error) {
      set({
        user: null,
        roles: [],
        isAuthenticated: false,
        isLoading: false,
      });
    }
  },
}));
