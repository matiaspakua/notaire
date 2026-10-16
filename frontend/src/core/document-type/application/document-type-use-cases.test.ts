// @vitest-environment node
/**
 * Core use cases for the document-type catalog (#1777 / ADR-030).
 * Fake gateway only — no React, Next, or api-client.
 */
import { describe, expect, it } from "vitest";
import type { DocumentType } from "../domain/document-type";
import type { DocumentTypeGateway, DocumentTypeInput } from "./ports";
import { createDocumentTypeUseCases } from "./use-cases";

function fakeGateway(seed: DocumentType[] = []): DocumentTypeGateway & {
  store: DocumentType[];
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
    create: async (input: DocumentTypeInput) => {
      const created: DocumentType = {
        id: nextId++,
        name: input.name,
        expires: input.expires,
        dueDays: input.dueDays,
        deliveredBy: input.deliveredBy,
        enabled: input.enabled,
        returned: input.returned,
      };
      store.push(created);
      return created;
    },
    update: async (id: number, input: DocumentTypeInput) => {
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

describe("document-type use cases (#1777)", () => {
  it("lists document types from the gateway", async () => {
    const gateway = fakeGateway([
      { id: 1, name: "ID card", enabled: true },
      { id: 2, name: "Deed", expires: true, dueDays: 30 },
    ]);
    const uc = createDocumentTypeUseCases(gateway);
    await expect(uc.list()).resolves.toEqual([
      { id: 1, name: "ID card", enabled: true },
      { id: 2, name: "Deed", expires: true, dueDays: 30 },
    ]);
  });

  it("searches, creates, updates, deletes and reports in-use", async () => {
    const gateway = fakeGateway([{ id: 1, name: "ID card", enabled: true }]);
    gateway.inUseIds.add(1);
    const uc = createDocumentTypeUseCases(gateway);

    await expect(uc.searchByName("card")).resolves.toHaveLength(1);
    await expect(uc.isInUse(1)).resolves.toBe(true);

    const created = await uc.create({ name: "Passport", enabled: true, returned: false });
    expect(created.id).toBeGreaterThan(0);

    await expect(
      uc.update(created.id, { name: "Passport copy", enabled: true, expires: true, dueDays: 7 }),
    ).resolves.toMatchObject({
      name: "Passport copy",
      expires: true,
      dueDays: 7,
    });

    await uc.delete(created.id);
    await expect(uc.list()).resolves.toHaveLength(1);
  });
});
