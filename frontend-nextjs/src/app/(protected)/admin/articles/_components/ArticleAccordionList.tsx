import { ArticleVariants } from "@/components/admin/articles/ArticleVariants";
import {
  Accordion,
  AccordionContent,
  AccordionItem,
} from "@/components/ui/Accordion";
import { ArticleImage } from "@/components/ui/ArticleImage";
import { Badge } from "@/components/ui/Badge";
import { Button } from "@/components/ui/Button";
import { ErrorBoundary } from "@/components/ui/ErrorBoundary";
import { getArticleImage } from "@/lib/articleUtils";
import type { Article, ArticleType } from "@/types/article";
import { Edit, Trash2 } from "lucide-react";
import Link from "next/link";
import { AccordionTrigger } from "./AccordionTrigger";
import { DeleteConfirmationDialog } from "./DeleteConfirmationDialog";

const articleTypeLabels: Record<ArticleType, string> = {
  MUG: "Mug",
  SHIRT: "T-Shirt",
};

const articleTypeColors: Record<ArticleType, string> = {
  MUG: "bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200",
  SHIRT: "bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200",
};

interface ArticleAccordionListProps {
  articles: Article[];
  searchParams: {
    open?: string;
    confirmDelete?: string;
    deleteId?: string;
    [key: string]: string | string[] | undefined;
  };
  currentPath: string;
}

export function ArticleAccordionList({ 
  articles, 
  searchParams, 
  currentPath 
}: ArticleAccordionListProps) {
  // Parse open accordion items from URL params
  const openItems = searchParams.open?.split(',') || [];
  
  // Parse delete confirmation state from URL params
  const isConfirmingDelete = searchParams.confirmDelete === 'true';
  const deleteArticleId = searchParams.deleteId ? parseInt(searchParams.deleteId) : null;
  const articleToDelete = deleteArticleId ? articles.find(a => a.id === deleteArticleId) : null;

  return (
    <>
      <Accordion 
        type="multiple" 
        value={openItems.map(id => `article-${id}`)}
        className="w-full"
      >
        {articles.map((article) => {
          const variantCount =
            article.articleType === "MUG"
              ? article.mugVariants?.length || 0
              : article.shirtVariants?.length || 0;

          return (
            <ErrorBoundary key={`error-boundary-${article.id}`}>
              <AccordionItem
                key={article.id}
                value={`article-${article.id}`}
                className="group border-b last:border-b-0"
              >
              <div className="relative">
                <AccordionTrigger articleId={article.id}>
                  <div className="flex w-full items-center justify-between py-3 pr-24">
                    <div className="grid flex-1 grid-cols-1 items-center gap-4 text-left md:grid-cols-6">
                      {/* Image Column */}
                      <div className="flex items-center">
                        <ArticleImage
                          src={getArticleImage(article)}
                          alt={`${article.name} preview`}
                          size="xs"
                        />
                      </div>

                      {/* Name Column */}
                      <div className="flex items-center md:col-span-2">
                        <div className="flex items-center gap-2">
                          <span className="font-medium">{article.name}</span>
                          {variantCount > 0 && (
                            <Badge variant="outline" className="text-xs">
                              {variantCount} variant
                              {variantCount !== 1 ? "s" : ""}
                            </Badge>
                          )}
                          {article.supplierArticleName && (
                            <span className="text-muted-foreground text-sm">
                              {article.supplierArticleName}
                            </span>
                          )}
                        </div>
                      </div>

                      {/* Type Column */}
                      <div className="flex items-center gap-2">
                        <Badge
                          variant="secondary"
                          className={articleTypeColors[article.articleType]}
                        >
                          {articleTypeLabels[article.articleType]}
                        </Badge>
                      </div>

                      {/* Category Column */}
                      <div className="flex items-center">
                        <div className="flex flex-wrap items-center">
                          <span className="font-medium">
                            {article.categoryName || "-"}
                          </span>
                          {article.subcategoryName && (
                            <>
                              <span className="px-1">&gt;</span>
                              <span className="text-muted-foreground text-sm">
                                {article.subcategoryName}
                              </span>
                            </>
                          )}
                        </div>
                      </div>

                      {/* Supplier Column */}
                      <div className="text-muted-foreground flex items-center">
                        {article.supplierName || "-"}
                      </div>
                    </div>

                    {/* Status Badge */}
                    <Badge
                      variant={article.active ? "default" : "secondary"}
                      className={
                        article.active
                          ? "bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200"
                          : ""
                      }
                    >
                      {article.active ? "Active" : "Inactive"}
                    </Badge>
                  </div>
                </AccordionTrigger>

                {/* Actions - Positioned absolutely */}
                <div className="absolute top-1/2 right-6 flex -translate-y-1/2 gap-1 opacity-0 transition-opacity group-hover:opacity-100">
                  <Button
                    variant="ghost"
                    size="sm"
                    asChild
                    title="Edit article"
                  >
                    <Link href={`/admin/articles/${article.id}/edit`}>
                      <Edit className="h-4 w-4" />
                    </Link>
                  </Button>
                  <Button
                    variant="ghost"
                    size="sm"
                    asChild
                    title="Delete article"
                    className="text-destructive hover:text-destructive"
                  >
                    <Link 
                      href={`${currentPath}?${new URLSearchParams({
                        ...Object.fromEntries(
                          Object.entries(searchParams).map(([k, v]) => [k, Array.isArray(v) ? v.join(',') : v || ''])
                        ),
                        confirmDelete: 'true',
                        deleteId: article.id.toString()
                      }).toString()}`}
                    >
                      <Trash2 className="h-4 w-4" />
                    </Link>
                  </Button>
                </div>
              </div>

              <AccordionContent className="px-6">
                <div className="bg-muted/50 rounded-lg p-4">
                  <h4 className="mb-3 text-sm font-medium">Variants</h4>
                  <ErrorBoundary>
                    <ArticleVariants article={article} />
                  </ErrorBoundary>
                </div>
              </AccordionContent>
            </AccordionItem>
            </ErrorBoundary>
          );
        })}
      </Accordion>

      {isConfirmingDelete && articleToDelete && (
        <DeleteConfirmationDialog
          isOpen={isConfirmingDelete}
          articleId={articleToDelete.id}
          articleName={articleToDelete.name}
          currentPath={currentPath}
        />
      )}
    </>
  );
}
