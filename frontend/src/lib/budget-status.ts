/**
 * Budget status vocabulary (#1346; Owner default 2026-10-10: the stored codes).
 * The backend stores and validates these codes (V44, BudgetStatus); the UI shows
 * `presupuestos.status.<CODE>` and never a Spanish literal.
 */
export const BUDGET_STATUSES = ["BORRADOR", "PENDIENTE", "APROBADO", "RECHAZADO", "FACTURADO"] as const;

export type BudgetStatusCode = (typeof BUDGET_STATUSES)[number];

export const DEFAULT_BUDGET_STATUS: BudgetStatusCode = "BORRADOR";

/** The code for a stored value in any letter case or spacing, or null when it is not in the vocabulary. */
export function normalizeBudgetStatus(value: string | null | undefined): BudgetStatusCode | null {
  const code = value?.trim().toUpperCase();
  return (BUDGET_STATUSES as readonly string[]).includes(code ?? "") ? (code as BudgetStatusCode) : null;
}

/** i18n key, relative to the `presupuestos` namespace, of a code's label. */
export function budgetStatusLabelKey(code: BudgetStatusCode): `status.${BudgetStatusCode}` {
  return `status.${code}`;
}
