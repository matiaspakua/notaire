/**
 * Copy domain (#1845 / ADR-030).
 */
export interface Copy {
  [key: string]: unknown;
}

export type CopyInput = Partial<Copy>;
