"use client";

import { Button } from "@/components/ui/Button";
import { Card, CardContent } from "@/components/ui/Card";
import { AlertTriangle, RefreshCw } from "lucide-react";
import { useEffect } from "react";

interface ErrorProps {
  error: Error & { digest?: string };
  reset: () => void;
}

export default function Error({ error, reset }: ErrorProps) {
  useEffect(() => {
    // Log the error to an error reporting service
    console.error("Protected route error:", error);
  }, [error]);

  // Show error UI (auth redirects are now handled by middleware)
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
