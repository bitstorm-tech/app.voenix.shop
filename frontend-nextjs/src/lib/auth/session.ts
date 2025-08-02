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
 * Get CSRF token from cookies (client-side)
 */
async function getCSRFTokenFromCookie(): Promise<string | null> {
  const cookies = document.cookie.split(';');
  for (const cookie of cookies) {
    const [name, value] = cookie.trim().split('=');
    if (name === 'csrf-token') {
      return decodeURIComponent(value);
    }
  }
  return null;
}

/**
 * Logout the current user by calling the logout endpoint
 */
export async function logout(): Promise<void> {
  try {
    // Get CSRF token for logout request
    const csrfToken = await getCSRFTokenFromCookie();
    
    await fetch("/api/auth/logout", {
      method: "POST",
      credentials: "include",
      headers: {
        "Content-Type": "application/json",
        ...(csrfToken && { "X-CSRF-Token": csrfToken }),
      },
    });

    // Clear session cache
    SessionCache.clear();

    // Redirect to login page after logout
    window.location.href = "/login";
  } catch (error) {
    console.error("Error during logout:", error);
    // Even if logout fails, clear cache and redirect to login
    SessionCache.clear();
    window.location.href = "/login";
  }
}

/**
 * Session storage keys for client-side session management
 */
export const SESSION_KEYS = {
  AUTH_STATE: "auth-state",
  LAST_CHECKED: "session-last-checked",
} as const;

/**
 * Session cache utilities for client-side performance
 * Only stores non-sensitive authentication state, not user data
 */
export class SessionCache {
  private static readonly CACHE_DURATION = 30 * 1000; // 30 seconds

  /**
   * Get cached authentication state (not full session data)
   */
  static getAuthState(): boolean | null {
    try {
      const authState = sessionStorage.getItem(SESSION_KEYS.AUTH_STATE);
      const lastChecked = sessionStorage.getItem(SESSION_KEYS.LAST_CHECKED);

      if (!authState || !lastChecked) {
        return null;
      }

      const isExpired =
        Date.now() - parseInt(lastChecked) > this.CACHE_DURATION;
      if (isExpired) {
        this.clear();
        return null;
      }

      return authState === "true";
    } catch {
      return null;
    }
  }

  /**
   * Set authentication state (only stores boolean, not sensitive data)
   */
  static setAuthState(isAuthenticated: boolean): void {
    try {
      sessionStorage.setItem(SESSION_KEYS.AUTH_STATE, String(isAuthenticated));
      sessionStorage.setItem(SESSION_KEYS.LAST_CHECKED, Date.now().toString());
    } catch {
      // Ignore storage errors (private browsing, etc.)
    }
  }

  static clear(): void {
    try {
      sessionStorage.removeItem(SESSION_KEYS.AUTH_STATE);
      sessionStorage.removeItem(SESSION_KEYS.LAST_CHECKED);
    } catch {
      // Ignore storage errors
    }
  }
}

/**
 * Get session with caching of auth state only
 */
export async function getCachedSession(): Promise<SessionInfo | null> {
  // Check if we have a cached auth state
  const cachedAuthState = SessionCache.getAuthState();
  
  // Always fetch fresh session data from server
  const session = await getClientSession();
  
  // Only cache the authentication state, not the full session
  if (session) {
    SessionCache.setAuthState(session.authenticated);
  } else {
    SessionCache.clear();
  }

  return session;
}
