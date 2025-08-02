import { SessionInfo } from "@/types/auth";

/**
 * Client-side session management utilities
 * These functions handle session state on the client side
 */

/**
 * Get session information from the server
 * This is used by client components to check authentication status
 */
export async function getClientSession(): Promise<SessionInfo | null> {
  try {
    const response = await fetch("/api/auth/session", {
      method: "GET",
      credentials: "include",
      cache: "no-store", // Don't cache auth checks
    });

    if (!response.ok) {
      return null;
    }

    const sessionInfo: SessionInfo = await response.json();
    return sessionInfo;
  } catch (error) {
    console.error("Error fetching session:", error);
    return null;
  }
}

/**
 * Check if user is authenticated (client-side)
 */
export async function isClientAuthenticated(): Promise<boolean> {
  const session = await getClientSession();
  return session?.authenticated === true;
}

/**
 * Logout the current user by calling the logout endpoint
 */
export async function logout(): Promise<void> {
  try {
    await fetch("/api/auth/logout", {
      method: "POST",
      credentials: "include",
    });

    // Redirect to login page after logout
    window.location.href = "/login";
  } catch (error) {
    console.error("Error during logout:", error);
    // Even if logout fails, redirect to login
    window.location.href = "/login";
  }
}

/**
 * Session storage keys for client-side session management
 */
export const SESSION_KEYS = {
  USER_DATA: "user-session",
  LAST_CHECKED: "session-last-checked",
} as const;

/**
 * Session cache utilities for client-side performance
 */
export class SessionCache {
  private static readonly CACHE_DURATION = 30 * 1000; // 30 seconds

  static get(key: string): SessionInfo | null {
    try {
      const cached = sessionStorage.getItem(key);
      const lastChecked = sessionStorage.getItem(SESSION_KEYS.LAST_CHECKED);

      if (!cached || !lastChecked) {
        return null;
      }

      const isExpired =
        Date.now() - parseInt(lastChecked) > this.CACHE_DURATION;
      if (isExpired) {
        this.clear();
        return null;
      }

      return JSON.parse(cached);
    } catch {
      return null;
    }
  }

  static set(key: string, value: SessionInfo): void {
    try {
      sessionStorage.setItem(key, JSON.stringify(value));
      sessionStorage.setItem(SESSION_KEYS.LAST_CHECKED, Date.now().toString());
    } catch {
      // Ignore storage errors (private browsing, etc.)
    }
  }

  static clear(): void {
    try {
      sessionStorage.removeItem(SESSION_KEYS.USER_DATA);
      sessionStorage.removeItem(SESSION_KEYS.LAST_CHECKED);
    } catch {
      // Ignore storage errors
    }
  }
}

/**
 * Get cached session or fetch from server
 */
export async function getCachedSession(): Promise<SessionInfo | null> {
  // Try cache first
  const cached = SessionCache.get(SESSION_KEYS.USER_DATA);
  if (cached) {
    return cached;
  }

  // If not cached or expired, fetch from server
  const session = await getClientSession();
  if (session) {
    SessionCache.set(SESSION_KEYS.USER_DATA, session);
  }

  return session;
}
