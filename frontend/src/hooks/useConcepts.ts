import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { apiGet, apiPost, apiPut, apiDelete } from "@/lib/api-client";
import type { Concept } from "@/types";

export const conceptosKeys = { all: ["conceptos"] as const };

export function useConcepts() {
  return useQuery({
    queryKey: conceptosKeys.all,
    queryFn: () => apiGet<Concept[]>("/concepts"),
  });
}

export function useCreateConcepto() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: Partial<Concept>) => apiPost<void>("/concepts", data),
    onSuccess: () => qc.invalidateQueries({ queryKey: conceptosKeys.all }),
  });
}

export function useUpdateConcepto() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: Partial<Concept> }) =>
      apiPut<void>(`/concepts/${id}`, data),
    onSuccess: () => qc.invalidateQueries({ queryKey: conceptosKeys.all }),
  });
}

export function useDeleteConcepto() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => apiDelete(`/concepts/${id}`),
    onSuccess: () => qc.invalidateQueries({ queryKey: conceptosKeys.all }),
  });
}
