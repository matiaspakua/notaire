/**
 * Workflow administration domain (#1792 / ADR-030).
 */

export type WorkflowNodeType = "INITIAL" | "INTERMEDIATE" | "FINAL";

export interface WorkflowDefinition {
  id?: number;
  name?: string;
  description?: string;
  active?: boolean;
  version?: number;
}

export interface WorkflowNode {
  id?: number;
  workflowDefinitionId?: number;
  statusManagementId?: number;
  statusManagementName?: string;
  type?: WorkflowNodeType;
  positionX?: number;
  positionY?: number;
  version?: number;
}

export interface WorkflowTransition {
  id?: number;
  workflowDefinitionId?: number;
  originNodeId?: number;
  destinationNodeId?: number;
  condition?: string;
  description?: string;
  version?: number;
}

export interface WorkflowValidateResult {
  valid: boolean;
  errors: string[];
}

/** Aggregated management workflow trace (dashboard). */
export interface ManagementWorkflowTrace {
  managementId: number;
  number?: number;
  heading?: string;
  dateStart?: string;
  currentStatus?: string;
  workflowDefinition?: WorkflowDefinition;
  nodes: WorkflowNode[];
  transitions: WorkflowTransition[];
  history: unknown[];
  nodeStatuses: Record<number, string>;
  testimonyMovements?: unknown[];
}

export interface LatestTracedManagement {
  managementId: number;
  trace: ManagementWorkflowTrace;
}

export interface ManagementPage<T> {
  content: T[];
}
