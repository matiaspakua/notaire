"use client";

import type { ReactNode } from "react";
import { Inbox } from "lucide-react";
import { cn } from "@/lib/utils";

export interface EmptyStateProps {
  title: string;
  description?: string;
  action?: ReactNode;
  icon?: ReactNode;
  className?: string;
}

/**
 * Empty invitation with one clear next action (#1942).
 */
export function EmptyState({
  title,
  description,
  action,
  icon,
  className,
}: EmptyStateProps) {
  return (
    <div
      data-testid="empty-state"
      role="status"
      className={cn(
        "flex flex-col items-center justify-center gap-3 px-6 py-12 text-center animate-in fade-in-0 duration-base ease-standard",
        className,
      )}
    >
      <div className="rounded-2xl bg-secondary p-4 text-muted-foreground">
        {icon ?? <Inbox className="h-8 w-8 opacity-40" aria-hidden />}
      </div>
      <div className="space-y-1">
        <p className="text-lg font-semibold tracking-tight text-foreground">{title}</p>
        {description ? (
          <p className="max-w-sm text-sm font-medium text-muted-foreground">{description}</p>
        ) : null}
      </div>
      {action ? <div className="pt-1">{action}</div> : null}
    </div>
  );
}
