"use client";

import type { ReactNode } from "react";
import { Card, CardContent } from "@/components/ui/card";
import { cn } from "@/lib/utils";

export interface MetricCardProps {
  label: string;
  icon: ReactNode;
  children: ReactNode;
  className?: string;
  iconWellClassName?: string;
}

/**
 * Home / overview metric tile (#1953): label + value slot + tinted icon well.
 */
export function MetricCard({
  label,
  icon,
  children,
  className,
  iconWellClassName = "bg-primary/10 text-primary",
}: MetricCardProps) {
  return (
    <Card
      data-testid="metric-card"
      className={cn("bg-white border-none apple-shadow rounded-[28px] overflow-hidden", className)}
    >
      <CardContent className="p-8 flex items-center justify-between">
        <div className="space-y-1 min-w-0">
          <p className="text-[13px] font-semibold tracking-tight text-muted-foreground">{label}</p>
          {children}
        </div>
        <div className={cn("p-5 rounded-3xl shrink-0", iconWellClassName)}>{icon}</div>
      </CardContent>
    </Card>
  );
}
