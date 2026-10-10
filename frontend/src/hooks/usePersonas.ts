import { keepPreviousData, useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { apiGet, apiGetPage, apiGetPaged, apiPost, apiPut, apiDelete } from "@/lib/api-client";
import type { Persona } from "@/types";
import { useDebouncedValue } from "@/hooks/useDebouncedValue";

export const personasKeys = {
  all: ["personas"] as const,
  detail: (id: number) => ["personas", id] as const,
  page: (params: PersonasPageParams) => ["personas", "page", params] as const,
  search: (params: PersonasSearchParams) => ["personas", "search", { ...params }] as const,
};

/** Criteria of the personas search (#1357); primitives only, so they are a stable query key. */
export interface PersonasSearchParams {
  firstName: string;
  lastName: string;
  identificationNumber: string;
  onlyClients: boolean;
}

/** Wait after the last keystroke before searching (#1357). */
export const PERSONAS_SEARCH_DEBOUNCE_MS = 300;

/**
 * GET /people/search for the personas list (#1357). The criteria are debounced,
 * so typing a surname sends one request; the previous results stay on screen
 * while the next ones load, and a stale response never replaces a newer one
 * (each criteria set has its own cache entry).
 */
export function useSearchPersonas(params: PersonasSearchParams) {
  const debounced = useDebouncedValue(params, PERSONAS_SEARCH_DEBOUNCE_MS);
  const settled: PersonasSearchParams = {
    firstName: debounced.firstName.trim(),
    lastName: debounced.lastName.trim(),
    identificationNumber: debounced.identificationNumber.trim(),
    onlyClients: debounced.onlyClients,
  };
  const active = !!(settled.firstName || settled.lastName || settled.identificationNumber || settled.onlyClients);
  const query = useQuery({
    queryKey: personasKeys.search(settled),
    enabled: active,
    queryFn: () => {
      // Backend route is /people/search (PersonController#searchPeople).
      const qs = new URLSearchParams();
      if (settled.firstName) qs.set("firstName", settled.firstName);
      if (settled.lastName) qs.set("lastName", settled.lastName);
      if (settled.identificationNumber) qs.set("identificationNumber", settled.identificationNumber);
      if (settled.onlyClients) qs.set("isClient", "true");
      return apiGet<Persona[]>(`/people/search?${qs.toString()}`);
    },
    placeholderData: keepPreviousData,
  });
  return { active, results: query.data ?? [], isLoading: query.isLoading, isFetching: query.isFetching };
}

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
export function usePersonasPage(params: PersonasPageParams, options: { enabled?: boolean } = {}) {
  return useQuery({
    enabled: options.enabled ?? true,
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
