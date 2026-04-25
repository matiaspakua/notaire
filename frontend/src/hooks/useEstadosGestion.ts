/**
 * React Query hooks for EstadoDeGestion (CU67 — Gestionar estados de gestión).
 * Endpoint: GET/POST/PUT/DELETE /api/v1/estado-gestion
 */
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { apiGet, apiPost, apiPut, apiDelete } from "@/lib/api-client";
import type { EstadoDeGestion } from "@/types";

export const estadosGestionKeys = {
  all: ["estadosGestion"] as const,
  detail: (id: number) => ["estadosGestion", id] as const,
};

export function useEstadosGestion() {
  return useQuery({
    queryKey: estadosGestionKeys.all,
    queryFn: () => apiGet<EstadoDeGestion[]>("/estado-gestion"),
  });
}

export function useCreateEstadoGestion() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: Partial<EstadoDeGestion>) =>
      apiPost<void>("/estado-gestion", data),
    onSuccess: () =>
      qc.invalidateQueries({ queryKey: estadosGestionKeys.all }),
  });
}

export function useUpdateEstadoGestion() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: Partial<EstadoDeGestion> }) =>
      apiPut<void>(`/estado-gestion/${id}`, data),
    onSuccess: () =>
      qc.invalidateQueries({ queryKey: estadosGestionKeys.all }),
  });
}

export function useDeleteEstadoGestion() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => apiDelete(`/estado-gestion/${id}`),
    onSuccess: () =>
      qc.invalidateQueries({ queryKey: estadosGestionKeys.all }),
  });
}
