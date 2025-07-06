import { clsx, type ClassValue } from 'clsx';
import { twMerge } from 'tailwind-merge';

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

function getCsrfToken(): string {
  const meta = document.querySelector('meta[name="csrf-token"]');
  return meta?.getAttribute('content') || '';
}

async function refreshCsrfToken(): Promise<string> {
  try {
    const response = await fetch('/api/csrf-token');
    if (!response.ok) {
      throw new Error('Failed to refresh CSRF token');
    }
    const data = await response.json();
    const newCsrfToken = data.csrf_token;

    const meta = document.querySelector('meta[name="csrf-token"]');
    if (meta) {
      meta.setAttribute('content', newCsrfToken);
    }
    return newCsrfToken;
  } catch (error) {
    console.error('CSRF refresh error:', error);
    // If refresh fails, force a reload to get a new session and token
    window.location.reload();
    throw new Error('CSRF token refresh failed. The page will reload.');
  }
}

type FetchOptions = RequestInit & {
  skipCsrf?: boolean;
};

export async function apiFetch(url: string, options: FetchOptions = {}, isRetry = false): Promise<Response> {
  const { skipCsrf = false, headers = {}, ...restOptions } = options;

  const defaultHeaders: Record<string, string> = {
    'X-Requested-With': 'XMLHttpRequest',
    Accept: 'application/json',
  };

  // Merge provided headers
  if (headers instanceof Headers) {
    headers.forEach((value, key) => {
      defaultHeaders[key] = value;
    });
  } else if (Array.isArray(headers)) {
    headers.forEach(([key, value]) => {
      defaultHeaders[key] = value;
    });
  } else {
    Object.assign(defaultHeaders, headers);
  }

  // Add CSRF token for non-GET requests
  if (!skipCsrf && restOptions.method && restOptions.method !== 'GET') {
    defaultHeaders['X-CSRF-TOKEN'] = getCsrfToken();
  }

  // Add Content-Type for JSON payloads if not already set
  // Don't set Content-Type for FormData - browser will set it with boundary
  if (restOptions.body && !(restOptions.body instanceof FormData) && !defaultHeaders['Content-Type']) {
    if (typeof restOptions.body === 'string') {
      try {
        JSON.parse(restOptions.body);
        defaultHeaders['Content-Type'] = 'application/json';
      } catch {
        // Not JSON, do nothing
      }
    }
  }

  const response = await fetch(url, {
    ...restOptions,
    headers: defaultHeaders,
  });

  // Handle CSRF token mismatch errors
  if (response.status === 419) {
    if (isRetry) {
      // We've already retried once, so something is wrong.
      window.location.reload();
      throw new Error('CSRF token mismatch after retry. Page will reload.');
    }

    console.log('CSRF token mismatch detected. Refreshing token and retrying...');
    await refreshCsrfToken();

    // Retry the original request with the new token
    return apiFetch(url, options, true);
  }

  return response;
}
