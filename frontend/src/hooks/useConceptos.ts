import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { apiGet, apiPost, apiPut, apiDelete } from "@/lib/api-client";
import type { Concepto } from "@/types";

export const conceptosKeys = { all: ["conceptos"] as const };

export function useConceptos() {
  return useQuery({
    queryKey: conceptosKeys.all,
    queryFn: () => apiGet<Concepto[]>("/conceptos"),
  });
}

export function useCreateConcepto() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: Partial<Concepto>) => apiPost<void>("/conceptos", data),
    onSuccess: () => qc.invalidateQueries({ queryKey: conceptosKeys.all }),
  });
}

export function useUpdateConcepto() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: Partial<Concepto> }) =>
      apiPut<void>(`/conceptos/${id}`, data),
    onSuccess: () => qc.invalidateQueries({ queryKey: conceptosKeys.all }),
  });
}

export function useDeleteConcepto() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => apiDelete(`/conceptos/${id}`),
    onSuccess: () => qc.invalidateQueries({ queryKey: conceptosKeys.all }),
  });
}
