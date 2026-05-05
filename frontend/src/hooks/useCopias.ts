import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { apiGet, apiPost, apiPut, apiDelete } from "@/lib/api-client";
import type { Copia } from "@/types";

export const copiasKeys = {
  all: ["copias"] as const,
  detail: (id: number) => ["copias", id] as const,
  byTestimonio: (id: number) => ["copias", "testimonio", id] as const,
};

export function useCopias() {
  return useQuery({
    queryKey: copiasKeys.all,
    queryFn: () => apiGet<Copia[]>("/copias"),
  });
}

export function useCopiasByTestimonio(idTestimonio: number) {
  return useQuery({
    queryKey: copiasKeys.byTestimonio(idTestimonio),
    queryFn: () => apiGet<Copia[]>(`/copias/testimonio/${idTestimonio}`),
    enabled: idTestimonio > 0,
  });
}

export function useCreateCopia() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: Partial<Copia>) => apiPost<void>("/copias", data),
    onSuccess: () => qc.invalidateQueries({ queryKey: copiasKeys.all }),
  });
}

export function useUpdateCopia() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: Partial<Copia> }) =>
      apiPut<void>(`/copias/${id}`, data),
    onSuccess: () => qc.invalidateQueries({ queryKey: copiasKeys.all }),
  });
}

export function useDeleteCopia() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => apiDelete(`/copias/${id}`),
    onSuccess: () => qc.invalidateQueries({ queryKey: copiasKeys.all }),
  });
}
