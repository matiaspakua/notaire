"use client";

/**
 * Shared status badge (#1366): tone + icon + text, so a status never relies on
 * color alone (WCAG 1.4.1). The icon is decorative; the text carries the
 * meaning. Unknown values render neutral with the stored text, or a dash.
 */
import { useTranslations } from "next-intl";
import { CheckCircle2, CircleDashed, CircleDot, Clock, XCircle, type LucideIcon } from "lucide-react";
import { Badge, type BadgeProps } from "@/components/ui/badge";
import { resolveStatus, type StatusDomain, type StatusTone } from "@/lib/status-maps";
import { cn } from "@/lib/utils";

const TONE_VARIANT: Record<StatusTone, NonNullable<BadgeProps["variant"]>> = {
  neutral: "secondary",
  info: "info",
  success: "success",
  warning: "warning",
  danger: "destructive",
};

const TONE_ICON: Record<StatusTone, LucideIcon> = {
  neutral: CircleDashed,
  info: CircleDot,
  success: CheckCircle2,
  warning: Clock,
  danger: XCircle,
};

export interface StatusBadgeProps {
  domain: StatusDomain;
  value: string | boolean | null | undefined;
  className?: string;
}

export function StatusBadge({ domain, value, className }: StatusBadgeProps) {
  const t = useTranslations();
  const { tone, labelKey, raw } = resolveStatus(domain, value);
  const label = labelKey ? t(labelKey) : raw || "—";
  const Icon = TONE_ICON[tone];
  return (
    <Badge
      variant={TONE_VARIANT[tone]}
      className={cn("gap-1 whitespace-nowrap", className)}
      data-testid="status-badge"
      data-domain={domain}
      data-tone={tone}
    >
      <Icon className="h-3.5 w-3.5 shrink-0" aria-hidden="true" />
      <span>{label}</span>
    </Badge>
  );
}
