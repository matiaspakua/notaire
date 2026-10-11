"use client";

import { useTranslations } from "next-intl";
import { useFormat } from "@/hooks/useFormat";

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
  const fmt = useFormat();
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
          <span aria-hidden="true" className="inline-block h-12 w-24 align-middle rounded-xl bg-secondary motion-safe:animate-pulse" />
          <span className="sr-only">{tc("loading")}</span>
        </>
      ) : value === undefined || isError ? (
        "—"
      ) : (
        fmt.number(value)
      )}
    </p>
  );
}
