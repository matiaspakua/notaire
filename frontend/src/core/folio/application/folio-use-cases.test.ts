// @vitest-environment node
/**
 * Core use cases for folio instance admin (#1785 / ADR-030).
 * Fake gateway only — no React, Next, or api-client.
 */
import { describe, expect, it } from "vitest";
import type { AvailableNotary, Folio, FolioWriteInput } from "../domain/folio";
import type { FolioGateway } from "./ports";
import { createFolioUseCases } from "./use-cases";

function fakeGateway(seed: Folio[] = []): FolioGateway & {
  store: Folio[];
  notaries: AvailableNotary[];
} {
  const store = [...seed];
  const notaries: AvailableNotary[] = [{ personId: 10, firstName: "Ana", lastName: "Notary" }];
  let nextId = Math.max(0, ...store.map((f) => f.idFolio ?? 0)) + 1;
  return {
    store,
    notaries,
    list: async () => [...store],
    searchByStatus: async (status: string) => store.filter((f) => f.status === status),
    create: async (input: FolioWriteInput) => {
      store.push({
        idFolio: nextId++,
        number: input.number,
        year: input.year,
        status: input.status,
        notes: input.notes,
        fkIdFolioType: { idFolioType: input.typeFolioId },
        fkIdNotaryPerson: { personId: input.notaryId },
        fkIdDeed: input.deedId != null ? { idDeed: input.deedId } : undefined,
      });
    },
    update: async (id: number, input: FolioWriteInput) => {
      const idx = store.findIndex((f) => f.idFolio === id);
      if (idx < 0) throw new Error(`missing ${id}`);
      store[idx] = {
        ...store[idx],
        number: input.number,
        year: input.year,
        status: input.status,
        notes: input.notes,
        fkIdFolioType: { idFolioType: input.typeFolioId },
        fkIdNotaryPerson: { personId: input.notaryId },
        fkIdDeed: input.deedId != null ? { idDeed: input.deedId } : undefined,
      };
    },
    delete: async (id: number) => {
      const idx = store.findIndex((f) => f.idFolio === id);
      if (idx < 0) throw new Error(`missing ${id}`);
      store.splice(idx, 1);
    },
    listAvailableNotaries: async () => [...notaries],
  };
}

describe("folio use cases (#1785)", () => {
  it("lists and filters folios by status", async () => {
    const gateway = fakeGateway([
      { idFolio: 1, number: 1, status: "Nuevo" },
      { idFolio: 2, number: 2, status: "Utilizado" },
    ]);
    const uc = createFolioUseCases(gateway);
    await expect(uc.list()).resolves.toHaveLength(2);
    await expect(uc.searchByStatus("Nuevo")).resolves.toEqual([
      { idFolio: 1, number: 1, status: "Nuevo" },
    ]);
  });

  it("creates, updates, deletes and lists notaries", async () => {
    const gateway = fakeGateway([]);
    const uc = createFolioUseCases(gateway);
    const input: FolioWriteInput = {
      number: 42,
      year: 2026,
      status: "Nuevo",
      notes: "",
      typeFolioId: 1,
      notaryId: 10,
    };
    await uc.create(input);
    expect(gateway.store).toHaveLength(1);

    await uc.update(1, { ...input, status: "Utilizado", deedId: 7 });
    expect(gateway.store[0].status).toBe("Utilizado");
    expect(gateway.store[0].fkIdDeed?.idDeed).toBe(7);

    await expect(uc.listAvailableNotaries()).resolves.toEqual([
      { personId: 10, firstName: "Ana", lastName: "Notary" },
    ]);

    await uc.delete(1);
    await expect(uc.list()).resolves.toHaveLength(0);
  });
});
