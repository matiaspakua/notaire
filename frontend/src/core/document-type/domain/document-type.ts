/**
 * Document-type catalog domain model (#1777 / ADR-030).
 * Pure TypeScript — no React, Next, or HTTP.
 */

export interface DocumentType {
  id: number;
  name: string;
  expires?: boolean;
  dueDays?: number | null;
  deliveredBy?: string | null;
  enabled?: boolean;
  returned?: boolean;
}
