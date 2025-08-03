"use client";

import { Button } from "@/components/ui/Button";
import { Card, CardContent } from "@/components/ui/Card";
import { AlertTriangle, RefreshCw, Home } from "lucide-react";
import Link from "next/link";

interface ErrorProps {
  error: Error & { digest?: string };
  reset: () => void;
}

export default function Error({ error, reset }: ErrorProps) {
  return (
    <div className="container mx-auto max-w-7xl p-4 md:p-6">
      {/* Page Header */}
      <div className="mb-8">
        <div className="flex flex-col gap-6 md:flex-row md:items-center md:justify-between">
          <div>
            <h1 className="text-3xl font-bold tracking-tight">Articles</h1>
            <p className="text-muted-foreground mt-2">
              Manage your product catalog
            </p>
          </div>
        </div>
      </div>

      {/* Error Card */}
      <Card className="py-0">
        <CardContent className="p-0">
          <div className="flex h-32 flex-col items-center justify-center gap-3 text-center">
            <AlertTriangle className="text-destructive h-8 w-8" />
            <div>
              <p className="text-destructive font-medium">
                Something went wrong!
              </p>
              <p className="text-muted-foreground text-sm">{error.message}</p>
            </div>
            <div className="flex gap-2 mt-2">
              <Button
                variant="outline"
                size="sm"
                onClick={reset}
              >
                <RefreshCw className="mr-2 h-4 w-4" />
                Try again
              </Button>
              <Button
                variant="default"
                size="sm"
                asChild
              >
                <Link href="/admin/articles">
                  <Home className="mr-2 h-4 w-4" />
                  Back to Articles
                </Link>
              </Button>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
