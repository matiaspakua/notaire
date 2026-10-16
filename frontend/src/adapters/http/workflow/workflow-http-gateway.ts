/**
 * HTTP adapter for workflow administration (#1792 / ADR-030).
 * Only place in this surface that may import api-client for definitions/nodes/transitions.
 */
import { apiDelete, apiGet, apiPost, apiPut } from "@/lib/api-client";
import type { WorkflowGateway } from "@/core/workflow/application/ports";
import type {
  WorkflowDefinition,
  WorkflowNode,
  WorkflowTransition,
  WorkflowValidateResult,
} from "@/core/workflow/domain/workflow";

export const workflowHttpGateway: WorkflowGateway = {
  listDefinitions: () => apiGet<WorkflowDefinition[]>("/workflow-definition"),
  createDefinition: (data) => apiPost<WorkflowDefinition>("/workflow-definition", data),
  updateDefinition: async (id, data) => {
    await apiPut<void>(`/workflow-definition/${id}`, data);
  },
  deleteDefinition: (id) => apiDelete(`/workflow-definition/${id}`),
  validateDefinition: (id) =>
    apiPost<WorkflowValidateResult>(`/workflow-definition/${id}/validate`, {}),

  listNodesByWorkflow: (workflowId) =>
    apiGet<WorkflowNode[]>(`/workflow-node/by-workflow/${workflowId}`),
  createNode: (data) => apiPost<WorkflowNode>("/workflow-node", data),
  updateNode: async (id, data) => {
    await apiPut<void>(`/workflow-node/${id}`, data);
  },
  deleteNode: (id) => apiDelete(`/workflow-node/${id}`),

  listTransitionsByWorkflow: (workflowId) =>
    apiGet<WorkflowTransition[]>(`/workflow-transition/by-workflow/${workflowId}`),
  createTransition: (data) => apiPost<WorkflowTransition>("/workflow-transition", data),
  updateTransition: (id, data) => apiPut<WorkflowTransition>(`/workflow-transition/${id}`, data),
  deleteTransition: (id) => apiDelete(`/workflow-transition/${id}`),
};
