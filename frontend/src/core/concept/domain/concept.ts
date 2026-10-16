/**
 * Concept catalog domain model (#1781 / ADR-030).
 * Pure TypeScript — no React, Next, or HTTP.
 */

export interface Concept {
  id: number;
  name: string;
  value?: number;
  percentage?: number;
  fixed?: boolean;
  enabled?: boolean;
  version?: number;
}
