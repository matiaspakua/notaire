import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { apiGet, apiPost, apiPut, apiDelete } from "@/lib/api-client";
import type { Concept } from "@/types";

export const conceptsKeys = { all: ["concepts"] as const };

export function useConcepts() {
  return useQuery({
    queryKey: conceptsKeys.all,
    queryFn: () => apiGet<Concept[]>("/concepts"),
  });
}

export function useCreateConcept() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: Partial<Concept>) => apiPost<void>("/concepts", data),
    onSuccess: () => qc.invalidateQueries({ queryKey: conceptsKeys.all }),
  });
}

export function useUpdateConcept() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: Partial<Concept> }) =>
      apiPut<void>(`/concepts/${id}`, data),
    onSuccess: () => qc.invalidateQueries({ queryKey: conceptsKeys.all }),
  });
}

export function useDeleteConcept() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => apiDelete(`/concepts/${id}`),
    onSuccess: () => qc.invalidateQueries({ queryKey: conceptsKeys.all }),
  });
}
