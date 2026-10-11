import { keepPreviousData, useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { apiGet, apiGetPage, apiPost, apiPut, apiDelete } from "@/lib/api-client";
import type { Person } from "@/types";
import { useDebouncedValue } from "@/hooks/useDebouncedValue";

export const peopleKeys = {
  all: ["people"] as const,
  detail: (id: number) => ["people", id] as const,
  page: (params: PeoplePageParams) => ["people", "page", params] as const,
  search: (params: PeopleSearchParams) => ["people", "search", { ...params }] as const,
};

/** Criteria of the people search (#1357); primitives only, so they are a stable query key. */
export interface PeopleSearchParams {
  firstName: string;
  lastName: string;
  identificationNumber: string;
  onlyClients: boolean;
}

/** Wait after the last keystroke before searching (#1357). */
export const PEOPLE_SEARCH_DEBOUNCE_MS = 300;

/**
 * GET /people/search for the people list (#1357). The criteria are debounced,
 * so typing a surname sends one request; the previous results stay on screen
 * while the next ones load, and a stale response never replaces a newer one
 * (each criteria set has its own cache entry).
 */
export function useSearchPeople(params: PeopleSearchParams) {
  const debounced = useDebouncedValue(params, PEOPLE_SEARCH_DEBOUNCE_MS);
  const settled: PeopleSearchParams = {
    firstName: debounced.firstName.trim(),
    lastName: debounced.lastName.trim(),
    identificationNumber: debounced.identificationNumber.trim(),
    onlyClients: debounced.onlyClients,
  };
  const active = !!(settled.firstName || settled.lastName || settled.identificationNumber || settled.onlyClients);
  const query = useQuery({
    queryKey: peopleKeys.search(settled),
    enabled: active,
    queryFn: () => {
      // Backend route is /people/search (PersonController#searchPeople).
      const qs = new URLSearchParams();
      if (settled.firstName) qs.set("firstName", settled.firstName);
      if (settled.lastName) qs.set("lastName", settled.lastName);
      if (settled.identificationNumber) qs.set("identificationNumber", settled.identificationNumber);
      if (settled.onlyClients) qs.set("isClient", "true");
      return apiGet<Person[]>(`/people/search?${qs.toString()}`);
    },
    placeholderData: keepPreviousData,
  });
  return { active, results: query.data ?? [], isLoading: query.isLoading, isFetching: query.isFetching };
}

export interface PeoplePageParams {
  page: number;
  size: number;
}

/** Sort for the people list: newest first, so a person just created is on page 1 (#1340). */
export const PEOPLE_SORT = "idPerson,desc";

/**
 * One server page of GET /people (#1340). The people list outgrows any fixed
 * size, so the list screen never loads it whole.
 */
export function usePeoplePage(params: PeoplePageParams, options: { enabled?: boolean } = {}) {
  return useQuery({
    enabled: options.enabled ?? true,
    queryKey: peopleKeys.page(params),
    queryFn: () =>
      apiGetPage<Person>("/people", { page: params.page, size: params.size, sort: PEOPLE_SORT }),
    placeholderData: keepPreviousData,
  });
}

/** One person by id, for links to a record that is not on the loaded page. */
export function fetchPerson(id: number) {
  return apiGet<Person>(`/people/${id}`);
}

export function useCreatePerson() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: Partial<Person>) => apiPost<void>("/people", data),
    onSuccess: () => qc.invalidateQueries({ queryKey: peopleKeys.all }),
  });
}

export function useUpdatePerson() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: Partial<Person> }) =>
      apiPut<void>(`/people/${id}`, data),
    onSuccess: () => qc.invalidateQueries({ queryKey: peopleKeys.all }),
  });
}

export function useDeletePerson() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => apiDelete(`/people/${id}`),
    onSuccess: () => qc.invalidateQueries({ queryKey: peopleKeys.all }),
  });
}
