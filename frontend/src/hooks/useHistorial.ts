import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { apiGet, apiPost, apiPut, apiDelete } from "@/lib/api-client";
import type { Historial } from "@/types";

export const historialKeys = {
  all: ["historial"] as const,
  detail: (id: number) => ["historial", id] as const,
  byGestion: (id: number) => ["historial", "gestion", id] as const,
};

export function useHistorial() {
  return useQuery({
    queryKey: historialKeys.all,
    queryFn: () => apiGet<Historial[]>("/historial"),
  });
}

/** CU13, CU53 — Obtener historial de una gestión */
export function useHistorialByGestion(idGestion: number) {
  return useQuery({
    queryKey: historialKeys.byGestion(idGestion),
    queryFn: () => apiGet<Historial[]>(`/historial/gestion/${idGestion}`),
    enabled: idGestion > 0,
  });
}

export function useCreateHistorial() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: Partial<Historial>) => apiPost<void>("/historial", data),
    onSuccess: () => qc.invalidateQueries({ queryKey: historialKeys.all }),
  });
}

export function useUpdateHistorial() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: Partial<Historial> }) =>
      apiPut<void>(`/historial/${id}`, data),
    onSuccess: () => qc.invalidateQueries({ queryKey: historialKeys.all }),
  });
}

export function useDeleteHistorial() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => apiDelete(`/historial/${id}`),
    onSuccess: () => qc.invalidateQueries({ queryKey: historialKeys.all }),
  });
}
