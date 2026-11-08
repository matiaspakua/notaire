/**
 * Server-side person lookup for pickers (#1340 slice 3). Forms never load the
 * whole people table: they show the newest page and search GET /people/search.
 * Hexagon wiring: #1823 / ADR-030.
 */
import { keepPreviousData, useQuery } from "@tanstack/react-query";
import { personUseCases } from "@/composition/person";
import { fetchPerson, peopleKeys, usePeoplePage } from "@/hooks/usePeople";
import type { Person as DomainPerson } from "@/core/person/domain/person";
import type { Person } from "@/types";

export type PersonSearchParams = { firstName?: string; lastName?: string; identificationNumber?: string };

/** Newest people listed before the user types. */
export const PICKER_RECENT_SIZE = 20;

/**
 * Turns one free-text query into /people/search calls. The backend ANDs first
 * and last name and ignores the rest when a document number is given, so:
 * a number searches the document; one word is tried as first and as last name;
 * several words are "first + rest" and the whole text as a (compound) last name.
 */
export function buildPersonSearchQueries(raw: string): PersonSearchParams[] {
  const q = raw.trim().replace(/\s+/g, " ");
  if (!q) return [];
  if (/^\d+$/.test(q)) return [{ identificationNumber: q }];
  const words = q.split(" ");
  if (words.length === 1) return [{ firstName: q }, { lastName: q }];
  return [{ firstName: words[0], lastName: words.slice(1).join(" ") }, { lastName: q }];
}

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

export async function searchPeople(raw: string): Promise<Person[]> {
  const queries = buildPersonSearchQueries(raw);
  const results = await Promise.all(queries.map((params) => personUseCases.search(params)));
  const seen = new Set<number>();
  return results
    .flat()
    .map(toWire)
    .filter((p) => {
      if (p.personId == null || seen.has(p.personId)) return false;
      seen.add(p.personId);
      return true;
    });
}

export function usePersonSearch(query: string, enabled: boolean) {
  const q = query.trim();
  return useQuery({
    queryKey: ["people", "picker-search", q],
    queryFn: () => searchPeople(q),
    enabled: enabled && q.length > 0,
    placeholderData: keepPreviousData,
  });
}

/** The newest people, for an empty picker query. */
export function useRecentPeople(enabled: boolean) {
  return usePeoplePage({ page: 0, size: PICKER_RECENT_SIZE }, { enabled });
}

/** One person by id, so an edit form shows its current person whatever page it is on. */
export function usePerson(id: number | undefined, enabled = true) {
  return useQuery({
    queryKey: peopleKeys.detail(id ?? -1),
    queryFn: () => fetchPerson(id!),
    enabled: enabled && id != null,
    staleTime: 60_000,
  });
}
