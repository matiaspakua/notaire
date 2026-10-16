/**
 * Item domain (#1845 / ADR-030).
 */
export interface Item {
  idItem?: number;
  name?: string;
  value?: number;
  percentage?: number;
  notes?: string;
  type?: string;
  reason?: string;
  fixedConcept?: boolean;
  fkIdBudget?: { idBudget?: number };
  version?: number;
  [key: string]: unknown;
}

export type ItemInput = Partial<Item>;
