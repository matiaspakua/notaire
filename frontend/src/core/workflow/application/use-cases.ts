/**
 * Workflow administration use cases (#1792 / ADR-030).
 * No React, Next, or api-client — only the gateway ports.
 */
import type { ManagementWorkflowGateway, WorkflowGateway } from "./ports";
import type { WorkflowDefinition, WorkflowNode, WorkflowTransition } from "../domain/workflow";

export function createWorkflowUseCases(gateway: WorkflowGateway) {
  return {
    listDefinitions: () => gateway.listDefinitions(),
    createDefinition: (data: Partial<WorkflowDefinition>) => gateway.createDefinition(data),
    updateDefinition: (id: number, data: Partial<WorkflowDefinition>) => gateway.updateDefinition(id, data),
    deleteDefinition: (id: number) => gateway.deleteDefinition(id),
    validateDefinition: (id: number) => gateway.validateDefinition(id),

    listNodesByWorkflow: (workflowId: number) => gateway.listNodesByWorkflow(workflowId),
    createNode: (data: Partial<WorkflowNode>) => gateway.createNode(data),
    updateNode: (id: number, data: Partial<WorkflowNode>) => gateway.updateNode(id, data),
    deleteNode: (id: number) => gateway.deleteNode(id),

    listTransitionsByWorkflow: (workflowId: number) => gateway.listTransitionsByWorkflow(workflowId),
    createTransition: (data: Partial<WorkflowTransition>) => gateway.createTransition(data),
    updateTransition: (id: number, data: Partial<WorkflowTransition>) =>
      gateway.updateTransition(id, data),
    deleteTransition: (id: number) => gateway.deleteTransition(id),
  };
}

export type WorkflowUseCases = ReturnType<typeof createWorkflowUseCases>;

export function createManagementWorkflowUseCases(gateway: ManagementWorkflowGateway) {
  return {
    getTrace: (managementId: number) => gateway.getTrace(managementId),
    findLatestTraced: (candidateLimit: number, batchSize: number, sort: string) =>
      gateway.findLatestTraced(candidateLimit, batchSize, sort),
  };
}

export type ManagementWorkflowUseCases = ReturnType<typeof createManagementWorkflowUseCases>;
