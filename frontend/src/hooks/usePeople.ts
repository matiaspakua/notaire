/**
 * React Query hooks for people.
 * Hexagon wiring: #1823 / ADR-030.
 * Endpoint: GET/POST/PUT/DELETE /api/v1/people
 */
import { keepPreviousData, useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { personUseCases } from "@/composition/person";
import type { Person as DomainPerson } from "@/core/person/domain/person";
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

function toWire(p: DomainPerson): Person {
  return {
    personId: p.id,
    firstName: p.firstName,
    lastName: p.lastName,
    nationality: p.nationality,
    identificationNumber: p.identificationNumber,
    taxId: p.taxId,
    sex: p.sex,
    birthDate: p.birthDate,
    maritalStatus: p.maritalStatus,
    marriageCount: p.marriageCount,
    occupation: p.occupation,
    address: p.address,
    phone: p.phone,
    email: p.email,
    notaryRegistrationNumber: p.notaryRegistrationNumber,
    isClient: p.isClient,
    identificationTypeId: p.identificationTypeId,
    fkIdIdentificationType:
      p.identificationTypeId != null || p.identificationTypeName
        ? {
            idIdentificationType: p.identificationTypeId ?? undefined,
            name: p.identificationTypeName,
            characters: p.identificationTypeCharacters,
          }
        : undefined,
    version: p.version,
  };
}

function toInput(data: Partial<Person>) {
  return {
    firstName: data.firstName,
    lastName: data.lastName,
    nationality: data.nationality,
    identificationNumber: data.identificationNumber,
    taxId: data.taxId,
    sex: data.sex,
    birthDate: data.birthDate,
    maritalStatus: data.maritalStatus,
    marriageCount: data.marriageCount,
    occupation: data.occupation,
    address: data.address,
    phone: data.phone,
    email: data.email,
    notaryRegistrationNumber: data.notaryRegistrationNumber,
    isClient: data.isClient,
    identificationTypeId: data.identificationTypeId,
    version: data.version,
  };
}

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
    queryFn: async () =>
      (
        await personUseCases.search({
          firstName: settled.firstName || undefined,
          lastName: settled.lastName || undefined,
          identificationNumber: settled.identificationNumber || undefined,
          isClient: settled.onlyClients || undefined,
        })
      ).map(toWire),
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
    queryFn: async () => {
      const page = await personUseCases.listPage({
        page: params.page,
        size: params.size,
        sort: PEOPLE_SORT,
      });
      return {
        content: page.content.map(toWire),
        totalElements: page.totalElements,
        totalPages: page.totalPages,
        number: page.number,
        size: page.size,
      };
    },
    placeholderData: keepPreviousData,
  });
}

/** One person by id, for links to a record that is not on the loaded page. */
export function fetchPerson(id: number) {
  return personUseCases.getById(id).then(toWire);
}

export function useCreatePerson() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: Partial<Person>) => personUseCases.create(toInput(data)),
    onSuccess: () => qc.invalidateQueries({ queryKey: peopleKeys.all }),
  });
}

export function useUpdatePerson() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: Partial<Person> }) =>
      personUseCases.update(id, toInput(data)),
    onSuccess: () => qc.invalidateQueries({ queryKey: peopleKeys.all }),
  });
}

export function useDeletePerson() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => personUseCases.delete(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: peopleKeys.all }),
  });
}
