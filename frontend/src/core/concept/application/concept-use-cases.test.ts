// @vitest-environment node
/**
 * Core use cases for the concept catalog (#1781 / ADR-030).
 */
import { describe, expect, it } from "vitest";
import type { Concept } from "../domain/concept";
import type { ConceptGateway, ConceptInput } from "./ports";
import { createConceptUseCases } from "./use-cases";

function fakeGateway(seed: Concept[] = []): ConceptGateway & {
  store: Concept[];
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
    create: async (input: ConceptInput) => {
      const created: Concept = {
        id: nextId++,
        name: input.name,
        value: input.value,
        percentage: input.percentage,
        fixed: input.fixed,
        enabled: input.enabled,
        version: 0,
      };
      store.push(created);
      return created;
    },
    update: async (id: number, input: ConceptInput) => {
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
  };
}

describe("concept use cases (#1781)", () => {
  it("lists concepts from the gateway", async () => {
    const gateway = fakeGateway([
      { id: 1, name: "Honorarium", value: 100 },
      { id: 2, name: "Stamp", value: 50 },
    ]);
    const uc = createConceptUseCases(gateway);
    await expect(uc.list()).resolves.toEqual([
      { id: 1, name: "Honorarium", value: 100 },
      { id: 2, name: "Stamp", value: 50 },
    ]);
  });

  it("searches, creates, updates, deletes and reports in-use", async () => {
    const gateway = fakeGateway([{ id: 1, name: "Honorarium", value: 100 }]);
    gateway.inUseIds.add(1);
    const uc = createConceptUseCases(gateway);

    await expect(uc.searchByName("honor")).resolves.toHaveLength(1);
    await expect(uc.isInUse(1)).resolves.toBe(true);

    const created = await uc.create({ name: "Fee", value: 25 });
    expect(created.id).toBeGreaterThan(0);

    await expect(uc.update(created.id, { name: "Fee updated", value: 30 })).resolves.toMatchObject({
      name: "Fee updated",
      value: 30,
    });

    await uc.delete(created.id);
    await expect(uc.list()).resolves.toHaveLength(1);
  });
});
