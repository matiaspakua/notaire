/**
 * React Query hooks for Folio (CU28, CU40, CU58, CU68 — Gestión de folios).
 * Endpoints: GET/POST/PUT/DELETE /api/v1/folio
 *            GET /api/v1/tipo-folio
 */
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { apiGet, apiPost, apiPut, apiDelete } from "@/lib/api-client";
import type { Folio, TipoDeFolio } from "@/types";

export const foliosKeys = {
  all: ["folios"] as const,
  detail: (id: number) => ["folios", id] as const,
  tiposFolio: ["tiposFolio"] as const,
};

export function useFolios() {
  return useQuery({
    queryKey: foliosKeys.all,
    queryFn: () => apiGet<Folio[]>("/folio"),
  });
}

export function useTiposFolio() {
  return useQuery({
    queryKey: foliosKeys.tiposFolio,
    queryFn: () => apiGet<TipoDeFolio[]>("/tipo-folio"),
  });
}

export function useCreateFolio() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: Partial<Folio>) => apiPost<void>("/folio", data),
    onSuccess: () => qc.invalidateQueries({ queryKey: foliosKeys.all }),
  });
}

export function useUpdateFolio() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: Partial<Folio> }) =>
      apiPut<void>(`/folio/${id}`, data),
    onSuccess: () => qc.invalidateQueries({ queryKey: foliosKeys.all }),
  });
}

export function useDeleteFolio() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => apiDelete(`/folio/${id}`),
    onSuccess: () => qc.invalidateQueries({ queryKey: foliosKeys.all }),
  });
}
