import { keepPreviousData, useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { apiGet, apiGetPage, apiPost, apiPut, apiDelete } from "@/lib/api-client";
import { managementWorkflowKeys } from "@/hooks/useManagementWorkflow";
import type { DtoOutstandingBalance, DeedManagement } from "@/types/api";
import type {
  ProcedureFolder,
  CreateCompleteManagementInput,
  DtoArchivedManagement,
  ManagementHistory,
} from "@/types";

export const managementsKeys = {
  all: ["managements"] as const,
  detail: (id: number) => ["managements", id] as const,
  byClient: (id: number) => ["managements", "client", id] as const,
  byNumber: (number: number) => ["managements", "number", number] as const,
  outstandingBalance: (id: number) => ["managements", id, "outstanding-balance"] as const,
  page: (params: ManagementsPageParams) => ["managements", "page", params] as const,
};

export interface ManagementsPageParams {
  page: number;
  size: number;
  /** Spring sort; defaults to newest first. `null` keeps the backend order. */
  sort?: string | null;
}

/** Sort for the managements list: newest first (#1340). */
export const MANAGEMENTS_SORT = "idManagement,desc";

/**
 * One server page of GET /managements (#1340). The managements table outgrows any
 * fixed size, so screens never load it whole.
 */
export function useManagementsPage(params: ManagementsPageParams, options: { enabled?: boolean } = {}) {
  return useQuery({
    queryKey: managementsKeys.page(params),
    queryFn: () =>
      apiGetPage<DeedManagement>("/managements", {
        page: params.page,
        size: params.size,
        sort: params.sort === null ? undefined : (params.sort ?? MANAGEMENTS_SORT),
      }),
    placeholderData: keepPreviousData,
    enabled: options.enabled ?? true,
  });
}

export const historyKeys = {
  byManagement: (id: number) => ["history", "management", id] as const,
};

export const procedureFolderKeys = {
  byManagement: (id: number) => ["procedure-folders", "management", id] as const,
};

export function useManagementByNumber(number: number | undefined) {
  return useQuery({
    queryKey: managementsKeys.byNumber(number ?? 0),
    queryFn: () => apiGet<DeedManagement>(`/managements/number/${number}`),
    enabled: number != null && number > 0,
    retry: false,
  });
}

export function useManagementsByClient(idPerson: number) {
  return useQuery({
    queryKey: managementsKeys.byClient(idPerson),
    queryFn: () => apiGet<DeedManagement[]>(`/managements/client/${idPerson}`),
    enabled: idPerson > 0,
  });
}

export function useCreateManagement() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: Partial<DeedManagement>) =>
      apiPost<void>("/managements", data),
    onSuccess: () => qc.invalidateQueries({ queryKey: managementsKeys.all }),
  });
}

export function useCreateCompleteManagement() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: CreateCompleteManagementInput) =>
      apiPost<DeedManagement>("/managements/complete-case", data),
    onSuccess: () => qc.invalidateQueries({ queryKey: managementsKeys.all }),
  });
}

export function useUpdateManagement() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: Partial<DeedManagement> }) =>
      apiPut<void>(`/managements/${id}`, data),
    onSuccess: () => qc.invalidateQueries({ queryKey: managementsKeys.all }),
  });
}

export function useDeleteManagement() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => apiDelete(`/managements/${id}`),
    onSuccess: () => qc.invalidateQueries({ queryKey: managementsKeys.all }),
  });
}

/** CU16 - Aggregated outstanding balance of a management (RF-22), loaded before archive. */
export function useOutstandingBalance(managementId: number | undefined) {
  return useQuery({
    queryKey: managementsKeys.outstandingBalance(managementId ?? 0),
    queryFn: () => apiGet<DtoOutstandingBalance>(`/managements/${managementId}/outstanding-balance`),
    enabled: !!managementId,
  });
}

/**
 * CU16 - Archives the management, warning about and recording outstanding debt (RF-22, RF-37).
 * The `confirmado` query flag must be sent as true when the user archives anyway
 * despite unresolved procedure folders on hold (CU85, HTTP 409).
 */
export function useArchiveManagement() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, confirmado = false }: { id: number; confirmado?: boolean }) =>
      apiPost<DtoArchivedManagement>(`/managements/${id}/archive?confirmado=${confirmado}`, {}),
    onSuccess: (_data, { id }) => {
      qc.invalidateQueries({ queryKey: managementsKeys.all });
      qc.invalidateQueries({ queryKey: procedureFolderKeys.byManagement(id) });
    },
  });
}

/** CU83 - Moves a management to another status after the defined workflow allows it. */
export function useTransitionManagement() {
  const qc = useQueryClient();
  return useMutation({
    // Backend route is /transition (English, see ManagementController#transition) and its
    // request body is DtoTransicionRequest(statusDestination) — not /transicionar+estadoDestino.
    mutationFn: ({ id, estadoDestino }: { id: number; estadoDestino: string }) =>
      apiPost<DeedManagement>(`/managements/${id}/transition`, { statusDestination: estadoDestino }),
    onSuccess: (_data, { id }) => {
      qc.invalidateQueries({ queryKey: managementsKeys.all });
      qc.invalidateQueries({ queryKey: managementWorkflowKeys.trace(id) });
      qc.invalidateQueries({ queryKey: historyKeys.byManagement(id) });
    },
  });
}

/** CU13 - Full history of a management, in chronological order. */
export function useHistory(managementId: number | undefined) {
  return useQuery({
    queryKey: historyKeys.byManagement(managementId ?? 0),
    queryFn: () => apiGet<ManagementHistory[]>(`/managements/${managementId}/history`),
    enabled: !!managementId,
  });
}

/** CU85 - Procedure folders linked to a management. */
export function useFoldersByManagement(managementId: number | undefined) {
  return useQuery({
    queryKey: procedureFolderKeys.byManagement(managementId ?? 0),
    queryFn: () => apiGet<ProcedureFolder[]>(`/procedure-folders?managementId=${managementId}`),
    enabled: !!managementId,
  });
}

/** CU85 - Puts a procedure folder on hold and requires a reason (exception 3.1). */
export function usePutFolderOnHold() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ idFolder, motivo }: { idFolder: number; motivo: string; managementId: number }) =>
      apiPut<ProcedureFolder>(`/procedure-folders/${idFolder}/wait`, { reason: motivo }),
    onSuccess: (_data, { managementId }) => {
      qc.invalidateQueries({ queryKey: procedureFolderKeys.byManagement(managementId) });
    },
  });
}
