/**
 * Folio-type catalog domain model (#1785 / ADR-030).
 * Pure TypeScript — no React, Next, or HTTP.
 */

export interface FolioType {
  id: number;
  name: string;
  isAuxiliary?: boolean;
  notes?: string;
  enabled?: boolean;
  /** Optimistic-lock version from the list; full update sends it back (#655). */
  version?: number;
}
