/**
 * React Query hooks for items.
 * Hexagon wiring: #1845 / ADR-030.
 */
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { itemUseCases } from "@/composition/item";
import type { Item as DomainItem, ItemInput } from "@/core/item/domain/item";
import type { Item } from "@/types";

export const itemsKeys = {
  all: ["items"] as const,
  detail: (id: number) => ["items", id] as const,
  byBudget: (id: number) => ["items", "budget", id] as const,
};

function toWire(i: DomainItem): Item {
  return i as Item;
}

export function useItems() {
  return useQuery({
    queryKey: itemsKeys.all,
    queryFn: async () => (await itemUseCases.list()).map(toWire),
  });
}

export function useItemsByBudget(idBudget: number | undefined) {
  return useQuery({
    queryKey: itemsKeys.byBudget(idBudget ?? 0),
    queryFn: async () => (await itemUseCases.listByBudget(idBudget!)).map(toWire),
    enabled: !!idBudget,
  });
}

export function useDiscountsAndSurcharges(idBudget: number | undefined) {
  return useQuery({
    queryKey: ["items", "budget", idBudget, "discounts-surcharges"],
    queryFn: async () => (await itemUseCases.listDiscountsAndSurcharges(idBudget!)).map(toWire),
    enabled: !!idBudget,
  });
}

export function useCreateItem() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: Partial<Item>) => itemUseCases.create(data as ItemInput),
    onSuccess: (_, variables) => {
      qc.invalidateQueries({ queryKey: itemsKeys.all });
      if (variables.fkIdBudget?.idBudget) {
        qc.invalidateQueries({
          queryKey: itemsKeys.byBudget(variables.fkIdBudget.idBudget),
        });
      }
    },
  });
}

export function useUpdateItem() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: Partial<Item> }) =>
      itemUseCases.update(id, data as ItemInput),
    onSuccess: () => qc.invalidateQueries({ queryKey: itemsKeys.all }),
  });
}

export function useDeleteItem() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => itemUseCases.delete(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: itemsKeys.all }),
  });
}
