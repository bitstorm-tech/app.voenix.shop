import "server-only";
import { cookies } from "next/headers";
import { SessionInfo } from "@/types/auth";

const BACKEND_URL =
  process.env.NEXT_PUBLIC_BACKEND_URL || "http://localhost:8080";

/**
 * Server-side authentication utilities for Next.js
 * These functions only run on the server and handle session validation
 */

/**
 * Get the current user session from the server
 * This function validates the JSESSIONID cookie with the backend
 */
export async function getSession(): Promise<SessionInfo | null> {
  try {
    const cookieStore = await cookies();
    const sessionCookie = cookieStore.get("JSESSIONID");

    if (!sessionCookie?.value) {
      return null;
    }

    // Make request to backend with the session cookie
    const response = await fetch(`${BACKEND_URL}/api/auth/session`, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        Cookie: `JSESSIONID=${sessionCookie.value}`,
      },
      credentials: "include",
    });

    if (!response.ok) {
      // If session is invalid, return null
      return null;
    }

    const sessionInfo: SessionInfo = await response.json();
    return sessionInfo;
  } catch (error) {
    // In case of network error or other issues, assume not authenticated
    console.error("Error validating session:", error);
    return null;
  }
}

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
    throw new Error("Authentication required");
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
    throw new Error("Admin access required");
  }

  return session;
}
