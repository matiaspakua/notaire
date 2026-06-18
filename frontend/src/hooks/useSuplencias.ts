import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { apiGet, apiPost, apiPut, apiDelete } from "@/lib/api-client";
import type { Suplencia } from "@/types";

export const suplenciasKeys = {
  all: ["suplencias"] as const,
  detail: (id: number) => ["suplencias", id] as const,
};

/** CU12, CU42, CU59 — Gestión de suplencias de escribano */
export function useSuplencias() {
  return useQuery({
    queryKey: suplenciasKeys.all,
    queryFn: () => apiGet<Suplencia[]>("/suplencia"),
  });
}

export function useCreateSuplencia() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: Partial<Suplencia>) => apiPost<void>("/suplencia", data),
    onSuccess: () => qc.invalidateQueries({ queryKey: suplenciasKeys.all }),
  });
}

export function useUpdateSuplencia() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: Partial<Suplencia> }) =>
      apiPut<void>(`/suplencia/${id}`, data),
    onSuccess: () => qc.invalidateQueries({ queryKey: suplenciasKeys.all }),
  });
}

export function useDeleteSuplencia() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => apiDelete(`/suplencia/${id}`),
    onSuccess: () => qc.invalidateQueries({ queryKey: suplenciasKeys.all }),
  });
}
