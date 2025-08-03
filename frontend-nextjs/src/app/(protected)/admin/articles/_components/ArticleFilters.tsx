import type { ArticleType } from "@/types/article";
import { Filter } from "lucide-react";
import { redirect } from "next/navigation";

interface ArticleFiltersProps {
  currentType?: ArticleType | null;
  currentSearchParams: Record<string, string | undefined>;
}

export function ArticleFilters({ 
  currentType, 
  currentSearchParams 
}: ArticleFiltersProps) {
  async function handleFilterChange(formData: FormData) {
    "use server";
    
    const selectedType = formData.get("type") as string;
    const params = new URLSearchParams();
    
    // Preserve existing search params except type and page
    Object.entries(currentSearchParams).forEach(([key, value]) => {
      if (key !== "type" && key !== "page" && value !== undefined) {
        params.set(key, value);
      }
    });
    
    // Add the new type filter if it's not "all"
    if (selectedType && selectedType !== "all") {
      params.set("type", selectedType);
    }
    
    // Reset to first page when filtering
    params.delete("page");
    
    const queryString = params.toString();
    const url = queryString ? `/admin/articles?${queryString}` : "/admin/articles";
    
    redirect(url);
  }

  return (
    <form action={handleFilterChange} className="flex items-center gap-2">
      <Filter className="text-muted-foreground h-4 w-4" />
      <select
        name="type"
        defaultValue={currentType || "all"}
        className="border-input text-muted-foreground focus-visible:border-ring focus-visible:ring-ring/50 flex h-9 w-[180px] items-center justify-between gap-2 rounded-md border bg-transparent px-3 py-2 text-sm shadow-xs transition-[color,box-shadow] outline-none focus-visible:ring-[3px] disabled:cursor-not-allowed disabled:opacity-50"
      >
        <option value="all">All Types</option>
        <option value="MUG">Mugs</option>
        <option value="SHIRT">T-Shirts</option>
      </select>
      <button
        type="submit"
        className="ml-2 rounded-md bg-primary px-3 py-1.5 text-sm font-medium text-primary-foreground shadow-sm transition-colors hover:bg-primary/90 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2"
      >
        Apply
      </button>
    </form>
  );
}
