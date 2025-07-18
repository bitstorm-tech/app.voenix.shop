import { authApi } from '@/lib/api';
import type { LoginRequest, SessionInfo, User } from '@/types/auth';
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

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [roles, setRoles] = useState<string[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const navigate = useNavigate();

  const checkSession = async () => {
    try {
      const sessionInfo: SessionInfo = await authApi.checkSession();
      if (sessionInfo.authenticated && sessionInfo.user) {
        setUser(sessionInfo.user);
        setRoles(sessionInfo.roles);
      } else {
        setUser(null);
        setRoles([]);
      }
    } catch (error) {
      setUser(null);
      setRoles([]);
    } finally {
      setIsLoading(false);
    }
  };

  const login = async (credentials: LoginRequest) => {
    const response = await authApi.login(credentials);
    setUser(response.user);
    setRoles(response.roles);
    navigate('/admin');
  };

  const logout = async () => {
    try {
      await authApi.logout();
    } finally {
      setUser(null);
      setRoles([]);
      navigate('/login');
    }
  };

  useEffect(() => {
    checkSession();
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
