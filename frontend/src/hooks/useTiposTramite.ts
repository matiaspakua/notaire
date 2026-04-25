/**
 * React Query hooks for TipoDeTramite (CU26, CU57, CU64 — Tipos de trámite).
 * Endpoint: GET/POST/PUT/DELETE /api/v1/tipo-tramite
 */
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { apiGet, apiPost, apiPut, apiDelete } from "@/lib/api-client";
import type { TipoDeTramite } from "@/types";

export const tiposTramiteKeys = {
  all: ["tiposTramite"] as const,
  detail: (id: number) => ["tiposTramite", id] as const,
};

export function useTiposTramite() {
  return useQuery({
    queryKey: tiposTramiteKeys.all,
    queryFn: () => apiGet<TipoDeTramite[]>("/tipo-tramite"),
  });
}

export function useCreateTipoTramite() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: Partial<TipoDeTramite>) =>
      apiPost<void>("/tipo-tramite", data),
    onSuccess: () =>
      qc.invalidateQueries({ queryKey: tiposTramiteKeys.all }),
  });
}

export function useUpdateTipoTramite() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: Partial<TipoDeTramite> }) =>
      apiPut<void>(`/tipo-tramite/${id}`, data),
    onSuccess: () =>
      qc.invalidateQueries({ queryKey: tiposTramiteKeys.all }),
  });
}

export function useDeleteTipoTramite() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => apiDelete(`/tipo-tramite/${id}`),
    onSuccess: () =>
      qc.invalidateQueries({ queryKey: tiposTramiteKeys.all }),
  });
}
