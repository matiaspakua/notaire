// @vitest-environment node
/**
 * Core use cases for the folio-type catalog (#1785 / ADR-030).
 * Fake gateway only — no React, Next, or api-client.
 */
import { describe, expect, it } from "vitest";
import type { FolioType } from "../domain/folio-type";
import type { FolioTypeGateway, FolioTypeInput } from "./ports";
import { createFolioTypeUseCases } from "./use-cases";

function fakeGateway(seed: FolioType[] = []): FolioTypeGateway & {
  store: FolioType[];
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
    create: async (input: FolioTypeInput) => {
      const created: FolioType = {
        id: nextId++,
        name: input.name,
        isAuxiliary: input.isAuxiliary,
        notes: input.notes,
        enabled: input.enabled,
        version: input.version,
      };
      store.push(created);
      return created;
    },
    update: async (id: number, input: FolioTypeInput) => {
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

describe("folio-type use cases (#1785)", () => {
  it("lists folio types from the gateway", async () => {
    const gateway = fakeGateway([
      { id: 1, name: "Ordinary", enabled: true },
      { id: 2, name: "Auxiliary", isAuxiliary: true },
    ]);
    const uc = createFolioTypeUseCases(gateway);
    await expect(uc.list()).resolves.toEqual([
      { id: 1, name: "Ordinary", enabled: true },
      { id: 2, name: "Auxiliary", isAuxiliary: true },
    ]);
  });

  it("searches, creates, updates, deletes and reports in-use", async () => {
    const gateway = fakeGateway([{ id: 1, name: "Ordinary", enabled: true, version: 3 }]);
    gateway.inUseIds.add(1);
    const uc = createFolioTypeUseCases(gateway);

    await expect(uc.searchByName("ord")).resolves.toHaveLength(1);
    await expect(uc.isInUse(1)).resolves.toBe(true);

    const created = await uc.create({ name: "Special", isAuxiliary: false });
    expect(created.id).toBeGreaterThan(0);

    await expect(
      uc.update(1, { name: "Ordinary renamed", isAuxiliary: false, version: 3, enabled: true }),
    ).resolves.toMatchObject({
      name: "Ordinary renamed",
      version: 3,
    });

    await uc.delete(created.id);
    await expect(uc.list()).resolves.toHaveLength(1);
  });
});
