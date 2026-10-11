import { keepPreviousData, useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { apiGet, apiGetPage, apiPost, apiPut, apiDelete } from "@/lib/api-client";
import { itemsKeys } from "@/hooks/useItems";
import type { Budget, BudgetSummary } from "@/types/api";
import type { Item } from "@/types";

export const budgetsKeys = {
  all: ["budgets"] as const,
  detail: (id: number) => ["budgets", id] as const,
  byPerson: (id: number) => ["budgets", "person", id] as const,
  summary: (id: number) => ["budgets", id, "summary"] as const,
  page: (params: { page: number; size: number }) => ["budgets", "page", params] as const,
};

/** Sort for the budgets list: newest first (#1340). */
export const BUDGETS_SORT = "idBudget,desc";

/** One server page of GET /budgets (#1340); the list is never loaded whole. */
export function useBudgetsPage(params: { page: number; size: number }, options: { enabled?: boolean } = {}) {
  return useQuery({
    queryKey: budgetsKeys.page(params),
    queryFn: () =>
      apiGetPage<Budget>("/budgets", { page: params.page, size: params.size, sort: BUDGETS_SORT }),
    placeholderData: keepPreviousData,
    enabled: options.enabled ?? true,
  });
}

/** CU47 - Consultar Payment: financial summary (gestión, total, saldo, pagos) for a presupuesto. */
export function useBudgetSummary(id: number | null) {
  return useQuery({
    queryKey: budgetsKeys.summary(id ?? 0),
    queryFn: () => apiGet<BudgetSummary>(`/budgets/${id}/summary`),
    enabled: id !== null,
  });
}

interface CreateBudgetInput {
  data: Partial<Budget>;
  tipoTramiteId?: number;
}

interface CreateBudgetResult {
  presupuesto: Budget;
  itemsLoaded: boolean;
}

/**
 * CU01/CU39 - Creates the presupuesto and, when a tipo de trámite is given, loads that type's template items into it.
 * A missing template must not undo the creation, so the second step only reports `itemsLoaded`.
 */
export function useCreateBudget() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: async ({ data, tipoTramiteId }: CreateBudgetInput): Promise<CreateBudgetResult> => {
      const presupuesto = await apiPost<Budget>("/budgets", data);
      if (tipoTramiteId === undefined) return { presupuesto, itemsLoaded: false };
      try {
        await apiPost<Item[]>(
          `/budgets/${presupuesto.idBudget}/items-from-template?procedureTypeId=${tipoTramiteId}`,
          undefined,
        );
        return { presupuesto, itemsLoaded: true };
      } catch {
        return { presupuesto, itemsLoaded: false };
      }
    },
    onSuccess: (result) => {
      qc.invalidateQueries({ queryKey: budgetsKeys.all });
      if (result.itemsLoaded) qc.invalidateQueries({ queryKey: itemsKeys.byBudget(result.presupuesto.idBudget!) });
    },
  });
}

export function useUpdateBudget() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: Partial<Budget> }) =>
      apiPut<void>(`/budgets/${id}`, data),
    onSuccess: () => qc.invalidateQueries({ queryKey: budgetsKeys.all }),
  });
}

export function useDeleteBudget() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => apiDelete(`/budgets/${id}`),
    onSuccess: () => qc.invalidateQueries({ queryKey: budgetsKeys.all }),
  });
}

/** CU39 - Cargar los ítems del presupuesto desde la plantilla del tipo de trámite. */
export function useLoadItemsFromTemplate() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ idBudget, tipoTramiteId }: { idBudget: number; tipoTramiteId: number }) =>
      apiPost<Item[]>(`/budgets/${idBudget}/items-from-template?procedureTypeId=${tipoTramiteId}`, undefined),
    onSuccess: (_, variables) =>
      qc.invalidateQueries({ queryKey: itemsKeys.byBudget(variables.idBudget) }),
  });
}

/** CU71 - Agregar al presupuesto copias de ítems existentes del catálogo. */
export function useAddItemsFromCatalog() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ idBudget, idItems }: { idBudget: number; idItems: number[] }) =>
      apiPost<Item[]>(`/budgets/${idBudget}/items-from-catalog`, idItems),
    onSuccess: (_, variables) =>
      qc.invalidateQueries({ queryKey: itemsKeys.byBudget(variables.idBudget) }),
  });
}
