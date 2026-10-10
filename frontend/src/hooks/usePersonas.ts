import { keepPreviousData, useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { apiGet, apiGetPage, apiGetPaged, apiPost, apiPut, apiDelete } from "@/lib/api-client";
import type { Persona } from "@/types";

export const personasKeys = {
  all: ["personas"] as const,
  detail: (id: number) => ["personas", id] as const,
  page: (params: PersonasPageParams) => ["personas", "page", params] as const,
};

export interface PersonasPageParams {
  page: number;
  size: number;
}

/** Sort for the people list: newest first, so a person just created is on page 1 (#1340). */
export const PERSONAS_SORT = "idPerson,desc";

/**
 * One server page of GET /people (#1340). The people list outgrows any fixed
 * size, so the list screen never loads it whole.
 */
export function usePersonasPage(params: PersonasPageParams) {
  return useQuery({
    queryKey: personasKeys.page(params),
    queryFn: () =>
      apiGetPage<Persona>("/people", { page: params.page, size: params.size, sort: PERSONAS_SORT }),
    placeholderData: keepPreviousData,
  });
}

/** One person by id, for links to a record that is not on the loaded page. */
export function fetchPersona(id: number) {
  return apiGet<Persona>(`/people/${id}`);
}

export function usePersonas() {
  return useQuery({
    queryKey: personasKeys.all,
    queryFn: () => apiGetPaged<Persona>("/people"),
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
