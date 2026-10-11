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

/** CU16 - Saldo pendiente agregado de una gestión (RF-22), consultado antes de archivar. */
export function useOutstandingBalance(managementId: number | undefined) {
  return useQuery({
    queryKey: managementsKeys.outstandingBalance(managementId ?? 0),
    queryFn: () => apiGet<DtoOutstandingBalance>(`/managements/${managementId}/outstanding-balance`),
    enabled: !!managementId,
  });
}

/**
 * CU16 - Archiva la gestión advirtiendo y registrando la debt pendiente (RF-22, RF-37).
 * `confirmado` debe reenviarse en `true` cuando el usuario acepta archivar a pesar de
 * folders de trámite (CU85) en espera sin resolver (HTTP 409).
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

/** CU83 - Transiciona el estado de una gestión validando el workflow definido. */
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

/** CU13 - Bitácora completa de una gestión, ordenada cronológicamente. */
export function useHistory(managementId: number | undefined) {
  return useQuery({
    queryKey: historyKeys.byManagement(managementId ?? 0),
    queryFn: () => apiGet<ManagementHistory[]>(`/managements/${managementId}/history`),
    enabled: !!managementId,
  });
}

/** CU85 - Carpetas de trámite asociadas a una gestión. */
export function useFoldersByManagement(managementId: number | undefined) {
  return useQuery({
    queryKey: procedureFolderKeys.byManagement(managementId ?? 0),
    queryFn: () => apiGet<ProcedureFolder[]>(`/procedure-folders?managementId=${managementId}`),
    enabled: !!managementId,
  });
}

/** CU85 - Pone una carpeta de trámite en espera, exigiendo un motivo (Excepción 3.1). */
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
