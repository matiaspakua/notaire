/**
 * Management-status catalog domain model (#1769 / ADR-030).
 * Pure TypeScript — no React, Next, or HTTP.
 */

export interface ManagementStatus {
  id: number;
  name: string;
  notes?: string | null;
  version?: number;
}
