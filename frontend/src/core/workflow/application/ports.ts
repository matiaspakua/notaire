/**
 * Outbound ports for workflow administration (#1792 / ADR-030).
 */
import type {
  LatestTracedManagement,
  ManagementPage,
  ManagementWorkflowTrace,
  WorkflowDefinition,
  WorkflowNode,
  WorkflowTransition,
  WorkflowValidateResult,
} from "../domain/workflow";

export interface WorkflowGateway {
  listDefinitions(): Promise<WorkflowDefinition[]>;
  createDefinition(data: Partial<WorkflowDefinition>): Promise<WorkflowDefinition>;
  updateDefinition(id: number, data: Partial<WorkflowDefinition>): Promise<void>;
  deleteDefinition(id: number): Promise<void>;
  validateDefinition(id: number): Promise<WorkflowValidateResult>;

  listNodesByWorkflow(workflowId: number): Promise<WorkflowNode[]>;
  createNode(data: Partial<WorkflowNode>): Promise<WorkflowNode>;
  updateNode(id: number, data: Partial<WorkflowNode>): Promise<void>;
  deleteNode(id: number): Promise<void>;

  listTransitionsByWorkflow(workflowId: number): Promise<WorkflowTransition[]>;
  createTransition(data: Partial<WorkflowTransition>): Promise<WorkflowTransition>;
  updateTransition(id: number, data: Partial<WorkflowTransition>): Promise<WorkflowTransition>;
  deleteTransition(id: number): Promise<void>;
}

export interface ManagementWorkflowGateway {
  getTrace(managementId: number): Promise<ManagementWorkflowTrace>;
  listManagementIds(page: number, size: number, sort: string): Promise<ManagementPage<{ idManagement?: number }>>;
  findLatestTraced(candidateLimit: number, batchSize: number, sort: string): Promise<LatestTracedManagement | null>;
}
