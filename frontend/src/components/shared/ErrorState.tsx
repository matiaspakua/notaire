"use client";

import type { ReactNode } from "react";
import { AlertCircle } from "lucide-react";
import { cn } from "@/lib/utils";

export interface ErrorStateProps {
  title: string;
  description?: string;
  action?: ReactNode;
  className?: string;
}

/**
 * Failure invitation with optional retry (#1947).
 */
export function ErrorState({ title, description, action, className }: ErrorStateProps) {
  return (
    <div
      data-testid="error-state"
      role="alert"
      className={cn(
        "flex flex-col items-center justify-center gap-3 px-6 py-12 text-center animate-in fade-in-0 duration-base ease-standard",
        className,
      )}
    >
      <div className="rounded-2xl bg-destructive/10 p-4 text-destructive">
        <AlertCircle className="h-8 w-8" aria-hidden />
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
