// TEMPORARY: Authentication disabled for development
// To re-enable: revert changes in this file

import type { LoginRequest, User } from '@/types/auth';
import { createContext, ReactNode, useContext, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';

interface AuthContextType {
  user: User | null;
  roles: string[];
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (credentials: LoginRequest) => Promise<void>;
  logout: () => Promise<void>;
  checkSession: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

// TEMPORARY: Mock user for development
const MOCK_USER: User = {
  id: 1,
  email: 'admin@example.com',
  firstName: 'Admin',
  lastName: 'User',
  createdAt: new Date().toISOString(),
  updatedAt: new Date().toISOString(),
};
const MOCK_ROLES = ['ADMIN', 'USER'];

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(MOCK_USER);
  const [roles, setRoles] = useState<string[]>(MOCK_ROLES);
  const [isLoading, setIsLoading] = useState(false);
  const navigate = useNavigate();

  const checkSession = async () => {
    // TEMPORARY: Skip session check, always use mock user
    setUser(MOCK_USER);
    setRoles(MOCK_ROLES);
    setIsLoading(false);
  };

  const login = async (_credentials: LoginRequest) => {
    // TEMPORARY: Skip actual login, just navigate to admin
    setUser(MOCK_USER);
    setRoles(MOCK_ROLES);
    navigate('/admin');
  };

  const logout = async () => {
    // TEMPORARY: Just redirect to home
    navigate('/');
  };

  useEffect(() => {
    // TEMPORARY: Skip session check on mount
    // checkSession();
  }, []);

  const value: AuthContextType = {
    user,
    roles,
    isAuthenticated: !!user,
    isLoading,
    login,
    logout,
    checkSession,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
