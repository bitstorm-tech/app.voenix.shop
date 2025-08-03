/**
 * URL utilities for safe URL manipulation across environments
 */

/**
 * Get the frontend base URL for the current environment
 * This should be used for client-side URL manipulations
 */
export function getFrontendBaseUrl(): string {
  if (typeof window !== 'undefined') {
    // Client-side: use current window location
    return window.location.origin;
  }
  
  // Server-side: use environment variable or default
  return process.env.NEXT_PUBLIC_APP_URL || 'http://localhost:3000';
}

/**
 * Create a URL with the proper base URL for the current environment
 * Safe replacement for hardcoded localhost URLs
 */
export function createUrl(path: string): URL {
  return new URL(path, getFrontendBaseUrl());
}

/**
 * Update URL search parameters safely
 * Returns the pathname + search string for use with Next.js routing
 */
export function updateUrlSearchParams(
  currentPath: string,
  updates: Record<string, string | null>
): string {
  const url = createUrl(currentPath);
  
  Object.entries(updates).forEach(([key, value]) => {
    if (value === null) {
      url.searchParams.delete(key);
    } else {
      url.searchParams.set(key, value);
    }
  });
  
  return url.pathname + url.search;
}