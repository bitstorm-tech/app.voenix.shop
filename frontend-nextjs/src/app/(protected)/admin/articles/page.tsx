import { Button } from "@/components/ui/Button";
import { Card, CardContent } from "@/components/ui/Card";
import { serverArticlesApi, type GetArticlesParams } from "@/lib/api/server";
import type { ArticleType } from "@/types/article";
import { Package, Plus } from "lucide-react";
import Link from "next/link";
import { ArticleAccordionList } from "./_components/ArticleAccordionList";
import { ArticleFilters } from "./_components/ArticleFilters";

// Force dynamic rendering since we use cookies
export const dynamic = "force-dynamic";

interface ArticlesPageProps {
  searchParams: Promise<{
    type?: ArticleType;
    page?: string;
    size?: string;
  }>;
}

export default async function ArticlesPage({
  searchParams,
}: ArticlesPageProps) {
  const params = await searchParams;

  const queryParams: GetArticlesParams = {
    page: params.page ? parseInt(params.page) : 0,
    size: params.size ? parseInt(params.size) : 50,
    type: params.type || undefined,
  };

  let data;
  let error: string | null = null;

  try {
    data = await serverArticlesApi.getAll(queryParams);
  } catch (err) {
    error = err instanceof Error ? err.message : "Failed to load articles";
    console.error("Failed to fetch articles:", err);
  }

  const articles = data?.content || [];

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
            {/* Filter */}
            <ArticleFilters />

            {/* Add Button */}
            <Button asChild className="w-full sm:w-auto">
              <Link href="/admin/articles/new">
                <Plus className="mr-2 h-4 w-4" />
                New Article
              </Link>
            </Button>
          </div>
        </div>
      </div>

      {/* Table Card */}
      <Card className="py-0">
        <CardContent className="p-0">
          {error ? (
            <div className="text-destructive flex h-32 flex-col items-center justify-center gap-3">
              <Package className="h-8 w-8" />
              <p>Error loading articles: {error}</p>
            </div>
          ) : articles.length === 0 ? (
            <div className="text-muted-foreground flex h-32 flex-col items-center justify-center gap-3">
              <Package className="h-8 w-8" />
              <p>No articles found</p>
              <Button variant="outline" size="sm" asChild>
                <Link href="/admin/articles/new">
                  <Plus className="mr-2 h-4 w-4" />
                  Add your first article
                </Link>
              </Button>
            </div>
          ) : (
            <ArticleAccordionList articles={articles} />
          )}
        </CardContent>
      </Card>
    </div>
  );
}
