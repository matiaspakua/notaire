/**
 * Server-side management lookup for pickers (#1340). Forms never load the whole
 * managements table (GET /managements?size=1000 left every case after the 1000th
 * out): they show the newest page and search by case number or by client.
 * Hexagon wiring: #1839 / ADR-030.
 */
import { keepPreviousData, useQuery } from "@tanstack/react-query";
import { managementUseCases } from "@/composition/management";
import { searchPeople } from "@/hooks/usePersonSearch";
import { managementsKeys, useManagementsPage } from "@/hooks/useManagements";
import type { Management as DomainManagement } from "@/core/management/domain/management";
import type { DeedManagement } from "@/types";

/** Newest managements listed before the user types. */
export const MANAGEMENT_PICKER_RECENT_SIZE = 20;
/** Clients whose managements one search loads (one request each). */
export const MANAGEMENT_PICKER_MAX_CLIENTS = 5;

function toWire(m: DomainManagement): DeedManagement {
  return {
    idManagement: m.idManagement,
    number: m.number,
    heading: m.heading,
    notes: m.notes,
    currentStatus: m.currentStatus,
    dateStart: m.dateStart,
    procedureCount: m.procedureCount,
  };
}

async function managementByNumber(number: string): Promise<DeedManagement[]> {
  const found = await managementUseCases.findByNumber(Number(number));
  return found ? [toWire(found)] : [];
}

/**
 * One free-text query: a number is a case number and also a client document
 * number; any text searches clients (GET /people/search) and lists their
 * managements (GET /managements/client/{id}). Newest first, no duplicates.
 */
export async function searchManagements(raw: string): Promise<DeedManagement[]> {
  const q = raw.trim();
  if (!q) return [];
  const [byNumber, people] = await Promise.all([
    /^\d+$/.test(q) ? managementByNumber(q) : Promise.resolve([]),
    searchPeople(q),
  ]);
  const byClient = await Promise.all(
    people
      .slice(0, MANAGEMENT_PICKER_MAX_CLIENTS)
      .map((p) => managementUseCases.listByClient(p.personId!)),
  );
  const seen = new Set<number>();
  const clientCases = byClient
    .flat()
    .map(toWire)
    .sort((a, b) => (b.idManagement ?? 0) - (a.idManagement ?? 0));
  return [...byNumber, ...clientCases].filter((g) => {
    if (g.idManagement == null || seen.has(g.idManagement)) return false;
    seen.add(g.idManagement);
    return true;
  });
}

export function useManagementSearch(query: string, enabled: boolean) {
  const q = query.trim();
  return useQuery({
    queryKey: ["managements", "picker-search", q],
    queryFn: () => searchManagements(q),
    enabled: enabled && q.length > 0,
    placeholderData: keepPreviousData,
  });
}

/** The newest managements, for an empty picker query. */
export function useRecentManagements(enabled: boolean) {
  return useManagementsPage({ page: 0, size: MANAGEMENT_PICKER_RECENT_SIZE }, { enabled });
}

/** One management by id, so a form shows its current case whatever page it is on. */
export function useManagement(id: number | undefined, enabled = true) {
  return useQuery({
    queryKey: managementsKeys.detail(id ?? -1),
    queryFn: async () => toWire(await managementUseCases.getById(id!)),
    enabled: enabled && id != null,
    staleTime: 60_000,
  });
}
