"use client";

import { Button } from "@/components/ui/Button";
import { Card, CardContent } from "@/components/ui/Card";
import { AlertTriangle, RefreshCw, Loader2 } from "lucide-react";
import { useRouter, usePathname } from "next/navigation";
import { useEffect, useState } from "react";

interface ErrorProps {
  error: Error & { digest?: string };
  reset: () => void;
}

export default function Error({ error, reset }: ErrorProps) {
  const router = useRouter();
  const pathname = usePathname();
  const [isRedirecting, setIsRedirecting] = useState(false);

  // Check if this is an authentication error
  const isAuthError =
    error.name === "AuthenticationError" ||
    error.message.includes("Authentication required") ||
    error.message.includes("Admin privileges required");

  useEffect(() => {
    // Log the error to an error reporting service
    console.error("Protected route error:", error);

    // If it's an auth error, redirect to login after a short delay
    if (isAuthError && !isRedirecting) {
      setIsRedirecting(true);
      const timer = setTimeout(() => {
        const callbackUrl = encodeURIComponent(pathname);
        router.push(`/login?callbackUrl=${callbackUrl}`);
      }, 1500);

      return () => clearTimeout(timer);
    }
  }, [error, isAuthError, isRedirecting, router, pathname]);

  // Show loading state while redirecting for auth errors
  if (isAuthError && isRedirecting) {
    return (
      <div className="container mx-auto max-w-7xl p-4 md:p-6">
        <Card className="py-0">
          <CardContent className="p-0">
            <div className="flex h-32 flex-col items-center justify-center gap-3 text-center">
              <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
              <div>
                <p className="font-medium">Authentication Required</p>
                <p className="text-muted-foreground text-sm">
                  Redirecting to login...
                </p>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>
    );
  }

  // Show immediate redirect option for auth errors (before auto-redirect kicks in)
  if (isAuthError && !isRedirecting) {
    return (
      <div className="container mx-auto max-w-7xl p-4 md:p-6">
        <Card className="py-0">
          <CardContent className="p-0">
            <div className="flex h-32 flex-col items-center justify-center gap-3 text-center">
              <AlertTriangle className="text-amber-500 h-8 w-8" />
              <div>
                <p className="font-medium">Authentication Required</p>
                <p className="text-muted-foreground text-sm">
                  You need to sign in to access this page
                </p>
              </div>
              <Button
                variant="default"
                size="sm"
                onClick={() => {
                  setIsRedirecting(true);
                  const callbackUrl = encodeURIComponent(pathname);
                  router.push(`/login?callbackUrl=${callbackUrl}`);
                }}
                className="mt-2"
              >
                Go to Login
              </Button>
            </div>
          </CardContent>
        </Card>
      </div>
    );
  }

  // Show regular error UI for non-auth errors
  return (
    <div className="container mx-auto max-w-7xl p-4 md:p-6">
      <Card className="py-0">
        <CardContent className="p-0">
          <div className="flex h-32 flex-col items-center justify-center gap-3 text-center">
            <AlertTriangle className="text-destructive h-8 w-8" />
            <div>
              <p className="text-destructive font-medium">
                Something went wrong!
              </p>
              <p className="text-muted-foreground text-sm">
                {error.message || "An unexpected error occurred"}
              </p>
            </div>
            <Button
              variant="outline"
              size="sm"
              onClick={reset}
              className="mt-2"
            >
              <RefreshCw className="mr-2 h-4 w-4" />
              Try again
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
