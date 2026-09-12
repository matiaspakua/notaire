import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { apiGet, apiPost, apiPut, apiDelete } from "@/lib/api-client";
import type { Item } from "@/types";

export const itemsKeys = {
  all: ["items"] as const,
  detail: (id: number) => ["items", id] as const,
  byPresupuesto: (id: number) => ["items", "presupuesto", id] as const,
};

export function useItems() {
  return useQuery({
    queryKey: itemsKeys.all,
    queryFn: () => apiGet<Item[]>("/items"),
  });
}

export function useItemsByPresupuesto(idPresupuesto: number | undefined) {
  return useQuery({
    queryKey: itemsKeys.byPresupuesto(idPresupuesto ?? 0),
    queryFn: () => apiGet<Item[]>(`/items/presupuesto/${idPresupuesto}`),
    enabled: !!idPresupuesto,
  });
}

export function useDescuentosYRecargos(idPresupuesto: number | undefined) {
  return useQuery({
    queryKey: ["items", "presupuesto", idPresupuesto, "descuentos-recargos"],
    queryFn: () => apiGet<Item[]>(`/items/presupuesto/${idPresupuesto}/descuentos-recargos`),
    enabled: !!idPresupuesto,
  });
}

export function useCreateItem() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: Partial<Item>) => apiPost<void>("/items", data),
    onSuccess: (_, variables) => {
      qc.invalidateQueries({ queryKey: itemsKeys.all });
      if (variables.fkIdBudget?.idBudget) {
        qc.invalidateQueries({
          queryKey: itemsKeys.byPresupuesto(variables.fkIdBudget.idBudget),
        });
      }
    },
  });
}

export function useUpdateItem() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: Partial<Item> }) =>
      apiPut<void>(`/items/${id}`, data),
    onSuccess: () => qc.invalidateQueries({ queryKey: itemsKeys.all }),
  });
}

export function useDeleteItem() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => apiDelete(`/items/${id}`),
    onSuccess: () => qc.invalidateQueries({ queryKey: itemsKeys.all }),
  });
}
