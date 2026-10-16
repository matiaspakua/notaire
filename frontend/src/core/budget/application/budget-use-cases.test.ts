// @vitest-environment node
/**
 * Core use cases for budgets (#1827 / ADR-030).
 */
import { describe, expect, it } from "vitest";
import type { Budget } from "../domain/budget";
import type { BudgetGateway, BudgetInput, BudgetPage, BudgetPageRequest } from "./ports";
import { createBudgetUseCases } from "./use-cases";

function fakeGateway(seed: Budget[] = []): BudgetGateway & { store: Budget[] } {
  const store = [...seed];
  let nextId = Math.max(0, ...store.map((b) => b.idBudget ?? 0)) + 1;
  return {
    store,
    getById: async (id: number) => {
      const found = store.find((b) => b.idBudget === id);
      if (!found) throw new Error(`missing ${id}`);
      return found;
    },
    findById: async (id: number) => store.find((b) => b.idBudget === id) ?? null,
    listPage: async (request: BudgetPageRequest): Promise<BudgetPage> => {
      const start = request.page * request.size;
      const content = store.slice(start, start + request.size);
      return {
        content,
        number: request.page,
        size: request.size,
        totalElements: store.length,
        totalPages: Math.ceil(store.length / request.size) || 0,
      };
    },
    listByPerson: async (personId: number) =>
      store.filter((b) => b.person?.personId === personId),
    listByStatus: async (status: string) => store.filter((b) => b.status === status),
    getSummary: async (id: number) => ({ idBudget: id, total: 0, pendingBalance: 0 }),
    create: async (input: BudgetInput) => {
      const created = { ...input, idBudget: nextId++, version: 0 };
      store.push(created);
      return created;
    },
    update: async (id: number, input: BudgetInput) => {
      const idx = store.findIndex((b) => b.idBudget === id);
      if (idx < 0) throw new Error(`missing ${id}`);
      store[idx] = { ...store[idx], ...input };
    },
    delete: async (id: number) => {
      const idx = store.findIndex((b) => b.idBudget === id);
      if (idx < 0) throw new Error(`missing ${id}`);
      store.splice(idx, 1);
    },
    loadItemsFromTemplate: async () => [{ idItem: 1, name: "Honorarios", value: 100 }],
    addItemsFromCatalog: async (_id, idItems) => idItems.map((idItem) => ({ idItem, name: `Item ${idItem}` })),
  };
}

describe("budget use cases (#1827)", () => {
  it("lists a page and loads by id from the gateway", async () => {
    const gateway = fakeGateway([
      { idBudget: 1, heading: "One", status: "PENDIENTE" },
      { idBudget: 2, heading: "Two", status: "BORRADOR" },
    ]);
    const uc = createBudgetUseCases(gateway);
    await expect(uc.listPage({ page: 0, size: 1 })).resolves.toMatchObject({
      content: [{ idBudget: 1 }],
      totalElements: 2,
    });
    await expect(uc.getById(2)).resolves.toMatchObject({ idBudget: 2, heading: "Two" });
    await expect(uc.findById(99)).resolves.toBeNull();
  });

  it("filters by status and person, creates and deletes", async () => {
    const gateway = fakeGateway([
      { idBudget: 1, status: "PENDIENTE", person: { personId: 10 } },
      { idBudget: 2, status: "BORRADOR", person: { personId: 10 } },
    ]);
    const uc = createBudgetUseCases(gateway);

    await expect(uc.listByStatus("PENDIENTE")).resolves.toHaveLength(1);
    await expect(uc.listByPerson(10)).resolves.toHaveLength(2);

    const created = await uc.create({ heading: "New", status: "BORRADOR" });
    expect(created.idBudget).toBeDefined();
    expect(gateway.store).toHaveLength(3);

    await uc.delete(1);
    await expect(uc.listPage({ page: 0, size: 10 })).resolves.toMatchObject({ totalElements: 2 });
  });
});
