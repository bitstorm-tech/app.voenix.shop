import type { NextRequest } from "next/server";
import { NextResponse } from "next/server";

// Define protected routes that require authentication
const protectedRoutes = ["/admin"];

// Define auth routes that should redirect to admin if already authenticated
const authRoutes = ["/login"];

// Cache for session validation results
const sessionCache = new Map<string, { valid: boolean; timestamp: number }>();
const CACHE_DURATION = 60 * 1000; // 1 minute

async function validateSession(sessionId: string): Promise<boolean> {
  // Check cache first
  const cached = sessionCache.get(sessionId);
  if (cached && Date.now() - cached.timestamp < CACHE_DURATION) {
    return cached.valid;
  }

  try {
    // Validate session with backend
    const response = await fetch(
      `${process.env.BACKEND_URL}/api/auth/session`,
      {
        method: "GET",
        headers: {
          Cookie: `JSESSIONID=${sessionId}`,
        },
      }
    );

    const isValid = response.ok;
    
    // Cache the result
    sessionCache.set(sessionId, { valid: isValid, timestamp: Date.now() });
    
    // Clean up old cache entries
    if (sessionCache.size > 100) {
      const now = Date.now();
      for (const [key, value] of sessionCache) {
        if (now - value.timestamp > CACHE_DURATION) {
          sessionCache.delete(key);
        }
      }
    }

    return isValid;
  } catch (error) {
    console.error("Session validation error:", error);
    // On error, assume session is invalid for security
    return false;
  }
}

export async function middleware(request: NextRequest) {
  const { pathname } = request.nextUrl;

  // Get the JSESSIONID cookie which is set by the backend
  const sessionCookie = request.cookies.get("JSESSIONID");
  const hasSessionCookie = !!sessionCookie?.value;

  // Check if the current path is a protected route
  const isProtectedRoute = protectedRoutes.some((route) =>
    pathname.startsWith(route),
  );

  // Check if the current path is an auth route
  const isAuthRoute = authRoutes.some((route) => pathname.startsWith(route));

  // For protected routes, validate the session
  if (isProtectedRoute && hasSessionCookie) {
    const isValidSession = await validateSession(sessionCookie.value);
    
    if (!isValidSession) {
      // Invalid session, redirect to login
      const loginUrl = new URL("/login", request.url);
      loginUrl.searchParams.set("callbackUrl", pathname);
      return NextResponse.redirect(loginUrl);
    }
  }

  // If accessing a protected route without any session cookie
  if (isProtectedRoute && !hasSessionCookie) {
    const loginUrl = new URL("/login", request.url);
    loginUrl.searchParams.set("callbackUrl", pathname);
    return NextResponse.redirect(loginUrl);
  }

  // If accessing auth routes while having a session cookie
  if (isAuthRoute && hasSessionCookie) {
    // Validate the session before redirecting
    const isValidSession = await validateSession(sessionCookie.value);
    
    if (isValidSession) {
      return NextResponse.redirect(new URL("/admin", request.url));
    }
  }

  // For authenticated users accessing the root, redirect to admin
  if (pathname === "/" && hasSessionCookie) {
    const isValidSession = await validateSession(sessionCookie.value);
    
    if (isValidSession) {
      return NextResponse.redirect(new URL("/admin", request.url));
    }
  }

  // Add security headers to all responses
  const response = NextResponse.next();
  
  // Security headers
  response.headers.set("X-Frame-Options", "DENY");
  response.headers.set("X-Content-Type-Options", "nosniff");
  response.headers.set("Referrer-Policy", "strict-origin-when-cross-origin");
  response.headers.set("Permissions-Policy", "geolocation=(), microphone=(), camera=()");
  
  // Only set HSTS in production
  if (process.env.NODE_ENV === "production") {
    response.headers.set(
      "Strict-Transport-Security",
      "max-age=31536000; includeSubDomains"
    );
  }
  
  return response;
}

export const config = {
  matcher: [
    /*
     * Match all request paths except for the ones starting with:
     * - api (API routes)
     * - _next/static (static files)
     * - _next/image (image optimization files)
     * - favicon.ico (favicon file)
     * - public files (images, etc.)
     */
    "/((?!api|_next/static|_next/image|favicon.ico|.*\\..*$).*)",
  ],
};
