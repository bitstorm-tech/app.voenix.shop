import { cookies } from "next/headers";
import crypto from "crypto";

const CSRF_TOKEN_NAME = "csrf-token";
const CSRF_HEADER_NAME = "X-CSRF-Token";

/**
 * Generates a new CSRF token and stores it in a secure cookie
 */
export async function generateCSRFToken(): Promise<string> {
  const token = crypto.randomBytes(32).toString("hex");
  const cookieStore = await cookies();
  
  cookieStore.set(CSRF_TOKEN_NAME, token, {
    httpOnly: true,
    secure: process.env.NODE_ENV === "production",
    sameSite: "strict",
    path: "/",
    maxAge: 60 * 60 * 24, // 24 hours
  });
  
  return token;
}

/**
 * Gets the current CSRF token from cookies
 */
export async function getCSRFToken(): Promise<string | null> {
  const cookieStore = await cookies();
  const token = cookieStore.get(CSRF_TOKEN_NAME);
  return token?.value || null;
}

/**
 * Validates a CSRF token from the request
 */
export async function validateCSRFToken(requestToken: string | null): Promise<boolean> {
  if (!requestToken) {
    return false;
  }
  
  const storedToken = await getCSRFToken();
  if (!storedToken) {
    return false;
  }
  
  // Use timing-safe comparison to prevent timing attacks
  return crypto.timingSafeEqual(
    Buffer.from(requestToken),
    Buffer.from(storedToken)
  );
}

/**
 * Gets CSRF token from request headers or form data
 */
export function getCSRFTokenFromRequest(request: Request): string | null {
  // Check header first
  const headerToken = request.headers.get(CSRF_HEADER_NAME);
  if (headerToken) {
    return headerToken;
  }
  
  // Check form data if it's a form submission
  const contentType = request.headers.get("content-type");
  if (contentType?.includes("application/x-www-form-urlencoded")) {
    // This would need to be parsed from the body
    // For server actions, Next.js handles this differently
    return null;
  }
  
  return null;
}

/**
 * Middleware helper to validate CSRF for API routes
 */
export async function requireCSRFToken(request: Request): Promise<Response | null> {
  // Skip CSRF check for GET requests
  if (request.method === "GET" || request.method === "HEAD") {
    return null;
  }
  
  const token = getCSRFTokenFromRequest(request);
  const isValid = await validateCSRFToken(token);
  
  if (!isValid) {
    return new Response("Invalid CSRF token", { status: 403 });
  }
  
  return null;
}