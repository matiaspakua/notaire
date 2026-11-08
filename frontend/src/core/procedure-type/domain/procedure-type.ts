/**
 * Procedure-type catalog domain model (#1796 / ADR-030).
 * Pure TypeScript — no React, Next, or HTTP.
 */

export interface ProcedureType {
  id: number;
  name: string;
  notes?: string | null;
  isArchived?: boolean;
  isRegistered?: boolean;
  associatesProperties?: boolean;
  enabled?: boolean;
  workflowDefinitionId?: number | null;
  workflowDefinitionName?: string | null;
}
