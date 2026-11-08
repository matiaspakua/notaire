"use client";

import type { ReactNode } from "react";
import { cn } from "@/lib/utils";

export interface PageHeaderProps {
  title: string;
  description?: string;
  actions?: ReactNode;
  className?: string;
  /** Heading level; sticky chrome uses h1, nested intros may use h2. */
  as?: "h1" | "h2";
  /** `display` for home heroes; `page` for sticky chrome and list pages. */
  size?: "page" | "display";
}

/**
 * In-content page title + optional support line + actions (#1942).
 * Token typography; sentence case — never decorative ALL-CAPS.
 */
export function PageHeader({
  title,
  description,
  actions,
  className,
  as: Tag = "h1",
  size = "page",
}: PageHeaderProps) {
  return (
    <div
      data-testid="page-header"
      className={cn("flex flex-wrap items-end justify-between gap-4", className)}
    >
      <div className="min-w-0 space-y-1">
        <Tag
          className={cn(
            "font-semibold tracking-tight text-foreground",
            size === "display" ? "text-4xl" : "text-2xl md:text-[1.75rem]",
          )}
        >
          {title}
        </Tag>
        {description ? (
          <p
            className={cn(
              "max-w-2xl font-medium text-muted-foreground",
              size === "display" ? "text-xl" : "text-sm md:text-base",
            )}
          >
            {description}
          </p>
        ) : null}
      </div>
      {actions ? (
        <div data-testid="page-header-actions" className="flex flex-wrap items-center gap-3">
          {actions}
        </div>
      ) : null}
    </div>
  );
}
