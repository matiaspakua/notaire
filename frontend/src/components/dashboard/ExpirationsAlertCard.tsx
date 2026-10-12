"use client";

import Link from "next/link";
import { AlertTriangle, ArrowRight } from "lucide-react";
import { useTranslations } from "next-intl";
import { UPCOMING_EXPIRATIONS_DEFAULT_DAYS, useUpcomingExpirations } from "@/hooks/useUpcomingExpirations";
import { useFormat } from "@/hooks/useFormat";

/**
 * CU42 (#1556): home-dashboard alert for documents that expire soon. Same endpoint and
 * window as /dashboard/upcoming-expirations; renders nothing when there are none or on error.
 */
export function ExpirationsAlertCard() {
  const t = useTranslations("dashboard");
  const fmt = useFormat();
  const days = UPCOMING_EXPIRATIONS_DEFAULT_DAYS;
  const { data, isError } = useUpcomingExpirations(days);
  if (isError || !data || data.length === 0) return null;
  const nearest = [...data].sort((a, b) => a.daysRemaining - b.daysRemaining).slice(0, 3);
  return (
    <section
      data-testid="expirations-alert"
      role="status"
      aria-labelledby="expirations-alert-title"
      className="rounded-[20px] border border-warning/30 bg-warning/10 p-5 md:p-6"
    >
      <div className="flex flex-col gap-4 md:flex-row md:items-start md:justify-between">
        <div className="flex items-start gap-3 min-w-0">
          <AlertTriangle className="h-6 w-6 shrink-0 text-warning" aria-hidden="true" />
          <div className="min-w-0 space-y-2">
            <h2 id="expirations-alert-title" className="text-lg font-semibold text-foreground">
              {t("vencimientos.titulo", { count: data.length, days })}
            </h2>
            <ul className="space-y-1 text-sm text-foreground">
              {nearest.map((v) => (
                <li key={v.idSubmittedDocument} data-testid="expirations-alert-item" className="break-words">
                  <span className="font-medium">{v.documentName ?? "—"}</span>
                  {v.managementHeading ? <span className="text-muted-foreground"> · {v.managementHeading}</span> : null}
                  <span className="text-muted-foreground"> · {fmt.date(v.dateDue)}</span>
                </li>
              ))}
            </ul>
          </div>
        </div>
        <Link
          href="/dashboard/upcoming-expirations"
          data-testid="expirations-alert-link"
          className="inline-flex shrink-0 items-center gap-1 self-start rounded-full border border-black/5 bg-white px-3 py-2 text-sm font-semibold text-primary shadow-sm hover:underline focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
        >
          {t("vencimientos.verTodos")}
          <ArrowRight className="h-4 w-4" aria-hidden="true" />
        </Link>
      </div>
    </section>
  );
}
