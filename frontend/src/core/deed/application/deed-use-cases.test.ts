// @vitest-environment node
/**
 * Deed use cases delegate to the gateway port (#1831).
 */
import { describe, expect, it } from "vitest";
import type { Deed } from "../domain/deed";
import type { DeedGateway, DeedPage } from "./ports";
import { createDeedUseCases } from "./use-cases";

function fakeGateway(initial: Deed[] = []): DeedGateway & { store: Deed[] } {
  const store = [...initial];
  return {
    store,
    getById: async (id) => {
      const found = store.find((d) => d.idDeed === id);
      if (!found) throw new Error("not found");
      return found;
    },
    listPage: async (request): Promise<DeedPage> => {
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
    searchByNumber: async (number) => store.filter((d) => String(d.number) === number),
    create: async (input) => {
      store.push({ ...input, idDeed: store.length + 1 });
    },
    update: async (id, input) => {
      const idx = store.findIndex((d) => d.idDeed === id);
      if (idx >= 0) store[idx] = { ...store[idx], ...input };
    },
    delete: async (id) => {
      const idx = store.findIndex((d) => d.idDeed === id);
      if (idx >= 0) store.splice(idx, 1);
    },
    sign: async (id) => {
      const found = store.find((d) => d.idDeed === id);
      if (!found) throw new Error("not found");
      found.status = "Firmada";
      return found;
    },
  };
}

describe("deed use cases (#1831)", () => {
  it("lists a page and loads by id from the gateway", async () => {
    const gateway = fakeGateway([
      { idDeed: 1, number: 100, status: "Sin Firmar" },
      { idDeed: 2, number: 200, status: "Firmada" },
    ]);
    const uc = createDeedUseCases(gateway);
    await expect(uc.listPage({ page: 0, size: 1 })).resolves.toMatchObject({
      content: [{ idDeed: 1 }],
      totalElements: 2,
    });
    await expect(uc.getById(2)).resolves.toMatchObject({ idDeed: 2, number: 200 });
  });

  it("searches by number, creates, signs and deletes", async () => {
    const gateway = fakeGateway([{ idDeed: 1, number: 77, status: "Sin Firmar" }]);
    const uc = createDeedUseCases(gateway);

    await expect(uc.searchByNumber("77")).resolves.toHaveLength(1);
    await expect(uc.searchByNumber("99")).resolves.toHaveLength(0);

    await uc.create({ number: 88, status: "Sin Firmar", idFolio: 9 });
    expect(gateway.store).toHaveLength(2);
    expect(gateway.store[1]).toMatchObject({ number: 88, idFolio: 9 });

    await expect(uc.sign(1)).resolves.toMatchObject({ status: "Firmada" });

    await uc.delete(1);
    await expect(uc.listPage({ page: 0, size: 10 })).resolves.toMatchObject({ totalElements: 1 });
  });
});
