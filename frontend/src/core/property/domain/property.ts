/**
 * Property domain (#1845 / ADR-030).
 */
export interface Property {
  [key: string]: unknown;
}

export type PropertyInput = Partial<Property>;
