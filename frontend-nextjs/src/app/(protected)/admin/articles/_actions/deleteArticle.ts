"use server";

import { serverArticlesApi } from "@/lib/api/server";
import { validateCSRFToken } from "@/lib/auth/csrf.server";
import { revalidatePath } from "next/cache";

export async function deleteArticleAction(id: number, csrfToken: string) {
  if (!await validateCSRFToken(csrfToken)) {
    return {
      success: false,
      error: "Invalid CSRF token",
    };
  }

  try {
    await serverArticlesApi.delete(id);

    // Revalidate the articles page to refresh the data
    revalidatePath("/admin/articles");

    return { success: true };
  } catch (error) {
    console.error("Failed to delete article:", error);
    return {
      success: false,
      error:
        error instanceof Error ? error.message : "Failed to delete article",
    };
  }
}
