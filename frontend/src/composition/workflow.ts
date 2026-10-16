/**
 * Composition root for workflows admin (#1792 / ADR-030).
 */
import { managementWorkflowHttpGateway } from "@/adapters/http/workflow/management-workflow-http-gateway";
import { workflowHttpGateway } from "@/adapters/http/workflow/workflow-http-gateway";
import {
  createManagementWorkflowUseCases,
  createWorkflowUseCases,
} from "@/core/workflow/application/use-cases";

export const workflowUseCases = createWorkflowUseCases(workflowHttpGateway);
export const managementWorkflowUseCases = createManagementWorkflowUseCases(
  managementWorkflowHttpGateway,
);
