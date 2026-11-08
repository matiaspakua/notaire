"use client";

import type { ReactNode } from "react";
import type { LucideIcon } from "lucide-react";
import { FileDown } from "lucide-react";
import { Button } from "@/components/ui/button";
import { cn } from "@/lib/utils";

export interface ReportCardProps {
  testId: string;
  icon: LucideIcon;
  title: string;
  description: string;
  downloadLabel: string;
  downloadTestId: string;
  canDownload: boolean;
  onDownload: () => void;
  children: ReactNode;
  className?: string;
}

/**
 * PDF / report download card (#1953): one job, token chrome, full-width secondary CTA.
 */
export function ReportCard({
  testId,
  icon: Icon,
  title,
  description,
  downloadLabel,
  downloadTestId,
  canDownload,
  onDownload,
  children,
  className,
}: ReportCardProps) {
  return (
    <div
      data-testid={testId}
      className={cn(
        "flex flex-col gap-4 rounded-[28px] border border-border/40 bg-white p-6 apple-shadow",
        className,
      )}
    >
      <div className="flex flex-col gap-1">
        <div className="flex items-center gap-2">
          <Icon className="h-5 w-5 shrink-0 text-primary" aria-hidden />
          <h3 className="m-0 text-base font-semibold tracking-tight text-foreground">{title}</h3>
        </div>
        <p className="m-0 text-sm font-medium text-muted-foreground">{description}</p>
      </div>
      {children}
      <Button
        className="w-full"
        variant="secondary"
        disabled={!canDownload}
        onClick={onDownload}
        data-testid={downloadTestId}
      >
        <FileDown className="h-4 w-4 mr-2" />
        {downloadLabel}
      </Button>
    </div>
  );
}
