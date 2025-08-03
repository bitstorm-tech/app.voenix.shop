const CSRF_TOKEN_NAME = "csrf-token";
const CSRF_HEADER_NAME = "X-CSRF-Token";

/**
 * Gets the current CSRF token from cookies (client-side)
 */
export async function getClientCSRFToken(): Promise<string | null> {
  if (typeof window === "undefined") {
    return null;
  }
  const cookies = document.cookie.split(";");
  for (const cookie of cookies) {
    const [name, value] = cookie.trim().split("=");
    if (name === CSRF_TOKEN_NAME) {
      return decodeURIComponent(value);
    }
  }
  return null;
}

export { CSRF_TOKEN_NAME, CSRF_HEADER_NAME };