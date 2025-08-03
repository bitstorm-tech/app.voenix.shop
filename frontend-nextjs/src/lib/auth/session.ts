import { SessionInfo } from "@/types/auth";

const BACKEND_URL = process.env.NEXT_PUBLIC_BACKEND_URL || "http://localhost:8080";

/**
 * Client-side session cache
 */
export class SessionCache {
  private static cache: SessionInfo | null = null;
  private static timestamp: number = 0;
  private static readonly CACHE_DURATION = 60 * 1000; // 1 minute

  static set(session: SessionInfo | null) {
    this.cache = session;
    this.timestamp = Date.now();
  }

  static get(): SessionInfo | null {
    if (this.cache && Date.now() - this.timestamp < this.CACHE_DURATION) {
      return this.cache;
    }
    return null;
  }

  static clear() {
    this.cache = null;
    this.timestamp = 0;
  }
}

/**
 * Get the current user session from the client side
 * This function fetches the session from the backend API
 * and caches it for performance
 */
export async function getCachedSession(): Promise<SessionInfo | null> {
  // Check cache first
  const cached = SessionCache.get();
  if (cached) {
    return cached;
  }

  try {
    const response = await fetch("/api/auth/session", {
      method: "GET",
      credentials: "include",
    });

    if (!response.ok) {
      if (response.status === 401) {
        // Session expired or invalid
        SessionCache.clear();
        return null;
      }
      throw new Error(`Session fetch failed: ${response.status}`);
    }

    const sessionInfo: SessionInfo = await response.json();
    SessionCache.set(sessionInfo);
    return sessionInfo;
  } catch (error) {
    console.error("Error fetching session:", error);
    SessionCache.clear();
    return null;
  }
}