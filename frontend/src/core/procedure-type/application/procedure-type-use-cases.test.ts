// @vitest-environment node
/**
 * Core use cases for the procedure-type catalog (#1796 / ADR-030).
 * Fake gateway only — no React, Next, or api-client.
 */
import { describe, expect, it } from "vitest";
import type { ProcedureType } from "../domain/procedure-type";
import type { ProcedureTypeGateway, ProcedureTypeInput } from "./ports";
import { createProcedureTypeUseCases } from "./use-cases";

function fakeGateway(seed: ProcedureType[] = []): ProcedureTypeGateway & {
  store: ProcedureType[];
  inUseIds: Set<number>;
} {
  const store = [...seed];
  const inUseIds = new Set<number>();
  let nextId = Math.max(0, ...store.map((t) => t.id)) + 1;
  return {
    store,
    inUseIds,
    list: async () => [...store],
    searchByName: async (name: string) =>
      store.filter((s) => s.name.toLowerCase().includes(name.toLowerCase())),
    create: async (input: ProcedureTypeInput) => {
      const created: ProcedureType = { id: nextId++, name: input.name, notes: input.notes, enabled: input.enabled };
      store.push(created);
      return created;
    },
    update: async (id: number, input: ProcedureTypeInput) => {
      const idx = store.findIndex((t) => t.id === id);
      if (idx < 0) throw new Error(`missing ${id}`);
      const updated = { ...store[idx], ...input };
      store[idx] = updated;
      return updated;
    },
    delete: async (id: number) => {
      const idx = store.findIndex((t) => t.id === id);
      if (idx < 0) throw new Error(`missing ${id}`);
      store.splice(idx, 1);
    },
    isInUse: async (id: number) => inUseIds.has(id),
    assignWorkflow: async (id: number, workflowDefinitionId: number | null) => {
      const idx = store.findIndex((t) => t.id === id);
      if (idx < 0) throw new Error(`missing ${id}`);
      store[idx] = { ...store[idx], workflowDefinitionId };
      return store[idx];
    },
  };
}

describe("procedure-type use cases (#1796)", () => {
  it("lists and searches procedure types", async () => {
    const gateway = fakeGateway([
      { id: 1, name: "Sale", enabled: true },
      { id: 2, name: "Mortgage" },
    ]);
    const uc = createProcedureTypeUseCases(gateway);
    await expect(uc.list()).resolves.toHaveLength(2);
    await expect(uc.searchByName("sale")).resolves.toEqual([{ id: 1, name: "Sale", enabled: true }]);
  });

  it("creates, updates, deletes, reports in-use, and assigns workflow", async () => {
    const gateway = fakeGateway([{ id: 1, name: "Sale", enabled: true }]);
    gateway.inUseIds.add(1);
    const uc = createProcedureTypeUseCases(gateway);

    await expect(uc.isInUse(1)).resolves.toBe(true);
    const created = await uc.create({ name: "Gift", enabled: true });
    expect(created.id).toBeGreaterThan(0);

    await expect(uc.update(created.id, { name: "Gift deed", enabled: true })).resolves.toMatchObject({
      name: "Gift deed",
    });
    await expect(uc.assignWorkflow(created.id, 9)).resolves.toMatchObject({ workflowDefinitionId: 9 });
    await expect(uc.assignWorkflow(created.id, null)).resolves.toMatchObject({ workflowDefinitionId: null });

    await uc.delete(created.id);
    await expect(uc.list()).resolves.toHaveLength(1);
  });
});
