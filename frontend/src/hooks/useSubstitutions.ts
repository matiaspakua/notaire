import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { apiGet, apiPost, apiPut, apiDelete } from "@/lib/api-client";
import type { Substitution } from "@/types";

export const substitutionsKeys = {
  all: ["substitutions"] as const,
  detail: (id: number) => ["substitutions", id] as const,
};

/** CU12, CU42, CU59 — Gestión de suplencias de escribano */
export function useSubstitutions() {
  return useQuery({
    queryKey: substitutionsKeys.all,
    queryFn: () => apiGet<Substitution[]>("/substitutions"),
  });
}

export function useCreateSubstitution() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: Partial<Substitution>) => apiPost<void>("/substitutions", data),
    onSuccess: () => qc.invalidateQueries({ queryKey: substitutionsKeys.all }),
  });
}

export function useUpdateSubstitution() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: Partial<Substitution> }) =>
      apiPut<void>(`/substitutions/${id}`, data),
    onSuccess: () => qc.invalidateQueries({ queryKey: substitutionsKeys.all }),
  });
}

export function useDeleteSubstitution() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => apiDelete(`/substitutions/${id}`),
    onSuccess: () => qc.invalidateQueries({ queryKey: substitutionsKeys.all }),
  });
}
