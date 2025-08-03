"use client";

import { cn } from "@/lib/utils";
import * as AccordionPrimitive from "@radix-ui/react-accordion";
import { ChevronDownIcon } from "lucide-react";
import { useSearchParams, useRouter, usePathname } from "next/navigation";

interface AccordionTriggerProps {
  articleId: number;
  children: React.ReactNode;
}

export function AccordionTrigger({ articleId, children }: AccordionTriggerProps) {
  const searchParams = useSearchParams();
  const router = useRouter();
  const pathname = usePathname();
  
  const openItems = searchParams.get('open')?.split(',') || [];
  const isOpen = openItems.includes(articleId.toString());

  const handleToggle = () => {
    const params = new URLSearchParams(searchParams);
    const currentOpen = params.get('open')?.split(',').filter(Boolean) || [];
    
    if (isOpen) {
      // Remove this item from open items
      const newOpen = currentOpen.filter(id => id !== articleId.toString());
      if (newOpen.length > 0) {
        params.set('open', newOpen.join(','));
      } else {
        params.delete('open');
      }
    } else {
      // Add this item to open items
      const newOpen = [...currentOpen, articleId.toString()];
      params.set('open', newOpen.join(','));
    }

    router.push(`${pathname}?${params.toString()}`, { scroll: false });
  };

  return (
    <AccordionPrimitive.Header className="flex">
      <button
        onClick={handleToggle}
        className={cn(
          "focus-visible:border-ring focus-visible:ring-ring/50 flex w-full items-start gap-4 rounded-md px-6 py-0 text-left text-sm font-medium transition-all outline-none hover:no-underline focus-visible:ring-[3px] disabled:pointer-events-none disabled:opacity-50",
        )}
      >
        <ChevronDownIcon 
          className={cn(
            "text-muted-foreground pointer-events-none size-4 shrink-0 self-center transition-transform duration-200",
            isOpen && "rotate-180"
          )} 
        />
        {children}
      </button>
    </AccordionPrimitive.Header>
  );
}