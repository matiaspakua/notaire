/**
 * HTTP adapter for items (#1845 / ADR-030).
 */
import { apiDelete, apiGet, apiPost, apiPut } from "@/lib/api-client";
import type { Item, ItemInput } from "@/core/item/domain/item";
import type { ItemGateway } from "@/core/item/application/ports";

export const itemHttpGateway: ItemGateway = {
  list: () => apiGet<Item[]>("/items"),
  listByBudget: (idBudget) => apiGet<Item[]>(`/items/budget/${idBudget}`),
  listDiscountsAndSurcharges: (idBudget) =>
    apiGet<Item[]>(`/items/budget/${idBudget}/discounts-surcharges`),
  create: async (input: ItemInput) => { await apiPost<void>("/items", input); },
  update: async (id, input) => { await apiPut<void>(`/items/${id}`, input); },
  delete: async (id) => { await apiDelete(`/items/${id}`); },
};
