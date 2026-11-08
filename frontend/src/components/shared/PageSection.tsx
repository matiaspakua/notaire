"use client";

import type { ReactNode } from "react";
import { cn } from "@/lib/utils";

export interface PageSectionProps {
  title: string;
  description?: string;
  actions?: ReactNode;
  children: ReactNode;
  className?: string;
}

/**
 * One-job section with token rhythm (#1942).
 */
export function PageSection({
  title,
  description,
  actions,
  children,
  className,
}: PageSectionProps) {
  return (
    <section data-testid="page-section" className={cn("space-y-6", className)}>
      <div className="flex flex-wrap items-end justify-between gap-3 px-0.5">
        <div className="min-w-0 space-y-1">
          <h2 className="text-2xl font-semibold tracking-tight text-foreground">{title}</h2>
          {description ? (
            <p className="text-sm font-medium text-muted-foreground">{description}</p>
          ) : null}
        </div>
        {actions ? <div className="flex flex-wrap items-center gap-3">{actions}</div> : null}
      </div>
      {children}
    </section>
  );
}
