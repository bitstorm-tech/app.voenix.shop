"use client";

import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/Select";
import type { ArticleType } from "@/types/article";
import { Filter } from "lucide-react";
import { useRouter, useSearchParams } from "next/navigation";
import { useCallback } from "react";

export function ArticleFilters() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const typeFilter = searchParams.get("type") as ArticleType | null;

  const handleTypeFilterChange = useCallback(
    (value: string) => {
      const params = new URLSearchParams(searchParams);

      if (value === "all") {
        params.delete("type");
      } else {
        params.set("type", value);
      }

      // Reset to first page when filtering
      params.delete("page");

      router.push(`/admin/articles?${params.toString()}`);
    },
    [router, searchParams],
  );

  return (
    <div className="flex items-center gap-2">
      <Filter className="text-muted-foreground h-4 w-4" />
      <Select
        value={typeFilter || "all"}
        onValueChange={handleTypeFilterChange}
      >
        <SelectTrigger className="w-[180px]">
          <SelectValue placeholder="Filter by type" />
        </SelectTrigger>
        <SelectContent>
          <SelectItem value="all">All Types</SelectItem>
          <SelectItem value="MUG">Mugs</SelectItem>
          <SelectItem value="SHIRT">T-Shirts</SelectItem>
        </SelectContent>
      </Select>
    </div>
  );
}
