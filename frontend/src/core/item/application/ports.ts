import type { Item, ItemInput } from "../domain/item";

export interface ItemGateway {
  list(): Promise<Item[]>;
  listByBudget(idBudget: number): Promise<Item[]>;
  listDiscountsAndSurcharges(idBudget: number): Promise<Item[]>;
  create(input: ItemInput): Promise<void>;
  update(id: number, input: ItemInput): Promise<void>;
  delete(id: number): Promise<void>;
}
