/**
 * React Query hooks for Workflow (CU70, CU71 — Gestionar workflows de estados).
 * Endpoints: /api/v1/workflow-definition, /api/v1/workflow-node, /api/v1/workflow-transition
 */
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { apiGet, apiPost, apiPut, apiDelete } from "@/lib/api-client";
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
    queryFn: () => apiGet<WorkflowDefinition[]>("/workflow-definition"),
  });
}

export function useWorkflowNodes(workflowId: number | undefined) {
  return useQuery({
    queryKey: workflowKeys.nodes(workflowId ?? 0),
    queryFn: () => apiGet<WorkflowNode[]>(`/workflow-node/by-workflow/${workflowId}`),
    enabled: !!workflowId,
  });
}

export function useWorkflowTransitions(workflowId: number | undefined) {
  return useQuery({
    queryKey: workflowKeys.transitions(workflowId ?? 0),
    queryFn: () => apiGet<WorkflowTransition[]>(`/workflow-transition/by-workflow/${workflowId}`),
    enabled: !!workflowId,
  });
}

export function useCreateWorkflowDefinition() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: Partial<WorkflowDefinition>) =>
      apiPost<WorkflowDefinition>("/workflow-definition", data),
    onSuccess: () => qc.invalidateQueries({ queryKey: workflowKeys.allDefinitions }),
  });
}

export function useUpdateWorkflowDefinition() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: Partial<WorkflowDefinition> }) =>
      apiPut<void>(`/workflow-definition/${id}`, data),
    onSuccess: () => qc.invalidateQueries({ queryKey: workflowKeys.allDefinitions }),
  });
}

export function useDeleteWorkflowDefinition() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => apiDelete(`/workflow-definition/${id}`),
    onSuccess: () => qc.invalidateQueries({ queryKey: workflowKeys.allDefinitions }),
  });
}

export function useCreateWorkflowNode() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: Partial<WorkflowNode>) =>
      apiPost<WorkflowNode>("/workflow-node", data),
    onSuccess: (_, vars) =>
      qc.invalidateQueries({ queryKey: workflowKeys.nodes(vars.workflowDefinitionId ?? 0) }),
  });
}

export function useUpdateWorkflowNode() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: Partial<WorkflowNode> }) =>
      apiPut<void>(`/workflow-node/${id}`, data),
    onSuccess: (_, vars) =>
      qc.invalidateQueries({ queryKey: workflowKeys.nodes(vars.data.workflowDefinitionId ?? 0) }),
  });
}

export function useDeleteWorkflowNode() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id }: { id: number; workflowId: number }) =>
      apiDelete(`/workflow-node/${id}`),
    onSuccess: (_, vars) =>
      qc.invalidateQueries({ queryKey: workflowKeys.nodes(vars.workflowId) }),
  });
}

export function useCreateWorkflowTransition() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: Partial<WorkflowTransition>) =>
      apiPost<WorkflowTransition>("/workflow-transition", data),
    onSuccess: (_, vars) =>
      qc.invalidateQueries({ queryKey: workflowKeys.transitions(vars.workflowDefinitionId ?? 0) }),
  });
}

export function useDeleteWorkflowTransition() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id }: { id: number; workflowId: number }) =>
      apiDelete(`/workflow-transition/${id}`),
    onSuccess: (_, vars) =>
      qc.invalidateQueries({ queryKey: workflowKeys.transitions(vars.workflowId) }),
  });
}
