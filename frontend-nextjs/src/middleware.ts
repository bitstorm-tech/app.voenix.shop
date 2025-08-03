import type { NextRequest } from "next/server";
import { NextResponse } from "next/server";

// Define auth routes that should redirect to admin if already authenticated
const authRoutes = ["/login"];

// Define all protected routes that require authentication
const protectedRoutes = ["/admin", "/cart", "/checkout"];

// Define admin routes that require admin role
const adminRoutes = ["/admin"];

// Cache for session validation results
const sessionCache = new Map<
  string,
  { valid: boolean; timestamp: number; user?: { roles?: string[] } }
>();
const CACHE_DURATION = 60 * 1000; // 1 minute

async function validateSession(
  sessionId: string,
): Promise<{ valid: boolean; user?: { roles?: string[] } }> {
  // Check cache first
  const cached = sessionCache.get(sessionId);
  if (cached && Date.now() - cached.timestamp < CACHE_DURATION) {
    return { valid: cached.valid, user: cached.user };
  }

  try {
    // Validate session with backend
    const response = await fetch(
      `${process.env.BACKEND_URL}/api/auth/session`,
      {
        method: "GET",
        headers: {
          Cookie: `SESSION=${sessionId}`,
        },
      },
    );

    if (response.ok) {
      const user = await response.json();

      // Cache the result with user data
      sessionCache.set(sessionId, {
        valid: true,
        timestamp: Date.now(),
        user,
      });

      // Clean up old cache entries
      if (sessionCache.size > 100) {
        const now = Date.now();
        for (const [key, value] of sessionCache) {
          if (now - value.timestamp > CACHE_DURATION) {
            sessionCache.delete(key);
          }
        }
      }

      return { valid: true, user };
    } else {
      // Cache invalid session
      sessionCache.set(sessionId, {
        valid: false,
        timestamp: Date.now(),
      });
      return { valid: false };
    }
  } catch (error) {
    console.error("Session validation error:", error);
    // On error, assume session is invalid for security
    return { valid: false };
  }
}

export async function middleware(request: NextRequest) {
  const { pathname } = request.nextUrl;

  // Get the SESSION cookie which is set by the backend
  const sessionCookie = request.cookies.get("SESSION");
  const hasSessionCookie = !!sessionCookie?.value;

  // Check if the current path is an auth route
  const isAuthRoute = authRoutes.some((route) => pathname.startsWith(route));

  // Check if the current path is a protected route
  const isProtectedRoute = protectedRoutes.some((route) =>
    pathname.startsWith(route),
  );

  // Check if the current path is an admin route
  const isAdminRoute = adminRoutes.some((route) => pathname.startsWith(route));

  // Handle auth routes (/login) - redirect authenticated users to their callback URL or /admin
  if (isAuthRoute && hasSessionCookie) {
    const sessionResult = await validateSession(sessionCookie.value);

    if (sessionResult.valid) {
      // Get callback URL from query params or default to /admin
      const callbackUrl =
        request.nextUrl.searchParams.get("callbackUrl") || "/admin";
      return NextResponse.redirect(new URL(callbackUrl, request.url));
    }
  }

  // Handle protected routes - redirect unauthenticated users to login with callbackUrl
  if (isProtectedRoute) {
    if (!hasSessionCookie) {
      const loginUrl = new URL("/login", request.url);
      loginUrl.searchParams.set("callbackUrl", pathname);
      return NextResponse.redirect(loginUrl);
    }

    const sessionResult = await validateSession(sessionCookie.value);

    if (!sessionResult.valid) {
      const loginUrl = new URL("/login", request.url);
      loginUrl.searchParams.set("callbackUrl", pathname);
      return NextResponse.redirect(loginUrl);
    }

    // Additional check for admin routes - require ADMIN role
    if (isAdminRoute) {
      const user = sessionResult.user;
      if (!user?.roles?.includes("ADMIN")) {
        // Redirect non-admin users to login
        const loginUrl = new URL("/login", request.url);
        loginUrl.searchParams.set("callbackUrl", pathname);
        return NextResponse.redirect(loginUrl);
      }
    }
  }

  // Handle root route (/) - redirect authenticated users to /admin
  if (pathname === "/" && hasSessionCookie) {
    const sessionResult = await validateSession(sessionCookie.value);

    if (sessionResult.valid) {
      return NextResponse.redirect(new URL("/admin", request.url));
    }
  }

  // Add security headers to all responses
  const response = NextResponse.next();

  // Security headers
  response.headers.set("X-Frame-Options", "DENY");
  response.headers.set("X-Content-Type-Options", "nosniff");
  response.headers.set("Referrer-Policy", "strict-origin-when-cross-origin");
  response.headers.set(
    "Permissions-Policy",
    "geolocation=(), microphone=(), camera=()",
  );

  // Only set HSTS in production
  if (process.env.NODE_ENV === "production") {
    response.headers.set(
      "Strict-Transport-Security",
      "max-age=31536000; includeSubDomains",
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
