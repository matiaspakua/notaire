"use client";

/**
 * Formatters for the active locale (#1355). Screens use this instead of the
 * es-AR helpers, so the English UI shows "08/07/2026" and "ARS 75,000.00"
 * while Spanish keeps "7/8/2026" and "$ 75.000,00".
 */
import { useMemo } from "react";
import { useLocale } from "next-intl";
import { formatCalendarDate, formatInstant } from "@/lib/dates";
import { formatMoney, formatNumber, intlLocale } from "@/lib/format";

export interface Formatters {
  /** The Intl locale in use (es-AR or en-GB). */
  intl: string;
  /** A date-only business field, day first, same calendar day in any browser zone (#1339). */
  date: (value?: string | null) => string;
  /** A timestamp as date and time in the business time zone. */
  instant: (value?: string | null) => string;
  /** An ARS amount. */
  currency: (amount?: number | null) => string;
  number: (value?: number | null) => string;
  /** Today-style long date ("domingo, 11 de octubre de 2026" / "Sunday 11 October 2026"). */
  longDate: (value: Date) => string;
}

export function useFormat(): Formatters {
  const intl = intlLocale(useLocale());
  return useMemo(
    () => ({
      intl,
      date: (value) => formatCalendarDate(value, intl),
      instant: (value) => formatInstant(value, intl),
      currency: (amount) => formatMoney(amount, intl),
      number: (value) => formatNumber(value, intl),
      longDate: (value) =>
        value.toLocaleDateString(intl, { weekday: "long", year: "numeric", month: "long", day: "numeric" }),
    }),
    [intl],
  );
}
