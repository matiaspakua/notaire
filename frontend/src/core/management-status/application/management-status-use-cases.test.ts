// @vitest-environment node
/**
 * Core use cases for the management-status catalog (#1769 / ADR-030).
 * Fake gateway only — no React, Next, or api-client.
 */
import { describe, expect, it } from "vitest";
import type { ManagementStatus } from "../domain/management-status";
import type { ManagementStatusGateway, ManagementStatusInput } from "./ports";
import { createManagementStatusUseCases } from "./use-cases";

function fakeGateway(seed: ManagementStatus[] = []): ManagementStatusGateway & {
  store: ManagementStatus[];
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
    create: async (input: ManagementStatusInput) => {
      const created: ManagementStatus = {
        id: nextId++,
        name: input.name,
        notes: input.notes,
        version: 0,
      };
      store.push(created);
      return created;
    },
    update: async (id: number, input: ManagementStatusInput) => {
      const idx = store.findIndex((t) => t.id === id);
      if (idx < 0) throw new Error(`missing ${id}`);
      const updated = { ...store[idx], name: input.name, notes: input.notes, version: input.version };
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

describe("management-status use cases (#1769)", () => {
  it("lists statuses from the gateway", async () => {
    const gateway = fakeGateway([
      { id: 1, name: "Started", notes: null, version: 0 },
      { id: 2, name: "Archived", notes: "done", version: 0 },
    ]);
    const uc = createManagementStatusUseCases(gateway);
    await expect(uc.list()).resolves.toEqual([
      { id: 1, name: "Started", notes: null, version: 0 },
      { id: 2, name: "Archived", notes: "done", version: 0 },
    ]);
  });

  it("searches, creates, updates, deletes and reports in-use", async () => {
    const gateway = fakeGateway([{ id: 1, name: "Started", version: 0 }]);
    gateway.inUseIds.add(1);
    const uc = createManagementStatusUseCases(gateway);

    await expect(uc.searchByName("start")).resolves.toHaveLength(1);
    await expect(uc.isInUse(1)).resolves.toBe(true);

    const created = await uc.create({ name: "In progress", notes: "" });
    expect(created.id).toBeGreaterThan(0);

    await expect(uc.update(created.id, { name: "Working", notes: "n", version: 1 })).resolves.toMatchObject({
      name: "Working",
      version: 1,
    });

    await uc.delete(created.id);
    await expect(uc.list()).resolves.toHaveLength(1);
  });
});
