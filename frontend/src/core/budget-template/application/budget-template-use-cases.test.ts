// @vitest-environment node
import { describe, expect, it } from "vitest";
import type { BudgetTemplate } from "../domain/budget-template";
import type { BudgetTemplateGateway } from "./ports";
import { createBudgetTemplateUseCases } from "./use-cases";

function fake(seed: BudgetTemplate[] = []): BudgetTemplateGateway & { store: BudgetTemplate[] } {
  const store = [...seed];
  return {
    store,
    list: async () => [...store],
    create: async (input) => {
      store.push({ ...input });
    },
    update: async (input) => {
      const idx = store.findIndex(
        (r) => r.procedureTypeId === input.procedureTypeId && r.conceptId === input.conceptId,
      );
      store[idx] = { ...store[idx], ...input };
    },
    delete: async (procedureTypeId, conceptId) => {
      const idx = store.findIndex(
        (r) => r.procedureTypeId === procedureTypeId && r.conceptId === conceptId,
      );
      store.splice(idx, 1);
    },
  };
}

describe("budget-template use cases (#1788)", () => {
  it("lists creates updates and deletes", async () => {
    const gateway = fake([]);
    const uc = createBudgetTemplateUseCases(gateway);
    await uc.create({ procedureTypeId: 1, conceptId: 2, notes: "n" });
    await expect(uc.list()).resolves.toHaveLength(1);
    await uc.update({ procedureTypeId: 1, conceptId: 2, notes: "m" });
    expect(gateway.store[0].notes).toBe("m");
    await uc.delete(1, 2);
    await expect(uc.list()).resolves.toHaveLength(0);
  });
});
