"use server";

import { serverArticlesApi } from "@/lib/api/server";
import { revalidatePath } from "next/cache";

export async function deleteArticleAction(id: number) {
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
