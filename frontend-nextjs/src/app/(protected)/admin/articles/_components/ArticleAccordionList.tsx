"use client";

import { ArticleVariants } from "@/components/admin/articles/ArticleVariants";
import {
  Accordion,
  AccordionContent,
  AccordionItem,
} from "@/components/ui/Accordion";
import { ArticleImage } from "@/components/ui/ArticleImage";
import { Badge } from "@/components/ui/Badge";
import { Button } from "@/components/ui/Button";
import ConfirmationDialog from "@/components/ui/ConfirmationDialog";
import { getArticleImage } from "@/lib/articleUtils";
import { cn } from "@/lib/utils";
import type { Article, ArticleType } from "@/types/article";
import * as AccordionPrimitive from "@radix-ui/react-accordion";
import { ChevronDownIcon, Edit, Trash2 } from "lucide-react";
import Link from "next/link";
import { useState, useTransition, useEffect } from "react";
import { deleteArticleAction } from "../_actions/deleteArticle";

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
}

import { getClientCSRFToken } from "@/lib/auth/csrf.client";

// ... (rest of the imports)

export function ArticleAccordionList({ articles }: ArticleAccordionListProps) {
  const [isDeleting, setIsDeleting] = useState(false);
  const [deleteId, setDeleteId] = useState<number | null>(null);
  const [isPending, startTransition] = useTransition();
  const [csrfToken, setCsrfToken] = useState<string | null>(null);

  useEffect(() => {
    async function fetchToken() {
      const token = await getClientCSRFToken();
      setCsrfToken(token);
    }
    fetchToken();
  }, []);

  const handleDelete = async (id: number) => {
    setDeleteId(id);
    setIsDeleting(true);
  };

  const confirmDelete = async () => {
    if (deleteId && csrfToken) {
      startTransition(async () => {
        const result = await deleteArticleAction(deleteId, csrfToken);
        if (result.success) {
          setIsDeleting(false);
          setDeleteId(null);
        } else {
          // Handle error - could show toast notification here
          console.error("Delete failed:", result.error);
          setIsDeleting(false);
          setDeleteId(null);
        }
      });
    }
  };

  const cancelDelete = () => {
    setIsDeleting(false);
    setDeleteId(null);
  };

  return (
    <>
      <Accordion type="multiple" className="w-full">
        {articles.map((article) => {
          const variantCount =
            article.articleType === "MUG"
              ? article.mugVariants?.length || 0
              : article.shirtVariants?.length || 0;

          return (
            <AccordionItem
              key={article.id}
              value={`article-${article.id}`}
              className="group border-b last:border-b-0"
            >
              <div className="relative">
                <AccordionPrimitive.Header className="flex">
                  <AccordionPrimitive.Trigger
                    data-slot="accordion-trigger"
                    className={cn(
                      "focus-visible:border-ring focus-visible:ring-ring/50 flex w-full items-start gap-4 rounded-md px-6 py-0 text-left text-sm font-medium transition-all outline-none hover:no-underline focus-visible:ring-[3px] disabled:pointer-events-none disabled:opacity-50",
                    )}
                  >
                    <ChevronDownIcon className="text-muted-foreground pointer-events-none size-4 shrink-0 self-center transition-transform duration-200 [[data-state=open]>&]:rotate-180" />
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
                  </AccordionPrimitive.Trigger>
                </AccordionPrimitive.Header>

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
                    onClick={(e) => {
                      e.stopPropagation();
                      handleDelete(article.id);
                    }}
                    title="Delete article"
                    className="text-destructive hover:text-destructive"
                    disabled={isPending && deleteId === article.id}
                  >
                    <Trash2 className="h-4 w-4" />
                  </Button>
                </div>
              </div>

              <AccordionContent className="px-6">
                <div className="bg-muted/50 rounded-lg p-4">
                  <h4 className="mb-3 text-sm font-medium">Variants</h4>
                  <ArticleVariants article={article} />
                </div>
              </AccordionContent>
            </AccordionItem>
          );
        })}
      </Accordion>

      <ConfirmationDialog
        isOpen={isDeleting}
        onConfirm={confirmDelete}
        onCancel={cancelDelete}
        description="Are you sure you want to delete this article? This action cannot be undone."
      />
    </>
  );
}
