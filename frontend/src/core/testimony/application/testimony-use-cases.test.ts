// @vitest-environment node
import { describe, expect, it } from "vitest";
import type { Testimony, TestimonyCreateInput, TestimonyUpdateInput } from "../domain/testimony";
import type { TestimonyGateway } from "./ports";
import { createTestimonyUseCases } from "./use-cases";

describe("testimony use cases (#1834)", () => {
  it("lists, generates, creates with deed ref, updates with version, deletes", async () => {
    const store: Testimony[] = [];
    let lastCreate: TestimonyCreateInput | undefined;
    let lastUpdate: TestimonyUpdateInput | undefined;
    const gateway: TestimonyGateway = {
      list: async () => [...store],
      generate: async (idDeed) => {
        const t = { idTestimony: 1, number: 10, deed: { idDeed }, version: 0 };
        store.push(t);
        return t;
      },
      create: async (input) => {
        lastCreate = input;
        const t = { idTestimony: 2, ...input, version: 0 };
        store.push(t);
        return t;
      },
      verify: async (id, input) => {
        const t = store.find((x) => x.idTestimony === id)!;
        Object.assign(t, input, { verified: true });
        return t;
      },
      update: async (id, input) => {
        lastUpdate = input;
        const t = store.find((x) => x.idTestimony === id)!;
        Object.assign(t, input);
      },
      delete: async (id) => {
        const i = store.findIndex((x) => x.idTestimony === id);
        if (i >= 0) store.splice(i, 1);
      },
      downloadCopy: async () => new Blob(["pdf"]),
    };
    const uc = createTestimonyUseCases(gateway);
    await uc.generate(99);
    await uc.create({
      deed: { idDeed: 5, number: 500 },
      number: 7,
      notes: "n",
      flagged: false,
      verified: false,
    });
    expect(lastCreate?.deed).toEqual({ idDeed: 5, number: 500 });
    await uc.update(2, { number: 7, notes: "x", flagged: false, verified: false, version: 0 });
    expect(lastUpdate?.version).toBe(0);
    await expect(uc.downloadCopy(1)).resolves.toBeInstanceOf(Blob);
    await uc.delete(1);
    await expect(uc.list()).resolves.toHaveLength(1);
  });
});
