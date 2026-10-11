/**
 * Locale-aware display formatting (#1355, RNF-05, ADR-015). One map from the
 * app locale to the Intl locale: es → es-AR; en → en-GB, so English keeps the
 * day first (Owner decision for an Argentine notary office). Money is always
 * ARS. React code formats through `useFormat()`; these are its building blocks.
 */
export const INTL_LOCALES = { es: "es-AR", en: "en-GB" } as const;

/** The Intl locale for an app locale; anything unknown falls back to es-AR. */
export function intlLocale(locale?: string | null): string {
  return locale === "en" ? INTL_LOCALES.en : INTL_LOCALES.es;
}

/** An amount in ARS ("$ 75.000,00" in es-AR, "ARS 75,000.00" in en-GB); "—" when empty. */
export function formatMoney(amount: number | null | undefined, intl: string = INTL_LOCALES.es): string {
  if (amount == null || Number.isNaN(amount)) return "—";
  return new Intl.NumberFormat(intl, { style: "currency", currency: "ARS" }).format(amount);
}

/** A plain number with the locale's grouping; "—" when empty. */
export function formatNumber(value: number | null | undefined, intl: string = INTL_LOCALES.es): string {
  if (value == null || Number.isNaN(value)) return "—";
  return new Intl.NumberFormat(intl).format(value);
}
