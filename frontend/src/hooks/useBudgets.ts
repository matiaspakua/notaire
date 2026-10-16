/**
 * React Query hooks for budgets.
 * Hexagon wiring: #1827 / ADR-030.
 */
import { keepPreviousData, useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { budgetUseCases } from "@/composition/budget";
import { itemsKeys } from "@/hooks/useItems";
import type { Budget as DomainBudget, BudgetItem, BudgetSummary as DomainSummary } from "@/core/budget/domain/budget";
import type { Budget, BudgetSummary } from "@/types/api";
import type { Item } from "@/types";

export const budgetsKeys = {
  all: ["budgets"] as const,
  detail: (id: number) => ["budgets", id] as const,
  byPerson: (id: number) => ["budgets", "person", id] as const,
  summary: (id: number) => ["budgets", id, "summary"] as const,
  page: (params: { page: number; size: number }) => ["budgets", "page", params] as const,
  byStatus: (status: string) => ["budgets", "search", status] as const,
};

/** Sort for the budgets list: newest first (#1340). */
export const BUDGETS_SORT = "idBudget,desc";

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

function toSummary(s: DomainSummary): BudgetSummary {
  return {
    idBudget: s.idBudget,
    idManagement: s.idManagement,
    managementHeading: s.managementHeading,
    numberBudget: s.numberBudget,
    numberManagement: s.numberManagement,
    pendingBalance: s.pendingBalance,
    total: s.total,
    payments: s.payments as BudgetSummary["payments"],
  };
}

function toItem(i: BudgetItem): Item {
  return {
    idItem: i.idItem,
    name: i.name,
    value: i.value,
    percentage: i.percentage,
    notes: i.notes,
    type: i.type as Item["type"],
    reason: i.reason,
    fixedConcept: i.fixedConcept,
    fkIdBudget: i.budgetId != null ? { idBudget: i.budgetId } : undefined,
  };
}

/** One server page of GET /budgets (#1340); the list is never loaded whole. */
export function useBudgetsPage(params: { page: number; size: number }, options: { enabled?: boolean } = {}) {
  return useQuery({
    queryKey: budgetsKeys.page(params),
    queryFn: async () => {
      const page = await budgetUseCases.listPage({
        page: params.page,
        size: params.size,
        sort: BUDGETS_SORT,
      });
      return {
        ...page,
        content: page.content.map(toWire),
      };
    },
    placeholderData: keepPreviousData,
    enabled: options.enabled ?? true,
  });
}

/** CU60 — budgets filtered by status. */
export function useBudgetsByStatus(status: string, options: { enabled?: boolean } = {}) {
  return useQuery({
    queryKey: budgetsKeys.byStatus(status),
    queryFn: async () => (await budgetUseCases.listByStatus(status)).map(toWire),
    enabled: options.enabled ?? true,
  });
}

/** CU47 - Budget financial summary: management, total, outstanding balance, and payments. */
export function useBudgetSummary(id: number | null) {
  return useQuery({
    queryKey: budgetsKeys.summary(id ?? 0),
    queryFn: async () => toSummary(await budgetUseCases.getSummary(id!)),
    enabled: id !== null,
  });
}

interface CreateBudgetInput {
  data: Partial<Budget>;
  procedureTypeId?: number;
}

interface CreateBudgetResult {
  budget: Budget;
  itemsLoaded: boolean;
}

/**
 * CU01/CU39 - Creates the budget and, when a procedure type is given, loads that type's template items into it.
 * A missing template must not undo the creation, so the second step only reports `itemsLoaded`.
 */
export function useCreateBudget() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: async ({ data, procedureTypeId }: CreateBudgetInput): Promise<CreateBudgetResult> => {
      const budget = toWire(await budgetUseCases.create(data));
      if (procedureTypeId === undefined) return { budget, itemsLoaded: false };
      try {
        await budgetUseCases.loadItemsFromTemplate(budget.idBudget!, procedureTypeId);
        return { budget, itemsLoaded: true };
      } catch {
        return { budget, itemsLoaded: false };
      }
    },
    onSuccess: (result) => {
      qc.invalidateQueries({ queryKey: budgetsKeys.all });
      if (result.itemsLoaded) qc.invalidateQueries({ queryKey: itemsKeys.byBudget(result.budget.idBudget!) });
    },
  });
}

export function useUpdateBudget() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: Partial<Budget> }) => budgetUseCases.update(id, data),
    onSuccess: () => qc.invalidateQueries({ queryKey: budgetsKeys.all }),
  });
}

export function useDeleteBudget() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => budgetUseCases.delete(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: budgetsKeys.all }),
  });
}

/** CU39 - Loads budget items from the procedure type's template. */
export function useLoadItemsFromTemplate() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: async ({ idBudget, procedureTypeId }: { idBudget: number; procedureTypeId: number }) =>
      (await budgetUseCases.loadItemsFromTemplate(idBudget, procedureTypeId)).map(toItem),
    onSuccess: (_, variables) => qc.invalidateQueries({ queryKey: itemsKeys.byBudget(variables.idBudget) }),
  });
}

/** CU71 - Adds copies of existing catalog items to the budget. */
export function useAddItemsFromCatalog() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: async ({ idBudget, idItems }: { idBudget: number; idItems: number[] }) =>
      (await budgetUseCases.addItemsFromCatalog(idBudget, idItems)).map(toItem),
    onSuccess: (_, variables) => qc.invalidateQueries({ queryKey: itemsKeys.byBudget(variables.idBudget) }),
  });
}
