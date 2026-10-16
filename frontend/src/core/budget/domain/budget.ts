/**
 * Budget domain model (#1827 / ADR-030).
 * Pure TypeScript — no React, Next, or HTTP.
 * Field names align with BudgetResponse wire shape to limit mapping churn.
 */

export interface BudgetPersonRef {
  personId?: number;
  name?: string;
  lastName?: string;
}

export interface Budget {
  idBudget?: number;
  number?: number;
  heading?: string;
  status?: string;
  propertyAmount?: number;
  notes?: string;
  date?: string;
  person?: BudgetPersonRef;
  version?: number;
}

export interface BudgetSummary {
  idBudget?: number;
  idManagement?: number;
  managementHeading?: string;
  numberBudget?: number;
  numberManagement?: number;
  pendingBalance?: number;
  total?: number;
  payments?: unknown[];
}

/** Minimal item row returned by template/catalog load endpoints. */
export interface BudgetItem {
  idItem?: number;
  name?: string;
  value?: number;
  percentage?: number;
  notes?: string;
  type?: string;
  reason?: string;
  fixedConcept?: boolean;
  budgetId?: number;
  version?: number;
}
