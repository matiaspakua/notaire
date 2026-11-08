/**
 * Server-side budget lookup for pickers (#1340). Forms never load the whole
 * budgets table (GET /budgets?size=1000 left the oldest of ~1300 out):
 * they show the newest page and search by budget number or by client.
 * Hexagon wiring: #1827 / ADR-030.
 */
import { keepPreviousData, useQuery } from "@tanstack/react-query";
import { budgetUseCases } from "@/composition/budget";
import { searchPeople } from "@/hooks/usePersonSearch";
import { budgetsKeys, useBudgetsPage } from "@/hooks/useBudgets";
import type { Budget as DomainBudget } from "@/core/budget/domain/budget";
import type { Budget } from "@/types";

/** Newest budgets listed before the user types. */
export const BUDGET_PICKER_RECENT_SIZE = 20;
/** Clients whose budgets one search loads (one request each). */
export const BUDGET_PICKER_MAX_CLIENTS = 5;

function toWire(b: DomainBudget): Budget {
  return {
    idBudget: b.idBudget,
    number: b.number,
    heading: b.heading,
    status: b.status,
    propertyAmount: b.propertyAmount,
    notes: b.notes,
    date: b.date,
    person: b.person,
    version: b.version,
  };
}

async function budgetById(id: string): Promise<Budget[]> {
  const found = await budgetUseCases.findById(Number(id));
  return found ? [toWire(found)] : [];
}

/**
 * One free-text query: a number is a budget number and also a client document
 * number; any text searches clients (GET /people/search) and lists their
 * budgets (GET /budgets/person/{id}). Newest budget first, no duplicates.
 */
export async function searchBudgets(raw: string): Promise<Budget[]> {
  const q = raw.trim();
  if (!q) return [];
  const [byNumber, people] = await Promise.all([
    /^\d+$/.test(q) ? budgetById(q) : Promise.resolve([]),
    searchPeople(q),
  ]);
  const byClient = await Promise.all(
    people
      .slice(0, BUDGET_PICKER_MAX_CLIENTS)
      .map(async (p) => (await budgetUseCases.listByPerson(p.personId!)).map(toWire)),
  );
  const seen = new Set<number>();
  const clientBudgets = byClient.flat().sort((a, b) => (b.idBudget ?? 0) - (a.idBudget ?? 0));
  return [...byNumber, ...clientBudgets].filter((b) => {
    if (b.idBudget == null || seen.has(b.idBudget)) return false;
    seen.add(b.idBudget);
    return true;
  });
}

export function useBudgetSearch(query: string, enabled: boolean) {
  const q = query.trim();
  return useQuery({
    queryKey: ["budgets", "picker-search", q],
    queryFn: () => searchBudgets(q),
    enabled: enabled && q.length > 0,
    placeholderData: keepPreviousData,
  });
}

/** The newest budgets, for an empty picker query. */
export function useRecentBudgets(enabled: boolean) {
  return useBudgetsPage({ page: 0, size: BUDGET_PICKER_RECENT_SIZE }, { enabled });
}

/** One budget by id, so a form shows its current budget whatever page it is on. */
export function useBudget(id: number | undefined, enabled = true) {
  return useQuery({
    queryKey: budgetsKeys.detail(id ?? -1),
    queryFn: async () => toWire(await budgetUseCases.getById(id!)),
    enabled: enabled && id != null,
    staleTime: 60_000,
  });
}

/** Lookup by id that treats 404 as null (budgets dashboard search). */
export function useBudgetFind(id: number | null, enabled = true) {
  return useQuery({
    queryKey: ["budgets", id ?? 0, "search"],
    queryFn: async () => {
      const found = await budgetUseCases.findById(id!);
      return found ? toWire(found) : null;
    },
    enabled: enabled && id !== null,
  });
}
