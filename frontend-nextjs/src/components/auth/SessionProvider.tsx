"use client";

import {
  createContext,
  useContext,
  useEffect,
  useState,
  ReactNode,
} from "react";
import { SessionInfo } from "@/types/auth";
import { getCachedSession, SessionCache } from "@/lib/auth/session";

interface SessionContextType {
  session: SessionInfo | null;
  isLoading: boolean;
  isAuthenticated: boolean;
  isAdmin: boolean;
  refresh: () => Promise<void>;
  logout: () => void;
}

const SessionContext = createContext<SessionContextType | undefined>(undefined);

interface SessionProviderProps {
  children: ReactNode;
  initialSession?: SessionInfo | null;
}

export function SessionProvider({
  children,
  initialSession = null,
}: SessionProviderProps) {
  const [session, setSession] = useState<SessionInfo | null>(initialSession);
  const [isLoading, setIsLoading] = useState(!initialSession);

  const refresh = async () => {
    setIsLoading(true);
    try {
      const newSession = await getCachedSession();
      setSession(newSession);
    } catch (error) {
      console.error("Error refreshing session:", error);
      setSession(null);
    } finally {
      setIsLoading(false);
    }
  };

  const logout = () => {
    // Clear session state
    setSession(null);
    SessionCache.clear();

    // Make logout request (redirect will be handled by server-side logoutAction)
    fetch("/api/auth/logout", {
      method: "POST",
      credentials: "include",
    }).catch((error) => {
      console.error("Logout request failed:", error);
    });
  };

  useEffect(() => {
    // If no initial session, fetch it
    if (!initialSession) {
      refresh();
    }
  }, [initialSession]);

  const value: SessionContextType = {
    session,
    isLoading,
    isAuthenticated: session?.authenticated === true,
    isAdmin: session?.roles.includes("ADMIN") === true,
    refresh,
    logout,
  };

  return (
    <SessionContext.Provider value={value}>{children}</SessionContext.Provider>
  );
}

export function useSession() {
  const context = useContext(SessionContext);
  if (context === undefined) {
    throw new Error("useSession must be used within a SessionProvider");
  }
  return context;
}

/**
 * Hook to check authentication status in client components
 * Note: Redirects are now handled server-side by middleware
 */
export function useRequireAuth() {
  const { session, isLoading } = useSession();

  return {
    session,
    isLoading,
    isAuthenticated: session?.authenticated === true,
  };
}

/**
 * Hook to check admin access in client components
 * Note: Redirects are now handled server-side by middleware
 */
export function useRequireAdmin() {
  const { session, isLoading } = useSession();

  return {
    session,
    isLoading,
    isAuthenticated: session?.authenticated === true,
    isAdmin: session?.roles.includes("ADMIN") === true,
  };
}
