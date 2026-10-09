"use client";

import { ChevronLeft, ChevronRight, ChevronsLeft, ChevronsRight } from "lucide-react";
import { useTranslations } from "next-intl";
import { Button } from "@/components/ui/button";

export const PAGE_SIZE_OPTIONS = [20, 50, 100] as const;

export interface PaginationProps {
  /** Zero-based page index, as Spring Data returns it. */
  page: number;
  size: number;
  totalElements: number;
  onPageChange: (page: number) => void;
  /** When given, a rows-per-page selector is shown. */
  onSizeChange?: (size: number) => void;
}

/**
 * Footer for server-side paged lists (#1340): range and total, first /
 * previous / next / last, and rows per page. Rendered as a labelled
 * navigation landmark.
 */
export function Pagination({ page, size, totalElements, onPageChange, onSizeChange }: PaginationProps) {
  const t = useTranslations("common.pagination");
  const pages = Math.max(1, Math.ceil(totalElements / Math.max(size, 1)));
  const current = Math.min(Math.max(page, 0), pages - 1);
  const from = totalElements === 0 ? 0 : current * size + 1;
  const to = Math.min(totalElements, (current + 1) * size);
  const atFirst = current <= 0;
  const atLast = current >= pages - 1 || totalElements === 0;

  return (
    <nav
      aria-label={t("label")}
      className="flex flex-wrap items-center justify-between gap-3 px-6 py-4 border-t border-border/40 text-sm text-muted-foreground"
    >
      <p data-testid="pagination-status" aria-live="polite">
        {t("status", { from, to, total: totalElements })}
      </p>
      <div className="flex items-center gap-3">
        {onSizeChange && (
          <label className="flex items-center gap-2">
            <span>{t("rowsPerPage")}</span>
            <select
              className="h-9 rounded-lg border border-input bg-background px-2 text-sm text-foreground focus-visible:ring-2 focus-visible:ring-ring"
              value={size}
              onChange={(e) => onSizeChange(Number(e.target.value))}
            >
              {PAGE_SIZE_OPTIONS.map((option) => (
                <option key={option} value={option}>
                  {option}
                </option>
              ))}
            </select>
          </label>
        )}
        <span data-testid="pagination-page">{t("page", { page: current + 1, pages })}</span>
        <div className="flex items-center gap-1">
          <Button size="sm" variant="ghost" aria-label={t("first")} disabled={atFirst} onClick={() => onPageChange(0)}>
            <ChevronsLeft className="h-4 w-4" aria-hidden="true" />
          </Button>
          <Button size="sm" variant="ghost" aria-label={t("previous")} disabled={atFirst} onClick={() => onPageChange(current - 1)}>
            <ChevronLeft className="h-4 w-4" aria-hidden="true" />
          </Button>
          <Button size="sm" variant="ghost" aria-label={t("next")} disabled={atLast} onClick={() => onPageChange(current + 1)}>
            <ChevronRight className="h-4 w-4" aria-hidden="true" />
          </Button>
          <Button size="sm" variant="ghost" aria-label={t("last")} disabled={atLast} onClick={() => onPageChange(pages - 1)}>
            <ChevronsRight className="h-4 w-4" aria-hidden="true" />
          </Button>
        </div>
      </div>
    </nav>
  );
}
