// @vitest-environment node
import { describe, expect, it } from "vitest";
import type { SubstitutionGateway } from "./ports";
import { createSubstitutionUseCases } from "./use-cases";

describe("createSubstitutionUseCases", () => {
  it("delegates CRUD", async () => {
    const calls: unknown[] = [];
    const gw: SubstitutionGateway = {
      list: async () => { calls.push(["list"]); return []; },
      create: async (input) => { calls.push(["create", input]); },
      update: async (id, input) => { calls.push(["update", id, input]); },
      delete: async (id) => { calls.push(["delete", id]); },
    };
    const uc = createSubstitutionUseCases(gw);
    await uc.list();
    await uc.create({});
    await uc.delete(1);
    expect(calls).toEqual([["list"], ["create", {}], ["delete", 1]]);
  });
});
