/**
 * Substitution domain (#1845 / ADR-030).
 */
export interface Substitution {
  [key: string]: unknown;
}

export type SubstitutionInput = Partial<Substitution>;
