import { keepPreviousData, useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { apiGet, apiGetPage, apiPost, apiPut, apiDelete } from "@/lib/api-client";
import { gestionWorkflowKeys } from "@/hooks/useManagementWorkflow";
import type { DtoOutstandingBalance, DeedManagement } from "@/types/api";
import type {
  ProcedureFolder,
  CreateCompleteManagementInput,
  DtoArchivedManagement,
  ManagementHistory,
} from "@/types";

export const gestionesKeys = {
  all: ["gestiones"] as const,
  detail: (id: number) => ["gestiones", id] as const,
  byCliente: (id: number) => ["gestiones", "cliente", id] as const,
  byNumero: (numero: number) => ["gestiones", "numero", numero] as const,
  saldoPendiente: (id: number) => ["gestiones", id, "saldo-pendiente"] as const,
  page: (params: GestionesPageParams) => ["gestiones", "page", params] as const,
};

export interface GestionesPageParams {
  page: number;
  size: number;
  /** Spring sort; defaults to newest first. `null` keeps the backend order. */
  sort?: string | null;
}

/** Sort for the managements list: newest first (#1340). */
export const GESTIONES_SORT = "idManagement,desc";

/**
 * One server page of GET /managements (#1340). The managements table outgrows any
 * fixed size, so screens never load it whole.
 */
export function useGestionesPage(params: GestionesPageParams, options: { enabled?: boolean } = {}) {
  return useQuery({
    queryKey: gestionesKeys.page(params),
    queryFn: () =>
      apiGetPage<DeedManagement>("/managements", {
        page: params.page,
        size: params.size,
        sort: params.sort === null ? undefined : (params.sort ?? GESTIONES_SORT),
      }),
    placeholderData: keepPreviousData,
    enabled: options.enabled ?? true,
  });
}

export const historialKeys = {
  byGestion: (id: number) => ["historial", "gestion", id] as const,
};

export const carpetasTramiteKeys = {
  byGestion: (id: number) => ["carpetas-tramite", "gestion", id] as const,
};

export function useGestionByNumero(numero: number | undefined) {
  return useQuery({
    queryKey: gestionesKeys.byNumero(numero ?? 0),
    queryFn: () => apiGet<DeedManagement>(`/managements/number/${numero}`),
    enabled: numero != null && numero > 0,
    retry: false,
  });
}

export function useGestionesByCliente(idPersona: number) {
  return useQuery({
    queryKey: gestionesKeys.byCliente(idPersona),
    queryFn: () => apiGet<DeedManagement[]>(`/managements/client/${idPersona}`),
    enabled: idPersona > 0,
  });
}

export function useCreateGestion() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: Partial<DeedManagement>) =>
      apiPost<void>("/managements", data),
    onSuccess: () => qc.invalidateQueries({ queryKey: gestionesKeys.all }),
  });
}

export function useCreateCompleteGestion() {
  const qc = useQueryClient();
  return useMutation({
    // ManagementController's CompleteCaseRequest record field names are
    // (number, encabezado, notes, budgetId, notaryId, statusManagementId,
    // typeProcedureId, propertyId) — not the presupuestoId/escribanoId/
    // estadoGestionId/tipoTramiteId names kept on CreateCompleteManagementInput
    // for callers, so translate before posting.
    mutationFn: (data: CreateCompleteManagementInput) =>
      apiPost<DeedManagement>("/managements/complete-case", {
        number: data.number,
        encabezado: data.encabezado,
        notes: data.notes,
        budgetId: data.presupuestoId,
        notaryId: data.escribanoId,
        statusManagementId: data.estadoGestionId,
        typeProcedureId: data.tipoTramiteId,
        propertyId: data.inmuebleId,
      }),
    onSuccess: () => qc.invalidateQueries({ queryKey: gestionesKeys.all }),
  });
}

export function useUpdateGestion() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: Partial<DeedManagement> }) =>
      apiPut<void>(`/managements/${id}`, data),
    onSuccess: () => qc.invalidateQueries({ queryKey: gestionesKeys.all }),
  });
}

export function useDeleteGestion() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => apiDelete(`/managements/${id}`),
    onSuccess: () => qc.invalidateQueries({ queryKey: gestionesKeys.all }),
  });
}

/** CU16 - Saldo pendiente agregado de una gestión (RF-22), consultado antes de archivar. */
export function useSaldoPendiente(gestionId: number | undefined) {
  return useQuery({
    queryKey: gestionesKeys.saldoPendiente(gestionId ?? 0),
    queryFn: () => apiGet<DtoOutstandingBalance>(`/managements/${gestionId}/outstanding-balance`),
    enabled: !!gestionId,
  });
}

/**
 * CU16 - Archiva la gestión advirtiendo y registrando la deuda pendiente (RF-22, RF-37).
 * `confirmado` debe reenviarse en `true` cuando el usuario acepta archivar a pesar de
 * carpetas de trámite (CU85) en espera sin resolver (HTTP 409).
 */
export function useArchivarGestion() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, confirmado = false }: { id: number; confirmado?: boolean }) =>
      apiPost<DtoArchivedManagement>(`/managements/${id}/archive?confirmado=${confirmado}`, {}),
    onSuccess: (_data, { id }) => {
      qc.invalidateQueries({ queryKey: gestionesKeys.all });
      qc.invalidateQueries({ queryKey: carpetasTramiteKeys.byGestion(id) });
    },
  });
}

/** CU83 - Transiciona el estado de una gestión validando el workflow definido. */
export function useTransicionarGestion() {
  const qc = useQueryClient();
  return useMutation({
    // Backend route is /transition (English, see ManagementController#transition) and its
    // request body is DtoTransicionRequest(statusDestination) — not /transicionar+estadoDestino.
    mutationFn: ({ id, estadoDestino }: { id: number; estadoDestino: string }) =>
      apiPost<DeedManagement>(`/managements/${id}/transition`, { statusDestination: estadoDestino }),
    onSuccess: (_data, { id }) => {
      qc.invalidateQueries({ queryKey: gestionesKeys.all });
      qc.invalidateQueries({ queryKey: gestionWorkflowKeys.trace(id) });
      qc.invalidateQueries({ queryKey: historialKeys.byGestion(id) });
    },
  });
}

/** CU13 - Bitácora completa de una gestión, ordenada cronológicamente. */
export function useHistorial(gestionId: number | undefined) {
  return useQuery({
    queryKey: historialKeys.byGestion(gestionId ?? 0),
    queryFn: () => apiGet<ManagementHistory[]>(`/managements/${gestionId}/history`),
    enabled: !!gestionId,
  });
}

/** CU85 - Carpetas de trámite asociadas a una gestión. */
export function useCarpetasByGestion(gestionId: number | undefined) {
  return useQuery({
    queryKey: carpetasTramiteKeys.byGestion(gestionId ?? 0),
    queryFn: () => apiGet<ProcedureFolder[]>(`/procedure-folders?managementId=${gestionId}`),
    enabled: !!gestionId,
  });
}

/** CU85 - Pone una carpeta de trámite en espera, exigiendo un motivo (Excepción 3.1). */
export function usePonerCarpetaEnEspera() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ idCarpeta, motivo }: { idCarpeta: number; motivo: string; gestionId: number }) =>
      apiPut<ProcedureFolder>(`/procedure-folders/${idCarpeta}/wait`, { reason: motivo }),
    onSuccess: (_data, { gestionId }) => {
      qc.invalidateQueries({ queryKey: carpetasTramiteKeys.byGestion(gestionId) });
    },
  });
}
