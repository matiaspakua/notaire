import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { apiGet, apiGetPaged, apiPost, apiPut, apiDelete } from "@/lib/api-client";
import { gestionWorkflowKeys } from "@/hooks/useGestionWorkflow";
import type {
  CarpetaTramite,
  CreateCompleteGestionInput,
  DtoGestionArchivada,
  DtoSaldoPendiente,
  GestionDeEscritura,
  Historial,
} from "@/types";

export const gestionesKeys = {
  all: ["gestiones"] as const,
  detail: (id: number) => ["gestiones", id] as const,
  byCliente: (id: number) => ["gestiones", "cliente", id] as const,
  byNumero: (numero: number) => ["gestiones", "numero", numero] as const,
  saldoPendiente: (id: number) => ["gestiones", id, "saldo-pendiente"] as const,
};

export const historialKeys = {
  byGestion: (id: number) => ["historial", "gestion", id] as const,
};

export const carpetasTramiteKeys = {
  byGestion: (id: number) => ["carpetas-tramite", "gestion", id] as const,
};

export function useGestiones() {
  return useQuery({
    queryKey: gestionesKeys.all,
    queryFn: () => apiGetPaged<GestionDeEscritura>("/gestiones"),
  });
}

export function useGestionByNumero(numero: number | undefined) {
  return useQuery({
    queryKey: gestionesKeys.byNumero(numero ?? 0),
    queryFn: () => apiGet<GestionDeEscritura>(`/gestiones/numero/${numero}`),
    enabled: numero != null && numero > 0,
    retry: false,
  });
}

export function useGestionesByCliente(idPersona: number) {
  return useQuery({
    queryKey: gestionesKeys.byCliente(idPersona),
    queryFn: () => apiGet<GestionDeEscritura[]>(`/gestiones/cliente/${idPersona}`),
    enabled: idPersona > 0,
  });
}

export function useCreateGestion() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: Partial<GestionDeEscritura>) =>
      apiPost<void>("/gestiones", data),
    onSuccess: () => qc.invalidateQueries({ queryKey: gestionesKeys.all }),
  });
}

export function useCreateCompleteGestion() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: CreateCompleteGestionInput) =>
      apiPost<GestionDeEscritura>("/gestiones/complete-case", data),
    onSuccess: () => qc.invalidateQueries({ queryKey: gestionesKeys.all }),
  });
}

export function useUpdateGestion() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: Partial<GestionDeEscritura> }) =>
      apiPut<void>(`/gestiones/${id}`, data),
    onSuccess: () => qc.invalidateQueries({ queryKey: gestionesKeys.all }),
  });
}

export function useDeleteGestion() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => apiDelete(`/gestiones/${id}`),
    onSuccess: () => qc.invalidateQueries({ queryKey: gestionesKeys.all }),
  });
}

/** CU16 - Saldo pendiente agregado de una gestión (RF-22), consultado antes de archivar. */
export function useSaldoPendiente(gestionId: number | undefined) {
  return useQuery({
    queryKey: gestionesKeys.saldoPendiente(gestionId ?? 0),
    queryFn: () => apiGet<DtoSaldoPendiente>(`/gestiones/${gestionId}/saldo-pendiente`),
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
      apiPost<DtoGestionArchivada>(`/gestiones/${id}/archivar?confirmado=${confirmado}`, {}),
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
    mutationFn: ({ id, estadoDestino }: { id: number; estadoDestino: string }) =>
      apiPost<GestionDeEscritura>(`/gestiones/${id}/transicionar`, { estadoDestino }),
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
    queryFn: () => apiGet<Historial[]>(`/gestiones/${gestionId}/historial`),
    enabled: !!gestionId,
  });
}

/** CU85 - Carpetas de trámite asociadas a una gestión. */
export function useCarpetasByGestion(gestionId: number | undefined) {
  return useQuery({
    queryKey: carpetasTramiteKeys.byGestion(gestionId ?? 0),
    queryFn: () => apiGet<CarpetaTramite[]>(`/carpetas?gestionId=${gestionId}`),
    enabled: !!gestionId,
  });
}

/** CU85 - Pone una carpeta de trámite en espera, exigiendo un motivo (Excepción 3.1). */
export function usePonerCarpetaEnEspera() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ idCarpeta, motivo }: { idCarpeta: number; motivo: string; gestionId: number }) =>
      apiPut<CarpetaTramite>(`/carpetas/${idCarpeta}/espera`, { motivo }),
    onSuccess: (_data, { gestionId }) => {
      qc.invalidateQueries({ queryKey: carpetasTramiteKeys.byGestion(gestionId) });
    },
  });
}
