import type { ItemGateway } from "./ports";
import type { ItemInput } from "../domain/item";

export function createItemUseCases(gateway: ItemGateway) {
  return {
    list: () => gateway.list(),
    listByBudget: (idBudget: number) => gateway.listByBudget(idBudget),
    listDiscountsAndSurcharges: (idBudget: number) => gateway.listDiscountsAndSurcharges(idBudget),
    create: (input: ItemInput) => gateway.create(input),
    update: (id: number, input: ItemInput) => gateway.update(id, input),
    delete: (id: number) => gateway.delete(id),
  };
}

export type ItemUseCases = ReturnType<typeof createItemUseCases>;
