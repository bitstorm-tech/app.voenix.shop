"use server";

import { serverArticlesApi } from "@/lib/api/server";
import { getCSRFToken } from "@/lib/auth/csrf.server";
import { updateUrlSearchParams } from "@/lib/urls";
import { revalidatePath } from "next/cache";
import { redirect } from "next/navigation";

export async function deleteArticleWithFormAction(formData: FormData) {
  const articleId = formData.get("articleId");
  const redirectPath = formData.get("redirectPath") || "/admin/articles";
  
  if (!articleId) {
    throw new Error("Article ID is required");
  }

  // Get CSRF token from server-side cookies
  const csrfToken = await getCSRFToken();
  if (!csrfToken) {
    throw new Error("CSRF token not found");
  }

  try {
    await serverArticlesApi.delete(Number(articleId), csrfToken);

    // Revalidate the articles page to refresh the data
    revalidatePath("/admin/articles");

    // Remove any delete confirmation parameters from URL
    const cleanUrl = updateUrlSearchParams(redirectPath.toString(), {
      confirmDelete: null,
      deleteId: null,
    });
    
    redirect(cleanUrl);
  } catch (error) {
    console.error("Failed to delete article:", error);
    throw new Error(
      error instanceof Error ? error.message : "Failed to delete article"
    );
  }
}