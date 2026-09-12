import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { apiGet, apiPost, apiPut, apiDelete } from "@/lib/api-client";
import type { Persona } from "@/types";

export const personasKeys = {
  all: ["personas"] as const,
  detail: (id: number) => ["personas", id] as const,
};

export function usePersonas() {
  return useQuery({
    queryKey: personasKeys.all,
    queryFn: () => apiGet<Persona[]>("/people"),
  });
}

export function useCreatePersona() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: Partial<Persona>) => apiPost<void>("/people", data),
    onSuccess: () => qc.invalidateQueries({ queryKey: personasKeys.all }),
  });
}

export function useUpdatePersona() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: Partial<Persona> }) =>
      apiPut<void>(`/people/${id}`, data),
    onSuccess: () => qc.invalidateQueries({ queryKey: personasKeys.all }),
  });
}

export function useDeletePersona() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => apiDelete(`/people/${id}`),
    onSuccess: () => qc.invalidateQueries({ queryKey: personasKeys.all }),
  });
}
