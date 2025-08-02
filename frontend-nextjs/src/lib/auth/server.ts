import "server-only";
import { cookies } from "next/headers";
import { SessionInfo } from "@/types/auth";
import { cache } from "react";

const BACKEND_URL =
  process.env.BACKEND_URL || "http://localhost:8080";

/**
 * Custom error classes for better error handling
 */
export class AuthenticationError extends Error {
  constructor(message: string, public code: string) {
    super(message);
    this.name = "AuthenticationError";
  }
}

export class NetworkError extends Error {
  constructor(message: string, public cause?: Error) {
    super(message);
    this.name = "NetworkError";
  }
}

/**
 * Server-side authentication utilities for Next.js
 * These functions only run on the server and handle session validation
 */

/**
 * Get the current user session from the server
 * This function validates the JSESSIONID cookie with the backend
 * Cached per request to avoid multiple backend calls
 */
export const getSession = cache(async (): Promise<SessionInfo | null> => {
  try {
    const cookieStore = await cookies();
    const sessionCookie = cookieStore.get("JSESSIONID");

    if (!sessionCookie?.value) {
      // No session cookie present
      return null;
    }

    // Make request to backend with the session cookie
    const response = await fetch(`${BACKEND_URL}/api/auth/session`, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        Cookie: `JSESSIONID=${sessionCookie.value}`,
      },
      // Note: credentials: "include" doesn't work in server-side fetch
      cache: "no-store", // Don't cache auth responses
    });

    if (!response.ok) {
      if (response.status === 401) {
        // Session expired or invalid
        console.warn("Session validation failed: Invalid or expired session");
        return null;
      }
      
      // Other HTTP errors
      console.error(`Session validation failed with status: ${response.status}`);
      return null;
    }

    const sessionInfo: SessionInfo = await response.json();
    return sessionInfo;
  } catch (error) {
    // Detailed error logging based on error type
    if (error instanceof TypeError && error.message.includes('fetch')) {
      console.error("Network error connecting to backend:", {
        message: error.message,
        backend: BACKEND_URL,
      });
      throw new NetworkError("Unable to connect to authentication service", error);
    }
    
    if (error instanceof SyntaxError) {
      console.error("Invalid JSON response from backend:", error);
      return null;
    }
    
    // Unexpected errors
    console.error("Unexpected error validating session:", error);
    return null;
  }
});

/**
 * Check if the current user is authenticated
 */
export async function isAuthenticated(): Promise<boolean> {
  const session = await getSession();
  return session?.authenticated === true;
}

/**
 * Check if the current user has admin role
 */
export async function isAdmin(): Promise<boolean> {
  const session = await getSession();
  return session?.roles.includes("ADMIN") === true;
}

/**
 * Require authentication - throws if user is not authenticated
 * Use this in server components/actions that require authentication
 */
export async function requireAuth(): Promise<SessionInfo> {
  const session = await getSession();

  if (!session?.authenticated) {
    throw new AuthenticationError(
      "Authentication required to access this resource",
      "AUTH_REQUIRED"
    );
  }

  return session;
}

/**
 * Require admin role - throws if user is not admin
 * Use this in server components/actions that require admin access
 */
export async function requireAdmin(): Promise<SessionInfo> {
  const session = await requireAuth();

  if (!session.roles.includes("ADMIN")) {
    throw new AuthenticationError(
      "Admin privileges required to access this resource",
      "ADMIN_REQUIRED"
    );
  }

  return session;
}

/**
 * Check if user has a specific role
 */
export async function hasRole(role: string): Promise<boolean> {
  try {
    const session = await getSession();
    return session?.roles.includes(role) === true;
  } catch {
    return false;
  }
}
