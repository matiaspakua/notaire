// @vitest-environment node
/**
 * Core use cases for people (#1823 / ADR-030).
 */
import { describe, expect, it } from "vitest";
import type { Person } from "../domain/person";
import type { PersonGateway, PersonInput, PersonPage, PersonPageRequest, PersonSearchCriteria } from "./ports";
import { createPersonUseCases } from "./use-cases";

function fakeGateway(seed: Person[] = []): PersonGateway & { store: Person[] } {
  const store = [...seed];
  let nextId = Math.max(0, ...store.map((p) => p.id)) + 1;
  return {
    store,
    getById: async (id: number) => {
      const found = store.find((p) => p.id === id);
      if (!found) throw new Error(`missing ${id}`);
      return found;
    },
    listPage: async (request: PersonPageRequest): Promise<PersonPage> => {
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
    search: async (criteria: PersonSearchCriteria) =>
      store.filter((p) => {
        if (criteria.firstName && !(p.firstName ?? "").toLowerCase().includes(criteria.firstName.toLowerCase()))
          return false;
        if (criteria.lastName && !(p.lastName ?? "").toLowerCase().includes(criteria.lastName.toLowerCase()))
          return false;
        if (criteria.identificationNumber && p.identificationNumber !== criteria.identificationNumber) return false;
        if (criteria.isClient != null && p.isClient !== criteria.isClient) return false;
        return true;
      }),
    create: async (input: PersonInput) => {
      store.push({ id: nextId++, ...input, version: 0 });
    },
    update: async (id: number, input: PersonInput) => {
      const idx = store.findIndex((p) => p.id === id);
      if (idx < 0) throw new Error(`missing ${id}`);
      store[idx] = { ...store[idx], ...input };
    },
    delete: async (id: number) => {
      const idx = store.findIndex((p) => p.id === id);
      if (idx < 0) throw new Error(`missing ${id}`);
      store.splice(idx, 1);
    },
  };
}

describe("person use cases (#1823)", () => {
  it("lists a page and loads by id from the gateway", async () => {
    const gateway = fakeGateway([
      { id: 1, firstName: "Ana", lastName: "Latest" },
      { id: 2, firstName: "Bruno", lastName: "Recent" },
    ]);
    const uc = createPersonUseCases(gateway);
    await expect(uc.listPage({ page: 0, size: 1 })).resolves.toMatchObject({
      content: [{ id: 1, firstName: "Ana" }],
      totalElements: 2,
    });
    await expect(uc.getById(2)).resolves.toMatchObject({ id: 2, lastName: "Recent" });
  });

  it("searches, creates, updates and deletes", async () => {
    const gateway = fakeGateway([{ id: 1, firstName: "Ana", lastName: "Perez", isClient: true }]);
    const uc = createPersonUseCases(gateway);

    await expect(uc.search({ lastName: "per" })).resolves.toHaveLength(1);

    await uc.create({ firstName: "Carla", lastName: "Garcia" });
    expect(gateway.store).toHaveLength(2);

    await uc.update(1, { firstName: "Ana", lastName: "Perez", isClient: false });
    await expect(uc.getById(1)).resolves.toMatchObject({ isClient: false });

    await uc.delete(1);
    await expect(uc.listPage({ page: 0, size: 10 })).resolves.toMatchObject({ totalElements: 1 });
  });
});
