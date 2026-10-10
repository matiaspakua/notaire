/**
 * React Query hooks for ProcedureType (CU26, CU57, CU64 — Tipos de trámite).
 * Endpoint: GET/POST/PUT/DELETE /api/v1/procedure-types
 */
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { apiGet, apiPost, apiPut, apiDelete } from "@/lib/api-client";
import type { ProcedureType } from "@/types";

export const tiposTramiteKeys = {
  all: ["tiposTramite"] as const,
  detail: (id: number) => ["tiposTramite", id] as const,
};

export function useProcedureTypes() {
  return useQuery({
    queryKey: tiposTramiteKeys.all,
    queryFn: () => apiGet<ProcedureType[]>("/procedure-types"),
  });
}

export function useCreateTipoTramite() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: Partial<ProcedureType>) =>
      apiPost<void>("/procedure-types", data),
    onSuccess: () =>
      qc.invalidateQueries({ queryKey: tiposTramiteKeys.all }),
  });
}

export function useUpdateTipoTramite() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: Partial<ProcedureType> }) =>
      apiPut<void>(`/procedure-types/${id}`, data),
    onSuccess: () =>
      qc.invalidateQueries({ queryKey: tiposTramiteKeys.all }),
  });
}

export function useDeleteTipoTramite() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => apiDelete(`/procedure-types/${id}`),
    onSuccess: () =>
      qc.invalidateQueries({ queryKey: tiposTramiteKeys.all }),
  });
}

export function useAssignWorkflowToTipoTramite() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, workflowDefinitionId }: { id: number; workflowDefinitionId: number | null }) =>
      apiPut<ProcedureType>(`/procedure-types/${id}/workflow`, { workflowDefinitionId }),
    onSuccess: () =>
      qc.invalidateQueries({ queryKey: tiposTramiteKeys.all }),
  });
}
