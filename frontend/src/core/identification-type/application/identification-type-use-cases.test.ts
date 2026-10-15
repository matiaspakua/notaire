// @vitest-environment node
/**
 * Core use cases for the identification-type catalog (#1732 / ADR-030).
 * Fake gateway only — no React, Next, or api-client.
 */
import { describe, expect, it } from "vitest";
import type { IdentificationType } from "../domain/identification-type";
import {
  DEFAULT_IDENTIFICATION_TYPE,
  defaultIdentificationTypeId,
} from "../domain/identification-type";
import type { IdentificationTypeGateway, IdentificationTypeInput } from "./ports";
import { createIdentificationTypeUseCases } from "./use-cases";

function fakeGateway(seed: IdentificationType[] = []): IdentificationTypeGateway & { store: IdentificationType[] } {
  const store = [...seed];
  let nextId = Math.max(0, ...store.map((t) => t.id)) + 1;
  return {
    store,
    list: async () => [...store],
    create: async (input: IdentificationTypeInput) => {
      const created: IdentificationType = {
        id: nextId++,
        name: input.name,
        characters: input.characters,
        version: 0,
      };
      store.push(created);
      return created;
    },
    update: async (id: number, input: IdentificationTypeInput) => {
      const idx = store.findIndex((t) => t.id === id);
      if (idx < 0) throw new Error(`missing ${id}`);
      const updated = { ...store[idx], name: input.name, characters: input.characters };
      store[idx] = updated;
      return updated;
    },
    delete: async (id: number) => {
      const idx = store.findIndex((t) => t.id === id);
      if (idx < 0) throw new Error(`missing ${id}`);
      store.splice(idx, 1);
    },
  };
}

describe("identification-type use cases (#1732)", () => {
  it("lists types from the gateway", async () => {
    const gateway = fakeGateway([
      { id: 1, name: "DNI", characters: "8", version: 0 },
      { id: 5, name: "CUIT", characters: "11", version: 0 },
    ]);
    const uc = createIdentificationTypeUseCases(gateway);
    await expect(uc.list()).resolves.toEqual([
      { id: 1, name: "DNI", characters: "8", version: 0 },
      { id: 5, name: "CUIT", characters: "11", version: 0 },
    ]);
  });

  it("creates a type through the gateway", async () => {
    const gateway = fakeGateway();
    const uc = createIdentificationTypeUseCases(gateway);
    const created = await uc.create({ name: "Pasaporte", characters: "12" });
    expect(created).toMatchObject({ name: "Pasaporte", characters: "12" });
    expect(gateway.store).toHaveLength(1);
  });

  it("updates and deletes a type through the gateway", async () => {
    const gateway = fakeGateway([{ id: 3, name: "Old", characters: "9", version: 0 }]);
    const uc = createIdentificationTypeUseCases(gateway);
    await expect(uc.update(3, { name: "New", characters: "10" })).resolves.toMatchObject({
      id: 3,
      name: "New",
      characters: "10",
    });
    await uc.delete(3);
    expect(gateway.store).toHaveLength(0);
  });

  it("resolves the default identification type id (DNI, any letter case)", () => {
    expect(DEFAULT_IDENTIFICATION_TYPE).toBe("DNI");
    expect(
      defaultIdentificationTypeId([
        { id: 2, name: "CUIT", characters: "11" },
        { id: 1, name: "dni", characters: "8" },
      ]),
    ).toBe(1);
    expect(defaultIdentificationTypeId(undefined)).toBeUndefined();
  });
});
