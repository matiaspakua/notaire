"use client";

import { useLocale, useTranslations } from "next-intl";

interface StatValueProps {
  /** `totalElements` of a `size=1` page request; undefined until it arrives. */
  value: number | undefined;
  isLoading: boolean;
  isError?: boolean;
}

/**
 * Dashboard stat number (#1358): a skeleton while the count loads instead of a
 * misleading 0, a dash when it failed, and the exact total in the UI locale.
 */
export function StatValue({ value, isLoading, isError }: StatValueProps) {
  const locale = useLocale();
  const tc = useTranslations("common");
  const busy = isLoading && value === undefined;
  return (
    <p
      className="text-5xl font-semibold tracking-tighter text-foreground"
      data-testid="stat-value"
      aria-busy={busy}
    >
      {busy ? (
        <>
          <span aria-hidden="true" className="inline-block h-12 w-24 align-middle rounded-xl bg-secondary animate-pulse" />
          <span className="sr-only">{tc("loading")}</span>
        </>
      ) : value === undefined || isError ? (
        "—"
      ) : (
        new Intl.NumberFormat(locale === "en" ? "en-US" : "es-AR").format(value)
      )}
    </p>
  );
}
