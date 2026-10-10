/**
 * Server-side budget lookup for pickers (#1340). Forms never load the whole
 * budgets table (GET /presupuestos?size=1000 left the oldest of ~1300 out):
 * they show the newest page and search by budget number or by client.
 */
import { keepPreviousData, useQuery } from "@tanstack/react-query";
import { apiGet } from "@/lib/api-client";
import { searchPeople } from "@/hooks/usePersonSearch";
import { presupuestosKeys, usePresupuestosPage } from "@/hooks/usePresupuestos";
import type { Presupuesto } from "@/types";

/** Newest budgets listed before the user types. */
export const BUDGET_PICKER_RECENT_SIZE = 20;
/** Clients whose budgets one search loads (one request each). */
export const BUDGET_PICKER_MAX_CLIENTS = 5;

async function budgetById(id: string): Promise<Presupuesto[]> {
  try {
    return [await apiGet<Presupuesto>(`/presupuestos/${id}`)];
  } catch {
    return []; // 404: no budget with that number
  }
}

/**
 * One free-text query: a number is a budget number and also a client document
 * number; any text searches clients (GET /people/search) and lists their
 * budgets (GET /presupuestos/persona/{id}). Newest budget first, no duplicates.
 */
export async function searchBudgets(raw: string): Promise<Presupuesto[]> {
  const q = raw.trim();
  if (!q) return [];
  const [byNumber, people] = await Promise.all([/^\d+$/.test(q) ? budgetById(q) : Promise.resolve([]), searchPeople(q)]);
  const byClient = await Promise.all(
    people
      .slice(0, BUDGET_PICKER_MAX_CLIENTS)
      .map((p) => apiGet<Presupuesto[]>(`/presupuestos/persona/${p.personId}`)),
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
    queryKey: ["presupuestos", "picker-search", q],
    queryFn: () => searchBudgets(q),
    enabled: enabled && q.length > 0,
    placeholderData: keepPreviousData,
  });
}

/** The newest budgets, for an empty picker query. */
export function useRecentBudgets(enabled: boolean) {
  return usePresupuestosPage({ page: 0, size: BUDGET_PICKER_RECENT_SIZE }, { enabled });
}

/** One budget by id, so a form shows its current budget whatever page it is on. */
export function useBudget(id: number | undefined, enabled = true) {
  return useQuery({
    queryKey: presupuestosKeys.detail(id ?? -1),
    queryFn: () => apiGet<Presupuesto>(`/presupuestos/${id}`),
    enabled: enabled && id != null,
    staleTime: 60_000,
  });
}
