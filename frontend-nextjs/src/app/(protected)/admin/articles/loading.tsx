import { Card, CardContent } from "@/components/ui/Card";
import { Package } from "lucide-react";

export default function Loading() {
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

          <div className="flex flex-col gap-3 sm:flex-row sm:items-center">
            {/* Filter skeleton */}
            <div className="flex items-center gap-2">
              <div className="h-4 w-4 animate-pulse rounded bg-gray-200" />
              <div className="h-10 w-[180px] animate-pulse rounded bg-gray-200" />
            </div>

            {/* Add Button skeleton */}
            <div className="h-10 w-full animate-pulse rounded bg-gray-200 sm:w-auto sm:w-32" />
          </div>
        </div>
      </div>

      {/* Table Card */}
      <Card className="py-0">
        <CardContent className="p-0">
          <div className="text-muted-foreground flex h-32 flex-col items-center justify-center gap-3">
            <Package className="h-8 w-8 animate-pulse" />
            <p>Loading articles...</p>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
