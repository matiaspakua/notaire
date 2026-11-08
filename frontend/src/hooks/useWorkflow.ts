/**
 * React Query hooks for workflow (CU70, CU71 — manage status workflows).
 * Hexagon wiring: #1792 / ADR-030.
 */
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { workflowUseCases } from "@/composition/workflow";
import type { WorkflowDefinition, WorkflowNode, WorkflowTransition } from "@/types";

export const workflowKeys = {
  allDefinitions: ["workflow-definition"] as const,
  definition: (id: number) => ["workflow-definition", id] as const,
  nodes: (workflowId: number) => ["workflow-node", workflowId] as const,
  transitions: (workflowId: number) => ["workflow-transition", workflowId] as const,
};

export function useWorkflowDefinitions() {
  return useQuery({
    queryKey: workflowKeys.allDefinitions,
    queryFn: () => workflowUseCases.listDefinitions() as Promise<WorkflowDefinition[]>,
  });
}

export function useWorkflowNodes(workflowId: number | undefined) {
  return useQuery({
    queryKey: workflowKeys.nodes(workflowId ?? 0),
    queryFn: () => workflowUseCases.listNodesByWorkflow(workflowId!) as Promise<WorkflowNode[]>,
    enabled: !!workflowId,
  });
}

export function useWorkflowTransitions(workflowId: number | undefined) {
  return useQuery({
    queryKey: workflowKeys.transitions(workflowId ?? 0),
    queryFn: () =>
      workflowUseCases.listTransitionsByWorkflow(workflowId!) as Promise<WorkflowTransition[]>,
    enabled: !!workflowId,
  });
}

export function useCreateWorkflowDefinition() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: Partial<WorkflowDefinition>) => workflowUseCases.createDefinition(data),
    onSuccess: () => qc.invalidateQueries({ queryKey: workflowKeys.allDefinitions }),
  });
}

export function useUpdateWorkflowDefinition() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: Partial<WorkflowDefinition> }) =>
      workflowUseCases.updateDefinition(id, data),
    onSuccess: () => qc.invalidateQueries({ queryKey: workflowKeys.allDefinitions }),
  });
}

export function useDeleteWorkflowDefinition() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => workflowUseCases.deleteDefinition(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: workflowKeys.allDefinitions }),
  });
}

export function useCreateWorkflowNode() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: Partial<WorkflowNode>) => workflowUseCases.createNode(data),
    onSuccess: (_, vars) =>
      qc.invalidateQueries({ queryKey: workflowKeys.nodes(vars.workflowDefinitionId ?? 0) }),
  });
}

export function useUpdateWorkflowNode() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: Partial<WorkflowNode> }) =>
      workflowUseCases.updateNode(id, data),
    onSuccess: (_, vars) =>
      qc.invalidateQueries({ queryKey: workflowKeys.nodes(vars.data.workflowDefinitionId ?? 0) }),
  });
}

export function useDeleteWorkflowNode() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id }: { id: number; workflowId: number }) => workflowUseCases.deleteNode(id),
    onSuccess: (_, vars) => qc.invalidateQueries({ queryKey: workflowKeys.nodes(vars.workflowId) }),
  });
}

export function useCreateWorkflowTransition() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: Partial<WorkflowTransition>) => workflowUseCases.createTransition(data),
    onSuccess: (_, vars) =>
      qc.invalidateQueries({ queryKey: workflowKeys.transitions(vars.workflowDefinitionId ?? 0) }),
  });
}

/** #1576: edit a transition's description (PUT needs origin and destination too). */
export function useUpdateWorkflowTransition() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: Partial<WorkflowTransition> }) =>
      workflowUseCases.updateTransition(id, data),
    onSuccess: (_, vars) =>
      qc.invalidateQueries({ queryKey: workflowKeys.transitions(vars.data.workflowDefinitionId ?? 0) }),
  });
}

export function useDeleteWorkflowTransition() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id }: { id: number; workflowId: number }) =>
      workflowUseCases.deleteTransition(id),
    onSuccess: (_, vars) =>
      qc.invalidateQueries({ queryKey: workflowKeys.transitions(vars.workflowId) }),
  });
}
