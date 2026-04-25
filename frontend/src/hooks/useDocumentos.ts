/**
 * React Query hooks for TipoDeDocumento (CU27, CU65 — Tipos de documento).
 * Endpoint: GET/POST/PUT/DELETE /api/v1/tipo-de-documento
 */
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { apiGet, apiPost, apiPut, apiDelete } from "@/lib/api-client";
import type { TipoDeDocumento } from "@/types";

export const documentosKeys = {
  all: ["tiposDocumento"] as const,
  detail: (id: number) => ["tiposDocumento", id] as const,
};

export function useTiposDocumento() {
  return useQuery({
    queryKey: documentosKeys.all,
    queryFn: () => apiGet<TipoDeDocumento[]>("/tipo-de-documento"),
  });
}

export function useCreateTipoDocumento() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: Partial<TipoDeDocumento>) =>
      apiPost<void>("/tipo-de-documento", data),
    onSuccess: () =>
      qc.invalidateQueries({ queryKey: documentosKeys.all }),
  });
}

export function useUpdateTipoDocumento() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: Partial<TipoDeDocumento> }) =>
      apiPut<void>(`/tipo-de-documento/${id}`, data),
    onSuccess: () =>
      qc.invalidateQueries({ queryKey: documentosKeys.all }),
  });
}

export function useDeleteTipoDocumento() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => apiDelete(`/tipo-de-documento/${id}`),
    onSuccess: () =>
      qc.invalidateQueries({ queryKey: documentosKeys.all }),
  });
}
