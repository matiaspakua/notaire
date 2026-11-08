// @vitest-environment node
import { describe, expect, it } from "vitest";
import type { CopyGateway } from "./ports";
import { createCopyUseCases } from "./use-cases";

describe("createCopyUseCases", () => {
  it("delegates CRUD", async () => {
    const calls: unknown[] = [];
    const gw: CopyGateway = {
      list: async () => { calls.push(["list"]); return []; },
      create: async (input) => { calls.push(["create", input]); },
      update: async (id, input) => { calls.push(["update", id, input]); },
      delete: async (id) => { calls.push(["delete", id]); },
    };
    const uc = createCopyUseCases(gw);
    await uc.list();
    await uc.create({});
    await uc.delete(1);
    expect(calls).toEqual([["list"], ["create", {}], ["delete", 1]]);
  });
});
