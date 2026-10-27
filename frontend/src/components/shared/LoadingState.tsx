"use client";

import { useTranslations } from "next-intl";
import { Skeleton } from "@/components/ui/skeleton";
import { cn } from "@/lib/utils";

export interface LoadingStateProps {
  className?: string;
  /** Number of skeleton rows (1–6). */
  rows?: number;
  label?: string;
}

/**
 * Busy panel for non-table async surfaces (#1947).
 */
export function LoadingState({ className, rows = 3, label }: LoadingStateProps) {
  const tc = useTranslations("common");
  const text = label ?? tc("loading");
  const count = Math.min(Math.max(rows, 1), 6);

  return (
    <div
      data-testid="loading-state"
      role="status"
      aria-busy="true"
      className={cn(
        "flex flex-col gap-3 rounded-[24px] border border-border/40 bg-white px-6 py-8 apple-shadow",
        className,
      )}
    >
      <span className="sr-only">{text}</span>
      {Array.from({ length: count }).map((_, i) => (
        <Skeleton key={i} className="h-4 w-full max-w-xl rounded-md" />
      ))}
    </div>
  );
}
